package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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

import am.appwise.components.ni.NoInternetDialog;
import me.anwarshahriar.calligrapher.Calligrapher;


public class Fragment_Calendar extends Fragment implements RecyclerViewAdapter_Calendar.ItemClickListener{

    Calligrapher calligrapher;
    FloatingActionButton calendar_add_fab;
    String today_date="", selected_date="", selected_day, today_date_temp = "", selected_date_temp = "", current_user;
    TextView current_date, current_day;
    CalendarView calendarView;
    RecyclerView recyclerView;
    RecyclerViewAdapter_Calendar adapter;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference, databaseReference2;

    List<String> time_list = new ArrayList<String>();
    List<String> name_list = new ArrayList<String>();
    List<String> description_list = new ArrayList<String>();
    List<String> status_list = new ArrayList<String>();

    ProgressDialog progressDialog, progressDialog2;

    String[] days = new String[] { "","SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY" };

    NoInternetDialog noInternetDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calendar,container,false);

        HomeActivity.toolbar.setTitle("My Calendar");

        progressDialog = new ProgressDialog(getActivity());

        progressDialog.setMessage("Loading...");
        progressDialog.show();

        progressDialog2 = new ProgressDialog(getActivity());

        progressDialog2.setMessage("Deleting...");

        noInternetDialog = new NoInternetDialog.Builder(getActivity()).setCancelable(true).setBgGradientStart(getResources().getColor(R.color.statusbar_darkblue)).setBgGradientCenter(getResources().getColor(R.color.darkblue)).setBgGradientEnd(getResources().getColor(R.color.darkblue)).setButtonColor(getResources().getColor(R.color.colorAccent)).build();


        calendar_add_fab = (FloatingActionButton) v.findViewById(R.id.calendar_add_fab);
        current_date = (TextView) v.findViewById(R.id.current_date);
        current_day = (TextView) v.findViewById(R.id.current_day);
        calendarView = (CalendarView) v.findViewById(R.id.calendarView);
        recyclerView = (RecyclerView) v.findViewById(R.id.calendar_list);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference2 = FirebaseDatabase.getInstance().getReference();

        calendar_add_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"Add event",Toast.LENGTH_SHORT).show();
                getFragmentManager().beginTransaction().replace(R.id.container,new Fragment_Calendar_Add()).addToBackStack(null).commit();
            }
        });

        today_date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        selected_date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        current_date.setText(today_date);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {

                progressDialog.show();
                today_date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

                time_list.clear();
                name_list.clear();
                description_list.clear();
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
        description_list.clear();
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
        adapter = new RecyclerViewAdapter_Calendar(getActivity(), time_list, name_list, description_list, status_list);
        adapter.setClickListener(this);
        //recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
    }

    @Override
    public void onLongClick(View view, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirmation");
        builder.setMessage("What do you want to do?");
        builder.setPositiveButton("Remove Event", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog2.show();
                new MyTask_deleteEvent(position, current_date.getText().toString()).execute();
            }
        }).setNegativeButton("Edit Event", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Fragment_Calendar_Update fragment_calendar_update = new Fragment_Calendar_Update(name_list.get(position), time_list.get(position), description_list.get(position), selected_date);
                getFragmentManager().beginTransaction().replace(R.id.container,new Fragment_Calendar_Update()).addToBackStack(null).commit();
                //new MyTask_editEvent(position, current_date.getText().toString()).execute();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

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
                    else
                    {
                        Toast.makeText(getActivity(),"No Event Available!",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
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
                    name_list.add(eventAdapter.getsCalendarname());
                    description_list.add(eventAdapter.getsCalendardescription());
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
                    name_list.add(eventAdapter.getsCalendarname());
                    description_list.add(eventAdapter.getsCalendardescription());
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

    private class MyTask_deleteEvent extends AsyncTask<String, Integer, String>{

        int position;
        String clicked_date;
        public MyTask_deleteEvent(int position, String date) {
            this.position = position;
            this.clicked_date = date;
            System.out.println("@@@@@ "+name_list.get(position)+ " "+clicked_date);
        }

        @Override
        protected String doInBackground(String... strings) {

            clicked_date = clicked_date.replace("/", "_");
            DatabaseReference databaseReference_del = FirebaseDatabase.getInstance().getReference().child("UserAccounts").child("Staffs").child(current_user).child("EventsDiary").child(clicked_date).child(name_list.get(position));
            System.out.println("^^^^ "+databaseReference_del);
            databaseReference_del.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        System.out.println("%%%% removing " + dataSnapshot1.getKey());
                        dataSnapshot1.getRef().removeValue();
                    }
                    name_list.remove(position);
                    description_list.remove(position);
                    time_list.remove(position);
                    status_list.remove(position);
                    adapter.notifyItemRemoved(position);
                    progressDialog2.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return null;
        }
    }

//    private class MyTask_editEvent extends AsyncTask<String, Integer, String>{
//
//        int position;
//        String clicked_date;
//        public MyTask_editEvent(int position, String date) {
//            this.position = position;
//            this.clicked_date = date;
//            System.out.println("@@@@@ "+name_list.get(position)+ " "+clicked_date);
//        }
//
//        @Override
//        protected String doInBackground(String... strings) {
//
//            clicked_date = clicked_date.replace("/", "_");
//            DatabaseReference databaseReference_edit = FirebaseDatabase.getInstance().getReference().child("UserAccounts").child("Staffs").child(current_user).child("EventsDiary").child(clicked_date).child(name_list.get(position));
//            System.out.println("^^^^ "+databaseReference_edit);
//            databaseReference_edit.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
//                        System.out.println("%%%% editing " + dataSnapshot1.getKey());
//                        EventAdapter eventAdapter = dataSnapshot1.getValue(EventAdapter.class);
//                    }
//                    progressDialog2.dismiss();
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
//
//            return null;
//        }
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }

}
