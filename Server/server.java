
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
        socketRef = sc;
        protocol = protocolArg;
    }

    public void run() {
        try {
            System.out.println("Running thread " + Thread.currentThread().getId());
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
                                tcp_transport.send_message(socketRef, "File delivered from server.");
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
                } else {
                    try {

                        // Setup input and output streams for communication with the client
                        DataInputStream in = new DataInputStream(socketRef.getInputStream());
                        // DataInputStream(clientSocket.getInputStream());
                        DataOutputStream out = new DataOutputStream(socketRef.getOutputStream());
                        // DataOutputStream(clientSocket.getOutputStream());

                        String message = in.readUTF();

                        String[] commands = message.split(" ");
                        if (commands[0].equals("get")) {
                            System.out.println("Looking for file in" + server_folder + commands[1]);
                            String file_dir = server_folder + commands[1];
                            File file = new File(file_dir);
                            if (file.exists()) {
                                System.out.println("Sending file to cache");
                                snw_transport.send_file(socketRef, file_dir, true);
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
            cacheSocket = server.accept();
            System.out.println("Cache accepted");
            System.out.println("Waiting for a client ...");

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
