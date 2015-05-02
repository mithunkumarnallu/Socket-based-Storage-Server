/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interfaces;
import java.util.List;
import socketbasedstorageserver.*;

/**
 *
 * @author ashwinbahulkar
 */
public interface PageMethods {
    
   Page getNewPage(String fileName);
   void freePages(List<Page> pages);
   
    
}
