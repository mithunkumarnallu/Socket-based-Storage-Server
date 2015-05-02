/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketbasedstorageserver;

import java.util.List;

/**
 *
 * @author ashwinbahulkar
 */
public class FileTableEntry {

    public FileTableEntry(String filename, List<Page> pageList) {
        this.filename = filename;
        this.pageList = pageList;
    }
    
    
    public String filename;
    public List<Page> pageList;
    
    
    public synchronized void runCommand(String command)
    {
        
    }
    
    
}
