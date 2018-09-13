package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.nadus.pu_planner.FirebaseAdapters.EventAdapter;
import com.example.nadus.pu_planner.HomeActivity;
import com.example.nadus.pu_planner.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import me.anwarshahriar.calligrapher.Calligrapher;


public class Fragment_Calendar_Add extends Fragment {

    Calligrapher calligrapher;

    EditText calendar_event_add_dp, calendar_event_add_tp, calender_add_name, calender_add_description;
    String sCalendername, sCalenderdescription, sDatepicker, sTimepicker;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private String mAMPM;
    Button add_event;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calendar_add,container,false);

        HomeActivity.toolbar.setTitle("Add an event");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");

        calendar_event_add_dp = (EditText) v.findViewById(R.id.calendar_event_add_dp);
        calendar_event_add_tp = (EditText) v.findViewById(R.id.calendar_event_add_tp);
        calender_add_name = (EditText) v.findViewById(R.id.calender_add_name);
        calender_add_description = (EditText) v.findViewById(R.id.calender_add_description);
        add_event = (Button) v.findViewById(R.id.add_event);

        calendar_event_add_dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Date
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
                                    hourOfDay = hourOfDay - 12;
                                    mAMPM = "PM";
                                }

                                calendar_event_add_tp.setText(String.format("%02d",hourOfDay) + ":" + String.format("%02d",minute) + " " + mAMPM);
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

        add_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getValues())
                {
                    progressDialog.show();
                    updateValueinDB();
                    Toast.makeText(getActivity(),"Success!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateValueinDB()
    {
        EventAdapter eventAdapter = new EventAdapter();
        eventAdapter.setsCalendername(sCalendername);
        eventAdapter.setsCalenderdescription(sCalenderdescription);
        eventAdapter.setsDatepicker(sDatepicker);
        eventAdapter.setsTimepicker(sTimepicker);

        String current_user = firebaseAuth.getCurrentUser().getEmail();
        current_user = current_user.replace(".","_");

        sDatepicker = sDatepicker.replace("/","_");
        sTimepicker = sTimepicker.replace(":","_");

        databaseReference.child("UserAccounts").child("Staffs").child(current_user).child("EventsDiary").child(sDatepicker).child(sCalendername).setValue(eventAdapter);
        clearAll();
        progressDialog.dismiss();
    }

    private boolean getValues()
    {
        sCalendername = calender_add_name.getText().toString().trim();
        sCalenderdescription = calender_add_description.getText().toString().trim();
        sDatepicker = calendar_event_add_dp.getText().toString().trim();
        sTimepicker = calendar_event_add_tp.getText().toString().trim();

        if(sCalendername.equals(""))
        {
            Toast.makeText(getActivity(),"Field Empty!",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(sCalenderdescription.equals(""))
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
        calender_add_name.getText().clear();
        calender_add_description.getText().clear();
        calendar_event_add_dp.getText().clear();
        calendar_event_add_tp.getText().clear();
    }
}
