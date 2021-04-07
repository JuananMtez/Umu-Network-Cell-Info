package com.example.myapplication;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private File uriFichero;
    private static final int DISTANCIA_PUNTOS_MINIMA = 10;
    private static final int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
    private static final int SIGNAL_STRENGTH_POOR = 1;
    private static final int SIGNAL_STRENGTH_MODERATE = 2;
    private static final int SIGNAL_STRENGTH_GOOD = 3;
    private static final int SIGNAL_STRENGTH_GREAT = 4;
    private static final int STROKE_WIDTH = 4;
    private static final int RADIUS = 4;
    private static final float ZOOM_INICIAL = 20;
    private static final long FASTEST_INTERVAL = 500;
    private static final long INTERVAL = 1000;

    private GoogleMap mMap;
    private FusedLocationProviderClient client;
    private LocationCallback callback;
    private List<LatLng> points;
    private List<LatLng> cells;
    private List<JSONObject> cellsFound;



    private TelephonyManager telephonyManager;
    private Button bntIniciar;
    private Button btnSaveTowers;
    private String tecnologia;


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
        btnSaveTowers = findViewById(R.id.btnSaveTowers);
        Bundle extras = getIntent().getExtras();

        if (!extras.isEmpty()) {
            Object extra = extras.get("tecnologia");
            if (extra instanceof String) {
                tecnologia = new String((String) extra);
            }
        }

        bntIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeTextButton();

            }
        });

        btnSaveTowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAddTowersToFile();
                Snackbar.make(v, getString(R.string.Notification),
                        Snackbar.LENGTH_SHORT)
                        .setAction(getString(R.string.AbrirFichero), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {


                            }
                        })
                        .show();

            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        client = LocationServices.getFusedLocationProviderClient(this);

        points = new ArrayList<LatLng>();
        cells = new ArrayList<>();
        cellsFound = new ArrayList<>();
        uriFichero = this.getExternalFilesDir(null);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                codeCallback(locationResult);
            }
        };
    }

    private void codeCallback(LocationResult locationResult) {

        LatLng received = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());

        if (points.size() == 0 || distanciaCoord(points.get(points.size() - 1), received) > DISTANCIA_PUNTOS_MINIMA) {
            points.add(received);
            getCircle();
        }
    }

    private void getCircle() {


        int color = 0;
        int level = getLevelSignalAndCell();

        switch (level) {
            case SIGNAL_STRENGTH_NONE_OR_UNKNOWN:
                color = Color.BLACK;
                break;
            case SIGNAL_STRENGTH_POOR:
                color = Color.BLUE;
                break;
            case SIGNAL_STRENGTH_MODERATE:
                color = Color.GREEN;
                break;
            case SIGNAL_STRENGTH_GOOD:
                color = Color.YELLOW;
                break;
            case SIGNAL_STRENGTH_GREAT:
                color = Color.RED;
                break;
        }

        mMap.addCircle(new CircleOptions()
                .center(points.get(points.size() - 1))
                .strokeColor(Color.BLACK)
                .strokeWidth(STROKE_WIDTH)
                .fillColor(color)
                .radius(RADIUS));

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

        if (bntIniciar.getText().equals(getString(R.string.Iniciar)) || bntIniciar.getText().equals(getString(R.string.Reanudar)) ) {

            showPosition();
            bntIniciar.setText(getString(R.string.Parar));

        } else  {
            client.removeLocationUpdates(callback);
            bntIniciar.setText(getString(R.string.Reanudar));
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
                    .zoom(ZOOM_INICIAL)
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
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        client.requestLocationUpdates(locationRequest, callback, null);
    }

    private int getLevelSignalAndCell() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_PHONE_STATE
            }, 1);
            return -1;
        }

        List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
        int valorMaximo = 0;

        if (cellInfoList.size() > 0) {

            int mcc = 0;
            int mnc = 0;
            int cellId = 0;
            int lac = 0;

            for (CellInfo info: cellInfoList) {

                if (info instanceof CellInfoGsm && tecnologia.equals(getString(R.string.Gsm))) {

                    CellInfoGsm cellInfoGsm = (CellInfoGsm) info;
                    CellIdentityGsm id = cellInfoGsm.getCellIdentity();

                    mcc = id.getMcc();
                    mnc = id.getMnc();
                    cellId = id.getCid();
                    lac = id.getLac();

                    if (valorMaximo < cellInfoGsm.getCellSignalStrength().getLevel())
                        valorMaximo = cellInfoGsm.getCellSignalStrength().getLevel();

                } else if (info instanceof CellInfoWcdma && tecnologia.equals(getString(R.string.Wcdma))) {

                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) info;

                    CellIdentityWcdma id = cellInfoWcdma.getCellIdentity();

                    mcc = id.getMcc();
                    mnc = id.getMnc();
                    cellId = id.getCid();
                    lac = id.getLac();

                    if (valorMaximo < cellInfoWcdma.getCellSignalStrength().getLevel())
                        valorMaximo = cellInfoWcdma.getCellSignalStrength().getLevel();

                } else if (info instanceof CellInfoLte && tecnologia.equals(getString(R.string.Lte))) {

                    CellInfoLte cellInfoLte = (CellInfoLte) info;

                    CellIdentityLte id = cellInfoLte.getCellIdentity();

                    mcc = id.getMcc();
                    mnc = id.getMnc();
                    cellId = id.getCi();
                    lac = id.getTac();

                    if (valorMaximo < cellInfoLte.getCellSignalStrength().getLevel())
                        valorMaximo = cellInfoLte.getCellSignalStrength().getLevel();
                }

                if ( (mcc != 0 && mcc != 2147483647) || (mnc != 0  && mnc != 2147483647) || (cellId != 0 && cellId != 2147483647) || (lac != 0 && lac != 2147483647) )
                    getTowers(mcc, mnc, cellId, lac);
            }
        }

        return valorMaximo;
    }

    private void getTowers(int mcc, int mnc, int cellId, int lac) {

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "https://api.mylnikov.org/geolocation/cell?v=1.1&data=open" + "&mcc=" + mcc + "&mnc=" + mnc + "&lac=" + lac + "&cellid=" + cellId;
        System.out.println("La url es = " + url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject json = new JSONObject(response);

                    if (json.getString("result").equals("200")) {

                        JSONObject data = json.getJSONObject("data");
                        cellsFound.add(data);

                        LatLng latLng = new LatLng(Double.valueOf(data.getString("lat")), Double.valueOf(data.getString("lon")));

                        for (LatLng cell : cells) {
                            if (cell.longitude == latLng.longitude && cell.latitude == latLng.latitude)
                                return;
                        }

                        cells.add(latLng);

                        mMap.addMarker(new MarkerOptions()
                                .position(latLng));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.getMessage());
            }
        });

        queue.add(stringRequest);
    }


    public void onClickAddTowersToFile() {

        String texto = "";
        for (JSONObject json: cellsFound) {
            try {

                texto += "Latitud: " + json.getString("lat");
                texto += ", Longitud: " + json.getString("lon");
                texto += ", Rango: " + json.getString("range") + "\n";


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            StorageHelper.saveStringToFile("towers.json", texto, this);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //Preguntar al profesor si se puede hacer de otra forma para no repetir codigo
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case 0:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showPosition();
                } else {
                    Toast.makeText(this, getString(R.string.Permisos), Toast.LENGTH_SHORT).show();
                }
                break;

            case 1:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLevelSignalAndCell();
                } else {
                    Toast.makeText(this, getString(R.string.Permisos), Toast.LENGTH_SHORT).show();
                }
                break;

            case 2:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showInitialPosition();
                } else {
                    Toast.makeText(this, getString(R.string.Permisos), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}