package org.telegram.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
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

import com.teamjihu.ThemeManager;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;
import org.telegram.messenger.phonethemeshop.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.ChatBaseCell;
import org.telegram.ui.Views.Switch;

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
    List<Drawable> themeIcons = new ArrayList<Drawable>();
    String prevSelectedThemePkg;

    private final int REQUEST_UNINSTALL = 1;

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        INFLATER = inflater;
        if (fragmentView == null) {
            themeManager = new ThemeManager(getParentActivity());
            actionBar.setBackButtonDrawable(themeManager.getDrawable("ic_ab_back", false));
            actionBar.setTitle(LocaleController.getString("SelectTheme", R.string.SelectTheme));
            actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
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
            arrayAdapterThemeList = new ArrayAdapterThemeList(getParentActivity(), R.layout.theme_item, themeItems, themeIcons);
            themeList.setAdapter(arrayAdapterThemeList);

            themeList.setOnItemClickListener(onThemeItemClickListener);

            TextView btnMoreTheme = (TextView)fragmentView.findViewById(R.id.btnMoreTheme);
            btnMoreTheme.setText(LocaleController.getString("moreTheme", R.string.moreTheme));
            btnMoreTheme.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    String targetPackage = "com.iconnect.app.pts.a";

                    PackageManager pm = getParentActivity().getPackageManager();
                    try {
                        PackageInfo info=pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
                        intent.setData(Uri.parse("pts://theme?serverType=telegram"));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getParentActivity().startActivity(intent);
                    } catch (PackageManager.NameNotFoundException e) {
                        getParentActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + targetPackage)));
                    }
                }
            });
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
        ArrayList<Drawable> icons = new ArrayList<Drawable>();
        themeItems.clear();
        themeIcons.clear();

        themeManager.GetThemeList(pkgs, names, icons);
        for ( int i = 0; i < pkgs.size(); i++ ) {
            Bundle b = new Bundle();
            b.putString("themeName", names.get(i));
            b.putString("themePkg", pkgs.get(i));

            if ( names.get(i).equals(getParentActivity().getString(R.string.theme_title)) ) {
                themeItems.add(0, b);
                themeIcons.add(0, icons.get(i));
            }else {
                themeItems.add(b);
                themeIcons.add(icons.get(i));
            }
        }
    }

    AdapterView.OnItemClickListener onThemeItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            FrameLayout selectedThemeItem = (FrameLayout)view;
            Bundle b = (Bundle)selectedThemeItem.getTag();

            if ( !currentThemePkg.equals(b.getString("themePkg")) ) {
                themeManager.applyTheme(b.getString("themePkg"));

                currentThemePkg = b.getString("themePkg");
            }
            prevSelectedThemePkg = currentThemePkg;
            arrayAdapterThemeList.notifyDataSetChanged();
        }
    };

    public class ArrayAdapterThemeList extends ArrayAdapter<Bundle> {
        int layoutResourceId;
        List<Bundle> _themeItems;
        List<Drawable> _themeIcons;

        public ArrayAdapterThemeList(Context context, int resource, List<Bundle> objects, List<Drawable> iconObjects) {
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
                ImageView btnDelete = (ImageView)convertView.findViewById(R.id.settings_row_delete_button);

                Switch checkBox;
                boolean hasCheckBox = false;
                int checkBoxIndex = 0;
                for ( int i = 0; i < ((FrameLayout)convertView).getChildCount(); i++ ) {
                    if ( ((FrameLayout)convertView).getChildAt(i) instanceof Switch ) {
                        checkBoxIndex = i;
                        hasCheckBox = true;
                    }
                }

                if ( !hasCheckBox ) {
                    checkBox = new Switch(getContext());
                    checkBox.setDuplicateParentStateEnabled(false);
                    checkBox.setFocusable(false);
                    checkBox.setFocusableInTouchMode(false);
                    checkBox.setClickable(false);
                    ((FrameLayout) convertView).addView(checkBox);
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) checkBox.getLayoutParams();
                    layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
                    layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
                    layoutParams.rightMargin = AndroidUtilities.dp(48);
                    layoutParams.gravity = (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL;
                    checkBox.setLayoutParams(layoutParams);
                } else
                    checkBox = (Switch)((FrameLayout)convertView).getChildAt(checkBoxIndex);

                btnDelete.setTag(item);

                if ( icon == null )
                    iconView.setVisibility(View.GONE);
                else
                    iconView.setImageDrawable(icon);

                if ( themeName != null ) {
                    themeName.setText(item.getString("themeName"));
                    if ( currentThemePkg.equals(item.getString("themePkg")) ) {
                        setChecked(checkBox, true);
                        prevSelectedThemePkg = item.getString("themePkg");
                    } else {
                        setChecked(checkBox, false);
                    }

                    if ( item.getString("themeName").equals(getParentActivity().getString(R.string.theme_title)) ) {
                        btnDelete.setVisibility(View.GONE);
                    } else {
                        btnDelete.setVisibility(View.VISIBLE);
                    }
                }
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle b = (Bundle)view.getTag();
                        String selectedThemeName = b.getString("themeName");
                        String prevSelectedThemePkg = b.getString("themePkg");

                        if ( !currentThemePkg.equals(prevSelectedThemePkg) ) {
                            int index = -1;
                            for ( int i = 0; i < themeItems.size(); i++ )
                                if ( themeItems.get(i).getString("themeName").equals(selectedThemeName) ) {
                                    index = i;
                                    break;
                                }
                            if ( index != -1 ) {
                                Uri packageURI = Uri.parse("package:" + prevSelectedThemePkg);
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


    public void setChecked(Switch checkBox, boolean checked) {
        checkBox.setChecked(checked);
    }

    @Override
    public void onActivityResultFragment(int index, int resultCode, Intent data) {
        getThemeList();
        arrayAdapterThemeList.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        getThemeList();
        arrayAdapterThemeList.notifyDataSetChanged();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();

        if ( !currentThemePkg.equals(prevThemePkg) ) {
            ChatBaseCell chatBaseCell = new ChatBaseCell(getParentActivity(), true);    //init talk balloon(ex : msg_out_selected.9.png)
            Intent i = getParentActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage(getParentActivity().getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            getParentActivity().finish();
            getParentActivity().startActivity(i);
        }
    }
}
