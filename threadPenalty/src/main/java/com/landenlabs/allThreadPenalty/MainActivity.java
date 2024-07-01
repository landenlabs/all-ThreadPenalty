/*
 *  Copyright (c) 2020 Dennis Lang(LanDen Labs) landenlabs@gmail.com
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 *  associated documentation files (the "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 *  following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 *  LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 *  NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *  @author Dennis Lang  (Jan-2020)
 *  @see https://landenlabs.com
 *
 */
package com.landenlabs.allThreadPenalty;

import static com.landenlabs.allThreadPenalty.util.SysUtil.getNavController;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.internal.NavigationMenuView;
import com.google.android.material.navigation.NavigationView;
import com.landenlabs.allThreadPenalty.util.UncaughtExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("FieldCanBeLocal")
public class MainActivity extends AppCompatActivity {

    private androidx.appcompat.widget.Toolbar toolbar;
    private NavController navController;
    private UncaughtExceptionHandler uncaughtExceptionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navController = getNavController(this, R.id.mainNavFragment);
        // navController = Navigation.findNavController(this, R.id.mainNavFragment);

        // Set up ActionBar
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout);

        // Set up navigation menu
        NavigationView navigationView = findViewById(R.id.navigationView);
        @SuppressLint("RestrictedApi")
        NavigationMenuView navMenuView = (NavigationMenuView) navigationView.getChildAt(0);
        navMenuView.addItemDecoration(new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL));
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.post(() -> {
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
            addShortcut(bottomNavigationView);
        });

        String action = getIntent() != null ? getIntent().getAction() : null;
        if (action1.equals(action)) {
            // Show the correct fragment
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        uncaughtExceptionHandler = new UncaughtExceptionHandler(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_side, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        return NavigationUI.navigateUp(
                getNavController(this, R.id.mainNavFragment), drawerLayout);
        // return NavigationUI.navigateUp( Navigation.findNavController(this, R.id.mainNavFragment), drawerLayout);
    }

    private static final String action1= "action1";
    private void addShortcut( BottomNavigationView bottomNavigationView) {

        int menuSize = bottomNavigationView.getMenu().size();
        Map<String, MenuItem> menus = new HashMap<>(menuSize);
        for (int menuIdx = 0; menuIdx < menuSize; menuIdx++) {
            MenuItem menuItem = bottomNavigationView.getMenu().getItem(menuIdx);
            menus.put(menuItem.getTitle().toString(), menuItem);
        }

        ShortcutManager shortcutManager = this.getSystemService(ShortcutManager.class);
        Iterator<NavDestination> navIT =  navController.getGraph().iterator();
        List<ShortcutInfo> shortcutList = new ArrayList<>();
        while (navIT.hasNext()) {
            NavDestination navDestination = navIT.next();
            MenuItem menuItem = menus.get(navDestination.getLabel());
            if (menuItem != null && navDestination.getLabel() != null) {
                Intent newTaskIntent = new Intent(this, MainActivity.class);
                newTaskIntent.setAction(navDestination.getNavigatorName());
                newTaskIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                ShortcutInfo postShortcut
                        = new ShortcutInfo.Builder(this, navDestination.getLabel().toString())
                        .setShortLabel(navDestination.getLabel())
                        .setLongLabel(navDestination.getLabel())
                        .setIcon(Icon.createWithResource(this, R.drawable.logo_small))
                        .setIntent(newTaskIntent)
                        .build();
                shortcutList.add(postShortcut);
            }
        }

        shortcutManager.addDynamicShortcuts(shortcutList);
    }
}
