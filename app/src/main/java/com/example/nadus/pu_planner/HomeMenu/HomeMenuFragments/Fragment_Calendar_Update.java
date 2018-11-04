package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.nadus.pu_planner.FirebaseAdapters.EventAdapter;
import com.example.nadus.pu_planner.FirebaseAdapters.StatusAdapter;
import com.example.nadus.pu_planner.HomeActivity;
import com.example.nadus.pu_planner.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import am.appwise.components.ni.NoInternetDialog;
import me.anwarshahriar.calligrapher.Calligrapher;


@SuppressLint("ValidFragment")
public class Fragment_Calendar_Update extends Fragment {

    Calligrapher calligrapher;

    EditText calendar_event_add_dp, calendar_event_add_tp, calendar_add_name, calendar_add_description;
    static String sCalendarname = "", sCalendardescription = "", sDatepicker = "", sTimepicker = "", mAMPM = "";
    private int mYear, mMonth, mDay, mHour, mMinute;
    Button continue_button;

    int temp_hourOfDay;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    ProgressDialog progressDialog;

    NoInternetDialog noInternetDialog;
    private String status = "";

    @SuppressLint("ValidFragment")
    public Fragment_Calendar_Update(ArrayList<String> mycalendar_list, String date){
        sCalendarname = mycalendar_list.get(0);
        sTimepicker = mycalendar_list.get(3);
        sCalendardescription = mycalendar_list.get(1);
        sDatepicker = date;
    }

    public Fragment_Calendar_Update() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calendar_add,container,false);

        HomeActivity.toolbar.setTitle("Edit an event");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");

        new MyTask_statusCheck().execute();
        noInternetDialog = new NoInternetDialog.Builder(getActivity()).setCancelable(true).setBgGradientStart(getResources().getColor(R.color.statusbar_darkblue)).setBgGradientCenter(getResources().getColor(R.color.darkblue)).setBgGradientEnd(getResources().getColor(R.color.darkblue)).setButtonColor(getResources().getColor(R.color.colorAccent)).build();

        calendar_event_add_dp = (EditText) v.findViewById(R.id.calendar_event_add_dp);
        calendar_event_add_tp = (EditText) v.findViewById(R.id.calendar_event_add_tp);
        calendar_add_name = (EditText) v.findViewById(R.id.calendar_add_name);
        calendar_add_description = (EditText) v.findViewById(R.id.calendar_add_description);
        continue_button = (Button) v.findViewById(R.id.continue_button);

        calendar_event_add_dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                monthOfYear = monthOfYear+1;
                                calendar_event_add_dp.setText(String.format("%02d",dayOfMonth) + "/" + String.format("%02d",monthOfYear) + "/" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        calendar_event_add_tp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Time
                final Calendar c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {

                                if(hourOfDay < 12) {
                                    mAMPM = "AM";
                                } else {
                                    temp_hourOfDay = hourOfDay-12;
                                    mAMPM = "PM";
                                }
                                calendar_event_add_tp.setText(String.format("%02d",hourOfDay) + ":" + String.format("%02d",minute)+ " "+mAMPM);
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
            }
        });



        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calligrapher = new Calligrapher(getActivity());
        calligrapher.setFont(getActivity(),"Ubuntu_R.ttf",true);

        continue_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Warning!");
                builder.setIcon(getResources().getDrawable(R.drawable.ic_timer));
                builder.setMessage("Please click the clock icon in the events page once again to update your reminder for this event.");
                builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.show();
                        new MyTask_deleteEvent().execute();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        calendar_add_name.setText(sCalendarname);
        calendar_add_description.setText(sCalendardescription);
        calendar_event_add_dp.setText(sDatepicker);
        calendar_event_add_tp.setText(sTimepicker);
    }

    private void updateValueToNext()
    {
        new MyTask_editEvent().execute();
    }

    private boolean getValues()
    {
        sCalendarname = calendar_add_name.getText().toString().trim();
        sCalendardescription = calendar_add_description.getText().toString().trim();
        sDatepicker = calendar_event_add_dp.getText().toString().trim();
        sTimepicker = calendar_event_add_tp.getText().toString().trim();

        if(sCalendarname.equals(""))
        {
            Toast.makeText(getActivity(),"Field Empty!",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(sCalendardescription.equals(""))
        {
            Toast.makeText(getActivity(),"Field Empty!",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(sDatepicker.equals(""))
        {
            Toast.makeText(getActivity(),"Field Empty!",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(sTimepicker.equals(""))
        {
            Toast.makeText(getActivity(),"Field Empty!",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void clearAll()
    {
        calendar_add_name.getText().clear();
        calendar_add_description.getText().clear();
        calendar_event_add_dp.getText().clear();
        calendar_event_add_tp.getText().clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }

    private class MyTask_editEvent extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... strings) {

            String current_user = firebaseAuth.getCurrentUser().getEmail();
            current_user = current_user.replace(".","_");

            EventAdapter eventAdapter = new EventAdapter();
            eventAdapter.setsCalendarname(calendar_add_name.getText().toString());
            eventAdapter.setsCalendardescription(calendar_add_description.getText().toString());
            eventAdapter.setsDatepicker(calendar_event_add_dp.getText().toString());
            eventAdapter.setsTimepicker(calendar_event_add_tp.getText().toString());

            String temp_date = sDatepicker.replace("/","_");
            databaseReference.child("UserAccounts").child("Staffs").child(current_user).child("EventsDiary").child(temp_date).child(eventAdapter.getsCalendarname()).setValue(eventAdapter);
            progressDialog.dismiss();

            startActivity(new Intent(getActivity(),HomeActivity.class));

            return null;
        }
    }

    private class MyTask_deleteEvent extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... strings) {

            String current_user = firebaseAuth.getCurrentUser().getEmail();
            current_user = current_user.replace(".","_");

            String temp_date = sDatepicker.replace("/","_");

            DatabaseReference databaseReference_del = FirebaseDatabase.getInstance().getReference().child("UserAccounts").child("Staffs").child(current_user).child("EventsDiary").child(temp_date).child(sCalendarname);
            databaseReference_del.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        dataSnapshot1.getRef().removeValue();
                    }
                    if(getValues()) {
                        updateValueToNext();
                    }
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

}
