package cat.view;

import cat.dao.ProjectDAO;
import cat.model.Project;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

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

@ManagedBean(name = "projectsBean")
@ViewScoped
public class ProjectsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Project> projects;
    private List<Project> selectedProjects = new ArrayList<Project>();

    private UploadedFile uploadedProjectsCsvFile;
    private UploadedFile uploadedProjectsXlsxFile;

    private final ProjectDAO dao = new ProjectDAO();

    @PostConstruct
    public void init() {
        reloadProjects();
    }

    private void reloadProjects() {
        projects = dao.findAll();
    }

    public List<Project> getProjects() {
        return projects;
    }

    public List<Project> getSelectedProjects() {
        return selectedProjects;
    }

    public void setSelectedProjects(List<Project> selectedProjects) {
        this.selectedProjects = selectedProjects;
    }

    public void onProjectsCsvUpload(FileUploadEvent event) {
        this.uploadedProjectsCsvFile = event.getFile();
        addMessage(FacesMessage.SEVERITY_INFO, "CSV selected", event.getFile().getFileName());
    }

    public void onProjectsXlsxUpload(FileUploadEvent event) {
        this.uploadedProjectsXlsxFile = event.getFile();
        addMessage(FacesMessage.SEVERITY_INFO, "Excel selected", event.getFile().getFileName());
    }

    public void downloadSelectedProjectsCsv() {
        List<Project> exportList = getSelectedProjectExportList();
        if (exportList.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "No projects selected", "Please select at least one project.");
            return;
        }
        exportProjectsCsv(exportList, "projects_selected.csv");
    }

    public void downloadAllProjectsCsv() {
        exportProjectsCsv(dao.findAll(), "projects_all.csv");
    }

    public void downloadSelectedProjectsXlsx() {
        List<Project> exportList = getSelectedProjectExportList();
        if (exportList.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "No projects selected", "Please select at least one project.");
            return;
        }
        exportProjectsXlsx(exportList, "projects_selected.xlsx");
    }

    public void downloadAllProjectsXlsx() {
        exportProjectsXlsx(dao.findAll(), "projects_all.xlsx");
    }

    private List<Project> getSelectedProjectExportList() {
        return selectedProjects != null ? selectedProjects : new ArrayList<Project>();
    }

    private void exportProjectsCsv(List<Project> exportList, String fileName) {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext external = context.getExternalContext();

        try {
            external.responseReset();
            external.setResponseContentType("text/csv");
            external.setResponseCharacterEncoding("UTF-8");
            external.setResponseHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            OutputStream out = external.getResponseOutputStream();
            Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);

            writer.write("id,name,source_lang,target_langs,status,description\n");

            for (Project p : exportList) {
                writer.write(csv(String.valueOf(p.getId())));
                writer.write(",");
                writer.write(csv(p.getName()));
                writer.write(",");
                writer.write(csv(p.getSourceLang()));
                writer.write(",");
                writer.write(csv(p.getTargetLangs()));
                writer.write(",");
                writer.write(csv(p.getStatus()));
                writer.write(",");
                writer.write(csv(p.getDescription()));
                writer.write("\n");
            }

            writer.flush();
            context.responseComplete();
        } catch (Exception e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Export failed", "Could not export projects CSV.");
        }
    }

    private void exportProjectsXlsx(List<Project> exportList, String fileName) {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext external = context.getExternalContext();

        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Projects");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("id");
            header.createCell(1).setCellValue("name");
            header.createCell(2).setCellValue("source_lang");
            header.createCell(3).setCellValue("target_langs");
            header.createCell(4).setCellValue("status");
            header.createCell(5).setCellValue("description");

            int rowIndex = 1;
            for (Project p : exportList) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(nvl(p.getName()));
                row.createCell(2).setCellValue(nvl(p.getSourceLang()));
                row.createCell(3).setCellValue(nvl(p.getTargetLangs()));
                row.createCell(4).setCellValue(nvl(p.getStatus()));
                row.createCell(5).setCellValue(nvl(p.getDescription()));
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

    public void uploadProjectsCsv() {
        if (uploadedProjectsCsvFile == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "No file selected", "Please upload a CSV file first.");
            return;
        }

        int imported = 0;
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(
                    new InputStreamReader(uploadedProjectsCsvFile.getInputstream(), StandardCharsets.UTF_8));

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
                String name = trimToEmpty(cols.get(1));
                String sourceLang = trimToEmpty(cols.get(2));
                String targetLangs = trimToEmpty(cols.get(3));
                String status = trimToEmpty(cols.get(4));
                String description = trimToEmpty(cols.get(5));

                if (name.isEmpty()) {
                    continue;
                }

                Project p = new Project(id, name, sourceLang, targetLangs, status, description);
                dao.upsertProject(p);
                imported++;
            }

            reloadProjects();
            uploadedProjectsCsvFile = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Import complete", "Imported/updated projects: " + imported);
        } catch (Exception e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Import failed", "Could not import projects CSV.");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    public void uploadProjectsXlsx() {
        if (uploadedProjectsXlsxFile == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "No file selected", "Please upload an Excel file first.");
            return;
        }

        int imported = 0;
        InputStream in = null;
        Workbook workbook = null;

        try {
            in = uploadedProjectsXlsxFile.getInputstream();
            workbook = new XSSFWorkbook(in);

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                int id = parseInt(cellValue(row.getCell(0)));
                String name = cellValue(row.getCell(1));
                String sourceLang = cellValue(row.getCell(2));
                String targetLangs = cellValue(row.getCell(3));
                String status = cellValue(row.getCell(4));
                String description = cellValue(row.getCell(5));

                if (name.isEmpty()) {
                    continue;
                }

                Project p = new Project(id, name, sourceLang, targetLangs, status, description);
                dao.upsertProject(p);
                imported++;
            }

            reloadProjects();
            uploadedProjectsXlsxFile = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Import complete", "Imported/updated projects: " + imported);
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