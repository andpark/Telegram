package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.teamjihu.EmoticonManager;
import com.teamjihu.ThemeManager;

import org.telegram.android.LocaleController;
import org.telegram.android.PhoneThemeShopEmoji;
import org.telegram.messenger.phonethemeshop.R;
import org.telegram.ui.Views.ActionBar.ActionBarLayer;
import org.telegram.ui.Views.ActionBar.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by soohwanpark on 2014-10-22.
 */
public class SettingsEmoticonActivity extends BaseFragment {
    ThemeManager themeManager;
    EmoticonManager emoticonManager;
    LayoutInflater INFLATER;

    ListView emoticonList = null;
    ArrayAdapterEmoticonList arrayAdapterEmoticonList = null;
    List<Bundle> emoticonItems = new ArrayList<Bundle>();
    List<Drawable> emoticonIcons = new ArrayList<Drawable>();

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        INFLATER = inflater;
        if (fragmentView == null) {
            themeManager = new ThemeManager(getParentActivity());
            emoticonManager = new EmoticonManager(getParentActivity());
            //actionBarLayer.setDisplayHomeAsUpEnabled(true, R.drawable.ic_ab_back);
            actionBarLayer.setDisplayHomeAsUpEnabled(true, themeManager.getDrawable("ic_ab_back", false));
            actionBarLayer.setBackOverlay(R.layout.updating_state_layout);
            actionBarLayer.setTitle(LocaleController.getString("ManageEmoticon", R.string.ManageEmoticon));
            actionBarLayer.setActionBarMenuOnItemClick(new ActionBarLayer.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        finishFragment();
                    }
                }
            });

            fragmentView = inflater.inflate(R.layout.settings_emoticon_layout, container, false);

            getEmoticonList();

            emoticonList = (ListView)fragmentView.findViewById(R.id.emoticon_list);
            arrayAdapterEmoticonList = new ArrayAdapterEmoticonList(getParentActivity(), R.layout.emoticon_item, emoticonItems, emoticonIcons);
            emoticonList.setAdapter(arrayAdapterEmoticonList);

            emoticonList.setOnItemClickListener(onThemeItemClickListener);
        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        return fragmentView;
    }

    private void getEmoticonList() {
        emoticonItems.clear();
        emoticonIcons.clear();

        List<PhoneThemeShopEmoji> emoticonList = emoticonManager.GetEmoticonList();
        for ( int i = 0; i < emoticonList.size(); i++ ) {
            Bundle b = new Bundle();
            String emoticonName = emoticonList.get(i).name;
            b.putString("emoticonName", emoticonName);
            b.putString("emoticonPkg", emoticonList.get(i).pkg);

            //if ( emoticonName.equals(getParentActivity().getString(R.string.emoticon_title)) )
            //    continue;

            emoticonIcons.add(emoticonList.get(i).icon);
            emoticonItems.add(b);
        }
    }

    AdapterView.OnItemClickListener onThemeItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            FrameLayout selectedThemeItem = (FrameLayout)view;
            Bundle b = (Bundle)selectedThemeItem.getTag();
        }
    };

    public class ArrayAdapterEmoticonList extends ArrayAdapter<Bundle> {
        int layoutResourceId;
        List<Bundle> _emoticonItems;
        List<Drawable> _emoticonIcons;

        public ArrayAdapterEmoticonList(Context context, int resource, List<Bundle> objects,  List<Drawable> icons) {
            super(context, resource, objects);

            layoutResourceId = resource;
            _emoticonItems = objects;
            _emoticonIcons = icons;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }

            if(_emoticonItems.size() == 0)
                return null;
            if(position < 0)
                position = 0;
            else if(position >= _emoticonItems.size())
                position = _emoticonItems.size()-1;
            Bundle item = _emoticonItems.get(position);
            Drawable icon = _emoticonIcons.get(position);

            convertView.setTag(item);

            if ( item != null ) {
                ImageView emoticonIcon = (ImageView)convertView.findViewById(R.id.emoticon_icon);
                TextView emoticonName = (TextView)convertView.findViewById(R.id.emoticon_name);
                ImageView btnDelete = (ImageView)convertView.findViewById(R.id.settings_row_delete_button);
                btnDelete.setTag(item);
                if ( emoticonName != null ) {
                    emoticonIcon.setImageDrawable(icon);
                    emoticonName.setText(item.getString("emoticonName"));
                    if ( item.getString("emoticonName").equals(getParentActivity().getString(R.string.emoticon_title)) ) {
                        btnDelete.setVisibility(View.GONE);
                    } else {
                        btnDelete.setVisibility(View.VISIBLE);
                    }
                }
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle b = (Bundle)view.getTag();
                        String selectedThemeName = b.getString("emoticonName");
                        String selectedThemePkg = b.getString("emoticonPkg");

                        int index = -1;
                        for ( int i = 0; i < emoticonItems.size(); i++ )
                            if ( emoticonItems.get(i).getString("emoticonName").equals(selectedThemeName) ) {
                                index = i;
                                break;
                            }
                        if ( index != -1 ) {
                            Uri packageURI = Uri.parse("package:" + selectedThemePkg);
                            Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageURI);
                            startActivityForResult(uninstallIntent, index);
                        }
                    }
                });
            }

            return convertView;
        }
    }

    @Override
    public void onActivityResultFragment(int index, int resultCode, Intent data) {
        getEmoticonList();
        arrayAdapterEmoticonList.notifyDataSetChanged();
    }
}
