package com.example.dell.blogapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    //using this code in onStart because whenever the app starts we need to check that the user is logged in or not?
    @Override
    protected void onStart() {
        super.onStart();
        //getting the id of the current user
        //by first getting an instance(i.e. an entry point to the firebase authentication)
        // and then using the getCurrent user method
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser==null)
        {
            //start the login intent
            Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(loginIntent);
            //finish the previous page so that when the user presses the back button of the phone
            //he/she navigates out of the app
            finish();
        }

    }
}
