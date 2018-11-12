package com.sum10.drone_user;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CallActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private DatabaseReference myRef2;
    private Marker currentMarker = null;

    Handler handler = new Handler();

    double lat;
    double lng;
    double dronelat;
    double dronelng;
    LatLng drone = null;
    String address;
    String locker;
    TextView textview;
    Button complete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("callservice");

        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        locker = intent.getStringExtra("locker");
        textview = (TextView) findViewById(R.id.textview);
        textview.setText("드론이 지금 열심히 날아가고 있습니다!");
        complete = (Button) findViewById(R.id.complete);
        complete.setEnabled(false);

        complete.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(CallActivity.this);
                alert.setMessage("물품을 받았습니다. 고객님의 소중한 물품은 \r\n" + locker + " " +((int) (Math.random() * 50) + 1) + "번 보관함에 보관될 예정입니다.").setCancelable(false).setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            if (snapshot.child("address").getValue().equals(address) && snapshot.child("ack").getValue().equals(true)) {
                                                Map<String, Object> update = new HashMap<>();
                                                String key = snapshot.getKey();
                                                update.put(key, null);
                                                myRef.updateChildren(update);
                                                finish();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.w("TAG: ", "Failed to read value", databaseError.toException());
                                    }
                                });
                            }
                        });
                alert.show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocationName(address, 1);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("TAG", "geocoder Error");
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
        } else {
            Address address = addresses.get(0);
            lat = address.getLatitude();
            lng = address.getLongitude();
        }

        LatLng here = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(here).title("현재 위치"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here, 14));
    }

    @Override
    protected void onStart() {
        super.onStart();

        myRef2 = database.getReference("drone");
        handler.postDelayed(new Runnable() {
            public void run() {
                myRef2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dronelat = dataSnapshot.child("latitude").getValue(double.class);
                        dronelng = dataSnapshot.child("longitude").getValue(double.class);
                        drone = new LatLng(dronelat, dronelng);
                        setCurrentLocation(drone);
                        if (getCurrentAddress(drone).equals(address)) {
                            textview.setText("드론이 고객님 위치로 도착했습니다. 물품을 넣으시고 완료 버튼을 눌러주세요.");
                            complete.setEnabled(true);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("TAG: ", "Failed to read value", databaseError.toException());
                    }
                });
            }
        }, 1000);
    }

    public String getCurrentAddress(LatLng latlng) {
        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0);
        }
    }

    public void setCurrentLocation(LatLng currentDrone) {

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentDrone);
        markerOptions.title(locker + "으로 갑니다");
        markerOptions.draggable(true);

        //구글맵의 디폴트 현재 위치는 파란색 동그라미로 표시
        //마커를 원하는 이미지로 변경하여 현재 위치 표시하도록 수정 fix - 2017. 11.27
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.drone));

        currentMarker = mMap.addMarker(markerOptions);
    }
}
