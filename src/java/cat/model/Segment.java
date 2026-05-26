package cat.model;

import java.io.Serializable;
import java.util.Date;

public class Segment implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int projectId;
    private String languagePair;
    private String sourceText;
    private String targetText;
    private String status;
    private Date createdAt;
    private Date updatedAt;

    public Segment() {
    }

    public Segment(int id,
                   int projectId,
                   String languagePair,
                   String sourceText,
                   String targetText,
                   String status,
                   Date createdAt,
                   Date updatedAt) {
        this.id = id;
        this.projectId = projectId;
        this.languagePair = languagePair;
        this.sourceText = sourceText;
        this.targetText = targetText;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Segment)) {
            return false;
        }

        Segment other = (Segment) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(id).hashCode();
    }

    @Override
    public String toString() {
        return "Segment{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", languagePair='" + languagePair + '\'' +
                ", sourceText='" + sourceText + '\'' +
                ", targetText='" + targetText + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}