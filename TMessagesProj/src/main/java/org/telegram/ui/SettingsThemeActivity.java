package org.telegram.ui;

import org.telegram.android.LocaleController;
import org.telegram.ui.Cells.ChatBaseCell;
import org.telegram.ui.Views.ActionBar.ActionBarLayer;
import org.telegram.ui.Views.ActionBar.BaseFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.widget.Toast;

import com.teamjihu.ThemeManager;

import org.telegram.messenger.phonethemeshop.R;
import org.telegram.ui.Views.ChatActivityEnterView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by soohwanpark on 2014-10-22.
 */
public class SettingsThemeActivity extends BaseFragment {
    ThemeManager themeManager;
    String currentThemePkg;
    String prevThemePkg;
    LayoutInflater INFLATER;

    ListView themeList = null;
    ArrayAdapterThemeList arrayAdapterThemeList = null;
    List<Bundle> themeItems = new ArrayList<Bundle>();
    ArrayList<Drawable> icons = new ArrayList<Drawable>();
    int prevThemePosition;

    private final int REQUEST_UNINSTALL = 1;

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
            prevThemePkg = currentThemePkg;
            getThemeList();

            themeList = (ListView)fragmentView.findViewById(R.id.theme_list);
            arrayAdapterThemeList = new ArrayAdapterThemeList(getParentActivity(), R.layout.theme_item, themeItems, icons);
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
        themeItems.clear();
        icons.clear();

        themeManager.GetThemeList(pkgs, names, icons);
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

                currentThemePkg = b.getString("themePkg");
            }
            prevThemePosition = pos;
        }
    };

    public class ArrayAdapterThemeList extends ArrayAdapter<Bundle> {
        int layoutResourceId;
        List<Bundle> _themeItems;
        ArrayList<Drawable> _themeIcons;

        public ArrayAdapterThemeList(Context context, int resource, List<Bundle> objects, ArrayList<Drawable> iconObjects) {
            super(context, resource, objects);

            layoutResourceId = resource;
            _themeItems = objects;
            _themeIcons = iconObjects;
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
            Drawable icon = _themeIcons.get(position);

            convertView.setTag(item);

            if ( item != null ) {
                ImageView iconView = (ImageView)convertView.findViewById(R.id.theme_icon);
                TextView themeName = (TextView)convertView.findViewById(R.id.theme_name);
                ImageView btnCheck = (ImageView)convertView.findViewById(R.id.settings_row_check_button);
                ImageView btnDelete = (ImageView)convertView.findViewById(R.id.settings_row_delete_button);
                btnDelete.setTag(item);

                if ( icon == null )
                    iconView.setVisibility(View.GONE);
                else
                    iconView.setImageDrawable(icon);

                if ( themeName != null ) {
                    themeName.setText(item.getString("themeName"));
                    if ( currentThemePkg.equals(item.getString("themePkg")) ) {
                        btnCheck.setImageResource(R.drawable.btn_check_on);
                        prevThemePosition = position;
                    }
                    if ( item.getString("themeName").equals(getParentActivity().getString(R.string.theme_title)) ) {
                        btnDelete.setVisibility(View.GONE);
                    }
                }
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle b = (Bundle)view.getTag();
                        String selectedThemeName = b.getString("themeName");
                        String selectedThemePkg = b.getString("themePkg");

                        if ( !currentThemePkg.equals(selectedThemePkg) ) {
                            int index = -1;
                            for ( int i = 0; i < themeItems.size(); i++ )
                                if ( themeItems.get(i).getString("themeName").equals(selectedThemeName) ) {
                                    index = i;
                                    break;
                                }
                            if ( index != -1 ) {
                                Uri packageURI = Uri.parse("package:" + selectedThemePkg);
                                Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageURI);
                                startActivityForResult(uninstallIntent, index);
                            }
                        } else
                            Toast.makeText(getParentActivity(), R.string.usingTheme, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            return convertView;
        }
    }

    @Override
    public void onActivityResultFragment(int index, int resultCode, Intent data) {
        getThemeList();
        arrayAdapterThemeList.notifyDataSetChanged();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();

        if ( !currentThemePkg.equals(prevThemePkg) ) {
            ChatBaseCell chatBaseCell = new ChatBaseCell(getParentActivity(), true);
            Intent i = getParentActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage(getParentActivity().getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            getParentActivity().finish();
            getParentActivity().startActivity(i);
        }
    }
}
