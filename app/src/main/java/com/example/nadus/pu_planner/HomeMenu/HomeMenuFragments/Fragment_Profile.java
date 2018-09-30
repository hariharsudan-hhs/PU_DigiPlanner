package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nadus.pu_planner.FirebaseAdapters.RegisterAdapter;
import com.example.nadus.pu_planner.HomeActivity;
import com.example.nadus.pu_planner.LoginActivity;
import com.example.nadus.pu_planner.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import am.appwise.components.ni.NoInternetDialog;
import me.anwarshahriar.calligrapher.Calligrapher;


public class Fragment_Profile extends Fragment {

    Calligrapher calligrapher;
    TextView about_me_name, about_me_empid, about_me_email, about_me_mobile, about_me_password, about_me_language, about_me_logout, app_status;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference, databaseReference2;

    ProgressDialog progressDialog;

    ImageView password_switch;

    NoInternetDialog noInternetDialog;

    Boolean lol = true;
    private String temp = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile,container,false);

        HomeActivity.toolbar.setTitle("Profile");

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        noInternetDialog = new NoInternetDialog.Builder(getActivity()).setCancelable(true).setBgGradientStart(getResources().getColor(R.color.statusbar_darkblue)).setBgGradientCenter(getResources().getColor(R.color.darkblue)).setBgGradientEnd(getResources().getColor(R.color.darkblue)).setButtonColor(getResources().getColor(R.color.lightgreen)).build();

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference2 = FirebaseDatabase.getInstance().getReference();

        about_me_name = (TextView) v.findViewById(R.id.about_me_name);
        about_me_empid = (TextView) v.findViewById(R.id.about_me_empid);
        about_me_email = (TextView) v.findViewById(R.id.about_me_email);
        about_me_mobile = (TextView) v.findViewById(R.id.about_me_mobile);
        about_me_password = (TextView) v.findViewById(R.id.about_me_password);
        about_me_logout = (TextView) v.findViewById(R.id.about_me_logout);
        app_status = (TextView) v.findViewById(R.id.app_status);


        about_me_password.setTransformationMethod(PasswordTransformationMethod.getInstance());

        password_switch = (ImageView) v.findViewById(R.id.password_switch);

        new MyTask().execute();

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calligrapher = new Calligrapher(getActivity());
        calligrapher.setFont(getActivity(),"Ubuntu_R.ttf",true);

        about_me_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Confirmation");
                builder.setMessage("Are you sure you want to logout?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseAuth.getInstance().signOut();
                        getActivity().finish();
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        password_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lol) {
                    password_switch.setImageResource(R.drawable.ic_action_hide_password);
                    about_me_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    lol = false;
                }
                else {
                    password_switch.setImageResource(R.drawable.ic_action_show_password);
                    about_me_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    lol = true;
                }
            }
        });
    }

    private class MyTask extends AsyncTask<String, Integer, String>
    {
        @Override
        protected String doInBackground(String... strings) {

            String current_user = firebaseAuth.getCurrentUser().getEmail();
            current_user = current_user.replace(".","_");

            databaseReference = databaseReference.child("UserAccounts").child("Staffs").child(current_user).child("Profile");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    RegisterAdapter registerAdapter = dataSnapshot.getValue(RegisterAdapter.class);
                    about_me_name.setText(firebaseAuth.getCurrentUser().getDisplayName());
                    about_me_empid.setText(registerAdapter.getsEmployeeid());
                    about_me_email.setText(registerAdapter.getsEmail());
                    about_me_mobile.setText(registerAdapter.getsMobile());
                    about_me_password.setText(registerAdapter.getsPassword());

                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }
}
