/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketbasedstorageserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 *
 * @author ashwinbahulkar
 */
public class SocketBasedStorageServer {

    public static void main(String[] args) {
        new SocketBasedStorageServer();
    }

    public SocketBasedStorageServer() {
        System.out.println("MultiThreadServer started at " + new Date());

        try {
            // Create a server socket
            ServerSocket serverSocket = new ServerSocket(8765);

            // Number each client connection
            int clientNumber = 1;

            while (true) {
                // Listen for a new connection request
                Socket socket = serverSocket.accept();   // BLOCK

                // Display the client number
                System.out.println("Starting thread for client "
                        + clientNumber + " at " + new Date());

                // Find the client's host name, and IP address
                InetAddress inetAddress = socket.getInetAddress();
                System.out.println("Client " + clientNumber
                        + "'s host name is " + inetAddress.getHostName());
                System.out.println("Client " + clientNumber
                        + "'s IP Address is " + inetAddress.getHostAddress());

                // Create a new thread for the connection
                ConnectionHandler thread = new ConnectionHandler(socket);

                // Start the new thread
                thread.start();

                // Increment clientNo
                clientNumber++;
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }

        System.out.println("MultiThreadServer ended at " + new Date());

    }
}
