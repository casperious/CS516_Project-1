package snw;

import java.net.*;
import java.io.*;

public class snw_transport {
    public static void send_file(Socket dest, String fp) throws Exception {
        int bytes = 0;
        File file = new File(fp);
        FileInputStream fileInputStream = new FileInputStream(file);
        DataOutputStream out = new DataOutputStream(dest.getOutputStream());
        DataInputStream in = new DataInputStream(dest.getInputStream());
        // send file size
        out.writeUTF("LEN:" + file.length());
        // break file into chunks
        byte[] buffer = new byte[1000];
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            out.write(buffer, 0, bytes);
            out.flush();
            long startTime = System.nanoTime();
            System.out.println("Waiting for ACK");
            while (true) {
                long curr = System.nanoTime();
                if (curr - startTime >= 1000000000) {
                    System.out.println("Did not receive ACK. Terminating");
                    return;
                }
                String wait = in.readUTF();
                String[] commands = wait.split(" ");
                System.out.println(commands[0]);
                if (commands[0].equals("ACK")) {
                    System.out.println("ACK received. continue transport");
                    break;
                }
            }
        }

        fileInputStream.close();
    }

    public static void receiveFile(Socket src, String fileName) throws Exception {
        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        DataInputStream in = new DataInputStream(src.getInputStream());
        DataOutputStream out = new DataOutputStream(src.getOutputStream());
        String lenMessage = in.readUTF(); // read file size
        long lenStartTime = System.nanoTime();

        String[] comps = lenMessage.split(":");
        long size = Long.parseLong(comps[1]);
        byte[] buffer = new byte[1000];
        while (true) {
            long currTime = System.nanoTime();
            if (currTime - lenStartTime >= 1000000000) {
                System.out.println("Did not receive Data. Terminating");
                return;
            }
            bytes = in.read(buffer, 0, (int) Math.min(buffer.length, size));
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes; // read upto file size
            if (size <= 0) {
                break;
            }
            out.writeUTF("ACK ");
            lenStartTime = System.nanoTime();

        }

        fileOutputStream.close();
        out.writeUTF("FIN close connections");
    }
}
