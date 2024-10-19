
// A Java program for a Server
import java.net.*;
import java.io.*;
import tcp.tcp_transport;
import snw.snw_transport;

class ThreadSocket implements Runnable {
    private Socket socketRef;
    private String protocol;
    private String server_folder = "./server_fl/";

    public ThreadSocket(Socket sc, String protocolArg) {
        socketRef = sc;
        protocol = protocolArg;
    }

    public void run() {
        try {
            System.out.println("Running thread " + Thread.currentThread().getId());
            String line = "";
            while (true) {
                if (protocol.equals("tcp")) {
                    try {

                        // Setup input and output streams for communication with the client
                        DataInputStream in = new DataInputStream(socketRef.getInputStream());
                        // DataInputStream(clientSocket.getInputStream());
                        DataOutputStream out = new DataOutputStream(socketRef.getOutputStream());
                        // DataOutputStream(clientSocket.getOutputStream());

                        String message = in.readUTF();
                        System.out.println("Messaged received is: " + message);
                        String[] commands = message.split(" ");
                        if (commands[0].equals("get")) {
                            System.out.println("Looking for file in" + server_folder + commands[1]);
                            String file_dir = server_folder + commands[1];
                            File file = new File(file_dir);
                            if (file.exists()) {
                                tcp_transport.send_file(socketRef, file_dir);
                                System.out.println("Sending file to cache");
                            } else {
                                System.out.println("File does not exist in server");
                            }
                        } else if (commands[0].equals("put")) {

                            String[] file_loc = commands[1].split("/");
                            int len = file_loc.length;
                            String filename = file_loc[len - 1];
                            System.out.println("Putting file in " + server_folder + filename);
                            String file_dir = server_folder + filename;
                            File file = new File(file_dir);
                            tcp_transport.receiveFile(socketRef, file_dir);
                        }
                    } catch (IOException ioe) {
                        System.out.println("IOE in cache");
                    } catch (Exception e) {
                        System.out.println("Error in file transfer");
                    }
                } else {

                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

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
            Thread clientThread = new Thread(new ThreadSocket(clientSocket, protocol));
            Thread cacheThread = new Thread(new ThreadSocket(cacheSocket, protocol));
            clientThread.start();
            cacheThread.start();
            // takes input from the client socket
            // in = new DataInputStream(
            // new BufferedInputStream(socket.getInputStream()));
            /*
             * /
             * String line = "";
             * while (true) {
             * if (protocol.equals("tcp")) {
             * try {
             * 
             * // Setup input and output streams for communication with the client
             * DataInputStream inClient = new
             * DataInputStream(clientSocket.getInputStream());
             * // DataInputStream(clientSocket.getInputStream());
             * DataOutputStream outClient = new
             * DataOutputStream(clientSocket.getOutputStream());
             * // DataOutputStream(clientSocket.getOutputStream());
             * 
             * DataInputStream inCache = new DataInputStream(cacheSocket.getInputStream());
             * DataOutputStream outCache = new
             * DataOutputStream(cacheSocket.getOutputStream());
             * // String messageClient = inClient.readUTF();
             * String messageCache = inCache.readUTF();
             * /*
             * String clientMessage = inClient.readUTF();
             * System.out.println("Client says: " + clientMessage);
             * System.out.println("Cache says: " + messageCache);
             * String message = "";
             * if (clientMessage == null) {
             * message = messageCache;
             * } else {
             * message = clientMessage;
             * }
             */
            /*
             * / String message = messageCache;
             * String[] commands = message.split(" ");
             * if (commands[0].equals("get")) {
             * System.out.println("Looking for file in" + server_folder + commands[1]);
             * String file_dir = server_folder + commands[1];
             * File file = new File(file_dir);
             * 
             * if (file.exists()) {
             * tcp_transport.send_file(cacheSocket, file_dir);
             * System.out.println("Sending file to cache");
             * } else {
             * System.out.println("File does not exist in server");
             * }
             * } else if (commands[0].equals("put")) {
             * System.out.println("Putting file in " + server_folder + commands[1]);
             * String file_dir = server_folder + commands[1];
             * File file = new File(file_dir);
             * tcp_transport.receiveFile(clientSocket, file_dir);
             * }
             * } catch (IOException ioe) {
             * System.out.println("IOE in cache");
             * } catch (Exception e) {
             * System.out.println("Error in file transfer");
             * }
             * } else {
             * 
             * }
             * }
             */
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
