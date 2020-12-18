package com.sayantan.smartalarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.sayantan.smartalarm.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class AlarmActivity extends AppCompatActivity {

    TextView txtMail;
    TextView txtTemp;
    TextView txtSmoke;
    TextView txtStatus;
    Button btnPolice;
    Button btnFire;
    Button btnAmbulance;
    Button btnEdit;
    Button btnAid;
    LinearLayout lLayout;
    SharedPreferences sharedPreferences;
    String serial;
    String url = "https://www.healthline.com/health/first-aid-with-burns";
    DatabaseReference connectedRef;
    DatabaseReference mydb;
    boolean connected;
    RelativeLayout rl;
    MediaPlayer player;
    Switch swAlarm;

    boolean sent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Fire Notification", "Alarm", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},1);
            }
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE},1);
            }
        }

        rl = (RelativeLayout)findViewById(R.id.rl);
        txtMail = (TextView)findViewById(R.id.txtMail);
        txtTemp = (TextView)findViewById(R.id.txtTemp);
        txtSmoke = (TextView)findViewById(R.id.txtSmoke);
        txtStatus = (TextView)findViewById(R.id.txtStatus);
        btnPolice =(Button) findViewById(R.id.btnPolice);
        btnFire = (Button)findViewById(R.id.btnFire);
        btnAmbulance= (Button)findViewById(R.id.btnAmbulance);
        btnEdit = (Button)findViewById(R.id.btnEdit);
        btnAid = (Button)findViewById(R.id.btnAid);
        lLayout = (LinearLayout)findViewById(R.id.lLayout);
        swAlarm = (Switch)findViewById(R.id.swAlarm);

        sharedPreferences = getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        serial = sharedPreferences.getString("serial","Sensors");
        mydb= FirebaseDatabase.getInstance().getReference().child("Users").child(serial);
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                connectedRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        connected = snapshot.getValue(Boolean.class);
                        if (!connected) {
                            Toast.makeText(AlarmActivity.this,"Check Internet Connection",Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }

                });

                handler.postDelayed(this,4000);

            }

        },4000);

        try {

            mydb.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.

                    String tempdata = dataSnapshot.child("Temperature").getValue().toString();
                    String smdata = dataSnapshot.child("gas level").getValue().toString();
                    String email = dataSnapshot.child("email").getValue().toString();
                    String monum = dataSnapshot.child("mobile").getValue().toString();
                    txtTemp.setText(tempdata);
                    //txtSmoke.setText(smdata);
                    txtMail.setText(email);

                    Double temperature = Double.parseDouble(tempdata);
                    Double smoke = Double.parseDouble(smdata);

                    if(temperature>=140 )
                    {


                            rl.setBackgroundColor(Color.parseColor("#F8BCC1"));
                            if(smoke == 0) {
                                txtSmoke.setText("Detected");

                            }else
                            {
                                txtSmoke.setText(R.string.not_detected);
                            }
                            txtStatus.setText(R.string.danger);
                            txtStatus.setTextColor(Color.parseColor("#E10A0A"));

                            if(!sent) {

                                play();
                                swAlarm.setChecked(true);
                                swAlarm.setVisibility(View.VISIBLE);

                                swAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        if (!isChecked) {
                                            stop();
                                            swAlarm.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });
                                String message = "POTENTIAL FIRE HAZARD DETECTED!!!";
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(
                                        AlarmActivity.this, "Fire Notification"
                                ).setSmallIcon(R.drawable.ic_fire)
                                        .setContentTitle("FIRE ALERT!")
                                        .setContentText(message)
                                        .setAutoCancel(true);

                                Intent intentNotify = new Intent(AlarmActivity.this, AlarmActivity.class);
                                intentNotify.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intentNotify.putExtra("message", message);

                                PendingIntent pendingIntent = PendingIntent.getActivity(AlarmActivity.this, 0, intentNotify, PendingIntent.FLAG_UPDATE_CURRENT);

                                builder.setContentIntent(pendingIntent);

                                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(AlarmActivity.this);
                                managerCompat.notify(1, builder.build());

                                sms(monum);
                                sent = true;

                            }

                        btnPolice.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                caller("100");
                            }
                        });

                        btnFire.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                caller("101");
                            }
                        });

                        btnAmbulance.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                caller("102");
                            }
                        });



                        lLayout.setVisibility(View.VISIBLE);

                    }
                    else {
                        sent = false;
                        //txtSmoke.setText(R.string.not_detected);
                        if(smoke == 0) {
                            txtSmoke.setText("Detected");
                            txtStatus.setText(R.string.check_room);
                            txtStatus.setTextColor(Color.parseColor("#0E0E0E"));
                            String message = "SMOKE DETECTED!! PLEASE CHECK";
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                                    AlarmActivity.this, "Fire Notification"
                            ).setSmallIcon(R.drawable.ic_fire)
                                    .setContentTitle("POTENTIAL FIRE ALERT!")
                                    .setContentText(message)
                                    .setAutoCancel(true);

                            Intent intentNotify = new Intent(AlarmActivity.this, AlarmActivity.class);
                            intentNotify.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intentNotify.putExtra("message", message);

                            PendingIntent pendingIntent = PendingIntent.getActivity(AlarmActivity.this, 0, intentNotify, PendingIntent.FLAG_UPDATE_CURRENT);

                            builder.setContentIntent(pendingIntent);

                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(AlarmActivity.this);
                            managerCompat.notify(1, builder.build());
                        }else
                        {
                            txtSmoke.setText(R.string.not_detected);
                            txtStatus.setText(R.string.safe);
                            txtStatus.setTextColor(Color.parseColor("#09970B"));
                            lLayout.setVisibility(View.INVISIBLE);
                            swAlarm.setChecked(false);
                            if(!swAlarm.isChecked())
                            {
                                stop();
                                swAlarm.setVisibility(View.INVISIBLE);
                            }

                            rl.setBackgroundColor(Color.parseColor("#FFAA00"));

                        }



                    }

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Toast.makeText(AlarmActivity.this,"Some Error occurred!",Toast.LENGTH_LONG).show();
                }
            });
        } catch(Exception e)
        {


        }

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlarmActivity.this,EditActivity.class);
                startActivity(intent);

            }
        });

        btnAid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);

            }
        });


    }

    public void caller(String number){
        String num = "tel:"+number;
        startActivity(new Intent(Intent.ACTION_CALL,Uri.parse(num)));
    }

    public void sms(String number){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number,null,"Fire Hazard",null,null);

    }

    public void play() {
        if (player == null) {
            player = MediaPlayer.create(this, R.raw.alarm_sound);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    play();
                }
            });
        }
        player.start();
    }


    public void stop() {
        stopPlayer();
    }
    private void stopPlayer() {
        if (player != null) {
            player.release();
            player = null;
            //Toast.makeText(this, "MediaPlayer released", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        stopPlayer();
    }

}




