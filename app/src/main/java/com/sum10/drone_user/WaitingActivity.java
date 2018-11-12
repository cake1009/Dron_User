package com.sum10.drone_user;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class WaitingActivity extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("callservice");

    Handler handler = new Handler();

    Intent intent;
    String myloc;
    String locker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);

        intent = getIntent();
        myloc = intent.getStringExtra("myloc");
        locker = intent.getStringExtra("locker");

        TextView location = (TextView) findViewById(R.id.textView2);
        location.setText("현재 위치 : " + myloc);
        TextView Locker = (TextView) findViewById(R.id.textView3);
        Locker.setText("목적지 : " + locker);

        Button cancel = (Button) findViewById(R.id.cancel);

        cancel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(WaitingActivity.this);
                alert.setMessage("호출을 취소하시겠습니까?").setCancelable(false).setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'YES'
                                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            if (snapshot.child("address").getValue().equals(myloc) && snapshot.child("ack").getValue().equals(false)) {
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
                        }).setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'No'
                                return;
                            }
                        });
                alert.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(WaitingActivity.this);
        alert.setMessage("호출을 취소하시겠습니까?").setCancelable(false).setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 'YES'
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    if (snapshot.child("address").getValue().equals(myloc) && snapshot.child("ack").getValue().equals(false)) {
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
                }).setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 'No'
                        return;
                    }
                });
        alert.show();
    }


    protected void onResume() {
        super.onResume();
    }

    protected void onStart() {
        super.onStart();
        handler.postDelayed(new Runnable() {
            public void run() {
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (snapshot.child("address").getValue().equals(myloc)) {
                                if (snapshot.child("ack").getValue().equals(true)) {
                                    Intent intent2 = new Intent(WaitingActivity.this, CallActivity.class);
                                    intent2.putExtra("address", myloc);
                                    intent2.putExtra("lat", intent.getDoubleExtra("lat", -1));
                                    intent2.putExtra("lng", intent.getDoubleExtra("lng", -1));
                                    intent2.putExtra("locker", locker);
                                    startActivity(intent2);
                                    finish();
                                }
                            }
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

    protected void onPause() {
        super.onPause();
    }

    protected void onStop() {
        super.onStop();
    }
}
