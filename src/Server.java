import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.TimeUnit;


public class Server {
    private ServerSocket serverSocket;
    private ArrayList<Socket> LoginQueue = new ArrayList<>();
    private ArrayList<Client> Clients = new ArrayList<>();
    private addClientToQueue ClientListener = new addClientToQueue(this);
    private ReceiveString MessageReceiver = new ReceiveString(this);
    private timeOutClients timeOutClients = new timeOutClients(this);
    private int timeOutResetValue = 5;

    public static void main(String[] args) throws IOException {
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
    public void start() throws IOException {
        serverSocket = new ServerSocket(42069);
        ClientListener.start();
        MessageReceiver.start();
        timeOutClients.start();
        Clients.add(new Client("TEST1",""));
        Clients.add(new Client("TEST2",""));
        Clients.add(new Client("TEST3",""));
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
    private void processInMessage(Client client) throws IOException {
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
                    client.setTimeOut(timeOutResetValue);
                    break;
                case "loadchat":
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
    private void processOutMessage(Socket socket, String commandPrefix, String string) throws IOException {
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
    private void processLoginMessage(Socket socket) throws IOException {
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
                    signUp(socket,message);
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

    private void signUp(Socket socket, String message) throws IOException {
        String[] chunks = message.split(",");
        System.out.println(chunks.length);
        if(chunks.length>0 && chunks.length<3) {
            String username = chunks[0];
            String password;
            try {
                password = chunks[1];
            } catch (ArrayIndexOutOfBoundsException e) {
                password = "";
            }
            if (!username.isEmpty()) {
                if (!(username == null)) {
                    if (!(username.contains(" "))) {
                        if(getByName(username)==null) {
                            Client client = new Client(username, password);
                            client.setSocket(socket);
                            Clients.add(client);
                            processOutMessage(socket, "signupok", "");
                        } else {
                            processOutMessage(socket, "signuperr", "Username taken");
                        }
                    } else {
                        processOutMessage(socket, "signuperr", "Username can not contain \" \" or ,");
                    }
                } else {
                    processOutMessage(socket, "signuperr", "Looking for exploits huh?");
                }
            } else {
                processOutMessage(socket, "signuperr", "Enter a username");
            }
        } else {
            processOutMessage(socket,"signuperr","Wrong format");
        }
    }

    /**
     * Checks if login info given by a socket is correct and replies with loginerr or loginok and then adds the socket to the client list.
     * @param socket socket the login info is from
     * @param message login info from socket
     * @throws IOException
     */
    private void checkLogin(Socket socket, String message) throws IOException {
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
    private void addQueueClient() throws IOException {
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
    private void addClient(Client client, Socket socket) throws IOException {
        client.setSocket(socket);
        client.setAvailable(false);
        System.out.println("Client " + client.getName() + " connected");
        processOutMessage(client.getSocket(), "loginok", "");
        LoginQueue.remove(socket);
        sendUpdatedClientList();
        client.setTimeOut(timeOutResetValue);
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
    private ArrayList<Socket> getLoginQueue() {
        return this.LoginQueue;
    }

    /**
     * Sends a string message to all clients in a list.
     * @param Clients List of clients the string will be sent to.
     * @param msg The string the method will send.
     * @throws IOException
     */
    private void sendMessageToAllClients(ArrayList<Client> Clients, String commandPrefix, String msg) throws IOException {
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
    private void sendUpdatedClientList() throws IOException {
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

        addClientToQueue(Server server) {
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

        ReceiveString(Server server) {
            this.server = server;
            this.Clients = server.getClients();
            this.LoginQueue = server.getLoginQueue();
        }

        @Override
        public void run() {
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
                            try {
                                if (socket.getInputStream().available() != 0) {
                                    processLoginMessage(socket);
                                }
                            } catch (NullPointerException ignored) {}
                        } catch (IOException e)   {
                            e.printStackTrace();
                        }
                    }
                } catch (ConcurrentModificationException ignored) {}
            }
        }
    }

    public class timeOutClients extends Thread {
        private ArrayList<Client> Clients;

        timeOutClients(Server server) {
            this.Clients = server.getClients();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    for (Client client : this.Clients) {
                        if (!client.getAvailable())
                            try {
                                if (client.getTimeOut() < 5) {
                                    processOutMessage(client.getSocket(), "ping", "your session will time out soon, return ping to continue connection");
                                }
                                if (client.getTimeOut() < 1) {
                                    System.out.println(client.getName() + " timed out");
                                    logOut(client);
                                }
                                client.decrementTimeOut();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }
                } catch (ConcurrentModificationException ignored) {}
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}