package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
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

import java.util.Calendar;
import java.util.GregorianCalendar;

import am.appwise.components.ni.NoInternetDialog;
import me.anwarshahriar.calligrapher.Calligrapher;


public class Fragment_Calendar_Add_Reminder extends Fragment {

    Calligrapher calligrapher;

    TextView reminder_set_event_name, reminder_set_event_description, reminder_set_event_date, reminder_set_event_time, reminder_set_button;
    String sCalendarname, sCalendardescription, sDatepicker, sTimepicker;
    private int mYear, mMonth,mDay, mHour, mMinute;
    private String mAMPM;
    Button add_event;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    ProgressDialog progressDialog, progressDialog2;

    NoInternetDialog noInternetDialog;

    EventAdapter eventAdapter;
    private String status = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calendar_reminder_set, container, false);

        HomeActivity.toolbar.setTitle("Set Reminder");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        progressDialog = new ProgressDialog(getActivity());
        progressDialog2 = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog2.setMessage("Loading...");
        progressDialog2.show();

        new MyTask_statusCheck().execute();
        noInternetDialog = new NoInternetDialog.Builder(getActivity()).setCancelable(true).setBgGradientStart(getResources().getColor(R.color.statusbar_darkblue)).setBgGradientCenter(getResources().getColor(R.color.darkblue)).setBgGradientEnd(getResources().getColor(R.color.darkblue)).setButtonColor(getResources().getColor(R.color.colorAccent)).build();

        reminder_set_event_name = (TextView) v.findViewById(R.id.reminder_set_event_name);
        reminder_set_event_description = (TextView) v.findViewById(R.id.reminder_set_event_description);
        reminder_set_event_date = (TextView) v.findViewById(R.id.reminder_set_event_date);
        reminder_set_event_time = (TextView) v.findViewById(R.id.reminder_set_event_time);
        reminder_set_button = (TextView) v.findViewById(R.id.reminder_set_button);
        add_event = (Button) v.findViewById(R.id.add_event);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calligrapher = new Calligrapher(getActivity());
        calligrapher.setFont(getActivity(), "Ubuntu_R.ttf", true);

        sCalendarname = Fragment_Calendar_Add.sCalendarname;
        sCalendardescription = Fragment_Calendar_Add.sCalendardescription;
        sDatepicker = Fragment_Calendar_Add.sDatepicker;
        sTimepicker = Fragment_Calendar_Add.sTimepicker;

        showValues();

        add_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Warning!");
                builder.setMessage("Make sure you have set reminder for the event else you will not be notified.");
                builder.setPositiveButton("Yes i've set it", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.show();
                        updateValueinDB();
                        Toast.makeText(getActivity(), "Success!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), HomeActivity.class));
                    }
                }).setNegativeButton("Oops I forgot", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        reminder_set_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra(CalendarContract.Events.TITLE, sCalendarname);
//                intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "Pondicherry");
                intent.putExtra(CalendarContract.Events.DESCRIPTION, sCalendardescription);

                // Setting dates
                GregorianCalendar calDate = new GregorianCalendar(mYear, (mMonth-1), mDay);
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                        calDate.getTimeInMillis());
                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                        calDate.getTimeInMillis());

                // make it a full day event
                intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);

//                // make it a recurring Event
//                intent.putExtra(Events.RRULE, "FREQ=WEEKLY;COUNT=11;WKST=SU;BYDAY=TU,TH");

//                // Making it private and shown as busy
//                intent.putExtra(Events.ACCESS_LEVEL, Events.ACCESS_PRIVATE);
//                intent.putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);

                startActivity(intent);
                reminder_set_button.setTextColor(getResources().getColor(R.color.green));
            }
        });
    }

    private void showValues()
    {
        eventAdapter = new EventAdapter();
        eventAdapter.setsCalendarname(Fragment_Calendar_Add.sCalendarname);
        eventAdapter.setsCalendardescription(Fragment_Calendar_Add.sCalendardescription);
        eventAdapter.setsDatepicker(Fragment_Calendar_Add.sDatepicker);
        eventAdapter.setsTimepicker(Fragment_Calendar_Add.sNormal_time);

        reminder_set_event_name.setText(Fragment_Calendar_Add.sCalendarname);
        reminder_set_event_description.setText(Fragment_Calendar_Add.sCalendardescription);
        reminder_set_event_date.setText(Fragment_Calendar_Add.sDatepicker);
        reminder_set_event_time.setText(Fragment_Calendar_Add.sTimepicker);

        String[] time = sTimepicker.split ( ":" );
        mHour = Integer.parseInt ( time[0].trim() );
        mMinute = Integer.parseInt ( time[1].trim() );

        String[] date = sDatepicker.split("/");
        mDay = Integer.valueOf(date[0].trim());
        mMonth = Integer.valueOf(date[1].trim());
        mYear = Integer.valueOf(date[2].trim());

        progressDialog2.dismiss();
    }

    private void updateValueinDB()
    {
        String current_user = firebaseAuth.getCurrentUser().getEmail();
        current_user = current_user.replace(".","_");

        sDatepicker = sDatepicker.replace("/","_");

        databaseReference.child("UserAccounts").child("Staffs").child(current_user).child("EventsDiary").child(sDatepicker).child(sCalendarname).setValue(eventAdapter);
        progressDialog.dismiss();
    }

    private class MyTask_statusCheck extends AsyncTask<String, Integer, String> {

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
            builder.setMessage("Application is "+status+". Please try after some time. If application inactive for more than 1 hour please contact Admin.");
            builder.setCancelable(false);
            builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
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
