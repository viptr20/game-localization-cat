package cat.view;

import cat.dao.SegmentDAO;
import cat.model.Segment;
import cat.wikidata.WikidataClient;
import cat.wikidata.WikidataEntry;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.mindmap.DefaultMindmapNode;
import org.primefaces.model.mindmap.MindmapNode;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "projectEditorBean")
@ViewScoped
public class ProjectEditorBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private int projectId;
    private List<Segment> segments;
    private List<Segment> selectedSegments = new ArrayList<Segment>();
    private Segment selectedSegment;

    private UploadedFile uploadedSegmentsCsvFile;
    private UploadedFile uploadedSegmentsXlsxFile;

    private List<String> semanticSuggestions = new ArrayList<String>();
    private List<WikidataEntry> wikidataEntries = new ArrayList<WikidataEntry>();

    private MindmapNode mindmapRoot;
    private MindmapNode selectedMindmapNode;

    private final SegmentDAO dao = new SegmentDAO();
    private final WikidataClient wikidataClient = new WikidataClient();

    @PostConstruct
    public void init() {
    }

    public void load() {
        if (projectId > 0 && segments == null) {
            segments = dao.findByProject(projectId);
        }
    }

    private void reloadSegments() {
        segments = dao.findByProject(projectId);
        selectedSegments = new ArrayList<Segment>();
        selectedSegment = null;
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

    public List<Segment> getSelectedSegments() {
        return selectedSegments;
    }

    public void setSelectedSegments(List<Segment> selectedSegments) {
        this.selectedSegments = selectedSegments;
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

    public void onSegmentsCsvUpload(FileUploadEvent event) {
        this.uploadedSegmentsCsvFile = event.getFile();
        addMessage(FacesMessage.SEVERITY_INFO, "CSV selected", event.getFile().getFileName());
    }

    public void onSegmentsXlsxUpload(FileUploadEvent event) {
        this.uploadedSegmentsXlsxFile = event.getFile();
        addMessage(FacesMessage.SEVERITY_INFO, "Excel selected", event.getFile().getFileName());
    }

    public void downloadSelectedSegmentsCsv() {
        List<Segment> exportList = getSelectedSegmentExportList();
        if (exportList.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "No segments selected", "Please select at least one segment.");
            return;
        }
        exportSegmentsCsv(exportList, "segments_selected_project_" + projectId + ".csv");
    }

    public void downloadAllSegmentsCsv() {
        exportSegmentsCsv(dao.findByProject(projectId), "segments_all_project_" + projectId + ".csv");
    }

    public void downloadSelectedSegmentsXlsx() {
        List<Segment> exportList = getSelectedSegmentExportList();
        if (exportList.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "No segments selected", "Please select at least one segment.");
            return;
        }
        exportSegmentsXlsx(exportList, "segments_selected_project_" + projectId + ".xlsx");
    }

    public void downloadAllSegmentsXlsx() {
        exportSegmentsXlsx(dao.findByProject(projectId), "segments_all_project_" + projectId + ".xlsx");
    }

    private List<Segment> getSelectedSegmentExportList() {
        return selectedSegments != null ? selectedSegments : new ArrayList<Segment>();
    }

    private void exportSegmentsCsv(List<Segment> exportList, String fileName) {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext external = context.getExternalContext();

        try {
            external.responseReset();
            external.setResponseContentType("text/csv");
            external.setResponseCharacterEncoding("UTF-8");
            external.setResponseHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            OutputStream out = external.getResponseOutputStream();
            Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);

            writer.write("id,project_id,language_pair,source_text,target_text,status\n");

            for (Segment s : exportList) {
                writer.write(csv(String.valueOf(s.getId())));
                writer.write(",");
                writer.write(csv(String.valueOf(s.getProjectId())));
                writer.write(",");
                writer.write(csv(s.getLanguagePair()));
                writer.write(",");
                writer.write(csv(s.getSourceText()));
                writer.write(",");
                writer.write(csv(s.getTargetText()));
                writer.write(",");
                writer.write(csv(s.getStatus()));
                writer.write("\n");
            }

            writer.flush();
            context.responseComplete();
        } catch (Exception e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Export failed", "Could not export segments CSV.");
        }
    }

    private void exportSegmentsXlsx(List<Segment> exportList, String fileName) {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext external = context.getExternalContext();

        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Segments");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("id");
            header.createCell(1).setCellValue("project_id");
            header.createCell(2).setCellValue("language_pair");
            header.createCell(3).setCellValue("source_text");
            header.createCell(4).setCellValue("target_text");
            header.createCell(5).setCellValue("status");

            int rowIndex = 1;
            for (Segment s : exportList) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(s.getId());
                row.createCell(1).setCellValue(s.getProjectId());
                row.createCell(2).setCellValue(nvl(s.getLanguagePair()));
                row.createCell(3).setCellValue(nvl(s.getSourceText()));
                row.createCell(4).setCellValue(nvl(s.getTargetText()));
                row.createCell(5).setCellValue(nvl(s.getStatus()));
            }

            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            external.responseReset();
            external.setResponseContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            external.setResponseHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            OutputStream out = external.getResponseOutputStream();
            workbook.write(out);
            out.flush();
            context.responseComplete();
        } catch (Throwable e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Export failed",
                    "Excel export failed. Check that only POI 3.17 jars are present.");
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    public void uploadSegmentsCsv() {
        if (uploadedSegmentsCsvFile == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "No file selected", "Please upload a CSV file first.");
            return;
        }

        int imported = 0;
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(
                    new InputStreamReader(uploadedSegmentsCsvFile.getInputstream(), StandardCharsets.UTF_8));

            String line;
            boolean first = true;

            while ((line = reader.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                List<String> cols = parseCsvLine(line);
                if (cols.size() < 6) {
                    continue;
                }

                int id = parseInt(cols.get(0));
                String languagePair = trimToEmpty(cols.get(2));
                String sourceText = trimToEmpty(cols.get(3));
                String targetText = trimToEmpty(cols.get(4));
                String status = normalizeStatus(trimToEmpty(cols.get(5)));

                if (sourceText.isEmpty()) {
                    continue;
                }

                Segment s = new Segment(id, projectId, languagePair, sourceText, targetText, status, null, null);

                if (id > 0) {
                    dao.upsertSegment(s);
                } else {
                    dao.insertSegmentForProject(s);
                }

                imported++;
            }

            reloadSegments();
            uploadedSegmentsCsvFile = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Import complete", "Imported/updated segments: " + imported);
        } catch (Exception e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Import failed", "Could not import segments CSV.");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    public void uploadSegmentsXlsx() {
        if (uploadedSegmentsXlsxFile == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "No file selected", "Please upload an Excel file first.");
            return;
        }

        int imported = 0;
        InputStream in = null;
        Workbook workbook = null;

        try {
            in = uploadedSegmentsXlsxFile.getInputstream();
            workbook = new XSSFWorkbook(in);

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                int id = parseInt(cellValue(row.getCell(0)));
                String languagePair = cellValue(row.getCell(2));
                String sourceText = cellValue(row.getCell(3));
                String targetText = cellValue(row.getCell(4));
                String status = normalizeStatus(cellValue(row.getCell(5)));

                if (sourceText.isEmpty()) {
                    continue;
                }

                Segment s = new Segment(id, projectId, languagePair, sourceText, targetText, status, null, null);

                if (id > 0) {
                    dao.upsertSegment(s);
                } else {
                    dao.insertSegmentForProject(s);
                }

                imported++;
            }

            reloadSegments();
            uploadedSegmentsXlsxFile = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Import complete", "Imported/updated segments: " + imported);
        } catch (Throwable e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Import failed",
                    "Excel import failed. Check that only POI 3.17 jars are present.");
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception ignore) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    public void loadSemanticSuggestions() {
        semanticSuggestions.clear();
        wikidataEntries.clear();
        mindmapRoot = null;
        selectedMindmapNode = null;
        selectedSegment = null;

        if (selectedSegments == null || selectedSegments.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "No segment selected",
                            "Please select at least one segment first."));
            return;
        }

        selectedSegment = selectedSegments.get(0);

        if (selectedSegment == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "No segment selected",
                            "Please select at least one segment first."));
            return;
        }

        String source = selectedSegment.getSourceText() != null
                ? selectedSegment.getSourceText().trim()
                : "";

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

        boolean hasUsableSemanticInfo = false;

        try {
            List<WikidataEntry> results = wikidataClient.searchEntries(source, sourceLang);

            if (results != null) {
                for (WikidataEntry entry : results) {
                    if (!isDummyEntry(entry)) {
                        wikidataEntries.add(entry);
                    }
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

        if (wikidataEntries.isEmpty()) {
            DefaultMindmapNode infoChild = new DefaultMindmapNode(
                    "No information found",
                    "No semantic data available for this segment",
                    "6e9ebf",
                    true
            );
            infoChild.setData(null);
            mindmapRoot.addNode(infoChild);
        } else {
            for (WikidataEntry e : wikidataEntries) {
                String label = safe(e.getLabel(), "Unnamed");
                String description = safe(e.getDescription(), "No description");

                DefaultMindmapNode child = new DefaultMindmapNode(label, description, "6e9ebf", true);

                if (e.getUri() != null && !e.getUri().trim().isEmpty()) {
                    child.setData(e.getUri().trim());
                }

                mindmapRoot.addNode(child);
            }
        }
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
        } else {
            selectedMindmapNode = null;
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

    private String cellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        int type = cell.getCellType();

        if (type == Cell.CELL_TYPE_STRING) {
            return trimToEmpty(cell.getStringCellValue());
        }

        if (type == Cell.CELL_TYPE_NUMERIC) {
            double d = cell.getNumericCellValue();
            if (d == Math.floor(d)) {
                return String.valueOf((int) d);
            }
            return String.valueOf(d);
        }

        if (type == Cell.CELL_TYPE_BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }

        if (type == Cell.CELL_TYPE_FORMULA) {
            try {
                return trimToEmpty(cell.getStringCellValue());
            } catch (Exception e) {
                try {
                    double d = cell.getNumericCellValue();
                    if (d == Math.floor(d)) {
                        return String.valueOf((int) d);
                    }
                    return String.valueOf(d);
                } catch (Exception ex) {
                    return "";
                }
            }
        }

        return "";
    }

    private String csv(String value) {
        String s = value == null ? "" : value;
        s = s.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(trimToEmpty(s));
        } catch (Exception e) {
            return 0;
        }
    }

    private String trimToEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private String normalizeStatus(String s) {
        String value = trimToEmpty(s).toUpperCase();
        if ("IN PROGRESS".equals(value)) {
            return "IN_PROGRESS";
        }
        if (value.length() == 0) {
            return "NEW";
        }
        return value;
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<String>();
        if (line == null) {
            return result;
        }

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        result.add(current.toString());
        return result;
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
}