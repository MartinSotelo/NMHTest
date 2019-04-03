import java.sql.*;

public class ConnectionManager {
    static BasicConnectionPool masterPool;
    static BasicConnectionPool slavePool;
    static boolean initialized = false;
    static boolean masterUnreachable = false;
    static boolean slaveUnreachable = false;

    /**
     * Mechanism to get connection from Master Db.
     * If Master DB is not available the Slave DB is used.
     * Before use of slave DB if in masterUnreachable mode will try to regain master connection
     * if both are unreachable an exception is thrown
     *
     * @return
     */
    public static Connection getConnection() throws RuntimeException {
        if (!initialized) {
            initialize();
        }
        try (Connection con = masterPool.getConnection();
             PreparedStatement ps = createPreparedStatement(con);
             ResultSet rs = ps.executeQuery()) {
            return con;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (masterUnreachable) {
            regainConnection();
        }
        try (Connection con = masterPool.getConnection();
             PreparedStatement ps = createPreparedStatement(con);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            masterUnreachable = false;
            return con;
        } catch (SQLException e) {
            // do whatever is needed to do
            e.printStackTrace();
        }
        masterUnreachable = true;
        try (Connection con = slavePool.getConnection();
             PreparedStatement ps = createPreparedStatement(con);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            slaveUnreachable = false;
            return con;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        slaveUnreachable = true;
        throw new RuntimeException("Both Databases are unreachable");
    }

    private static void regainConnection() {
        try {
            masterPool = BasicConnectionPool.create("masterUrl", "master", "master");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (slaveUnreachable) {
            try {
                masterPool = BasicConnectionPool.create("masterUrl", "master", "master");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void initialize() {
        try {
            masterPool = BasicConnectionPool.create("masterUrl", "master", "master");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            slavePool = BasicConnectionPool.create("slaveurl", "slave", "slave");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialized = true;
    }

    private static PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        String sql = "SELECT 1";
        PreparedStatement ps = con.prepareStatement(sql);
        return ps;
    }


}
