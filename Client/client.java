package Client;

import java.io.*;
import java.net.*;

public class client {
    // initialize socket and input output streams
    private Socket ServerSocket = null;
    private Socket CacheSocket = null;
    private DataInputStream input = null;
    private DataOutputStream out = null;

    // constructor to put ip address and port
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
            // takes input from terminal
            // input = new DataInputStream(System.in);

            // sends output to the socket
            // out = new DataOutputStream(
            // ServerSocket.getOutputStream());
        } catch (UnknownHostException u) {
            System.out.println(u);
            return;
        } catch (IOException i) {
            System.out.println(i);
            return;
        }
        String input = "";
        // Input loop
        try {

            while (true) {
                input = System.console().readLine();
                String[] commands = input.split(" ");
                System.out.println(commands[0]);
                if (commands[0].equals("quit")) {
                    System.out.println("Exiting");
                    break;
                } else if (commands[0].equals("put")) {
                    System.out.println("Uploading file at " + commands[1]);
                    System.out.println("Awaiting server response");
                } else if (commands[0].equals("get")) {
                    System.out.println("Fetching file");
                }
            }
        } catch (Exception i) {
            System.out.println(i);
        }

        // close the connection
        /*
         * try {
         * // input.close();
         * // out.close();
         * // ServerSocket.close();
         * // CacheSocket.close();
         * } catch (IOException i) {
         * System.out.println(i);
         * }
         */
        System.out.println("Goodbye");
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
