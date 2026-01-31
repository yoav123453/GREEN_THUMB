package com.yoav_s.tashtit.ACTIVITIES;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yoav_s.helper.BitMapHelper;
import com.yoav_s.helper.Global;
import com.yoav_s.helper.LauncherHelper;
import com.yoav_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.yoav_s.tashtit.R;

import java.io.File;
import java.io.IOException;


public class MembersActivity extends BaseActivity {
    private Button btnHideMenu;
    private Button btnHideAnimation;
    private ImageView imgPicture;
    private Button btnTakePreview;
    private Button btnTakePicture;

    private LauncherHelper launcherHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_members);
        setLayout(R.layout.activity_members);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeActivity();
    }

    @Override
    protected void initializeActivity() {
        launcherHelper = new LauncherHelper(this);
        initializeViews();
    }

    @Override
    protected void initializeViews() {
        btnHideMenu      = findViewById(R.id.btnHideMenu);
        btnHideAnimation = findViewById(R.id.btnHideAnimation);

        imgPicture       = findViewById(R.id.imgPicture);
        btnTakePreview   = findViewById(R.id.btnTakePreview);
        btnTakePicture   = findViewById(R.id.btnTakePicture);

        setListeners();
    }

    @Override
    protected void setListeners() {
        btnHideMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnHideMenu.getText().equals("Hide menu")){
                    btnHideMenu.setText("Show menu");
                    setBottomNavigationVisibility(false);
                }
                else{
                    btnHideMenu.setText("Hide menu");
                    setBottomNavigationVisibility(true);
                }
            }
        });

        btnHideAnimation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnHideAnimation.getText().equals("Hide animation")){
                    btnHideAnimation.setText("Show animation");
                    setBottomNavigationVisibility(false, true);
                }
                else{
                    btnHideAnimation.setText("Hide animation");
                    setBottomNavigationVisibility(true, true);
                }
            }
        });

        btnTakePreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launcherHelper.takePhotoWithPermissionCheck(bitmap -> {
                    if (bitmap != null) {
                        // Photo taken successfully
                        imgPicture.setImageBitmap(bitmap);
                        Toast.makeText(MembersActivity.this, "Photo taken successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        // Photo failed or permission denied
                        Toast.makeText(MembersActivity.this, "Photo failed or permission denied", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MembersActivity.this, "Not implemented !", Toast.LENGTH_SHORT).show();

                // 1. Create the file to save the image to
                File photoFile = null;
                try {
                    photoFile = Global.getTempFile(MembersActivity.this);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    // Handle the error appropriately
                    Toast.makeText(MembersActivity.this, "Error occurred while creating the File", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. Get a Uri from the file using FileProvider
                Uri photoURI = FileProvider.getUriForFile(
                        MembersActivity.this,
                        getPackageName() + ".fileprovider",
                        photoFile);


                // 3. Call the takePictureWithPermissionCheck method
                launcherHelper.takePictureWithPermissionCheck(photoURI, uri -> {
                    if (uri != null) {
                        // Image was successfully captured and saved at the URI
                        // 'uri' is the same as 'photoURI' in this case.
                        // You can now display the image or process it further.
                        imgPicture.setImageBitmap(BitMapHelper.getBitmapFromUri(MembersActivity.this, uri, imgPicture.getWidth(), imgPicture.getHeight()));
                    } else {
                        // Image capture failed or permission was denied.
                        // Handle this case, e.g., show a toast message.
                        Toast.makeText(MembersActivity.this, "Image capture failed or permission denied", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    @Override
    protected void setViewModel() {

    }
}