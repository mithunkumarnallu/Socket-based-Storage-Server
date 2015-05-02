/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketbasedstorageserver;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ashwinbahulkar
 */
public class FileTableEntry {

    public FileTableEntry(String filename) {
        this.filename = filename;
        this.pageList = new ArrayList<>();
        timestamp = System.currentTimeMillis();
    }
    
    
    public String filename;
    public List<Page> pageList;
    public long timestamp;
    
    public synchronized void runCommand(String command)
    {
        
    }
    
    
}
