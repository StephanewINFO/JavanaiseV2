/** *
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact:
 *
 * Authors:
 */
package jvn;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JvnCoordImpl
        extends UnicastRemoteObject
        implements JvnRemoteCoord {

    private int id;
    private HashMap<String, Integer> objectNames;
    private HashMap<Integer, JvnObject> jvnObjects;
    private HashMap<Integer, HashSet<JvnRemoteServer>> readers;
    private HashMap<Integer, JvnRemoteServer> writer;
    ArrayList<JvnRemoteServer> listServersLocaux;
    

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
        listServersLocaux = new ArrayList<>();
        

        File stockServeurs = new File("stockServeurs.txt");
        File stockServeursReaders = new File("stockServeursReaders.txt");
        File stockServeurWriter = new File("stockServeurWriter.txt");
        File stockObjName = new File("objNames.txt");
        File stockJvnObject = new File("jvnObjects.txt");

        
        
        if (stockServeurs.exists()) {

            recuperateJvnConnectServerRemote(stockServeurs.getName());
            recuperateJvnConnectReadersWriters(stockServeursReaders.getName(), stockServeurWriter.getName());
            recuperateJvnObjects(stockObjName.getName(), stockJvnObject.getName());

            stockServeurs.delete();
            stockServeursReaders.delete();
            stockServeurWriter.delete();
            stockObjName.delete();
            stockJvnObject.delete();
            
        } else {
            System.out.println("n'existe pas stock de donnees");
        }

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
            stockJvnServerRemoteObjet(objectNames, "objNames.txt");
            jvnObjects.put(jo.jvnGetObjectId(), jo);
            stockJvnServerRemoteObjet(jvnObjects, "jvnObjects.txt");
            this.readers.put(jo.jvnGetObjectId(), new HashSet());
            //Posible actalizar lectores
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
                stockJvnServerRemoteObjet(jvnObjects, "jvnObjects.txt");
                writer.put(joi, null);
                readers.get(joi).add(writerJvnServer);
// call stockJvnServerRemote??
            }
            readers.get(joi).add(js);
            stockJvnServerRemoteObjet(readers, "stockServeursReaders.txt");
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
                    stockJvnServerRemoteObjet(jvnObjects, "jvnObjects.txt");
                    writer.put(joi, null);
                    stockJvnServerRemoteObjet(writer, "stockServeurWriter.txt");

                } else {
                    if (!(readers.get(joi).isEmpty())) {
                        for (JvnRemoteServer s : readers.get(joi)) {
                            s.jvnInvalidateReader(joi);

                        }
                        readers.get(joi).clear();
                        stockJvnServerRemoteObjet(readers, "stockServeursReaders.txt");
                    }
                }
            }
            writer.put(joi, js);
            stockJvnServerRemoteObjet(writer, "stockServeurWriter.txt");
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
    //SBA: actualizar archivos 
    public synchronized void jvnTerminate(JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {
        HashSet jvnRemoteServer;

        listServersLocaux.remove(js);

        for (Map.Entry<Integer, HashSet<JvnRemoteServer>> entry : readers.entrySet()) {
            jvnRemoteServer = entry.getValue();
            if (jvnRemoteServer.contains(js)) {
                jvnRemoteServer.remove(js);
            }
        }

        for (Map.Entry<Integer, JvnRemoteServer> entry : writer.entrySet()) {

            if (writer.containsValue(js)) {
                writer.replace(entry.getKey(), null);
            }

        }

        // y actualizar el objeto -> ??
    }

    @Override
    public void jvnConnectServerRemote(JvnRemoteServer js) throws RemoteException, JvnException {

        listServersLocaux.add(js);
        //System.out.println("Serveur connecte: "+js.getIdServerLocal()+ "Nombre de Serveurs Locaux connecte: "+ listServersLocaux.size());
        System.out.println("Nombre de Serveurs Locaux connecte: " + listServersLocaux.size());
        stockJvnServerRemoteObjet(listServersLocaux, "stockServeurs.txt");

    }

    @Override
    public File stockJvnServerRemoteObjet(Serializable jsStock, String nameFile) throws RemoteException, JvnException {
        File file = null;

        try {

            file = new File(nameFile);
            FileOutputStream fos = null;

            fos = new FileOutputStream(file, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            /* if (!file.exists()) {
                
                file.createNewFile();        
            }*/
            oos.writeObject(jsStock);
            fos.flush();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(JvnCoordImpl.class.getName()).log(Level.SEVERE, null, ex);

        } catch (IOException ex) {
            Logger.getLogger(JvnCoordImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return file;

    }

    @Override
    public void recuperateJvnConnectServerRemote(String nomFichier) throws RemoteException {
        JvnRemoteServer js;
        try {
            FileInputStream streamIn = new FileInputStream(nomFichier);
            ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
            ArrayList arrayJvnServerRemote = (ArrayList) objectinputstream.readObject();
            System.out.println("servidores conectados" + arrayJvnServerRemote.size());
            listServersLocaux = arrayJvnServerRemote;
            for (int i = 0; i < arrayJvnServerRemote.size(); i++) {

                js = (JvnRemoteServer) arrayJvnServerRemote.get(i);
                js.coordReconect(this);
                System.out.println("Coordinateur Reconect");
                System.out.println(js.getIdServerRemote());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void recuperateJvnConnectReadersWriters(String nomFichierReader, String nomFichierWriter) throws RemoteException {

        try {
            FileInputStream streamInReaders = new FileInputStream(nomFichierReader);
            ObjectInputStream objectinputstreamReaders = new ObjectInputStream(streamInReaders);
            HashMap JvnServeursReaders = (HashMap) objectinputstreamReaders.readObject();
            readers = JvnServeursReaders;
            

            FileInputStream streamInWriter = new FileInputStream(nomFichierWriter);
            ObjectInputStream objectinputstreamWriter = new ObjectInputStream(streamInWriter);
            HashMap JvnServeurWriter = (HashMap) objectinputstreamWriter.readObject();
            writer = JvnServeurWriter;
            

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void recuperateJvnObjects(String nomFichierObjName, String nomFichierJvnObject) throws RemoteException {

        try {
            FileInputStream streamInReaders = new FileInputStream(nomFichierObjName);
            ObjectInputStream objectinputstreamReaders = new ObjectInputStream(streamInReaders);
            HashMap JvnObjectNames = (HashMap) objectinputstreamReaders.readObject();
            objectNames = JvnObjectNames;
  

            FileInputStream streamInWriter = new FileInputStream(nomFichierJvnObject);
            ObjectInputStream objectinputstreamWriter = new ObjectInputStream(streamInWriter);
            HashMap jvnObjectsRecup = (HashMap) objectinputstreamWriter.readObject();
            jvnObjects = jvnObjectsRecup;
          

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        JvnRemoteCoord h_stub;
        try {
            h_stub = new JvnCoordImpl();
            Registry registry = LocateRegistry.createRegistry(4300);
            registry.rebind("Coord", h_stub);
            System.out.println("Server ready");

        } catch (Exception e) {
            System.err.println("Error on server :" + e);
            e.printStackTrace();
        }

    }



}
