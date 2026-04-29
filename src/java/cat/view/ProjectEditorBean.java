package cat.view;

import cat.dao.SegmentDAO;
import cat.model.Segment;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "projectEditorBean")
@ViewScoped
public class ProjectEditorBean implements Serializable {

    private int projectId;
    private List<Segment> segments;
    private SegmentDAO dao = new SegmentDAO();

    public void init() {
        if (!FacesContext.getCurrentInstance().isPostback()
                && segments == null
                && projectId > 0) {
            segments = dao.findByProject(projectId);
        }
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public void save() {
        if (segments != null) {
            for (Segment s : segments) {
                dao.updateSegment(s.getId(), s.getTargetText(), s.getStatus());
            }
        }

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Saved",
                        "Segments updated successfully."));
    }
}