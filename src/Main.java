import java.util.ArrayList;

public class Main {
    public static void main(String[] args){
        try {
            DBConnector DB = new DBConnector();
            ArrayList<String> bois = DB.getUsernames();
            for (int i = 0; i<bois.size(); i++) {
                System.out.println(bois.get(i));
            }

            boolean boii = DB.checkPas("Zretzy", "2062f80093066633876b542212c496501a5e79523cc4ea9b28667dff065afd8f");
            if(boii){System.out.println("Confirmed pas");}

        }catch (Exception e){System.out.println(e.getMessage());}
    }
}
