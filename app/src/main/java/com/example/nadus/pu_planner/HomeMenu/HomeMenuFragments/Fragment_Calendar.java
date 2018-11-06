package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
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
import com.example.nadus.pu_planner.FirebaseAdapters.StatusAdapter;
import com.example.nadus.pu_planner.HomeActivity;
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

public class Fragment_Calendar extends Fragment implements RecyclerViewAdapter_Calendar.ItemClickListener{

    Calligrapher calligrapher;
    FloatingActionButton calendar_add_fab;
    String today_date="", selected_date="", selected_day, current_user, status = "";
    TextView current_date, current_day, b_name, b_description, b_datetime, total_events;
    CalendarView calendarView;
    RecyclerView recyclerView;
    RecyclerViewAdapter_Calendar adapter;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    List<ArrayList<String>> mycalendar_list = new ArrayList<ArrayList<String>>();
    ArrayList<String> mycalendarlistdetail;

    ProgressDialog progressDialog, progressDialog2;

    String[] days = new String[] { "","SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY" };

    NoInternetDialog noInternetDialog;
    private String date_temp = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calendar,container,false);

        HomeActivity.toolbar.setTitle("My Calendar");

        new MyTask_statusCheck().execute();
        progressDialog = new ProgressDialog(getActivity());

        progressDialog.setMessage("Loading...");
        progressDialog.show();

        progressDialog2 = new ProgressDialog(getActivity());
        progressDialog2.setMessage("Deleting...");

        noInternetDialog = new NoInternetDialog.Builder(getActivity()).setCancelable(true).setBgGradientStart(getResources().getColor(R.color.statusbar_darkblue)).setBgGradientCenter(getResources().getColor(R.color.darkblue)).setBgGradientEnd(getResources().getColor(R.color.darkblue)).setButtonColor(getResources().getColor(R.color.colorAccent)).build();


        calendar_add_fab = (FloatingActionButton) v.findViewById(R.id.calendar_add_fab);
        current_date = (TextView) v.findViewById(R.id.current_date);
        current_day = (TextView) v.findViewById(R.id.current_day);
        total_events = (TextView) v.findViewById(R.id.total_events);
        calendarView = (CalendarView) v.findViewById(R.id.calendarView);
        recyclerView = (RecyclerView) v.findViewById(R.id.calendar_list);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);

        calendar_add_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                mycalendar_list.clear();

                selected_date = String.format("%02d", dayOfMonth)+"/"+String.format("%02d", month+1)+"/"+year;

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                selected_day = days[dayOfWeek];

                if(today_date.equals(selected_date))
                {
                    current_day.setText("TODAY");
                    current_date.setText(selected_date);
                    date_temp = today_date.replace("/","_");
                    date_func(date_temp);
                }
                else
                {
                    current_day.setText(selected_day);
                    current_date.setText(selected_date);
                    date_temp = selected_date.replace("/","_");
                    date_func(date_temp);
                }


            }
        });

        mycalendar_list.clear();

        date_temp = today_date.replace("/","_");
        date_func(date_temp);

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
        adapter = new RecyclerViewAdapter_Calendar(getActivity(), mycalendar_list);
        adapter.setClickListener(this);
    }

    @Override
    public void onItemClick(View view, int position) {
        View bottom_view = getActivity().getLayoutInflater().inflate(R.layout.event_bottom_sheet, null);
        b_name = (TextView) bottom_view.findViewById(R.id.b_name);
        b_description = (TextView) bottom_view.findViewById(R.id.b_description);
        b_datetime = (TextView) bottom_view.findViewById(R.id.b_datetime);

        b_name.setText(mycalendar_list.get(position).get(0));
        b_description.setText(mycalendar_list.get(position).get(1));
        b_datetime.setText(current_date.getText().toString() + " ("+current_day.getText().toString()+") at "+mycalendar_list.get(position).get(3));
        BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        dialog.setContentView(bottom_view);
        dialog.show();

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
                deleteReminder(mycalendar_list.get(position).get(0), mycalendar_list.get(position).get(1));
                new MyTask_deleteEvent(position, current_date.getText().toString()).execute();
            }
        }).setNegativeButton("Edit Event", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Fragment_Calendar_Update fragment_calendar_update = new Fragment_Calendar_Update(mycalendar_list.get(position), selected_date);
                getFragmentManager().beginTransaction().replace(R.id.container,new Fragment_Calendar_Update()).addToBackStack(null).commit();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void deleteReminder(String name, String description) {
        try{
            Uri CALENDAR_URI = Uri.parse("content://com.android.calendar/events");
            Cursor cursors = getActivity().getContentResolver().query(CALENDAR_URI, null, null, null, null);
            if (cursors.moveToFirst())
            {
                while (cursors.moveToNext())
                {
                    String desc = cursors.getString(cursors.getColumnIndex("description"));
                    String location = cursors.getString(cursors.getColumnIndex("eventLocation"));
                    String title = cursors.getString(cursors.getColumnIndex("title"));
                    // event id
                    String id = cursors.getString(cursors.getColumnIndex("_id"));
                    if ((desc==null) && (location == null))
                    {
                    }
                    else
                    {
                        if (desc.equals(description) && title.equals(title))
                        {
                            Uri uri = ContentUris.withAppendedId(CALENDAR_URI, Integer.parseInt(id));
                            getActivity().getContentResolver().delete(uri, null, null);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Exception: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }


    private void date_func(String date_temp)
    {
        current_user = firebaseAuth.getCurrentUser().getEmail();
        current_user = current_user.replace(".","_");

        databaseReference = FirebaseDatabase.getInstance().getReference().child("UserAccounts").child("Staffs").child(current_user).child("EventsDiary").child(date_temp);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    EventAdapter eventAdapter = dataSnapshot1.getValue(EventAdapter.class);
                    mycalendarlistdetail = new ArrayList<String>();
                    mycalendarlistdetail.add(0,eventAdapter.getsCalendarname());
                    mycalendarlistdetail.add(1,eventAdapter.getsCalendardescription());
                    mycalendarlistdetail.add(2,eventAdapter.getsDatepicker());
                    mycalendarlistdetail.add(3,eventAdapter.getsTimepicker());
                    mycalendarlistdetail.add(4,"Available");
                    mycalendarlistdetail.add(5,checkReminder(mycalendarlistdetail.get(0), mycalendarlistdetail.get(1)));
                    mycalendar_list.add(mycalendarlistdetail);
                }
                if(mycalendar_list.isEmpty())
                {
                    recyclerView.setAdapter(adapter);
                }
                else
                {
                    recyclerView.setAdapter(adapter);
                }
                total_events.setText(mycalendar_list.size()+" event(s) available");
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String checkReminder(String name, String description) {
        String flag = "off";
        try{
            Uri CALENDAR_URI = Uri.parse("content://com.android.calendar/events");
            Cursor cursors = getActivity().getContentResolver().query(CALENDAR_URI, null, null, null, null);
            if (cursors.moveToFirst())
            {
                while (cursors.moveToNext())
                {
                    String desc = cursors.getString(cursors.getColumnIndex("description"));
                    String title = cursors.getString(cursors.getColumnIndex("title"));
                    // event id
                    String id = cursors.getString(cursors.getColumnIndex("_id"));
                    if ((desc==null) && (title == null))
                    {
                    }
                    else
                    {
                        if (desc.equals(description) && title.equals(name))
                        {
                            flag = "on";
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    private class MyTask_deleteEvent extends AsyncTask<String, Integer, String>{

        int position;
        String clicked_date;
        public MyTask_deleteEvent(int position, String date) {
            this.position = position;
            this.clicked_date = date;
        }

        @Override
        protected String doInBackground(String... strings) {

            clicked_date = clicked_date.replace("/", "_");
            DatabaseReference databaseReference_del = FirebaseDatabase.getInstance().getReference().child("UserAccounts").child("Staffs").child(current_user).child("EventsDiary").child(clicked_date).child(mycalendar_list.get(position).get(0));
            databaseReference_del.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        dataSnapshot1.getRef().removeValue();
                    }
                    mycalendar_list.remove(position);
                    adapter.notifyItemRemoved(position);
                    total_events.setText(mycalendar_list.size()+" event(s) available");
                    progressDialog2.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return null;
        }
    }

    private class MyTask_statusCheck extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... strings) {

            FirebaseDatabase.getInstance().getReference().child("Z_ApplicationStatus").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    StatusAdapter statusAdapter = dataSnapshot.getValue(StatusAdapter.class);
                    status = statusAdapter.getStatus();
                    statusCheck(status);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return null;
        }
    }
    private void statusCheck(String status){
        if(status.equals("Inactive")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Status");
            builder.setMessage("We are sorry for the inconvenience caused. Application is "+status+". Please try again after some time.");
            builder.setCancelable(false);
            builder.setPositiveButton("Close App", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }

}
