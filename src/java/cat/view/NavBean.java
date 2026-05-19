package cat.view;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

@ManagedBean(name = "navBean")
@RequestScoped
public class NavBean {

    public String toProjectDetails() {
        // Always navigate to projectDetails with redirect
        return "/index/projectDetails.xhtml?faces-redirect=true";
    }

    public String toProjectEditor() {
        return "/index/projectEditor.xhtml?faces-redirect=true";
    }

    public String toProjects() {
        return "/index/projects.xhtml?faces-redirect=true";
    }

    public String toReview() {
        return "/index/review.xhtml?faces-redirect=true";
    }
}