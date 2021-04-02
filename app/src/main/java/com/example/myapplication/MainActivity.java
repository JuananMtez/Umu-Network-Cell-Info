package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    private Button button;
    private int select;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.tecnologia);
        select = 0;
    }


    public void onClickActividadMapa(View v) {

        if (button.getText().equals(getString(R.string.Tecnologia))) {

            Toast.makeText(this, getString(R.string.ChooseTec), Toast.LENGTH_SHORT).show();

        } else {

            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("tecnologia", button.getText());
            startActivity(intent);
        }
    }


    public void onClickDialog(View v) {

        final String[] items = new String[3];

        items[0] = getString(R.string.Lte);
        items[1] = getString(R.string.Wcdma);
        items[2] = getString(R.string.Gsm);

        new AlertDialog.Builder(this).setTitle(getString(R.string.ChooseTec))
                .setSingleChoiceItems(items, select, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which){
                            case 0:
                                button.setText(getString(R.string.Lte));
                                select = 0;
                                break;
                            case 1:
                                button.setText(getString(R.string.Wcdma));
                                select = 1;
                                break;
                            case 2:
                                button.setText(getString(R.string.Gsm));
                                select = 2;
                                break;
                        }

                        dialog.dismiss();

                    }
                }).show();
    }
}