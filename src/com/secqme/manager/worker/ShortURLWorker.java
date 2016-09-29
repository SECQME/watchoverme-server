package com.secqme.manager.worker;

import com.secqme.CoreException;
import com.secqme.util.ShortURLUpdateService;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

/**
 * A Thread working of sending email in background thread
 * @author james
 */
public class ShortURLWorker implements Callable {

    private static Logger myLog = Logger.getLogger(ShortURLWorker.class);
    private Object valueObject;
    private ShortURLUpdateService shortURLUpdateService;
    private String longURL;

    public ShortURLWorker(ShortURLUpdateService shortURLUpdateSrv, Object valueObject, String longURL) {
        this.valueObject = valueObject;
        this.shortURLUpdateService = shortURLUpdateSrv;
        this.longURL = longURL;
    }


    public Boolean call() {
        boolean updateSuccess = false;
        try {
            shortURLUpdateService.updateShortURL(valueObject, longURL);
            updateSuccess = true;
        } catch (CoreException ex) {
            myLog.error("Error->" + ex.getMessage(), ex);
        }
        return updateSuccess;
    }

}
