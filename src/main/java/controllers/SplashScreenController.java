package controllers;

import com.jfoenix.controls.*;
import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.*;

public class SplashScreenController {
    // combo-box references
    @FXML
    private JFXComboBox<String> modeComboBox;

    @FXML
    private JFXComboBox<String> protocolComboBox;

    // button references
    @FXML
    private JFXButton proceedButton;

    // label references
    @FXML
    private Text statusLabel;

    // mainStage
    private Stage mainStage;

    // Constructor
    public SplashScreenController() {
    }

    // Initialize method
    @FXML
    private void initialize() {
        proceedButton.setOnAction(e -> {
            proceedButtonHandle();
        });
    }

    // control handle methods
    private void modeComboBoxHandle() {

    }

    private void protocolComboBoxHandle() {

    }

    private void proceedButtonHandle() {
        try {
            String modeVal = modeComboBox.getValue();
            String protocolVal = protocolComboBox.getValue();

            FXMLLoader loader = new FXMLLoader();
            Parent root;

            if(modeVal.equals("Server")) {
                loader.setLocation(getClass().getResource("/views/serverscreen.fxml"));
                root = loader.load();
                ServerScreenController controller = loader.getController();
                controller.setProtocol(protocolVal);
            }
            else {
                loader.setLocation(getClass().getResource("/views/clientscreen.fxml"));
                root = loader.load();
                ClientScreenController controller = loader.getController();
                controller.setProtocol(protocolVal);
            }

            mainStage.setTitle("Network Monitor - " + modeVal + " - " + protocolVal);
            mainStage.setScene(new Scene(root, 800, 600));
            mainStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("You have to specify both MODE and PROTOCOL");
        }
    }

    // public methods
    public void setStage(Stage stage) {
        mainStage = stage;
    }
}
