package com.example.nadus.pu_planner.HomeMenu.HomeMenuFragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nadus.pu_planner.HomeActivity;
import com.example.nadus.pu_planner.R;

import me.anwarshahriar.calligrapher.Calligrapher;


public class Fragment_Help extends Fragment {

    Calligrapher calligrapher;

    TextView help_faq, help_contactus, help_policy, help_appinfo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_help,container,false);

        HomeActivity.toolbar.setTitle("Help");

        help_faq = (TextView) v.findViewById(R.id.help_faq);
        help_contactus = (TextView) v.findViewById(R.id.help_contactus);
        help_policy = (TextView) v.findViewById(R.id.help_policy);
        help_appinfo = (TextView) v.findViewById(R.id.help_appinfo);

        help_faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"FAQ",Toast.LENGTH_SHORT).show();
            }
        });

        help_contactus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"Contact us",Toast.LENGTH_SHORT).show();
                getFragmentManager().beginTransaction().replace(R.id.container,new Fragment_Help_2()).addToBackStack(null).commit();

            }
        });

        help_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"Policy",Toast.LENGTH_SHORT).show();
            }
        });

        help_appinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"App Info",Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calligrapher = new Calligrapher(getActivity());
        calligrapher.setFont(getActivity(),"Ubuntu_R.ttf",true);
    }
}
