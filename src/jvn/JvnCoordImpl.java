/** *
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact:
 *
 * Authors:
 */
package jvn;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class JvnCoordImpl
        extends UnicastRemoteObject
        implements JvnRemoteCoord {

    private int id;
    private HashMap<String, Integer> objectNames;
    private HashMap<Integer, JvnObject> jvnObjects;
    private HashMap<Integer, HashSet<JvnRemoteServer>> readers;
    private HashMap<Integer, JvnRemoteServer> writer;

    /**
     * Default constructor
     *
     * @throws JvnException
     *
     */
    private JvnCoordImpl() throws Exception {
        objectNames = new HashMap();
        jvnObjects = new HashMap();
        readers = new HashMap();
        writer = new HashMap();
    }

    /**
     * Allocate a NEW JVN object id (usually allocated to a newly created JVN
     * object)
     *
     * @throws java.rmi.RemoteException,JvnException
     *
     */
    public int jvnGetObjectId()
            throws java.rmi.RemoteException, jvn.JvnException {
        return id++;
    }

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jon : the JVN object name
     * @param jo : the JVN object
     * @param joi : the JVN object identification
     * @param js : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     *
     */
    public synchronized void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
            throws java.rmi.RemoteException, jvn.JvnException {
        if (!objectNames.containsKey(jon)) {
            objectNames.put(jon, jo.jvnGetObjectId());
            jvnObjects.put(jo.jvnGetObjectId(), jo);
            this.readers.put(jo.jvnGetObjectId(), new HashSet());
        }
    }

    /**
     * Get the reference of a JVN object managed by a given JVN server
     *
     * @param jon : the JVN object name
     * @param js : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     *
     */
    public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
            throws java.rmi.RemoteException, jvn.JvnException {

        JvnObject object = null;

        if (objectNames.containsKey(jon)) {
            object = this.jvnObjects.get(this.objectNames.get(jon));
        }
        return object;
    }

    /**
     * Get a Read lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     *
     */
    public synchronized Serializable jvnLockRead(int joi, JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {

        try {
            if (writer.get(joi) != null) {
                JvnRemoteServer writerJvnServer = writer.get(joi);
                //objectUpdate = writerJvnServer.jvnInvalidateWriterForReader(joi);
                this.jvnObjects.get(joi).jvnSetObjectState(writerJvnServer.jvnInvalidateWriterForReader(joi));
                writer.put(joi, null);
                readers.get(joi).add(writerJvnServer);
            }
            readers.get(joi).add(js);

        } catch (Exception e) {
            //Caso invalidate writer for reader con el JvnServer sin conexion
            System.err.println("Error on coordinator at jvnLockRead():" + e);
            e.printStackTrace();
        }
 
        return this.jvnObjects.get(joi).jvnGetObjectState();
    }

    /**
     * Get a Write lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     *
     */
    public synchronized Serializable jvnLockWrite(int joi, JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {
        try {
            if (writer.containsKey(joi)) {
                if (writer.get(joi) != null) {
                    JvnRemoteServer writerJvnServer = writer.get(joi);
                    //objectUpdate = writerJvnServer.jvnInvalidateWriter(joi);
                    this.jvnObjects.get(joi).jvnSetObjectState(writerJvnServer.jvnInvalidateWriter(joi));
                    writer.put(joi, null);

                } else {
                    if (!(readers.get(joi).isEmpty())) {
                        for (JvnRemoteServer s : readers.get(joi)) {
                            s.jvnInvalidateReader(joi);

                        }
                        readers.get(joi).clear();

                    }
                }
            }
            writer.put(joi, js);
            Thread.sleep(5000);
        } catch (Exception e) {

            //caso invalide write/reader servidor local sin conexion
            System.err.println("Error on server at jvnLockWrite():" + e);
            e.printStackTrace();
        }
        
        return this.jvnObjects.get(joi).jvnGetObjectState();
    }

    /**
     * A JVN server terminates
     *
     * @param js : the remote reference of the server
     * @throws java.rmi.RemoteException, JvnException
     *
     */
    public synchronized void jvnTerminate(JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {
    }

    //uego llamo al metodo en la excepton cuando el cordinador no tiene conexion
    public synchronized void jvnPanneCoordinateurReaders() throws RemoteException, JvnException {
        JvnRemoteServer jsr;
        for (Map.Entry<Integer, HashSet<JvnRemoteServer>> entry : readers.entrySet()) {
            Integer joi = entry.getKey();
            HashSet jvnReaders = entry.getValue();
            Iterator iter = jvnReaders.iterator();
            while (iter.hasNext()) {
                jsr = (JvnRemoteServer) iter.next();
                jsr.jvnInvalidateReader(joi);
            }
            

        }

    }

    public synchronized void jvnPanneCoordinateurWriter() throws RemoteException, JvnException {
        JvnRemoteServer jsr;
        for (Map.Entry<Integer, JvnRemoteServer> entry : writer.entrySet()) {
            Integer joi = entry.getKey();
            jsr = (JvnRemoteServer) entry.getValue();
            jsr.jvnInvalidateWriter(joi);
 
        }
  
    }




public static void main(String[] args) {
        JvnRemoteCoord h_stub;
        try {
            h_stub = new JvnCoordImpl();

            Registry registry = LocateRegistry.createRegistry(4500);
            registry.rebind("Coord", h_stub);
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Error on server :" + e);
            e.printStackTrace();
        }

    }
}
