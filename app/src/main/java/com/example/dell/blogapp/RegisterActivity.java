package com.example.dell.blogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText regEmailText;
    private EditText regPassword;
    private EditText regConfirmPassword;
    private Button regBtn;
    private Button regLoginBtn;
    private ProgressBar regProgressbar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        regEmailText = findViewById(R.id.regEmail);
        regPassword = findViewById(R.id.regPassword);
        regConfirmPassword =findViewById(R.id.regConfirmPassword);
        regBtn = findViewById(R.id.regBtn);
        regLoginBtn = findViewById(R.id.regLoginBtn);
        regProgressbar = findViewById(R.id.regProgressBar);

        mAuth = FirebaseAuth.getInstance();


        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = regEmailText.getText().toString();
                String password = regPassword.getText().toString();
                String confirmPassword = regConfirmPassword.getText().toString();


                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword))
                {
                    if (password.equals(confirmPassword))
                    {
                        regProgressbar.setVisibility(View.VISIBLE);


                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful())
                                {
                                    sendToMain();
                                }
                                else {
                                    String e=task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this,"Error : " + e ,Toast.LENGTH_LONG).show();
                                }

                                regProgressbar.setVisibility(View.INVISIBLE);
                            }
                        });

                    }
                    else
                    {
                        Toast.makeText(RegisterActivity.this,"Password does not match",Toast.LENGTH_LONG).show();
                    }


                }
                else {


                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser!=null)
        {
            sendToMain();
        }

    }

   private void sendToMain()
   {
       Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
       startActivity(mainIntent);
       finish();
   }
}
