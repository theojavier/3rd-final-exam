package com.example.a3rd.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.a3rd.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ScheduleFragment extends Fragment {

    private static final String TAG = "ScheduleDebug";

    private TableLayout tableLayout;
    private FirebaseFirestore firestore;

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_schedule, container, false);

        tableLayout = view.findViewById(R.id.schedule_table);
        firestore = FirebaseFirestore.getInstance();

        if (tableLayout == null) {
            Log.e(TAG, "schedule_table view not found in layout!");
            return view;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String currentStudentId = prefs.getString("studentId", null);

        if (currentStudentId != null) {
            loadStudentData(currentStudentId);
        } else {
            Log.e(TAG, "No studentId found in SharedPreferences → nothing to load");
            Toast.makeText(requireContext(), "Not logged in (no studentId)", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadStudentData(String studentId) {
        firestore.collection("users")
                .whereEqualTo("studentId", studentId)
                .limit(1)
                .get()
                .addOnSuccessListener(userQuery -> {
                    if (!userQuery.isEmpty()) {
                        DocumentSnapshot userDoc = userQuery.getDocuments().get(0);
                        String program = userDoc.getString("program");
                        String yearBlock = userDoc.getString("yearBlock");

                        Log.d(TAG, "Student program=" + program + " yearBlock=" + yearBlock);
                        Toast.makeText(requireContext(),
                                "Program: " + program + " | YearBlock: " + yearBlock,
                                Toast.LENGTH_LONG).show();

                        loadExamsForWeek(program, yearBlock);
                    } else {
                        Log.w(TAG, "No matching user found for studentId=" + studentId);
                        Toast.makeText(requireContext(),
                                "No matching user found for studentId=" + studentId,
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user data", e);
                    Toast.makeText(requireContext(),
                            "Error loading user data: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void loadExamsForWeek(String program, String yearBlock) {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date weekStart = cal.getTime();

        cal.add(Calendar.DAY_OF_WEEK, 7);
        Date weekEnd = cal.getTime();

        Log.d(TAG, "Querying exams for " + program + "/" + yearBlock + " between " + weekStart + " & " + weekEnd);

        clearScheduleCells();

        firestore.collection("exams")
                .whereEqualTo("program", program)
                .whereEqualTo("yearBlock", yearBlock)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        Log.w(TAG, "No exams for program/yearBlock");
                        Toast.makeText(requireContext(),
                                "No exams this week for " + program + " - " + yearBlock,
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    int placed = 0;
                    for (DocumentSnapshot doc : querySnapshot) {
                        Timestamp startTS = doc.getTimestamp("startTime");
                        Timestamp endTS = doc.getTimestamp("endTime");
                        String subject = doc.getString("subject");

                        if (startTS == null || endTS == null || subject == null) {
                            continue;
                        }

                        Date start = startTS.toDate();
                        Date end = endTS.toDate();

                        if (!start.before(weekStart) && start.before(weekEnd)) {
                            placeExamOnSchedule(subject, start, end);
                            placed++;
                        }
                    }

                    if (placed == 0) {
                        Toast.makeText(requireContext(),
                                "No exams scheduled this week for your program/yearBlock.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching exams", e);
                    Toast.makeText(requireContext(),
                            "Error fetching exams: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void clearScheduleCells() {
        if (tableLayout == null) return;

        for (int r = 1; r < tableLayout.getChildCount(); r++) {
            View rowView = tableLayout.getChildAt(r);
            if (!(rowView instanceof TableRow)) continue;
            TableRow row = (TableRow) rowView;

            for (int c = 1; c < row.getChildCount(); c++) {
                View cell = row.getChildAt(c);
                if (cell instanceof TextView) {
                    ((TextView) cell).setText(""); // placeholder
                    cell.setBackgroundResource(R.drawable.exam_cell_background); // custom border
                }
            }
        }
    }

    private void placeExamOnSchedule(String subject, Date start, Date end) {
        if (tableLayout == null) return;

        Calendar cal = Calendar.getInstance();
        cal.setTime(start);

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int colIndex = -1;
        switch (dayOfWeek) {
            case Calendar.MONDAY: colIndex = 1; break;
            case Calendar.TUESDAY: colIndex = 2; break;
            case Calendar.WEDNESDAY: colIndex = 3; break;
            case Calendar.THURSDAY: colIndex = 4; break;
            case Calendar.FRIDAY: colIndex = 5; break;
            case Calendar.SATURDAY: colIndex = 6; break;
            case Calendar.SUNDAY: colIndex = 7; break;
        }
        if (colIndex == -1) return;

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(start);
        int startHour = startCal.get(Calendar.HOUR_OF_DAY);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(end);
        int endHour = endCal.get(Calendar.HOUR_OF_DAY);

        int allowedStart = Math.max(startHour, 7);
        int allowedEnd = Math.min(endHour, 19);

        if (allowedEnd <= allowedStart) return;

        String timeRange = timeFormat.format(start) + " – " + timeFormat.format(end);

        for (int hour = allowedStart; hour < allowedEnd; hour++) {
            fillSlot(hour, colIndex, subject, timeRange);
        }
    }

    private void fillSlot(int hour, int colIndex, String subject, String timeRange) {
        if (tableLayout == null) return;

        int rowIndex = (hour - 7) + 1; // 7AM -> row 1

        if (rowIndex < 1 || rowIndex >= tableLayout.getChildCount()) return;

        TableRow row = (TableRow) tableLayout.getChildAt(rowIndex);
        if (row == null) return;

        if (colIndex < 0 || colIndex >= row.getChildCount()) return;

        View child = row.getChildAt(colIndex);
        if (!(child instanceof TextView)) return;

        TextView cell = (TextView) child;
        String existing = cell.getText() != null ? cell.getText().toString().trim() : "";

        String toSet = existing.equals("—") || existing.isEmpty()
                ? (subject + "\n" + timeRange)
                : (existing + "\n\n" + subject + "\n" + timeRange);

        cell.setText(toSet);
        cell.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200));
        cell.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
    }
}

