package com.example.android_group3;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.android_group3.fragment.Change_password_fragment;
import com.example.android_group3.fragment.Favorite_fragment;
import com.example.android_group3.fragment.History_fragment;
import com.example.android_group3.fragment.Home_fragment;
import com.example.android_group3.fragment.My_profile_fragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mapbox.mapboxsdk.Mapbox;

public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener {

    private static final int FRAGMENT_HOME = 0;
    private static final int FRAGMENT_FAVORITE = 1;
    private static final int FRAGMENT_HISTORY = 2;
    private static final int FRAGMENT_MY_PROFILE =3;
    private static final int FRAGMENT_CHANGE_PASSWORD = 4;


    private int mCurrentFragment = FRAGMENT_HOME;

    public TextView tvEmail;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setSupportActionBar(toolbar);

        initUi();

        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        mNavigationView.setNavigationItemSelectedListener(this);

        replaceFragment(new Home_fragment());
        mNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);

        showUserInformation();

    }

    private void initUi(){
        mNavigationView = findViewById(R.id.navigation_view);
        tvEmail = mNavigationView.getHeaderView(0).findViewById(R.id.tv_email);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home){
            if (mCurrentFragment != FRAGMENT_HOME) {
                replaceFragment(new Home_fragment());
                mCurrentFragment = FRAGMENT_HOME;
            }
        }else if (id == R.id.nav_favorite){
            if (mCurrentFragment != FRAGMENT_FAVORITE) {
                replaceFragment(new Favorite_fragment());
                mCurrentFragment = FRAGMENT_FAVORITE;
            }
        }else if (id == R.id.nav_history){
            if (mCurrentFragment != FRAGMENT_HISTORY) {
                replaceFragment(new History_fragment());
                mCurrentFragment = FRAGMENT_HISTORY;
            }
        }else if (id == R.id.nav_my_profile){
            if (mCurrentFragment != FRAGMENT_MY_PROFILE) {
                replaceFragment(new My_profile_fragment());
                mCurrentFragment = FRAGMENT_MY_PROFILE;
            }
        } else if (id == R.id.nav_change_password){
            if (mCurrentFragment != FRAGMENT_CHANGE_PASSWORD) {
                replaceFragment(new Change_password_fragment());
                mCurrentFragment = FRAGMENT_CHANGE_PASSWORD;
            }
        } else if (id == R.id.nav_sign_out){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, SigninActivity.class);
            startActivity(intent);
            finish();
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
    }

    private void showUserInformation(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            return;
        }

        String email = user.getEmail();

        tvEmail.setText(email);
    }
}