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

    private static final long serialVersionUID = 1L;

    private List<Project> projects;

    @PostConstruct
    public void init() {
        projects = new ProjectDAO().findAll();
    }

    public List<Project> getProjects() {
        return projects;
    }
}