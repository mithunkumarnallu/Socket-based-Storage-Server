/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketbasedstorageserver;

import Interfaces.PageMethods;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
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
       
        FileTableEntry entry=server.getFileTableEntry(fileName);
        Page page = null;
        
//        if(entry==null)
//        {
//            try {
//                page = createNewPage(fileName, pageNo);
//            } catch (IOException ex) {
//                page = null;
//                ex.printStackTrace();
//            }
//        }
        //else
        {
            for(Page pageEnt:entry.pageList)
            {
                if(pageNo==pageEnt.pageNo)
                {
                    page =  pageEnt;
                    break;
                }
            }
            if(page == null) {
                try {
                    page =  createNewPage(fileName, pageNo);
                } catch (IOException ex) {
                    page = null;
                    ex.printStackTrace();
                }    
            }
        }
        
        if(page!=null)
            page.timeStamp = System.currentTimeMillis();
        
      return page;
    }
    
    public synchronized Page createNewPage(String fileName,int pageNo) throws IOException
    {
        FileTableEntry fileTableEntry = server.getFileTableEntry(fileName);
        Page page = GetPageFromSecondaryStorage(fileName, pageNo);
        if(fileTableEntry!=null)
        {
            //File entry exists. You may be required to replace it's corresponding pages.
            fileTableEntry.timestamp = System.currentTimeMillis();
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
                FileTableEntry newFileTableEntry = new FileTableEntry(fileName, this);
                newFileTableEntry.pageList.add(page);
                server.fileMap.put(fileName, newFileTableEntry);
            }
            else
            {
                FileTableEntry oldestFile = GetOldestFile(server.fileMap);
                server.fileMap.remove(oldestFile.filename);
                FileTableEntry newFileTableEntry = new FileTableEntry(fileName, this);
                newFileTableEntry.pageList.add(page);
                server.fileMap.put(fileName, newFileTableEntry);
            }
        }
        return page;
    }
    
    public Page GetPageFromSecondaryStorage(String fileName, int pageNo) throws FileNotFoundException, IOException
    {
        Page page;
        try (RandomAccessFile file = new RandomAccessFile(".store//" + fileName, "rw")) {
            long pageStartOffset = pageNo*1024;
            file.seek(pageStartOffset);
            page = new Page();
            page.pageNo=pageNo;
            int i;
            //Read 1024 bytes from file
            for(i=0;(i<1024)&&(pageStartOffset+i < file.length());i++)
            {
                page.pageContent[i] = file.readByte();
            }   page.bytesInPage = i;
        }
        return page;
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
    
    public FileTableEntry GetOldestFile(HashMap<String,FileTableEntry> fileMap)
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
