import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection
{
    public static Connection getConnection() throws Exception
    {
        if (connection == null)
        {
            DriverManager.registerDriver(new org.postgresql.Driver());
            connection = DriverManager.getConnection(Settings.DB_URI, Settings.DB_USER, Settings.DB_PASSWD);
        }
        return connection;
    }

    private static Connection connection = null;
}
