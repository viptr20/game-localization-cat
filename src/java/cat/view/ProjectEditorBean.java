package cat.view;

import cat.dao.SegmentDAO;
import cat.model.Segment;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "projectEditorBean")
@ViewScoped
public class ProjectEditorBean implements Serializable {

    private int projectId;
    private List<Segment> segments;
    private Segment selectedSegment;
    private List<String> semanticSuggestions = new ArrayList<>();

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

    public Segment getSelectedSegment() {
        return selectedSegment;
    }

    public void setSelectedSegment(Segment selectedSegment) {
        this.selectedSegment = selectedSegment;
    }

    public List<String> getSemanticSuggestions() {
        return semanticSuggestions;
    }

    public void setSemanticSuggestions(List<String> semanticSuggestions) {
        this.semanticSuggestions = semanticSuggestions;
    }

    public void loadSemanticSuggestions() {
        semanticSuggestions.clear();

        if (selectedSegment == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "No segment selected",
                            "Please select a segment first."));
            return;
        }

        String source = selectedSegment.getSourceText() != null
                ? selectedSegment.getSourceText().trim()
                : "";

        if (!source.isEmpty()) {
            semanticSuggestions.add("Demo suggestion 1 for: " + source);
            semanticSuggestions.add("Demo suggestion 2 for: " + source);
            semanticSuggestions.add("Demo suggestion 3 for: " + source);
        }
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