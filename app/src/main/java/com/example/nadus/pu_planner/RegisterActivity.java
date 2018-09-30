package com.example.nadus.pu_planner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nadus.pu_planner.FirebaseAdapters.CredentialAdapter;
import com.example.nadus.pu_planner.FirebaseAdapters.RegisterAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import am.appwise.components.ni.NoInternetDialog;
import me.anwarshahriar.calligrapher.Calligrapher;

public class RegisterActivity extends AppCompatActivity {

    Button register;
    Calligrapher calligrapher;
    EditText register_firstname, register_lastname, register_employeeid, register_mobile, register_email, register_password;
    String sFirstname, sLastname, sEmployeeid, sMobile, sEmail, sPassword;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference, databaseReference2;

//    SharedPreferences pref;
//    SharedPreferences.Editor editor;

    NoInternetDialog noInternetDialog;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        calligrapher = new Calligrapher(this);
        calligrapher.setFont(this, "Ubuntu_R.ttf", true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        //databaseReference2 = FirebaseDatabase.getInstance().getReference();

        noInternetDialog = new NoInternetDialog.Builder(RegisterActivity.this).setCancelable(true).setBgGradientStart(getResources().getColor(R.color.statusbar_darkblue)).setBgGradientCenter(getResources().getColor(R.color.darkblue)).setBgGradientEnd(getResources().getColor(R.color.darkblue)).setButtonColor(getResources().getColor(R.color.lightgreen)).build();

        register = (Button) findViewById(R.id.register);
        register_firstname = (EditText) findViewById(R.id.register_firstname);
        register_lastname = (EditText) findViewById(R.id.register_lastname);
        register_employeeid = (EditText) findViewById(R.id.register_employeeid);
        register_mobile = (EditText) findViewById(R.id.register_mobile);
        register_email = (EditText) findViewById(R.id.register_email);
        register_password = (EditText) findViewById(R.id.register_password);

//        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE); // 0 - for private mode
//        editor = pref.edit();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getValues())
                {
                    progressDialog.show();
                    progressDialog.setCancelable(false);
                    createAccount();
                }
                else
                {
                    Toast.makeText(RegisterActivity.this,"Registration Failed!",Toast.LENGTH_SHORT).show();
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
                    startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                    finish();
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

            }
        });
    }

    private void updateValuesinDB()
    {
        RegisterAdapter registerAdapter = new RegisterAdapter();
        registerAdapter.setsEmail(sEmail);
        registerAdapter.setsFirstname(sFirstname);
        registerAdapter.setsLastname(sLastname);
        registerAdapter.setsPassword(sPassword);
        registerAdapter.setsMobile(sMobile);
        registerAdapter.setsEmployeeid(sEmployeeid);

//        editor.putString("emp_id", sEmployeeid);
//        editor.commit();

        String fEmail = sEmail.replace(".","_");
        System.out.println("@@@@ fEmail is "+fEmail);

//        CredentialAdapter credentialAdapter = new CredentialAdapter();
//        credentialAdapter.setfEmpno(sEmployeeid);

        databaseReference.child("UserAccounts").child("Staffs").child(fEmail).child("Profile").setValue(registerAdapter);
        //databaseReference2.child("UserAccounts").child("StaffsCredentials").child(fEmail).setValue(credentialAdapter);
    }

    private boolean getValues()
    {
        sFirstname = register_firstname.getText().toString().trim();
        sLastname = register_lastname.getText().toString().trim();
        sEmployeeid = register_employeeid.getText().toString().trim();
        sMobile = register_mobile.getText().toString().trim();
        sEmail = register_email.getText().toString().trim();
        sPassword = register_password.getText().toString().trim();

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
        else if(!(sEmail.contains("@") && sEmail.contains(".com")))
        {
            Toast.makeText(RegisterActivity.this,"Incorrect Email!",Toast.LENGTH_SHORT).show();
            return false;

        }
        else if(sPassword.length()<=8)
        {
            Toast.makeText(RegisterActivity.this,"Password less than 9 characters!",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }
}
