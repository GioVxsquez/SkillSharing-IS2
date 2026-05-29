import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDB {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://dpg-d8cg79favr4c73ec86p0-a.ohio-postgres.render.com/skillsharing_db_qauy?sslmode=require";
        String user = "skillsharing_db_qauy_user";
        String pass = "8i5y6ogzMmybr8SRVzAYd8HntiXrxcyE";
        try (Connection c = DriverManager.getConnection(url, user, pass);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM habilidad")) {
            if (rs.next()) {
                System.out.println("SUCCESS: Connected! Found " + rs.getInt(1) + " skills in Render DB.");
            }
        } catch (Exception e) {
            System.out.println("FAIL: " + e.getMessage());
        }
    }
}
