package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import com.github.zagum.switchicon.SwitchIconView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import am.appwise.components.ni.NoInternetDialog;
import me.anwarshahriar.calligrapher.Calligrapher;


public class Fragment_Calendar_Add_Reminder extends Fragment {

    Calligrapher calligrapher;

    TextView reminder_set_event_name, reminder_set_event_description, reminder_set_event_date, reminder_set_event_time;
    String sCalendarname, sCalendardescription, sDatepicker, sTimepicker;
    Button add_event;
    SwitchIconView switchIconView_reminder;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    ProgressDialog progressDialog, progressDialog2;

    NoInternetDialog noInternetDialog;

    EventAdapter eventAdapter;
    private String status = "";
    private Uri mInsert;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calendar_reminder_set, container, false);

        HomeActivity.toolbar.setTitle("Set Reminder");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);

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
        switchIconView_reminder = (SwitchIconView) v.findViewById(R.id.switchIconView_reminder);
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
                builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.show();
                        updateValueinDB();
                        Toast.makeText(getActivity(), "Success!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), HomeActivity.class));
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        switchIconView_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchIconView_reminder.switchState(true);
                if(switchIconView_reminder.isIconEnabled()){
//                  add event
                    String temp_date[] = reminder_set_event_date.getText().toString().split("/");
                    int mDay = Integer.valueOf(temp_date[0]);
                    int mMonth = Integer.valueOf(temp_date[1]);
                    int mYear = Integer.valueOf(temp_date[2]);
                    String temp_time[] = reminder_set_event_time.getText().toString().split(" ");
                    String temp_time2[] = temp_time[0].split(":");
                    int mHour = Integer.valueOf(temp_time2[0]);
                    int mMinute = Integer.valueOf(temp_time2[1]);
                    GregorianCalendar calDate = new GregorianCalendar(mYear, (mMonth - 1), mDay, mHour, mMinute);
                    Cursor cursor = null;
                    String[] projection = new String[]{
                            CalendarContract.Calendars._ID,
                            CalendarContract.Calendars.ACCOUNT_NAME,};

                    ContentResolver cr = getActivity().getContentResolver();
                    cursor = cr.query(Uri.parse("content://com.android.calendar/calendars"), projection, null, null, null);

                    int[] calIds = new int[0];
                    if (cursor.moveToFirst()) {
                        final String[] calNames = new String[cursor.getCount()];
                        calIds = new int[cursor.getCount()];
                        for (int i = 0; i < calNames.length; i++) {
                            calIds[i] = cursor.getInt(0);
                            calNames[i] = cursor.getString(1);
                            cursor.moveToNext();
                        }
                    }

                    try {
                        ContentValues values = new ContentValues();
                        values.put(CalendarContract.Events.DTSTART, calDate.getTimeInMillis());
                        values.put(CalendarContract.Events.DTEND, calDate.getTimeInMillis());
                        values.put(CalendarContract.Events.TITLE, reminder_set_event_name.getText().toString().trim());
                        values.put(CalendarContract.Events.DESCRIPTION, reminder_set_event_description.getText().toString().trim());
                        values.put(CalendarContract.Events.CALENDAR_ID, calIds[0]);
                        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        mInsert = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                        long eventID = Long.parseLong(mInsert.getLastPathSegment());

                        int minutes=60;
                        // add reminder for the event
                        ContentValues reminders = new ContentValues();
                        reminders.put("event_id", eventID);
                        reminders.put("method", "1");
                        reminders.put("minutes", minutes);

                        String reminderUriString = "content://com.android.calendar/reminders";
                        getActivity().getApplicationContext().getContentResolver()
                                .insert(Uri.parse(reminderUriString), reminders);

                        Toast.makeText(getActivity(), "Reminder added!",
                                Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Exception: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
//                  delete event
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
                                    if (desc.equals(reminder_set_event_description.getText().toString().trim()) && title.equals(reminder_set_event_name.getText().toString().trim()))
                                    {
                                        Uri uri = ContentUris.withAppendedId(CALENDAR_URI, Integer.parseInt(id));
                                        getActivity().getContentResolver().delete(uri, null, null);
                                        Toast.makeText(getActivity(), "Reminder removed!",
                                                Toast.LENGTH_LONG).show();
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

            }
        });
    }

    private void showValues()
    {
        eventAdapter = new EventAdapter();
        eventAdapter.setsCalendarname(Fragment_Calendar_Add.sCalendarname);
        eventAdapter.setsCalendardescription(Fragment_Calendar_Add.sCalendardescription);
        eventAdapter.setsDatepicker(Fragment_Calendar_Add.sDatepicker);
        eventAdapter.setsTimepicker(Fragment_Calendar_Add.sTimepicker);

        reminder_set_event_name.setText(Fragment_Calendar_Add.sCalendarname);
        reminder_set_event_description.setText(Fragment_Calendar_Add.sCalendardescription);
        reminder_set_event_date.setText(Fragment_Calendar_Add.sDatepicker);
        reminder_set_event_time.setText(Fragment_Calendar_Add.sTimepicker);

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
