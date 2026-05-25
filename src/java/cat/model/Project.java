package cat.model;

import java.io.Serializable;

public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String sourceLang;
    private String targetLangs;
    private String status;
    private String description;

    public Project() {
    }

    public Project(int id, String name, String sourceLang, String targetLangs, String status, String description) {
        this.id = id;
        this.name = name;
        this.sourceLang = sourceLang;
        this.targetLangs = targetLangs;
        this.status = status;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceLang() {
        return sourceLang;
    }

    public void setSourceLang(String sourceLang) {
        this.sourceLang = sourceLang;
    }

    public String getTargetLangs() {
        return targetLangs;
    }

    public void setTargetLangs(String targetLangs) {
        this.targetLangs = targetLangs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Project)) {
            return false;
        }

        Project other = (Project) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(id).hashCode();
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sourceLang='" + sourceLang + '\'' +
                ", targetLangs='" + targetLangs + '\'' +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}