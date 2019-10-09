import javafx.application.Application;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import java.io.IOException;
import javafx.scene.control.TextArea;
import javafx.scene.control.ListView;

public class ClientGUI extends Application {
    public TextField ChatTextField;
    public TextArea ChatArea;
    public ListView ClientList;
    public GUIClient client;
    public String privateMessageClient = "";

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
            if(chatText.isEmpty()) {
                if (privateMessageClient.isEmpty()) {
                    client.processOutMessage("msg", chatText);
                } else {
                    client.processOutMessage("privmsg", privateMessageClient + " " + chatText);
                }
            } else {
                if (chatText.substring(0, 1).equals("/")) {
                    client.sendNewMessage(chatText.substring(1));
                } else {
                    if (privateMessageClient.isEmpty()) {
                        client.processOutMessage("msg", chatText);
                    } else {
                        client.processOutMessage("privmsg", privateMessageClient + " " + chatText);
                    }
                }
            }

            ChatTextField.setText("");
        }
    }

    public void ClientListClicked() {
        String client = (ClientList.getSelectionModel().getSelectedItem()).toString();
        privateMessageClient = client;
        ChatTextField.setPromptText("Send private message to " + client);
    }

    public void ChatFieldClicked() {
        ClientList.getSelectionModel().clearSelection();
        privateMessageClient = "";
        ChatTextField.setPromptText("Send public message");
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
