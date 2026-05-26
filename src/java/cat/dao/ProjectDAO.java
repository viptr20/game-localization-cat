package cat.dao;

import cat.db.DBUtil;
import cat.model.Project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {

    private static final String FIND_ALL_SQL =
            "SELECT * FROM projects ORDER BY id";

    private static final String FIND_BY_ID_SQL =
            "SELECT * FROM projects WHERE id = ?";

    private static final String EXISTS_SQL =
            "SELECT COUNT(*) FROM projects WHERE id = ?";

    private static final String INSERT_SQL =
            "INSERT INTO projects (id, name, source_lang, target_langs, status, description) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String INSERT_WITHOUT_ID_SQL =
            "INSERT INTO projects (name, source_lang, target_langs, status, description) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE projects SET name = ?, source_lang = ?, target_langs = ?, status = ?, description = ? WHERE id = ?";

    public List<Project> findAll() {
        List<Project> list = new ArrayList<Project>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Project findById(int id) {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_ID_SQL)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void upsertProject(Project p) {
        if (p == null) {
            return;
        }

        try (Connection con = DBUtil.getConnection()) {
            if (p.getId() > 0) {
                if (exists(con, p.getId())) {
                    try (PreparedStatement ps = con.prepareStatement(UPDATE_SQL)) {
                        ps.setString(1, p.getName());
                        ps.setString(2, p.getSourceLang());
                        ps.setString(3, p.getTargetLangs());
                        ps.setString(4, p.getStatus());
                        ps.setString(5, p.getDescription());
                        ps.setInt(6, p.getId());
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = con.prepareStatement(INSERT_SQL)) {
                        ps.setInt(1, p.getId());
                        ps.setString(2, p.getName());
                        ps.setString(3, p.getSourceLang());
                        ps.setString(4, p.getTargetLangs());
                        ps.setString(5, p.getStatus());
                        ps.setString(6, p.getDescription());
                        ps.executeUpdate();
                    }
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement(INSERT_WITHOUT_ID_SQL)) {
                    ps.setString(1, p.getName());
                    ps.setString(2, p.getSourceLang());
                    ps.setString(3, p.getTargetLangs());
                    ps.setString(4, p.getStatus());
                    ps.setString(5, p.getDescription());
                    ps.executeUpdate();
                }
            }
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