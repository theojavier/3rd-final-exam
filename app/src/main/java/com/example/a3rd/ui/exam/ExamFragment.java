package com.example.a3rd.ui.exam;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.a3rd.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.*;

public class ExamFragment extends Fragment {

    private TextView tvExamTitle, tvQuestion;
    private RadioGroup radioGroupOptions, radioGroupTrueFalse;
    private Spinner spinnerMatching;
    private Button btnNextQuestion;

    private FirebaseFirestore db;
    private List<QuestionModel> questionList;
    private List<String> matchingPool;
    private int currentQuestionIndex = 0;

    private Map<String, Object> studentAnswers = new HashMap<>();
    private int score = 0; // auto-scored as student answers questions

    public ExamFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.exam, container, false);

        // Initialize views
        tvExamTitle = view.findViewById(R.id.tv_exam_title);
        tvQuestion = view.findViewById(R.id.tv_question);
        radioGroupOptions = view.findViewById(R.id.radio_group_options);
        radioGroupTrueFalse = view.findViewById(R.id.radio_group_true_false);
        spinnerMatching = view.findViewById(R.id.spinner_matching);
        btnNextQuestion = view.findViewById(R.id.btn_next_question);

        db = FirebaseFirestore.getInstance();
        questionList = new ArrayList<>();
        matchingPool = new ArrayList<>();

        loadExamAndQuestions();

        btnNextQuestion.setOnClickListener(v -> showNextQuestion(v));

        return view;
    }

    private void loadExamAndQuestions() {
        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String examId = prefs.getString("examId", "sampleExam");

        db.collection("exams").document(examId)
                .get()
                .addOnSuccessListener(examDoc -> {
                    if (examDoc.exists()) {
                        String examTitle = examDoc.getString("examTitle");
                        tvExamTitle.setText(examTitle);

                        db.collection("exams").document(examId)
                                .collection("questions")
                                .get()
                                .addOnSuccessListener(query -> {
                                    questionList.clear();
                                    matchingPool.clear();

                                    for (QueryDocumentSnapshot doc : query) {
                                        QuestionModel q = new QuestionModel();
                                        q.questionText = doc.getString("questionText");
                                        q.type = doc.getString("type");

                                        List<String> optionsFromDoc = null;
                                        Object o = doc.get("options");
                                        if (o instanceof List) {
                                            optionsFromDoc = (List<String>) o;
                                        }

                                        List<String> finalOptions;
                                        if ("matching".equalsIgnoreCase(q.type)
                                                && (optionsFromDoc == null || optionsFromDoc.isEmpty())) {
                                            finalOptions = matchingPool.isEmpty() ? Collections.emptyList() : new ArrayList<>(matchingPool);
                                        } else {
                                            finalOptions = optionsFromDoc;
                                        }

                                        q.options = finalOptions;

                                        if ("matching".equalsIgnoreCase(q.type) && finalOptions != null) {
                                            matchingPool.addAll(finalOptions);
                                        }

                                        q.correctAnswer = doc.get("correctAnswer");
                                        questionList.add(q);
                                    }

                                    if (!questionList.isEmpty()) {
                                        currentQuestionIndex = 0;
                                        showQuestion(questionList.get(currentQuestionIndex));
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("ExamFragment", "Error loading questions", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("ExamFragment", "Error loading exam", e));
    }

    private void showQuestion(QuestionModel q) {
        tvQuestion.setText(q.questionText);

        radioGroupOptions.setVisibility(View.GONE);
        radioGroupTrueFalse.setVisibility(View.GONE);
        spinnerMatching.setVisibility(View.GONE);

        if ("multiple-choice".equalsIgnoreCase(q.type) && q.options != null) {
            radioGroupOptions.setVisibility(View.VISIBLE);
            radioGroupOptions.removeAllViews();
            for (String option : q.options) {
                RadioButton rb = new RadioButton(getContext());
                rb.setText(option);
                radioGroupOptions.addView(rb);
            }
        } else if ("true-false".equalsIgnoreCase(q.type)) {
            radioGroupTrueFalse.setVisibility(View.VISIBLE);
        } else if ("matching".equalsIgnoreCase(q.type)) {
            spinnerMatching.setVisibility(View.VISIBLE);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_spinner_item,
                    q.options != null ? q.options : matchingPool
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerMatching.setAdapter(adapter);
        }
    }

    private void saveAnswer(QuestionModel q) {
        String answer = null;

        if ("multiple-choice".equalsIgnoreCase(q.type)) {
            int selectedId = radioGroupOptions.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selected = radioGroupOptions.findViewById(selectedId);
                answer = selected.getText().toString();
            }
        } else if ("true-false".equalsIgnoreCase(q.type)) {
            int selectedId = radioGroupTrueFalse.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selected = radioGroupTrueFalse.findViewById(selectedId);
                answer = selected.getText().toString();
            }
        } else if ("matching".equalsIgnoreCase(q.type)) {
            if (spinnerMatching.getSelectedItem() != null) {
                answer = spinnerMatching.getSelectedItem().toString();
            }
        }

        if (answer != null) {
            studentAnswers.put(q.questionText, answer);

            // ✅ Auto-score: compare answer to correctAnswer
            if (q.correctAnswer != null) {
                if (q.correctAnswer instanceof List) {
                    if (((List<?>) q.correctAnswer).contains(answer)) {
                        score++;
                    }
                } else {
                    if (answer.equalsIgnoreCase(q.correctAnswer.toString())) {
                        score++;
                    }
                }
            }
        }
    }

    private void showNextQuestion(View view) {
        QuestionModel currentQ = questionList.get(currentQuestionIndex);
        saveAnswer(currentQ);

        if (currentQuestionIndex + 1 < questionList.size()) {
            currentQuestionIndex++;
            showQuestion(questionList.get(currentQuestionIndex));
        } else {
            // Submit when exam finishes
            SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            String studentId = prefs.getString("studentId", "unknown");
            String examId = prefs.getString("examId", "sampleExam");

            submitExam(examId, studentId, studentAnswers, score, view);
        }
    }

    private void submitExam(String examId, String studentId, Map<String, Object> answers, int score, View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> result = new HashMap<>();
        result.put("studentId", studentId);
        result.put("examId", examId);
        result.put("answers", answers);
        result.put("score", score);
        result.put("total", questionList.size());
        result.put("status", "completed");
        result.put("submittedAt", System.currentTimeMillis());

        db.collection("examResults")
                .document(studentId + "_" + examId)
                .set(result)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Exam submitted!", Toast.LENGTH_SHORT).show();

                    // ✅ Navigate to result fragment and pass score
                    Bundle bundle = new Bundle();
                    bundle.putInt("score", score);
                    bundle.putInt("total", questionList.size());
                    bundle.putSerializable("answers", new HashMap<>(answers));

                    Navigation.findNavController(view).navigate(R.id.action_examFragment_to_examResultFragment, bundle);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Submission failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    public static class QuestionModel {
        public String questionText;
        public String type;
        public List<String> options;
        public Object correctAnswer;
    }
}