import java.sql.*;
import java.util.ArrayList;


public class Database {

    Statement stm;

    Database() {
        //connect to database
        try {
            //Connect to Sql server on localhost
            Connection con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/data", "AdminBoy", "ass");
            // create statement
            Statement stm = con.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] getUsernames(){
        String[] results = null;
        try {
            //Get selected data from database
            ResultSet result = stm.executeQuery("select username from users");
            // create arraylist for storage
            ArrayList<String> usernames = new ArrayList<String>();

            // Add each name to a separate array index
            while(result.next()){
                usernames.add(result.getString("username"));
            }

            // transfer to NON Arraylist String Array, for ease of use
            results = new String[usernames.size()];
            results = usernames.toArray(results);

            for (int i = 0; i < results.length; i++){
                System.out.println(results[i]);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }



        return results;
    }
}
