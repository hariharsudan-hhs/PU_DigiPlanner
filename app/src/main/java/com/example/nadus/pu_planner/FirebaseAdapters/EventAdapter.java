package com.example.nadus.pu_planner.FirebaseAdapters;

public class EventAdapter {
    public EventAdapter() {
    }

    String sCalendername, sCalenderdescription, sDatepicker, sTimepicker;

    public String getsCalendername() {
        return sCalendername;
    }

    public void setsCalendername(String sCalendername) {
        this.sCalendername = sCalendername;
    }

    public String getsCalenderdescription() {
        return sCalenderdescription;
    }

    public void setsCalenderdescription(String sCalenderdescription) {
        this.sCalenderdescription = sCalenderdescription;
    }

    public String getsDatepicker() {
        return sDatepicker;
    }

    public void setsDatepicker(String sDatepicker) {
        this.sDatepicker = sDatepicker;
    }

    public String getsTimepicker() {
        return sTimepicker;
    }

    public void setsTimepicker(String sTimepicker) {
        this.sTimepicker = sTimepicker;
    }
}
