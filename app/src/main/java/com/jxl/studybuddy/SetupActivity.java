package com.jxl.studybuddy;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import id.zelory.compressor.Compressor;

public class SetupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mCurrentUser;
    private FirebaseUser mUser;
    private StorageReference mStorageImage;
    private byte[] mThumbByte;
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
    private InputFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);
        mUser = mAuth.getCurrentUser();
        mCurrentUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.getUid());
        mStorageImage = FirebaseStorage.getInstance().getReference().child("profile_images");
        mProgress = new ProgressDialog(this);
        mNameTextView = (TextView) findViewById(R.id.textView_name_profile);
        mProfilePictureButton = (ImageButton) findViewById(R.id.button_profilePicture_setup);
        mCourse1Button = (Button) findViewById(R.id.button_courseCode1_setup);
        mCourse2Button = (Button) findViewById(R.id.button_courseCode2_setup);
        mCourse3Button = (Button) findViewById(R.id.button_courseCode3_setup);
        mCourse4Button = (Button) findViewById(R.id.button_courseCode4_setup);
        mSubmitButton = (Button) findViewById(R.id.button_submit_setup);

        mCurrentUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mNameTextView.setText(dataSnapshot.child("name").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Input filter limits characters that can be entered in course codes to alphanumeric only.
        filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        //***********************************************************************************************************
        //Listeners for course code buttons. Opens a dialogue window for user to input or remove course codes.
        // If code is removed by user, it's text is reset to +NEW.
        //***********************************************************************************************************

        mCourse1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(SetupActivity.this);
                final EditText courseCodeInput = new EditText(SetupActivity.this);
                courseCodeInput.setFilters(new InputFilter[]{filter});
                alert.setView(courseCodeInput);
                alert.setMessage("Add or remove course code").setCancelable(false)

                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCourse1Button.setText(courseCodeInput.getText().toString().toUpperCase().trim());
                            }
                        })
                        .setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCourse1Button.setText("+New");
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
                courseCodeInput.setFilters(new InputFilter[]{filter});
                alert.setView(courseCodeInput);
                alert.setMessage("Add or remove course code").setCancelable(false)

                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCourse2Button.setText(courseCodeInput.getText().toString().toUpperCase().trim());
                            }
                        })
                        .setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCourse2Button.setText("+New");
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
                courseCodeInput.setFilters(new InputFilter[]{filter});
                alert.setView(courseCodeInput);
                alert.setMessage("Add or remove course code").setCancelable(false)

                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCourse3Button.setText(courseCodeInput.getText().toString().toUpperCase().trim());
                            }
                        })
                        .setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCourse3Button.setText("+New");
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
                courseCodeInput.setFilters(new InputFilter[]{filter});
                alert.setView(courseCodeInput);
                alert.setMessage("Add or remove course code").setCancelable(false)

                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCourse4Button.setText(courseCodeInput.getText().toString().toUpperCase().trim());
                            }
                        })
                        .setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCourse4Button.setText("+New");
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
        //User must choose an thumb_image that is square cropped automatically.
        mProfilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);

            }
        });
    }

    //Once the user has chosen a display thumb_image and input at least one course code, this method
    //writes the data to the DB and transitions to the MainActivity.
    private void setUpAccount() {
        String courses = "";
        //Adds any entered course codes to a String object, separated with a # as a delimeter.
        final String user_id = mAuth.getCurrentUser().getUid();
        if(!mCourse1Button.getText().toString().equals("+New")){
            courses += "#" + mCourse1Button.getText().toString();
        }
        if(!mCourse2Button.getText().toString().equals("+New")){
            courses += "#" + mCourse2Button.getText().toString();
        }
        if(!mCourse3Button.getText().toString().equals("+New")){
            courses += "#" + mCourse3Button.getText().toString();
        }
        if(!mCourse4Button.getText().toString().equals("+New")){
            courses += "#" + mCourse4Button.getText().toString();
        }
        System.out.println("Courses: " + courses);
        //If an thumb_image has been chosen and the courses string isn't empty, proceed.
        if((mImageUri != null) && (!courses.equals(""))) {
            File thumbFile = new File(mImageUri.getPath());
            try {
                Bitmap thumbBitmap = new Compressor(this)
                        .setMaxHeight(200)
                        .setMaxWidth(200)
                        .setQuality(75)
                        .compressToBitmap(thumbFile);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] thumbByte = baos.toByteArray();
                mThumbByte = thumbByte;
            } catch (IOException e) {
                e.printStackTrace();
            }
            final String currentUID = mAuth.getCurrentUser().getUid().toString();
            StorageReference filePath = mStorageImage.child(currentUID + ".jpg");
            final StorageReference thumbFilePath = mStorageImage.child("thumb_images").child(currentUID + ".jpg");
            filePath.putFile(mImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        @SuppressWarnings("VisibleForTests")String downloadURL = task.getResult().getDownloadUrl().toString();
                        UploadTask uploadTask = thumbFilePath.putBytes(mThumbByte);
                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                @SuppressWarnings("VisibleForTests")String thumbDownloadUrl = task.getResult().getDownloadUrl().toString();
                                if(task.isSuccessful()){
                                    DatabaseReference current_user_db = mDatabaseUsers.child(currentUID);
                                    current_user_db.child("thumb_image").setValue(thumbDownloadUrl);
                                }
                            }
                        });
                        DatabaseReference current_user_db = mDatabaseUsers.child(currentUID);
                        //Set thumb_image to URL in DB.
                        current_user_db.child("image").setValue(downloadURL).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    //Image uploaded successfully, try upload courses.
                                }
                            }
                        });
                    }else{
                        Toast.makeText(getApplicationContext(), "Failed to upload thumb_image.", Toast.LENGTH_LONG).show();
                    }
                }
            });
            final String coursesString = courses;
            DatabaseReference current_user_db = mDatabaseUsers.child(user_id);
            //Set courses to coursesString.
            current_user_db.child("courses").setValue(coursesString).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        //Transition to MainActivity
                        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                    }else{
                        Toast.makeText(getApplicationContext(), "Failed to upload courses.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else {
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
