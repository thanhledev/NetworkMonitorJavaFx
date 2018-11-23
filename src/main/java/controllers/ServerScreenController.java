package controllers;

import com.jfoenix.controls.*;
import de.uniba.ktr.Entities.Message;
import javafx.application.Platform;
import javafx.fxml.*;
import javafx.scene.text.Text;
import processors.JfxTcpServerProcessor;
import processors.JfxUdpServerProcessor;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class ServerScreenController {
    // TextField References
    @FXML
    private JFXTextField listenPortTextField;

    // Button References
    @FXML
    private JFXButton startButton;

    @FXML
    private JFXButton stopButton;

    // TextArea References
    @FXML
    private JFXTextArea serverLog;

    // Label References
    @FXML
    private Text statusLabel;

    // other properties
    private String protocol;
    private Thread producerThread;
    private Thread consumerThread;

    private final Queue shareQ;
    private final int maxSize = 10;
    private DatagramSocket udpSocket;
    private ServerSocket tcpSocket;

    // Constructor
    public ServerScreenController() {
        shareQ = new LinkedList<Message>();
    }

    @FXML
    private void initialize() {
        listenPortTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("\\d*")) {
                listenPortTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        startButton.setOnAction(e -> {
            startServer();
            Platform.runLater(() -> {
                startButton.setMouseTransparent(true);
                stopButton.setMouseTransparent(false);
                statusLabel.getStyleClass().clear();
                statusLabel.getStyleClass().add("success-text");
                statusLabel.setText("Start listening on port " + listenPortTextField.getText());
            });
        });

        stopButton.setOnAction(e -> {
            stopServer();
            Platform.runLater(() -> {
                stopButton.setMouseTransparent(true);
                startButton.setMouseTransparent(false);
                statusLabel.getStyleClass().clear();
                statusLabel.getStyleClass().add("error-text");
                statusLabel.setText("Stop listening on port " + listenPortTextField.getText());
            });
        });
    }

    // Getter and setter methods
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    // private methods
    private void startServer() {

        try {

            // Initialize consumer thread
            // Create a Runnable
            Runnable consumerTask = () -> runConsumerTask();

            // Run the task in a background thread
            consumerThread = new Thread(consumerTask);
            // Terminate the thread if the application exits
            consumerThread.setDaemon(true);
            // Start the thread
            consumerThread.start();

            if(this.protocol.equals("TCP")) {
                tcpSocket = new ServerSocket(Integer.parseInt(listenPortTextField.getText()));

                // Initialize producer thread - mode tcp
                Runnable producerTask = () -> runProducerTask();

                producerThread = new Thread(producerTask);
                producerThread.setDaemon(true);
                producerThread.start();

            } else {
                // Initialize the socket
                udpSocket = new DatagramSocket(Integer.parseInt(listenPortTextField.getText()));

                // Initialize producer thread - mode udp
                producerThread = new Thread(new JfxUdpServerProcessor(udpSocket, shareQ, maxSize));
                producerThread.setDaemon(true);
                producerThread.start();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void runProducerTask() {
        try {
            while (true) {
                Socket socket = tcpSocket.accept();
                System.out.println("New client connected");

                Thread newClientThread = new Thread(new JfxTcpServerProcessor(socket, shareQ, maxSize));
                newClientThread.setDaemon(true);
                newClientThread.start();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    private void runConsumerTask() {
        while (true) {
            try {
                synchronized (this.shareQ) {
                    while (shareQ.isEmpty()) {
                        shareQ.wait();
                    }
                }

                Message message = (Message) shareQ.poll();
                Platform.runLater(() -> {
                    this.serverLog.appendText(message.shortDescription() + "\n");
                });
                System.out.println(message.toString());
                synchronized (shareQ) {
                    shareQ.notify();
                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    private void stopServer() {
        // Stop the thread
        producerThread.interrupt();
        consumerThread.interrupt();

        // Check for protocol TCP or UDP
        try {
            if(this.protocol.equals("TCP")) {
                tcpSocket.close();
            } else {
                // Close the listening socket
                udpSocket.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
