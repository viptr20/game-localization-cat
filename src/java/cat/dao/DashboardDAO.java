package cat.dao;

import cat.db.DBUtil;
import cat.model.DashboardStats;

import java.sql.*;
import java.util.*;

public class DashboardDAO {

    public DashboardDAO() {
        // без JNDI тук – DBUtil решава дали да ползва JNDI или директен JDBC
    }

    private Connection getConnection() throws SQLException {
        return DBUtil.getConnection();
    }

    /* ===================== Проекти ===================== */

    public Map<Integer, String> loadProjects() {
        String sql = "SELECT id, name FROM projects ORDER BY id";
        Map<Integer, String> result = new LinkedHashMap<Integer, String>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.put(rs.getInt("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<ProjectRowDTO> loadProjectRows() {
        String sql =
            "SELECT p.id, " +
            "       p.name, " +
            "       CONCAT(p.source_lang, ' -> ', p.target_langs) AS languages, " +
            "       COUNT(s.id) AS segments, " +
            "       COALESCE(ROUND(100.0 * SUM(CASE WHEN s.status = 'DONE' THEN 1 ELSE 0 END) / NULLIF(COUNT(s.id),0)), 0) AS progress_percent, " +
            "       p.created_at, " +
            "       p.completed_at " +
            "FROM projects p " +
            "LEFT JOIN segments s ON s.project_id = p.id " +
            "GROUP BY p.id, p.name, p.source_lang, p.target_langs, p.created_at, p.completed_at " +
            "ORDER BY p.id";

        List<ProjectRowDTO> list = new ArrayList<ProjectRowDTO>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ProjectRowDTO row = new ProjectRowDTO();
                row.setId(rs.getInt("id"));
                row.setName(rs.getString("name"));
                row.setLanguages(rs.getString("languages"));
                row.setSegments(rs.getInt("segments"));
                row.setProgressPercent(rs.getInt("progress_percent"));
                row.setCreatedAt(rs.getTimestamp("created_at"));
                row.setCompletedAt(rs.getTimestamp("completed_at"));
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /* ===================== Quick Stats ===================== */

    public DashboardStats loadStats(Integer projectId) {
        DashboardStats stats = new DashboardStats();
        try (Connection con = getConnection()) {
            stats.setTotalProjects(countProjects(con));
            stats.setTotalSegments(countSegments(con, projectId, null));
            stats.setNewSegments(countSegments(con, projectId, "NEW"));
            stats.setInProgressSegments(countSegments(con, projectId, "IN_PROGRESS"));
            stats.setDoneSegments(countSegments(con, projectId, "DONE"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    private int countProjects(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM projects");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int countSegments(Connection con, Integer projectId, String status) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM segments WHERE 1=1");
        if (projectId != null) {
            sb.append(" AND project_id = ?");
        }
        if (status != null) {
            sb.append(" AND status = ?");
        }

        try (PreparedStatement ps = con.prepareStatement(sb.toString())) {
            int idx = 1;
            if (projectId != null) {
                ps.setInt(idx++, projectId);
            }
            if (status != null) {
                ps.setString(idx, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /* ===================== Данни за графики ===================== */

    public Map<String, Integer> loadStatusCounts(Integer projectId) {
        String sql =
            "SELECT status, COUNT(*) AS cnt " +
            "FROM segments " +
            "WHERE (? IS NULL OR project_id = ?) " +
            "GROUP BY status";

        Map<String, Integer> result = new LinkedHashMap<String, Integer>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (projectId == null) {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(1, projectId);
                ps.setInt(2, projectId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("status"), rs.getInt("cnt"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Map<String, Integer> loadLanguageSplit(Integer projectId) {
        String sql =
            "SELECT COALESCE(language_pair, 'Unknown') AS lang, COUNT(*) AS cnt " +
            "FROM segments " +
            "WHERE (? IS NULL OR project_id = ?) " +
            "GROUP BY COALESCE(language_pair, 'Unknown') " +
            "ORDER BY lang";

        Map<String, Integer> result = new LinkedHashMap<String, Integer>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (projectId == null) {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(1, projectId);
                ps.setInt(2, projectId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("lang"), rs.getInt("cnt"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Map<String, Integer> loadRadarProfile(Integer projectId) {
        String sql =
            "SELECT COUNT(*) AS total_segments, " +
            "       SUM(CASE WHEN status = 'NEW' THEN 1 ELSE 0 END) AS new_count, " +
            "       SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS in_progress_count, " +
            "       SUM(CASE WHEN status = 'DONE' THEN 1 ELSE 0 END) AS done_count " +
            "FROM segments " +
            "WHERE (? IS NULL OR project_id = ?)";

        Map<String, Integer> result = new LinkedHashMap<String, Integer>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (projectId == null) {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(1, projectId);
                ps.setInt(2, projectId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total_segments");
                    int newCount = rs.getInt("new_count");
                    int inProgressCount = rs.getInt("in_progress_count");
                    int doneCount = rs.getInt("done_count");

                    int completionPercent = total > 0 ? (int) Math.round((doneCount * 100.0) / total) : 0;
                    int newPercent = total > 0 ? (int) Math.round((newCount * 100.0) / total) : 0;
                    int inProgressPercent = total > 0 ? (int) Math.round((inProgressCount * 100.0) / total) : 0;
                    int donePercent = total > 0 ? (int) Math.round((doneCount * 100.0) / total) : 0;
                    int volumeScore = Math.min(total * 10, 100);

                    result.put("Completion %", completionPercent);
                    result.put("New %", newPercent);
                    result.put("In Progress %", inProgressPercent);
                    result.put("Done %", donePercent);
                    result.put("Volume Score", volumeScore);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static class ProjectRowDTO {
        private int id;
        private String name;
        private String languages;
        private int segments;
        private int progressPercent;
        private Timestamp createdAt;
        private Timestamp completedAt;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getLanguages() { return languages; }
        public void setLanguages(String languages) { this.languages = languages; }

        public int getSegments() { return segments; }
        public void setSegments(int segments) { this.segments = segments; }

        public int getProgressPercent() { return progressPercent; }
        public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }

        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

        public Timestamp getCompletedAt() { return completedAt; }
        public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }
    }
}