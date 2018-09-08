package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nadus.pu_planner.HomeActivity;
import com.example.nadus.pu_planner.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.anwarshahriar.calligrapher.Calligrapher;


public class Fragment_Calendar extends Fragment {

    Calligrapher calligrapher;
    FloatingActionButton calender_add_fab;
    String today_date;
    TextView current_date;
    CalendarView calendarView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calendar,container,false);

        HomeActivity.toolbar.setTitle("Calendar");

        calender_add_fab = (FloatingActionButton) v.findViewById(R.id.calender_add_fab);
        current_date = (TextView) v.findViewById(R.id.current_date);
        calendarView = (CalendarView) v.findViewById(R.id.calendarView);

        calender_add_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"Add event",Toast.LENGTH_SHORT).show();
                getFragmentManager().beginTransaction().replace(R.id.container,new Fragment_Calendar_Add()).addToBackStack(null).commit();
            }
        });

        today_date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        current_date.setText(today_date);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Toast.makeText(getActivity(),"Selected Date : "+dayOfMonth+"/"+month+"/"+year,Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calligrapher = new Calligrapher(getActivity());
        calligrapher.setFont(getActivity(),"Ubuntu_R.ttf",true);
    }
}
