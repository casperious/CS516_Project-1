package tcp;

import java.net.*;
import java.io.*;

public class tcp_transport {

    public static void send_message(Socket dest, String message) {
        try {
            DataOutputStream out = new DataOutputStream(dest.getOutputStream());
            // Setup input stream to receive data from the server
            DataInputStream in = new DataInputStream(dest.getInputStream());
            out.writeUTF(message);

        } catch (IOException ioe) {
            System.out.println("io error in tcp transport");
        }
    }

    public static void send_file(Socket dest, String fp) {

    }

}
