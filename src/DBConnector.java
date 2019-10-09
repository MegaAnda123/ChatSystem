import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;

/**
 * @Author Mikael Nilssen
 * @Date September 2019
 *
 * Connector to sql database for chat app
 * depends on jdbc sql connector external library.
 */
public class DBConnector{

    /**
     * Gets connection Object that is used in other functions to connect to your sql database.
     * Password and Username is currently just hardcoded.
     * @return Returns Connection Object
     * @throws Exception
     */
    public Connection getConnection() throws Exception{
        try{
            String host = "jdbc:mysql://localhost:3306/data?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
            String username = "root";
            String pass = "ass1";

            Connection conn = DriverManager.getConnection(host, username, pass);

            System.out.println("SQL connection established.");

            return conn;

        }catch(Exception e){System.out.println(e.getMessage());}

        return null;
    }

    /**
     *  Get all usernames
     * @param con Connection to database
     * @return Arraylist"<String>" of all usernames in the users table
     * @throws Exception
     */
    public ArrayList<String> getUsernames(Connection con) throws Exception{

        try {
            // Connects to database
            Connection connection = con;
            Statement stm = connection.createStatement();

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


    /**
     * Creates a SHA-256 hashed string from the input
     * @param password A string that u want turned into a SHA-256 HASH
     * @return HASH in SHA-256 Form
     */
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


    /**
     * Checks if the username and hashed version of the password are correct towards the login info on the Database
     * @param con Connection object
     * @param username Login username
     * @param hash SHA-256 Hash of password
     * @return Returns a boolean
     * @throws Exception
     */
    public boolean checkPas(Connection con, String username, String hash) throws Exception{

        try {
            // Connects to database
            Connection connection = con;
            Statement stm = connection.createStatement();

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

    /**
     * Stores a message object on the database
     * @param con Connection object
     * @param o Message object containing Message, sender, date, serverName
     */
    public void storeMessage(Connection con, Message o){

        String message = o.getMessage();
        String sender = o.getSender();
        String date = o.getDate();
        String serverName = o.getServerName();

        try{

            // Connects to database
            Connection connection = con;
            Statement stm = connection.createStatement();

            // check if server exists

            boolean tab = checkTable(connection,serverName,"data");


            // Upload message to Database
            if (tab) {
                try {
                    String queryMsg = "INSERT INTO " + serverName + " (message,sender,date) VALUES(" + message + "," + sender + "," + date + ");";
                    stm.executeQuery(queryMsg);
                } catch (Exception e) {
                    System.out.println("Upload problem");
                    System.out.println(e.getMessage());
                }
            }
            else {
                System.out.println("message was not uploaded due to missing server.");
            }


        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    /**
     *  Checks all tables in the given schema and returns boolean TRUE if located
     * @param con Connection object to sql server
     * @param serverName name of the sql server table
     * @param schemaName name of the sql schema the table is located in
     * @return boolean of found or not
     */
    public boolean checkTable(Connection con, String serverName, String schemaName){

        boolean serverExists = false;

       try {
           // Connects to database
           Connection connection = con;
           Statement stm = connection.createStatement();

           //Make Servername all small cases
           String servername = serverName.toLowerCase();

           //get tables
           ResultSet servers = stm.executeQuery("SELECT * FROM information_schema.tables WHERE table_schema='"+schemaName+"';");

           //checks all table name rows for servername
           while(servers.next()){
               if(servers.getString("table_name").equals(servername)){
                   serverExists = true;
               }
           }

       }
       catch (Exception e){
           System.out.println(e.getMessage());
       }

       if(serverExists){
           System.out.println("Server already exists");
       }
       return serverExists;
    }

    /**
     * Creates a new table for storage of messages on that server
     * @param con Connection object
     * @param serverName    Name of Server table
     * @param schemaName    Name of schema of table
     */
    public void createServerTable(Connection con, String serverName, String schemaName){

        try{
            String[] s = serverName.split("\\s");
            int wordsS = s.length;
            if(wordsS != 1){
                System.out.println("Servername must be 1 Word");
            }
            else {

                String query = "CREATE TABLE " + serverName + "(message_id INT auto_increment,message VARCHAR(400), date VARCHAR(20), sender VARCHAR(30), PRIMARY KEY (message_id));";

                // Connects to database
                Connection connection = con;
                Statement stm = connection.createStatement();

                //Check server
                boolean exists = checkTable(con, serverName, schemaName);

                if (!exists) {
                    // send query
                    System.out.println("Executing: " + query);
                    stm.execute(query);
                } else {
                    System.out.println("Server already exists");
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Adds a user to the user table on the server
     * @param con Connection object for sql interaction
     * @param username Username in app
     * @param email Email for login
     * @param hashedPassword a hashed version of the password
     * @param joined Date person joined the server
     * @param Level Premisiion level
     */
    public void addUser(Connection con, String username, String email, String hashedPassword, String joined, int Level ){

        try {
            // Create statement
            Connection connection = con;
            Statement stm = connection.createStatement();

            // Query SQL
            String query = "INSERT INTO users (username, email, hashedPassword, joined, PLevel) VALUES ('"+username+"','"+email+"','"+hashedPassword+"','"+joined+"',"+Level+");";

            // Send query
            System.out.println("Ran this query: "+query);
            stm.execute(query);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}