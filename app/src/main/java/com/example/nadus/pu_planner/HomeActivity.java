package com.example.nadus.pu_planner;

import android.app.Fragment;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.example.nadus.pu_planner.HomeMenu.DrawerAdapter;
import com.example.nadus.pu_planner.HomeMenu.DrawerItem;
import com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments.Fragment_About;
import com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments.Fragment_Calendar;
import com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments.Fragment_Contacts;
import com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments.Fragment_Help;
import com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments.Fragment_Profile;
import com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments.Fragment_Settings;
import com.example.nadus.pu_planner.HomeMenu.SimpleItem;
import com.example.nadus.pu_planner.HomeMenu.SpaceItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.Arrays;

import me.anwarshahriar.calligrapher.Calligrapher;

public class HomeActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener {

    private static final int POS_CALENDAR = 0;
    private static final int POS_CONTACTS = 1;
    private static final int POS_PROFILE = 2;
    private static final int POS_SETTINGS = 3;
    private static final int POS_ABOUT = 5;
    private static final int POS_HELP = 6;

    private String[] screenTitles;
    private Drawable[] screenIcons;

    private SlidingRootNav slidingRootNav;

    public static Toolbar toolbar;

    Calligrapher calligrapher;

    TextView name,email;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        calligrapher = new Calligrapher(this);
        calligrapher.setFont(this, "Ubuntu_R.ttf", true);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        slidingRootNav = new SlidingRootNavBuilder(this)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.menu_left_drawer)
                .inject();

        screenIcons = loadScreenIcons();
        screenTitles = loadScreenTitles();

        DrawerAdapter adapter = new DrawerAdapter(Arrays.asList(
                createItemFor(POS_CALENDAR).setChecked(true),
                createItemFor(POS_CONTACTS),
                createItemFor(POS_PROFILE),
                createItemFor(POS_SETTINGS),
                new SpaceItem(34),
                createItemFor(POS_ABOUT),
                createItemFor(POS_HELP)));
        adapter.setListener(HomeActivity.this);

        RecyclerView list = findViewById(R.id.list);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        adapter.setSelected(POS_CALENDAR);

        name = (TextView) findViewById(R.id.name);
        email = (TextView) findViewById(R.id.email);

        name.setText(firebaseAuth.getCurrentUser().getDisplayName());
        email.setText(firebaseAuth.getCurrentUser().getEmail());

    }

    @Override
    public void onItemSelected(int position) {

        Fragment selectedScreen = null;

        if(position == POS_CALENDAR)
        {
            selectedScreen = new Fragment_Calendar();
        }
        else if(position == POS_CONTACTS)
        {
            selectedScreen = new Fragment_Contacts();
        }
        else if(position == POS_PROFILE)
        {
            selectedScreen = new Fragment_Profile();
        }
        else if(position == POS_SETTINGS)
        {
            selectedScreen = new Fragment_Settings();
        }
        else if(position == POS_ABOUT)
        {
            selectedScreen = new Fragment_About();
        }
        else if(position == POS_HELP)
        {
            selectedScreen = new Fragment_Help();
        }

        slidingRootNav.closeMenu();
        showFragment(selectedScreen);
    }

    private void showFragment(Fragment fragment) {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(color(R.color.textColorSecondary))
                .withTextTint(color(R.color.textColorPrimary))
                .withSelectedIconTint(color(R.color.lightgreen))
                .withSelectedTextTint(color(R.color.lightgreen));
    }

    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.ld_activityScreenTitles);
    }

    private Drawable[] loadScreenIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.ld_activityScreenIcons);
        Drawable[] icons = new Drawable[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            int id = ta.getResourceId(i, 0);
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        ta.recycle();
        return icons;
    }

    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }

}
