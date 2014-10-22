package org.telegram.ui;

import org.telegram.ui.Views.ActionBar.BaseFragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import org.telegram.messenger.phonethemeshop.R;
/**
 * Created by soohwanpark on 2014-10-22.
 */
public class SettingsThemeActivity extends BaseFragment {
    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.settings_theme_layout, container, false);
        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        return fragmentView;
    }
}
