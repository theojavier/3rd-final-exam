package com.example.a3rd.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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

    // Match your XML hours (7AM – 7PM)
    private final String[] timeSlots = {
            "7:00 – 8:00 AM", "8:00 – 9:00 AM", "9:00 – 10:00 AM", "10:00 – 11:00 AM",
            "11:00 – 12:00 PM", "12:00 – 1:00 PM", "1:00 – 2:00 PM", "2:00 – 3:00 PM",
            "3:00 – 4:00 PM", "4:00 – 5:00 PM", "5:00 – 6:00 PM", "6:00 – 7:00 PM"
    };

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

        loadExamsForWeek();

        return view;
    }

    private void loadExamsForWeek() {
        firestore.collection("exams")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "Loaded " + task.getResult().size() + " exam docs");
                        for (DocumentSnapshot doc : task.getResult()) {
                            String docId = doc.getId();
                            String subject = doc.getString("subject");
                            Timestamp startTS = doc.getTimestamp("startTime");
                            Timestamp endTS = doc.getTimestamp("endTime");

                            Log.d(TAG, "Doc: " + docId + " subject=" + subject + " startTS=" + startTS + " endTS=" + endTS);

                            if (subject == null || startTS == null || endTS == null) {
                                Log.w(TAG, "Skipping doc " + docId + " (missing subject/startTime/endTime)");
                                continue;
                            }

                            Date startDate = startTS.toDate();
                            Date endDate = endTS.toDate();

                            // Optionally keep filter for current week. For testing you may disable this.
                             if (!isInCurrentWeek(startDate))
                             { Log.d(TAG, "Skipping (not current week): " + docId);
                                 continue;
                             }

                            // CALL the placement function (fixed — you were not calling this)

                            placeExamOnSchedule(subject, startDate, endDate);
                        }
                    } else {
                        Log.e(TAG, "Error loading exams", task.getException());
                    }
                });
    }

    // optional helper — you can enable/disable checking
    private boolean isInCurrentWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // Monday
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date weekStart = cal.getTime();

        // Sunday
        cal.add(Calendar.DAY_OF_WEEK, 6);
        Date weekEnd = cal.getTime();

        return !date.before(weekStart) && !date.after(weekEnd);
    }

    private void placeExamOnSchedule(String subject, Date start, Date end) {
        if (tableLayout == null) return;

        Calendar cal = Calendar.getInstance();
        cal.setTime(start);

        // Map day-of-week -> column (Time column = 0, Mon = 1, Tue = 2 ... Sun = 7)
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
        if (colIndex == -1) {
            Log.w(TAG, "Invalid dayOfWeek for subject=" + subject);
            return;
        }

        // Hours (24-hour)
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(start);
        int startHour = startCal.get(Calendar.HOUR_OF_DAY);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(end);
        int endHour = endCal.get(Calendar.HOUR_OF_DAY);

        // clamp to your schedule window (7..19)
        int allowedStart = Math.max(startHour, 7);
        int allowedEnd = Math.min(endHour, 19); // end hour e.g., 19 means last slot 18->19 is allowed

        if (allowedEnd <= allowedStart) {
            Log.w(TAG, "Exam time outside visible schedule for subject=" + subject + " start=" + start + " end=" + end);
            return;
        }

        String timeRange = timeFormat.format(start) + " – " + timeFormat.format(end);

        Log.d(TAG, "Placing " + subject + " on col=" + colIndex + " hours=" + allowedStart + "-" + allowedEnd + " (" + timeRange + ")");

        for (int hour = allowedStart; hour < allowedEnd; hour++) {
            fillSlot(hour, colIndex, subject, timeRange);
        }
    }

    /**
     * hour = HOUR_OF_DAY (24h). Converts to TableRow index then sets text.
     * rowIndex computation: header row = 0, 7AM row = 1, 8AM = 2, ... 6PM row = 12
     */
    private void fillSlot(int hour, int colIndex, String subject, String timeRange) {
        if (tableLayout == null) return;

        int rowIndex = (hour - 7) + 1; // 7 -> 1, 8 -> 2, ..., 18 -> 12

        if (rowIndex < 1 || rowIndex >= tableLayout.getChildCount()) {
            Log.w(TAG, "Row index out of bounds for subject=" + subject + " rowIndex=" + rowIndex + " tableRows=" + tableLayout.getChildCount());
            return;
        }

        TableRow row = (TableRow) tableLayout.getChildAt(rowIndex);
        if (row == null) {
            Log.w(TAG, "Row is null at index " + rowIndex);
            return;
        }

        if (colIndex < 0 || colIndex >= row.getChildCount()) {
            Log.w(TAG, "Column index out of bounds for subject=" + subject + " colIndex=" + colIndex + " cols=" + row.getChildCount());
            return;
        }

        View child = row.getChildAt(colIndex);
        if (!(child instanceof TextView)) {
            Log.w(TAG, "Cell is not a TextView at r=" + rowIndex + " c=" + colIndex);
            return;
        }

        TextView cell = (TextView) child;
        String existing = cell.getText() != null ? cell.getText().toString().trim() : "";

        // append if already has text (prevents overwriting)
        String toSet = existing.isEmpty() ? (subject + "\n" + timeRange) : (existing + "\n\n" + subject + "\n" + timeRange);

        cell.setText(toSet);
        cell.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200));
        cell.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));

        Log.d(TAG, "Filled cell r=" + rowIndex + " c=" + colIndex + " -> " + subject);
    }
}
