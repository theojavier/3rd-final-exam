package com.example.a3rd.models;
import android.text.format.DateFormat;

import com.google.firebase.Timestamp;

import java.util.Date;

public class ExamHistoryModel {
    private String id;         // not in Firestore, we set manually
    private String examId;
    private String studentId;
    private String status;
    private String subject;

    private long score;
    private long total;
    private Timestamp submittedAt;

    public ExamHistoryModel() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Timestamp getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Timestamp submittedAt) { this.submittedAt = submittedAt; }

    public long getScore() { return score; }
    public void setScore(long score) { this.score = score; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }


}
