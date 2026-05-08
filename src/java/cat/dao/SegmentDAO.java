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

    public List<Segment> findByProject(int projectId) {
        List<Segment> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_PROJECT_SQL)) {

            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Segment(
                            rs.getInt("id"),
                            rs.getInt("project_id"),
                            rs.getString("source_text"),
                            rs.getString("target_text"),
                            rs.getString("status")
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
}