package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class ActividadClase3 extends AppCompatActivity {


    private TelephonyManager telephonyManager;
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad_clase3);

        textView = findViewById(R.id.texto);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    }


    private void currentNetworkInfo() {

        String text = "";
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_PHONE_STATE
            }, 0);

            return;
        }
        text += telephonyManager.getVoiceNetworkType() + "\n";

        text += telephonyManager.getDataNetworkType() + "\n";

        text += telephonyManager.getSimState() + "\n";
        text += telephonyManager.getNetworkOperatorName() + "\n";
        textView.setText(text);
    }


    public void onClickGetCurrent(View v) {
        currentNetworkInfo();
    }

    public void onClickGetCellInfos(View v) {
        cellInfos();
    }

    private void cellInfos() {



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_PHONE_STATE
            }, 1);

            return;
        }


        StringBuilder text = new StringBuilder();
        List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
        text.append("Found ").append(cellInfoList.size()).append(" cells\n");

        for (CellInfo info: cellInfoList){
            if (info instanceof CellInfoWcdma) {
                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) info;
                CellIdentityWcdma id = cellInfoWcdma.getCellIdentity();

                text.append("WCDMA ID:{cid: ").append(id.getCid());
                text.append(" mcc: ").append(id.getMcc());
                text.append(" mnc: ").append(id.getMnc());
                text.append(" lac: ").append(id.getLac());
                text.append(") Level: ").append(cellInfoWcdma.getCellSignalStrength().getLevel()).append("\n");

            } else if (info instanceof CellInfoLte) {
                CellInfoLte cellInfoLte = (CellInfoLte) info;
                CellIdentityLte id = cellInfoLte.getCellIdentity();



                text.append("LET ID:{cid: ").append(id.getCi());
                text.append(" mcc: ").append(id.getMcc());
                text.append(" mnc: ").append(id.getMnc());

                text.append(" tac: ").append(id.getTac());
                text.append(") Level: ").append(cellInfoLte.getCellSignalStrength().getLevel()).append("\n");
            }
        }

        textView.setText(text);



    }




    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch(requestCode) {
            case 0:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    currentNetworkInfo();
                } else {
                    Toast.makeText(this, "Acepta", Toast.LENGTH_SHORT).show();
                }
                break;

            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    cellInfos();
                } else {
                    Toast.makeText(this, "Acepta", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }




}