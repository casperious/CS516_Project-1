
// A Java program for a Server
import java.net.*;
import java.io.*;

public class server {
    // initialize socket and input stream
    private Socket clientSocket = null;
    private Socket cacheSocket = null;
    private ServerSocket server = null;
    private DataInputStream in = null;

    // constructor with port
    public server(int port, String protocol) {
        // starts server and waits for a connection
        try {
            server = new ServerSocket(port);
            System.out.println("Server started");
            System.out.println("Waiting for cache");
            cacheSocket = server.accept();
            System.out.println("Cache accepted");
            System.out.println("Waiting for a client ...");

            clientSocket = server.accept();
            System.out.println("Client accepted");

            // takes input from the client socket
            // in = new DataInputStream(
            // new BufferedInputStream(socket.getInputStream()));

            String line = "";
            while (true) {

            }
            // reads message from client until "Over" is sent
            /*
             * while (!line.equals("Over")) {
             * try {
             * line = in.readUTF();
             * System.out.println(line);
             * 
             * } catch (IOException i) {
             * System.out.println(i);
             * }
             * }
             * System.out.println("Closing connection");
             */
            // close connection
            // socket.close();
            // in.close();
        } catch (IOException i) {
            System.out.println(i);
        }
    }

    public static void main(String args[]) {
        if (args.length != 2) {
            System.out.println("Incorrect number of args. Please enter Port number and protocol");
            return;
        }
        if (Integer.valueOf(args[0]) < 20000 || Integer.valueOf(args[0]) > 24000) {
            System.out.println("Please use ports within 20000 to 24000 for security reasons.");
            return;
        }
        server server = new server(Integer.valueOf(args[0]), args[1]);
    }
}
