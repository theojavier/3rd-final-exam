package com.example.a3rd.models;

import com.google.firebase.Timestamp;
import android.text.format.DateFormat;

import java.util.Date;

public class ExamModel {
    private String id;
    private String subject;
    private String teacherId;
    private Timestamp startTime;
    private Timestamp endTime;
    private String status;

    public ExamModel() {}

    public ExamModel(String id, String subject, String teacherId, Timestamp startTime, Timestamp endTime, String status) {
        this.id = id;
        this.subject = subject;
        this.teacherId = teacherId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    // ✅ Getter and Setter for ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    // ✅ Helper methods for formatted text
    public String getFormattedLoginTime() {
        if (startTime == null) return "N/A";
        Date date = startTime.toDate();
        return DateFormat.format("MMM d, yyyy h:mm a", date).toString();
    }

    public String getFormattedPostedDate() {
        if (endTime == null) return "N/A";
        Date date = endTime.toDate();
        return DateFormat.format("MMM d, yyyy h:mm a", date).toString();
    }

    // ✅ Time checks
    public boolean isExamNotStarted() {
        return System.currentTimeMillis() < (startTime != null ? startTime.toDate().getTime() : 0);
    }

    public boolean isExamEnded() {
        return System.currentTimeMillis() > (endTime != null ? endTime.toDate().getTime() : Long.MAX_VALUE);
    }

    public boolean isExamActive() {
        long now = System.currentTimeMillis();
        long start = startTime != null ? startTime.toDate().getTime() : 0;
        long end = endTime != null ? endTime.toDate().getTime() : Long.MAX_VALUE;
        return now >= start && now <= end;
    }
}