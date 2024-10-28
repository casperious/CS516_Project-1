
package server;

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
        socketRef = sc; // cache or client socket loaded for thread
        protocol = protocolArg; // protocol for thread
    }

    /*
     * Multithreaded implementation to listen for put from client, and get
     * from cache at the same time.
     */
    public void run() {
        try {
            while (true) {
                if (protocol.equals("tcp")) {
                    try {

                        // Setup input and output streams for communication with the client
                        DataInputStream in = new DataInputStream(socketRef.getInputStream());
                        DataOutputStream out = new DataOutputStream(socketRef.getOutputStream());
                        // parse input message
                        String message = in.readUTF();
                        String[] commands = message.split(" ");
                        if (commands[0].equals("get")) {
                            String file_dir = server_folder + commands[1];
                            File file = new File(file_dir);
                            if (file.exists()) {
                                // send file via tcp to requesting socket
                                tcp_transport.send_file(socketRef, file_dir);
                                // send confirmation message
                                tcp_transport.send_message(socketRef, "File delivered from server.");
                            } else {
                                System.out.println("File does not exist in server");
                            }
                        } else if (commands[0].equals("put")) {
                            // initialize file for incoming data
                            String[] file_loc = commands[1].split("/");
                            int len = file_loc.length;
                            String filename = file_loc[len - 1];
                            String file_dir = server_folder + filename;
                            File file = new File(file_dir);
                            // receive file once file location is created
                            tcp_transport.receiveFile(socketRef, file_dir);
                            // send confirmation message
                            tcp_transport.send_message(socketRef, "File successfully uploaded.");
                        } else if (commands[0].equals("quit")) {
                            System.out.println("Goodbye.");
                            return;
                        }
                    } catch (IOException ioe) {
                        System.out.println("IOE in cache");
                        System.out.println("Goodbye");
                        return;
                    } catch (Exception e) {
                        System.out.println("Error in file transfer");
                        System.exit(0);

                    }
                } else { // snw
                    try {

                        // Setup input and output streams for communication with the client
                        DataInputStream in = new DataInputStream(socketRef.getInputStream());
                        DataOutputStream out = new DataOutputStream(socketRef.getOutputStream());
                        // parse input message
                        String message = in.readUTF();

                        String[] commands = message.split(" ");
                        if (commands[0].equals("get")) {

                            String file_dir = server_folder + commands[1];
                            File file = new File(file_dir);
                            if (file.exists()) {
                                // send file to cache via snw
                                snw_transport.send_file(socketRef, file_dir, true);
                            } else {
                                System.out.println("File does not exist in server");
                            }
                        } else if (commands[0].equals("put")) {
                            // initialize file to store incoming data
                            String[] file_loc = commands[1].split("/");
                            int len = file_loc.length;
                            String filename = file_loc[len - 1];
                            String file_dir = server_folder + filename;
                            File file = new File(file_dir);
                            // receive file via snw
                            snw_transport.receiveFile(socketRef, file_dir, false);

                        } else if (commands[0].equals("quit")) {
                            System.out.println("Goodbye.");
                            socketRef.close();
                            return;
                        }
                    } catch (IOException ioe) {
                        System.out.println("IOE in cache " + ioe);
                        return;
                    } catch (Exception e) {
                        System.out.println("Error in file transfer " + e);
                        System.exit(0);
                        ;
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    public static void main(String[] args) {

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

            // First accept cache connection
            cacheSocket = server.accept();
            System.out.println("Cache accepted");
            System.out.println("Waiting for a client ...");

            // then accept any clients
            clientSocket = server.accept();
            System.out.println("Client accepted");
            Thread clientThread = new Thread(new ThreadSocket(clientSocket, protocol));
            Thread cacheThread = new Thread(new ThreadSocket(cacheSocket, protocol));
            clientThread.start();
            cacheThread.start();

        } catch (Exception i) {
            System.out.println(i);
            System.exit(0);
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
