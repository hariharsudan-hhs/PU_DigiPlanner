package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nadus.pu_planner.FirebaseAdapters.EventAdapter;
import com.example.nadus.pu_planner.HomeActivity;
import com.example.nadus.pu_planner.ListAdapters.RecyclerViewAdapter_All_Calendar;
import com.example.nadus.pu_planner.ListAdapters.RecyclerViewAdapter_Calendar;
import com.example.nadus.pu_planner.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import am.appwise.components.ni.NoInternetDialog;
import me.anwarshahriar.calligrapher.Calligrapher;


public class Fragment_AllEvents extends Fragment implements RecyclerViewAdapter_All_Calendar.ItemClickListener{

    Calligrapher calligrapher;
    String today_date2="", selected_date2="", selected_day2, today_date_temp2 = "", selected_date_temp2 = "", current_user2;
    TextView current_date2, current_day2;
    CalendarView calendarView2;
    RecyclerView recyclerView2;
    RecyclerViewAdapter_All_Calendar adapter2;

    NoInternetDialog noInternetDialog;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference_allevents, databaseReference_allevents_2;

    List<String> time_list2 = new ArrayList<String>();
    List<String> name_list2 = new ArrayList<String>();
    List<String> description_list2 = new ArrayList<String>();
    List<String> status_list2 = new ArrayList<String>();
    public static List<String> date_list2 = new ArrayList<String>();

    ProgressDialog progressDialog;

    String[] days = new String[] { "","SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY" };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_allevents,container,false);

        HomeActivity.toolbar.setTitle("Academic Calendar");

        progressDialog = new ProgressDialog(getActivity());

        progressDialog.setMessage("Loading...");
        progressDialog.show();


        noInternetDialog = new NoInternetDialog.Builder(getActivity()).setCancelable(true).setBgGradientStart(getResources().getColor(R.color.statusbar_darkblue)).setBgGradientCenter(getResources().getColor(R.color.darkblue)).setBgGradientEnd(getResources().getColor(R.color.darkblue)).setButtonColor(getResources().getColor(R.color.lightgreen)).build();

        current_date2 = (TextView) v.findViewById(R.id.current_date2);
        current_day2 = (TextView) v.findViewById(R.id.current_day2);
        calendarView2 = (CalendarView) v.findViewById(R.id.calendarView2);
        recyclerView2 = (RecyclerView) v.findViewById(R.id.calender_list2);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference_allevents = FirebaseDatabase.getInstance().getReference();
        databaseReference_allevents_2 = FirebaseDatabase.getInstance().getReference();

        today_date2 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        selected_date2 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        current_date2.setText(today_date2);

        calendarView2.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {

//                progressDialog2.setMessage("Loading...");
//                progressDialog2.show();

                progressDialog.show();
                today_date2 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

                time_list2.clear();
                name_list2.clear();
                description_list2.clear();
                status_list2.clear();

                //Toast.makeText(getActivity(),"Selected Date : "+String.format("%02d", dayOfMonth)+"/"+String.format("%02d", month+1)+"/"+year,Toast.LENGTH_SHORT).show();
                selected_date2 = String.format("%02d", dayOfMonth)+"/"+String.format("%02d", month+1)+"/"+year;

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                System.out.println("@@@@ day is "+days[dayOfWeek]);

                selected_day2 = days[dayOfWeek];

                System.out.println("@@@@ today date "+today_date2+" selected date "+selected_date2);

                if(today_date2.equals(selected_date2))
                {
                    current_day2.setText("TODAY");
                    current_date2.setText(selected_date2);
                    today_date_temp2 = today_date2.replace("/","_");
                    today_date_func();
                }
                else
                {
                    current_day2.setText(selected_day2);
                    current_date2.setText(selected_date2);
                    selected_date_temp2 = selected_date2.replace("/","_");
                    selected_date_func();
                }


            }
        });

        time_list2.clear();
        name_list2.clear();
        description_list2.clear();
        status_list2.clear();

        new MyTask_allevents().execute();

        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calligrapher = new Calligrapher(getActivity());
        calligrapher.setFont(getActivity(),"Ubuntu_R.ttf",true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView2.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView2.setLayoutManager(mLayoutManager);
        recyclerView2.setItemAnimator(new DefaultItemAnimator());
        adapter2 = new RecyclerViewAdapter_All_Calendar(getActivity(), time_list2, name_list2, description_list2, status_list2);
        adapter2.setClickListener(this);
        //recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(getActivity(), "You clicked " + adapter2.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    private class MyTask_allevents extends AsyncTask<String, Integer, String>
    {
        @Override
        protected String doInBackground(String... strings) {

            today_date_temp2 = today_date2.replace("/","_");

            databaseReference_allevents = databaseReference_allevents.child("AllEventDiary");
            databaseReference_allevents.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(today_date_temp2))
                    {
                        System.out.println("@@@@ MY TASK today_date_temp "+today_date_temp2);
                        today_date_func();
                    }
                    else
                    {
                        Toast.makeText(getActivity(),"No Event Available!",Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
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

        DatabaseReference databaseReference1_allevents = FirebaseDatabase.getInstance().getReference().child("AllEventDiary").child(today_date_temp2);

        System.out.println("!!!! today_date_fucn() db path is "+databaseReference1_allevents);

        databaseReference1_allevents.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    EventAdapter eventAdapter = dataSnapshot1.getValue(EventAdapter.class);
                    date_list2.add(eventAdapter.getsDatepicker());
                    time_list2.add(eventAdapter.getsTimepicker());
                    name_list2.add(eventAdapter.getsCalendername());
                    description_list2.add(eventAdapter.getsCalenderdescription());
                    status_list2.add("Available");
                    System.out.println("@@@@ event adapter values " + time_list2);
                }
                if(time_list2.isEmpty())
                {
                    Toast.makeText(getActivity(),"No Event Available!",Toast.LENGTH_SHORT).show();
                    recyclerView2.setAdapter(adapter2);
                }
                else
                {
                    recyclerView2.setAdapter(adapter2);
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
        DatabaseReference databaseReference2_allevents = FirebaseDatabase.getInstance().getReference().child("AllEventDiary").child(selected_date_temp2);

        System.out.println("!!!! selected_date_fucn() db path is "+databaseReference2_allevents);

        databaseReference2_allevents.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    EventAdapter eventAdapter = dataSnapshot1.getValue(EventAdapter.class);
                    date_list2.add(eventAdapter.getsDatepicker());
                    time_list2.add(eventAdapter.getsTimepicker());
                    name_list2.add(eventAdapter.getsCalendername());
                    description_list2.add(eventAdapter.getsCalenderdescription());
                    status_list2.add("Available");
                    System.out.println("@@@@ event adapter values " + time_list2);
                }
                if(time_list2.isEmpty())
                {
                    Toast.makeText(getActivity(),"No Event Available!",Toast.LENGTH_SHORT).show();
                    recyclerView2.setAdapter(adapter2);
                }
                else
                {
                    recyclerView2.setAdapter(adapter2);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }

}
