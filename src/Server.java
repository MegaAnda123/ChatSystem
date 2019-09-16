import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args){

        Server s = new Server();

        s.run();

    }

    private void run() {

        try{
            // Creates welcoming server socket
            ServerSocket mainSocket = new ServerSocket(6969);
            // Get Accepted connection socket
            System.out.println("Waiting for connection");
            Socket clientSocket = mainSocket.accept();
            System.out.println("Connection successful!");
            // Create buffered input reader
            InputStreamReader inReader = new InputStreamReader(clientSocket.getInputStream());
            BufferedReader bufReader = new BufferedReader(inReader);

            String response = bufReader.readLine();
            System.out.println("Message recived from client: "+ response);

            // Creates writer
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            // send response
            writer.println(response);
            System.out.println("Sent to client: " + response);

            //close individual and server sockets
            clientSocket.close();
            mainSocket.close();

        }catch (IOException e){
            e.getStackTrace();

        }

    }
}
