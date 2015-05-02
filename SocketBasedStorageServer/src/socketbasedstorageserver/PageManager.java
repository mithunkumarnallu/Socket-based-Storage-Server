/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketbasedstorageserver;

import Interfaces.PageMethods;
import java.util.List;

/**
 *
 * @author ashwinbahulkar
 */
public class PageManager implements PageMethods{

    public SocketBasedStorageServer server;
    public PageManager(SocketBasedStorageServer server) {
        this.server=server;
    }
    
    

    @Override
    public Page getNewPage(String fileName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void freePages(List<Page> pages) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
}
