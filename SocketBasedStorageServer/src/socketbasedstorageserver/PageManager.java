/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketbasedstorageserver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Ashwin Bahulkar, Siddharth Shenolikar, Mithun Nallu
 */

//alocates pages for a requested file,frees pages etc.
public class PageManager {

    int usedPageCnt;
    public SocketBasedStorageServer server;

    public HashMap<String, HashMap<Integer, Page>> usedpageMap = new HashMap<String, HashMap<Integer, Page>>();
    public HashMap<Integer, Page> avlbpageMap = new HashMap<Integer, Page>();

    public PageManager(SocketBasedStorageServer server) {

        for (int i = 0; i < 32; i++) {
            Page page = new Page(i);
            avlbpageMap.put(i, page);
        }
        this.server = server;
        usedPageCnt = 0;
    }

    //gets a new page for the requested file segment
    public synchronized PageMessage getNewPage(String fileName, int pageNo) {

        FileTableEntry entry = server.getFileTableEntry(fileName);
        Page page = null;
        PageMessage pageMessage = null;

        for (Page pageEnt : entry.pageList) {
            if (pageNo == pageEnt.pageNo) {

                page = pageEnt;
                //page.timeStamp=System.currentTimeMillis();
                pageMessage = new PageMessage("", page);
                break;
            }
        }
        if (page == null) {
            try {
                pageMessage = createNewPage(fileName, pageNo);
                page = pageMessage.page;
            } catch (IOException ex) {
                page = null;
                ex.printStackTrace();
            }
        }

        if (page != null) {
            page.timeStamp = System.currentTimeMillis();
            entry.timestamp = System.currentTimeMillis();
        }

        return pageMessage;
    }

    //creates a new page for requested file,only if needed
    public synchronized PageMessage createNewPage(String fileName, int pageNo) throws IOException {
        String msg = "";
        FileTableEntry fileTableEntry = server.getFileTableEntry(fileName);
        Page page;
        PageMessage pageMsg = null;
        if (fileTableEntry != null) {
            //File entry exists. You may be required to replace it's corresponding pages.
            fileTableEntry.timestamp = System.currentTimeMillis();
            //Do we need to replace a page!

            if (fileTableEntry.pageList.size() < 4) {
                //It means theres more room.
                pageMsg = getAvlbPage();
                page = pageMsg.page;
                GetPageFromSecondaryStorage(fileName, pageNo, page);
                fileTableEntry.pageList.add(page);
                msg += pageMsg.message + "\nAllocated page " + pageNo + " to frame " + page.frameNo;
                pageMsg.message = msg;
            } else {
                //You need to replace a page.
                //Find LRU page.
                int prevPgNo;

                page = GetOldestPage(fileTableEntry);
                prevPgNo = page.pageNo;
                GetPageFromSecondaryStorage(fileName, pageNo, page);
                //page.timeStamp = System.currentTimeMillis();
                msg = "Allocated page " + pageNo + " to frame " + page.frameNo + " (replaced page " + prevPgNo + ")";
                pageMsg = new PageMessage(msg, page);
                // fileTableEntry.pageList.remove(LruPage);

                //fileTableEntry.pageList.add(page);
            }
        }

        return pageMsg;
    }

    //gets page from secondary storage
    public Page GetPageFromSecondaryStorage(String fileName, int pageNo, Page page) throws FileNotFoundException, IOException {

        try (RandomAccessFile file = new RandomAccessFile(".store//" + fileName, "rw")) {
            long pageStartOffset = (pageNo  - 1)* 1024;
            file.seek(pageStartOffset);

            page.pageNo = pageNo;
            int i;
            //Read 1024 bytes from file
            for (i = 0; (i < 1024) && (pageStartOffset + i < file.length()); i++) {
                page.pageContent[i] = file.readByte();
            }
            page.contentsInString = new String(page.pageContent);
            page.bytesInPage = i;
        }
        return page;
    }

    //LRU algorithm
    private Page GetOldestPage(FileTableEntry fileTableEntry) {
        Page oldestPage = fileTableEntry.getOldestPage();
        return oldestPage;
    }

    //gets oldest file
    public FileTableEntry GetOldestFile(HashMap<String, FileTableEntry> fileMap) {
        FileTableEntry oldestFile;
        oldestFile = null;

        for (Entry<String, FileTableEntry> file : fileMap.entrySet()) {
            if (oldestFile == null || file.getValue().timestamp < oldestFile.timestamp) {
                oldestFile = file.getValue();
            }
        }
        return oldestFile;
    }

    //frees pages
    public synchronized String  freePages(String filename) {
        String msg = "";
        FileTableEntry entry = server.fileMap.get(filename);
        if(entry != null) {
            for (Page page : entry.pageList) {
                freePage(page);

                msg = msg + "Deallocated frame " + page.frameNo + "\n";
            }
            while(entry.isOccupied==true)
            {
                continue;
            }
            server.fileMap.remove(filename);
        }
        return msg;
    }

    public void freePage(Page page) {
        usedPageCnt--;
        avlbpageMap.put(page.frameNo, page);
    }

    //gets a page from available pages
    public PageMessage getAvlbPage() {
        String msg = "";
        if (usedPageCnt == 32) {
            FileTableEntry entry = GetOldestFile(server.fileMap);
            msg = freePages(entry.filename);

        }
        usedPageCnt++;
        
        Map.Entry<Integer, Page> entry = avlbpageMap.entrySet().iterator().next();
        avlbpageMap.remove(entry.getKey());
        //entry.getValue().timeStamp = System.currentTimeMillis();
        
        return new PageMessage(msg, entry.getValue());
    }
    //public synchronized int 

}
