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

    /**
     * Opens server socket.
     * Starts to listen for incoming connections and handles them.
     * Listens for messages from connected clients.
     * @throws IOException
     * @throws InterruptedException
     */
    public void start() throws IOException, InterruptedException {
        serverSocket = new ServerSocket(42069);
        ClientListener.start();
        MessageReceiver.start();
        Clients.add(new Client("ADMIN","5d"));
        Clients.add(new Client("TEST1",""));
    }

    /**
     * Processes message from a given client.
     * Reads the whole message if it arrives in several "packets" and pieces it together.
     * Then regex the message to separate the command in the message.
     * Then checks if the command is valid (sends error otherwise).
     * Then executes the command.
     * @param client client the command and message is from.
     * @throws IOException
     */
    public void processInMessage(Client client) throws IOException {
        InputStreamReader in = new InputStreamReader(client.getSocket().getInputStream());
        BufferedReader bf = new BufferedReader(in);
        String string = "";
        while (client.getSocket().getInputStream().available() != 0) {
            string += bf.readLine();
        }
        String[] chunks = string.split(" ");
        String command = chunks[0];
        String message;
        try {
            message = string.substring((chunks[0].length() + 1));
        } catch (StringIndexOutOfBoundsException e) {
            message = "";
        }
            switch (command) {
                case "msg":
                    sendMessageToAllClients(Clients,command, client.getName() + " " + message);
                    break;
                case "privmsg":
                    if(message.isEmpty()) {
                        processOutMessage(client.getSocket(),"cmderr","Missing data");
                    } else {
                        privateMessage(client, chunks, string);
                    }
                    break;
                case "logout":
                    logOut(client);
                    break;
                case "ping":
                    //TODO
                    break;
                case "help":
                    processOutMessage(client.getSocket(),"supported","msg privmsg logout ping help");
                    break;
                default:
                    processOutMessage(client.getSocket(),"cmderr", "command not supported");
                    break;
            }
    }

    /**
     * Decodes a private message command.
     * Checks if target client exists and responds with a msgerr if it does not.
     * If the target client exists the message is relayed to target client and conditioned with command prefix.
     * @param client client the message is from.
     * @param chunks regex'ed chunks of message from "processInMessage" method
     * @param string Whole message from message client.
     * @throws IOException
     */
    private void privateMessage(Client client, String[] chunks, String string) throws IOException {
        if(getByName(chunks[1])==null) {
            processOutMessage(client.getSocket(),"msgerr","User does not exist");
        } else {
            String message = string.substring((chunks[0].length()+chunks[1].length())+1);
            processOutMessage(getByName(chunks[1]).getSocket(),"privmsg",client.getName() + message);
        }
    }

    /**
     * Logs user out.
     * Sets that user to be available again and sends the updated client list to all clients connected.
     * @param client the client that is logging out.
     * @throws IOException
     */
    public void logOut(Client client) throws IOException {
        client.getSocket().close();
        client.setAvailable(true);
        sendUpdatedClientList();
    }

    /**
     * Send message to a socket.
     * @param socket socket the message will be sent to.
     * @param msg the message
     * @throws IOException
     */
    public void sendMessage(Socket socket, String msg) throws IOException {
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
        printWriter.println(msg);
        printWriter.flush();
    }

    /**
     * Conditions message to be protocol compatible and sends it to the specified socket.
     * @param socket socket the message will be sent to.
     * @param commandPrefix what command is being sent.
     * @param string the message.
     * @throws IOException
     */
    public void processOutMessage(Socket socket, String commandPrefix, String string) throws IOException {
        String message;
        message = (commandPrefix + " " + string + "\n");
        sendMessage(socket,message);
    }

    /**
     * Reads the whole message if it arrives in several "packets" and pieces it together.
     * Then regex the message to separate the command in the message.
     * Checks if the command is a login related command.
     * @param socket socket the message is from.
     * @throws IOException
     */
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
                    processOutMessage(socket,"supported","login signup help");
                    break;
                default:
                    processOutMessage(socket,"cmderr", "command not supported");
                    break;
            }
        } catch (StringIndexOutOfBoundsException e) {
            if(command=="help") {
                processOutMessage(socket,"supported","login signup help");
            } else {
                processOutMessage(socket, "cmderr", "missing data");
            }
        }
    }

    /**
     * Checks if login info given by a socket is correct and replies with loginerr or loginok and then adds the socket to the client list.
     * @param socket socket the login info is from
     * @param message login info from socket
     * @throws IOException
     */
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

    /**
     * Accepts a socket connection and adds socket to login queue to listen for login commands from socket.
     * @throws IOException
     */
    public void addQueueClient() throws IOException {
        LoginQueue.add(serverSocket.accept());
        System.out.println("Client queued");
        ClientListener.run();
    }

    /**
     * Searches array of clients by name and returns that client or null if it did not find that client.
     * @param username the username used to search the array.
     * @return returns the found client or null.
     */
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
     * Moves client from login queue to client list and sends a updated client list to all users connected.
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

    /**
     * @return returns list of queued sockets to listen for login commands (for thread methods).
     */
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
     * And login queued clients.
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