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

/**
 *
 * @author stephanie
 */
public class JvnInvocationHandler implements InvocationHandler{
private Object obj;


public JvnInvocationHandler(Object obj){
    this.obj=obj;
}


@Override
public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable
    {
        
       Method m = obj.getClass().getMethod(method.getName(), method.getParameterTypes());
  if (m.isAnnotationPresent(AnnotationReader.class)) {
      
           ((Irc)obj).sentence.jvnLockRead();
            String s = ((Sentence) (((Irc)obj).sentence.jvnGetObjectState())).read();
            ((Irc)obj).sentence.jvnUnLock();
            ((Irc)obj).data.setText(s);
            ((Irc)obj).text.append(s + "\n");

 //  System.out.println("\tIn the annotation processor Reader");   
  } 
  
  else if (m.isAnnotationPresent(AnnotationWriter.class)){
  
            String s = ((Irc)obj).data.getText();
            ((Irc)obj).sentence.jvnLockWrite();
            ((Sentence) (((Irc)obj).sentence.jvnGetObjectState())).write(s);
            ((Irc)obj).sentence.jvnUnLock();
      
 //System.out.println("\tIn the annotation processor Writer");      
      
  }
    return method.invoke(obj, args);

}
}
