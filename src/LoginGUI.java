import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.io.IOException;

public class LoginGUI {

    private static Stage window;

    public static void close() {
        window.close();
    }

    public static void display(GUIClient client) {
        window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Login");

        window.setMinWidth(250);
        window.setMinHeight(180);
        window.setResizable(false);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10,10,10,10));
        grid.setVgap(8);
        grid.setHgap(10);

        javafx.scene.control.Label usernameLabel = new javafx.scene.control.Label("Username");
        GridPane.setConstraints(usernameLabel,0,1);

        final javafx.scene.control.TextField usernameField = new javafx.scene.control.TextField();
        GridPane.setConstraints(usernameField,1,1);

        javafx.scene.control.Label passwordLabel = new javafx.scene.control.Label("Password");
        GridPane.setConstraints(passwordLabel,0,2);

        final javafx.scene.control.TextField passwordField = new javafx.scene.control.PasswordField();
        GridPane.setConstraints(passwordField,1,2);

        javafx.scene.control.Button loginButton = new Button("Login");

        javafx.scene.control.RadioButton signupButton = new RadioButton("Signup");

        HBox hBox = new HBox(10);
        hBox.getChildren().addAll(loginButton,signupButton);
        GridPane.setConstraints(hBox,1,5);

        grid.getChildren().addAll(usernameLabel,usernameField,passwordLabel,passwordField,hBox);

        Scene scene = new Scene(grid, 10,10);
        window.setScene(scene);

        window.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
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
