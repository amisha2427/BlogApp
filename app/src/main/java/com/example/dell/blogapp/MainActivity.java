package com.example.dell.blogapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {


    private android.support.v7.widget.Toolbar mainToolBar;

    private FirebaseAuth mAuth;
    // Amisha
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainToolBar=findViewById(R.id.mainToolbar);
        setSupportActionBar(mainToolBar);

        getSupportActionBar().setTitle("Blog App");

        mAuth=FirebaseAuth.getInstance();

    }



    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser==null)
        {
            sendToLogin();

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {

            case (R.id.action_logout_btn):
                    {
                        logOut();
                        return true;
                    }
                    default:
                        return false;
        }



    }

    private void logOut() {

        mAuth.signOut();
        sendToLogin();
    }


    private void sendToLogin() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(loginIntent);
        finish();

    }
}
