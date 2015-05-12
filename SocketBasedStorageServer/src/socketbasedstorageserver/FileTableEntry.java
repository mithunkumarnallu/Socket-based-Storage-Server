/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketbasedstorageserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ashwin Bahulkar, Siddharth Shenolikar, Mithun Nallu
 */

public class FileTableEntry {
    public String filename;
    public List<Page> pageList;
    public long timestamp;
    private PageManager pageManager = null;
    public boolean isOccupied=false;
    int nextPageNo = 0;
    
    //pageList holds all the Page objects for this file.
    //timestamp is used to find the file whose pages have to be removed from page table
    //pageManager is used to perfrom page related operations
    public FileTableEntry(String filename, PageManager pageManager) {
        this.filename = filename;
        this.pageList = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
        this.pageManager = pageManager;
    }
    
    //Sends messages to client
    private void sendMessageToClient(String message, DataOutputStream output) throws IOException {
        try {
            output.writeUTF(message + "\n");
        } catch (IOException ex) {
            System.err.println(ex);
            throw ex;
        }
    }
    
    //Outputs output on the console on the server side
    private void printOutputToConsole(String response, long threadId) {
        String[] messages = response.split("\n");
        for(String msg: messages) {
            if(!msg.equals(""))
                System.out.println("[thread " + threadId + "] " + msg);
        }
    }
    
    //Takes care of storing a file on the file system. Also writes proper output to console and to the client
    private void storeFile(String command, DataOutputStream output, long threadId) throws IOException {
        String commandInfo = "", fileContents = "";
        
        if(command.indexOf('\n') != -1) {
            commandInfo = command.substring(0, command.indexOf('\n'));
            fileContents = command.substring(command.indexOf('\n') + 1);
        }
        else {
            commandInfo = command.substring(0, command.indexOf("\\n"));
            fileContents = command.substring(command.indexOf("\\n") + 1);
        }
            
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

    //Takes care of deleting a file on the file system. Also frees up pages allocated for it and writes proper output to console and to the client
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
        sendMessageToClient(response, output);
        this.isOccupied=false;
        response = pageManager.freePages(filename) + "\n" + response;
        printOutputToConsole(response, threadId);
    }
    
    //Takes care of reading a file from the file system. Also writes proper output to console and to the client
    private void readFile(String command, DataOutputStream output, long threadId) throws IOException {
        String[] commandInfo = command.split(" ");
        
        String fileName = commandInfo[1];
        long byteOffset = Long.parseLong(commandInfo[2]);
        long length = Long.parseLong(commandInfo[3]);
        
        File file = new File(".store//" + fileName);
        String response = "";

        try {
            //Invalid inputs
            if (!file.exists()) {
                response = "ERROR: File does not exist!";
                sendMessageToClient(response, output);
                printOutputToConsole(response, threadId);
            } else if(length + byteOffset > file.length()) {
                response = "ERROR: Invalid byte range!";
                sendMessageToClient(response, output);
                printOutputToConsole(response, threadId);
            }
            else {
                //Valid input. Split the input into multiple pages and read them sequentially from file system.
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
                    
                    sendMessageToClient("ACK " + bytesSent + "\n" + pageMessage.page.getContent((int)(byteOffset - bytesSent) % 1024, bytesSent), output);
                    printOutputToConsole(pageMessage.message + "\nTransferred " + bytesSent + " bytes from offset " + (byteOffset - bytesSent), threadId);
                    nextPageNo = (nextPageNo + 1) % 4;
                }
            }
        } catch (Exception ex) {
            System.err.println(ex);
            throw ex;
        }
    }
    
    //Gets oldest page allocated for this file
    public Page getOldestPage()
    {
        return this.pageList.get(nextPageNo);
    }
    
    //Main synchronized method to ensure only one client is running a command on this file.
    //Interprets the command and calls correspoding methods to perform the actions
    public synchronized boolean runCommand(String command, DataOutputStream output, long threadId)
    {
        isOccupied=true;
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
            isOccupied=false;
            return true;
        }
        catch(IOException ex) {
            printOutputToConsole("Client closed its socket... terminating", threadId);
            return false;
        }
    }
}
