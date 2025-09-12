package com.example.a3rd.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.a3rd.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ScheduleFragment extends Fragment {

    private TableLayout tableLayout;
    private FirebaseFirestore firestore;

    // 🔹 Match the hours you put in XML (7AM – 7PM)
    private final String[] timeSlots = {
            "7:00 – 8:00 AM", "8:00 – 9:00 AM", "9:00 – 10:00 AM", "10:00 – 11:00 AM",
            "11:00 – 12:00 PM", "12:00 – 1:00 PM", "1:00 – 2:00 PM", "2:00 – 3:00 PM",
            "3:00 – 4:00 PM", "4:00 – 5:00 PM", "5:00 – 6:00 PM", "6:00 – 7:00 PM"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_schedule, container, false);

        tableLayout = view.findViewById(R.id.schedule_table); // 👈 set id in XML for TableLayout
        firestore = FirebaseFirestore.getInstance();

        loadExamsForWeek();

        return view;
    }

    private void loadExamsForWeek() {
        // Example Firestore collection: "exams"
        firestore.collection("exams")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            String subject = doc.getString("subject");
                            String day = doc.getString("day"); // e.g., "Mon", "Tue"
                            String startTime = doc.getString("startTime"); // e.g., "07:00"
                            String endTime = doc.getString("endTime");     // e.g., "09:00"

                            if (subject != null && day != null && startTime != null && endTime != null) {
                                placeExamOnSchedule(subject, day, startTime, endTime);
                            }
                        }
                    } else {
                        Log.e("Schedule", "Error loading exams", task.getException());
                    }
                });
    }

    private void placeExamOnSchedule(String subject, String day, String start, String end) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date startTime = sdf.parse(start);
            Date endTime = sdf.parse(end);

            Calendar cal = Calendar.getInstance();
            cal.setTime(startTime);
            int startHour = cal.get(Calendar.HOUR_OF_DAY);

            cal.setTime(endTime);
            int endHour = cal.get(Calendar.HOUR_OF_DAY);

            // 🔹 Example: 7:00–9:00 → fills 7–8 and 8–9
            for (int hour = startHour; hour < endHour; hour++) {
                fillSlot(hour, day, subject);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void fillSlot(int hour, String day, String subject) {
        // Map day string → column index (0 = Time, 1 = Mon, 2 = Tue, etc.)
        int colIndex = -1;
        switch (day) {
            case "Mon":
                colIndex = 1;
                break;
            case "Tue":
                colIndex = 2;
                break;
            case "Wed":
                colIndex = 3;
                break;
            case "Thu":
                colIndex = 4;
                break;
            case "Fri":
                colIndex = 5;
                break;
            case "Sat":
                colIndex = 6;
                break;
            case "Sun":
                colIndex = 7;
                break;
        }

        if (colIndex == -1) return; // invalid day, do nothing

        // Now find the correct row by `hour`
        TableRow row = (TableRow) tableLayout.getChildAt(hour - 7 + 1);
        // +1 because row 0 is header

        if (row != null && row.getChildCount() > colIndex) {
            TextView cell = (TextView) row.getChildAt(colIndex);
            cell.setText(subject);
            cell.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200));
            cell.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        }
    }
}