package com.example.nadus.pu_planner.FirebaseAdapters;

public class RegisterAdapter {
    public RegisterAdapter()
    {}

    public String sFirstname, sLastname, sEmployeeid, sMobile, sEmail;

    public String getsFirstname() {
        return sFirstname;
    }

    public void setsFirstname(String sFirstname) {
        this.sFirstname = sFirstname;
    }

    public String getsLastname() {
        return sLastname;
    }

    public void setsLastname(String sLastname) {
        this.sLastname = sLastname;
    }

    public String getsEmployeeid() {
        return sEmployeeid;
    }

    public void setsEmployeeid(String sEmployeeid) {
        this.sEmployeeid = sEmployeeid;
    }

    public String getsMobile() {
        return sMobile;
    }

    public void setsMobile(String sMobile) {
        this.sMobile = sMobile;
    }

    public String getsEmail() {
        return sEmail;
    }

    public void setsEmail(String sEmail) {
        this.sEmail = sEmail;
    }
}
