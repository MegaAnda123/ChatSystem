import java.util.ArrayList;

public class Main {
    public static void main(String[] args){
        try {
            DBConnector DB = new DBConnector();

            Chat ch = new Chat(1);
            Message me = new Message("aiojdaoshndoiasjdh","10000", "bob");

            ch.addMessage(me);

            DB.storeChatObject(ch);

        }catch (Exception e){System.out.println(e.getMessage());}
    }
}
