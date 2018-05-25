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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

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
    private FirebaseFirestore mfirebaseFirestore;
    private String user_name;
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        //Toolbar for setupAccount
        Toolbar setupToolbar=findViewById(R.id.setup_Toolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Settings");

        mfirebaseAuth=FirebaseAuth.getInstance();
        user_id=mfirebaseAuth.getCurrentUser().getUid();

        mstorageReference= FirebaseStorage.getInstance().getReference();
        mfirebaseFirestore =FirebaseFirestore.getInstance();

        setupImage=findViewById(R.id.setupProfile);
        setUpName=findViewById(R.id.setup_name);
        setUpBtn=findViewById(R.id.setupBtn);
        setUpProgressBar = findViewById(R.id.setup_ProgressBar);

        mfirebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {

                if(task.isSuccessful())
                {
                    if(task.getResult().exists()){

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        // To retrieve image from  string image name ,add library
                        RequestOptions placeHolderRequest = new RequestOptions();
                        placeHolderRequest.placeholder(R.drawable.ic_launcher_background);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeHolderRequest).load(image).into(setupImage);

                        setUpName.setText(name);
                    }


                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this,"(Firestore Error) : " + error , Toast.LENGTH_LONG).show();
                }

            }
        });

        setUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                user_name=setUpName.getText().toString();

                if(!TextUtils.isEmpty(user_name) && mainImageURI != null)
                {

                    user_id = mfirebaseAuth.getCurrentUser().getUid();
                    setUpProgressBar.setVisibility(View.VISIBLE);

                    StorageReference image_path= mstorageReference.child("profile_images").child(user_id +".jpg");
                    image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if(task.isSuccessful())
                            {
                                Uri download_uri;
                                if (task!=null)
                                    download_uri=task.getResult().getDownloadUrl();
                                else
                                    download_uri=mainImageURI;

                                Map<String,String> usermap = new HashMap<>();
                                usermap.put("name",user_name);
                                usermap.put("image",download_uri.toString());

                                // Create collection in firestore and create a document with userid and two fields in there name and images
                                mfirebaseFirestore.collection("Users").document(user_id).set(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful())
                                        {
                                            Toast.makeText(SetupActivity.this, "The user Settings are updated. ", Toast.LENGTH_SHORT).show();
                                            Intent mainIntent = new Intent(SetupActivity.this,MainActivity.class);
                                            startActivity(mainIntent);
                                            finish(); // When user presses back button we don't want to come back to the page

                                        } else
                                        {

                                            String error = task.getException().getMessage();
                                            Toast.makeText(SetupActivity.this,"(Firestore Error) : " + error , Toast.LENGTH_LONG).show();
                                        }
                                        setUpProgressBar.setVisibility(View.INVISIBLE);

                                    }
                                });

                                Toast.makeText(SetupActivity.this,"The image is uploaded", Toast.LENGTH_LONG).show();

                            }
                            else
                            {
                                String error = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this," Image Error: " + error, Toast.LENGTH_LONG).show();

                                setUpProgressBar.setVisibility(View.INVISIBLE);

                            }


                        }
                    });

                }

            }
        });

        //make circleImageview clickable

        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

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
                        cropImagePicker();

                    }

                }

                else
                {
                    cropImagePicker();
                }

            }
        }); //onzclick circleimage  function ends
    }//onCreate method ends here

    private void cropImagePicker()
    {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SetupActivity.this); // send user to cropActivity to select image from camera,gallery,etc
    }


    // requestPermission demands requestCode 1 so it must generate some result


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // permission granted and after that the stuffs we want to do can be coded here


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
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
