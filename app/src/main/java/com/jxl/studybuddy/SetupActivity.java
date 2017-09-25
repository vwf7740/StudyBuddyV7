package com.jxl.studybuddy;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashSet;
import java.util.List;

public class SetupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mCurrentUser;
    private FirebaseUser mUser;
    private StorageReference mStorageImage;
    private TextView mNameTextView;
    private ImageButton mProfilePictureButton;
    private Button mCourse1Button;
    private Button mCourse2Button;
    private Button mCourse3Button;
    private Button mCourse4Button;
    private Button mSubmitButton;
    private Uri mImageUri = null;
    private ProgressDialog mProgress;

    private static final int GALLERY_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);
        mCurrentUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.getUid());
        mUser = mAuth.getCurrentUser();
        mStorageImage = FirebaseStorage.getInstance().getReference().child("profile_images");
        mProgress = new ProgressDialog(this);

        mNameTextView = (TextView) findViewById(R.id.textView_name_setup);
        mProfilePictureButton = (ImageButton) findViewById(R.id.button_profilePicture_setup);
        mCourse1Button = (Button) findViewById(R.id.button_courseCode1_setup);
        mCourse2Button = (Button) findViewById(R.id.button_courseCode2_setup);
        mCourse3Button = (Button) findViewById(R.id.button_courseCode3_setup);
        mCourse4Button = (Button) findViewById(R.id.button_courseCode4_setup);
        mSubmitButton = (Button) findViewById(R.id.button_submit_setup);

        mCurrentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mNameTextView.setText(dataSnapshot.child("name").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //***********************************************************************************************************
        //Listeners for course code buttons. Opens a dialogue window for user to input or remove course codes.
        //Only first button is visible until first code has been entered. Next button becomes available when previous
        //button has a code in it. If code is removed by user, button is made invisible unless it is first button,
        //in which case, it's text is reset to +NEW.
        //***********************************************************************************************************
        mCourse1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(SetupActivity.this);
                final EditText courseCodeInput = new EditText(SetupActivity.this);
                alert.setView(courseCodeInput);
                alert.setMessage("Add or remove course code").setCancelable(false)

                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCourse1Button.setText(courseCodeInput.getText().toString().toUpperCase().trim());
                                mCourse2Button.setVisibility(View.VISIBLE);
                            }
                        })
                        .setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCourse1Button.setText("+NEW");
                            }
                        });
                alert.show();

            }
        });

        mCourse2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(SetupActivity.this);
                final EditText courseCodeInput = new EditText(SetupActivity.this);
                alert.setView(courseCodeInput);
                alert.setMessage("Add or remove course code").setCancelable(false)

                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCourse2Button.setText(courseCodeInput.getText().toString().toUpperCase().trim());
                                mCourse3Button.setVisibility(View.VISIBLE);
                            }
                        })
                        .setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCourse2Button.setText("+NEW");
                                mCourse2Button.setVisibility(View.INVISIBLE);
                            }
                        });
                alert.show();
            }
        });

        mCourse3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(SetupActivity.this);
                final EditText courseCodeInput = new EditText(SetupActivity.this);
                alert.setView(courseCodeInput);
                alert.setMessage("Add or remove course code").setCancelable(false)

                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCourse3Button.setText(courseCodeInput.getText().toString().toUpperCase().trim());
                                mCourse4Button.setVisibility(View.VISIBLE);
                            }
                        })
                        .setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCourse3Button.setText("+NEW");
                                mCourse3Button.setVisibility(View.INVISIBLE);
                            }
                        });
                alert.show();
            }
        });

        mCourse4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(SetupActivity.this);
                final EditText courseCodeInput = new EditText(SetupActivity.this);
                alert.setView(courseCodeInput);
                alert.setMessage("Add or remove course code").setCancelable(false)

                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCourse4Button.setText(courseCodeInput.getText().toString().toUpperCase().trim());
                                //mCourse4Button.setVisibility(View.VISIBLE);
                            }
                        })
                        .setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCourse4Button.setText("+NEW");
                                mCourse4Button.setVisibility(View.INVISIBLE);
                            }
                        });
                alert.show();
            }
        });
        //*********************************************************************************
        //Course code button listeners complete.
        //*********************************************************************************

        //If user hits Submit button, the setUpAccount() method is executed.
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpAccount();
            }
        });

        //When user hits the default ImageButton, their device gallery is opened.
        //User must choose an image that is square cropped automatically.
        mProfilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);

            }
        });
    }

    //Once the user has chosen a display image and input at least one course code, this method
    //writes the data to the DB and transitions to the MainActivity.
    private void setUpAccount() {
        String courses = "";
        //Adds any entered course codes to a String object, separated with a new line as a delimeter.
        final String user_id = mAuth.getCurrentUser().getUid();
        if(!mCourse1Button.getText().toString().equals("+New")){
            courses += mCourse1Button.getText().toString() + "\n";
        }else if(!mCourse2Button.getText().toString().equals("+New")){
            courses += mCourse2Button.getText().toString() + "\n";
        }else if(!mCourse3Button.getText().toString().equals("+New")){
            courses += mCourse3Button.getText().toString() + "\n";
        }else if(!mCourse4Button.getText().toString().equals("+New")){
            courses += mCourse4Button.getText().toString() + "\n";
        }
        //If an image has been chosen and the courses string isn't empty, proceed.
        if(mImageUri != null && !courses.equals("")){
            mProgress.setMessage("Saving changes...");
            mProgress.show();
            final String coursesString = courses;
            StorageReference filePath = mStorageImage.child(mImageUri.getLastPathSegment());
            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests")final String downloadURI = taskSnapshot.getDownloadUrl().toString();
                    //Set image to URL in DB.
                    mDatabaseUsers.child(user_id).child("image").setValue(downloadURI);
                    //Set courses to coursesString.
                    mDatabaseUsers.child(user_id).child("courses").setValue(coursesString);
                    //Set init_setup to true so that next time user logs in, they stay in the MainActivity.
                    mDatabaseUsers.child(user_id).child("init").setValue("complete");
                    mProgress.dismiss();
                    //Transition to MainActivity
                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                }
            });

        }else{
            //Do not proceed if Image is still default and/or courses String is empty.
            Toast.makeText(getApplicationContext(), "Please choose a profile picture, and at least one course code.", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    //Handles the Image cropping tool and forces it to be a square, 1:1 aspect ratio.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                mProfilePictureButton.setImageURI(mImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
