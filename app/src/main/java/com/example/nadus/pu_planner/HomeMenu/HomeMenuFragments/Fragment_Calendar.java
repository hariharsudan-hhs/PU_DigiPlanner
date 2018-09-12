package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nadus.pu_planner.FirebaseAdapters.EventAdapter;
import com.example.nadus.pu_planner.HomeActivity;
import com.example.nadus.pu_planner.ListAdapters.RecyclerViewAdapter_Calendar;
import com.example.nadus.pu_planner.ListAdapters.RecyclerViewAdapter_Contacts;
import com.example.nadus.pu_planner.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.anwarshahriar.calligrapher.Calligrapher;


public class Fragment_Calendar extends Fragment implements RecyclerViewAdapter_Calendar.ItemClickListener{

    Calligrapher calligrapher;
    FloatingActionButton calender_add_fab;
    String today_date="", selected_date="", selected_day, today_date_temp = "", selected_date_temp = "", current_user;
    TextView current_date, current_day;
    CalendarView calendarView;
    RecyclerView recyclerView;
    RecyclerViewAdapter_Calendar adapter;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference, databaseReference2;

    List<String> time_list = new ArrayList<String>();
    List<String> name_list = new ArrayList<String>();
    List<String> location_list = new ArrayList<String>();
    List<String> status_list = new ArrayList<String>();

    ProgressDialog progressDialog;

    String[] days = new String[] { "","SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY" };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calendar,container,false);

        HomeActivity.toolbar.setTitle("Calendar");

        progressDialog = new ProgressDialog(getActivity());

        progressDialog.setMessage("Loading...");
        progressDialog.show();

        calender_add_fab = (FloatingActionButton) v.findViewById(R.id.calender_add_fab);
        current_date = (TextView) v.findViewById(R.id.current_date);
        current_day = (TextView) v.findViewById(R.id.current_day);
        calendarView = (CalendarView) v.findViewById(R.id.calendarView);
        recyclerView = (RecyclerView) v.findViewById(R.id.calender_list);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference2 = FirebaseDatabase.getInstance().getReference();

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

//                progressDialog2.setMessage("Loading...");
//                progressDialog2.show();

                progressDialog.show();
                today_date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

                time_list.clear();
                name_list.clear();
                location_list.clear();
                status_list.clear();

                //Toast.makeText(getActivity(),"Selected Date : "+String.format("%02d", dayOfMonth)+"/"+String.format("%02d", month+1)+"/"+year,Toast.LENGTH_SHORT).show();
                selected_date = String.format("%02d", dayOfMonth)+"/"+String.format("%02d", month+1)+"/"+year;

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                System.out.println("@@@@ day is "+days[dayOfWeek]);

                selected_day = days[dayOfWeek];

                System.out.println("@@@@ today date "+today_date+" selected date "+selected_date);

                if(today_date.equals(selected_date))
                {
                    current_day.setText("TODAY");
                    current_date.setText(selected_date);
                    today_date_temp = today_date.replace("/","_");
                    today_date_func();
                }
                else
                {
                    current_day.setText(selected_day);
                    current_date.setText(selected_date);
                    selected_date_temp = selected_date.replace("/","_");
                    selected_date_func();
                }


            }
        });

        time_list.clear();
        name_list.clear();
        location_list.clear();
        status_list.clear();

        new MyTask().execute();

        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calligrapher = new Calligrapher(getActivity());
        calligrapher.setFont(getActivity(),"Ubuntu_R.ttf",true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new RecyclerViewAdapter_Calendar(getActivity(), time_list, name_list, location_list, status_list);
        adapter.setClickListener(this);
        //recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(getActivity(), "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    private class MyTask extends AsyncTask<String, Integer, String>
    {
        @Override
        protected String doInBackground(String... strings) {

            current_user = firebaseAuth.getCurrentUser().getEmail();
            current_user = current_user.replace(".","_");
            today_date_temp = today_date.replace("/","_");

            databaseReference = databaseReference.child("UserAccounts").child("Staffs").child(current_user).child("EventsDiary");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(today_date_temp))
                    {
                        System.out.println("@@@@ MY TASK today_date_temp "+today_date_temp);
                        today_date_func();
                    }

                    else if(dataSnapshot.hasChild(selected_date_temp))
                    {
                        System.out.println("@@@@ MY TASK today_date_temp "+selected_date_temp);
                        selected_date_func();
                    }

                    else
                    {
                        Toast.makeText(getActivity(),"MY TASK No Event Available!",Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return null;
        }
    }

    private void today_date_func()
    {

        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("UserAccounts").child("Staffs").child(current_user).child("EventsDiary").child(today_date_temp);

        System.out.println("!!!! today_date_fucn() db path is "+databaseReference1);

        databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    EventAdapter eventAdapter = dataSnapshot1.getValue(EventAdapter.class);
                    time_list.add(eventAdapter.getsTimepicker());
                    name_list.add(eventAdapter.getsCalendername());
                    location_list.add(eventAdapter.getsCalenderlocation());
                    status_list.add("Available");
                    System.out.println("@@@@ event adapter values " + time_list);
                }
                if(time_list.isEmpty())
                {
                    Toast.makeText(getActivity(),"No Event Available!",Toast.LENGTH_SHORT).show();
                    recyclerView.setAdapter(adapter);
                }
                else
                {
                    recyclerView.setAdapter(adapter);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void selected_date_func()
    {
        DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference().child("UserAccounts").child("Staffs").child(current_user).child("EventsDiary").child(selected_date_temp);

        System.out.println("!!!! selected_date_fucn() db path is "+databaseReference2);

        databaseReference2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    EventAdapter eventAdapter = dataSnapshot1.getValue(EventAdapter.class);
                    time_list.add(eventAdapter.getsTimepicker());
                    name_list.add(eventAdapter.getsCalendername());
                    location_list.add(eventAdapter.getsCalenderlocation());
                    status_list.add("Available");
                    System.out.println("@@@@ event adapter values " + time_list);
                }
                if(time_list.isEmpty())
                {
                    Toast.makeText(getActivity(),"No Event Available!",Toast.LENGTH_SHORT).show();
                    recyclerView.setAdapter(adapter);
                }
                else
                {
                    recyclerView.setAdapter(adapter);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
