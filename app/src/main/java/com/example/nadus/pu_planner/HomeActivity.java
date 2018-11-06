package com.example.nadus.pu_planner;

import android.app.Fragment;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
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
import com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments.Fragment_AllContacts_new;
import com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments.Fragment_AllEvents;
import com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments.Fragment_Calendar;
import com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments.Fragment_Contacts;
import com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments.Fragment_Help;
import com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments.Fragment_Profile;
import com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments.Fragment_PuDocs;
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

    private static final int POS_MYCALENDAR = 0;
    private static final int POS_ALLEVENTS = 1;
    private static final int POS_MYCONTACTS = 2;
    private static final int POS_ALLCONTACTS = 3;
    private static final int POS_PUDOCS = 4;
    private static final int POS_PROFILE = 5;
    private static final int POS_SETTINGS = 6;
    private static final int POS_ABOUT = 8;
    private static final int POS_HELP = 9;

    private String[] screenTitles;
    private Drawable[] screenIcons;

    private SlidingRootNav slidingRootNav;

    public static Toolbar toolbar;

    Calligrapher calligrapher;

    TextView name,email,id;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    public static String mainID = "";

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
                createItemFor(POS_MYCALENDAR).setChecked(true),
                createItemFor(POS_ALLEVENTS),
                createItemFor(POS_MYCONTACTS),
                createItemFor(POS_ALLCONTACTS),
                createItemFor(POS_PUDOCS),
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

        adapter.setSelected(POS_MYCALENDAR);

        name = (TextView) findViewById(R.id.name);
        email = (TextView) findViewById(R.id.email);
        id = (TextView) findViewById(R.id.id);

        String tempMainID[] = firebaseAuth.getCurrentUser().getDisplayName().split("__");
        name.setText(tempMainID[1]);
        email.setText(firebaseAuth.getCurrentUser().getEmail());
        id.setText("Registered as : "+ tempMainID[0]);

        mainID = tempMainID[0];
    }

    @Override
    public void onItemSelected(int position) {

        Fragment selectedScreen = null;

        if(position == POS_MYCALENDAR)
        {
            selectedScreen = new Fragment_Calendar();
        }
        else if(position == POS_ALLEVENTS)
        {
            selectedScreen = new Fragment_AllEvents();
        }
        else if(position == POS_MYCONTACTS)
        {
            selectedScreen = new Fragment_Contacts();
        }
        else if(position == POS_ALLCONTACTS)
        {
            selectedScreen = new Fragment_AllContacts_new();
        }
        else if(position == POS_PUDOCS)
        {
            selectedScreen = new Fragment_PuDocs();
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
                .replace(R.id.container, fragment).addToBackStack(null)
                .commit();
    }

    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(color(R.color.textColorSecondary))
                .withTextTint(color(R.color.textColorPrimary))
                .withSelectedIconTint(color(R.color.colorAccent))
                .withSelectedTextTint(color(R.color.colorAccent));
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

    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
