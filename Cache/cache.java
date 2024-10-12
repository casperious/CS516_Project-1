import java.net.ServerSocket;
import java.net.Socket;

import tcp.tcp_transport;

import java.net.*;
import java.io.*;

public class cache {
    private ServerSocket cacheSocket = null;
    private Socket client = null;
    private Socket serverSocket = null;
    private DataInputStream in = null;
    private String cache_folder = "./cache_fl/";

    // constructor with port
    public cache(int port, String serverIp, int serverPort, String protocol) {
        // starts server and waits for a connection
        try {
            if (serverIp == "localhost") {
                serverIp = "127.0.0.1";
            }
            cacheSocket = new ServerSocket(port);
            System.out.println("Cache started");
            serverSocket = new Socket(serverIp, serverPort);
            System.out.println("Connected to server");

            System.out.println("Waiting for a client ...");
            client = cacheSocket.accept();
            System.out.println("Client accepted");

        } catch (IOException e) {
            System.out.println("Error in setting up cache");
        }
        while (true) {
            if (protocol.equals("tcp")) {
                try {

                    // Setup input and output streams for communication with the client
                    DataInputStream in = new DataInputStream(client.getInputStream());
                    // DataOutputStream out = new DataOutputStream(client.getOutputStream());
                    String message = in.readUTF();
                    System.out.println("Client says: " + message);
                    String[] commands = message.split(" ");
                    if (commands[0].equals("get")) {
                        System.out.println("Looking for file in" + cache_folder + commands[1]);
                        String file_dir = cache_folder + commands[1];
                        File file = new File(file_dir);

                        if (file.exists()) {
                            tcp_transport.send_file(client, file_dir);
                        } else {
                            System.out.println("File does not exist in cache, fetching from server");
                            tcp_transport.send_message(serverSocket, message);
                        }
                    }
                } catch (IOException ioe) {
                    System.out.println("IOE in cache");
                }
            } else {

            }
        }
    }

    // takes input from the client socket
    /*
     * in = new DataInputStream(
     * new BufferedInputStream(socket.getInputStream()));
     * 
     * String line = "";
     * 
     * // reads message from client until "Over" is sent
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
     * 
     * // close connection
     * socket.close();
     * in.close();
     * } catch (IOException i) {
     * System.out.println(i);
     * }
     */

    public static void main(String args[]) {
        if (args.length != 4) {
            System.out
                    .println("Incorrect number of args. Please enter Port number, server ip, server port and protocol");
            return;
        }
        if (Integer.valueOf(args[0]) < 20000 || Integer.valueOf(args[0]) > 24000 || Integer.valueOf(args[2]) < 20000
                || Integer.valueOf(args[2]) > 24000) {
            System.out.println("Please use ports within 20000 to 24000 for security reasons.");
            return;
        }
        cache cache = new cache(Integer.valueOf(args[0]), args[1], Integer.valueOf(args[2]), args[3]);
    }
}
