package com.example.nadus.pu_planner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nadus.pu_planner.FirebaseAdapters.RegisterAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import am.appwise.components.ni.NoInternetDialog;
import me.anwarshahriar.calligrapher.Calligrapher;

public class LoginActivity extends AppCompatActivity {

    Button login, register;
    Calligrapher calligrapher;
    EditText login_email, login_password;
    String sEmail, sPassword;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    ProgressDialog progressDialog;

    NoInternetDialog noInternetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        calligrapher = new Calligrapher(this);
        calligrapher.setFont(this, "Ubuntu_R.ttf", true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");

        firebaseAuth = FirebaseAuth.getInstance();

        noInternetDialog = new NoInternetDialog.Builder(LoginActivity.this).setCancelable(true).setBgGradientStart(getResources().getColor(R.color.statusbar_darkblue)).setBgGradientCenter(getResources().getColor(R.color.darkblue)).setBgGradientEnd(getResources().getColor(R.color.darkblue)).setButtonColor(getResources().getColor(R.color.colorAccent)).build();

        login = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);
        login_email = (EditText) findViewById(R.id.login_email);
        login_password = (EditText) findViewById(R.id.login_password);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getValues())
                {
                    progressDialog.show();
                    validateCredentials();
                }
                else
                {
                    Toast.makeText(LoginActivity.this,"Incorrect Credentials!",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void validateCredentials()
    {
        firebaseAuth.signInWithEmailAndPassword(sEmail,sPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this,"Login Successful!",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this,HomeActivity.class));
                    finish();
                }
                else
                {
                    Toast.makeText(LoginActivity.this,"Login Failed!",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private boolean getValues()
    {
        sEmail = login_email.getText().toString().trim();
        sPassword = login_password.getText().toString().trim();

        if(!(sEmail.contains("@") && sEmail.contains(".com")))
        {
            Toast.makeText(LoginActivity.this,"Incorrect Email!",Toast.LENGTH_SHORT).show();
            return false;

        }
        else if(sPassword.length()<=8)
        {
            Toast.makeText(LoginActivity.this,"Password less than 8 characters!",Toast.LENGTH_SHORT).show();
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
