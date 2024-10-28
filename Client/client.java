package client;

import java.io.*;
import java.net.*;
import tcp.tcp_transport;
import snw.snw_transport;

public class client {
    // initialize socket and input output streams
    private Socket ServerSocket = null;
    private Socket CacheSocket = null;
    private DataInputStream input = null;
    private DataOutputStream out = null;

    private String client_folder = "./client_fl/";

    // constructor to run client
    /*
     * params:-
     * ServerIp - IP address of server
     * ServerPort - Port number for server
     * CacheIp - IP address of cache
     * CachePort - Port number for cache
     * protocol - tcp or snw
     */
    public client(String ServerIp, int ServerPort, String CacheIp, int CachePort, String protocol) {
        // establish a connection
        try {
            if (ServerIp == "localhost") {
                ServerIp = "127.0.0.1";
            }
            if (CacheIp == "localhost") {
                CacheIp = "127.0.0.1";
            }
            ServerSocket = new Socket(ServerIp, ServerPort);
            System.out.println("Connected to Server");
            CacheSocket = new Socket(CacheIp, CachePort);
            System.out.println("Connected to Cache");

        } catch (UnknownHostException u) {
            System.out.println(u);
            return;
        } catch (IOException i) {
            System.out.println(i);
            return;
        }
        String input = "";
        // Input loop
        if (protocol.equals("tcp")) {
            try {

                while (true) {
                    System.out.print("Enter command: ");
                    input = System.console().readLine();

                    // parse user input
                    String[] commands = input.split(" ");

                    if (commands[0].equals("quit")) {
                        System.out.println("Exiting");
                        tcp_transport.send_message(CacheSocket, "quit ");
                        tcp_transport.send_message(ServerSocket, "quit ");
                        break;
                    } else if (commands[0].equals("put")) {
                        String file_dir = client_folder + commands[1];
                        System.out.println("Awaiting server response");
                        // send message to server via tcp
                        tcp_transport.send_message(ServerSocket, "put " + file_dir);

                        // send file via tcp
                        tcp_transport.send_file(ServerSocket, file_dir);

                        // Receive server response
                        DataInputStream in = new DataInputStream(ServerSocket.getInputStream());
                        String serverResp = in.readUTF();
                        System.out.println("Server response: " + serverResp);

                    } else if (commands[0].equals("get")) {
                        // send message via tcp
                        tcp_transport.send_message(CacheSocket, "get " + commands[1]);
                        String file_dir = client_folder + commands[1];

                        // receive file via tcp
                        tcp_transport.receiveFile(CacheSocket, file_dir);

                        // receive server response
                        DataInputStream in = new DataInputStream(CacheSocket.getInputStream());
                        String serverResp = in.readUTF();
                        System.out.println("Server response: " + serverResp);
                    }
                }
            } catch (Exception i) {
                System.out.println(i);
                System.exit(0);
            }
        } else if (protocol.equals("snw")) {
            try {
                while (true) {
                    System.out.print("Enter command: ");
                    input = System.console().readLine();
                    String[] commands = input.split(" "); // parse user input
                    if (commands[0].equals("quit")) {
                        System.out.println("Exiting");
                        tcp_transport.send_message(CacheSocket, "quit ");
                        tcp_transport.send_message(ServerSocket, "quit ");
                        break;
                    } else if (commands[0].equals("put")) {
                        String file_dir = client_folder + commands[1];
                        System.out.println("Awaiting server response");
                        // send put message to server via tcp
                        tcp_transport.send_message(ServerSocket, "put " + file_dir);

                        // send file to server via snw
                        snw_transport.send_file(ServerSocket, file_dir, false);

                        System.out.println("Server Response: File successfully uploaded");

                    } else if (commands[0].equals("get")) {
                        // send get message to cache via tcp
                        tcp_transport.send_message(CacheSocket, "get " + commands[1]);
                        String file_dir = client_folder + commands[1];

                        // receive file from cache via snw
                        snw_transport.receiveFile(CacheSocket, file_dir, false);

                        System.out.println("Server Response: File delivered");

                    }
                }
                ServerSocket.close();
                CacheSocket.close();
            } catch (Exception i) {
                System.out.println(i);
                System.exit(0);
            }
        }

        System.out.println("Goodbye");
        System.exit(0);
        return;
    }

    public static void main(String args[]) {
        if (args.length != 5) {
            System.out.println("Incorrect number of args");
        }
        if (Integer.valueOf(args[1]) < 20000 || Integer.valueOf(args[1]) > 24000 || Integer.valueOf(args[3]) < 20000
                || Integer.valueOf(args[3]) > 24000) {
            System.out.println("Please use ports within 20000 to 24000 for security reasons.");
            return;
        }
        client client = new client(args[0], Integer.valueOf(args[1]), args[2], Integer.valueOf(args[3]), args[4]);
    }
}
