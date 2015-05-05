/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketbasedstorageserver;

/**
 *
 * @author ashwinbahulkar
 */
public class Page {
    
    int pageNo;
    byte[] pageContent;
    long timeStamp;
    int bytesInPage;
    int frameNo;
    
    Page(int frameNo)
    {
        this.frameNo=frameNo;
        timeStamp = System.currentTimeMillis();
    }
    
    
}
