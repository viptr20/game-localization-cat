package cat.db;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DBUtil {

    private static DataSource dataSource;

    public static DataSource getDataSource() {
        if (dataSource == null) {
            try {
                Context ctx = new InitialContext();
                dataSource = (DataSource) ctx.lookup("jdbc/CATDB");
            } catch (NamingException e) {
                throw new RuntimeException("Failed to lookup DataSource jdbc/CATDB", e);
            }
        }
        return dataSource;
    }

    private DBUtil() {
    }
}