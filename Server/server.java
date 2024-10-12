
// A Java program for a Server
import java.net.*;
import java.io.*;
import tcp.tcp_transport;
import snw.snw_transport;

public class server {
    // initialize socket and input stream
    private Socket clientSocket = null;
    private Socket cacheSocket = null;
    private ServerSocket server = null;
    private DataInputStream in = null;
    private String server_folder = "./server_fl/";

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
                if (protocol.equals("tcp")) {
                    try {

                        // Setup input and output streams for communication with the client
                        // DataInputStream inClient = new
                        // DataInputStream(clientSocket.getInputStream());
                        // DataOutputStream outClient = new
                        // DataOutputStream(clientSocket.getOutputStream());

                        DataInputStream inCache = new DataInputStream(cacheSocket.getInputStream());
                        DataOutputStream outCache = new DataOutputStream(cacheSocket.getOutputStream());
                        // String messageClient = inClient.readUTF();
                        String messageCache = inCache.readUTF();
                        // System.out.println("Client says: " + messageClient);
                        System.out.println("Cache says: " + messageCache);
                        String message = "";
                        // if (messageClient.length() == 0) {
                        message = messageCache;
                        // } else {
                        // message = messageClient;
                        // }
                        String[] commands = message.split(" ");
                        if (commands[0].equals("get")) {
                            System.out.println("Looking for file in" + server_folder + commands[1]);
                            String file_dir = server_folder + commands[1];
                            File file = new File(file_dir);

                            if (file.exists()) {
                                tcp_transport.send_file(cacheSocket, file_dir);
                            } else {
                                System.out.println("File does not exist in server");
                            }
                        }
                    } catch (IOException ioe) {
                        System.out.println("IOE in cache");
                    }
                } else {

                }
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
