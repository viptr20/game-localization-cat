package cat.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Segment implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int projectId;
    private String languagePair;
    private String sourceText;
    private String targetText;
    private String status;
    private Timestamp createdAt;
    private Timestamp completedAt;

    public Segment() {
    }

    public Segment(int id,
                   int projectId,
                   String languagePair,
                   String sourceText,
                   String targetText,
                   String status,
                   Timestamp createdAt,
                   Timestamp completedAt) {
        this.id = id;
        this.projectId = projectId;
        this.languagePair = languagePair;
        this.sourceText = sourceText;
        this.targetText = targetText;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Segment)) return false;
        Segment other = (Segment) o;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Segment{id=" + id +
                ", projectId=" + projectId +
                ", languagePair='" + languagePair + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}