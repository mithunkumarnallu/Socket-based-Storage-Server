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

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    private void sendMessageToClient(String message) throws IOException {
        try {
            outputToClient.writeUTF(message);
        } catch (IOException ex) {
            System.err.println(ex);
            throw ex;
        }
    }

    private void storeFile(String command) throws IOException {
        String commandInfo = command.substring(0, command.indexOf('\n'));
        String fileContents = command.substring(command.indexOf('\n') + 1);
        String fileName = commandInfo.substring(commandInfo.indexOf(' ') + 1);
        fileName = fileName.substring(0, fileName.indexOf(' '));
        File file = new File(".store//" + fileName);
        String response = "";

        try {
            if (file.exists()) {
                response = "ERROR: File exists!";
            } else {
                file.createNewFile();
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                fw.write(fileContents);
                fw.close();
                response = "ACK\n";
            }
        } catch (Exception ex) {
            response = "ERROR: " + ex.getMessage();
        }

        sendMessageToClient(response);
    }

    private void deleteFile(String command) throws IOException {
        String fileName = command.substring(command.indexOf(' ') + 1);
        File file = new File(".store//" + fileName);
        String response = "";

        try {
            if (file.exists()) {
                file.delete();
                response = "ACK\n";
            } else {
                response = "ERROR: No such File\n";
            }
        } catch (Exception ex) {
            response = "ERROR: " + ex.getMessage();
        }

        sendMessageToClient(response);
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
                String command = inputFromClient.readUTF();
                System.out.println("Rcvd: " + command);
                int spaceIndex = command.indexOf(' ');

                String actualCommand = spaceIndex == -1 ? command : command.substring(0, command.indexOf(' '));

                switch (actualCommand) {
                    case "STORE":
                        storeFile(command);
                        break;

                    case "DELETE":
                        deleteFile(command);
                        break;

                    case "DIR":
                        listFiles(command);
                        break;
                }
            }
        } catch (IOException ex) {
            //System.err.println( ex );
            System.out.println("Client closed its socket... terminating");
        }
    }
}
