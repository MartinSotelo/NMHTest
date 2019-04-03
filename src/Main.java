import java.sql.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        ConnectionManager cm = new ConnectionManager();
        Connection connection = cm.getConnection();
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String sql;
        sql = "SELECT value FROM Table";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                String value  = rs.getString(0);
                System.out.println(value);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
