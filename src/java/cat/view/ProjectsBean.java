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
import java.util.List;

@ManagedBean(name = "projectsBean")
@ViewScoped
public class ProjectsBean implements Serializable {

    private List<Project> projects;

    @PostConstruct
    public void init() {
        projects = new ProjectDAO().findAll();
    }

    public List<Project> getProjects() {
        return projects;
    }
}