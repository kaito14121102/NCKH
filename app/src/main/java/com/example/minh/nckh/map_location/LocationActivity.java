package com.example.minh.nckh.map_location;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.minh.nckh.R;
import com.example.minh.nckh.map.MapsActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Locale;

public class LocationActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final String TAG = LocationActivity.class.getSimpleName();
    private static final long UPDATE_INTERVAL = 5000;
    private static final long FASTEST_INTERVAL = 5000;
    private static final int REQUEST_LOCATION_PERMISSION = 100;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private boolean mIsAutoUpdateLocation;

    private TextView mTvCurrentLocation;
    private Button mBtnGetLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        Log.d(TAG, "onCreate");
        initViews();

        requestLocationPermissions();

        if (isPlayServicesAvailable()) {
            setUpLocationClientIfNeeded();
            buildLocationRequest();
        } else {
            mTvCurrentLocation.setText("Device does not support Google Play services");
        }

//        if (isGpsOn()) {
//            updateUi();
//        } else {
//            Toast.makeText(LocationActivity.this, "GPS is OFF", Toast.LENGTH_SHORT).show();
//        }
        mBtnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUi();
//                if (isGpsOn()) {
////                    updateUi();
//                } else {
//                    Toast.makeText(LocationActivity.this, "GPS is OFF", Toast.LENGTH_SHORT).show();
//                }
            }
        });

//        mSwAutoUpdateLocation.setOnCheckedChangeListener(
//                new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        if (!isGpsOn()) {
//                            Toast.makeText(LocationActivity.this, "GPS is OFF",
//                                    Toast.LENGTH_SHORT).show();
//                            mSwAutoUpdateLocation.setChecked(false);
//                            return;
//                        }
//                        mIsAutoUpdateLocation = isChecked;
//                        if (isChecked) {
//                            startLocationUpdates();
//                        } else {
//                            stopLocationUpdates();
//                        }
//                    }
//                });
    }

    private void initViews() {
        mTvCurrentLocation = (TextView) findViewById(R.id.tv_current_location);
        mBtnGetLocation = (Button) findViewById(R.id.btn_get_location);
//        mSwAutoUpdateLocation = (Switch) findViewById(R.id.sw_auto_update_location);
    }

    private void updateUi() {
        if (mLastLocation != null) {
            Toast.makeText(this, "Vị trí hiện tại="+(mLastLocation.getLatitude()+"->"+ mLastLocation.getLongitude()), Toast.LENGTH_SHORT).show();
            mTvCurrentLocation.setText(String.format(Locale.getDefault(), "%f, %f",
                    mLastLocation.getLatitude(), mLastLocation.getLongitude()));



           final ProgressDialog title= ProgressDialog.
                    show(LocationActivity.this,"Dialog","Đang xác định vị trí của khách hàng...");
            title.setCanceledOnTouchOutside(false);
            new CountDownTimer(4000, 1000) {
                @Override
                public void onTick(long l) {
                }

                @Override
                public void onFinish() {
                    Toast.makeText(LocationActivity.this, "aa", Toast.LENGTH_SHORT).show();
                    title.dismiss();
                    Intent intent4=new Intent(LocationActivity.this, MapsActivity.class);
                    intent4.putExtra("LAT",mLastLocation.getLatitude());
                    intent4.putExtra("LNG",mLastLocation.getLongitude());
                    startActivity(intent4);

                }
            }.start();
        }
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    requestLocationPermissions();
                }
                break;
            default:
                break;
        }
    }

    private boolean isPlayServicesAvailable() {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
                == ConnectionResult.SUCCESS;
    }

    private boolean isGpsOn() {
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void setUpLocationClientIfNeeded() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void buildLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null) {
            mLastLocation = lastLocation;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, String.format(Locale.getDefault(), "onLocationChanged : %f, %f",
                location.getLatitude(), location.getLongitude()));
        mLastLocation = location;
        if (mIsAutoUpdateLocation) {
            updateUi();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient != null
                && mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
        Log.d(TAG, "onDestroy LocationService");
        super.onDestroy();
    }
}
