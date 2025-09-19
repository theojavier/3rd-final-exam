package com.example.a3rd.models;

import com.google.firebase.Timestamp;

public class ExamModel {
    private String subject;
    private String program;
    private String yearBlock;
    private String status;
    private String teacherId;
    private Timestamp createdAt;
    private Timestamp startTime;
    private Timestamp endTime;

    public ExamModel() {
        // Required empty constructor for Firestore
    }

    // Getters
    public String getSubject() { return subject; }
    public String getProgram() { return program; }
    public String getYearBlock() { return yearBlock; }
    public String getStatus() { return status; }
    public String getTeacherId() { return teacherId; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getStartTime() { return startTime; }
    public Timestamp getEndTime() { return endTime; }

    // Helpers for display
    public String getFormattedPostedDate() {
        if (createdAt != null) {
            return android.text.format.DateFormat.format("MMMM d, yyyy h:mm a", createdAt.toDate()).toString();
        }
        return "N/A";
    }

    public String getFormattedLoginTime() {
        if (startTime != null && endTime != null) {
            String start = android.text.format.DateFormat.format("EEEE, d MMM yyyy / h:mm a", startTime.toDate()).toString();
            String end = android.text.format.DateFormat.format("h:mm a", endTime.toDate()).toString();
            return start + " - " + end;
        }
        return "TBA";
    }
}