import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

            // cycles through each row and gets the string in column username and adds to array
            while(res.next()){
                result.add(res.getString("username"));
            }

            return result;

        }catch (Exception e){System.out.println(e.getMessage());}

        return null;
    }

    //creates a sha256 hash of input string
    public String createHash(String password){

        try {
            // set encoding
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            //turn string to bytes
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));


            StringBuffer hexString = new StringBuffer();

            //Encodes
            for (int i = 0; i < encodedhash.length; i++){
                String hex = Integer.toHexString(0xff & encodedhash[i]);
                if (hex.length()==1){
                    hexString.append('0');
                }
                hexString.append(hex);
            }


            String hash = hexString.toString();
            
            return hash;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    // Returns a list of all usernames in users table
    public boolean checkPas(String username, String hash) throws Exception{

        try {
            // Connects to database
            Connection con = getConnection();
            Statement stm = con.createStatement();

            // SQL COMMAND
            String query = "SELECT hashpas FROM users WHERE username='"+username+"'";

            // Sends query and returns result
            ResultSet res = stm.executeQuery(query);

            //selects the first row that is not just the column names
             res.next();

             //Checks if hashes are the same and returns true
             if(res.getString(1).equals(hash)){
                 System.out.println("Password confirmed");
                 return true;
             }
             //for false reply
            System.out.println("Authentication failed");
            return false;

        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        // Needed for return if try doesnt reach returns
        return false;
    }

}