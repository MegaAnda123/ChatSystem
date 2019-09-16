import javafx.application.Application;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.util.ArrayList;

import javafx.scene.control.TextArea;
import javafx.scene.control.ListView;

public class ClientGUI extends Application {
    public TextField ChatTextField;
    public TextArea ChatArea;
    public ListView ClientList;
    public GUIClient client;

    public void initialize() throws IOException, InterruptedException {
        client = new GUIClient();
        client.start(this);
    }

    @Override
    public void start(Stage primaryStage) {
    }

    public void ChatFieldTyping(KeyEvent ke) throws IOException {
        if (ke.getCode().equals(KeyCode.ENTER)) {
            String chatText = ChatTextField.getText();
            client.processOutMessage("msg",chatText);
            ChatTextField.setText("");
        }
    }

    public void displayNewMessage(String msg) {
        String temp = ChatArea.getText();
        temp = temp + msg + "\n";
        ChatArea.setText(temp);
        ChatArea.setScrollTop(10000);
    }

    public void setClientList(String[] newClientList) {
        ClientList.getItems().clear();
        for(String client : newClientList) {
            ClientList.getItems().add(client);
        }

    }
}
