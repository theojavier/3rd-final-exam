package com.example.a3rd.models;

public class ExamModel {
    private String subject;
    private String loginTime;
    private String postedDate;
    private boolean done;

    public ExamModel() {
        // Firestore needs empty constructor
    }

    public ExamModel(String subject, String loginTime, String postedDate, boolean done) {
        this.subject = subject;
        this.loginTime = loginTime;
        this.postedDate = postedDate;
        this.done = done;
    }

    public String getSubject() { return subject; }
    public String getLoginTime() { return loginTime; }
    public String getPostedDate() { return postedDate; }
    public boolean isDone() { return done; }
}
