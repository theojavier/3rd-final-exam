package com.example.a3rd.models;

public class ExamHistory {
    private String examId;  // 👈 add this
    private String subject;
    private String date;
    private String score;

    // ✅ Empty constructor (required by Firebase)
    public ExamHistory() {
    }

    // ✅ Full constructor (with examId)
    public ExamHistory(String examId, String subject, String date, String score) {
        this.examId = examId;
        this.subject = subject;
        this.date = date;
        this.score = score;
    }

    // ✅ Getters
    public String getExamId() { return examId; }
    public String getSubject() { return subject; }
    public String getDate() { return date; }
    public String getScore() { return score; }

    // ✅ Setters
    public void setExamId(String examId) { this.examId = examId; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setDate(String date) { this.date = date; }
    public void setScore(String score) { this.score = score; }
}