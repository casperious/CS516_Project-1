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
    }

    public static void main(String args[]) {
        if (args.length != 5) {
            System.out.println("Incorrect number of args");
        }
        client client = new client(args[0], Integer.valueOf(args[1]), args[2], Integer.valueOf(args[3]), args[4]);
    }
}
