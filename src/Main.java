import java.sql.Connection;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args){
        try {
            DBConnector DB = new DBConnector();
            Connection con = DB.getConnection();

            DB.createServerTable(con,"server1","data");

        }catch (Exception e){System.out.println(e.getMessage());}
    }
}
