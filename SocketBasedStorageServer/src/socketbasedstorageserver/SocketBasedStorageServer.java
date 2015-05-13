/*
 * Operating Systems : Assignment 4
   Ashwin Bahulkar, Siddharth Shenolikar , Mithun Nallu.
 */
package socketbasedstorageserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author Ashwin Bahulkar, Siddharth Shenolikar, Mithun Nallu
 */

public class SocketBasedStorageServer {
    
    public HashMap<String,FileTableEntry> fileMap=new HashMap<String,FileTableEntry>();
    PageManager pageManager = null;
    
    public static void main(String[] args) {
        SocketBasedStorageServer server = new SocketBasedStorageServer();
        server.startServer();
    }

    public void startServer() {
        pageManager = new PageManager(this);
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
                ConnectionHandler thread = new ConnectionHandler(socket, this);

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
    
    public FileTableEntry getFileTableEntry(String fileName) {
        
        if(!fileMap.containsKey(fileName)) 
        {
            //You may be required to replace a corresponding file entry
            fileMap.put(fileName, new FileTableEntry(fileName, pageManager));
        }
        
        return fileMap.get(fileName);
    }
    
    public void deleteFileTableEntry(String fileName) {
        this.fileMap.remove(fileName);
        //To-Do: Call PageManager's freePages function
        fileMap.entrySet().iterator().next().getValue();
    }
}
