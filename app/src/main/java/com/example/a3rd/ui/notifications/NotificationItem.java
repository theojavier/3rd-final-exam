package com.example.a3rd.ui.notifications;

public class NotificationItem {
    private String examId;
    private String title;
    private String message;
    private boolean viewed;

    public NotificationItem(String examId, String title, String message, boolean viewed) {
        this.examId = examId;
        this.title = title;
        this.message = message;
        this.viewed = viewed;
    }

    public String getExamId() {
        return examId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isViewed() {
        return viewed;
    }
}

