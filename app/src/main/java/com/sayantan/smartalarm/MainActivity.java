package com.sayantan.smartalarm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
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

public class MainActivity extends AppCompatActivity {
    TextInputLayout etEmail;
    TextInputLayout etSerial;
    TextInputLayout etMoNumber;
    Button btLogin;
    SharedPreferences sharedPreferences;
    FirebaseDatabase rootNode;
    DatabaseReference ref;
    boolean isLoggedIn;
    //ConnectivityManager connectivityManager;
    //NetworkInfo networkInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail =  (TextInputLayout) findViewById(R.id.etEmail);
        etSerial = (TextInputLayout) findViewById(R.id.etSerial);
        etMoNumber = (TextInputLayout) findViewById(R.id.etMoNumber);
        btLogin = (Button) findViewById(R.id.btLogin);
        rootNode = FirebaseDatabase.getInstance();
        ref = rootNode.getReference("Users");
        sharedPreferences = getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        //sharedPreferences.edit().clear().apply();
        isLoggedIn = sharedPreferences.getBoolean("log", false);
        //connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        //networkInfo = connectivityManager.getActiveNetworkInfo();

        if (isLoggedIn) {
            Intent intent = new Intent(MainActivity.this, AlarmActivity.class);
            startActivity(intent);
            finish();
        }

            btLogin.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    final String email = etEmail.getEditText().getText().toString();
                    final String serial = etSerial.getEditText().getText().toString();
                    final String mobile = etMoNumber.getEditText().getText().toString();
                    if (!connected()) {
                        Toast.makeText(MainActivity.this, "Check Internet Connection!", Toast.LENGTH_LONG).show();
                    }
                    else{
                    if (TextUtils.isEmpty(email) || TextUtils.isEmpty(serial) || TextUtils.isEmpty(mobile) || mobile.length() != 10) {
                        Toast.makeText(MainActivity.this, "Enter all fields correctly", Toast.LENGTH_LONG).show();
                    } else {
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.hasChild(serial)) {
                                    savePreference(serial);
                                    //UserProfile userProfile = new UserProfile(email,mobile);
                                    Toast.makeText(MainActivity.this, "Welcome!!", Toast.LENGTH_SHORT).show();
                                    Map<String, Object> hopperUpdates = new HashMap<>();
                                    hopperUpdates.put("email", email);
                                    hopperUpdates.put("mobile", mobile);
                                    ref.child(serial).updateChildren(hopperUpdates);

                                    Intent intent = new Intent(MainActivity.this, AlarmActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    Toast.makeText(MainActivity.this, "Invalid User!!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(MainActivity.this, "Some Error Occurred", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    }


                }
            });


    }


   public void savePreference(String serialCode){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("log",true).apply();
        editor.putString("serial",serialCode).apply();
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