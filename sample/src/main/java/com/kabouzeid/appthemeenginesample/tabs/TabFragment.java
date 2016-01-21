package com.kabouzeid.appthemeenginesample.tabs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kabouzeid.appthemehelper.ATH;
import com.kabouzeid.appthemeenginesample.R;
import com.kabouzeid.appthemeenginesample.Util;

/**
 * @author Aidan Follestad (afollestad)
 */
public class TabFragment extends Fragment {

    public static TabFragment create(int position) {
        Bundle args = new Bundle();
        args.putInt("position", position);
        TabFragment fragment = new TabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TextView view = (TextView) inflater.inflate(R.layout.fragment_tab, container, false);
        view.setText(String.format("TAB %d", getArguments().getInt("position")));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Apply theming to the Fragment view
        ATH.apply(this, Util.resolveString(getActivity(), R.attr.ate_key));
    }
}