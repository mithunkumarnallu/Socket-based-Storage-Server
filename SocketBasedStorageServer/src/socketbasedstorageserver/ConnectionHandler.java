/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketbasedstorageserver;

import java.net.*;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 *
 * @author nmk
 */
public class ConnectionHandler extends Thread {

    private Socket socket; // A connected socket
    private DataInputStream inputFromClient = null;
    private DataOutputStream outputToClient = null;
    private SocketBasedStorageServer server = null;
    byte[] commandRaw;
    String command;
    
    public ConnectionHandler(Socket socket, SocketBasedStorageServer server) {
        this.socket = socket;
        this.server = server;
        commandRaw = new byte[1000];
    }
    
    private void sendMessageToClient(String message) throws IOException {
        try {
            outputToClient.writeUTF(message);
        } catch (IOException ex) {
            System.err.println(ex);
            throw ex;
        }
    }

    private void printOutputToConsole(String response, long threadId) {
        System.out.println("[thread " + threadId + "] " + response);
    }
    
    private void listFiles(String command) throws IOException {
        File file = new File(".store//");
        String response = "";
        try {

            DirectoryStream<Path> dir = Files.newDirectoryStream(file.toPath());
            response = response + file.toPath().getNameCount() + "\n";
            for (Path filePath : dir) {
                response = response + filePath.getFileName() + "\n";
            }
        } catch (Exception ex) {
            response = "ERROR: " + ex.getMessage();
        }

        sendMessageToClient(response);
    }
    
    public void run() {
        try {
            // Create data input and output streams
            inputFromClient
                    = new DataInputStream(socket.getInputStream());
            outputToClient
                    = new DataOutputStream(socket.getOutputStream());

            // Continuously serve the client
            while (true) {
                command = Read();
                
                String fileName = "";
                
                if(command!=null)
                {
                    command = command.trim();
                    if(command.equals(""))
                    {
                        continue;
                    }
                    
                    
                    System.out.println("Rcvd: " + command);
                    if(command.toUpperCase().startsWith("DIR"))
                        listFiles(command);
                    else 
                    {
                        if(command.toUpperCase().startsWith("DELETE"))
                            fileName = command.substring(command.indexOf(' ') + 1);
                        else if(command.toUpperCase().startsWith("STORE")) {
                            //command = command + "\n" + inputFromClient.readLine();
                            //Check if the file contents are there as well.
                            if(!(command.contains("\\n") || command.contains("\n")))
                            {
                                command+="\n" + Read();
                            }
                            
                            fileName = command.substring(command.indexOf(' ') + 1, command.indexOf(' ', command.indexOf(' ') + 1));
                        }
                        else
                            fileName = command.substring(command.indexOf(' ') + 1, command.indexOf(' ', command.indexOf(' ') + 1));
                        
                       if(!server.getFileTableEntry(fileName).runCommand(command,outputToClient, this.getId()))
                           return;
                    }
                }
            }
        } catch (IOException ex) {
            //System.err.println( ex );
            printOutputToConsole("Client closed its socket... terminating",this.getId());
        }
    }
    
    private String Read() throws IOException
    {
        String command="";
        int bytesAvailableToRead=-1;
        
        bytesAvailableToRead = inputFromClient.available();
        byte[] inputStreamBytes;
        
        while(bytesAvailableToRead==0)
        {
            bytesAvailableToRead = inputFromClient.available();
        }
        
        inputStreamBytes = new byte[bytesAvailableToRead+1];
            inputFromClient.read(inputStreamBytes);
            command = new String(inputStreamBytes);
        
        return command;
    }
}
