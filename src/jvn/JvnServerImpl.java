/** *
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact:
 *
 * Authors:
 */
package jvn;

import java.rmi.server.UnicastRemoteObject;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JvnServerImpl
        extends UnicastRemoteObject
        implements JvnLocalServer, JvnRemoteServer {

    // A JVN server is managed as a singleton 
    private HashMap<Integer, JvnObject> mapJvnObject;
    private static JvnServerImpl js = null;
    private static JvnRemoteCoord jrc;
    private static  int idServerLocal;

    /**
     * Default constructor
     *
     * @throws JvnException
  *
     */
    JvnServerImpl() throws Exception {
        super();
        mapJvnObject = new HashMap<Integer, JvnObject>();
        Random random = new Random();
        idServerLocal = random.nextInt(Integer.MAX_VALUE);
        try {

            Registry registry = LocateRegistry.getRegistry("localhost", 4300);
            jrc = (JvnRemoteCoord) registry.lookup("Coord");
            
          
        } catch (Exception e) {
            System.err.println("Error on client JvnServerImpl(): " + e);
            e.printStackTrace();
        }
    }



 

    
    /**
     * Static method allowing an application to get a reference to a JVN server
     * instance
     *
     * @throws JvnException
    *
     */
    public static JvnServerImpl jvnGetServer()  {
        if (js == null) {
            try {
                js = new JvnServerImpl();
                jrc.jvnConnectServerRemote(js);
               // js.showMessage(""+idServerLocal);
                System.out.println("Server Local Connect id: " + js.getIdServerRemote());
                
              
            } catch (Exception e) {
                return null;
            }
        }
        
        return js;
    }

   
    
   
    
    /**
     * The JVN service is not used anymore
     *
     * @throws JvnException
	*
     */
    public void jvnTerminate()
            throws jvn.JvnException {
        try {
            jrc.jvnTerminate(js);

        } catch (RemoteException ex) {
            Logger.getLogger(JvnServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * creation of a JVN object
     *
     * @param o : the JVN object state
     * @throws JvnException
	*
     */
    public JvnObject jvnCreateObject(Serializable o)
            throws jvn.JvnException {
        try {
            int id = jrc.jvnGetObjectId();
            JvnObject jvnObject = new JvnObjectImpl(id, o);
            mapJvnObject.put(id, jvnObject);
            return jvnObject;
        } catch (Exception e) {
            System.err.println("Error jvnCreateObject(): " + e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jon : the JVN object name
     * @param jo : the JVN object
     * @throws JvnException
	*
     */
    public void jvnRegisterObject(String jon, JvnObject jo)
            throws jvn.JvnException {
        try {
            jrc.jvnRegisterObject(jon, jo, js);
        } catch (RemoteException ex) {
            Logger.getLogger(JvnServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Provide the reference of a JVN object being given its symbolic name
     *
     * @param jon : the JVN object name
     * @return the JVN object
     * @throws JvnException
	*
     */
    public JvnObject jvnLookupObject(String jon)
            throws jvn.JvnException {
        JvnObject object = null;
        try {
            object = jrc.jvnLookupObject(jon, js);
            if (object != null) {
                JvnObject newJvnObj = new JvnObjectImpl(object.jvnGetObjectId(), object.jvnGetObjectState());
                mapJvnObject.put(newJvnObj.jvnGetObjectId(), newJvnObj);
                object = newJvnObj;
            }

        } catch (RemoteException ex) {
            Logger.getLogger(JvnServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return object;
    }

    //buscar en la lista local y su estado NL,RLC
    //sino existe busca en el coord y retorna el objeto
    // to be completed 
    /**
     * Get a Read lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
	*
     */
    public Serializable jvnLockRead(int joi)
            throws JvnException {
        Serializable result = null;
        try {
            result = jrc.jvnLockRead(joi, js);
            if (result == null) {
                result = mapJvnObject.get(joi).jvnGetObjectState();
            }
        } catch (RemoteException ex) {
            Logger.getLogger(JvnServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return result;

    }

    /**
     * Get a Write lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
	*
     */
    public Serializable jvnLockWrite(int joi)
            throws JvnException {
        Serializable result = null;
        try {
            result = jrc.jvnLockWrite(joi, js);
            if (result == null) {
                result = mapJvnObject.get(joi).jvnGetObjectState();
            }
        } catch (RemoteException ex) {
            Logger.getLogger(JvnServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return result;

    }

    //Sba:Methodes RemoteServer
    /**
     * Invalidate the Read lock of the JVN object identified by id called by the
     * JvnCoord
     *
     * @param joi : the JVN object id
     * @return void
     * @throws java.rmi.RemoteException,JvnException
	*
     */
    public void jvnInvalidateReader(int joi)
            throws java.rmi.RemoteException, jvn.JvnException {
        this.mapJvnObject.get(joi).jvnInvalidateReader();
    }

    /**
     * Invalidate the Write lock of the JVN object identified by id
     *
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException,JvnException
	*
     */
    public Serializable jvnInvalidateWriter(int joi)
            throws java.rmi.RemoteException, jvn.JvnException {
        return this.mapJvnObject.get(joi).jvnInvalidateWriter();
    }

    /**
     * Reduce the Write lock of the JVN object identified by id
     *
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException,JvnException
	*
     */
    public Serializable jvnInvalidateWriterForReader(int joi)
            throws java.rmi.RemoteException, jvn.JvnException {
        return this.mapJvnObject.get(joi).jvnInvalidateWriterForReader();
    }
    
  public void showMessage(String message) throws RemoteException {
        System.out.println(message);
    }

    @Override
    public int getIdServerRemote() throws RemoteException {
       return idServerLocal;
    }

    @Override
    public void coordReconect(JvnRemoteCoord coord) throws RemoteException {
        jrc=coord;
    }

  



}
