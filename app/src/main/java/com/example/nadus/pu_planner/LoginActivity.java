package com.example.nadus.pu_planner;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.nadus.pu_planner.FirebaseAdapters.StatusAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import am.appwise.components.ni.NoInternetDialog;
import me.anwarshahriar.calligrapher.Calligrapher;

public class LoginActivity extends AppCompatActivity {

    Button login, register;
    Calligrapher calligrapher;
    EditText login_email, login_password;
    TextView titletext, forgotpassword;
    String sEmail, sPassword, status = "";
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;
    NoInternetDialog noInternetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        calligrapher = new Calligrapher(this);
        calligrapher.setFont(this, "Ubuntu_R.ttf", true);
        new MyTask_statusCheck().execute();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        firebaseAuth = FirebaseAuth.getInstance();
        noInternetDialog = new NoInternetDialog.Builder(LoginActivity.this).setCancelable(true).setBgGradientStart(getResources().getColor(R.color.statusbar_darkblue)).setBgGradientCenter(getResources().getColor(R.color.darkblue)).setBgGradientEnd(getResources().getColor(R.color.darkblue)).setButtonColor(getResources().getColor(R.color.colorAccent)).build();
        login = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);
        login_email = (EditText) findViewById(R.id.login_email);
        login_password = (EditText) findViewById(R.id.login_password);
        titletext = (TextView) findViewById(R.id.titletext);
        forgotpassword = (TextView) findViewById(R.id.forgotpassword);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getValues()) {
                    progressDialog.show();
                    validateCredentials();
                    closeKeyboard();
                } else {
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
        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPassword();
            }
        });
    }

    private boolean sendEmailVerification() {
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        } else {
                            System.out.println("----> sendEmailVerification failed!"+task.getException());
                            Toast.makeText(getApplicationContext(), "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        return user.isEmailVerified();
    }

    private void forgotPassword(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Forgot Password?");
        View v = getLayoutInflater().inflate(R.layout.dialog_forgotpassword, null);
        final EditText resetemail = (EditText) v.findViewById(R.id.resetemail);
        builder.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String sResetemail = resetemail.getText().toString().trim();
                System.out.println("----> email "+sResetemail);
                if (TextUtils.isEmpty(sResetemail)) {
                    Toast.makeText(LoginActivity.this, "Enter your email!", Toast.LENGTH_SHORT).show();
                } else {
                    firebaseAuth.sendPasswordResetEmail(sResetemail)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "Check email to reset your password!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Fail to send reset password email!", Toast.LENGTH_SHORT).show();
                                        System.out.println("-------> onComplete: Failed=" + task.getException().getMessage());
                                    }
                                }
                            });
                }

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setView(v);
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void validateCredentials() {
            firebaseAuth.signInWithEmailAndPassword(sEmail, sPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        if(firebaseAuth.getCurrentUser().isEmailVerified()){
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            progressDialog.dismiss();
                            verifyDialog();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
    }

    private boolean getValues() {
        sEmail = login_email.getText().toString().trim();
        sPassword = login_password.getText().toString().trim();
        if (!(sEmail.contains("@") && (sEmail.contains(".com")) || (sEmail.contains(".co.in")))) {
            Toast.makeText(LoginActivity.this, "Incorrect Email!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (sPassword.length() <= 8) {
            Toast.makeText(LoginActivity.this, "Password less than 8 characters!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void verifyDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Verify email address");
        builder.setMessage("Please check your mail and verify your account creation to login into the application.");
        builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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

    private void statusCheck(String status) {
        progressDialog.dismiss();
        if (status.equals("Active") || status.equals("Under Maintenance")) {
            if (firebaseAuth.getCurrentUser() != null) {
                if(sendEmailVerification()){
                    finish();
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                } else {
                    verifyDialog();
                }
            }
        } else if (status.equals("Inactive")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setTitle("Status");
            builder.setMessage("We are sorry for the inconvenience caused. Application is " + status + ". Please try again after some time.");
            builder.setCancelable(false);
            builder.setPositiveButton("Close App", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    LoginActivity.this.finish();
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

    private void closeKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
}