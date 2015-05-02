/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketbasedstorageserver;

import Interfaces.PageMethods;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 *
 * @author ashwinbahulkar
 */
public class PageManager implements PageMethods{

    public SocketBasedStorageServer server;
    char[][] pageArray;
    public PageManager(SocketBasedStorageServer server) {
        pageArray=new char[32][1024];
        this.server=server;
    }
    
    

    @Override
    public synchronized  Page getNewPage(String fileName,int pageNo) {
       
        FileTableEntry entry=server.fileMap.get(fileName);
        
        if(entry==null)
        {
            return createNewPage(fileName, pageNo);
        }
        else
        {
            for(Page pageEnt:entry.pageList)
            {
                
                if(pageNo==pageEnt.pageNo)
                {
                    return pageEnt;
                }
            }
            return createNewPage(fileName, pageNo);
            
            
        }
        
        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public synchronized Page createNewPage(String fileName,int pageNo)
    {
        FileTableEntry fileTableEntry = server.fileMap.get(fileName);
        Page page = GetPageFromSecondaryStorage(fileName, pageNo);
        if(fileTableEntry!=null)
        {
            //File entry exists. You may be required to replace it's corresponding pages.
            
            //Do we need to replace a page!
            if(fileTableEntry.pageList.size()<4)
            {
                //It means theres more room.
                fileTableEntry.pageList.add(page);
            }
            else
            {
                //You need to replace a page.
                //Find LRU page.
                Page LruPage = GetOldestPage(fileTableEntry);
                fileTableEntry.pageList.remove(LruPage);
                fileTableEntry.pageList.add(page);
            }
        }
        else
        {
            //You may be required to replace a corresponding file entry
            if(server.fileMap.size()<8)
            {
                FileTableEntry newFileTableEntry = new FileTableEntry(fileName);
                newFileTableEntry.pageList.add(page);
                server.fileMap.put(fileName, newFileTableEntry);
            }
            else
            {
                FileTableEntry oldestFile = GetOldestFile(server.fileMap);
                server.fileMap.remove(oldestFile.filename);
                FileTableEntry newFileTableEntry = new FileTableEntry(fileName);
                newFileTableEntry.pageList.add(page);
                server.fileMap.put(fileName, newFileTableEntry);
            }
        }
        return page;
    }
    
    public Page GetPageFromSecondaryStorage(String fileName, int pageNo)
    {
        return null;
    }
    
    private Page GetOldestPage(FileTableEntry pageList)
    {
        Page oldestPage = pageList.pageList.get(0);
        
        for(Page page : pageList.pageList)
        {
            if(page.timeStamp<oldestPage.timeStamp)
            {
                oldestPage = page;
            }
        }
        return oldestPage;
        
    }
    
     private FileTableEntry GetOldestFile(HashMap<String,FileTableEntry> fileMap)
    {
        FileTableEntry oldestFile;
        oldestFile=null;
        
        for(Entry<String,FileTableEntry> file : fileMap.entrySet())
        {
            if(oldestFile == null || file.getValue().timestamp<oldestFile.timestamp)
            {
                oldestFile = file.getValue();
            }
        }
        return oldestFile;
    }
    
    //public synchronized int 
    
}
