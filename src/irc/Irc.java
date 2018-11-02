/** *
 * Irc class : simple implementation of a chat using JAVANAISE
 * Contact:
 *
 * Authors:
 */
package irc;

import com.sun.security.auth.module.JndiLoginModule;
import java.awt.*;
import java.awt.event.*;

import jvn.*;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.Scanner;
import Annotation.*;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Irc implements IFoo{

    public TextArea text;
    public TextField data;
    Frame frame;
    public JvnObject sentence;
    private static IFoo instance;
    private static JvnServerImpl js;

    /**
     * main method create a JVN object nammed IRC for representing the Chat
     * application
  *
     */
   public static void main(String argv[]) {
        try {

            // initialize JVN
            js = JvnServerImpl.jvnGetServer();

            // look up the IRC object in the JVN server
            // if not found, create it, and register it in the JVN server
            JvnObject jo = js.jvnLookupObject("IRC");

            if (jo == null) {
                jo = js.jvnCreateObject((Serializable) new Sentence());
                // after creation, I have a write lock on the object
                js.jvnRegisterObject("IRC", jo);
            }
            // create the graphical part of the Chat application
            new Irc(jo);

        } catch (Exception e) {
            System.out.println("IRC problem : " + e.getMessage());
        }
    }
    
    
    


    /**
     * IRC Constructor
     *
     * @param jo the JVN object representing the Chat
   *
     */
    public Irc(JvnObject jo) {


        try {
            if (instance == null) {
                instance = (IFoo) TestingProxy.getNewProxy(this,
                        IFoo.class);
            }
            
            
            sentence = jo;
            frame = new Frame();
            frame.setLayout(new GridLayout(1, 1));
            text = new TextArea(10, 60);
            text.setEditable(false);
            text.setForeground(Color.red);
            frame.add(text);
            data = new TextField(40);
            frame.add(data);
            Button read_button = new Button("read");
            read_button.addActionListener(new readListener(instance));
            frame.add(read_button);
            Button write_button = new Button("write");
            write_button.addActionListener(new writeListener(instance));
            frame.add(write_button);
            frame.setSize(545, 201);
            text.setBackground(Color.black);
            frame.setVisible(true);
            frame.setTitle("Id Serveur Remote "+Irc.js.getIdServerRemote());
            frame.addWindowListener( new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                    try {
                        Irc.js.jvnTerminate();
                    } catch (JvnException ex) {
                        Logger.getLogger(Irc.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.exit(0);
                    
                    
                }
            } );
        } catch (RemoteException ex) {
            Logger.getLogger(Irc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    



    
    @AnnotationReader
    public void x() {
       //System.out.println("1 - Text in annotated method->Read"); 
       
       
    }

    @AnnotationWriter
    public void y() {
 //System.out.println("2 - Text in annotated method->Writer");        

    }


}
 


/**
 * Internal class to manage user events (read) on the CHAT application
  *
 */
class readListener implements ActionListener {

    IFoo irc;

    public readListener(IFoo i) {
        irc = i;
    }

    /**
     * Management of user events
  *
     */
    public void actionPerformed(ActionEvent e) {
        irc.x();
        
  }
}

/**
 * Internal class to manage user events (write) on the CHAT application
  *
 */
class writeListener implements ActionListener {

    IFoo irc;

    public writeListener(IFoo i) {
        irc = i;
    }

    /**
     * Management of user events
   *
     */
    public void actionPerformed(ActionEvent e) {
        irc.y();
 }
}
