package com.secqme.web.jsf.mbean;

import com.secqme.domain.model.TrackingLogVO;
import org.primefaces.model.map.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jameskhoo
 */
public class MapVO implements Serializable {
    
    private static final String MAP_STARTING_URL = "https://secq.me/mainportal/images/mapstarting.png";
    private static final String MAP_CURRENT_URL = "https://secq.me/mainportal/images/mapcurrent.png";

    private String centerLocation = null;
    private List<TrackingLogVO> trackingLogVOList;
    private MapModel mapModel = null;
    private Integer zoomLevel = 0;
    private Integer defaultZoomLevel = 15;

    public MapVO() {
        // Empty Constructor
    }

    public MapVO(List<TrackingLogVO> trackingVOList, MapType mapType) {
        trackingLogVOList = trackingVOList;
        switch (mapType) {
            case Simple:
                initSimpleMap(trackingVOList);
                break;
            case Complex:
                initComplexMap(trackingVOList);
                break;

        }
    }

    private void initSimpleMap(List<TrackingLogVO> trackingVOList) {
        mapModel = new DefaultMapModel();
        Polyline ovrallPolyline = new Polyline();

        TrackingLogVO currentLogVO = trackingVOList.get(0);
        TrackingLogVO startingLogVO = trackingVOList.get(trackingVOList.size() - 1);

        LatLng latestLatLng = new LatLng(currentLogVO.getLatitude(), currentLogVO.getLongitude());
        LatLng startingLatLng = new LatLng(startingLogVO.getLatitude(), startingLogVO.getLongitude());

        ovrallPolyline.setStrokeWeight(3);
        ovrallPolyline.setStrokeColor("#00FFFF");
        ovrallPolyline.setStrokeOpacity(0.4);
        ovrallPolyline.getPaths().add(new LatLng(currentLogVO.getLatitude(), currentLogVO.getLongitude()));
        ovrallPolyline.getPaths().add(new LatLng(startingLogVO.getLatitude(), startingLogVO.getLongitude()));
        mapModel.addOverlay(ovrallPolyline);
        addMapMarker(latestLatLng, currentLogVO, startingLatLng, startingLogVO);

        List<TrackingLogVO> newTrackingVOList = new ArrayList<TrackingLogVO>();
        newTrackingVOList.add(startingLogVO);
        newTrackingVOList.add(currentLogVO);
        
        initCenterZoom(newTrackingVOList);
    }

    private void addMapMarker(LatLng latestLatLng, TrackingLogVO currentLogVO, LatLng startingLatLng, TrackingLogVO startingLogVO) {
        if (startingLatLng.getLat() != latestLatLng.getLat() && startingLatLng.getLng() != latestLatLng.getLng()) {
            mapModel.addOverlay(new Marker(startingLatLng, "Starting Location (" + startingLogVO.getLocString() + ")", startingLogVO, MAP_STARTING_URL));
        }
        mapModel.addOverlay(new Marker(latestLatLng, "Current Location (" + currentLogVO.getLocString() + ")", currentLogVO, MAP_CURRENT_URL));
    }


    public String getGoogleMapPolylines() {
        StringBuffer strBuffer = new StringBuffer();
        int index = 0;
        for(TrackingLogVO logVO: trackingLogVOList) {
            strBuffer.append("new google.maps.LatLng(" + logVO.getLocString() + ")");
            if(index < (trackingLogVOList.size() - 1)) {
                strBuffer.append(',');
            }
        }
        return strBuffer.toString();
    }

    private void initComplexMap(List<TrackingLogVO> trackingVOList) {
        mapModel = new DefaultMapModel();
        Polyline ovrallPolyline = new Polyline();
        Polyline emergencyPolyline = null;

        TrackingLogVO latestTrackingLogVO = trackingVOList.get(0);
        TrackingLogVO startingTrackingLogVO = trackingVOList.get(trackingVOList.size() - 1);
        LatLng latestLatLng = new LatLng(latestTrackingLogVO.getLatitude(), latestTrackingLogVO.getLongitude());
        LatLng startingLatLng = new LatLng(startingTrackingLogVO.getLatitude(), startingTrackingLogVO.getLongitude());
        // use the latest trackingVO as center Location
        Circle circle1;
        if(latestTrackingLogVO.getAccuracy() == null ) {
         circle1  = new Circle(latestLatLng, 50.00);
        } else {
            circle1 = new Circle(latestLatLng, latestTrackingLogVO.getAccuracy());
        }
        circle1.setStrokeColor("#d93c3c");  
        circle1.setFillColor("#d93c3c");  
        circle1.setFillOpacity(0.5);

        for (TrackingLogVO logVO : trackingLogVOList) {
            if (logVO.getOtherInfo() != null && logVO.getOtherInfo().indexOf(TrackingLogVO.EMERGENCY_FLAG) >= 0) {
                if (emergencyPolyline == null) {
                    emergencyPolyline = new Polyline();
                    emergencyPolyline.setStrokeWeight(5);
                    emergencyPolyline.setStrokeColor("#FF0000");
                    emergencyPolyline.setStrokeOpacity(0.75);
                }
                emergencyPolyline.getPaths().add(new LatLng(logVO.getLatitude(), logVO.getLongitude()));
            }

            ovrallPolyline.getPaths().add(new LatLng(logVO.getLatitude(), logVO.getLongitude()));
        }

        ovrallPolyline.setStrokeWeight(5);
        ovrallPolyline.setStrokeColor("#0000FF");
        ovrallPolyline.setStrokeOpacity(0.5);

        mapModel.addOverlay(ovrallPolyline);
        if (emergencyPolyline != null) {
            mapModel.addOverlay(emergencyPolyline);
        }
        addMapMarker(latestLatLng, latestTrackingLogVO, startingLatLng, startingTrackingLogVO);
        
        mapModel.addOverlay(circle1);
        this.zoomLevel = defaultZoomLevel;
        this.centerLocation = latestTrackingLogVO.getLocString();

    }

