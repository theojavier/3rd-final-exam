package com.example.a3rd.models;

public class ExamHistory {
    private String examId;
    private String subject;
    private String date;
    private String score;
    private String status;

    public ExamHistory() { }

    public ExamHistory(String examId, String subject, String date, String score, String status) {
        this.examId = examId;
        this.subject = subject;
        this.date = date;
        this.score = score;
        this.status = status;
    }

    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getScore() { return score; }
    public void setScore(String score) { this.score = score; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}