import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
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
                        System.out.println("yes");
                        processMessage();
                        //InputStreamReader in = new InputStreamReader(client.getSocket().getInputStream());
                        //BufferedReader bf = new BufferedReader(in);
                        //String out = bf.readLine();
                        //clientGUI.displayNewMessage(out);
                        //System.out.println(out);
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

    /**
     * Reads all data from the socket if the message arrives in pieces.
     * Checks what data type the message is and preforms corresponding action.
     * @throws IOException
     */
    public void processMessage() throws IOException {
        String message ="";
        while (inStream.available() != 0) {
            message += bf.readLine();
        }

        String[] dataType = message.split("@");
        //Remove dataType info on the first 2 characters in the message
        message = message.substring(2);

        switch (dataType[0]) {
            //Message is a string
            case "1":
                clientGUI.displayNewMessage(message);
                break;
                //Message is a string from client with permission level 4
            case "2":
                System.out.println("TODO, code not added yet");
                break;
                //Message is clients connected
            case "3":
                updateClientList(message);
                break;
        }
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

