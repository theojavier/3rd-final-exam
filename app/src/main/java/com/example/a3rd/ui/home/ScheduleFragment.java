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

    private TableLayout tableLayout;
    private FirebaseFirestore firestore;

    // 🔹 Match your XML hours (7AM – 7PM)
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

        loadExamsForWeek();

        return view;
    }

    private void loadExamsForWeek() {
        firestore.collection("exams")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            String subject = doc.getString("subject");
                            Timestamp startTS = doc.getTimestamp("startTime");
                            Timestamp endTS = doc.getTimestamp("endTime");

                            if (subject != null && startTS != null && endTS != null) {
                                Date startDate = startTS.toDate();
                                Date endDate = endTS.toDate();

                                if (isInCurrentWeek(startDate)) {
                                    placeExamOnSchedule(subject, startDate, endDate);
                                }
                            }
                        }
                    } else {
                        Log.e("Schedule", "Error loading exams", task.getException());
                    }
                });
    }

    // 🔹 Check if date belongs to this week (Mon–Sun)
    private boolean isInCurrentWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        // Reset time part
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // Start of week (Monday)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date weekStart = cal.getTime();

        // End of week (Sunday)
        cal.add(Calendar.DAY_OF_WEEK, 6);
        Date weekEnd = cal.getTime();

        return !date.before(weekStart) && !date.after(weekEnd);
    }

    private void placeExamOnSchedule(String subject, Date start, Date end) {
        Calendar cal = Calendar.getInstance();

        // Get day of week (Mon=1 … Sun=7 for our table)
        cal.setTime(start);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int colIndex;
        switch (dayOfWeek) {
            case Calendar.MONDAY: colIndex = 1; break;
            case Calendar.TUESDAY: colIndex = 2; break;
            case Calendar.WEDNESDAY: colIndex = 3; break;
            case Calendar.THURSDAY: colIndex = 4; break;
            case Calendar.FRIDAY: colIndex = 5; break;
            case Calendar.SATURDAY: colIndex = 6; break;
            case Calendar.SUNDAY: colIndex = 7; break;
            default: colIndex = -1;
        }

        if (colIndex == -1) return;

        // Hours
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(start);
        int startHour = startCal.get(Calendar.HOUR_OF_DAY);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(end);
        int endHour = endCal.get(Calendar.HOUR_OF_DAY);

        String timeRange = timeFormat.format(start) + " – " + timeFormat.format(end);

        // Example: 7–9 → fill 7–8 and 8–9
        for (int hour = startHour; hour < endHour; hour++) {
            fillSlot(hour, colIndex, subject, timeRange);
        }
    }

    private void fillSlot(int hour, int colIndex, String subject, String timeRange) {
        // Find row by hour (7AM=Row1, 8AM=Row2, etc.)
        int rowIndex = hour - 7 + 1; // +1 because row0 = header

        if (rowIndex < 1 || rowIndex >= tableLayout.getChildCount()) return;

        TableRow row = (TableRow) tableLayout.getChildAt(rowIndex);

        if (row != null && row.getChildCount() > colIndex) {
            TextView cell = (TextView) row.getChildAt(colIndex);
            cell.setText(subject + "\n" + timeRange);
            cell.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200));
            cell.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        }
    }
}