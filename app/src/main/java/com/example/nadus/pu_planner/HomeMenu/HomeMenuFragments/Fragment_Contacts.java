package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
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
import com.example.nadus.pu_planner.ListAdapters.RecyclerViewAdapter_Contacts;
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


public class Fragment_Contacts extends Fragment implements RecyclerViewAdapter_Contacts.ItemClickListener{

    Calligrapher calligrapher;
    private RecyclerView recyclerView;
    RecyclerViewAdapter_Contacts adapter2;

    FloatingActionButton contact_add_fab;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    List<ArrayList<String>> mycontact_list = new ArrayList<ArrayList<String>>();
    ArrayList<String> mydetail_list;
    List<ArrayList<String>> mynewList;
    TextView contact_card_name, contact_card_empid, contact_card_designation, contact_card_email_1, contact_card_email_2, contact_card_email_3, contact_card_number_1, contact_card_number_2, contact_card_number_3;

    MaterialSearchBar searchBar2;

    ProgressDialog progressDialog;

    boolean refreshFlag = false;

    NoInternetDialog noInternetDialog;
    private String status = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contacts,container,false);

        HomeActivity.toolbar.setTitle("My Contacts");
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        new MyTask_statusCheck().execute();
        noInternetDialog = new NoInternetDialog.Builder(getActivity()).setCancelable(true).setBgGradientStart(getResources().getColor(R.color.statusbar_darkblue)).setBgGradientCenter(getResources().getColor(R.color.darkblue)).setBgGradientEnd(getResources().getColor(R.color.darkblue)).setButtonColor(getResources().getColor(R.color.colorAccent)).build();

        contact_add_fab = (FloatingActionButton) v.findViewById(R.id.contact_add_fab);
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        searchBar2 = (MaterialSearchBar) v.findViewById(R.id.searchBar2);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);

        contact_add_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.container,new Fragment_Contacts_Add()).commit();
            }
        });

        new MyTask().execute();

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calligrapher = new Calligrapher(getActivity());
        calligrapher.setFont(getActivity(),"Ubuntu_R.ttf",true);

        // set up the RecyclerView
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter2 = new RecyclerViewAdapter_Contacts(getActivity(), mycontact_list);
        adapter2.setClickListener(this);
        recyclerView.setAdapter(adapter2);
        adapter2.notifyDataSetChanged();

        searchBar2.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s1, int start1, int count1, int after1) {
            }

            @Override
            public void onTextChanged(CharSequence s1, int start1, int before1, int count1) {
                String input1 = s1.toString().toLowerCase();
                mynewList = new ArrayList<>();
                for (int i = 0; i < mycontact_list.size() ; i++) {
                    if (mycontact_list.get(i).get(2).toLowerCase().contains(input1.toLowerCase()) || mycontact_list.get(i).get(4).toLowerCase().contains(input1.toLowerCase()) || mycontact_list.get(i).get(0).toLowerCase().contains(input1.toLowerCase()) || mycontact_list.get(i).get(8).toLowerCase().contains(input1.toLowerCase())) {
                        mynewList.add(mycontact_list.get(i));
                    }
                }
                adapter2 = new RecyclerViewAdapter_Contacts(getActivity(), mynewList);
                recyclerView.setAdapter(adapter2);
                adapter2.setClickListener(new RecyclerViewAdapter_Contacts.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        mycontactDialog(mynewList.get(position));
                    }
                });
                adapter2.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s1) {
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        mycontactDialog(mycontact_list.get(position));
    }

    private void mycontactDialog(ArrayList<String> contact_list){
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
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            String current_user = firebaseAuth.getCurrentUser().getEmail();
            current_user = current_user.replace(".","_");

            databaseReference = FirebaseDatabase.getInstance().getReference().child("UserAccounts").child("Staffs").child(current_user).child("ContactsDiary");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mycontact_list.clear();
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                    {
                        ContactsAdapter contactsAdapter = dataSnapshot1.getValue(ContactsAdapter.class);
                        mydetail_list = new ArrayList<String>();
                        mydetail_list.add(0,dataSnapshot1.getKey());
                        mydetail_list.add(1,contactsAdapter.getsCategory());
                        mydetail_list.add(2,contactsAdapter.getsContact_name());
                        mydetail_list.add(3,contactsAdapter.getsDepartment());
                        mydetail_list.add(4,contactsAdapter.getsDesignation());
                        mydetail_list.add(5,contactsAdapter.getsEmail_1());
                        mydetail_list.add(6,contactsAdapter.getsEmail_2());
                        mydetail_list.add(7,contactsAdapter.getsEmail_3());
                        mydetail_list.add(8,contactsAdapter.getsNumber_1());
                        mydetail_list.add(9,contactsAdapter.getsNumber_2());
                        mydetail_list.add(10,contactsAdapter.getsNumber_3());
                        mycontact_list.add(mydetail_list);
                    }
                    Toast.makeText(getActivity(),"Now1",Toast.LENGTH_SHORT).show();
                    recyclerView.setAdapter(adapter2);
                    adapter2.notifyDataSetChanged();
                    if(mycontact_list.isEmpty()){
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
