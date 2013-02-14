package net;

import model.beans.Collectors;
import dao.DAO;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import model.beans.Configuration;
import org.apache.log4j.Logger;
import util.IOUtilities;

/**
 *
 * @author skuarch
 */
class DispatcherObject {

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
    public DispatcherObject(HashMap hashMap, InputStream myInputStream, OutputStream myOutputStream) {

        this.hashMap = hashMap;
        this.myInputStream = myInputStream;
        this.myOutputStream = myOutputStream;

    } // end DispatcherObject

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

        Object object = null;
        String type = null;
        String request = null;

        try {

            logger.info("attending client ");

            //attending request            
            type = hashMap.get("type").toString();
            request = hashMap.get("request").toString();

            if ("objectRequester".equalsIgnoreCase(type)) {

                if ("get all Categories".equalsIgnoreCase(request)) {

                    object = new DAO().getAll("Categories");

                } else if ("get all Subcategories".equalsIgnoreCase(request)) {

                    object = new DAO().getAll("Subcategories");

                } else if ("get all Collectors".equalsIgnoreCase(request)) {

                    object = new DAO().getAll("Collectors");

                } else if ("get Collectors".equalsIgnoreCase(request)) {

                    long id = Long.parseLong(hashMap.get("id").toString());
                    object = new DAO().get(id, Collectors.class);

                } else if ("get all Configurations".equalsIgnoreCase(request)) {

                    object = new DAO().getAll("Configuration");

                } else if ("get Configurations".equalsIgnoreCase(request)) {

                    long id = Long.parseLong(hashMap.get("id").toString());
                    object = new DAO().get(id, Configuration.class);

                } else if ("hsql".equalsIgnoreCase(request)) {
                    
                    String hsql = hashMap.get("hsql").toString();
                    object = new DAO().hsql(hsql);
                    
                }

            }

            //resend remoteObject to client
            sendObjectClient(object);

        } catch (Exception e) {
            logger.error("server: " + e + " " + collector.getIp() + " port: " + collector.getPort());
            e.printStackTrace();
            sendErrorClient(e);
        } finally {
            closer();
        }

    } // end transferData

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
