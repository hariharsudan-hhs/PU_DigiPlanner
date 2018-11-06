package com.example.nadus.pu_planner.FirebaseAdapters;

public class ContactsAdapter {
    public ContactsAdapter() {
    }

    public String sContact_name;
    public String sEmployee_id;
    public String sEmail_1;
    public String sEmail_2;
    public String sEmail_3;
    public String sNumber_1;
    public String sNumber_2;
    public String sNumber_3;
    public String sDesignation;
    public String sCategory;
    public String sDepartment;

    public ContactsAdapter(String sContact_name, String sEmployee_id, String sEmail_1, String sEmail_2, String sEmail_3, String sNumber_1, String sNumber_2, String sNumber_3, String sDesignation, String sCategory, String sDepartment) {
        this.sContact_name = sContact_name;
        this.sEmployee_id = sEmployee_id;
        this.sEmail_1 = sEmail_1;
        this.sEmail_2 = sEmail_2;
        this.sEmail_3 = sEmail_3;
        this.sNumber_1 = sNumber_1;
        this.sNumber_2 = sNumber_2;
        this.sNumber_3 = sNumber_3;
        this.sDesignation = sDesignation;
        this.sCategory = sCategory;
        this.sDepartment = sDepartment;
    }

    public String getsContact_name() {
        return sContact_name;
    }

    public void setsContact_name(String sContact_name) {
        this.sContact_name = sContact_name;
    }

    public String getsEmployee_id() {
        return sEmployee_id;
    }

    public void setsEmployee_id(String sEmployee_id) {
        this.sEmployee_id = sEmployee_id;
    }

    public String getsEmail_1() {
        return sEmail_1;
    }

    public void setsEmail_1(String sEmail_1) {
        this.sEmail_1 = sEmail_1;
    }

    public String getsEmail_2() {
        return sEmail_2;
    }

    public void setsEmail_2(String sEmail_2) {
        this.sEmail_2 = sEmail_2;
    }

    public String getsEmail_3() {
        return sEmail_3;
    }

    public void setsEmail_3(String sEmail_3) {
        this.sEmail_3 = sEmail_3;
    }

    public String getsNumber_1() {
        return sNumber_1;
    }

    public void setsNumber_1(String sNumber_1) {
        this.sNumber_1 = sNumber_1;
    }

    public String getsNumber_2() {
        return sNumber_2;
    }

    public void setsNumber_2(String sNumber_2) {
        this.sNumber_2 = sNumber_2;
    }

    public String getsNumber_3() {
        return sNumber_3;
    }

    public void setsNumber_3(String sNumber_3) {
        this.sNumber_3 = sNumber_3;
    }

    public String getsDesignation() {
        return sDesignation;
    }

    public void setsDesignation(String sDesignation) {
        this.sDesignation = sDesignation;
    }

    public String getsCategory() {
        return sCategory;
    }

    public void setsCategory(String sCategory) {
        this.sCategory = sCategory;
    }

    public String getsDepartment() {
        return sDepartment;
    }

    public void setsDepartment(String sDepartment) {
        this.sDepartment = sDepartment;
    }

    @Override
    public String toString() {
        StringBuffer retBuf = new StringBuffer();
        retBuf.append("Contact Info : Name = ");
        retBuf.append(this.getsContact_name());
        retBuf.append(" , ID = ");
        retBuf.append(this.getsEmployee_id());
        retBuf.append(" , Designation = ");
        retBuf.append(this.getsDesignation());
        retBuf.append(" , Email 1 = ");
        retBuf.append(this.getsEmail_1());
        retBuf.append(" , Email 2 = ");
        retBuf.append(this.getsEmail_2());
        retBuf.append(" , Email 3 = ");
        retBuf.append(this.getsEmail_3());
        retBuf.append(" , Number 1 = ");
        retBuf.append(this.getsNumber_1());
        retBuf.append(" , Number 2 = ");
        retBuf.append(this.getsNumber_2());
        retBuf.append(" , Number 3 = ");
        retBuf.append(this.getsNumber_3());
        return retBuf.toString();
    }

}
