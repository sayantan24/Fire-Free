package com.sayantan.smartalarm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sayantan.smartalarm.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditActivity extends AppCompatActivity {

    TextInputLayout etMail;
    TextInputLayout etMob;
    String mail;
    String mobile;
    String serial;
    Button btnSave;
    Button btnReset;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        etMail = (TextInputLayout) findViewById(R.id.etEmail);
        etMob = (TextInputLayout) findViewById(R.id.etMob);
        btnSave = (Button)findViewById(R.id.btnSave);
        btnReset = (Button)findViewById(R.id.btnReset);

        sharedPreferences = getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        serial = sharedPreferences.getString("serial","Sensors");

        final DatabaseReference mydb= FirebaseDatabase.getInstance().getReference().child("Users").child(serial);

        mydb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                mail = snapshot.child("email").getValue().toString();
                mobile = snapshot.child("mobile").getValue().toString();
                etMail.getEditText().setText(mail);
                etMob.getEditText().setText(mobile);

                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String newMail = etMail.getEditText().getText().toString();
                        String newMob = etMob.getEditText().getText().toString();
                        if(!connected()){
                            Toast.makeText(EditActivity.this, "Check Internet Connection!", Toast.LENGTH_LONG).show();
                        }
                        else{
                        Map<String, Object> hopperUpdates = new HashMap<>();
                        hopperUpdates.put("email",newMail);
                        hopperUpdates.put("mobile",newMob);

                        mydb.updateChildren(hopperUpdates);
                        Toast.makeText(EditActivity.this,"Changes Saved",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(EditActivity.this,AlarmActivity.class);
                        startActivity(intent);
                    }
                    }
                });

                btnReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(EditActivity.this,MainActivity.class);
                        if(!connected()){
                            Toast.makeText(EditActivity.this, "Check Internet Connection!", Toast.LENGTH_LONG).show();
                        }
                        else{
                            sharedPreferences.edit().remove("serial").commit();
                            sharedPreferences.edit().putBoolean("log",false).apply();
                            Map<String, Object> hopperUpdates = new HashMap<>();
                            hopperUpdates.put("email","no email!");
                            hopperUpdates.put("mobile","no number!");

                            mydb.updateChildren(hopperUpdates);
                            Toast.makeText(EditActivity.this,"Reset Successful",Toast.LENGTH_SHORT).show();
                            startActivity(i);
                            finish();
                        }}
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    public boolean connected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager!=null)
        {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo!=null)
            {
                if(networkInfo.getState()==NetworkInfo.State.CONNECTED){
                    return true;
                }
            }
        }

        return false;

    }
}