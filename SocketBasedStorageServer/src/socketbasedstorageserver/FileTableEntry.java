/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketbasedstorageserver;

import Interfaces.PageMethods;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ashwinbahulkar
 */
public class FileTableEntry {
    public String filename;
    public List<Page> pageList;
    public long timestamp;
    private PageManager pageManager = null;
    
    public FileTableEntry(String filename, PageManager pageManager) {
        this.filename = filename;
        this.pageList = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
        this.pageManager = pageManager;
    }
    
    private void sendMessageToClient(String message, DataOutputStream output) throws IOException {
        try {
            output.writeUTF(message);
        } catch (IOException ex) {
            System.err.println(ex);
            throw ex;
        }
    }
    
    private void printOutputToConsole(String response, long threadId) {
        String[] messages = response.split("\n");
        for(String msg: messages) {
            if(!msg.equals(""))
                System.out.println("[thread " + threadId + "] " + msg);
        }
    }
    
    private void storeFile(String command, DataOutputStream output, long threadId) throws IOException {
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
        
        sendMessageToClient(response, output);
        printOutputToConsole(response, threadId);
    }

    private void deleteFile(String command, DataOutputStream output, long threadId) throws IOException {
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
        
        response = pageManager.freePages(filename) + "\n" + response;
        sendMessageToClient(response, output);
        printOutputToConsole(response, threadId);
    }
    
    private void readFile(String command, DataOutputStream output, long threadId) throws IOException {
        String[] commandInfo = command.split(" ");
        
        String fileName = commandInfo[1];
        long byteOffset = Long.parseLong(commandInfo[2]);
        long length = Long.parseLong(commandInfo[3]);
        
        File file = new File(".store//" + fileName);
        String response = "";

        try {
            if (!file.exists()) {
                response = "ERROR: File does not exist!";
                sendMessageToClient(response, output);
                printOutputToConsole(response, threadId);
            } else {
                int pageNo = (int)(byteOffset / 1024) + 1;
                int bytesSent = 0;
                boolean isFirstPageSent = false;
                PageMessage pageMessage = null;
                
                while(length > 0) {

                    pageMessage = pageManager.getNewPage(fileName, pageNo);
                    if(isFirstPageSent == false && 1024 * pageNo - byteOffset <= length) {
                        bytesSent = (int)(1024 * pageNo - byteOffset);
                        isFirstPageSent = true;
                    } else if(length > 1024) {
                        bytesSent = 1024;
                    } else {
                        bytesSent = (int)length;
                    }
                    byteOffset += bytesSent;
                    pageNo++;
                    length -= bytesSent;
                    
                    //To - Do: Get required page contents from page
                    sendMessageToClient("ACK " + bytesSent + "\n", output);
                    printOutputToConsole(pageMessage.message + "\nTransferred " + bytesSent + " bytes from offset " + (byteOffset - bytesSent), threadId);
                }
            }
        } catch (Exception ex) {
            System.err.println(ex);
            throw ex;
        }
    }
    
    public synchronized boolean runCommand(String command, DataOutputStream output, long threadId)
    {
        int spaceIndex = command.indexOf(' ');
                    
        String actualCommand = spaceIndex == -1 ? command : command.substring(0, command.indexOf(' '));
        String response = "";
        
        try {
            switch (actualCommand) {
                case "STORE":
                    storeFile(command, output, threadId);
                    break;

                case "DELETE":
                    deleteFile(command, output, threadId);
                    break;

                case "READ":
                    readFile(command, output, threadId);
                    break;
            }
            return true;
        }
        catch(IOException ex) {
            printOutputToConsole("Client closed its socket... terminating", threadId);
            return false;
        }
    }
}
