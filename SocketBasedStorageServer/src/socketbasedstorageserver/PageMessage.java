/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketbasedstorageserver;

/**
 *
 * @author Ashwin Bahulkar, Siddharth Shenolikar, Mithun Nallu
 */

public class PageMessage {
    
    String message;
    Page page;

    public PageMessage(String message, Page page) {
        this.message = message;
        this.page = page;
    }
    
    
    
}
