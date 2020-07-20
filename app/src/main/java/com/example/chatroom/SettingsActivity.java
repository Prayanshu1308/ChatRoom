package com.example.chatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();

    }

    private void InitializeFields() {
        updateAccountSettings = (Button) findViewById(R.id.update_settings_button);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
    }

    private void UpdateSettings() {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please write your user_name", Toast.LENGTH_SHORT).show();
        }else{
            HashMap<String, String> profileMap = new HashMap<>();
              profileMap.put("uid", currentUserID);
              profileMap.put("name", setUserName);
              profileMap.put("status", setStatus);
            RootRef.child("Users").child(currentUserID).setValue(profileMap)
              .addOnCompleteListener(new OnCompleteListener<Void>() {
                  @Override
                  public void onComplete(@NonNull Task<Void> task) {
                      if(task.isSuccessful()){
                          SendUserToMainActivity();
                          Toast.makeText(SettingsActivity.this, "Profile updated successfully...", Toast.LENGTH_SHORT).show();
                      }else{
                          String message = task.getException().toString();
                          Toast.makeText(SettingsActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                      }
                  }
              });
        }
    }

    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                       if((snapshot.exists()) && (snapshot.hasChild("name") && (snapshot.hasChild("image")))){

                           String retrieveUserName = snapshot.child("name").getValue().toString();
                           String retrieveStatus = snapshot.child("status").getValue().toString();
                           String retrieveProfileImage = snapshot.child("image").getValue().toString();
                           userName.setText(retrieveUserName);
                           userStatus.setText(retrieveStatus);

                       }else if((snapshot.exists()) && (snapshot.hasChild("name"))){

                           String retrieveUserName = snapshot.child("name").getValue().toString();
                           String retrieveStatus = snapshot.child("status").getValue().toString();
                           userName.setText(retrieveUserName);
                           userStatus.setText(retrieveStatus);

                       }else{

                           Toast.makeText(SettingsActivity.this, "Please set and update your profile information...", Toast.LENGTH_SHORT).show();

                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}