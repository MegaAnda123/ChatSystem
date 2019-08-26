import java.util.ArrayList;

public class Main {
    public static void main(String[] args){
        try {
            DBConnector DB = new DBConnector();

            DB.createHash("bob");

        }catch (Exception e){System.out.println(e.getMessage());}
    }
}
