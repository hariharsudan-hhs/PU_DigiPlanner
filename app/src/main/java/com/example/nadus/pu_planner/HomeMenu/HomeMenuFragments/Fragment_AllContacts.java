package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.app.Fragment;
import android.app.ProgressDialog;
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

import com.example.nadus.pu_planner.FirebaseAdapters.ContactsAdapter;
import com.example.nadus.pu_planner.HomeActivity;
import com.example.nadus.pu_planner.ListAdapters.RecyclerViewAdapter_All_Contacts_Department_1;
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

import am.appwise.components.ni.NoInternetDialog;
import me.anwarshahriar.calligrapher.Calligrapher;


public class Fragment_AllContacts extends Fragment implements RecyclerViewAdapter_All_Contacts_Department_1.ItemClickListener{

    Calligrapher calligrapher;
    private RecyclerView recyclerView;
    RecyclerViewAdapter_All_Contacts_Department_1 adapter;

    FloatingActionButton contact_add_fab;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    List<String> dept_list = new ArrayList<String>();

    public static String current_item_clicked;

    ProgressDialog progressDialog;

    NoInternetDialog noInternetDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_allcontacts,container,false);

        HomeActivity.toolbar.setTitle("PU Contacts");

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        noInternetDialog = new NoInternetDialog.Builder(getActivity()).setCancelable(true).setBgGradientStart(getResources().getColor(R.color.statusbar_darkblue)).setBgGradientCenter(getResources().getColor(R.color.darkblue)).setBgGradientEnd(getResources().getColor(R.color.darkblue)).setButtonColor(getResources().getColor(R.color.colorAccent)).build();

        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view_allcontacts);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        current_item_clicked = "";
        dept_list.clear();
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
        adapter = new RecyclerViewAdapter_All_Contacts_Department_1(getActivity(), dept_list);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);



    }

    @Override
    public void onItemClick(View view, int position) {
        //Toast.makeText(getActivity(), "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
        current_item_clicked = dept_list.get(position);
        System.out.println("@@@@ current contact clicked "+ current_item_clicked);
        getFragmentManager().beginTransaction().replace(R.id.container,new Fragment_AllContacts_2()).addToBackStack(null).commit();
    }

    private class MyTask extends AsyncTask<String, Integer, String>
    {

        @Override
        protected String doInBackground(String... strings) {

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

            databaseReference = FirebaseDatabase.getInstance().getReference().child("AllContactDiary");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                    {
                        System.out.println("@@@@ departments are "+dataSnapshot1.getKey());
                        String key = dataSnapshot1.getKey();
                        dept_list.add(key);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }

}
