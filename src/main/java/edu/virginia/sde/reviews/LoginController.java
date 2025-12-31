package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;


public class LoginController {
    LoginModel model;

    public LoginController() {
        model = new LoginModel();
    }

    // TODO: // Login Screen - DONE
    @FXML
    private Label loginText;

    @FXML
    private Label createAccountText;

    @FXML
    private TextField createAccountUsername;

    @FXML
    private TextField createAccountPassword;

    @FXML
    private TextField enterUser;

    @FXML
    private TextField enterPass;

    @FXML
    private TextField enterName;

    @FXML
    private Button loginButton;

    @FXML
    private Button createAccountButton;

    @FXML
    private Button closeAppButton;

    @FXML
    private Label loginErrorLabel;

    @FXML
    private Label createAccountErrorLabel;

    @FXML
    protected void handleLoginButtonAction(ActionEvent event) { //deal with login
        String username = enterUser.getText().strip();
        String password = enterPass.getText().strip();
        if (username.isEmpty()) {
            loginErrorLabel.setText("Please enter a username");
            loginErrorLabel.setStyle("-fx-background-color: red; -fx-text-fill: white");
            return;
        } else if (password.isEmpty()) {
            loginErrorLabel.setText("Please enter a password");
            loginErrorLabel.setStyle("-fx-background-color: red; -fx-text-fill: white");
            return;
        }
        try { // TODO: ADD LOGIN SEQUENCE FUNCTION FROM MODEL - DONE
            model.loginUser(username, password);

            loginErrorLabel.setText("");
            loginErrorLabel.setStyle("-fx-background-color: transparent;");

            // TODO: Switch to new scene code goes here
            FXMLLoader loader = new FXMLLoader(getClass().getResource("course_search.fxml"));
            Parent root = loader.load();

            CourseSearchController controller = loader.getController();
            controller.initData(username);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 720));
            stage.show();

        }
        catch (RuntimeException e) { // TODO: ADD LOGIN SEQUENCE ERROR - DONE
            loginErrorLabel.setText(e.getMessage());
            loginErrorLabel.setStyle("-fx-background-color: red; -fx-text-fill: white");

        } catch (IOException e) {
            loginErrorLabel.setText("Error: Could not log out. ");
            loginErrorLabel.setStyle("-fx-text-fill: #e74c3c;");
            e.printStackTrace();
        }

    }

    @FXML
    protected void handleCreateAccountAction(ActionEvent event) throws Exception { //changes scene to create account scene
        String potentialUsername = createAccountUsername.getText().strip();
        String potentialPassword = createAccountPassword.getText().strip();
        if (potentialUsername.isEmpty()) {
            createAccountErrorLabel.setText("Please enter a username.");
            createAccountErrorLabel.setStyle("-fx-background-color: red; -fx-text-fill: white");
            return;
        } else if (potentialPassword.isEmpty()) {
            createAccountErrorLabel.setText("Please enter a password.");
            createAccountErrorLabel.setStyle("-fx-background-color: red; -fx-text-fill: white");
            return;
        }
        try { // TODO: ADD CREATE ACCOUNT METHOD CALL - DONE
            model.addNewUser(potentialUsername, potentialPassword);

            createAccountErrorLabel.setText("You have created a new account!");
            createAccountErrorLabel.setStyle("-fx-background-color: green; -fx-text-fill: white");
        } catch (RuntimeException e) { // TODO: ADD CREATE ACCOUNT ERROR SEQUENCE - DONE

            createAccountErrorLabel.setText(e.getMessage());
            createAccountErrorLabel.setStyle("-fx-background-color: red; -fx-text-fill: white");
        }

    }

    @FXML
    protected void handleCloseAppButton(ActionEvent event) {
        model.quitDatabase();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
