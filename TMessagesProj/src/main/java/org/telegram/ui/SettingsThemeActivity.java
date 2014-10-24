package org.telegram.ui;

import org.telegram.android.LocaleController;
import org.telegram.ui.Views.ActionBar.ActionBarLayer;
import org.telegram.ui.Views.ActionBar.BaseFragment;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.teamjihu.ThemeManager;

import org.telegram.messenger.phonethemeshop.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by soohwanpark on 2014-10-22.
 */
public class SettingsThemeActivity extends BaseFragment {
    ThemeManager themeManager;
    String currentThemePkg;
    LayoutInflater INFLATER;

    ListView themeList = null;
    ArrayAdapterThemeList arrayAdapterThemeList = null;
    List<Bundle> themeItems = new ArrayList<Bundle>();
    int prevThemePosition;

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        INFLATER = inflater;
        if (fragmentView == null) {
            themeManager = new ThemeManager(getParentActivity());
            //actionBarLayer.setDisplayHomeAsUpEnabled(true, R.drawable.ic_ab_back);
            actionBarLayer.setDisplayHomeAsUpEnabled(true, themeManager.getDrawable("ic_ab_back", false));
            actionBarLayer.setBackOverlay(R.layout.updating_state_layout);
            actionBarLayer.setTitle(LocaleController.getString("SelectTheme", R.string.SelectTheme));
            actionBarLayer.setActionBarMenuOnItemClick(new ActionBarLayer.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        finishFragment();
                    }
                }
            });

            fragmentView = inflater.inflate(R.layout.settings_theme_layout, container, false);

            currentThemePkg = themeManager.getCurrentTheme();
            getThemeList();

            themeList = (ListView)fragmentView.findViewById(R.id.theme_list);
            arrayAdapterThemeList = new ArrayAdapterThemeList(getParentActivity(), R.layout.theme_item, themeItems);
            themeList.setAdapter(arrayAdapterThemeList);

            themeList.setOnItemClickListener(onThemeItemClickListener);
        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        return fragmentView;
    }

    private void getThemeList() {
        ArrayList<String> pkgs = new ArrayList<String>();
        ArrayList<String> names = new ArrayList<String>();

        themeManager.GetThemeList(pkgs, names);
        for ( int i = 0; i < pkgs.size(); i++ ) {
            Bundle b = new Bundle();
            b.putString("themeName", names.get(i));
            b.putString("themePkg", pkgs.get(i));
            themeItems.add(b);
        }
    }

    AdapterView.OnItemClickListener onThemeItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            FrameLayout selectedThemeItem = (FrameLayout)view;
            Bundle b = (Bundle)selectedThemeItem.getTag();

            if ( !currentThemePkg.equals(b.getString("themePkg")) ) {
                themeManager.applyTheme(b.getString("themePkg"));
                ImageView btnCheck = (ImageView)selectedThemeItem.findViewById(R.id.settings_row_check_button);
                btnCheck.setImageResource(R.drawable.btn_check_on);

                FrameLayout prevThemeItem = (FrameLayout)parent.getChildAt(prevThemePosition);
                btnCheck = (ImageView)prevThemeItem.findViewById(R.id.settings_row_check_button);
                btnCheck.setImageResource(R.drawable.btn_check_off);
            }
            prevThemePosition = pos;
        }
    };

    public class ArrayAdapterThemeList extends ArrayAdapter<Bundle> {
        int layoutResourceId;
        List<Bundle> _themeItems;

        public ArrayAdapterThemeList(Context context, int resource, List<Bundle> objects) {
            super(context, resource, objects);

            layoutResourceId = resource;
            _themeItems = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }

            if(_themeItems.size() == 0)
                return null;
            if(position < 0)
                position = 0;
            else if(position >= _themeItems.size())
                position = _themeItems.size()-1;
            Bundle item = _themeItems.get(position);

            convertView.setTag(item);

            if ( item != null ) {
                TextView themeName = (TextView)convertView.findViewById(R.id.theme_name);
                ImageView btnCheck = (ImageView)convertView.findViewById(R.id.settings_row_check_button);
                if ( themeName != null ) {
                    themeName.setText(item.getString("themeName"));
                    if ( currentThemePkg.equals(item.getString("themePkg")) ) {
                        btnCheck.setImageResource(R.drawable.btn_check_on);
                        prevThemePosition = position;
                    }
                }
            }

            return convertView;
        }
    }
}
