/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.dao;

/**
 *
 * @author vanyaramirez
 */

import cat.db.DBUtil;
import cat.model.Segment;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SegmentDAO {

    private static final String FIND_BY_PROJECT_SQL =
            "SELECT * FROM segments WHERE project_id = ? ORDER BY id";

    private static final String UPDATE_SQL =
            "UPDATE segments SET target_text = ?, status = ? WHERE id = ?";

    public List<Segment> findByProject(int projectId) {
        List<Segment> list = new ArrayList<>();
        try {
            DataSource ds = DBUtil.getDataSource();
            try (Connection con = ds.getConnection();
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void updateSegment(int id, String targetText, String status) {
        try {
            DataSource ds = DBUtil.getDataSource();
            try (Connection con = ds.getConnection();
                 PreparedStatement ps = con.prepareStatement(UPDATE_SQL)) {

                ps.setString(1, targetText);
                ps.setString(2, status);
                ps.setInt(3, id);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
