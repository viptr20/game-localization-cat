package cat.dao;

import cat.db.DBUtil;
import cat.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class UserDAO {

    private static final String LOGIN_SQL =
            "SELECT id, username, password, full_name, role " +
            "FROM users WHERE username = ? AND password = ?";

    public User validate(String username, String password) {
        User user = null;

        try (Connection con = DBUtil.getConnection()) {

            // DEBUG – може да ги махнеш след като всичко тръгне
            System.out.println("DEBUG username = [" + username + "]");
            System.out.println("DEBUG password = [" + password + "]");
            System.out.println("DEBUG catalog = " + con.getCatalog());

            try (Statement st = con.createStatement();
                 ResultSet dbg = st.executeQuery("SELECT COUNT(*) FROM users")) {
                if (dbg.next()) {
                    System.out.println("DEBUG users count (from Java) = " + dbg.getInt(1));
                }
            }

            try (PreparedStatement ps = con.prepareStatement(LOGIN_SQL)) {
                ps.setString(1, username);
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        user = new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("full_name"),
                                rs.getString("role")
                        );
                        System.out.println("DEBUG FOUND USER = " + user.getUsername()
                                + ", role=" + user.getRole());
                    } else {
                        System.out.println("DEBUG NO USER FOUND");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }
}