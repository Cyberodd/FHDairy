           package com.hub.dairy;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public LatLng latLng;
    private Button btnDraw, btnClear;


    //Map Markers
    Polygon polygon = null;
    List<LatLng> latLngList = new ArrayList<>();
    List<Marker> markerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Assigning variables
        btnDraw = findViewById(R.id.btn_draw);
        btnClear = findViewById(R.id.btn_clear);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Instantiates a new Polygon object and adds points to define a rectangle
                PolygonOptions polygonOptions = new PolygonOptions()
                        .add(new LatLng(37.35, -122.0),
                                new LatLng(37.45, -122.0),
                                new LatLng(37.45, -122.2),
                                new LatLng(37.35, -122.2),
                                new LatLng(37.35, -122.0));
                // Get back the mutable Polygon
                Polygon polygon = mMap.addPolygon(polygonOptions);

            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Clear All
                if (polygon != null) polygon.remove();
                for (Marker marker : markerList) marker.remove();
                latLngList.clear();
                markerList.clear();
            }
        });
    }

    

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (latLng != null) {
            mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //Create marker options
                MarkerOptions markerOptions = new MarkerOptions().position(latLng);
                //Create marker
                Marker marker = mMap.addMarker(markerOptions);
                //Add latLng and marker
                latLngList.add(latLng);
                markerList.add(marker);
            }
        });
    }
}
