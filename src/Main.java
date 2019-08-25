import java.util.ArrayList;

public class Main {
    public static void main(String[] args){
        try {
            DBConnector DB = new DBConnector();
            ArrayList<String> bois = DB.getUsernames();
            for (int i = 0; i<bois.size(); i++) {
                System.out.println(bois.get(i));
            }
        }catch (Exception e){System.out.println(e.getMessage());}
    }
}
