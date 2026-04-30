package cat.view;

import cat.dao.DashboardDAO;
import cat.dao.DashboardDAO.ProjectRowDTO;
import cat.model.DashboardStats;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.PieChartModel;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.radar.RadarChartDataSet;
import org.primefaces.model.charts.radar.RadarChartModel;

import javax.annotation.PostConstruct;
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

@ManagedBean(name = "dashboardBean")
@ViewScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final DashboardDAO dao = new DashboardDAO();

    private Integer selectedProjectId;
    private Map<Integer, String> projectsMap;

    private List<ProjectRow> tableData;
    private DashboardStats stats;

    private RadarChartModel radarModel;
    private BarChartModel statusBarModel;
    private PieChartModel languagePieModel;

    @PostConstruct
    public void init() {
        loadProjects();
        selectedProjectId = null;
        reloadAll();
    }

    public void reloadAll() {
        stats = dao.loadStats(selectedProjectId);
        loadTableData();
        createRadarModel();
        createStatusBarModel();
        createLanguagePieModel();
    }

    // helper за достъп до bundle "msg" от faces-config.xml
    private String msg(String key) {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context == null) {
                return key;
            }
            Application app = context.getApplication();
            ResourceBundle bundle = app.getResourceBundle(context, "msg");
            if (bundle != null && bundle.containsKey(key)) {
                return bundle.getString(key);
            }
        } catch (Exception e) {
            // ignore, fall back
        }
        return "???" + key + "???";
    }

    private void loadProjects() {
        projectsMap = dao.loadProjects();
    }

    private void loadTableData() {
        tableData = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        for (ProjectRowDTO dto : dao.loadProjectRows()) {
            String created = dto.getCreatedAt() != null ? df.format(dto.getCreatedAt()) : "";
            String completed = dto.getCompletedAt() != null ? df.format(dto.getCompletedAt()) : "";

            tableData.add(new ProjectRow(
                    dto.getId(),
                    dto.getName(),
                    dto.getLanguages(),
                    dto.getSegments(),
                    dto.getProgressPercent(),
                    created,
                    completed
            ));
        }
    }

    // Radar chart – Project Profile
    private void createRadarModel() {
        radarModel = new RadarChartModel();

        ChartData data = new ChartData();
        RadarChartDataSet dataSet = new RadarChartDataSet();

        Map<String, Integer> radarData = dao.loadRadarProfile(selectedProjectId);

        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        labels.add(msg("chart.percentCompletion"));
        labels.add(msg("chart.percentNew"));
        labels.add(msg("chart.percentInProgress"));
        labels.add(msg("chart.percentDone"));
        labels.add(msg("chart.volumeScore"));

        // ВАЖНО: keys от DAO могат да останат на английски – те са вътрешни
        values.add(radarData.getOrDefault("Completion %", 0));
        values.add(radarData.getOrDefault("New %", 0));
        values.add(radarData.getOrDefault("In Progress %", 0));
        values.add(radarData.getOrDefault("Done %", 0));
        values.add(radarData.getOrDefault("Volume Score", 0));

        dataSet.setLabel(msg("chart.projectProfile"));
        dataSet.setData(values);
        dataSet.setFill(true);
        dataSet.setBackgroundColor("rgba(54, 162, 235, 0.2)");
        dataSet.setBorderColor("rgba(54, 162, 235, 1)");
        dataSet.setPointBackgroundColor("rgba(54, 162, 235, 1)");
        dataSet.setPointBorderColor("#fff");
        dataSet.setPointHoverBackgroundColor("#fff");
        dataSet.setPointHoverBorderColor("rgba(54, 162, 235, 1)");

        data.addChartDataSet(dataSet);
        data.setLabels(labels);

        radarModel.setData(data);
    }

    // Bar chart – Segment Status
    private void createStatusBarModel() {
        statusBarModel = new BarChartModel();
        ChartSeries series = new ChartSeries();
        series.setLabel(msg("chart.dataset.segments"));

        Map<String, Integer> data = dao.loadStatusCounts(selectedProjectId);

        series.set(msg("status.new"), data.getOrDefault("NEW", 0));
        series.set(msg("status.inProgress"), data.getOrDefault("IN_PROGRESS", 0));
        series.set(msg("status.done"), data.getOrDefault("DONE", 0));

        statusBarModel.addSeries(series);
        statusBarModel.setTitle(msg("chart.segmentStatus"));
        statusBarModel.setLegendPosition("ne");
        statusBarModel.setAnimate(true);
    }

    // Pie chart – Language Split
    private void createLanguagePieModel() {
        languagePieModel = new PieChartModel();
        Map<String, Integer> data = dao.loadLanguageSplit(selectedProjectId);

        if (data.isEmpty()) {
            languagePieModel.set(msg("chart.noData"), 1);
        } else {
            // По желание – сортиране на ключовете за по-постоянен ред
            Map<String, Integer> ordered = new TreeMap<>(data);
            for (Map.Entry<String, Integer> e : ordered.entrySet()) {
                languagePieModel.set(e.getKey(), e.getValue());
            }
        }

        languagePieModel.setTitle(msg("chart.languageSplit"));
        languagePieModel.setLegendPosition("w");
        languagePieModel.setShowDataLabels(true);
    }

    public void onProjectChange() {
        reloadAll();
    }

    // по избор – ако искаш да викаш ръчно след смяна на език без redirect
    public void refreshForLocaleChange() {
        reloadAll();
    }

    public DashboardStats getStats() {
        return stats;
    }

    public int getCompletedPercent() {
        if (stats == null || stats.getTotalSegments() == 0) {
            return 0;
        }
        return (int) Math.round(100.0 * stats.getDoneSegments() / stats.getTotalSegments());
    }

    public Integer getSelectedProjectId() {
        return selectedProjectId;
    }

    public void setSelectedProjectId(Integer selectedProjectId) {
        this.selectedProjectId = selectedProjectId;
    }

    public List<ProjectRow> getTableData() {
        return tableData;
    }

    public RadarChartModel getRadarModel() {
        return radarModel;
    }

    public BarChartModel getStatusBarModel() {
        return statusBarModel;
    }

    public PieChartModel getLanguagePieModel() {
        return languagePieModel;
    }

    public List<ProjectSelectItem> getProjects() {
        List<ProjectSelectItem> list = new ArrayList<>();
        if (projectsMap != null) {
            for (Map.Entry<Integer, String> e : projectsMap.entrySet()) {
                list.add(new ProjectSelectItem(e.getKey(), e.getValue()));
            }
        }
        return list;
    }

    // DTO за таблицата
    public static class ProjectRow implements Serializable {
        private final int id;
        private final String name;
        private final String languages;
        private final int segments;
        private final int progressPercent;
        private final String createdAt;
        private final String completedAt;

        public ProjectRow(int id, String name, String languages,
                          int segments, int progressPercent,
                          String createdAt, String completedAt) {
            this.id = id;
            this.name = name;
            this.languages = languages;
            this.segments = segments;
            this.progressPercent = progressPercent;
            this.createdAt = createdAt;
            this.completedAt = completedAt;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getLanguages() { return languages; }
        public int getSegments() { return segments; }
        public int getProgressPercent() { return progressPercent; }
        public String getCreatedAt() { return createdAt; }
        public String getCompletedAt() { return completedAt; }
    }

    // DTO за selectOneMenu
    public static class ProjectSelectItem implements Serializable {
        private final Integer id;
        private final String name;

        public ProjectSelectItem(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() { return id; }
        public String getName() { return name; }
    }
}