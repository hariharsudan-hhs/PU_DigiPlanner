package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nadus.pu_planner.FirebaseAdapters.ContactsAdapter;
import com.example.nadus.pu_planner.FirebaseAdapters.StatusAdapter;
import com.example.nadus.pu_planner.HomeActivity;
import com.example.nadus.pu_planner.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import am.appwise.components.ni.NoInternetDialog;
import lib.kingja.switchbutton.SwitchMultiButton;
import me.anwarshahriar.calligrapher.Calligrapher;


public class Fragment_Settings extends Fragment {

    Calligrapher calligrapher;
    NoInternetDialog noInternetDialog;
    TextView settings_pdf_generation, createPdf, note;
    EditText pdf_from_date, pdf_to_date;
    LinearLayout event_layout;
    private String status = "";
    String path = "";
    private File pdfFile;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;
    String pdf_mainID = "";
    private int mYear, mMonth, mDay;
    DatabaseReference pdf_databaseReference;

    List<ArrayList<String>> pdfcontact_list = new ArrayList<ArrayList<String>>();
    ArrayList<String> pdfdetail_list;
    private int loop;
    ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings,container,false);
        path = getActivity().getFilesDir().getAbsolutePath();

        HomeActivity.toolbar.setTitle("Settings");
        new MyTask_statusCheck().execute();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Processing...");

        noInternetDialog = new NoInternetDialog.Builder(getActivity()).setCancelable(true).setBgGradientStart(getResources().getColor(R.color.statusbar_darkblue)).setBgGradientCenter(getResources().getColor(R.color.darkblue)).setBgGradientEnd(getResources().getColor(R.color.darkblue)).setButtonColor(getResources().getColor(R.color.colorAccent)).build();

        settings_pdf_generation = (TextView) v.findViewById(R.id.settings_pdf_generation);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calligrapher = new Calligrapher(getActivity());
        calligrapher.setFont(getActivity(),"Ubuntu_R.ttf",true);

        settings_pdf_generation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               pdf_bottom_dialog();
            }
        });
    }

    private void pdf_bottom_dialog() {
        View bottom_view = getActivity().getLayoutInflater().inflate(R.layout.event_pdfcreation_bottom_sheet, null);
        event_layout = (LinearLayout) bottom_view.findViewById(R.id.event_layout);
        note = (TextView) bottom_view.findViewById(R.id.note);
        pdf_from_date = (EditText) bottom_view.findViewById(R.id.pdf_from_date);
        pdf_to_date = (EditText) bottom_view.findViewById(R.id.pdf_to_date);
        createPdf = (TextView) bottom_view.findViewById(R.id.createPdf);
        event_layout.setVisibility(View.VISIBLE);
        note.setVisibility(View.VISIBLE);
        SwitchMultiButton mSwitchMultiButton = (SwitchMultiButton) bottom_view.findViewById(R.id.switchButton);
        mSwitchMultiButton.setText("My Calendar", "PU Calendar","My Contacts", "PU Contacts").setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
            @Override
            public void onSwitch(int position, String tabText) {
                //Toast.makeText(RegisterActivity.this, tabText, Toast.LENGTH_SHORT).show();
                pdf_mainID = tabText;
                if(pdf_mainID.equals("My Contacts") || pdf_mainID.equals("PU Contacts")){
                    event_layout.setVisibility(View.GONE);
                    note.setVisibility(View.GONE);
                } else if(pdf_mainID.equals("My Calendar") || pdf_mainID.equals("PU Calendar")){
                    event_layout.setVisibility(View.VISIBLE);
                    note.setVisibility(View.VISIBLE);
                }
            }
        });

        pdf_from_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                monthOfYear = monthOfYear+1;
                                pdf_from_date.setText(String.format("%02d",dayOfMonth) + "/" + String.format("%02d",monthOfYear) + "/" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        pdf_to_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                monthOfYear = monthOfYear+1;
                                pdf_to_date.setText(String.format("%02d",dayOfMonth) + "/" + String.format("%02d",monthOfYear) + "/" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        createPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                new MyTask_contacts(pdf_mainID).execute();
            }
        });

        BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        dialog.setContentView(bottom_view);
        dialog.show();
    }

    private class MyTask_contacts extends AsyncTask<String,Integer,String>{

        String PDF_mainID = "";
        public MyTask_contacts(String pdf_mainID) {
            PDF_mainID = pdf_mainID;
        }

        @Override
        protected String doInBackground(String... strings) {

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            String current_user = firebaseAuth.getCurrentUser().getEmail();
            current_user = current_user.replace(".","_");
            if(PDF_mainID.equals("My Contacts")){
                pdf_databaseReference = FirebaseDatabase.getInstance().getReference().child("UserAccounts").child("Staffs").child(current_user).child("ContactsDiary");
            } else if(PDF_mainID.equals("PU Contacts")){
                pdf_databaseReference = FirebaseDatabase.getInstance().getReference().child("AllContactDiary");
            }

            pdf_databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    pdfcontact_list.clear();
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                    {
                        ContactsAdapter contactsAdapter = dataSnapshot1.getValue(ContactsAdapter.class);
                        pdfdetail_list = new ArrayList<String>();
                        pdfdetail_list.add(0,dataSnapshot1.getKey());
                        pdfdetail_list.add(1,contactsAdapter.getsCategory());
                        pdfdetail_list.add(2,contactsAdapter.getsContact_name());
                        pdfdetail_list.add(3,contactsAdapter.getsDepartment());
                        pdfdetail_list.add(4,contactsAdapter.getsDesignation());
                        pdfdetail_list.add(5,contactsAdapter.getsEmail_1());
                        pdfdetail_list.add(6,contactsAdapter.getsEmail_2());
                        pdfdetail_list.add(7,contactsAdapter.getsEmail_3());
                        pdfdetail_list.add(8,contactsAdapter.getsNumber_1());
                        pdfdetail_list.add(9,contactsAdapter.getsNumber_2());
                        pdfdetail_list.add(10,contactsAdapter.getsNumber_3());
                        pdfcontact_list.add(pdfdetail_list);
                    }
                    if(pdfcontact_list.isEmpty()){
                        Toast.makeText(getActivity(),"No contacts yet!",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    } else {
                        try {
                            createPdfWrapper(pdfcontact_list);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (DocumentException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return null;
        }
    }

    private void createPdfWrapper(List<ArrayList<String>> pdfcontact_list) throws FileNotFoundException,DocumentException{

        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    showMessageOKCancel("You need to allow access to Storage",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                REQUEST_CODE_ASK_PERMISSIONS);
                                    }
                                }
                            });
                    return;
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        }else {
            createPdf(pdfcontact_list);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    try {
                        createPdfWrapper(pdfcontact_list);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Permission Denied
                    Toast.makeText(getActivity(), "WRITE_EXTERNAL Permission Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void createPdf(List<ArrayList<String>> pdfcontact_list) throws FileNotFoundException, DocumentException {

        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
            //Log.i(TAG, "Created a new directory for PDF");
        }

        pdfFile = new File(docsFolder.getAbsolutePath(),pdf_mainID+".pdf");
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document();
        PdfWriter.getInstance(document, output);
        document.open();
        document.add(new Paragraph(pdf_mainID+"(s) Information"));
        document.add(new Paragraph(" "));
        for(loop = 0 ; loop < pdfcontact_list.size(); loop++){
            StringBuilder stringBuilder = new StringBuilder("Name : ").append(pdfcontact_list.get(loop).get(2))
                    .append("\nID : ").append(pdfcontact_list.get(loop).get(0))
                    .append("\nDepartment : ").append(pdfcontact_list.get(loop).get(3))
                    .append("\nDesignation : ").append(pdfcontact_list.get(loop).get(4))
                    .append("\nCategory : ").append(pdfcontact_list.get(loop).get(1))
                    .append("\nEmail 1 : ").append(pdfcontact_list.get(loop).get(5))
                    .append("\nEmail 2 : ").append(pdfcontact_list.get(loop).get(6))
                    .append("\nEmail 3 : ").append(pdfcontact_list.get(loop).get(7))
                    .append("\nNumber 1 : ").append(pdfcontact_list.get(loop).get(8))
                    .append("\nNumber 2 : ").append(pdfcontact_list.get(loop).get(9))
                    .append("\nNumber 3 : ").append(pdfcontact_list.get(loop).get(10))
                    .append("\n");
            document.add(new Paragraph(stringBuilder.toString()));
            document.add(new Paragraph(" "));
        }

        document.close();
        displayAlert();
        progressDialog.dismiss();
    }

    private void displayAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Success!");
        builder.setMessage("Your pdf is stored in\nInternal Storage -> Documents -> '\"+pdf_mainID+\".pdf'");
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
    private void statusCheck(String status){
        if(status.equals("Inactive")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Status");
            builder.setMessage("We are sorry for the inconvenience caused. Application is "+status+". Please try again after some time.");
            builder.setCancelable(false);
            builder.setPositiveButton("Close App", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }
}
