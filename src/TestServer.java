import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TestServer {
    ServerSocket serverSocket;
    Socket clientSocket;
    Client client;
    InputStream inStream;
    InputStreamReader inStreamReader;
    OutputStream outStream;
    Boolean readingMessage = false;
    ArrayList<String> messageArray = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        TestServer a = new TestServer();
        a.start();
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(8888);
        clientSocket = serverSocket.accept();
        client = new Client("TestClient",clientSocket);
        System.out.println("Client connected");

        inStream = client.getSocket().getInputStream();
        inStreamReader = new InputStreamReader(inStream);
        outStream = client.getSocket().getOutputStream();

        while (true) {
            if (inStream.available() != 0 && readingMessage==false) {
                readingMessage = true;
                readHeader(inStream);
            }
        }
    }

    public void readHeader(InputStream inStream) throws IOException {
        BufferedReader bf = new BufferedReader(inStreamReader);

        int ooga = 0;
        while (ooga==0) {
            if(inStream.available() != 0) {
                readMessage(inStream);
            }
        }


        boolean headerReceived = false;
        while (headerReceived==false) {
            System.out.println("try");
            if (inStream.available() == 1071) {
                System.out.println("hread");
                String str = bf.readLine();
                str = str.trim();

                String[] header = str.split("@");
                String dataType = header[0];
                int packages = Integer.parseInt(header[1]);
                headerReceived = true;
                System.out.println(dataType);
                System.out.println(packages);

                switch (header[0]) {
                    case "string message":
                        readInStrings(inStream, packages);
                        break;
                }
            } else {
                String str = bf.readLine();
                messageArray.add(str);
            }
        }
    }

    public void readInStrings(InputStream inStream, int inPackets) throws IOException {
        BufferedReader bf = new BufferedReader(inStreamReader);
        for (int i=0; i<inPackets; i++) {
            String str = bf.readLine();
            messageArray.add(str);
            System.out.println(str);
        }
        ArrayList<String> temp = sortMessages(messageArray);
        System.out.println(convertToOneString(temp));
        messageArray.clear();
        readingMessage = false;
    }

    public String convertToOneString(ArrayList<String> array) {
        String out = "";
        for (String string : array) {
            out += string;
        }
        return out;
    }

    public ArrayList<String> sortMessages(ArrayList<String> array) {
        String[] temp = new String[array.size()];
        ArrayList<String> out = new ArrayList<>();
        for (String string : array) {
            String[] splitTemp =  string.split("@");
            int msgNumber = Integer.parseInt(splitTemp[0]);
            temp[msgNumber] = string;                                    // change to splitTemp[1];
        }
        System.out.println("sorted:");
         for(int i=0; i<temp.length; i++) {
             out.add(temp[i]);
             System.out.println(temp[i]);
         }
         return out;
    }

    public void readMessage (InputStream inStream) throws IOException {
        BufferedReader bf = new BufferedReader(inStreamReader);
        String message = "";
        while (inStream.available() != 0) {
            message += bf.readLine();
        }

        System.out.println("Message length" + message.length());
        System.out.println(message);
    }
}
