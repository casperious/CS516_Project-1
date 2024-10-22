package snw;

import java.net.*;
import java.io.*;
import java.lang.Exception;

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
            System.out.println("Writing: " + (char) (buffer[0]) + " " + (char) (buffer[1]));
            out.write(buffer, 0, bytes);
            out.flush();
            // buffer = new byte[1000];
            long startTime = System.nanoTime();
            System.out.println("Waiting for ACK");
            while (true) {
                long curr = System.nanoTime();
                if (curr - startTime >= 1000000000) {
                    System.out.println("Did not receive ACK. Terminating");
                    fileInputStream.close();
                    out.close();
                    in.close();
                    throw new Exception("Did not receive ACK");
                }
                String wait = "";
                while (in.available() != 0) {
                    wait = in.readUTF();
                }
                String[] commands = wait.split(" ");
                // System.out.println(commands[0]);
                if (commands[0].equals("ACK")) {
                    System.out.println("ACK received. continue transport");
                    break;
                } else if (commands[0].equals("FIN")) {
                    System.out.println("FIN successfully transmitted");
                    // in.close();
                    // out.close();
                    fileInputStream.close();
                    return;
                } else {
                    continue;
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
        System.out.println("Size to read is " + size);
        byte[] buffer = new byte[1000];
        while (size > 0) {

            while (in.available() != 0) {
                bytes = in.read(buffer, 0, 1000); // (int) Math.min(buffer.length, size)
                System.out.println(
                        "Read length: " + bytes + " " + (char) buffer[0] + " " + (char) buffer[1] + "sending ACK");
                out.writeUTF("ACK ");
                lenStartTime = System.nanoTime();
                long currTime = System.nanoTime();
                if (currTime - lenStartTime >= 1000000000) {
                    System.out.println("Did not receive Data. Terminating");
                    in.close();
                    out.close();
                    fileOutputStream.close();
                    throw new Exception("Did not receive data.");
                }
                fileOutputStream.write(buffer, 0, bytes);
                size -= bytes; // read upto file size
                System.out.println("Remaining Size is now " + size);
            }

        }

        fileOutputStream.close();
        out.writeUTF("FIN close connections");
        System.out.println("FIN close connections");
    }
}
