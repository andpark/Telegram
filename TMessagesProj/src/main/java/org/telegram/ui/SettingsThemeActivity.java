package org.telegram.ui;

import org.telegram.android.LocaleController;
import org.telegram.ui.Views.ActionBar.ActionBarLayer;
import org.telegram.ui.Views.ActionBar.BaseFragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.teamjihu.ThemeManager;

import org.telegram.messenger.phonethemeshop.R;

import java.util.ArrayList;

/**
 * Created by soohwanpark on 2014-10-22.
 */
public class SettingsThemeActivity extends BaseFragment {
    ThemeManager mThemeManager;
    LayoutInflater INFLATER;

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        INFLATER = inflater;
        if (fragmentView == null) {
            actionBarLayer.setDisplayHomeAsUpEnabled(true, R.drawable.ic_ab_back);
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
            mThemeManager = new ThemeManager(getParentActivity());
            getThemeList();
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

        ListView themeList = (ListView)fragmentView.findViewById(R.id.theme_list);
        mThemeManager.GetThemeList(pkgs, names);
//        for ( int i = 0; i < pkgs.size(); i++ ) {
//            FrameLayout themeItem = (FrameLayout)INFLATER.inflate(R.layout.theme_item, themeList, false);
//            TextView themeName = (TextView)themeItem.findViewById(R.id.theme_name);
//            themeName.setText(names.get(i));
//
//            themeList.addView(themeItem);
//        }
    }
}
