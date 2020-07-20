package com.example.chatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private Button CreateAccountButton;
    private EditText UserEmail, UserPassword;
    private TextView AlreadyHaveAccountLink;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        InitializeFields();

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount(){
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please fill e-mail and password",Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Creating new account...");
            loadingBar.setMessage("Please wait while we are creating your account.");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password)
              .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                  @Override
                  public void onComplete(@NonNull Task<AuthResult> task) {

                      if(task.isSuccessful()) {
                          String currentUserID = mAuth.getCurrentUser().getUid();
                          RootRef.child("Users").child(currentUserID).setValue("");


                          mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                              @Override
                              public void onComplete(@NonNull Task<Void> task) {
                                  if(task.isSuccessful()){
                                      new AlertDialog.Builder(RegisterActivity.this)
                                              .setIcon(android.R.drawable.dark_header)
                                              .setTitle("Verify your E-mail")
                                              .setMessage("An E-mail has been sent to your registered E-mail. Please click the link given in it to verify your E-mail. Look in your spam or promotion section if not found.")
                                              .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                  @Override
                                                  public void onClick(DialogInterface dialogInterface, int i) {
                                                      Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                      startActivity(loginIntent);
                                                  }
                                              })
                                              .setCancelable(false)
                                              .show();
                                  }else{
                                      Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                  }
                              }
                          });


                          Toast.makeText(RegisterActivity.this, "Account created successfully...", Toast.LENGTH_SHORT).show();
                      }else {
                        String message = task.getException().toString();
                        Toast.makeText(RegisterActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                      }
                      loadingBar.dismiss();


                  }
              });
        }
    }

    private void InitializeFields() {
        CreateAccountButton = (Button) findViewById(R.id.register_button);
        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        AlreadyHaveAccountLink = (TextView) findViewById(R.id.already_have_account_link);
        loadingBar = new ProgressDialog(this);
    }

}