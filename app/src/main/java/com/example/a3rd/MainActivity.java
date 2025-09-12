package com.example.a3rd;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.a3rd.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity  {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private DrawerLayout drawer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Set top-level destinations
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_myschedule,R.id.nav_exam_item_page)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Hide toolbar & drawer for login/forgot fragments
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            if (id == R.id.nav_login || id == R.id.nav_forgot) {
                binding.appBarMain.toolbar.setVisibility(View.GONE);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            } else {
                binding.appBarMain.toolbar.setVisibility(View.VISIBLE);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
        });

        // 🔽 Header Views
        View headerView = navigationView.getHeaderView(0);

        LinearLayout headerTop = headerView.findViewById(R.id.nav_header_top);
        ImageView arrow = headerView.findViewById(R.id.nav_header_arrow);
        LinearLayout dropdown = headerView.findViewById(R.id.nav_header_dropdown);
        TextView myProfile = headerView.findViewById(R.id.nav_my_profile);
        TextView history = headerView.findViewById(R.id.nav_history);

        // 🔽 Profile info
        ImageView headerImage = headerView.findViewById(R.id.imageView);
        TextView headerName = headerView.findViewById(R.id.nav_header_name);
        TextView headerSection = headerView.findViewById(R.id.nav_header_section);

        // 🔽 Dropdown toggle logic
        final boolean[] expanded = {false};
        headerTop.setOnClickListener(v -> {
            expanded[0] = !expanded[0];
            dropdown.setVisibility(expanded[0] ? View.VISIBLE : View.GONE);
            arrow.animate().rotation(expanded[0] ? 180f : 0f).setDuration(200).start();
        });

        myProfile.setOnClickListener(v -> {
            navController.navigate(R.id.nav_profile);
        });

        history.setOnClickListener(v -> {
            navController.navigate(R.id.nav_exam_history);
        });

        // 🔽 Load user data from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();

            db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("name");
                    String section = doc.getString("section");
                    String imageUrl = doc.getString("profileImage");

                    headerName.setText(name != null ? name : "No Name");
                    headerSection.setText(section != null ? section : "No Section");

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_person)
                                .into(headerImage);
                    } else {
                        headerImage.setImageResource(R.drawable.ic_person);
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // -----------------------------
        // 1. Notifications Badge
        // -----------------------------
        MenuItem notificationItem = menu.findItem(R.id.action_notifications);
        notificationItem.setActionView(R.layout.notification_badge);

        View actionView = notificationItem.getActionView();
        TextView badgeTextView = actionView.findViewById(R.id.notification_badge);

        int notificationCount = 0; // Example value, replace with your actual count
        if (notificationCount > 0) {
            badgeTextView.setText(String.valueOf(notificationCount));
            badgeTextView.setVisibility(View.VISIBLE);
        } else {
            badgeTextView.setVisibility(View.GONE);
        }

        // -----------------------------
        // 2. Profile Picture in Toolbar
        // -----------------------------
        MenuItem profileItem = menu.findItem(R.id.action_profile);

        // Example: get the photo URL (same as Profile screen)
        String photoUrl = "https://example.com/student_profile.jpg";

        Glide.with(this)
                .asBitmap()
                .load(photoUrl)
                .circleCrop()
                .into(new CustomTarget<Bitmap>(96, 96) { // make smaller if needed
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource,
                                                @Nullable Transition<? super Bitmap> transition) {
                        profileItem.setIcon(new BitmapDrawable(getResources(), resource));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) { }
                });

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            return true;
        } else if (id == R.id.action_notifications) {
            return true;
        } else if (id == R.id.action_logout) {
            finishAffinity();
            System.exit(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}