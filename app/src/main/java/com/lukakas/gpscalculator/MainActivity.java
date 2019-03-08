package com.lukakas.gpscalculator;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;

    private Button discoverButton;
    private Button grantGPSButton;
    private Button enableGPSButton;
    private Button disableGPSButton;
    private Button startRouteButton;
    private Button stopRouteButton;

    private EditText discover;
    private TextView distanceValue;

    private double latAtual, longAtual, latAntigo, longAntigo;

    Location locAntigo = new Location(LocationManager.GPS_PROVIDER);
    Location locAtual = new Location(LocationManager.GPS_PROVIDER);
    private Chronometer chronometer;

    private float distance=0;

    private static final int REQUEST_PERMISSION_GPS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        discoverButton = findViewById(R.id.searchButton);
        stopRouteButton = findViewById(R.id.stoprouteButton);
        distanceValue = findViewById(R.id.travelleddistanceValueTextView);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        chronometer.setFormat("%s");
        locationManager =(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        discover =  (EditText) findViewById(R.id.searchPlainText);

        locationListener =
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        latAtual = location.getLatitude();
                        longAtual = location.getLongitude();
                        String exibir =
                                String.format(
                                        Locale.getDefault(),
                                        "Lat:%f, Long:%f",
                                        latAtual,
                                        longAtual
                                );
                        exibirMensagem(exibir, 1);
                        locAtual.setLatitude(latAtual);
                        locAtual.setLongitude(longAtual);
                        if (locAtual==null) exibirMensagem(getResources().getString(R.string.locationnotfound_Text), 1);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                };

        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                distance = distance + (locAtual.distanceTo(locAntigo)/10000000);
                DecimalFormat df = new DecimalFormat("0.0");
                distanceValue.setText(String.valueOf(df.format(distance))+" "+getResources().getString(R.string.meters));
            }
        });

        grantGPSButton = findViewById(R.id.grantgpsButton);
        grantGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    exibirMensagem(getResources().getString(R.string.permissiongpsalreadygrantedText), 1);
                } else {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            },
                            REQUEST_PERMISSION_GPS
                    );
                }
            }
        });
        enableGPSButton = findViewById(R.id.enablegpsButton);



        enableGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    exibirMensagem(getResources().getString(R.string.permissiongpsalreadygrantedText), 1);
                } else {
                    exibirMensagem(getResources().getString(R.string.clickgrantgpsText), 1);
                }

            }
        });

        disableGPSButton = findViewById(R.id.disablegpsButton);
        disableGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                boolean isOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (isOn) {
                    locationManager.removeUpdates(locationListener);
                    exibirMensagem(getResources().getString(R.string.gpsturnedoff), 1);
                }
                else {
                    exibirMensagem(getResources().getString(R.string.gpsalreadyoff), 1);
                }
            }
        });

        startRouteButton = findViewById(R.id.startrouteButton);
        startRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                locAntigo.setLongitude(longAtual);
                locAntigo.setLatitude(latAtual);
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
            }
        });

        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri =
                        Uri.parse(
                                String.format(
                                        Locale.getDefault(),
                                        "geo:%f,%f?q=" + discover.getText(),
                                        latAntigo,
                                        longAntigo
                                )
                        );
                Intent intent =
                        new Intent (
                                Intent.ACTION_VIEW,
                                uri
                        );
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });

        stopRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chronometer.stop();
                distance = 0;
            }
        });
    }

    public void exibirMensagem (String mensagem, int tempo){
        Toast.makeText(this, mensagem,  tempo).show();
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, locationListener);
//        }
//        else {
//            ActivityCompat.requestPermissions(this, new String[]{
//                    Manifest.permission.ACCESS_FINE_LOCATION
//            }, REQUEST_PERMISSION_GPS);
//        }
//    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_GPS){
            if (grantResults.length > 0 &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            2000,
                            5,
                            locationListener
                    );
                }
            }
            else{
                exibirMensagem(getResources().getString(R.string.nogpsisImpossibleText), 1);
            }
        }
    }
}