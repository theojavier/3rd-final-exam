package com.example.a3rd.ui.exam;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.a3rd.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.*;

public class ExamFragment extends Fragment {
    private static final String TAG = "ExamFragment";

    private TextView tvExamTitle, tvQuestion;
    private RadioGroup radioGroupOptions, radioGroupTrueFalse;
    private Spinner spinnerMatching;
    private Button btnNextQuestion;

    private FirebaseFirestore db;
    private List<QuestionModel> questionList;
    private Set<String> matchingPool;

    private int currentQuestionIndex = 0;
    private Map<String, Object> studentAnswers = new HashMap<>();
    private int score = 0;

    private String examId;
    private String studentId;
    private String subject;   // ✅ added field for subject
    private String teacherId;

    public ExamFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.exam, container, false);

        // Views
        tvExamTitle = view.findViewById(R.id.tv_exam_title);
        tvQuestion = view.findViewById(R.id.tv_question);
        radioGroupOptions = view.findViewById(R.id.radio_group_options);
        radioGroupTrueFalse = view.findViewById(R.id.radio_group_true_false);
        spinnerMatching = view.findViewById(R.id.spinner_matching);
        btnNextQuestion = view.findViewById(R.id.btn_next_question);

        btnNextQuestion.setEnabled(false);

        db = FirebaseFirestore.getInstance();
        questionList = new ArrayList<>();
        matchingPool = new LinkedHashSet<>();

        // examId from args or prefs
        Bundle args = getArguments();
        if (args != null && args.containsKey("examId")) {
            examId = args.getString("examId");
            Log.d(TAG, "examId from args = " + examId);
        }
        if (examId == null) {
            Context ctx = getContext();
            if (ctx != null) {
                SharedPreferences prefs = ctx.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                examId = prefs.getString("examId", null);
                Log.d(TAG, "examId from prefs = " + examId);
            }
        }

        // studentId from prefs
        Context ctx = getContext();
        if (ctx != null) {
            SharedPreferences prefs = ctx.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            studentId = prefs.getString("studentId", null);
            Log.d(TAG, "studentId from prefs = " + studentId);
        }

        if (examId == null || studentId == null) {
            tvExamTitle.setText("Missing exam or student");
            Toast.makeText(requireContext(), "Exam or student not set", Toast.LENGTH_SHORT).show();
            return view;
        }

        loadExamAndQuestions(examId);

        btnNextQuestion.setOnClickListener(this::onNextClicked);

        return view;
    }

    private void loadExamAndQuestions(@NonNull String examId) {
        db.collection("exams").document(examId)
                .get()
                .addOnSuccessListener(examDoc -> {
                    if (examDoc != null && examDoc.exists()) {
                        String examTitle = examDoc.getString("examTitle");
                        subject = examDoc.getString("subject");  // ✅ fetch subject
                        tvExamTitle.setText(examTitle != null ? examTitle : "Exam");
                        teacherId = examDoc.getString("teacherId");

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

                                        Object o = doc.get("options");
                                        if (o instanceof List<?>) {
                                            List<?> rawList = (List<?>) o;
                                            List<String> stringList = new ArrayList<>();
                                            for (Object item : rawList) {
                                                if (item != null) stringList.add(item.toString());
                                            }
                                            q.options = stringList;
                                        } else {
                                            q.options = null;
                                        }

                                        if ("matching".equalsIgnoreCase(q.type) && q.options != null) {
                                            matchingPool.addAll(q.options);
                                        }

                                        q.correctAnswer = doc.get("correctAnswer");
                                        questionList.add(q);
                                    }

                                    if (questionList.isEmpty()) {
                                        tvQuestion.setText("No questions in this exam.");
                                        Toast.makeText(requireContext(), "No questions found for this exam.", Toast.LENGTH_SHORT).show();
                                        btnNextQuestion.setEnabled(false);
                                    } else {
                                        currentQuestionIndex = 0;
                                        score = 0;
                                        studentAnswers.clear();
                                        btnNextQuestion.setEnabled(true);
                                        showQuestion(questionList.get(currentQuestionIndex));
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error loading questions", e);
                                    Toast.makeText(requireContext(), "Failed to load questions", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        tvExamTitle.setText("Exam not found");
                        Toast.makeText(requireContext(), "Exam not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading exam", e);
                    Toast.makeText(requireContext(), "Failed to load exam", Toast.LENGTH_SHORT).show();
                });
    }

    private void showQuestion(QuestionModel q) {
        if (q == null) {
            tvQuestion.setText("Question will appear here");
            return;
        }
        tvQuestion.setText(q.questionText != null ? q.questionText : "Question will appear here");

        radioGroupOptions.setVisibility(View.GONE);
        radioGroupTrueFalse.setVisibility(View.GONE);
        spinnerMatching.setVisibility(View.GONE);

        if ("multiple-choice".equalsIgnoreCase(q.type) && q.options != null && !q.options.isEmpty()) {
            radioGroupOptions.setVisibility(View.VISIBLE);
            radioGroupOptions.removeAllViews();
            radioGroupOptions.clearCheck();

            for (String option : q.options) {
                RadioButton rb = new RadioButton(requireContext());
                rb.setId(View.generateViewId());
                rb.setText(option);
                radioGroupOptions.addView(rb);
            }

        } else if ("true-false".equalsIgnoreCase(q.type)) {
            radioGroupTrueFalse.setVisibility(View.VISIBLE);
            radioGroupTrueFalse.removeAllViews();
            radioGroupTrueFalse.clearCheck();

            RadioButton rbTrue = new RadioButton(requireContext());
            rbTrue.setId(View.generateViewId());
            rbTrue.setText("True");
            radioGroupTrueFalse.addView(rbTrue);

            RadioButton rbFalse = new RadioButton(requireContext());
            rbFalse.setId(View.generateViewId());
            rbFalse.setText("False");
            radioGroupTrueFalse.addView(rbFalse);

        } else if ("matching".equalsIgnoreCase(q.type)) {
            spinnerMatching.setVisibility(View.VISIBLE);

            List<String> pool = new ArrayList<>(matchingPool);
            if (pool.isEmpty() && q.options != null) pool.addAll(q.options);

            Collections.shuffle(pool);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    pool
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerMatching.setAdapter(adapter);

            spinnerMatching.setEnabled(!pool.isEmpty());
        } else {
            tvQuestion.setText(q.questionText != null ? q.questionText : "Unsupported question type");
        }
    }

    private void saveAnswer(QuestionModel q) {
        if (q == null) return;

        final int qIndex = currentQuestionIndex;

        String answer = null;
        db.collection("examResults")
                .document(examId)
                .set(Collections.singletonMap("teacherId", teacherId), SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Ensured root examResult doc has teacherId"))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to set teacherId on root examResult doc", e));

        if ("multiple-choice".equalsIgnoreCase(q.type)) {
            int selectedId = radioGroupOptions.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton rb = radioGroupOptions.findViewById(selectedId);
                if (rb != null) answer = rb.getText().toString();
            }

        } else if ("true-false".equalsIgnoreCase(q.type)) {
            int selectedId = radioGroupTrueFalse.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton rb = radioGroupTrueFalse.findViewById(selectedId);
                if (rb != null) answer = rb.getText().toString();
            }

        } else if ("matching".equalsIgnoreCase(q.type)) {
            if (spinnerMatching.getSelectedItemPosition() < 0 && spinnerMatching.getAdapter() != null && spinnerMatching.getAdapter().getCount() > 0) {
                spinnerMatching.setSelection(0);
            }
            Object sel = spinnerMatching.getSelectedItem();
            if (sel != null) answer = sel.toString();
        }

        String key = (q.questionText != null ? q.questionText : "q" + qIndex) + " (q" + qIndex + ")";
        studentAnswers.put(key, answer != null ? answer : "");

        if (answer != null && q.correctAnswer != null) {
            if (q.correctAnswer instanceof List) {
                if (((List<?>) q.correctAnswer).contains(answer)) score++;
            } else {
                if (answer.equalsIgnoreCase(q.correctAnswer.toString())) score++;
            }
        }

        if (examId != null && studentId != null) {
            final String answerDocId = "q" + qIndex;
            Map<String, Object> answerData = new HashMap<>();
            answerData.put("question", q.questionText);
            answerData.put("answer", answer != null ? answer : "");
            answerData.put("index", qIndex);
            answerData.put("timestamp", System.currentTimeMillis());

            DocumentReference resultDocRef = db.collection("examResults")
                    .document(examId)
                    .collection(studentId)
                    .document("result");

            Map<String, Object> inProgressUpdate = new HashMap<>();
            inProgressUpdate.put("examId", examId);
            inProgressUpdate.put("studentId", studentId);
            inProgressUpdate.put("status", "in-progress");
            inProgressUpdate.put("lastSavedAt", com.google.firebase.Timestamp.now());

            resultDocRef.set(inProgressUpdate, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        resultDocRef.collection("answers")
                                .document(answerDocId)
                                .set(answerData)
                                .addOnSuccessListener(aVoid2 -> Log.d(TAG, "Auto-saved answer " + answerDocId))
                                .addOnFailureListener(e -> Log.w(TAG, "Auto-save answer failed", e));
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Ensure result doc failed", e));
        } else {
            Log.w(TAG, "saveAnswer: examId or studentId null");
        }
    }

    private void onNextClicked(View view) {
        if (questionList.isEmpty()) {
            Toast.makeText(requireContext(), "No questions to answer", Toast.LENGTH_SHORT).show();
            return;
        }

        QuestionModel currentQ = questionList.get(currentQuestionIndex);
        saveAnswer(currentQ);

        if (currentQuestionIndex + 1 < questionList.size()) {
            currentQuestionIndex++;
            showQuestion(questionList.get(currentQuestionIndex));
        } else {
            submitExam(examId, studentId, score, view);
        }
    }

    private void submitExam(String examId, String studentId, int score, View view) {
        if (examId == null || studentId == null) {
            Toast.makeText(requireContext(), "Missing exam or student info", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("studentId", studentId);
        result.put("examId", examId);
        result.put("score", score);
        result.put("total", questionList.size());
        result.put("subject", subject != null ? subject : ""); // ✅ save subject
        result.put("status", "completed");
        result.put("submittedAt", Timestamp.now());

        db.collection("examResults")
                .document(examId)
                .collection(studentId)
                .document("result")
                .set(result)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Exam submitted!", Toast.LENGTH_SHORT).show();

                    Bundle bundle = new Bundle();
                    bundle.putString("examId", examId);
                    bundle.putString("studentId", studentId);
                    bundle.putInt("score", score);
                    bundle.putInt("total", questionList.size());
                    bundle.putString("subject", subject); // ✅ pass subject to result screen
                    bundle.putSerializable("answers", new HashMap<>(studentAnswers));

                    Navigation.findNavController(view)
                            .navigate(R.id.action_examFragment_to_examResultFragment, bundle);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Submission failed", e);
                    Toast.makeText(requireContext(), "Submission failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    public static class QuestionModel {
        public String questionText;
        public String type;
        public List<String> options;
        public Object correctAnswer;
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Navigation.findNavController(requireView()).navigate(R.id.nav_exam_item_page);
                    }
                }
        );
    }
}
