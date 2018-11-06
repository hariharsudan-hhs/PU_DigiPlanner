package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nadus.pu_planner.FirebaseAdapters.ContactsAdapter;
import com.example.nadus.pu_planner.FirebaseAdapters.StatusAdapter;
import com.example.nadus.pu_planner.HomeActivity;
import com.example.nadus.pu_planner.ListAdapters.RecyclerViewAdapter_All_Contacts_new;
import com.example.nadus.pu_planner.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.List;

import am.appwise.components.ni.NoInternetDialog;
import me.anwarshahriar.calligrapher.Calligrapher;


public class Fragment_AllContacts_new extends Fragment implements RecyclerViewAdapter_All_Contacts_new.ItemClickListener {

    Calligrapher calligrapher;
    private RecyclerView recyclerView;
    RecyclerViewAdapter_All_Contacts_new adapter;
    MaterialSearchBar searchBar;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    List<ArrayList<String>> contact_list = new ArrayList<ArrayList<String>>();
    ArrayList<String> detail_list;
    List<ArrayList<String>> newList;
    TextView contact_card_name, contact_card_empid, contact_card_designation, contact_card_email_1, contact_card_email_2, contact_card_email_3, contact_card_number_1, contact_card_number_2, contact_card_number_3;

    ProgressDialog progressDialog;

    NoInternetDialog noInternetDialog;
    private String status = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_allcontacts,container,false);

        HomeActivity.toolbar.setTitle("PU Contacts");

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        new MyTask_statusCheck().execute();
        noInternetDialog = new NoInternetDialog.Builder(getActivity()).setCancelable(true).setBgGradientStart(getResources().getColor(R.color.statusbar_darkblue)).setBgGradientCenter(getResources().getColor(R.color.darkblue)).setBgGradientEnd(getResources().getColor(R.color.darkblue)).setButtonColor(getResources().getColor(R.color.colorAccent)).build();

        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view_allcontacts);
        searchBar = (MaterialSearchBar) v.findViewById(R.id.searchBar);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);

        contact_list.clear();
        new MyTask().execute();

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calligrapher = new Calligrapher(getActivity());
        calligrapher.setFont(getActivity(),"Ubuntu_R.ttf",true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new RecyclerViewAdapter_All_Contacts_new(getActivity(), contact_list);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().toLowerCase();
                newList = new ArrayList<>();
                for (int i = 0; i < contact_list.size() ; i++) {
                    if (contact_list.get(i).get(2).toLowerCase().contains(input.toLowerCase()) || contact_list.get(i).get(4).toLowerCase().contains(input.toLowerCase()) || contact_list.get(i).get(3).toLowerCase().contains(input.toLowerCase()) || contact_list.get(i).get(1).toLowerCase().contains(input.toLowerCase())) {
                        newList.add(contact_list.get(i));
                    }
                }
                adapter = new RecyclerViewAdapter_All_Contacts_new(getActivity(), newList);
                recyclerView.setAdapter(adapter);
                adapter.setClickListener(new RecyclerViewAdapter_All_Contacts_new.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        contactDialog(newList.get(position));
                    }
                });
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        contactDialog(contact_list.get(position));
    }

    private void contactDialog(ArrayList<String> contact_list){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_contact_view, null);
        contact_card_name = (TextView) v.findViewById(R.id.contact_card_name);
        contact_card_empid = (TextView) v.findViewById(R.id.contact_card_empid);
        contact_card_designation = (TextView) v.findViewById(R.id.contact_card_designation);
        contact_card_email_1 = (TextView) v.findViewById(R.id.contact_card_email_1);
        contact_card_email_2 = (TextView) v.findViewById(R.id.contact_card_email_2);
        contact_card_email_3 = (TextView) v.findViewById(R.id.contact_card_email_3);
        contact_card_number_1 = (TextView) v.findViewById(R.id.contact_card_number_1);
        contact_card_number_2 = (TextView) v.findViewById(R.id.contact_card_number_2);
        contact_card_number_3 = (TextView) v.findViewById(R.id.contact_card_number_3);

        contact_card_name.setText(contact_list.get(2));
        contact_card_empid.setText(contact_list.get(0));
        contact_card_designation.setText(contact_list.get(4));
        contact_card_email_1.setText(contact_list.get(5));
        contact_card_email_2.setText(contact_list.get(6));
        contact_card_email_3.setText(contact_list.get(7));
        contact_card_number_1.setText(contact_list.get(8));
        contact_card_number_2.setText(contact_list.get(9));
        contact_card_number_3.setText(contact_list.get(10));

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
        builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setView(v);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class MyTask extends AsyncTask<String, Integer, String>
    {

        @Override
        protected String doInBackground(String... strings) {

            databaseReference = FirebaseDatabase.getInstance().getReference().child("AllContactDiary");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                    {
                        String tempMainID[] = dataSnapshot1.getKey().split("__");
                        if(HomeActivity.mainID.equals(tempMainID[0]) || tempMainID[0].equals("Both")){
                            ContactsAdapter contactsAdapter = dataSnapshot1.getValue(ContactsAdapter.class);
                            detail_list = new ArrayList<String>();
                            detail_list.add(0,contactsAdapter.getsEmployee_id());
                            detail_list.add(1,contactsAdapter.getsCategory());
                            detail_list.add(2,contactsAdapter.getsContact_name());
                            detail_list.add(3,contactsAdapter.getsDepartment());
                            detail_list.add(4,contactsAdapter.getsDesignation());
                            detail_list.add(5,contactsAdapter.getsEmail_1());
                            detail_list.add(6,contactsAdapter.getsEmail_2());
                            detail_list.add(7,contactsAdapter.getsEmail_3());
                            detail_list.add(8,contactsAdapter.getsNumber_1());
                            detail_list.add(9,contactsAdapter.getsNumber_2());
                            detail_list.add(10,contactsAdapter.getsNumber_3());
                            contact_list.add(detail_list);
                        }
                    }
                    recyclerView.setAdapter(adapter);
                    if(contact_list.isEmpty()){
                        Toast.makeText(getActivity(),"No contacts yet!",Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return null;
        }
    }

    private class MyTask_statusCheck extends AsyncTask<String, Integer, String>{

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
