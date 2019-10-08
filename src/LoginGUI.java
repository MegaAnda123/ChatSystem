import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.IOException;

public class LoginGUI {

    private static Stage window;

    public static void close() {
        window.close();
    }

    public static void display(final GUIClient client) {
        window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Login");

        window.setMinWidth(350);
        window.setMinHeight(200);
        window.setResizable(false);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10,10,10,10));
        grid.setVgap(8);
        grid.setHgap(10);

        Label serverIpLabel = new Label("Server IP");
        GridPane.setConstraints(serverIpLabel,0,0);

        final TextField serverIpField = new TextField();
        serverIpField.setPromptText("Default port 42069");

        final Button connectButton = new Button("Connect");
        connectButton.setMinWidth(80);

        Label usernameLabel = new Label("Username");
        GridPane.setConstraints(usernameLabel,0,1);

        final TextField usernameField = new TextField();
        GridPane.setConstraints(usernameField,1,1);

        Label passwordLabel = new Label("Password");
        GridPane.setConstraints(passwordLabel,0,2);

        final TextField passwordField = new PasswordField();
        GridPane.setConstraints(passwordField,1,2);

        final Button loginButton = new Button("Login");
        loginButton.setDisable(true);

        final RadioButton signupButton = new RadioButton("Signup");

        HBox hBoxServerIp = new HBox(10);
        hBoxServerIp.getChildren().addAll(serverIpField,connectButton);
        GridPane.setConstraints(hBoxServerIp,1,0);

        HBox hBoxButtons = new HBox(10);
        hBoxButtons.getChildren().addAll(loginButton,signupButton);
        GridPane.setConstraints(hBoxButtons,1,5);

        grid.getChildren().addAll(serverIpLabel,hBoxServerIp,usernameLabel,usernameField,passwordLabel,passwordField,hBoxButtons);

        Scene scene = new Scene(grid, 10,10);
        window.setScene(scene);

        window.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });


        connectButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    if(client.connect(serverIpField.getText())) {
                        loginButton.setDisable(false);
                        connectButton.setDisable(true);
                        connectButton.setText("Connected");
                        connectButton.setTextFill(Color.GREEN);
                        serverIpField.setDisable(true);
                    } else {
                        AlertBox.display("Connection error","Connecting to server failed");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println(signupButton.isSelected());
                if (signupButton.isSelected()) {
                    try {
                        client.trySignup(usernameField.getText(), passwordField.getText());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        client.tryLogin(usernameField.getText(), passwordField.getText());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        window.showAndWait();
    }
}
