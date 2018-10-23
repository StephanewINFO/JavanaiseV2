/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jvn;

import irc.IFoo;
import java.lang.reflect.Proxy;

/**
 *
 * @author stephanie
 */
public class TestingProxy {
    
    
    public static Object getNewProxy(Object proxied, Class<?> interfaze) {
  IFoo proxy = (IFoo)Proxy.newProxyInstance(
      JvnInvocationHandler.class.getClassLoader(),
      new Class[] {interfaze}, 
      new JvnInvocationHandler(proxied));
  return proxy;
 }

}

        