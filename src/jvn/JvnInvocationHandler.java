/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jvn;

import irc.IFoo;
import irc.Irc;
import irc.Sentence;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import Annotation.*;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author stephanie
 */
public class JvnInvocationHandler implements InvocationHandler{
private JvnObject obj;


public JvnInvocationHandler(JvnObject obj){
    this.obj=obj;
}


 public static Object newInstance(Serializable object) {
     JvnObject jo = null;
    try {
        JvnServerImpl js = JvnServerImpl.jvnGetServer();
        
        // look up the IRC object in the JVN server
        // if not found, create it, and register it in the JVN server
        jo = js.jvnLookupObject("IRC");
        
        if (jo == null) {
            jo = js.jvnCreateObject(object);
            // after creation, I have a write lock on the object
            js.jvnRegisterObject("IRC", jo);
        }
        
        

    } catch (JvnException ex) {
        Logger.getLogger(JvnInvocationHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
    return java.lang.reflect.Proxy.newProxyInstance(
            object.getClass().getClassLoader(),
            object.getClass().getInterfaces(),
            new JvnInvocationHandler(jo));
 }


@Override
public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable
    {
        
        Object res=null;

       
  if (method.isAnnotationPresent(AnnotationReader.class)) {
      
      
            obj.jvnLockRead();
            res=method.invoke(obj.jvnGetObjectState(), args);
            obj.jvnUnLock();


  } 

  else if (method.isAnnotationPresent(AnnotationWriter.class)){ 
        System.out.println("wrote avant");
            obj.jvnLockWrite();
            System.out.println("locked write");
            res=method.invoke(obj.jvnGetObjectState(), args);
            System.out.println("finished write");
            obj.jvnUnLock();
  
  }
  

    return res;

}
}
