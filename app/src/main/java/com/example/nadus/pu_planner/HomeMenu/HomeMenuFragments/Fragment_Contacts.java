package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nadus.pu_planner.FirebaseAdapters.ContactsAdapter;
import com.example.nadus.pu_planner.HomeActivity;
import com.example.nadus.pu_planner.ListAdapters.RecyclerViewAdapter_Contacts;
import com.example.nadus.pu_planner.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import me.anwarshahriar.calligrapher.Calligrapher;


public class Fragment_Contacts extends Fragment implements RecyclerViewAdapter_Contacts.ItemClickListener{

    Calligrapher calligrapher;
    private RecyclerView recyclerView;
    RecyclerViewAdapter_Contacts adapter;

    FloatingActionButton contact_add_fab;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    List<String> contact_list = new ArrayList<String>();
    List<String> empno_list = new ArrayList<String>();

    String local_employeeid;

    ContactsAdapter contactsAdapter;

    public static String current_contact;

    ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contacts,container,false);

        HomeActivity.toolbar.setTitle("Contacts");

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        SharedPreferences pref = getActivity().getSharedPreferences("MyPref", 0); // 0 - for private mode
        local_employeeid = pref.getString("emp_id",null);

        contact_add_fab = (FloatingActionButton) v.findViewById(R.id.contact_add_fab);
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        contact_add_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"Add contact",Toast.LENGTH_SHORT).show();
                getFragmentManager().beginTransaction().replace(R.id.container,new Fragment_Contacts_Add()).addToBackStack(null).commit();
            }
        });

        current_contact = "";
        contact_list.clear();
        new MyTask().execute();

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calligrapher = new Calligrapher(getActivity());
        calligrapher.setFont(getActivity(),"Ubuntu_R.ttf",true);

        // data to populate the RecyclerView with
//        ArrayList<String> contactNames = new ArrayList<>();
//        contactNames.add("Horse");
//        contactNames.add("Cow");
//        contactNames.add("Camel");
//        contactNames.add("Sheep");
//        contactNames.add("Goat");

        // set up the RecyclerView
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new RecyclerViewAdapter_Contacts(getActivity(), contact_list, empno_list);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);



    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(getActivity(), "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
        current_contact = empno_list.get(position);
        System.out.println("@@@@ current contact clicked "+ current_contact);
        getFragmentManager().beginTransaction().replace(R.id.container,new Fragment_Contact_Display()).addToBackStack(null).commit();
    }

    private class MyTask extends AsyncTask<String, Integer, String>
    {

        @Override
        protected String doInBackground(String... strings) {

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            System.out.println("@@@@ current user email is "+firebaseAuth.getCurrentUser().getEmail());
            String current_user = firebaseAuth.getCurrentUser().getEmail();
            current_user = current_user.replace(".","_");

            databaseReference = FirebaseDatabase.getInstance().getReference().child("UserAccounts").child("Staffs").child(current_user).child("ContactsDiary");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                    {
                        System.out.println("@@@@ contacts are "+dataSnapshot1.getKey());
                        String key = dataSnapshot1.getKey();
                        empno_list.add(key);
                        contactsAdapter = dataSnapshot1.getValue(ContactsAdapter.class);
                        System.out.println("@@@@ Name is "+contactsAdapter.getsContact_name());
                        String key2 = contactsAdapter.getsContact_name();
                        contact_list.add(key2);

                    }
                    recyclerView.setAdapter(adapter);
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return null;
        }
    }
}
