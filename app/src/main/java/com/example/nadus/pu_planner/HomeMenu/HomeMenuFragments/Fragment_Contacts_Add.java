package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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

import am.appwise.components.ni.NoInternetDialog;
import me.anwarshahriar.calligrapher.Calligrapher;


public class Fragment_Contacts_Add extends Fragment {

    Calligrapher calligrapher;
    AutoCompleteTextView contact_add_designation;

    EditText contact_add_name, contact_add_empolyeeid, contact_add_email_1, contact_add_email_2, contact_add_email_3, contact_add_number_1, contact_add_number_2, contact_add_number_3;
    Button add_contact;
    String sContact_name, sEmployee_id, sEmail_1, sEmail_2, sEmail_3, sNumber_1, sNumber_2, sNumber_3, sDesignation;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    NoInternetDialog noInternetDialog;

    private String status = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contacts_add,container,false);

        HomeActivity.toolbar.setTitle("Add a contact");

        noInternetDialog = new NoInternetDialog.Builder(getActivity()).setCancelable(true).setBgGradientStart(getResources().getColor(R.color.statusbar_darkblue)).setBgGradientCenter(getResources().getColor(R.color.darkblue)).setBgGradientEnd(getResources().getColor(R.color.darkblue)).setButtonColor(getResources().getColor(R.color.colorAccent)).build();

        contact_add_designation = (AutoCompleteTextView) v.findViewById(R.id.contact_add_designation);
        contact_add_name = (EditText) v.findViewById(R.id.contacts_add_name);
        contact_add_empolyeeid = (EditText) v.findViewById(R.id.contacts_add_employeeid);
        contact_add_email_1 = (EditText) v.findViewById(R.id.contacts_add_email_1);
        contact_add_email_2 = (EditText) v.findViewById(R.id.contacts_add_email_2);
        contact_add_email_3 = (EditText) v.findViewById(R.id.contacts_add_email_3);
        contact_add_number_1 = (EditText) v.findViewById(R.id.contacts_add_number_1);
        contact_add_number_2 = (EditText) v.findViewById(R.id.contacts_add_number_2);
        contact_add_number_3 = (EditText) v.findViewById(R.id.contacts_add_number_3);
        contact_add_designation = (AutoCompleteTextView) v.findViewById(R.id.contact_add_designation);
        add_contact = (Button) v.findViewById(R.id.add_contact);


        String[] designation = getResources().getStringArray(R.array.list_of_designation);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,designation);
        contact_add_designation.setAdapter(adapter);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calligrapher = new Calligrapher(getActivity());
        calligrapher.setFont(getActivity(),"Ubuntu_R.ttf",true);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);

        add_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getValues())
                {
                    updateValuesinDB();
                }
            }
        });
    }

    private void updateValuesinDB()
    {
        ContactsAdapter contactsAdapter = new ContactsAdapter();
        contactsAdapter.setsContact_name(sContact_name);
        contactsAdapter.setsEmployee_id(sEmployee_id);
        contactsAdapter.setsEmail_1(sEmail_1);
        contactsAdapter.setsEmail_2(sEmail_2);
        contactsAdapter.setsEmail_3(sEmail_3);
        contactsAdapter.setsNumber_1(sNumber_1);
        contactsAdapter.setsNumber_2(sNumber_2);
        contactsAdapter.setsNumber_3(sNumber_3);
        contactsAdapter.setsDesignation(sDesignation);

        String current_user = firebaseAuth.getCurrentUser().getEmail();
        current_user = current_user.replace(".","_");

        databaseReference.child("UserAccounts").child("Staffs").child(current_user).child("ContactsDiary").child(sEmployee_id).setValue(contactsAdapter);
        Toast.makeText(getActivity(),"Added successfully!",Toast.LENGTH_SHORT).show();
        clearAll();
    }

    private boolean getValues()
    {
        sContact_name = contact_add_name.getText().toString().trim();
        sEmployee_id = contact_add_empolyeeid.getText().toString().trim();
        sEmail_1 = contact_add_email_1.getText().toString().trim();
        sEmail_2 = contact_add_email_2.getText().toString().trim();
        sEmail_3 = contact_add_email_3.getText().toString().trim();
        sNumber_1 = contact_add_number_1.getText().toString().trim();
        sNumber_2 = contact_add_number_2.getText().toString().trim();
        sNumber_3 = contact_add_number_3.getText().toString().trim();
        sDesignation = contact_add_designation.getText().toString().trim();

        if(sContact_name.equals(""))
        {
            Toast.makeText(getActivity(),"Field empty!",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(sEmployee_id.equals(""))
        {
            Toast.makeText(getActivity(),"Field empty!",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(sEmail_1.equals(""))
        {
            Toast.makeText(getActivity(),"Field empty!",Toast.LENGTH_SHORT).show();
            return false;
        }

        if(sEmail_2.equals(""))
        {
            sEmail_2 = "nil";
        }
        else if(sEmail_2.contains("@") && sEmail_2.contains(".com"))
        {}

        else
        {
            Toast.makeText(getActivity(),"Incorrect Email Format!",Toast.LENGTH_SHORT).show();
            return false;
        }

        if(sEmail_3.equals(""))
        {
            sEmail_3 = "nil";
        }
        else if(sEmail_3.contains("@") && sEmail_3.contains(".com"))
        {}

        else
        {
            Toast.makeText(getActivity(),"Incorrect Email Format!",Toast.LENGTH_SHORT).show();
            return false;
        }

        if(sNumber_1.equals(""))
        {
            Toast.makeText(getActivity(),"Field empty!",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(sNumber_2.equals(""))
        {
            sNumber_2 = "nil";
        }

        if(sNumber_3.equals(""))
        {
            sNumber_3 = "nil";
        }

        if(sDesignation.equals(""))
        {
            Toast.makeText(getActivity(),"Field empty!",Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void clearAll()
    {
        contact_add_name.getText().clear();
        contact_add_empolyeeid.getText().clear();
        contact_add_designation.getText().clear();
        contact_add_number_1.getText().clear();
        contact_add_number_2.getText().clear();
        contact_add_number_3.getText().clear();
        contact_add_email_1.getText().clear();
        contact_add_email_2.getText().clear();
        contact_add_email_3.getText().clear();
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
