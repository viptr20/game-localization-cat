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
import cat.model.Project;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {

    private static final String FIND_ALL_SQL =
            "SELECT * FROM projects ORDER BY id";

    private static final String FIND_BY_ID_SQL =
            "SELECT * FROM projects WHERE id = ?";

    public List<Project> findAll() {
        List<Project> list = new ArrayList<>();
        try {
            DataSource ds = DBUtil.getDataSource();
            try (Connection con = ds.getConnection();
                 PreparedStatement ps = con.prepareStatement(FIND_ALL_SQL);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Project findById(int id) {
        try {
            DataSource ds = DBUtil.getDataSource();
            try (Connection con = ds.getConnection();
                 PreparedStatement ps = con.prepareStatement(FIND_BY_ID_SQL)) {

                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapRow(rs);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Project mapRow(ResultSet rs) throws SQLException {
        return new Project(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("source_lang"),
                rs.getString("target_langs"),
                rs.getString("status"),
                rs.getString("description")
        );
    }
}