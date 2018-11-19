package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
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
import com.example.nadus.pu_planner.ListAdapters.RecyclerViewAdapter_All_Calendar;
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

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 222;
    Calligrapher calligrapher;
    String today_date2="", selected_date2="", selected_day2;
    TextView current_date2, current_day2, b_name, b_description, b_datetime, total_events2;
    CalendarView calendarView2;
    RecyclerView recyclerView2;
    RecyclerViewAdapter_All_Calendar adapter2;

    NoInternetDialog noInternetDialog;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference_allevents;

    List<ArrayList<String>> allcalendar_list2 = new ArrayList<ArrayList<String>>();
    ArrayList<String> allcalendarlistdetail;

    ProgressDialog progressDialog;

    String[] days = new String[] { "","SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY" };
    private String status = "";
    private String date_temp2 = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_allevents,container,false);

        HomeActivity.toolbar.setTitle("Academic Calendar");

        progressDialog = new ProgressDialog(getActivity());

        progressDialog.setMessage("Loading...");
        progressDialog.show();

        new MyTask_statusCheck().execute();

        noInternetDialog = new NoInternetDialog.Builder(getActivity()).setCancelable(true).setBgGradientStart(getResources().getColor(R.color.statusbar_darkblue)).setBgGradientCenter(getResources().getColor(R.color.darkblue)).setBgGradientEnd(getResources().getColor(R.color.darkblue)).setButtonColor(getResources().getColor(R.color.colorAccent)).build();

        current_date2 = (TextView) v.findViewById(R.id.current_date2);
        current_day2 = (TextView) v.findViewById(R.id.current_day2);
        total_events2 = (TextView) v.findViewById(R.id.total_events2);
        calendarView2 = (CalendarView) v.findViewById(R.id.calendarView2);
        recyclerView2 = (RecyclerView) v.findViewById(R.id.calendar_list2);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference_allevents = FirebaseDatabase.getInstance().getReference();
        databaseReference_allevents.keepSynced(true);

        today_date2 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        selected_date2 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        current_date2.setText(today_date2);

        calendarView2.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {

                progressDialog.show();
                today_date2 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

                allcalendar_list2.clear();

                selected_date2 = String.format("%02d", dayOfMonth)+"/"+String.format("%02d", month+1)+"/"+year;

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                selected_day2 = days[dayOfWeek];

                if(today_date2.equals(selected_date2))
                {
                    current_day2.setText("TODAY");
                    current_date2.setText(selected_date2);
                    date_temp2 = today_date2.replace("/","_");
                    date_func(date_temp2);
                }
                else
                {
                    current_day2.setText(selected_day2);
                    current_date2.setText(selected_date2);
                    date_temp2 = selected_date2.replace("/","_");
                    date_func(date_temp2);
                }


            }
        });

        allcalendar_list2.clear();

        date_temp2 = today_date2.replace("/","_");
        date_func(date_temp2);

        checkPermission();

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
        adapter2 = new RecyclerViewAdapter_All_Calendar(getActivity(), allcalendar_list2);
        adapter2.setClickListener(this);
    }

    @Override
    public void onItemClick(View view, int position) {
        View bottom_view = getActivity().getLayoutInflater().inflate(R.layout.event_bottom_sheet, null);
        b_name = (TextView) bottom_view.findViewById(R.id.b_name);
        b_description = (TextView) bottom_view.findViewById(R.id.b_description);
        b_datetime = (TextView) bottom_view.findViewById(R.id.b_datetime);

        b_name.setText(allcalendar_list2.get(position).get(0));
        b_description.setText(allcalendar_list2.get(position).get(1));
        b_datetime.setText(current_date2.getText().toString() + " ("+current_day2.getText().toString()+") at "+allcalendar_list2.get(position).get(3));
        BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        dialog.setContentView(bottom_view);
        dialog.show();
    }

    public void checkPermission() {

        int hasCalendarPermission1 = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR);
        int hasCalendarPermission2 = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR);
        if (hasCalendarPermission1 != PackageManager.PERMISSION_GRANTED && hasCalendarPermission2 != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    showMessageOKCancel("You need to allow access to Calendar",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR},
                                                REQUEST_CODE_ASK_PERMISSIONS);
                                    }
                                }
                            });
                    return;
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted

                } else {
                    // Permission Denied
                    Toast.makeText(getActivity(), "Calendar Permission Denied!", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setCancelable(false)
                .create()
                .show();
    }

    private void date_func(String date_temp2)
    {

        databaseReference_allevents = FirebaseDatabase.getInstance().getReference().child("AllEventDiary").child(date_temp2);

        databaseReference_allevents.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    String tempMainID[] = dataSnapshot1.getKey().split("__");
                    if(HomeActivity.mainID.equals(tempMainID[0]) || tempMainID[0].equals("Both")){
                        EventAdapter eventAdapter = dataSnapshot1.getValue(EventAdapter.class);
                        allcalendarlistdetail = new ArrayList<String>();
                        allcalendarlistdetail.add(0,eventAdapter.getsCalendarname());
                        allcalendarlistdetail.add(1,eventAdapter.getsCalendardescription());
                        allcalendarlistdetail.add(2,eventAdapter.getsDatepicker());
                        allcalendarlistdetail.add(3,eventAdapter.getsTimepicker());
                        allcalendarlistdetail.add(4,"Available");
                        allcalendarlistdetail.add(5,checkReminder(allcalendarlistdetail.get(0), allcalendarlistdetail.get(1)));
                        allcalendar_list2.add(allcalendarlistdetail);
                    }
                }
                if(allcalendar_list2.isEmpty())
                {
                    recyclerView2.setAdapter(adapter2);
                }
                else
                {
                    recyclerView2.setAdapter(adapter2);
                }
                total_events2.setText(allcalendar_list2.size()+" event(s) available");
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
            checkPermission();
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
