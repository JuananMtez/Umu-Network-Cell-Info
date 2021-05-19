package com.example.myapplication;

import android.Manifest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int DISTANCIA_PUNTOS_MINIMA = 30;
    private static final int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
    private static final int SIGNAL_STRENGTH_POOR = 1;
    private static final int SIGNAL_STRENGTH_MODERATE = 2;
    private static final int SIGNAL_STRENGTH_GOOD = 3;
    private static final int SIGNAL_STRENGTH_GREAT = 4;
    private static final int STROKE_WIDTH = 6;
    private static final int RADIUS = 10;
    private static final float ZOOM_INICIAL = 15.5f;
    private static final long FASTEST_INTERVAL = 500;
    private static final long INTERVAL = 1000;

    private GoogleMap mMap;
    private FusedLocationProviderClient client;
    private LocationCallback callback;

    private LocationCallback positionInitialCallback;

    private List<LatLng> points;
    private List<Tower> towers;

    private TelephonyManager telephonyManager;
    private Button btnIniciar;
    private Button btnSaveTowers;

    private String tecnologia;
    private boolean corriendo;
    private boolean zoomInicial;
    private String fileName;
    private int numCamino;



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
        btnIniciar = findViewById(R.id.btnFuncionalidad);
        btnIniciar.setEnabled(false);
                btnSaveTowers = findViewById(R.id.btnSaveTowers);
        btnSaveTowers.setEnabled(false);
        TextView textoTecnologia = findViewById(R.id.textoTecnologia);




        Bundle extras = getIntent().getExtras();

        if (!extras.isEmpty()) {

            if (extras.get("tecnologia") instanceof String) {

                tecnologia = (String) extras.get("tecnologia");
                textoTecnologia.setText(tecnologia);
            }
        }



        btnIniciar.setOnClickListener(this::changeTextButton);

        btnSaveTowers.setOnClickListener(v -> {
            onClickAddTowersToFile();
            Snackbar.make(v, getString(R.string.Notification),
                    Snackbar.LENGTH_SHORT)
                    .show();
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        client = LocationServices.getFusedLocationProviderClient(this);

        points = new ArrayList<>();
        towers = new ArrayList<>();
        corriendo = false;

        fileName = "datos";
        if (tecnologia.equals(getString(R.string.Gsm)))
            fileName += getString(R.string.Gsm);
        else if (tecnologia.equals(getString(R.string.Wcdma)))
            fileName += getString(R.string.Wcdma);
        else if (tecnologia.equals(getString(R.string.Lte)))
            fileName += getString(R.string.Lte);

        fileName += ".txt";

        try {
            StorageHelper.saveStringToFile(fileName, "Inicio del recorrido\n\n", this, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        numCamino = 1;


        zoomInicial = false;


        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult != null)
                    codeCallback(locationResult);
            }
        };

        positionInitialCallback = new LocationCallback() {
           @Override
           public void onLocationResult(LocationResult locationResult) {

               if (locationResult != null) {
                   CameraPosition cameraPosition = new CameraPosition.Builder()
                           .target(new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()))
                           .zoom(ZOOM_INICIAL)
                           .build();
                   mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                   client.removeLocationUpdates(positionInitialCallback);
               }
           }
       };
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (btnIniciar.getText().equals(getString(R.string.Parar)))
            client.removeLocationUpdates(callback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        if (corriendo && btnIniciar.getText().equals(getString(R.string.Parar)))
            showPosition();

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try {
            StorageHelper.saveStringToFile(fileName, "Fin del recorrido\n\n", this, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
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



        mMap.setOnCameraMoveListener(() -> {
            if (!zoomInicial && mMap.getCameraPosition().zoom == ZOOM_INICIAL) {
                btnIniciar.setEnabled(true);
                btnSaveTowers.setEnabled(true);
                zoomInicial = true;
            }
        });


        showInitialPosition();
    }

    public void changeTextButton(View v) {

        if (btnIniciar.getText().equals(getString(R.string.Iniciar)) || btnIniciar.getText().equals(getString(R.string.Reanudar)) ) {


            try {
                StorageHelper.saveStringToFile(fileName, "Camino " + numCamino + ":\n\n", this, true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            showPosition();
            btnIniciar.setText(getString(R.string.Parar));

            Snackbar.make(v, getString(R.string.Camino) + " " + numCamino + " " + getString(R.string.Comenzando),
                    Snackbar.LENGTH_SHORT)
                    .show();

            numCamino++;


        } else  {
            client.removeLocationUpdates(callback);
            btnIniciar.setText(getString(R.string.Reanudar));
            Snackbar.make(v, getString(R.string.Camino) + " " + (numCamino - 1) + " " + getString(R.string.Terminado),
                    Snackbar.LENGTH_SHORT)
                    .show();
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

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(INTERVAL);
            locationRequest.setFastestInterval(FASTEST_INTERVAL);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            client.requestLocationUpdates(locationRequest, positionInitialCallback, null);


        });

        corriendo = true;

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


        int level = 0;             // Signal level as an int from 0..4

        int asuLevel = 0;          /* Signal level as an asu value, asu is calculated based on 3GPP RSRP
                                   for GSM, between 0..31, 99 is unknown
                                   for WCDMA, between 0..31, 99 is unknown
                                   for LTE, between 0..97, 99 is unknown
                                   for CDMA, between 0..97, 99 is unknown */

        int dbm = 0;                // Signal level as dbm



        for (CellInfo info: cellInfoList) {

            if (info.isRegistered()) {

                Tower tower = null;
                if (info instanceof CellInfoGsm && tecnologia.equals(getString(R.string.Gsm))) {

                    CellInfoGsm cellInfoGsm = (CellInfoGsm) info;
                    CellIdentityGsm id = cellInfoGsm.getCellIdentity();


                    tower = new Tower(id.getMcc(), id.getMnc(), id.getLac(), id.getCid(), getString(R.string.Gsm));

                    if (level < cellInfoGsm.getCellSignalStrength().getLevel()) {

                        level = cellInfoGsm.getCellSignalStrength().getLevel();
                        dbm = cellInfoGsm.getCellSignalStrength().getDbm();
                        asuLevel = cellInfoGsm.getCellSignalStrength().getAsuLevel();
                    }

                } else if (info instanceof CellInfoWcdma && tecnologia.equals(getString(R.string.Wcdma))) {

                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) info;

                    CellIdentityWcdma id = cellInfoWcdma.getCellIdentity();


                    tower = new Tower(id.getMcc(), id.getMnc(), id.getLac(), id.getCid(), getString(R.string.Wcdma));


                    if (level < cellInfoWcdma.getCellSignalStrength().getLevel()) {

                        level = cellInfoWcdma.getCellSignalStrength().getLevel();
                        dbm = cellInfoWcdma.getCellSignalStrength().getDbm();
                        asuLevel = cellInfoWcdma.getCellSignalStrength().getAsuLevel();
                    }

                } else if (info instanceof CellInfoLte && tecnologia.equals(getString(R.string.Lte))) {

                    CellInfoLte cellInfoLte = (CellInfoLte) info;
                    CellIdentityLte id = cellInfoLte.getCellIdentity();

                    tower = new Tower(id.getMcc(), id.getMnc(), id.getTac(), id.getCi(), getString(R.string.Lte));

                    if (level < cellInfoLte.getCellSignalStrength().getLevel()) {

                        level = cellInfoLte.getCellSignalStrength().getLevel();
                        dbm = cellInfoLte.getCellSignalStrength().getDbm();
                        asuLevel = cellInfoLte.getCellSignalStrength().getAsuLevel();
                    }
                }

                if (tower != null)
                    getTower(tower);
            }
        }

        String texto = "- Lat: " + points.get(points.size() - 1).latitude + ", Lon: " + points.get(points.size() - 1).longitude + " {\n";

        texto += "\t Level: " + level + "\n";
        texto += "\t asuLevel: " + asuLevel + "\n";
        texto += "\t Dbm: " + dbm + "\n}\n\n";


        try {
            StorageHelper.saveStringToFile(fileName, texto, this, true);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return level;
    }

    private void getTower(Tower tower) {

        for (Tower t : towers) {
            if (t.getMcc() == tower.getMcc() && t.getMnc() == tower.getMnc()
                    && t.getLac() == tower.getLac() && t.getCellId() == tower.getCellId())
                return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "https://api.mylnikov.org/geolocation/cell?v=1.1&data=open" + "&mcc=" + tower.getMcc() + "&mnc=" + tower.getMnc()
                + "&lac=" + tower.getLac() + "&cellid=" + tower.getCellId();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject json = new JSONObject(response);

                if (json.getString("result").equals("200")) {

                    JSONObject data = json.getJSONObject("data");

                    tower.setLatLng(new LatLng(Double.parseDouble(data.getString("lat")), Double.parseDouble(data.getString("lon"))));
                    tower.setRange(data.getString("range"));
                    towers.add(tower);

                    mMap.addMarker(new MarkerOptions()
                            .position(tower.getLatLng()));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> System.out.println(error.getMessage()));

        queue.add(stringRequest);
    }


    public void onClickAddTowersToFile() {

        StringBuilder texto = new StringBuilder();


        for (Tower tower: towers) {

            texto.append(tower.getTecnologia()).append(" -- Cell_Id: ").append(tower.getCellId()).append(" {\n\n");

            texto.append("\tMcc: ").append(tower.getMcc());

            texto.append("\n\tMnc: ").append(tower.getMnc());

            texto.append("\n\tLac: ").append(tower.getLac());

            texto.append("\n\tLatitud: ").append(tower.getLatLng().latitude);

            texto.append("\n\tLongitud: ").append(tower.getLatLng().longitude);

            texto.append("\n\tRango: ").append(tower.getRange()).append("\n}\n\n");


        }

        try {
            StorageHelper.saveStringToFile("towers.json", texto.toString(), this, false);

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