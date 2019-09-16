import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;


public class Server {
    ServerSocket serverSocket;
    ArrayList<Socket> LoginQueue = new ArrayList<>();
    ArrayList<Client> Clients = new ArrayList<>();
    addClientToQueue ClientListener = new addClientToQueue(this);
    ReceiveString MessageReceiver = new ReceiveString(this);

    public static void main(String[] args) throws IOException, InterruptedException {
        Server a = new Server();
        a.start();
    }

    public void start() throws IOException, InterruptedException {
        serverSocket = new ServerSocket(42069);
        ClientListener.start();
        MessageReceiver.start();
        Clients.add(new Client("ADMIN","5d"));
        Clients.add(new Client("TEST1",""));
    }

    public void processInMessage(Client client) throws IOException {
        InputStreamReader in = new InputStreamReader(client.getSocket().getInputStream());
        BufferedReader bf = new BufferedReader(in);
        String string = "";
        while (client.getSocket().getInputStream().available() != 0) {
            string += bf.readLine();
        }
        String[] chunks = string.split(" ");
        String command = chunks[0];
        try {
            String message = string.substring((chunks[0].length() + 1));
            switch (command) {
                case "msg":
                    sendMessageToAllClients(Clients,command, client.getName() + " " + message);
                    break;
                case "privmsg":
                    //TODO
                    break;
                case "logout":
                    logOut(client);
                    break;
                case "ping":
                    //TODO
                    break;
                case "help":
                    //TODO
                    break;
                default:
                    processOutMessage(client.getSocket(),"cmderr", "command not supported");
                    break;
            }
        } catch (StringIndexOutOfBoundsException e) {
            processOutMessage(client.getSocket(),"cmderr", "command not supported");
        }
    }

    public void logOut(Client client) throws IOException {
        client.getSocket().close();
        client.setAvailable(true);
        sendUpdatedClientList();
    }

    /**
     * Send message to a client.
     * @param socket client the message will be sent to.
     * @param msg the message
     * @throws IOException
     */
    public void sendMessage(Socket socket, String msg) throws IOException {
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
        printWriter.println(msg);
        printWriter.flush();
    }

    public void processOutMessage(Socket socket, String commandPrefix, String string) throws IOException {
        String message;
        message = (commandPrefix + " " + string + "\n");
        sendMessage(socket,message);
    }

    public void processLoginMessage(Socket socket) throws IOException {
        InputStreamReader in = new InputStreamReader(socket.getInputStream());
        BufferedReader bf = new BufferedReader(in);
        String string = "";
        while (socket.getInputStream().available() != 0) {
            string += bf.readLine();
        }

        String[] chunks = string.split(" ");
        String command = chunks[0];
        try {
            String message = string.substring((chunks[0].length() + 1));
            switch (command) {
                case "login":
                    checkLogin(socket, message);
                    break;
                case "signup":
                    //TODO
                    break;
                case "help":
                    //TODO
                    break;
                default:
                    processOutMessage(socket,"cmderr", "command not supported");
                    break;
            }
        } catch (StringIndexOutOfBoundsException e) {
            processOutMessage(socket,"cmderr", "command not supported");
        }
    }

    public void checkLogin(Socket socket, String message) throws IOException {
        String[] chunks = message.split(",");
        String username = chunks[0];
        String password;
        try {
            password = chunks[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            password = "";
        }

        if(getByName(username) == null) {
            processOutMessage(socket,"loginerr","User does not exist");
        } else if (!getByName(username).getAvailable()) {
            processOutMessage(socket,"loginerr","Username already in use");
        } else {
            if(getByName(username).getHasPassword()) {
                System.out.println("has password");
                if (password.equals(getByName(username).getPassword())) {
                    Client client = getByName(username);
                    addClient(client, socket);
                } else {
                    processOutMessage(socket,"loginerr","Wrong password");
                }
            } else {
                Client client = getByName(username);
                addClient(client, socket);
            }
        }
    }

    public void addQueueClient() throws IOException {
        LoginQueue.add(serverSocket.accept());
        System.out.println("Client queued");
        ClientListener.run();
    }

    public Client getByName(String username) {
        Client out = null;
        for (Client client: Clients) {
            if(client.getName().equals(username)) {
                out = client;
            }
        }
        return out;
    }

    /**
     *Adds a client to the client list and sends the updated client list to all clients connected to the server.
     * @throws IOException
     */
    public void addClient(Client client, Socket socket) throws IOException {
        client.setSocket(socket);
        client.setAvailable(false);
        System.out.println("Client " + client.getName() + " connected");
        processOutMessage(client.getSocket(), "loginok", "");
        LoginQueue.remove(socket);
        sendUpdatedClientList();
    }

    /**
     * @return returns list of clients (for thread methods).
     */
    public ArrayList<Client> getClients() {
        return this.Clients;
    }

    public ArrayList<Socket> getLoginQueue() {
        return this.LoginQueue;
    }

    /**
     * Sends a string message to all clients in a list.
     * @param Clients List of clients the string will be sent to.
     * @param msg The string the method will send.
     * @throws IOException
     */
    public void sendMessageToAllClients(ArrayList<Client> Clients, String commandPrefix, String msg) throws IOException {
        for (Client client : Clients) {
            if(!client.getAvailable()) {
                processOutMessage(client.getSocket(), commandPrefix, msg);
            }
        }
    }

    /**
     * Sends client list to all clients connected to the server.
     * @throws IOException
     */
    public void sendUpdatedClientList() throws IOException {
        String message = "";
        for (Client client : Clients) {
            if(!client.getAvailable()) {
                message += (client.getName() + ",");
            }
        }

        for (Client client : Clients) {
            if(!client.getAvailable()) {
                processOutMessage(client.getSocket(),"clients",message);
            }
        }
    }

    /**
     * Accepts clients to login queue and listens for login commands.
     */
    public class addClientToQueue extends Thread {
        private Server server;

        public addClientToQueue(Server server) {
            this.server = server;
        }

        @Override
        public void run() {
            try {
                this.server.addQueueClient();
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
        private ArrayList<Socket> LoginQueue;

        public ReceiveString(Server server) {
            this.server = server;
        }

        @Override
        public void run() {
            this.Clients = server.getClients();
            this.LoginQueue = server.getLoginQueue();
            while (true) {
                try {
                    for (Client client : this.Clients) {
                        if (!client.getAvailable()) {
                            try {
                                if (client.getSocket().getInputStream().available() != 0) {
                                    try {
                                        processInMessage(client);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    } catch(ConcurrentModificationException ignored){}
                try {
                    for (Socket socket : this.LoginQueue) {
                        try {
                            if (socket.getInputStream().available() != 0) {
                                processLoginMessage(socket);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (ConcurrentModificationException ignored) {}
            }
        }
    }
}