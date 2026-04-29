/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package cat.view;

import cat.dao.ProjectDAO;
import cat.model.Project;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

@ManagedBean(name = "projectDetailsBean")
@ViewScoped
public class ProjectDetailsBean implements Serializable {

    private int projectId;
    private Project project;

    private ProjectDAO dao = new ProjectDAO();

    @PostConstruct
    public void init() {
        if (projectId > 0) {
            project = dao.findById(projectId);
        }
    }

    public int getProjectId() {
        return projectId;
    }
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public Project getProject() {
        return project;
    }

    public String getName() {
        return project != null ? project.getName() : "";
    }

    public String getSourceLang() {
        return project != null ? project.getSourceLang() : "";
    }

    public String getTargetLangs() {
        return project != null ? project.getTargetLangs() : "";
    }

    public String getStatus() {
        return project != null ? project.getStatus() : "";
    }

    public String getDescription() {
        return project != null ? project.getDescription() : "";
    }
}