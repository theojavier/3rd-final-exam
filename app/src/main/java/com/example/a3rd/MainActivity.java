package com.example.a3rd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.MenuItem;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;

import java.util.Arrays;
import java.util.List;
import android.view.SubMenu;

import com.example.a3rd.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {   // ✅ correct implements

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ✅ Set up toolbar
        setSupportActionBar(binding.appBarMain.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_analytics, R.id.nav_about_us)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // ✅ Listen for nav item clicks
        navigationView.setNavigationItemSelectedListener(this);

        // ✅ Example: Add submenu items dynamically
        Menu menu = navigationView.getMenu();
        MenuItem myClasses = menu.findItem(R.id.nav_my_classes);

        SubMenu subMenu = myClasses.getSubMenu();
        if (subMenu == null) {
            subMenu = menu.addSubMenu(R.id.nav_my_classes, Menu.NONE, Menu.NONE, "Classes");
        }

        List<String> serverClasses = Arrays.asList("Math", "Science", "English", "Exam Schedule");
        subMenu.clear();

        for (int i = 0; i < serverClasses.size(); i++) {
            subMenu.add(R.id.nav_my_classes, Menu.FIRST + i, Menu.NONE, serverClasses.get(i))
                    .setIcon(R.drawable.ic_class_black) // ✅ your class icon
                    .setCheckable(true);
        }

        for (int i = 0; i < subMenu.size(); i++) {
            MenuItem item = subMenu.getItem(i);
            SpannableString spanString = new SpannableString(item.getTitle());
            spanString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spanString.length(), 0);
            item.setTitle(spanString);
        }

        // ✅ Middle logo → go home
        ImageView logo = binding.appBarMain.toolbar.findViewById(R.id.toolbar_logo);
        logo.setOnClickListener(v -> {
            navController.navigate(R.id.nav_home);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem menuItem = menu.findItem(R.id.action_notifications);
        menuItem.setActionView(R.layout.notification_badge);

        View actionView = menuItem.getActionView();
        TextView badgeTextView = actionView.findViewById(R.id.notification_badge);

        int notificationCount = 50; // example value
        if (notificationCount > 0) {
            badgeTextView.setText(String.valueOf(notificationCount));
            badgeTextView.setVisibility(View.VISIBLE);
        } else {
            badgeTextView.setVisibility(View.GONE);
        }

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_myschedule) {
            Intent intent = new Intent(this, ScheduleActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}