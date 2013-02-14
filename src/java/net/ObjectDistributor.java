package net;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 *
 * @author skuarch
 */
class ObjectDistributor {

    private static final Logger logger = Logger.getLogger(ObjectDistributor.class);
    private InputStream myInputStream = null;
    private OutputStream myOutputStream = null;    
    private ObjectInputStream myObjectInputStream = null;    

    //==========================================================================
    public ObjectDistributor(InputStream myInputStream, OutputStream myOutputStream) {
        this.myInputStream = myInputStream;
        this.myOutputStream = myOutputStream;        
    } // end ObjectDistributor

    //==========================================================================
    public void assingDispatcher() throws IOException, ClassNotFoundException {

        HashMap hashMap = null;
        Object objectClient = receiveObject();
        String type = null;

        if (objectClient instanceof HashMap) {

            hashMap = (HashMap) objectClient;
            type = hashMap.get("type").toString();

            if ("objectRequester".equalsIgnoreCase(type)) {
                DispatcherObject dispatcherObject = new DispatcherObject(hashMap, myInputStream, myOutputStream);
                dispatcherObject.transferData();
            } else {                
                DispatcherLinker dispatcherLinker = new DispatcherLinker(hashMap, myInputStream, myOutputStream);
                dispatcherLinker.transferData();
            }

        }

    } // end assingDispatcher

    //==========================================================================
    /**
     * receive objectClient from client.
     *
     * @return Object
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Object receiveObject() throws IOException, ClassNotFoundException {

        Object object = null;

        try {

            myObjectInputStream = new ObjectInputStream(myInputStream);

            while (true) {
                object = myObjectInputStream.readObject();
                if (object != null) {
                    break;
                }
            }

        } catch (ClassNotFoundException cnfe) {
            logger.error(cnfe);
            throw cnfe;
        } catch (IOException ioe) {
            logger.error(ioe);
            throw ioe;
        }

        return object;
    } // end receiveObject
    
} // end class
