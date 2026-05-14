package cat.model;

import java.util.Date;

public class Segment {

    private int id;
    private int projectId;
    private String languagePair;
    private String sourceText;
    private String targetText;
    private String status;
    private Date createdAt;
    private Date completedAt;

    // Основен конструктор – за DAO
    public Segment(int id,
                   int projectId,
                   String languagePair,
                   String sourceText,
                   String targetText,
                   String status,
                   Date createdAt,
                   Date completedAt) {
        this.id = id;
        this.projectId = projectId;
        this.languagePair = languagePair;
        this.sourceText = sourceText;
        this.targetText = targetText;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    // Допълнителен конструктор за съвместимост
    public Segment(int id,
                   int projectId,
                   String sourceText,
                   String targetText,
                   String status) {
        this(id, projectId, null, sourceText, targetText, status, null, null);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getLanguagePair() {
        return languagePair;
    }

    public void setLanguagePair(String languagePair) {
        this.languagePair = languagePair;
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public String getTargetText() {
        return targetText;
    }

    public void setTargetText(String targetText) {
        this.targetText = targetText;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }
}