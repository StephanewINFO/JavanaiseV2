/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irc;

import Annotation.AnnotationReader;
import Annotation.AnnotationWriter;

/**
 *
 * @author stephanie
 */
public interface IFoo {
    
    @AnnotationReader
    public String read();
    
    @AnnotationWriter
     public void write(String text);
    
}
