import javafx.application.Platform;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

public class GUIClient {
    Socket socket;
    ReceiveStringClient receiver = new ReceiveStringClient(this);
    InputStream inStream;
    InputStreamReader in;
    BufferedReader bf;
    boolean connected = false;
    DataOutputStream outStream;
    PrintWriter pr;
    ClientGUI clientGUI;

    public void start(ClientGUI clientGUI) throws IOException, InterruptedException {
        this.clientGUI = clientGUI;
        while (connected==false) {

            try {
                socket = new Socket("83.243.160.197", 42069);
                connected=true;
            } catch (ConnectException e) {
                System.out.println("Failed to connect");
                TimeUnit.SECONDS.sleep(1);
            }
        }

        outStream = new DataOutputStream(socket.getOutputStream());
        inStream = socket.getInputStream();
        pr = new PrintWriter(outStream);
        in = new InputStreamReader(socket.getInputStream());
        bf = new BufferedReader(in);
        receiver.start();

        LoginGUI.display(this);
    }

    /**
     * Reads all data from the socket if the message arrives in pieces.
     * Checks what data type the message is and preforms corresponding action.
     * @throws IOException
     */
    public void processInMessage() throws IOException {
        String string ="";
        while (inStream.available() != 0) {
            string += bf.readLine();
        }
        System.out.println(string);

        String[] chunks = string.split(" ");
        String command = chunks[0];
        try {
            String message = string.substring((chunks[0].length() + 1));
            switch (command) {
                case"msg":
                    clientGUI.displayNewMessage(message);
                    break;
                case"privmsg":
                    //TODO
                    break;
                case"loginok":
                    LoginGUI.close();
                    break;
                case"clients":
                    updateClientList(message);
                    break;
                case"oplevl":
                    //TODO
                    break;
                default:
                    AlertBox.display("ERROR",message);
                    break;
            }
        } catch (StringIndexOutOfBoundsException e) {}
    }

    public void tryLogin(String username, String password) throws IOException {
        if (username.isEmpty()) {
            AlertBox.display("ERROR", "Enter a username");
        } else {
            if (password.isEmpty()) {
                processOutMessage("login", username);
            } else {
                String hash = createHash(password);
                //TODO change password to hash
                processOutMessage("login", username + "," + password);
            }
        }
    }

    /**
     * Creates a SHA-256 hashed string from the input
     * @param password A string that u want turned into a SHA-256 HASH
     * @return HASH in SHA-256 Form
     */
    public String createHash(String password){

        try {
            // set encoding
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            //turn string to bytes
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));


            StringBuffer hexString = new StringBuffer();

            //Encodes
            for (int i = 0; i < encodedhash.length; i++){
                String hex = Integer.toHexString(0xff & encodedhash[i]);
                if (hex.length()==1){
                    hexString.append('0');
                }
                hexString.append(hex);
            }


            String hash = hexString.toString();

            return hash;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * @return returns socket (for thread methods).
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Listens for messages form the server.
     */
    public class ReceiveStringClient extends Thread {
        GUIClient client;

        ReceiveStringClient(GUIClient client) {
            this.client = client;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (client.getSocket().getInputStream().available() != 0) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    processInMessage();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Sends new message to the server.
     * @param msg string message.
     * @throws IOException
     */
    public void sendNewMessage(String msg) throws IOException {
        System.out.println("message length:" + msg.length());
        pr.println(msg);
        pr.flush();
    }

    public void processOutMessage(String commandPrefix, String string) throws IOException {
        String message;
        message = (commandPrefix + " " + string + "\n");
        sendNewMessage(message);
    }

    /**
     * Converts csv string of clients to array of clients and updates the GUI list of clients.
     * @param message the csv string of clients.
     */
    public void updateClientList(String message) {
        String[] clients = message.split(",");
        clientGUI.setClientList(clients);
    }
}

