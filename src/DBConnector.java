import java.sql.*;
import java.util.ArrayList;

import com.mysql.jdbc.Driver;

public class DBConnector{


    public Connection getConnection() throws Exception{
        try{
            String host = "jdbc:mysql://localhost:3306/data";
            String username = "AdminBoy";
            String pass = "ass";

            Connection conn = DriverManager.getConnection(host, username, pass);

            System.out.println("Connection established.");

            return conn;

        }catch(Exception e){System.out.println(e.getMessage());}

        return null;
    }


    // Returns a list of all usernames in users table
    public ArrayList<String> getUsernames() throws Exception{

        try {
            Connection con = getConnection();
            Statement stm = con.createStatement();

            ResultSet res = stm.executeQuery("select * from users");

            ArrayList<String> result = new ArrayList<String>();

            while(res.next()){
                result.add(res.getString("username"));
            }

            return result;

        }catch (Exception e){System.out.println(e.getMessage());}

        return null;
    }

}