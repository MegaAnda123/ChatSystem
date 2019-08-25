import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javax.swing.text.html.ListView;
import java.awt.*;
import java.io.IOException;
import javafx.scene.control.TextArea;

public class ClientGUI extends Application {
    public TextField ChatTextField;
    public TextArea ChatArea;
    public ListView ClientList;
    public Client1 client;

    public void initialize() throws IOException, InterruptedException {
        client = new Client1();
        client.start(true,this);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("oooga");

    }

    public void ChatFieldTyping(KeyEvent ke) {
        if (ke.getCode().equals(KeyCode.ENTER)) {
            String chatText = ChatTextField.getText();
            client.sendNewMessage(chatText);
            ChatTextField.setText("");
        }
    }

    public void displayNewMessage(String msg) {
        String temp = ChatArea.getText();
        temp = temp + msg + "\n";
        ChatArea.setText(temp);
        ChatArea.setScrollTop(10000);
    }
}
