/**
 * Created by edward on 02/06/2015.
 */
var map;
var timer;
var startingMarker;
var prevData;

function init() {
    google.maps.Map.prototype.markers = new Array();
    google.maps.Map.prototype.getMarkers = function() {
        return this.markers
    };
    google.maps.Map.prototype.clearMarkers = function() {
        for(var i=0; i<this.markers.length; i++){
            this.markers[i].setMap(null);
        }
        this.markers = new Array();
    };
    google.maps.Marker.prototype._setMap = google.maps.Marker.prototype.setMap;
    google.maps.Marker.prototype.setMap = function(map) {
        if (map) {
            map.markers[map.markers.length] = this;
        }
        this._setMap(map);
    }

    google.maps.Map.prototype.rectangles = new Array();
    google.maps.Map.prototype.getRectangles = function() {
        return this.rectangles
    };
    google.maps.Map.prototype.clearRectangles = function() {
        for(var i=0; i<this.rectangles.length; i++){
            this.rectangles[i].setMap(null);
        }
        this.rectangles = new Array();
    };
    google.maps.Rectangle.prototype._setMap = google.maps.Rectangle.prototype.setMap;
    google.maps.Rectangle.prototype.setMap = function(map) {
        if (map) {
            map.rectangles[map.rectangles.length] = this;
        }
        this._setMap(map);
    }

    google.maps.Map.prototype.circles = new Array();
    google.maps.Map.prototype.getCircles = function() {
        return this.circles
    };
    google.maps.Map.prototype.clearCircles = function() {
        for(var i=0; i<this.circles.length; i++){
            this.circles[i].setMap(null);
        }
        this.circles = new Array();
    };
    google.maps.Circle.prototype._setMap = google.maps.Circle.prototype.setMap;
    google.maps.Circle.prototype.setMap = function(map) {
        if (map) {
            map.circles[map.circles.length] = this;
        }
        this._setMap(map);
    }

    google.maps.Map.prototype.polylines = new Array();
    google.maps.Map.prototype.getPolylines = function() {
        return this.polylines
    };
    google.maps.Map.prototype.clearPolylines = function() {
        for(var i=0; i<this.polylines.length; i++){
            this.polylines[i].setMap(null);
        }
        this.polylines = new Array();
    };
    google.maps.Polyline.prototype._setMap = google.maps.Polyline.prototype.setMap;
    google.maps.Polyline.prototype.setMap = function(map) {
        if (map) {
            map.polylines[map.polylines.length] = this;
        }
        this._setMap(map);
    }

    google.maps.Map.prototype.clearOverlays = function() {
        this.clearMarkers();
        this.clearRectangles();
        this.clearCircles();
        this.clearPolylines();
    };

    var mapOptions = {
        center: mapCenterLocation,
        zoom: zoomLevel,
        zoomControl: true,
        zoomControlOptions: {
            style: google.maps.ZoomControlStyle.SMALL,
            position: google.maps.ControlPosition.RIGHT_TOP
        },
        mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    map = new google.maps.Map(document.getElementById("map-canvas2"), mapOptions);

    connect(API_BASE_WS + "/ws/v2/events/" + trackingPin);
}

var ws = null;

function connect(target) {
    if ('WebSocket' in window) {
        ws = new WebSocket(target);
    } else if ('MozWebSocket' in window) {
        ws = new MozWebSocket(target);
    } else {
        alert('WebSocket is not supported by this browser.');
        return;
    }

    ws.onopen = function () {
        // console.log('Info: WebSocket connection opened.');
        echo();
        timer = setInterval(echo, 3000);
    };
    ws.onmessage = function (event) {
        // console.log(event.data);
        refresh(JSON.parse(event.data));
    };
    ws.onclose = function (event) {
        clearInterval(timer);
        // console.log('Info: WebSocket connection closed, Code: ' + event.code + (event.reason == "" ? "" : ", Reason: " + event.reason));
    };
}

function disconnect() {
    if (ws != null) {
        ws.close();
        ws = null;
    }
}

function echo() {
    if (ws != null) {
        try {
            ws.send('');
        } catch (ex) {
            disconnect();
        }
    } else {
        // console.log('WebSocket connection not established, please connect.');
    }
}

function refresh(data) {
    $('#eventStatus').text(data.eventStatus);
    if (data.eventStatus === 'END') {
        disconnect();
    }

    if (typeof(data.confirmSafetyTime) === 'undefined') {
        $('#confirmSafety').text('NO');
    } else {
        $('#confirmSafety').text(moment(data.confirmSafetyTime).tz(data.timeZone).format('MMM-DD hh:mm A'));
    }

    $('#eventNotes').text(data.message);

    var currentTrackingLog =  data.trackingLogArray[0];
    $('#currentTrackingTime').text(moment(currentTrackingLog.timeStamp).tz(data.timeZone).format('MMM-DD hh:mm A'));
    currentLocation = new google.maps.LatLng(currentTrackingLog.latitude, currentTrackingLog.longitude);

    var firstTime = false;
    if (typeof(startingMarker) === 'undefined') {
        var startingTrackingLog =  data.trackingLogArray[data.trackingLogArray.length - 1];
        $('#startTrackingTime').text(moment(startingTrackingLog.timeStamp).tz(data.timeZone).format('MMM-DD hh:mm A'));
        startingLocation = new google.maps.LatLng(startingTrackingLog.latitude, startingTrackingLog.longitude);

        startingMarker = new google.maps.Marker({
            map: map,
            draggable: false,
            icon: CONTEXT_PATH + "/images/mapstarting.png",
            position: startingLocation
        });

        firstTime = true;
    }

    // If the trackingLog is changed
    if (typeof(prevData) === 'undefined' || prevData.trackingLogArray.length != data.trackingLogArray.length) {
        map.clearOverlays();

        startingMarker.setMap(map);

        var trackingPathArray = [];
        for (var i = 0; i < data.trackingLogArray.length; i++) {
            trackingPathArray.push(new google.maps.LatLng(data.trackingLogArray[i].latitude, data.trackingLogArray[i].longitude));
        }

        var trackingPath = new google.maps.Polyline({
            path: trackingPathArray,
            strokeColor: "#FF0000",
            strokeOpacity: .75,
            strokeWeight: 5,
            map: map
        });

        var currentCircle = new google.maps.Circle({
            center: currentLocation,
            strokeColor: "#d93c3c",
            fillColor: "#d93c3c",
            fillOpacity: 0.4,
            radius: currentTrackingLog.accuracy,
            map: map
        });

        if (firstTime) {
            var currentMarker = new google.maps.Marker({
                map: map,
                draggable: false,
                animation: google.maps.Animation.DROP,
                icon: CONTEXT_PATH + "/images/mapcurrent.png",
                position: currentLocation
            });
        } else {
            var currentMarker = new google.maps.Marker({
                map: map,
                draggable: false,
                icon: CONTEXT_PATH + "/images/mapcurrent.png",
                position: currentLocation
            });
        }

        map.panTo(currentLocation);
    }

    prevData = data;
}

google.maps.event.addDomListener(window, 'load', init);