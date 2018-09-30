package com.example.nadus.pu_planner.ListAdapters;

import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments.Fragment_AllEvents;
import com.example.nadus.pu_planner.R;

import java.util.GregorianCalendar;
import java.util.List;

public class RecyclerViewAdapter_All_Calendar extends RecyclerView.Adapter<RecyclerViewAdapter_All_Calendar.ViewHolder> {

    public List<String> mData;
    public List<String> mData2;
    public List<String> mData3;
    public List<String> mData4;
    public LayoutInflater mInflater;
    public ItemClickListener mClickListener;

    Context context;

    // data is passed into the constructor
    public RecyclerViewAdapter_All_Calendar(Context context, List<String> data, List<String> data2, List<String> data3, List<String> data4) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mData2 = data2;
        this.mData3 = data3;
        this.mData4 = data4;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.calender_list_item_2, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        String time = mData.get(position);
        holder.calendar_card_time.setText(time);

        final String name = mData2.get(position);
        holder.calendar_card_name.setText(name);

        final String description = mData3.get(position);
        holder.calendar_card_description.setText(description);

        String status = mData4.get(position);
        holder.calendar_card_status.setText(status);

        holder.set_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra(CalendarContract.Events.TITLE, name);
//                intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "Pondicherry");
                intent.putExtra(CalendarContract.Events.DESCRIPTION, description);

                String temp_date[] = Fragment_AllEvents.date_list2.get(position).split("/");
                int mDay = Integer.valueOf(temp_date[0]);
                int mMonth = Integer.valueOf(temp_date[1]);
                int mYear = Integer.valueOf(temp_date[2]);

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

                context.startActivity(intent);
            }
        });

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView calendar_card_time, calendar_card_name, calendar_card_description, calendar_card_status, set_reminder;

        ViewHolder(View itemView) {
            super(itemView);
            calendar_card_time = itemView.findViewById(R.id.calendar_card_time);
            calendar_card_name = itemView.findViewById(R.id.calendar_card_name);
            calendar_card_description = itemView.findViewById(R.id.calendar_card_description);
            calendar_card_status = itemView.findViewById(R.id.calendar_card_status);
            set_reminder = itemView.findViewById(R.id.set_reminder);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}