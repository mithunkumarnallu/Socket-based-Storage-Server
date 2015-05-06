/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketbasedstorageserver;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ashwinbahulkar
 */
public class Page {
    
    int pageNo;
    byte[] pageContent;
    String contentsInString = "";
    long timeStamp;
    int bytesInPage;
    int frameNo;
    
    Page(int frameNo)
    {
        this.frameNo=frameNo;
        timeStamp = System.currentTimeMillis();
        this.pageContent = new byte[1024];
    }
    
    String getContent(int startIndex, int length) {
        return contentsInString.substring(startIndex, startIndex + length);
    }
}
