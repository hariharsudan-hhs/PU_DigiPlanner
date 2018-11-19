package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.nadus.pu_planner.FirebaseAdapters.ContactsAdapter;
import com.example.nadus.pu_planner.FirebaseAdapters.DocsAdapter;
import com.example.nadus.pu_planner.FirebaseAdapters.StatusAdapter;
import com.example.nadus.pu_planner.HomeActivity;
import com.example.nadus.pu_planner.ListAdapters.RecyclerViewAdapter_Contacts;
import com.example.nadus.pu_planner.ListAdapters.RecyclerViewAdapter_Pudocs;
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


public class Fragment_PuDocs extends Fragment implements RecyclerViewAdapter_Pudocs.ItemClickListener {

    Calligrapher calligrapher;
    private RecyclerView recyclerView;
    RecyclerViewAdapter_Pudocs adapter;
    NoInternetDialog noInternetDialog;
    private String status = "";
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;
    String current_doc = "";
    DocsAdapter docsAdapter;

    MaterialSearchBar searchBar2;

    List<String> docs_list = new ArrayList<String>();
    List<String> url_list = new ArrayList<String>();

    List<String> new_docs_list = new ArrayList<String>();
    List<String> new_url_list = new ArrayList<String>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pudocs,container,false);

        HomeActivity.toolbar.setTitle("PU Docs");
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);

        noInternetDialog = new NoInternetDialog.Builder(getActivity()).setCancelable(true).setBgGradientStart(getResources().getColor(R.color.statusbar_darkblue)).setBgGradientCenter(getResources().getColor(R.color.darkblue)).setBgGradientEnd(getResources().getColor(R.color.darkblue)).setButtonColor(getResources().getColor(R.color.colorAccent)).build();
        new MyTask_statusCheck().execute();

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);
        searchBar2 = (MaterialSearchBar) v.findViewById(R.id.searchBar2);

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
        adapter = new RecyclerViewAdapter_Pudocs(getActivity(), docs_list);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        searchBar2.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s1, int start1, int count1, int after1) {
            }

            @Override
            public void onTextChanged(CharSequence s1, int start1, int before1, int count1) {
                String input1 = s1.toString().toLowerCase();
                new_docs_list = new ArrayList<>();
                for (int i = 0; i < docs_list.size() ; i++) {
                    if (docs_list.get(i).toLowerCase().contains(input1.toLowerCase()) || docs_list.get(i).toLowerCase().contains(input1.toLowerCase()) || docs_list.get(i).toLowerCase().contains(input1.toLowerCase()) || docs_list.get(i).toLowerCase().contains(input1.toLowerCase())) {
                        new_docs_list.add(docs_list.get(i));
                        new_url_list.add(url_list.get(i));
                    }
                }
                adapter = new RecyclerViewAdapter_Pudocs(getActivity(), new_docs_list);
                recyclerView.setAdapter(adapter);
                adapter.setClickListener(new RecyclerViewAdapter_Pudocs.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Toast.makeText(getActivity(),"Opening "+new_docs_list.get(position),Toast.LENGTH_SHORT).show();
                        openWebPage(new_url_list.get(position));
                    }
                });
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s1) {
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        current_doc = docs_list.get(position);
        Toast.makeText(getActivity(),"Opening "+docs_list.get(position),Toast.LENGTH_SHORT).show();
        openWebPage(url_list.get(position));
    }

    public void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private class MyTask extends AsyncTask<String, Integer, String>
    {
        @Override
        protected String doInBackground(String... strings) {

            databaseReference = FirebaseDatabase.getInstance().getReference().child("CommonDocuments");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                    {
                        String key = dataSnapshot1.getKey();
                        docs_list.add(key);
                        docsAdapter = dataSnapshot1.getValue(DocsAdapter.class);
                        String key2 = docsAdapter.getFileUrl();
                        url_list.add(key2);

                    }
                    recyclerView.setAdapter(adapter);
                    if(docs_list.isEmpty()){
                        Toast.makeText(getActivity(),"No documents yet!",Toast.LENGTH_SHORT).show();
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
