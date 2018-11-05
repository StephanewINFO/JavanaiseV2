/***
 * Sentence class : used for representing the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package irc;

import Annotation.AnnotationReader;
import Annotation.AnnotationWriter;

public class Sentence implements java.io.Serializable, IFoo {
	String 		data;
  
	public Sentence() {
		data = "";
	}
	@AnnotationWriter
	public void write(String text) {
		data = text;
	}
        @AnnotationReader
	public String read() {
		return data;	
	}

    
  
}