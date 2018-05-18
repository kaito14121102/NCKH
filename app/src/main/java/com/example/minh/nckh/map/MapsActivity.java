package com.example.minh.nckh.map;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.minh.nckh.Model.ToaDo;
import com.example.minh.nckh.R;
import com.example.minh.nckh.map_location.ThongTinMapActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private List<LatLng> polyLineList;
    private Marker marker;
    private float v;
    private double lat, lng;
    private Handler handler;

    private LatLng startPosition, endPosition;
    private int index, next;

    private Button btnGo;
    private EditText edtPlace;
    private String destination;
    private PolylineOptions polylineOptions, blackpolylineOptions;
    private Polyline blackPolyline, greyPolyline;

    private LatLng myLocation;

    IGoogleApi mService;

    double LAT = 0.0, LNG = 0.0;

    CircleImageView img_detail;
    ToaDo toaDo=null;
    public static String END_LOCATE="203 hoang quoc viet";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        img_detail = findViewById(R.id.img_details);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        LAT = getIntent().getDoubleExtra("LAT", 0);
        LNG = getIntent().getDoubleExtra("LNG", 0);

        toaDo=new ToaDo(LAT,LNG);
//        mapFragment.getMapAsync(this);-----------------------------------------

        polyLineList = new ArrayList<>();
        btnGo = (Button) findViewById(R.id.btnSearch);
        edtPlace = (EditText) findViewById(R.id.edtPlace);

        edtPlace.setText(END_LOCATE);
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destination = edtPlace.getText().toString();
                destination = destination.replace(" ", "+");
                mapFragment.getMapAsync(MapsActivity.this);

            }
        });

        mService = Common.getGoogleApi();

        img_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "detail", Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(MapsActivity.this, ThongTinMapActivity.class);
                intent.putExtra("TOADO",toaDo);
                intent.putExtra("END",END_LOCATE);

                startActivity(intent);

            }
        });


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);

        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Add a marker in Sydney and move the camera20.956473, 105.756880
        final LatLng sydney = new LatLng(LAT, LNG);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(googleMap.getCameraPosition().target)
                .zoom(17)
                .bearing(30)
                .tilt(45)
                .build()));

        String requestUrl = null;
        try {
            requestUrl = "https://maps.googleapis.com/maps/api/directions/json?"
                    + "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + sydney.latitude + "," + sydney.longitude + "&" +
                    "destination=" + destination + "&" +
                    "key=" + getResources().getString(R.string.google_maps_key);
            Log.d("URL", requestUrl);

            mService.getDataFromGoogleApi(requestUrl)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());

                                JSONArray jsonArray = jsonObject.getJSONArray("routes");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject route = (JSONObject) jsonArray.getJSONObject(i);
                                    JSONObject poly = (JSONObject) route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    polyLineList = decodePoly(polyline);


                                    //Bounds
                                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                    for (LatLng latLng : polyLineList)
                                        builder.include(latLng);
                                    LatLngBounds bounds = builder.build();
                                    CameraUpdate mCameraUpdate = CameraUpdateFactory.
                                            newLatLngBounds(bounds, 2);

                                    mMap.animateCamera(mCameraUpdate);


                                    polylineOptions = new PolylineOptions();
                                    polylineOptions.color(Color.GRAY);
                                    polylineOptions.width(5);
                                    polylineOptions.startCap(new SquareCap());
                                    polylineOptions.endCap(new SquareCap());
                                    polylineOptions.jointType(JointType.ROUND);
                                    polylineOptions.addAll(polyLineList);
                                    greyPolyline = mMap.addPolyline(polylineOptions);

                                    //BlackPolyLine ()
                                    blackpolylineOptions = new PolylineOptions();
                                    blackpolylineOptions.color(Color.RED); //Màu của đường đi
                                    blackpolylineOptions.width(15); //Độc rộng của đường
                                    blackpolylineOptions.startCap(new SquareCap());
                                    blackpolylineOptions.endCap(new SquareCap());
                                    blackpolylineOptions.jointType(JointType.ROUND);
                                    blackpolylineOptions.addAll(polyLineList);
                                    blackPolyline = mMap.addPolyline(blackpolylineOptions);

                                    mMap.addMarker(new MarkerOptions().position(polyLineList.get(polyLineList.size() - 1)));
                                    //Animator

                                    final ValueAnimator polylineAnimator = ValueAnimator.ofInt(0, 100);
                                    polylineAnimator.setDuration(2000);
                                    polylineAnimator.setInterpolator(new LinearInterpolator());
                                    polylineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                        @Override
                                        public void onAnimationUpdate(ValueAnimator animation) {

                                            List<LatLng> points = greyPolyline.getPoints();
                                            int percentValue = (int) animation.getAnimatedValue();
                                            int size = points.size();
                                            int newPoints = (int) (size * (percentValue / 100.0f));
                                            List<LatLng> p = points.subList(0, newPoints);
                                            blackPolyline.setPoints(p);


                                        }
                                    });

                                    polylineAnimator.start();

                                    //Add car marker
                                    marker = mMap.addMarker(new MarkerOptions().position(sydney)
                                            .flat(true)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.iconmap))
                                    );

                                    //Car moving

                                    handler = new Handler();
                                    index = -1;
                                    next = 1;
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (index < polyLineList.size() - 1) {
                                                index++;
                                                next = index + 1;

                                            }
                                            if (index < polyLineList.size() - 1) {
                                                startPosition = polyLineList.get(index);
                                                endPosition = polyLineList.get(next);

                                            }

                                            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                                            valueAnimator.setDuration(3000);
                                            valueAnimator.setInterpolator(new LinearInterpolator());
                                            valueAnimator.addUpdateListener(new ValueAnimator.
                                                    AnimatorUpdateListener() {
                                                @Override
                                                public void onAnimationUpdate(ValueAnimator animation) {
                                                    v = animation.getAnimatedFraction();
                                                    lng = v * endPosition.longitude + (1 - v)
                                                            * startPosition.longitude;
                                                    lat = v * endPosition.latitude + (1 - v)
                                                            * startPosition.latitude;
                                                    LatLng newPos = new LatLng(lat, lng);
                                                    marker.setPosition(newPos);
                                                    marker.setAnchor(0.5f, 0.5f);
                                                    marker.setRotation(geBearing(startPosition, newPos));
                                                    mMap.moveCamera(CameraUpdateFactory.
                                                            newCameraPosition(new CameraPosition.Builder()
                                                                    .target(newPos)
                                                                    .zoom(15.5f)
                                                                    .build()
                                                            ));
                                                }
                                            });

                                            valueAnimator.start();
                                            handler.postDelayed(this, 3000);
                                        }
                                    }, 3000);


                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(MapsActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private float geBearing(LatLng startPosition, LatLng newPos) {
        double lat = Math.abs(startPosition.latitude - newPos.latitude);
        double lng = Math.abs(startPosition.longitude - newPos.longitude);

        if (startPosition.latitude < newPos.latitude && startPosition.longitude < newPos.longitude) {
            return (float) (Math.toDegrees(Math.atan(lng / lat)));

        } else if (startPosition.latitude >= newPos.latitude && startPosition.longitude < newPos.longitude) {
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);

        } else if (startPosition.latitude >= newPos.latitude && startPosition.longitude >= newPos.longitude) {
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);

        } else if (startPosition.latitude < newPos.latitude && startPosition.longitude >= newPos.longitude) {
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        }
        return -1;
    }

    private List<LatLng> decodePoly(String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;

            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }
}
