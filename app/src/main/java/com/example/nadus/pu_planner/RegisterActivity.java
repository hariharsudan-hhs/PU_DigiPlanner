package com.example.nadus.pu_planner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nadus.pu_planner.FirebaseAdapters.RegisterAdapter;
import com.example.nadus.pu_planner.FirebaseAdapters.StatusAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import am.appwise.components.ni.NoInternetDialog;
import me.anwarshahriar.calligrapher.Calligrapher;

public class RegisterActivity extends AppCompatActivity {

    Button register;
    Calligrapher calligrapher;
    EditText register_firstname, register_lastname, register_employeeid, register_mobile, register_email, register_password, confirm_password;
    String sFirstname, sLastname, sEmployeeid, sMobile, sEmail, sPassword, sConfirmpassword;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    NoInternetDialog noInternetDialog;
    ProgressDialog progressDialog;
    private String status = "";
    TextView titletext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        calligrapher = new Calligrapher(this);
        calligrapher.setFont(this, "Ubuntu_R.ttf", true);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        new MyTask_statusCheck().execute();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        noInternetDialog = new NoInternetDialog.Builder(RegisterActivity.this).setCancelable(true).setBgGradientStart(getResources().getColor(R.color.statusbar_darkblue)).setBgGradientCenter(getResources().getColor(R.color.darkblue)).setBgGradientEnd(getResources().getColor(R.color.darkblue)).setButtonColor(getResources().getColor(R.color.colorAccent)).build();
        register = (Button) findViewById(R.id.register);
        register_firstname = (EditText) findViewById(R.id.register_firstname);
        register_lastname = (EditText) findViewById(R.id.register_lastname);
        register_employeeid = (EditText) findViewById(R.id.register_employeeid);
        register_mobile = (EditText) findViewById(R.id.register_mobile);
        register_email = (EditText) findViewById(R.id.register_email);
        register_password = (EditText) findViewById(R.id.register_password);
        confirm_password = (EditText) findViewById(R.id.confirm_password);
        titletext = (TextView) findViewById(R.id.titletext);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getValues())
                {
                    progressDialog.show();
                    progressDialog.setCancelable(false);
                    createAccount();
                }
            }
        });
    }

    private void createAccount()
    {
        firebaseAuth.createUserWithEmailAndPassword(sEmail,sPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    finish();
                    updateValuesinDB();
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this,"Account Created!",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(RegisterActivity.this,"Registration Failed!",Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnSuccessListener(RegisterActivity.this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(sFirstname+" "+sLastname).build();
                user.updateProfile(userProfileChangeRequest);
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });
    }

    private void updateValuesinDB()
    {
        RegisterAdapter registerAdapter = new RegisterAdapter();
        registerAdapter.setsEmail(sEmail);
        registerAdapter.setsFirstname(sFirstname);
        registerAdapter.setsLastname(sLastname);
        registerAdapter.setsMobile(sMobile);
        registerAdapter.setsEmployeeid(sEmployeeid);
        String fEmail = sEmail.replace(".","_");
        System.out.println("@@@@ fEmail is "+fEmail);
        databaseReference.child("UserAccounts").child("Staffs").child(fEmail).child("Profile").setValue(registerAdapter);
    }

    private boolean getValues()
    {
        sFirstname = register_firstname.getText().toString().trim();
        sLastname = register_lastname.getText().toString().trim();
        sEmployeeid = register_employeeid.getText().toString().trim();
        sMobile = register_mobile.getText().toString().trim();
        sEmail = register_email.getText().toString().trim();
        sPassword = register_password.getText().toString().trim();
        sConfirmpassword = confirm_password.getText().toString().trim();

        if(sFirstname.equals(""))
        {
            Toast.makeText(RegisterActivity.this,"Field empty!",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(sLastname.equals(""))
        {
            Toast.makeText(RegisterActivity.this,"Field empty!",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(sEmployeeid.equals(""))
        {
            Toast.makeText(RegisterActivity.this,"Field empty!",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(sMobile.length()!=10)
        {
            Toast.makeText(RegisterActivity.this,"Invalid Mobile Number!",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(sEmail).matches())
        {
            Toast.makeText(RegisterActivity.this,"Incorrect Email!",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(sPassword.length()<=8)
        {
            Toast.makeText(RegisterActivity.this,"Password less than 9 characters!",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!(sPassword.equals(sConfirmpassword))){
            Toast.makeText(RegisterActivity.this,"Passwords do not match!",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
            builder.setTitle("Status");
            builder.setMessage("We are sorry for the inconvenience caused. Application is "+status+". Please try again after some time.");
            builder.setCancelable(false);
            builder.setPositiveButton("Close App", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    RegisterActivity.this.finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }
}
