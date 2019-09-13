import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.TimeUnit;


public class Server {
    ServerSocket serverSocket;
    int ClientNumber = 0;
    ArrayList<Client> Clients = new ArrayList<>();
    AcceptClient ClientListener = new AcceptClient(this);
    ReceiveString MessageReceiver = new ReceiveString(this);

    public static void main(String[] args) throws IOException, InterruptedException {
        Server a = new Server();
        a.start();
    }

    public void start() throws IOException, InterruptedException {
        serverSocket = new ServerSocket(42069);
        ClientListener.start();
        MessageReceiver.start();

        while (Clients.size() == 0) {
            System.out.println("no clients connected");
            TimeUnit.SECONDS.sleep(1);
        }
    }

    /**
     * Sends message with data type identifier at start of message to a client.
     * @param client client the data will be sent to.
     * @param msg the message
     * @param type what data type the message contains (1:string, 2:admin string, 3:client list).
     * @throws IOException
     */
    public void sendMessage(Client client, String msg, int type) throws IOException {
        PrintWriter printWriter = new PrintWriter(client.getSocket().getOutputStream());

        printWriter.println(type + "@" + msg);
        printWriter.flush();
    }

    /**
     * Reads message from a client socket and rebuilds the whole string if it arrived in pieces.
     * @param client the client the method will read from.
     * @return returns the string message.
     * @throws IOException
     */
    public String receiveString(Client client) throws IOException {
        InputStreamReader in = new InputStreamReader(client.getSocket().getInputStream());
        BufferedReader bf = new BufferedReader(in);
        String out = "";
        while (client.getSocket().getInputStream().available() != 0) {
            out += bf.readLine();
        }
        System.out.println("Message length" + out.length());
        System.out.println(out);
        sendStringToAllClients(Clients,out,client);
        return out;
    }

    /**
     *Adds a client to the client list and sends the updated client list to all clients connected to the server.
     * @throws IOException
     */
    public void addClient() throws IOException {
        ClientNumber++;
        Clients.add(new Client("Client" + ClientNumber,serverSocket.accept()));
        System.out.println("Client" + ClientNumber + " connected");
        sendUpdatedClientList();
        ClientListener.run();
    }

    /**
     * @return returns list of clients (for thread methods).
     */
    public ArrayList<Client> getClients() {
        return this.Clients;
    }

    /**
     * Sends a string message to all clients in a list.
     * @param Clients List of clients the string will be sent to.
     * @param msg The string the method will send.
     * @param sender Who the message is from.
     * @throws IOException
     */
    public void sendStringToAllClients(ArrayList<Client> Clients, String msg, Client sender) throws IOException {
        for (Client client : Clients) {
                sendMessage(client, sender.getName() + ": " + msg, 1);
        }
    }

    /**
     * Sends client list to all clients connected to the server.
     * @throws IOException
     */
    public void sendUpdatedClientList() throws IOException {
        System.out.println("yes");
        String message = "";
        for (Client client : Clients) {
            message += (client.getName() + ",");
        }

        for (Client client : Clients) {
            sendMessage(client,message,3);
        }
    }

    /**
     * Accepts clients trying to connect and adds them to the server.
     */
    public class AcceptClient extends Thread {
        private Server server;

        public AcceptClient(Server server) {
            this.server = server;
        }

        @Override
        public void run() {
            try {
                this.server.addClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks for any messages from all clients connected.
     */
    public class ReceiveString extends Thread {
        private Server server;
        private ArrayList<Client> Clients;

        public ReceiveString(Server server) {
            this.server = server;
        }

        @Override
        public void run() {
            this.Clients = server.getClients();
            while (true) {
                try {
                    for (Client client : this.Clients) {
                        try {
                            if (client.getSocket().getInputStream().available() != 0) {
                                try {
                                    System.out.println(this.server.receiveString(client));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (ConcurrentModificationException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}