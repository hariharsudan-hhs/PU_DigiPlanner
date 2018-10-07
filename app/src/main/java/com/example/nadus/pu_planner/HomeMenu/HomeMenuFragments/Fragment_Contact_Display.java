package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nadus.pu_planner.FirebaseAdapters.ContactsAdapter;
import com.example.nadus.pu_planner.HomeActivity;
import com.example.nadus.pu_planner.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

import am.appwise.components.ni.NoInternetDialog;
import me.anwarshahriar.calligrapher.Calligrapher;

import static com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments.Fragment_Contacts.current_contact;


public class Fragment_Contact_Display extends Fragment {

    Calligrapher calligrapher;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    TextView contact_card_name, contact_card_empid, contact_card_designation, contact_card_email_1, contact_card_email_2, contact_card_email_3, contact_card_number_1, contact_card_number_2, contact_card_number_3;
    String current_contact_clicked, contact_call_string;
    ProgressDialog progressDialog;

    NoInternetDialog noInternetDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contact_view,container,false);

        HomeActivity.toolbar.setTitle("Contact Detail");

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        noInternetDialog = new NoInternetDialog.Builder(getActivity()).setCancelable(true).setBgGradientStart(getResources().getColor(R.color.statusbar_darkblue)).setBgGradientCenter(getResources().getColor(R.color.darkblue)).setBgGradientEnd(getResources().getColor(R.color.darkblue)).setButtonColor(getResources().getColor(R.color.colorAccent)).build();

        contact_card_name = (TextView) v.findViewById(R.id.contact_card_name);
        contact_card_empid = (TextView) v.findViewById(R.id.contact_card_empid);
        contact_card_designation = (TextView) v.findViewById(R.id.contact_card_designation);
        contact_card_email_1 = (TextView) v.findViewById(R.id.contact_card_email_1);
        contact_card_email_2 = (TextView) v.findViewById(R.id.contact_card_email_2);
        contact_card_email_3 = (TextView) v.findViewById(R.id.contact_card_email_3);
        contact_card_number_1 = (TextView) v.findViewById(R.id.contact_card_number_1);
        contact_card_number_2 = (TextView) v.findViewById(R.id.contact_card_number_2);
        contact_card_number_3 = (TextView) v.findViewById(R.id.contact_card_number_3);

        current_contact_clicked = current_contact;

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        new MyTask().execute();

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calligrapher = new Calligrapher(getActivity());
        calligrapher.setFont(getActivity(),"Ubuntu_R.ttf",true);

        contact_card_email_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = contact_card_email_1.getText().toString();
                if(temp.equals("nil")){}
                else
                {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",temp, null));
                    getActivity().startActivity(intent);
                }
            }
        });

        contact_card_email_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = contact_card_email_2.getText().toString();
                if(temp.equals("nil")){}
                else
                {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",temp, null));
                    getActivity().startActivity(intent);
                }
            }
        });

        contact_card_email_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = contact_card_email_3.getText().toString();
                if(temp.equals("nil")){}
                else
                {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",temp, null));
                    getActivity().startActivity(intent);
                }
            }
        });

        contact_card_number_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String temp = contact_card_number_1.getText().toString();
                if(temp.equals("nil")){}
                else
                {
                    Intent intent2 = new Intent(Intent.ACTION_DIAL);
                    intent2.setData(Uri.parse("tel:"+temp));
                    getActivity().startActivity(intent2);

//                    Uri uri = Uri.parse("smsto:" + temp);
//                    Intent i = new Intent(Intent.ACTION_SENDTO, uri);
//                    i.setPackage("com.whatsapp");
//                    startActivity(i);
                }
            }
        });

        contact_card_number_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = contact_card_number_2.getText().toString();
                if(temp.equals("nil")){}
                else
                {
                    Intent intent2 = new Intent(Intent.ACTION_DIAL);
                    intent2.setData(Uri.parse("tel:"+temp));
                    getActivity().startActivity(intent2);
                }
            }
        });

        contact_card_number_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = contact_card_number_3.getText().toString();
                if(temp.equals("nil")){}
                else
                {
                    Intent intent2 = new Intent(Intent.ACTION_DIAL);
                    intent2.setData(Uri.parse("tel:"+temp));
                    getActivity().startActivity(intent2);
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
            databaseReference = databaseReference.child("UserAccounts").child("Staffs").child(current_user).child("ContactsDiary").child(current_contact_clicked);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ContactsAdapter contactsAdapter = dataSnapshot.getValue(ContactsAdapter.class);

                    contact_card_name.setText(contactsAdapter.getsContact_name());
                    contact_card_empid.setText(contactsAdapter.getsEmployee_id());
                    contact_card_designation.setText(contactsAdapter.getsDesignation());
                    contact_card_email_1.setText(contactsAdapter.getsEmail_1());
                    contact_card_email_2.setText(contactsAdapter.getsEmail_2());
                    contact_card_email_3.setText(contactsAdapter.getsEmail_3());
                    contact_card_number_1.setText(contactsAdapter.getsNumber_1());
                    contact_card_number_2.setText(contactsAdapter.getsNumber_2());
                    contact_card_number_3.setText(contactsAdapter.getsNumber_3());

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
