package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private static final int DISTANCIA_PUNTOS = 10;
    private GoogleMap mMap;
    private FusedLocationProviderClient client;
    private LocationCallback callback;
    private List<LatLng> allPoints;
    private TelephonyManager telephonyManager;
    private Button bntIniciar;


    private double distanciaCoord(LatLng latLng1, LatLng latLng2) {

        Location locationA = new Location("Punto A");
        locationA.setLatitude(latLng1.latitude);
        locationA.setLongitude(latLng1.longitude);
        Location locationB = new Location("Punto B");
        locationB.setLatitude(latLng2.latitude);
        locationB.setLongitude(latLng2.longitude);

        return locationA.distanceTo(locationB);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        bntIniciar = findViewById(R.id.btnFuncionalidad);


        bntIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeTextButton();
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        client = LocationServices.getFusedLocationProviderClient(this);

        allPoints = new ArrayList<LatLng>();

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {

                LatLng received = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());

                if (allPoints.size() == 0 || distanciaCoord(allPoints.get(allPoints.size() - 1), received) > DISTANCIA_PUNTOS) {

                    allPoints.add(received);
                    int color = 0;
                    int level = getLevelSignal();

                    switch (level) {
                        case 0:
                            color = Color.BLACK;
                            break;
                        case 1:
                            color = Color.BLUE;
                            break;
                        case 2:
                            color = Color.GREEN;
                            break;
                        case 3:
                            color = Color.YELLOW;
                            break;
                        case 4:
                            color = Color.RED;
                            break;
                    }

                    mMap.addCircle(new CircleOptions()
                            .center(received)
                            .strokeColor(Color.BLACK)
                            .strokeWidth(4)
                            .fillColor(color)
                            .radius(4));
                }
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json)
        );

        showInitialPosition();

    }

    public void changeTextButton() {



        // Si no esta pulsado entonces hay que iniciar
        if (bntIniciar.getText().equals(getString(R.string.Iniciar))) {

            showPosition();
            bntIniciar.setText(getString(R.string.Parar));

        } else  {
            client.removeLocationUpdates(callback);
            bntIniciar.setText(getString(R.string.Iniciar));
        }
    }


    private void showInitialPosition() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 2);

            return;
        }
        mMap.setMyLocationEnabled(true);


        client.getLastLocation().addOnSuccessListener(location -> {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .zoom(20)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        });
    }

    private void showPosition() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 0);

            return;
        }



        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        client.requestLocationUpdates(locationRequest, callback, null);


    }


    private int getLevelSignal() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_PHONE_STATE
            }, 1);
            return -1;
        }

        List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();

        if (cellInfoList.size() > 1) {

            int valorMaximo = 0;

            for (CellInfo info: cellInfoList) {

                if (info instanceof CellInfoGsm) {

                    CellInfoGsm cellInfoGsm = (CellInfoGsm) info;
                    if (valorMaximo < cellInfoGsm.getCellSignalStrength().getLevel())
                        valorMaximo = cellInfoGsm.getCellSignalStrength().getLevel();

                } else if (info instanceof CellInfoWcdma) {

                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) info;
                    if (valorMaximo < cellInfoWcdma.getCellSignalStrength().getLevel())
                        valorMaximo = cellInfoWcdma.getCellSignalStrength().getLevel();

                } else if (info instanceof CellInfoLte) {

                    CellInfoLte cellInfoLte = (CellInfoLte) info;
                    if (valorMaximo < cellInfoLte.getCellSignalStrength().getLevel())
                        valorMaximo = cellInfoLte.getCellSignalStrength().getLevel();
                }

            }


            return valorMaximo;

        }

        return 0;

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    showPosition();
                } else {
                    Toast.makeText(this, "Antonio", Toast.LENGTH_SHORT).show();
                }
            }

            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getLevelSignal();
                } else {
                    Toast.makeText(this, "Acepta", Toast.LENGTH_SHORT).show();
                }
                break;

            case 2:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    showInitialPosition();
                } else {
                    Toast.makeText(this, "Acepta", Toast.LENGTH_SHORT).show();
                }
                break;
        }


    }

}