package io.fyko.droidconnect;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

public class FrmNetCat extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/FrmNetCat.fxml"));
        loader.setController(this);
        Parent root = loader.load();
        Scene mainScene = new Scene(root);
        primaryStage.setTitle("DroidConnect netcat");
        primaryStage.setScene(mainScene);
        primaryStage.setOnCloseRequest(this::FormClosing);
        primaryStage.show();
        primaryStage.setMinWidth(primaryStage.getWidth());
        primaryStage.setMinHeight(primaryStage.getHeight());
        primaryStage.setMaxWidth(primaryStage.getWidth());
        primaryStage.setMaxHeight(primaryStage.getHeight());
    }

    @FXML
    public void initialize() {
        btnSend.setOnAction(this::BtnSendAction);
        txtKey.textProperty().addListener(this::MessageChanged);
        txtContent.textProperty().addListener(this::MessageChanged);
        //root.getScene().getWindow().setOnCloseRequest(this::FormClosing);

        txtHost.setText(Config.getSetting("host"));
        txtPort.setText(Config.getSetting("port"));
        txtContent.setText(Config.getSetting("content"));
        txtKey.setText(Config.getSetting("key"));

    }

    private void FormClosing(WindowEvent windowEvent) {
        Config.saveSetting("host", txtHost.getText());
        Config.saveSetting("port", txtPort.getText());
        Config.saveSetting("content", txtContent.getText());
        Config.saveSetting("key", txtKey.getText());
    }

    private void MessageChanged(Observable observable) {
        if ((txtKey.getText().length() == 16 || txtKey.getText().length() == 32) && txtContent.getText().length() > 0) {
            // generating IV
            SecureRandom sr = new SecureRandom();
            byte[] initVector = new byte[16];
            sr.nextBytes(initVector);

            StringBuilder hexIV = new StringBuilder();
            for (byte b : initVector) {
                String hex = Integer.toHexString(Byte.toUnsignedInt(b));
                if (hex.length() == 1) hex = "0" + hex;

                hexIV.append(hex);
                hexIV.append(" ");
            }
            txtIV.setText(hexIV.toString().trim());

            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                SecretKeySpec skeySpec = new SecretKeySpec(txtKey.getText().getBytes(), "AES");

                cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(initVector));
                byte[] encrypted = cipher.doFinal(txtContent.getText().getBytes());
                String hash = Base64.getEncoder().encodeToString(encrypted);
                txtEncrypted.setText(hash);
            } catch (Exception e) {
                output("Encryption error: " + e.getLocalizedMessage());
            }
        } else {
            txtIV.setText("");
            txtEncrypted.setText("");
        }
    }

    private void BtnSendAction(ActionEvent actionEvent) {
        /*try {
            Socket client = new Socket(getHost(), getPort());
            output("Connection successful");

            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            output("Sending message...");
            out.println(getContent());

            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            StringBuilder msgBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null)
                msgBuilder.append(line);
            client.close();

            String msg = msgBuilder.toString();
            output("Message received: " + msg);

            if (!msg.startsWith("{") && msg.length() > 0) {
                String encrypted = msg.substring(32);
                String iv = msg.substring(0, 32);
                output("Decrypted message: " + decrypt(encrypted, iv));
            }

        } catch (Exception e) {
            output("Connection error: " + e.getLocalizedMessage());
            e.printStackTrace();
        }*/

        new Thread(new Connect()).start();

    }

    private String getHost() {
        return txtHost.getText().length() > 0 ? txtHost.getText() : "localhost";
    }

    private int getPort() {
        return txtPort.getText().length() > 0 ? Integer.parseInt(txtPort.getText()) : 9001;
    }

    private String getContent() {
        if (txtIV.getText().length() > 0) {
            return txtIV.getText().replace(" ", "") + txtEncrypted.getText();
        } else {
            return txtContent.getText().replace("\n", "");
        }
    }

    private void output(String text) {
        String now = LocalDateTime.now().toString();
        txtOutput.insertText(0, now + " - " + text + "\n");
        txtOutput.getParent().layout();
    }

    private String decrypt(String msg, String iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        System.out.println(msg);
        System.out.println(iv);

        byte[] encryptedData = Base64.getDecoder().decode(msg.getBytes());
        byte[] ivArray = new byte[iv.length() / 2];

        for (int i = 0; i < iv.length(); i += 2) {
            ivArray[i / 2] = (byte) ((Character.digit(iv.charAt(i), 16) << 4)
                    + Character.digit(iv.charAt(i+1), 16));
        }

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        SecretKeySpec skeySpec = new SecretKeySpec(txtKey.getText().getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivArray);

        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
        byte[] decrypted = cipher.doFinal(encryptedData);

        return new String(decrypted);
    }

    @FXML private TextField txtHost;
    @FXML private TextField txtPort;
    @FXML private TextField txtKey;
    @FXML private TextField txtIV;
    @FXML private TextArea txtContent;
    @FXML private TextArea txtEncrypted;
    @FXML private TextArea txtOutput;
    @FXML private Button btnSend;

    private class Connect extends Task<Boolean> {

        @Override
        protected Boolean call() {
            try {
                Socket client = new Socket(getHost(), getPort());
                client.setSoTimeout(10000);
                Platform.runLater(() -> output("Connection successful"));

                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                Platform.runLater(() -> output("Sending message..."));
                out.println(getContent());

                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                StringBuilder msgBuilder = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null)
                    msgBuilder.append(line);
                client.close();

                String msg = msgBuilder.toString();
                Platform.runLater(() -> output("Message received: " + msg));

                if (!msg.startsWith("{") && msg.length() > 0) {
                    String encrypted = msg.substring(32);
                    String iv = msg.substring(0, 32);
                    String decoded =  decrypt(encrypted, iv);
                    Platform.runLater(() -> output("Decrypted message: " + decoded));
                }

            } catch (Exception e) {
                Platform.runLater(() -> output("Connection error: " + e.getLocalizedMessage()));
                e.printStackTrace();
                return false;
            }

            return true;
        }
    }
}

