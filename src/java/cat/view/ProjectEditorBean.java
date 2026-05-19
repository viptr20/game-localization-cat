package cat.view;

import cat.dao.SegmentDAO;
import cat.model.Segment;
import cat.wikidata.WikidataClient;
import cat.wikidata.WikidataEntry;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.mindmap.DefaultMindmapNode;
import org.primefaces.model.mindmap.MindmapNode;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "projectEditorBean")
@ViewScoped
public class ProjectEditorBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private int projectId;
    private List<Segment> segments;
    private Segment selectedSegment;

    private List<String> semanticSuggestions = new ArrayList<>();
    private List<WikidataEntry> wikidataEntries = new ArrayList<>();

    private MindmapNode mindmapRoot;
    private MindmapNode selectedMindmapNode;

    private final SegmentDAO dao = new SegmentDAO();
    private final WikidataClient wikidataClient = new WikidataClient();

    @PostConstruct
    public void init() {
        // load() is triggered from preRenderView
    }

    public void load() {
        if (projectId > 0 && segments == null) {
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

    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }

    public Segment getSelectedSegment() {
        return selectedSegment;
    }

    public void setSelectedSegment(Segment selectedSegment) {
        System.out.println("DEBUG setSelectedSegment = " + selectedSegment);
        this.selectedSegment = selectedSegment;
    }

    public List<String> getSemanticSuggestions() {
        return semanticSuggestions;
    }

    public void setSemanticSuggestions(List<String> semanticSuggestions) {
        this.semanticSuggestions = semanticSuggestions;
    }

    public List<WikidataEntry> getWikidataEntries() {
        return wikidataEntries;
    }

    public void setWikidataEntries(List<WikidataEntry> wikidataEntries) {
        this.wikidataEntries = wikidataEntries;
    }

    public MindmapNode getMindmapRoot() {
        return mindmapRoot;
    }

    public void setMindmapRoot(MindmapNode mindmapRoot) {
        this.mindmapRoot = mindmapRoot;
    }

    public MindmapNode getSelectedMindmapNode() {
        return selectedMindmapNode;
    }

    public void setSelectedMindmapNode(MindmapNode selectedMindmapNode) {
        this.selectedMindmapNode = selectedMindmapNode;
    }

    public void loadSemanticSuggestions() {
        System.out.println("DEBUG >>> loadSemanticSuggestions CALLED");

        semanticSuggestions.clear();
        wikidataEntries.clear();
        mindmapRoot = null;
        selectedMindmapNode = null;

        System.out.println("DEBUG selectedSegment = " + selectedSegment);

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

        System.out.println("DEBUG source = '" + source + "'");

        if (source.isEmpty()) {
            semanticSuggestions.add("No source text available.");
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Empty source",
                            "The selected segment has no source text."));
            return;
        }

        String sourceLang = extractSourceLanguage(selectedSegment.getLanguagePair());
        String targetLang = extractTargetLanguage(selectedSegment.getLanguagePair());

        System.out.println("DEBUG sourceLang = " + sourceLang);
        System.out.println("DEBUG targetLang = " + targetLang);

        boolean hasUsableSemanticInfo = false;

        try {
            List<WikidataEntry> results = wikidataClient.searchEntries(source, sourceLang);
            System.out.println("DEBUG wikidataEntries size = " + (results != null ? results.size() : 0));

            if (results != null) {
                for (WikidataEntry entry : results) {
                    if (isDummyEntry(entry)) {
                        System.out.println("DEBUG skipping dummy entry = " + entry.getLabel());
                        continue;
                    }
                    wikidataEntries.add(entry);
                }
            }

            if (!wikidataEntries.isEmpty()) {
                hasUsableSemanticInfo = true;

                WikidataEntry best = wikidataEntries.get(0);

                String bestLabel = safe(best.getLabel(), source);
                String bestDescription = safe(best.getDescription(), "No description available.");

                semanticSuggestions.add("Best semantic match: " + bestLabel + " — " + bestDescription);

                String targetSuggestion = buildTargetSuggestion(bestLabel, source, targetLang);
                if (targetSuggestion != null && !targetSuggestion.trim().isEmpty()) {
                    semanticSuggestions.add("Suggested target term (" + targetLang + "): " + targetSuggestion);
                } else {
                    semanticSuggestions.add("No target-language suggestion found for " + targetLang + ".");
                }

                if (wikidataEntries.size() > 1) {
                    semanticSuggestions.add("Related Wikidata concepts found: " + wikidataEntries.size());
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Wikidata unavailable",
                            "Problem while querying Wikidata."));
        }

        if (!hasUsableSemanticInfo) {
            String localTargetSuggestion = buildTargetSuggestion(source, source, targetLang);

            semanticSuggestions.add("No semantic information found.");

            if (localTargetSuggestion != null && !localTargetSuggestion.trim().isEmpty()) {
                semanticSuggestions.add("Local target suggestion (" + targetLang + "): " + localTargetSuggestion);
            } else {
                semanticSuggestions.add("No target-language suggestion found for " + targetLang + ".");
            }
        }

        mindmapRoot = new DefaultMindmapNode(source, "Source segment", "FFCC00", false);

        int childCount = 0;

        if (wikidataEntries.isEmpty()) {
            DefaultMindmapNode infoChild = new DefaultMindmapNode(
                    "No information found",
                    "No semantic data available for this segment",
                    "6e9ebf",
                    true
            );
            infoChild.setData(null);
            mindmapRoot.addNode(infoChild);
            childCount++;
        } else {
            for (WikidataEntry e : wikidataEntries) {
                String label = safe(e.getLabel(), "Unnamed");
                String description = safe(e.getDescription(), "No description");

                DefaultMindmapNode child = new DefaultMindmapNode(
                        label,
                        description,
                        "6e9ebf",
                        true
                );

                if (e.getUri() != null && !e.getUri().trim().isEmpty()) {
                    child.setData(e.getUri().trim());
                }

                mindmapRoot.addNode(child);
                childCount++;
            }
        }

        System.out.println("DEBUG mindmapRoot label = " + mindmapRoot.getLabel());
        System.out.println("DEBUG mindmap childCount = " + childCount);
    }

    private String extractSourceLanguage(String languagePair) {
        if (languagePair != null && languagePair.contains("-")) {
            String[] parts = languagePair.split("-");
            if (parts.length > 0 && parts[0] != null && !parts[0].trim().isEmpty()) {
                return parts[0].trim();
            }
        }
        return "en";
    }

    private String extractTargetLanguage(String languagePair) {
        if (languagePair != null && languagePair.contains("-")) {
            String[] parts = languagePair.split("-");
            if (parts.length > 1 && parts[1] != null && !parts[1].trim().isEmpty()) {
                return parts[1].trim();
            }
        }
        return "en";
    }

    private boolean isDummyEntry(WikidataEntry entry) {
        if (entry == null) {
            return true;
        }

        String label = entry.getLabel() != null ? entry.getLabel().trim() : "";
        String description = entry.getDescription() != null ? entry.getDescription().trim() : "";
        String uri = entry.getUri() != null ? entry.getUri().trim() : "";

        return label.startsWith("TEST_")
                || description.startsWith("Dummy description")
                || uri.startsWith("test:")
                || uri.startsWith("dummy:");
    }

    private String buildTargetSuggestion(String candidate, String source, String targetLang) {
        String text = candidate != null && !candidate.trim().isEmpty() ? candidate.trim() : source;
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        if ("bg".equalsIgnoreCase(targetLang)) {
            if ("settings".equalsIgnoreCase(text)) return "Настройки";
            if ("exit".equalsIgnoreCase(text)) return "Изход";
            if ("save".equalsIgnoreCase(text)) return "Запази";
            if ("cancel".equalsIgnoreCase(text)) return "Отказ";
            if ("new quest available".equalsIgnoreCase(text)) return "Нова задача е налична";
            if ("death".equalsIgnoreCase(text)) return "Смърт";
            if ("doorway".equalsIgnoreCase(text)) return "Вход";
            if ("portal".equalsIgnoreCase(text)) return "Портал";
        }

        if ("de".equalsIgnoreCase(targetLang)) {
            if ("settings".equalsIgnoreCase(text)) return "Einstellungen";
            if ("exit".equalsIgnoreCase(text)) return "Beenden";
            if ("save".equalsIgnoreCase(text)) return "Speichern";
            if ("cancel".equalsIgnoreCase(text)) return "Abbrechen";
            if ("new quest available".equalsIgnoreCase(text)) return "Neue Quest verfügbar";
            if ("death".equalsIgnoreCase(text)) return "Tod";
        }

        if ("pt".equalsIgnoreCase(targetLang)) {
            if ("settings".equalsIgnoreCase(text)) return "Definições";
            if ("exit".equalsIgnoreCase(text)) return "Sair";
            if ("save".equalsIgnoreCase(text)) return "Guardar";
            if ("cancel".equalsIgnoreCase(text)) return "Cancelar";
        }

        return null;
    }

    private String safe(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value.trim() : fallback;
    }

    public void onMindmapNodeSelect(SelectEvent event) {
        Object obj = event.getObject();

        if (obj instanceof MindmapNode) {
            selectedMindmapNode = (MindmapNode) obj;
            System.out.println("DEBUG selected mindmap node = " + selectedMindmapNode.getLabel());
        } else {
            selectedMindmapNode = null;
            System.out.println("DEBUG selected mindmap node = null");
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

    public void goBack() {
        try {
            FacesContext ctx = FacesContext.getCurrentInstance();
            String url = ctx.getExternalContext().getRequestContextPath()
                    + "/projectDetails.xhtml?projectId=" + projectId;
            ctx.getExternalContext().redirect(url);
            ctx.responseComplete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}