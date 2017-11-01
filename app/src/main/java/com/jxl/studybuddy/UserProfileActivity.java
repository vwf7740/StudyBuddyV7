package com.jxl.studybuddy;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.StringTokenizer;

import id.zelory.compressor.Compressor;

import static android.R.attr.bitmap;

public class UserProfileActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mCurrentUser;
    private FirebaseUser mUser;
    private StorageReference mStorageImage;
    private StorageReference mStorageUserImage;
    private byte[] mThumbByte;
    private TextView mNameTextView;
    private ImageButton mProfilePictureButton;
    private Button mCourse1Button;
    private Button mCourse2Button;
    private Button mCourse3Button;
    private Button mCourse4Button;
    private Button mSubmitButton;
    private Button mRemoveAccountButton;
    private Uri mImageUri = null;
    private ProgressDialog mProgress;
    private static String image;
    private static String coursesDownloaded;
    private static String name;
    private static final int GALLERY_REQUEST_CODE = 1;
    private InputFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.flContent); //Remember this is the FrameLayout area within your base_main.xml
        getLayoutInflater().inflate(R.layout.activity_user_profile, contentFrameLayout);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);
        mUser = mAuth.getCurrentUser();
        mCurrentUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.getUid().toString());
        mStorageImage = FirebaseStorage.getInstance().getReference().child("profile_images");
        mStorageUserImage = FirebaseStorage.getInstance().getReference();
        mProgress = new ProgressDialog(this);
        mNameTextView = (TextView) findViewById(R.id.textView_name_profile);
        mProfilePictureButton = (ImageButton) findViewById(R.id.button_profilePicture_profile);
        mCourse1Button = (Button) findViewById(R.id.button_courseCode1_profile);
        mCourse2Button = (Button) findViewById(R.id.button_courseCode2_profile);
        mCourse3Button = (Button) findViewById(R.id.button_courseCode3_profile);
        mCourse4Button = (Button) findViewById(R.id.button_courseCode4_profile);
        mSubmitButton = (Button) findViewById(R.id.button_submit_profile);
        mRemoveAccountButton = (Button) findViewById(R.id.button_removeAccount_profile);

        mCurrentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("Fetching data from db...");
                if(UserProfileActivity.this.isFinishing()) {
                    mProgress.setMessage("Fetching profile data...");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();
                }
                name = dataSnapshot.child("name").getValue().toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNameTextView.setText(name);
                    }
                });
                String imageUrl = dataSnapshot.child("image").getValue().toString();
                image = imageUrl;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(getApplicationContext())
                                .load(image)
                                .into(mProfilePictureButton);
                    }
                });

                String savedCourses = dataSnapshot.child("courses").getValue().toString();
                coursesDownloaded = savedCourses;
                System.out.println("LOAD Courses:" + savedCourses);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StringTokenizer tk = new StringTokenizer(coursesDownloaded, "#");
                        int count = 1;
                        while(tk.hasMoreTokens()){
                            if(count == 1){
                                mCourse1Button.setText(tk.nextToken());
                            }else if(count == 2){
                                mCourse2Button.setText(tk.nextToken());
                            }else if(count == 3){
                                mCourse3Button.setText(tk.nextToken());
                            }else if(count == 4){
                                mCourse4Button.setText(tk.nextToken());
                            }

                            count++;
                        }
                    }
                });

                mProgress.dismiss();
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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCourse1Button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(UserProfileActivity.this);
                        final EditText courseCodeInput = new EditText(UserProfileActivity.this);
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
                        AlertDialog.Builder alert = new AlertDialog.Builder(UserProfileActivity.this);
                        final EditText courseCodeInput = new EditText(UserProfileActivity.this);
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
                        AlertDialog.Builder alert = new AlertDialog.Builder(UserProfileActivity.this);
                        final EditText courseCodeInput = new EditText(UserProfileActivity.this);
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
                        AlertDialog.Builder alert = new AlertDialog.Builder(UserProfileActivity.this);
                        final EditText courseCodeInput = new EditText(UserProfileActivity.this);
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
            }
        });

        //*********************************************************************************
        //Course code button listeners complete.
        //*********************************************************************************

        //If user hits remove account button, the removeAccount() method is executed.
        mRemoveAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAccount();
            }
        });

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

    //Transitions to RemoveAccountActivity.
    private void removeAccount() {
        Intent removeAccountIntent = new Intent(getApplicationContext(), RemoveAccountActivity.class);
        removeAccountIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(removeAccountIntent);
    }

    //Once the user has chosen a display thumb_image and input at least one course code, this method
    //writes the data to the DB.
    private void setUpAccount() {
        mProgress.setMessage("Saving changes...");
        mProgress.setCanceledOnTouchOutside(false);
        AsyncTask asyncUpload = new AsyncRunner().execute();
    }

    //Uploads new user data if new thumb_image or courses have been input.
    public void uploadData(){
        String courses = gatherCourses();
        //If an thumb_image has been chosen and the courses string isn't empty, proceed.
        if((!image.equals("default")) && (!courses.equals(""))){
            //Upload new thumb_image if a new one has been selected.
            if(mImageUri != null){
                uploadImage();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            final String coursesString = courses;
            //Set courses to coursesString.
            if(!coursesDownloaded.equals(courses)) {
                uploadCourses(coursesString);
            }
            mProgress.dismiss();
            System.out.println("SAVING COMPLETE");
        }else{
            //Do not proceed if Image is still default and/or courses String is empty.
            mProgress.dismiss();
            System.out.println("NO COURSE CODES ENTERED");
            Toast.makeText(UserProfileActivity.this, "You must choose at least one course code", Toast.LENGTH_LONG).show();
        }
    }

    //Runs the long task of data upload in background thread so it doesn't disturb UI/main thread.
    public class AsyncRunner extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected String doInBackground(String... params) {
            uploadData();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            mProgress.dismiss();
        }
    }

    //Uploads new selected thumb_image to db along with a thumbnail version of the thumb_image.
    public void uploadImage(){
        System.out.println("NEW IMAGE");
        final String currentUID = mAuth.getCurrentUser().getUid().toString();
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
                                System.out.println("IMAGE UPLOADED");
                                runOnUiThread(new Runnable(){
                                    @Override
                                    public void run() {
                                        if(mProgress.isShowing())
                                            mProgress.dismiss();
                                    }
                                });
                            }
                        }
                    });
                }else{
                    Toast.makeText(getApplicationContext(), "Failed to upload thumb_image.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //Uploads new selected courses to db.
    public void uploadCourses(String coursesString){
        final String user_id = mAuth.getCurrentUser().getUid().toString();
        System.out.println("NEW COURSES");
        mDatabaseUsers.child(user_id).child("courses").setValue(coursesString).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    System.out.println("COURSES UPLOADED");
                    //Courses uploaded successfully.
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to upload courses.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //Builds a new string of the current course codes input by the user.
    public String gatherCourses(){
        String courses = "";
        //Adds any entered course codes to a String object, separated with a # as a delimeter.
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
        return courses;
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProfilePictureButton.setImageURI(mImageUri);
                        System.out.println("IMAGE SET");
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}

