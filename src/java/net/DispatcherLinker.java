package net;

import dao.DAO;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import model.beans.Collectors;
import org.apache.log4j.Logger;
import util.IOUtilities;

/**
 * dispatch each client
 *
 * @author skuarch
 */
public class DispatcherLinker {

    private static final Logger logger = Logger.getLogger(DispatcherLinker.class);
    private InputStream myInputStream = null;
    private OutputStream myOutputStream = null;
    private ObjectOutputStream myObjectOutputStream = null;
    private ObjectInputStream myObjectInputStream = null;
    private OutputStream remoteOutputStream = null;
    private ObjectOutputStream remoteObjectOutputStream = null;
    private InputStream remoteInputStream = null;
    private ObjectInputStream remoObjectInputStream = null;
    private Socket remoteSocket = null;
    private Collectors collector = null;
    private HashMap hashMap = null;

    //==========================================================================
    /**
     * create a instance.
     *
     * @param myInputStream InputStream
     * @param myOutputStream OutputStream
     */
    public DispatcherLinker(HashMap hashMap, InputStream myInputStream, OutputStream myOutputStream) {
        this.hashMap = hashMap;
        this.myInputStream = myInputStream;
        this.myOutputStream = myOutputStream;        
    } // end DispatcherClient

    //==========================================================================
    /**
     * in this method the main server receives the object and sends the object
     * to remote server
     */
    public synchronized void transferData() {

        if (myInputStream == null) {
            throw new NullPointerException("myInputStream is null");
        }

        if (myOutputStream == null) {
            throw new NullPointerException("myOutputStream is null");
        }
        
        Object objectRemote = null;        

        try {

            logger.info("attending client ");

            //get collector
            collector = getCollector(hashMap.get("collector").toString());

            //create socket 
            createRemoteSocket(collector.getIp(), collector.getPort());

            //tranfer object to remote server            
            transferObjectRemoteServer(hashMap);

            //wainting response from remote server
            objectRemote = receiveObjectFromRemoteServer();

            //resend remoteObject to client
            sendObjectClient(objectRemote);

        } catch (Exception e) {
            logger.error("server: " + e + " " + collector.getIp() + " port: " + collector.getPort());
            e.printStackTrace();
            sendErrorClient(e);
        } finally {
            closer();
        }

    } // end transferData

    //==========================================================================
    private void createRemoteSocket(String ip, int port) throws Exception {

        try {
            remoteSocket = new Socket(collector.getIp(), collector.getPort());
        } catch (Exception e) {
            throw e;
        }
    }

    //==========================================================================
    /**
     * return to client the new object.
     *
     * @param objectRemote Object
     */
    public void sendObjectClient(Object objectRemote) throws Exception {

        try {
            myObjectOutputStream = new ObjectOutputStream(myOutputStream);
            myObjectOutputStream.writeObject(objectRemote);
        } catch (Exception e) {
            throw e;
        }

    } // end sendObjectClient
    
    //==========================================================================
    /**
     * transfer object to remote server.
     *
     * @param remoteServer String IP address or hostname.
     * @param port int port of remote server.
     * @param object object
     */
    private void transferObjectRemoteServer(Object object) throws Exception {

        try {

            remoteOutputStream = remoteSocket.getOutputStream();
            remoteObjectOutputStream = new ObjectOutputStream(remoteOutputStream);
            remoteObjectOutputStream.writeObject(object);
            remoteObjectOutputStream.flush();

        } catch (Exception e) {
            throw e;
        }

    } // end transferObject

    //==========================================================================
    private Object receiveObjectFromRemoteServer() throws Exception {

        Object object = null;

        try {

            remoteInputStream = remoteSocket.getInputStream();
            remoObjectInputStream = new ObjectInputStream(remoteInputStream);

            while (true) {
                object = remoObjectInputStream.readObject();
                if (object != null) {
                    break;
                }
            }

        } catch (Exception e) {
            throw e;
        }

        return object;

    } // end receiveObjectFromRemoteServer

    //==========================================================================
    /**
     * send error to client
     *
     * @param object Exception
     */
    private void sendErrorClient(Object object) {

        try {
            logger.error("sending error to client");
            myObjectOutputStream = new ObjectOutputStream(myOutputStream);
            myObjectOutputStream.writeObject(object);
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }

    } // end sendErrorClient

    //==========================================================================
    /**
     * return a collector
     *
     * @param collectorName String name of collector
     * @return Collectors
     * @throws Exception
     */
    private Collectors getCollector(String collectorName) throws Exception {

        if (collectorName == null || collectorName.length() < 1) {
            throw new NullPointerException("collectorName is null or empty");
        }

        Collectors collector = null;

        try {

            collector = (Collectors) new DAO().find(Collectors.class, collectorName, "name").get(0);

        } catch (Exception e) {
            throw e;
        }

        return collector;

    } // end getCollectorIP

    //==========================================================================
    /**
     * close all
     */
    private void closer() {
        IOUtilities.closeOutputStream(remoteObjectOutputStream);
        IOUtilities.closeOutputStream(remoteOutputStream);
        IOUtilities.closeInputStream(remoteInputStream);
        IOUtilities.closeInputStream(remoObjectInputStream);
        IOUtilities.closeInputStream(myInputStream);
        IOUtilities.closeInputStream(myObjectInputStream);
        IOUtilities.closeOutputStream(myOutputStream);
        IOUtilities.closeOutputStream(myObjectOutputStream);
        IOUtilities.closeSocket(remoteSocket);
    } // end closer
} // end class
