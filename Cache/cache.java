package cache;

import java.net.ServerSocket;
import java.net.Socket;

import snw.snw_transport;
import tcp.tcp_transport;

import java.net.*;
import java.io.*;

public class cache {
    private ServerSocket cacheSocket = null;
    private Socket client = null;
    private Socket serverSocket = null;
    private DataInputStream in = null;
    private String cache_folder = "./cache_fl/";

    // constructor to run cache
    public cache(int port, String serverIp, int serverPort, String protocol) {
        // starts cache and waits for a connection
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
                    String message = in.readUTF();
                    // parse client message
                    String[] commands = message.split(" ");
                    if (commands[0].equals("get")) {
                        System.out.println("Looking for file in" + cache_folder + commands[1]);
                        String file_dir = cache_folder + commands[1];
                        File file = new File(file_dir);
                        // if file is in cache_fl send it to client
                        if (file.exists()) {
                            tcp_transport.send_file(client, file_dir);
                            tcp_transport.send_message(client, "File delivered from cache.");
                        } else {
                            // fetch file from server, store in cache_fl and then transmit to client.
                            System.out.println("File does not exist in cache, fetching from server");
                            tcp_transport.send_message(serverSocket, message);
                            tcp_transport.receiveFile(serverSocket, file_dir);
                            tcp_transport.send_file(client, file_dir);
                            tcp_transport.send_message(client, "File delivered from server.");
                        }
                    } else if (commands[0].equals("quit")) {
                        System.out.println("Goodbye.");
                        serverSocket.close();
                        client.close();
                        return;
                    }
                } catch (IOException ioe) {
                    System.out.println("IOE in cache");
                    return;
                } catch (Exception e) {
                    System.out.println("Error in file transfer");
                    System.exit(0);
                }
            } else {
                try {

                    // Setup input and output streams for communication with the client
                    DataInputStream in = new DataInputStream(client.getInputStream());
                    // get tcp message from client
                    String message = in.readUTF();
                    String[] commands = message.split(" ");
                    // No put functionality for cache
                    if (commands[0].equals("get")) {
                        String file_dir = cache_folder + commands[1];
                        File file = new File(file_dir);
                        // if file is in cache_fl send via snw to client
                        if (file.exists()) {
                            snw_transport.send_file(client, file_dir, false);
                            tcp_transport.send_message(client, "File delivered from cache.");
                        } else {
                            System.out.println("File does not exist in cache, fetching from server");
                            // send get message to server via tcp
                            tcp_transport.send_message(serverSocket, message);
                            // get file from server via snw
                            snw_transport.receiveFile(serverSocket, file_dir, true);
                            System.out.println("Received file in cache, sending to client now.");
                            // send file to client via snw
                            snw_transport.send_file(client, file_dir, false);
                            // send fin message
                            tcp_transport.send_message(client, "FIN completed transmission");
                            System.out.println("Complete");
                        }
                    } else if (commands[0].equals("quit")) {
                        System.out.println("Goodbye.");
                        serverSocket.close();
                        client.close();
                        return;
                    }
                } catch (IOException ioe) {
                    System.out.println("IOE in cache " + ioe);
                    return;
                } catch (Exception e) {
                    System.out.println("Error in file transfer " + e);
                    System.exit(0);

                }
            }
        }
    }

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
