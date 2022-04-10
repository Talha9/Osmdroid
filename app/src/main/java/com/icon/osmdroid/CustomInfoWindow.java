package com.icon.osmdroid;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.views.MapView;

import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

public class CustomInfoWindow extends InfoWindow {
    String TAG="logsCheckOsm";
    
    public CustomInfoWindow(View v, MapView mapView) {
        super(v, mapView);
        Log.d(TAG, "CustomInfoWindow: ");
    }

    @Override
    public void onOpen(Object item) {
        Log.d(TAG, "onOpen: ");
    }

    @Override
    public void onClose() {
        Log.d(TAG, "onClose: ");
    }
}





