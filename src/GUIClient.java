import javafx.application.Platform;
import jdk.management.resource.internal.inst.AbstractPlainDatagramSocketImplRMHooks;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

public class GUIClient {
    Socket socket;
    ReceiveStringClient receiver = new ReceiveStringClient(this);
    InputStream inStream;
    InputStreamReader in;
    BufferedReader bf;
    DataOutputStream outStream;
    PrintWriter pr;
    ClientGUI clientGUI;
    String usernameCache;
    String passwordCache;

    /**
     * Starts the client program.
     * Connects to server socket.
     * Opens login GUI.
     * Listens for messages from server.
     * @param clientGUI reference to GUI
     * @throws IOException
     * @throws InterruptedException
     */
    public void start(ClientGUI clientGUI) {
        this.clientGUI = clientGUI;
        LoginGUI.display(this);
    }

    /**
     * Processes message from server.
     * Reads the whole message if it arrives in several "packets" and pieces it together.
     * Then regex the message to separate the command in the message.
     * Then checks if the command is valid.
     * Then executes the command.
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
                    clientGUI.displayNewMessage(message);
                    break;
                case"loginok":
                    LoginGUI.close();
                    break;
                case"signupok":
                    tryLogin(usernameCache,passwordCache);
                    break;
                case"clients":
                    updateClientList(message);
                    break;
                case"ping":
                    processOutMessage("ping","");
                    break;
                case"loginerr":
                    AlertBox.display("Login error",message);
                    break;
                case"signuperr":
                    AlertBox.display("Sign up error",message);
                    break;
                case"supported":
                    clientGUI.displayNewMessage("[SERVER] Supported commands: " + message);
                    break;
                default:
                    AlertBox.display("ERROR","Unknown error:\n" + string);
                    break;
            }
        } catch (StringIndexOutOfBoundsException e) {}
    }

    /**
     * Method attempts to connect client to the server using the ip and socket given by the method call.
     * @param ip Ip that will be used to connect to the server and socket if the string is separated using ":"
     *           if not the default socket 42069 will be used.
     * @return Returns if the method connected to the server successfully.
     * @throws IOException
     */
    public boolean connect(String ip) throws IOException {
        if(ip.isEmpty()) {
            return false;
        }
        Boolean connected = false;
        String IP;
        String PORT;
        if (ip.contains(":")) {
            String[] chunks = ip.split(":");
            IP = chunks[0];
            PORT = chunks[1];
        } else {
            IP = ip;
            PORT = "42069";
        }
        try {
            socket = new Socket(IP, Integer.parseInt(PORT));
            connected=true;

            outStream = new DataOutputStream(socket.getOutputStream());
            inStream = socket.getInputStream();
            pr = new PrintWriter(outStream);
            in = new InputStreamReader(socket.getInputStream());
            bf = new BufferedReader(in);
            receiver.start();
        } catch (ConnectException e) {
            System.out.println("Failed to connect");
        } catch (UnknownHostException ignore) {}
        return connected;
    }

    /**
     * Sends login info to server.
     * @param username login username
     * @param password login password (if left blank password will not be hashed and login without password will be attempted).
     * @throws IOException
     */
    public void tryLogin(String username, String password) throws IOException {
        usernameCache="";
        passwordCache="";
        if (password.isEmpty()) {
            processOutMessage("login", username);
        } else {
            String hash = createHash(password);
            processOutMessage("login", username + "," + hash);
        }
    }

    public void trySignup(String username, String password) throws IOException {
        usernameCache = username;
        passwordCache = password;
        if (password.isEmpty()) {
            processOutMessage("signup", username);
        } else {
            String hash = createHash(password);
            processOutMessage("signup", username + "," + hash);
        }
    }

    /**
     * Creates a SHA-256 hashed string from the input
     * @param password A string that you want turned into a SHA-256 HASH
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
     * Sends new message to the server.
     * @param msg string message.
     * @throws IOException
     */
    public void sendNewMessage(String msg) throws IOException {
        System.out.println("message length:" + msg.length());
        pr.println(msg);
        pr.flush();
    }

    /**
     * Conditions message to be compatible with protocol.
     * @param commandPrefix what command is being sent.
     * @param string the message.
     * @throws IOException
     */
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
}