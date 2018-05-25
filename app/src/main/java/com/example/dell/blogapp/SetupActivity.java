package com.example.dell.blogapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private Toolbar setupToolbar;

    private CircleImageView setupImage;
    private Uri mainImageURI =null;

    private EditText setUpName;
    private Button setUpBtn;
    private ProgressBar setUpProgressBar;

    private StorageReference mstorageReference;
    private FirebaseAuth mfirebaseAuth; // needed to store the image with user id
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        //Toolbar for setupAccount
        Toolbar setupToolbar=findViewById(R.id.setup_Toolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Settings");

        mfirebaseAuth=FirebaseAuth.getInstance();
        mstorageReference= FirebaseStorage.getInstance().getReference();

        setupImage=findViewById(R.id.setupProfile);
        setUpName=findViewById(R.id.setup_name);
        setUpBtn=findViewById(R.id.setupBtn);
        setUpProgressBar = findViewById(R.id.setup_ProgressBar);

        setUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String user_name=setUpName.getText().toString();
                setUpProgressBar.setVisibility(View.VISIBLE);
                if(!TextUtils.isEmpty(user_name) && mainImageURI != null)
                {

                    String user_id = mfirebaseAuth.getCurrentUser().getUid();
                    StorageReference image_path= mstorageReference.child("profile_images").child(user_id +".jpg");
                    image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if(task.isSuccessful())
                            {
                                Uri download_image = task.getResult().getDownloadUrl();
                                Toast.makeText(SetupActivity.this,"The image is uploade", Toast.LENGTH_LONG).show();

                            }
                            else
                            {
                                String error = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this,"Error: " + error, Toast.LENGTH_LONG).show();

                            }

                            setUpProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });

                }

            }
        });

        //make circleImageview clickable

        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //when user taps on circleImage, to check if android version is greater than marshmallow version
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    // Check whether if permission is already granted or not

                    if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                    {

                        Toast.makeText(SetupActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                        // To grant permission for storage directly from app
                        ActivityCompat.requestPermissions(SetupActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }
                    else
                    {
                        // start picker to get image for cropping and then use the image in cropping activity

                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(SetupActivity.this); // send user to cropActivity to select image from camera,gallery,etc
                    }

                }

            }
        }); //onzclick circleimage  function ends
    }//onCreate method ends here


    // requestPermission demands requestCode 1 so it must generate some result


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // permission granted and after that the stuffs we want to do can be coded here


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageURI = result.getUri(); //store the uri of  cropped image  to the mainImageURI
                setupImage.setImageURI(mainImageURI);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }
}// SetupActivity ends
