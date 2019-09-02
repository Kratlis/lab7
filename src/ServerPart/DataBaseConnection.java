package ServerPart;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseConnection {
    private String DB_URL = "jdbc:postgresql://localhost:5432/collection";
    private String LOGIN = "postgres";
    private String PASSWORD = "KatyaSQL";
    private Connection connection = null;
    
    DataBaseConnection() {}
    
    public static void main(String[] args) throws SQLException {
        DataBaseConnection dbConnection = new DataBaseConnection();
        dbConnection.connect();
        dbConnection.connection.createStatement();
        
    }
    boolean connect(){
        try{
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver isn't found.");
            return false;
        }
        if (connection == null){
            try {
                connection = DriverManager.getConnection(DB_URL, LOGIN, PASSWORD);
                System.out.println("Connection is created.");
                return true;
            } catch (SQLException e) {
                System.out.println("Connection failed");
                return false;
            }
        }
        return false;
    }
    
    public Connection getConnection() {
        return connection;
    }
}
