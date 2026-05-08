package cat.db;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    private static DataSource dataSource;

    /**
     * Първо опитваме JNDI jdbc/CATDB (за локален GlassFish),
     * ако го няма – връщаме null и ще минем на директен JDBC.
     */
    public static DataSource getDataSource() {
        if (dataSource == null) {
            try {
                Context ctx = new InitialContext();
                dataSource = (DataSource) ctx.lookup("jdbc/CATDB");
            } catch (NamingException e) {
                // няма конфигуриран JNDI ресурс – ок за Docker
                dataSource = null;
            }
        }
        return dataSource;
    }

    /**
     * Unified начин да се вземе Connection.
     * - ако има JNDI DataSource -> него;
     * - иначе -> директен MariaDB JDBC с параметри от env vars.
     */
    public static Connection getConnection() throws SQLException {
        DataSource ds = getDataSource();
        if (ds != null) {
            return ds.getConnection();
        }

        // Fallback: директен JDBC по env variables (подходящо за Docker/HSS)
        String host = getenvOrDefault("DB_HOST", "localhost");
        String port = getenvOrDefault("DB_PORT", "3306");
        String db   = getenvOrDefault("DB_NAME", "catdb");
        String user = getenvOrDefault("DB_USER", "root");
        String pass = getenvOrDefault("DB_PASS", "rootpass");

        String url = "jdbc:mariadb://" + host + ":" + port + "/" + db + "?useSSL=false&serverTimezone=UTC";

        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MariaDB JDBC Driver not found in classpath", e);
        }

        return DriverManager.getConnection(url, user, pass);
    }

    private static String getenvOrDefault(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? def : v;
    }

    private DBUtil() {
    }
}