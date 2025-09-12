package com.example.a3rd.models;

public class ExamHistory {
    private String subject;
    private String date;
    private String score;

    // ✅ Empty constructor (required by Firebase)
    public ExamHistory() {
    }

    // ✅ Full constructor (useful when adding manually)
    public ExamHistory(String subject, String date, String score) {
        this.subject = subject;
        this.date = date;
        this.score = score;
    }

    // ✅ Getters
    public String getSubject() { return subject; }
    public String getDate() { return date; }
    public String getScore() { return score; }

    // ✅ Optional Setters (useful if you need to update values later)
    public void setSubject(String subject) { this.subject = subject; }
    public void setDate(String date) { this.date = date; }
    public void setScore(String score) { this.score = score; }
}
