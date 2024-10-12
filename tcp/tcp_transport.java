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

    public static void send_file(Socket dest, String fp) throws Exception {
        int bytes = 0;
        File file = new File(fp);
        FileInputStream fileInputStream = new FileInputStream(file);
        DataOutputStream out = new DataOutputStream(dest.getOutputStream());
        // break file into chunks
        out.writeLong(file.length());
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            out.write(buffer, 0, bytes);
            out.flush();
        }
        fileInputStream.close();

    }

    public static void receiveFile(Socket src, String fileName) throws Exception {
        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        DataInputStream in = new DataInputStream(src.getInputStream());
        long size = in.readLong(); // read file size
        byte[] buffer = new byte[4 * 1024];
        while (size > 0 && (bytes = in.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes; // read upto file size
        }
        fileOutputStream.close();
    }
}
