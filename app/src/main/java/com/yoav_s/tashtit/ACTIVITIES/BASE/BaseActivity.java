package com.yoav_s.tashtit.ACTIVITIES.BASE;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.yoav_s.model.User;
import com.yoav_s.tashtit.ACTIVITIES.MainActivity;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yoav_s.tashtit.ACTIVITIES.MembersActivity;
import com.yoav_s.tashtit.R;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_base);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setMenu();
    }

    public static String updateDate = "09/11/2025";

    protected abstract void initializeActivity();
    protected abstract void initializeViews();
    protected abstract void setListeners();
    protected abstract void setViewModel();
    public static User currentUser = null;

    //region Progress Dialog
    public ProgressDialog mProgressDialog;
    public void showProgressDialog(String title, @NonNull String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            if (title != null)
                mProgressDialog.setTitle(title);
            mProgressDialog.setIcon(R.mipmap.ic_launcher);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.setMessage(message);
            mProgressDialog.show();
        }
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
    //endregion

    //region NAV_BAR
    protected BottomNavigationView bottomNavigationView;

    private void setMenu(){
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home){
                    navigateToActivity(MainActivity.class);
                }
                else if(itemId == R.id.navigation_cities){
                    navigateToActivity(null);
                }
                else if(itemId == R.id.navigation_members){
                    navigateToActivity(MembersActivity.class);
                }
                else{

                }
                return false;
            }
        });
    }

    protected void setSelectedNavigationItem(int itemId) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(itemId);
        }
    }

    private void navigateToActivity(Class<?> activityClass) {
        // Check if we're not already in the target activity
        if (activityClass == null){
            Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show();
        }
        else {
            if (!this.getClass().equals(activityClass)) {
                Intent intent = new Intent(this, activityClass);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        }
    }

    // מטודה להצגת התפריט באקטיביטיס אחים
    protected void setLayout(int layoutId) {
        getLayoutInflater().inflate(layoutId,
                findViewById(R.id.content_frame), true);
    }

    // מטודה להצגה או הסתרה של התפריט
    protected void setBottomNavigationVisibility(boolean isVisible) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(isVisible ? View.VISIBLE :
                    View.GONE);
        }
    }


    // מטודה להצגה או הסתרה של התפריט עם אנימציה
    protected void setBottomNavigationVisibility(boolean isVisible,
                                                 boolean annimation) {
        if (annimation == false)
            setBottomNavigationVisibility(isVisible);
        else {
            if (isVisible) {
                // Slide up animation
                bottomNavigationView.animate()
                        .translationY(0)
                        .setDuration(300)
                        .withStartAction(() ->
                                bottomNavigationView.setVisibility(View.VISIBLE))
                        .start();
            } else {
                // Slide down animation
                bottomNavigationView.animate()
                        .translationY(bottomNavigationView.getHeight())
                        .setDuration(300)
                        .withEndAction(() ->
                                bottomNavigationView.setVisibility(View.GONE))
                        .start();
            }
        }
    }

    //endregion
}