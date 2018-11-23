package controllers;

import com.jfoenix.controls.*;
import de.uniba.ktr.Entities.Message;
import de.uniba.ktr.Libs.Helper;

import javafx.application.Platform;
import javafx.fxml.*;
import javafx.scene.text.Text;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Date;

public class ClientScreenController {
    // Control References
    @FXML
    private JFXTextField serverIpAddressTextField;

    @FXML
    private JFXTextField serverListenPortTextField;

    @FXML
    private JFXTextField clientSendPortTextField;

    @FXML
    private JFXTextField messageSubjectTextField;

    @FXML
    private JFXTextArea messageContentTextArea;

    @FXML
    private JFXButton sendButton;

    @FXML
    private Text statusLabel;

    // other properties
    private String protocol;

    // Constructor
    public ClientScreenController() { }

    @FXML
    private void initialize() {
        /*
        serverIpAddressTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("\\d+(\\.\\d+)*")) {
                serverIpAddressTextField.setText(newValue.replaceAll("[^\\d+(\\.\\d+)*$]", ""));
            }
        });
        */
        serverListenPortTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("\\d*")) {
                serverListenPortTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        serverListenPortTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("\\d*")) {
                serverListenPortTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Control handle methods
        sendButton.setOnAction((v) -> {
            if(this.protocol.equals("TCP")) {
                sendTcpPacket();
            }
            else {
                sendUdpPacket();
            }
        });
    }

    // getter and setter methods
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    // private methods
    private void sendTcpPacket() {
        try {
            // create new message
            Message message = new Message("abcd", messageSubjectTextField.getText(),
                    messageContentTextArea.getText(), new Date());

            // create temporary sending socket
            InetAddress serverAddr = InetAddress.getByName(serverIpAddressTextField.getText());
            int serverPort = Integer.parseInt(serverListenPortTextField.getText());
            Socket sSocket = new Socket(serverAddr, serverPort);

            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(sSocket.getOutputStream()));

            // send tcp packet
            out.writeObject(message);
            out.flush();

            // close stream & socket
            out.close();
            sSocket.close();
            printStatus(true, this.protocol + " | Send message to " +
                    serverIpAddressTextField.getText() + " successfully!");
        }
        catch (IOException | NumberFormatException e) {
            printStatus(false, this.protocol + " | Send message to " +
                    serverIpAddressTextField.getText() + " failed!");
        }
    }

    private void sendUdpPacket() {
        try {
            // create new message
            Message message = new Message("abcd", messageSubjectTextField.getText(),
                    messageContentTextArea.getText(), new Date());

            // create temporary sending socket
            DatagramSocket sSocket = new DatagramSocket();
            InetAddress serverAddr = InetAddress.getByName(serverIpAddressTextField.getText());
            int serverPort = Integer.parseInt(serverListenPortTextField.getText());

            // convert message to byte array
            byte[] mesgContain = Helper.objectToStream(message);
            DatagramPacket mesgPacket = new DatagramPacket(mesgContain, mesgContain.length,
                    serverAddr, serverPort);

            // send udp packet
            sSocket.send(mesgPacket);

            // close socket
            sSocket.close();

            // print status
            printStatus(true, this.protocol + " | Send message to " +
                    serverIpAddressTextField.getText() + " successfully!");

        } catch (IOException | NumberFormatException e) {
            printStatus(true, this.protocol + " | Send message to " +
                    serverIpAddressTextField.getText() + " successfully!");
        }
    }

    private void printStatus(boolean success, String message) {
        Platform.runLater(() -> {
            statusLabel.getStyleClass().clear();
            statusLabel.getStyleClass().add( success ? "success-text" : "error-text");
            statusLabel.setText(message);
        });
    }
}
