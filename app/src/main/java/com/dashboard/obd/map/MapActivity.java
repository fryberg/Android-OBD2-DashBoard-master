package com.dashboard.obd.map;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;


import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.dashboard.obd.map.GPSLocationListener;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.dashboard.obd.R;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.location.FilteringMode;
import com.yandex.mapkit.location.Location;
import com.yandex.mapkit.location.LocationListener;
import com.yandex.mapkit.location.LocationManager;
import com.yandex.mapkit.location.LocationStatus;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CompositeIcon;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.RotationType;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.image.ImageProvider;

/**
 * YandexMapKitSample.java: Main activity
 *
 * This file is a part of the Yandex Map Kit.
 *
 * Version for Android © 2012 YANDEX
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://legal.yandex.ru/mapkit/
 *
 */
public class MapActivity extends AppCompatActivity implements UserLocationObjectListener {


    private static final String TAG ="111" ;
    private final String MAPKIT_API_KEY = "408af083-4acd-4a9e-a51d-d0c62a8a9b8a";
    private  Point TARGET_LOCATION = new Point(59.945933, 30.320045);
    private MapView mapView;
    private UserLocationLayer userLocationLayer;
    private GPSLocationListener local;

    private static final double DESIRED_ACCURACY = 0;
    private static final long MINIMAL_TIME = 0;
    private static final double MINIMAL_DISTANCE = 50;
    private static final boolean USE_IN_BACKGROUND = false;
    public static final int COMFORTABLE_ZOOM_LEVEL = 18;

    //private MapView mapView;
    private CoordinatorLayout rootCoordinatorLayout;
    private LocationManager locationManager;
    private LocationListener myLocationListener;
    private Point myLocation;

    public void onCreate(Bundle savedInstanceState) {
        /**
         * Set the api key before calling initialize on MapKitFactory.
         * It is recommended to set api key in the Application.onCreate method,
         * but here we do it in each activity to make examples isolated.
         */
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        /**
         * Initialize the library to load required native libraries.
         * It is recommended to initialize the MapKit library in the Activity.onCreate method
         * Initializing in the Application.onCreate method may lead to extra calls and increased battery use.
         */

       /* LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);;
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (enabled) {

            final Context mainContext = this;
            new Thread(() -> GPSLocationListener.SetUpLocationListener(mainContext)).start();
            if (GPSLocationListener.imHere!=null)
            TARGET_LOCATION = new Point(GPSLocationListener.imHere.getLatitude(), GPSLocationListener.imHere.getLongitude());
        }else {
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }*/
        MapKitFactory.initialize(this);
        // Now MapView can be created.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        mapView = findViewById(R.id.mapview);

        // And to show what can be done with it, we move the camera to the center of Saint Petersburg.





        //rootCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.root_coordinator);

        locationManager = MapKitFactory.getInstance().createLocationManager();
        myLocationListener = new LocationListener() {
            @Override
            public void onLocationUpdated(Location location) {
                if (myLocation == null) {
                    moveCamera(location.getPosition(), COMFORTABLE_ZOOM_LEVEL);
                }
                myLocation = location.getPosition();
                Log.w(TAG, "my location - " + myLocation.getLatitude() + "," + myLocation.getLongitude());
            }

            @Override
            public void onLocationStatusUpdated(LocationStatus locationStatus) {
                if (locationStatus == LocationStatus.NOT_AVAILABLE) {
                    //Snackbar.make(rootCoordinatorLayout, R.string.error_cant_get_my_location, Snackbar.LENGTH_LONG).show();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();

        subscribeToLocationUpdate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MapKitFactory.getInstance().onStop();
        locationManager.unsubscribe(myLocationListener);
        mapView.onStop();
    }

    public void onFabCurrentLocationClick(View view) {
        if (myLocation == null) {
            //Snackbar.make(rootCoordinatorLayout, R.string.coordinates_are_not_yet_determinate, Snackbar.LENGTH_SHORT).show();
            return;
        }

        moveCamera(myLocation, COMFORTABLE_ZOOM_LEVEL);
    }

    private void subscribeToLocationUpdate() {
        if (locationManager != null && myLocationListener != null) {
            locationManager.subscribeForLocationUpdates(DESIRED_ACCURACY, MINIMAL_TIME, MINIMAL_DISTANCE, USE_IN_BACKGROUND, FilteringMode.OFF, myLocationListener);
        }
    }

    private void moveCamera(Point point, float zoom) {
        mapView.getMap().move(
                new CameraPosition(point, zoom, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 1),
                null);
    }
     @Override
    public void onObjectAdded(UserLocationView userLocationView) {
        userLocationLayer.setAnchor(
                new PointF((float)(mapView.getWidth() * 0.5), (float)(mapView.getHeight() * 0.5)),
                new PointF((float)(mapView.getWidth() * 0.5), (float)(mapView.getHeight() * 0.83)));

        userLocationView.getArrow().setIcon(ImageProvider.fromResource(
                this, R.drawable.user_arrow));

        CompositeIcon pinIcon = userLocationView.getPin().useCompositeIcon();

        pinIcon.setIcon(
                "icon",
                ImageProvider.fromResource(this, R.drawable.icon),
                new IconStyle().setAnchor(new PointF(0f, 0f))
                        .setRotationType(RotationType.ROTATE)
                        .setZIndex(0f)
                        .setScale(1f)
        );

        pinIcon.setIcon(
                "pin",
                ImageProvider.fromResource(this, R.drawable.search_result),
                new IconStyle().setAnchor(new PointF(0.5f, 0.5f))
                        .setRotationType(RotationType.ROTATE)
                        .setZIndex(1f)
                        .setScale(0.5f)
        );

        userLocationView.getAccuracyCircle().setFillColor(Color.BLUE & 0x99ffffff);
    }

    @Override
    public void onObjectRemoved(UserLocationView view) {
    }

    @Override
    public void onObjectUpdated(UserLocationView view, ObjectEvent event) {
    }
}
