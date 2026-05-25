package cat.dao;

import cat.db.DBUtil;
import cat.model.Segment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SegmentDAO {

    private static final String FIND_BY_PROJECT_SQL =
            "SELECT * FROM segments WHERE project_id = ? ORDER BY id";

    private static final String UPDATE_SQL =
            "UPDATE segments SET target_text = ?, status = ? WHERE id = ?";

    private static final String EXISTS_SQL =
            "SELECT COUNT(*) FROM segments WHERE id = ?";

    private static final String UPSERT_UPDATE_SQL =
            "UPDATE segments SET project_id = ?, language_pair = ?, source_text = ?, target_text = ?, status = ? WHERE id = ?";

    private static final String INSERT_WITH_ID_SQL =
            "INSERT INTO segments (id, project_id, language_pair, source_text, target_text, status, created_at, completed_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, NOW(), NULL)";

    private static final String INSERT_WITHOUT_ID_SQL =
            "INSERT INTO segments (project_id, language_pair, source_text, target_text, status, created_at, completed_at) " +
            "VALUES (?, ?, ?, ?, ?, NOW(), NULL)";

    public List<Segment> findByProject(int projectId) {
        List<Segment> list = new ArrayList<Segment>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_PROJECT_SQL)) {

            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Segment(
                            rs.getInt("id"),
                            rs.getInt("project_id"),
                            rs.getString("language_pair"),
                            rs.getString("source_text"),
                            rs.getString("target_text"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at"),
                            rs.getTimestamp("completed_at")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void updateSegment(int id, String targetText, String status) {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, targetText);
            ps.setString(2, status);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void upsertSegment(Segment s) {
        if (s == null) {
            return;
        }

        try (Connection con = DBUtil.getConnection()) {
            if (s.getId() > 0) {
                if (exists(con, s.getId())) {
                    try (PreparedStatement ps = con.prepareStatement(UPSERT_UPDATE_SQL)) {
                        ps.setInt(1, s.getProjectId());
                        ps.setString(2, s.getLanguagePair());
                        ps.setString(3, s.getSourceText());
                        ps.setString(4, s.getTargetText());
                        ps.setString(5, s.getStatus());
                        ps.setInt(6, s.getId());
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = con.prepareStatement(INSERT_WITH_ID_SQL)) {
                        ps.setInt(1, s.getId());
                        ps.setInt(2, s.getProjectId());
                        ps.setString(3, s.getLanguagePair());
                        ps.setString(4, s.getSourceText());
                        ps.setString(5, s.getTargetText());
                        ps.setString(6, s.getStatus());
                        ps.executeUpdate();
                    }
                }
            } else {
                insertSegmentForProject(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertSegmentForProject(Segment s) {
        if (s == null) {
            return;
        }

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_WITHOUT_ID_SQL)) {

            ps.setInt(1, s.getProjectId());
            ps.setString(2, s.getLanguagePair());
            ps.setString(3, s.getSourceText());
            ps.setString(4, s.getTargetText());
            ps.setString(5, s.getStatus());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean exists(Connection con, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(EXISTS_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}