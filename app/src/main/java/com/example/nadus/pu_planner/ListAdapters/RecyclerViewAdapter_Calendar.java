package com.example.nadus.pu_planner.ListAdapters;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments.Fragment_Calendar;
import com.example.nadus.pu_planner.R;
import com.github.zagum.switchicon.SwitchIconView;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

public class RecyclerViewAdapter_Calendar extends RecyclerView.Adapter<RecyclerViewAdapter_Calendar.ViewHolder> {

    public List<ArrayList<String>> mData;
    public LayoutInflater mInflater;
    public ItemClickListener mClickListener;
    Fragment_Calendar fragment_calendar;

    Context context;
    private Uri mInsert;

    // data is passed into the constructor
    public RecyclerViewAdapter_Calendar(Context context, List<ArrayList<String>> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.calendar_list_item, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        String time = mData.get(position).get(3);
        holder.calendar_card_time.setText(time);

        final String name = mData.get(position).get(0);
        holder.calendar_card_name.setText(name);

        final String description = mData.get(position).get(1);
        holder.calendar_card_description.setText(description);

        String status = mData.get(position).get(4);
        holder.calendar_card_status.setText(status);

        if(mData.get(position).get(5).equals("on")){
            holder.switchIconView.switchState(true);
        }

        holder.switchIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment_calendar.checkPermission();
                holder.switchIconView.switchState(true);
                if(holder.switchIconView.isIconEnabled()){
//                  add event
                    String temp_date[] = mData.get(position).get(2).split("/");
                    int mDay = Integer.valueOf(temp_date[0]);
                    int mMonth = Integer.valueOf(temp_date[1]);
                    int mYear = Integer.valueOf(temp_date[2]);
                    String temp_time[] = mData.get(position).get(3).split(" ");
                    String temp_time2[] = temp_time[0].split(":");
                    int mHour = Integer.valueOf(temp_time2[0]);
                    int mMinute = Integer.valueOf(temp_time2[1]);
                    GregorianCalendar calDate = new GregorianCalendar(mYear, (mMonth - 1), mDay, mHour, mMinute);
                    Cursor cursor = null;
                    String[] projection = new String[]{
                            CalendarContract.Calendars._ID,
                            CalendarContract.Calendars.ACCOUNT_NAME,};

                    ContentResolver cr = context.getContentResolver();
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
                        values.put(CalendarContract.Events.TITLE, name);
                        values.put(CalendarContract.Events.DESCRIPTION, description);
                        values.put(CalendarContract.Events.CALENDAR_ID, calIds[0]);
                        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
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
                        context.getApplicationContext().getContentResolver()
                                .insert(Uri.parse(reminderUriString), reminders);

                        Toast.makeText(context, "Reminder added!",
                                Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Exception: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
//                  delete event
                    try{
                        Uri CALENDAR_URI = Uri.parse("content://com.android.calendar/events");
                        Cursor cursors = context.getContentResolver().query(CALENDAR_URI, null, null, null, null);
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
                                        context.getContentResolver().delete(uri, null, null);
                                        Toast.makeText(context, "Reminder removed!",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Exception: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView calendar_card_time, calendar_card_name, calendar_card_description, calendar_card_status;
        SwitchIconView switchIconView;

        ViewHolder(View itemView) {
            super(itemView);
            calendar_card_time = itemView.findViewById(R.id.calendar_card_time);
            calendar_card_name = itemView.findViewById(R.id.calendar_card_name);
            calendar_card_description = itemView.findViewById(R.id.calendar_card_description);
            calendar_card_status = itemView.findViewById(R.id.calendar_card_status);
            switchIconView = itemView.findViewById(R.id.switchIconView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            if(mClickListener != null) mClickListener.onLongClick(view, getAdapterPosition());

            return true;
        }

//        @Override
//        public boolean onLongClick(View view) {
//            if(mLongClickListener != null) {
//                mLongClickListener.onItemLongClick(AdapterView.OnItemLongClickListener ,view, getAdapterPosition());
//                return true;
//            }
//        }
    }

//    // convenience method for getting data at click position
//    public String getItem(int id) {
//        return mData.get(id);
//    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
        void onLongClick(View view, int position);
    }
}