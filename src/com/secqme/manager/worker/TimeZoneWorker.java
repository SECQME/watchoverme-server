package com.secqme.manager.worker;

import com.secqme.CoreException;
import com.secqme.util.TimeZoneUpdateService;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

/**
 * A Thread working of sending email in background thread
 * @author james
 */
public class TimeZoneWorker implements Callable {

    private static Logger myLog = Logger.getLogger(TimeZoneWorker.class);
    private TimeZoneUpdateService updateServiceMgr;
    private Double lat;
    private Double lng;
    private Object valueObject;

    public TimeZoneWorker(TimeZoneUpdateService timeZoneUpdateMgr, Object valueObject, Double lat, Double lng) {
        this.lat = lat;
        this.lng = lng;
        this.updateServiceMgr = timeZoneUpdateMgr;
        this.valueObject = valueObject;
    }


    public Boolean call() {
        boolean updateSuccess = false;
        try {
            updateServiceMgr.updateTimeZone(valueObject, lat, lng);
            updateSuccess = true;
        } catch (CoreException ex) {
            myLog.error("Error->" + ex.getMessage(), ex);
        }
        return updateSuccess;
    }

}
