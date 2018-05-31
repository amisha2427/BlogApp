package com.example.dell.blogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {


    private android.support.v7.widget.Toolbar mainToolBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private String current_user_id;

    private FloatingActionButton addPostButton;
    private BottomNavigationView mainBottomNav;

    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    // Amisha
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();
        mFirestore=FirebaseFirestore.getInstance();

        //To check whether user has its account settings? if it has only then it will go to mainActivity


        mainToolBar=findViewById(R.id.mainToolbar);
        setSupportActionBar(mainToolBar);

        getSupportActionBar().setTitle("Blog App");

        if(mAuth.getCurrentUser()!= null) {

        mainBottomNav = findViewById(R.id.mainBottomNav);

        //FRAGMENTS
        homeFragment =new HomeFragment();
        notificationFragment = new NotificationFragment();
        accountFragment = new AccountFragment();

        replaceFragment(homeFragment);


        mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

               switch (item.getItemId())
               {

                   case R.id.bottom_action_home :
                       replaceFragment(homeFragment);
                       return  true;

                   case R.id.bottom_action_notification :
                       replaceFragment(notificationFragment);
                       return true;

                   case R.id.bottom_action_account :
                       replaceFragment(accountFragment);
                       return true;

                       default:
                           return false;

               }

            }
        });


        addPostButton=findViewById(R.id.add_post_button);
        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent newPostIntent = new Intent(MainActivity.this,NewPostActivity.class);
                startActivity(newPostIntent);
            }
        });

     }

    }



    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser==null)
        {
            sendToLogin();

        } else {

            current_user_id=mAuth.getCurrentUser().getUid();

            mFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if ((task.isSuccessful()))
                    {

                        if(!task.getResult().exists())
                        {
                            Intent setUpActivity = new Intent(MainActivity.this,SetupActivity.class);
                            startActivity(setUpActivity);
                            finish();
                        }
                    }
                    else {

                        String error=task.getException().getMessage();
                        Toast.makeText(MainActivity.this,"Error : " + error , Toast.LENGTH_LONG).show();
                    }

                }
            });
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

            case (R.id.action_settings_btn):
            {

                Intent setupActivityIntent = new Intent(MainActivity.this,SetupActivity.class);
                startActivity(setupActivityIntent);

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

    private void replaceFragment(Fragment fragment)
    {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container,fragment);
        fragmentTransaction.commit();

    }


}
