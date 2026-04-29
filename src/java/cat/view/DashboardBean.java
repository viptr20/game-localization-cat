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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

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

    private String msg(String key) {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context == null) {
                return key;
            }

            Application application = context.getApplication();
            ResourceBundle bundle = application.getResourceBundle(context, "msg");

            if (bundle != null && bundle.containsKey(key)) {
                return bundle.getString(key);
            }
        } catch (Exception e) {
            // fallback below
        }
        return "???" + key + "???";
    }

    private void loadProjects() {
        projectsMap = dao.loadProjects();
    }

    private void loadTableData() {
        tableData = new ArrayList<ProjectRow>();
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

    private void createRadarModel() {
        radarModel = new RadarChartModel();

        ChartData data = new ChartData();
        RadarChartDataSet dataSet = new RadarChartDataSet();

        Map<String, Integer> radarData = dao.loadRadarProfile(selectedProjectId);

        List<Number> values = new ArrayList<Number>();
        List<String> labels = new ArrayList<String>();

        labels.add(msg("chart.percentCompletion"));
        labels.add(msg("chart.percentNew"));
        labels.add(msg("chart.percentInProgress"));
        labels.add(msg("chart.percentDone"));
        labels.add(msg("chart.volumeScore"));

        values.add(radarData.containsKey("Completion %") ? radarData.get("Completion %") : 0);
        values.add(radarData.containsKey("New %") ? radarData.get("New %") : 0);
        values.add(radarData.containsKey("In Progress %") ? radarData.get("In Progress %") : 0);
        values.add(radarData.containsKey("Done %") ? radarData.get("Done %") : 0);
        values.add(radarData.containsKey("Volume Score") ? radarData.get("Volume Score") : 0);

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

    private void createStatusBarModel() {
        statusBarModel = new BarChartModel();
        ChartSeries series = new ChartSeries();
        series.setLabel(msg("chart.dataset.segments"));

        Map<String, Integer> data = dao.loadStatusCounts(selectedProjectId);

        series.set(msg("status.new"), data.containsKey("NEW") ? data.get("NEW") : 0);
        series.set(msg("status.inProgress"), data.containsKey("IN_PROGRESS") ? data.get("IN_PROGRESS") : 0);
        series.set(msg("status.done"), data.containsKey("DONE") ? data.get("DONE") : 0);

        statusBarModel.addSeries(series);
        statusBarModel.setTitle(msg("chart.segmentStatus"));
        statusBarModel.setLegendPosition("ne");
        statusBarModel.setAnimate(true);
    }

    private void createLanguagePieModel() {
        languagePieModel = new PieChartModel();
        Map<String, Integer> data = dao.loadLanguageSplit(selectedProjectId);

        if (data.isEmpty()) {
            languagePieModel.set(msg("chart.noData"), 1);
        } else {
            Map<String, Integer> ordered = new TreeMap<String, Integer>(data);
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
        List<ProjectSelectItem> list = new ArrayList<ProjectSelectItem>();
        if (projectsMap != null) {
            for (Map.Entry<Integer, String> e : projectsMap.entrySet()) {
                list.add(new ProjectSelectItem(e.getKey(), e.getValue()));
            }
        }
        return list;
    }

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