    private void initCenterZoom(List<TrackingLogVO> trackingList) {
        double minLng = trackingList.get(0).getLongitude();
        double minLat = trackingList.get(0).getLatitude();
        double maxLat = trackingList.get(0).getLatitude();
        double maxLng = trackingList.get(0).getLongitude();

        for (TrackingLogVO logVO : trackingList) {
            if (logVO.getLatitude() <= minLat) {
                minLat = logVO.getLatitude();
            }

            if (logVO.getLatitude() >= maxLat) {
                maxLat = logVO.getLatitude();
            }

            if (logVO.getLongitude() <= minLng) {
                minLng = logVO.getLongitude();
            }

            if (logVO.getLongitude() >= maxLng) {
                maxLng = logVO.getLongitude();
            }
        }

        double centerLat = (minLat + maxLat) / 2;
        double centerLng = (minLng + maxLng) / 2;

        centerLocation = "" + centerLat + ", " + centerLng;
        zoomLevel = calculateZoom(minLng, maxLng, minLat, maxLat, 500, 400);


    }

    public Integer getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(Integer zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    public MapModel getMapModel() {
        return mapModel;
    }

    public void setMapModel(MapModel mapModel) {
        this.mapModel = mapModel;
    }

    public String getCenterLocation() {
        return centerLocation;
    }

    private Long[] fromLatLngToPxl(Double lat, Double lng, int zoom) {

        Long[] result = new Long[2];

        Long[] pixelRange = new Long[19];

        Long pixels = 256l;
        Long origin;

        Long[] pixelsPerLonDegree = new Long[19];
        Long[] pixelsPerLonRadian = new Long[19];

        Long o = 0l;

        for (Integer z = 0; z <= 18; z++) {
            origin = pixels / 2;
            pixelsPerLonDegree[z] = pixels / 360;
            pixelsPerLonRadian[z] = ((Double) (pixels / (2 * Math.PI))).longValue();
            //pixelOrigo[z][0] = origin;
            //pixelOrigo[z][1] = origin;
            if (z == zoom) {
                o = origin;
                break;
            }
            pixelRange[z] = pixels;
            pixels *= 2;
        }

        result[0] = Math.round(o.doubleValue() + lng * pixelsPerLonDegree[zoom].doubleValue());
        Double siny = Math.sin(Math.toRadians(lat));
        siny = (siny < -0.9999) ? -0.9999 : (siny > 0.9999) ? 0.9999 : siny;
        result[1] = Math.round(o + 0.5 * Math.log((1 + siny) / (1 - siny)) * -pixelsPerLonRadian[zoom]);
        return result;
    }

    private Integer calculateZoom(Double minLng, Double maxLng, Double minLat, Double maxLat, int mapWidth, int mapHeight) {

        Long[] pixelRange = new Long[19];
        Long pixels = 256l;

        for (Integer z = 0; z <= 18; z++) {
            pixelRange[z] = pixels;
            pixels *= 2;
        }

        
        for (Integer z = 18; z > 0; z--) {
            Long[] btmLeftPxl = fromLatLngToPxl(minLng, minLat, z);
            Long[] topRghtPxl = fromLatLngToPxl(maxLng, maxLat, z);
            if (btmLeftPxl[0] > topRghtPxl[0]) {
                btmLeftPxl[0] -= pixelRange[z];
            }
            if (Math.abs(topRghtPxl[0] - btmLeftPxl[0]) <= mapWidth && Math.abs(topRghtPxl[1] - btmLeftPxl[1]) <= mapHeight) {
                return z;
            }
        }
        return 0;
    }
}
