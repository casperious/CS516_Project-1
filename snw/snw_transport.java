package snw;

import java.net.*;
import java.io.*;
import java.lang.Exception;

public class snw_transport {
    /*
     * Package function to send a file from local socket to destination socket IP
     * address and Port
     * params:-
     * dest - Socket at destination. Used by parent client/cache/socket for TCP
     * messages, passed as param to extract IP and port
     * fp - filepath of file to send
     * sendingToCache - bool flag to alter port number depending on if cache is
     * being sent to
     */
    public static void send_file(Socket dest, String fp, boolean sendingToCache) throws Exception {
        int dest_port_num = 23998;
        int src_port_num = 23999;
        if (sendingToCache) {
            dest_port_num = 23997; // cache port is hardwired to 23997. Pray it doesn't get used
        }
        File file = new File(fp);
        int bytes = 0;
        FileInputStream fileInputStream = new FileInputStream(file);

        // Process IP and port to send to
        String dest_ip = dest.getRemoteSocketAddress().toString();
        System.out.println("Dest ip before processing is " + dest_ip);
        if (dest_ip.charAt(0) == 'l') {
            String[] seperate = dest_ip.split("/");
            dest_ip = seperate[1];
        } else if (dest_ip.charAt(0) == '/') {
            dest_ip = dest_ip.substring(1);
        }
        String[] breakdown = dest_ip.split(":");
        dest_ip = breakdown[0];
        InetAddress ipAddress = InetAddress.getByName(dest_ip);

        // Send length message
        String msg = "LEN: " + file.length();
        Thread.sleep(200);
        DatagramSocket ds = new DatagramSocket(src_port_num);
        byte[] len_msg = msg.getBytes();

        DatagramPacket DpSend = new DatagramPacket(len_msg, len_msg.length, ipAddress, dest_port_num);

        // Sleep to avoid race conditions and ensure smooth data transfer
        Thread.sleep(200);
        ds.send(DpSend);

        // break file into chunks
        byte[] buffer = new byte[1000];
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            DatagramPacket chunks = new DatagramPacket(buffer, bytes, ipAddress, dest_port_num);
            ds.send(chunks);
            // set timeout to 1s
            ds.setSoTimeout(1000);
            long startTime = System.nanoTime();
            // old implementation before I found setSoTimeout. implementation works so have
            // left it as is
            while (true) {
                long curr = System.nanoTime();
                if (curr - startTime >= 1000000000) {
                    System.out.println("Did not receive ACK. Terminating");
                    fileInputStream.close();
                    ds.close();
                    throw new Exception("Did not receive ACK");
                }
                byte[] ack_buf = new byte[100];
                DatagramPacket ack = new DatagramPacket(ack_buf, 100);
                // Receive ACK/FIN message
                ds.receive(ack);

                String ack_msg = new String(ack_buf);
                String[] commands = ack_msg.split(" ");
                if (commands[0].equals("ACK")) {
                    // send next packet
                    break;
                } else if (commands[0].equals("FIN")) {
                    // terminate connection
                    ds.close();
                    fileInputStream.close();
                    return;
                } else {
                    System.out.println("Received " + ack_msg + "after checking for ACK and FIN");
                    continue;
                }
            }
        }
        byte[] ack_buf = new byte[100];
        DatagramPacket ack = new DatagramPacket(ack_buf, 100);
        // receive final message
        ds.receive(ack);

        String ack_msg = new String(ack_buf);
        System.out.println("Recieved " + ack_msg + " Successfully");
        ds.close();
        fileInputStream.close();
    }

    /*
     * Receives file from source Ip, and stores it to fileName
     * params:-
     * src - Source socket. Used by parent client/cache/socket for TCP
     * messages, passed as param to extract IP and port
     * fileName - name of file extracted from put/get message to store incoming file
     * isCache - bool to bind port number to 23997 if is cache
     */
    public static void receiveFile(Socket src, String fileName, boolean isCache) throws Exception {
        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        DatagramSocket ds; // initialize socket to 23998 if client, 23997 if cache
        if (!isCache) {
            ds = new DatagramSocket(23998);
        } else {
            ds = new DatagramSocket(23997);
        }
        ds.setReuseAddress(true);
        byte[] size_arr = new byte[100];
        DatagramPacket size_dp = new DatagramPacket(size_arr, 100);
        // receive length message
        ds.receive(size_dp);

        long lenStartTime = System.nanoTime();
        String lenMessage = new String(size_arr);
        String[] comps = lenMessage.split(":");

        // parse length
        long size = Long.parseLong(comps[1].trim());
        String src_ip = src.getRemoteSocketAddress().toString();
        String port = "";
        if (src_ip.charAt(0) == 'l') {
            String[] seperate = src_ip.split("/");
            src_ip = seperate[1];
        } else if (src_ip.charAt(0) == '/') {
            src_ip = src_ip.substring(1);
        }
        String[] breakdown = src_ip.split(":");
        src_ip = breakdown[0];
        port = breakdown[1];
        InetAddress ipAddress = InetAddress.getByName(src_ip);

        // initialize buffer
        byte[] buffer = new byte[1000];
        while (size > 0) {
            int chunkSize = 0;
            if (size > 1000) {
                chunkSize = 1000;
            } else {
                chunkSize = (int) size;
            }
            DatagramPacket chunk = new DatagramPacket(buffer, chunkSize);
            ds.receive(chunk);

            // send ACK
            String msg = "ACK ";
            byte[] len_msg = msg.getBytes();
            DatagramPacket AckSend = new DatagramPacket(len_msg, msg.length(), ipAddress, 23999);
            ds.send(AckSend);
            ds.setSoTimeout(1000);
            // start tracking time to receive next packet
            // Old implementation of manually tracking time.
            lenStartTime = System.nanoTime();
            long currTime = System.nanoTime();
            if (currTime - lenStartTime >= 1000000000) {
                System.out.println("Did not receive Data. Terminating");
                ds.close();
                fileOutputStream.close();
                throw new Exception("Did not receive data.");
            }
            fileOutputStream.write(buffer, 0, chunkSize);
            size -= chunkSize; // read upto file size

        }

        fileOutputStream.close();
        // send FIN
        String f_msg = "FIN close connection ";
        byte[] fin_msg = f_msg.getBytes();
        DatagramPacket FinSend = new DatagramPacket(fin_msg, f_msg.length(), ipAddress, 23999);
        ds.send(FinSend);
        ds.close();
    }
}