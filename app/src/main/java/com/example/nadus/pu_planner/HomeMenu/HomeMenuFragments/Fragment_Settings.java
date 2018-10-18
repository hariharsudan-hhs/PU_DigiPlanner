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

import am.appwise.components.ni.NoInternetDialog;
import me.anwarshahriar.calligrapher.Calligrapher;


public class Fragment_Settings extends Fragment {

    Calligrapher calligrapher;
    NoInternetDialog noInternetDialog;
    TextView settings_pdf_generation;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings,container,false);

        HomeActivity.toolbar.setTitle("Settings");

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
//                    String filename = "hhs";
//                    String filecontent = "hhsssss";
//                    Metodos fop = new Metodos();
//                    if (fop.write(filename, filecontent)) {
//                        Toast.makeText(getActivity(),
//                                filename + ".pdf created", Toast.LENGTH_SHORT)
//                                .show();
//                    } else {
//                        Toast.makeText(getActivity(), "I/O error",
//                                Toast.LENGTH_SHORT).show();
//                    }

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }
}
