package snw;

import java.net.*;
import java.io.*;
import java.lang.Exception;

public class snw_transport {
    public static void send_file(Socket dest, String fp, boolean sendingToCache) throws Exception {
        int dest_port_num = 23998;
        int src_port_num = 23999;
        if (sendingToCache) {
            dest_port_num = 23997;
        }
        File file = new File(fp);
        int bytes = 0;
        FileInputStream fileInputStream = new FileInputStream(file);
        // DataOutputStream out = new DataOutputStream(dest.getOutputStream());
        // DataInputStream in = new DataInputStream(dest.getInputStream());
        // send file size
        String dest_ip = dest.getRemoteSocketAddress().toString();
        System.out.println("Dest ip before processing is " + dest_ip);
        if (dest_ip.charAt(0) == 'l') {
            String[] seperate = dest_ip.split("/");
            dest_ip = seperate[1];
        } else if (dest_ip.charAt(0) == '/') {
            dest_ip = dest_ip.substring(1);
        }
        // dest_ip = dest_ip.substring(1);
        String[] breakdown = dest_ip.split(":");
        dest_ip = breakdown[0];
        System.out.println("Dest ip is " + dest_ip + " and port is " + dest.getPort());
        InetAddress ipAddress = InetAddress.getByName(dest_ip);
        System.out.println("Got ip address variable " + ipAddress);
        String msg = "LEN: " + file.length();
        DatagramSocket ds = new DatagramSocket(src_port_num);
        System.out.println("Created default datagram socket at port " + ds.getLocalPort());
        byte[] len_msg = msg.getBytes();

        System.out.println(
                "Sending  " + msg + " of size " + len_msg.length + " to " + ipAddress + " " + dest_port_num);
        DatagramPacket DpSend = new DatagramPacket(len_msg, len_msg.length, ipAddress, dest_port_num);
        ds.send(DpSend);
        System.out.println("Sent len message");
        // out.writeUTF("LEN:" + file.length());
        // break file into chunks
        byte[] buffer = new byte[1000];
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            System.out.println("Writing: " + (char) (buffer[0]) + " " + (char) (buffer[1]) + " to " + ipAddress
                    + " at port " + dest_port_num);
            // out.write(buffer, 0, bytes);
            DatagramPacket chunks = new DatagramPacket(buffer, bytes, ipAddress, dest_port_num);
            ds.send(chunks);
            // out.flush();
            // buffer = new byte[1000];
            long startTime = System.nanoTime();
            System.out.println("Waiting for ACK");
            while (true) {
                long curr = System.nanoTime();
                if (curr - startTime >= 1000000000) {
                    System.out.println("Did not receive ACK. Terminating");
                    fileInputStream.close();
                    ds.close();
                    // out.close();
                    // in.close();
                    throw new Exception("Did not receive ACK");
                }
                byte[] ack_buf = new byte[100];
                DatagramPacket ack = new DatagramPacket(ack_buf, 100);
                // while (ds.available() != 0) {
                // wait = in.readUTF();
                System.out.println("Waiting to receive");
                ds.receive(ack);
                // }
                String ack_msg = new String(ack_buf);
                System.out.println("Recieved " + ack_msg);
                String[] commands = ack_msg.split(" ");
                // System.out.println(commands[0]);
                if (commands[0].equals("ACK")) {
                    System.out.println("ACK received. continue transport");
                    break;
                } else if (commands[0].equals("FIN")) {
                    System.out.println("FIN successfully transmitted");
                    // in.close();
                    // out.close();
                    ds.close();
                    fileInputStream.close();
                    return;
                } else {
                    System.out.println("Received " + ack_msg + "after checking for ACK and FIN");
                    continue;
                }
            }
        }
        System.out.println("Exited loop without FIN");
        byte[] ack_buf = new byte[100];
        DatagramPacket ack = new DatagramPacket(ack_buf, 100);
        // while (ds.available() != 0) {
        // wait = in.readUTF();
        System.out.println("Waiting to receive");
        ds.receive(ack);
        // }
        String ack_msg = new String(ack_buf);
        System.out.println("Recieved " + ack_msg + " Successfully");
        ds.close();
        fileInputStream.close();
    }

    public static void receiveFile(Socket src, String fileName, boolean isCache) throws Exception {
        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        // DataInputStream in = new DataInputStream(src.getInputStream());
        // DataOutputStream out = new DataOutputStream(src.getOutputStream());
        System.out.println("SRC socket at " + src.getRemoteSocketAddress() + " " + src.getPort());
        DatagramSocket ds;
        if (!isCache) {
            ds = new DatagramSocket(23998);
        } else {
            ds = new DatagramSocket(23997);
        }
        byte[] size_arr = new byte[100];
        DatagramPacket size_dp = new DatagramPacket(size_arr, 100);
        System.out.println("Waiting for len message at " + ds.getLocalPort());
        ds.receive(size_dp);
        // String lenMessage = in.readUTF(); // read file size
        long lenStartTime = System.nanoTime();
        String lenMessage = new String(size_arr);
        System.out.println("Messaged received in receiveFile is " + lenMessage);
        String[] comps = lenMessage.split(":");
        long size = Long.parseLong(comps[1].trim());
        System.out.println("Size to read is " + size);
        String src_ip = src.getRemoteSocketAddress().toString();
        System.out.println("Src ip from socket is " + src_ip);
        String port = "";
        if (src_ip.charAt(0) == 'l') {
            String[] seperate = src_ip.split("/");
            src_ip = seperate[1];
        } else if (src_ip.charAt(0) == '/') {
            src_ip = src_ip.substring(1);
        }
        System.out.println("extracting ip from " + src_ip);
        // src_ip = src_ip.substring(1);
        String[] breakdown = src_ip.split(":");
        src_ip = breakdown[0];
        port = breakdown[1];
        System.out.println("getting inetaddress by name of " + src_ip);
        InetAddress ipAddress = InetAddress.getByName(src_ip);
        byte[] buffer = new byte[1000];
        while (size > 0) {
            int chunkSize = 0;
            if (size > 1000) {
                chunkSize = 1000;
            } else {
                chunkSize = (int) size;
            }
            DatagramPacket chunk = new DatagramPacket(buffer, chunkSize);
            // while (true) {
            // bytes = in.read(buffer, 0, 1000); // (int) Math.min(buffer.length, size)
            ds.receive(chunk);
            System.out.println(
                    "Read length: " + chunk.getLength() + " " + (char) buffer[0] + " " + (char) buffer[1]
                            + " sending ACK");
            // out.writeUTF("ACK ");
            String msg = "ACK ";
            byte[] len_msg = msg.getBytes();
            System.out.println("Sending ack to " + ipAddress + " " + src.getPort());
            DatagramPacket AckSend = new DatagramPacket(len_msg, msg.length(), ipAddress, 23999);
            ds.send(AckSend);
            lenStartTime = System.nanoTime();
            long currTime = System.nanoTime();
            if (currTime - lenStartTime >= 1000000000) {
                System.out.println("Did not receive Data. Terminating");
                // in.close();
                // out.close();
                ds.close();
                fileOutputStream.close();
                throw new Exception("Did not receive data.");
            }
            fileOutputStream.write(buffer, 0, chunkSize);
            size -= chunkSize; // read upto file size
            System.out.println("Remaining Size is now " + size);
            // }

        }

        fileOutputStream.close();
        String f_msg = "FIN close connection ";
        byte[] fin_msg = f_msg.getBytes();
        DatagramPacket FinSend = new DatagramPacket(fin_msg, f_msg.length(), ipAddress, 23999);
        ds.send(FinSend);
        // out.writeUTF("FIN close connections");
        System.out.println("FIN close connections");
        ds.close();
    }
}
