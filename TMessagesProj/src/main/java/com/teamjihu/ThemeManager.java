package com.teamjihu;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.content.Context;
import android.content.SharedPreferences;

public class ThemeManager {
	private static final String CUSTOM_PERMISSION = "com.teamjihu.theme.telegram.v1";
    private static final String DEFAULT_THEME_PACKAGE = "org.telegram.messenger.phonethemeshop";

    private static final String PREF_NAME = "THEME_PREF";
    private static final String PREF_THEME_PACKAGE = "PREF_THEME_PACKAGE";
	private PackageManager mPackageManager = null;
	private String mCurrentTheme = null;
	private Resources mCurrentThemeResource = null;
    private Context mContext = null;
	
	public ThemeManager(Context context) {
        if(mContext != context) {
            mContext = context;
            mPackageManager = mContext.getPackageManager();
        }

        setTheme();
	}

    private void setTheme() {
        String curTheme = getPreference(mContext, PREF_THEME_PACKAGE, DEFAULT_THEME_PACKAGE);
        setCurrentTheme(curTheme);
    }

    public void applyTheme(String themePackageName) {
        setPreference(mContext, PREF_THEME_PACKAGE, themePackageName);
        setTheme();
    }

    private static String getPreference(Context context, String name, String defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(name, defaultValue);
    }
    private static void setPreference(Context context, String name, String data) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(name, data);
        editor.commit();
    }

	private void setCurrentTheme(String themePkgName) {
		mCurrentTheme = themePkgName;
		
		try {
			mCurrentThemeResource = mPackageManager.getResourcesForApplication(mCurrentTheme);
		} catch (NameNotFoundException e) {
			setCurrentTheme(DEFAULT_THEME_PACKAGE);
		}
	}
	
	public String getCurrentTheme() {
		return mCurrentTheme;
	}
	
	public String getCurrentThemeName() {
		if(mCurrentThemeResource == null)
			return null;
		
		final int themeTitleId = mCurrentThemeResource.getIdentifier("theme_title",  "string", mCurrentTheme);
		String themeTitle = mCurrentThemeResource.getString(themeTitleId);
		return themeTitle;
	}
	
	public void GetThemeList(ArrayList<String> pkgs, ArrayList<String> names) {
		getThemeList(pkgs);
		
		for(String pkgName : pkgs) {
			try {
				Resources res = mPackageManager.getResourcesForApplication(pkgName);
				String themeTitle = res.getString(res.getIdentifier("theme_title", "string", pkgName));
				names.add(themeTitle);
			} catch (NameNotFoundException e) {
				names.add("NO_THEME_NAME");
			} catch(Resources.NotFoundException e) {
                names.add("NO_THEME_NAME");
            }
		}
	}
	
	public void getThemeList(ArrayList<String> pkgs){
    	List<PackageInfo> installedList = mPackageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);

    	for(PackageInfo pkgInfo : installedList){
    		if(pkgInfo.permissions == null)
    			continue;
    			
    		for(PermissionInfo perm : pkgInfo.permissions){
    			if(CUSTOM_PERMISSION.equals(perm.name)){
    				pkgs.add(pkgInfo.packageName);
    				break;
    			}
    		}
    	}
    }
	
	
	///////////////////////////////////////////////////////////////////////////
	// actual theme related resource retriever
	///////////////////////////////////////////////////////////////////////////
	
	public Drawable getDrawable(String name, boolean nullIfNotExist) {
		int id = mCurrentThemeResource.getIdentifier(name, "drawable", mCurrentTheme);
		try{
			return mCurrentThemeResource.getDrawable(id);
		} catch(Resources.NotFoundException ex) {
			if(nullIfNotExist)
				return null;
			String currentTheme = getCurrentTheme();
			setCurrentTheme(DEFAULT_THEME_PACKAGE);
			Drawable ret = getDrawable(name, true);
			setCurrentTheme(currentTheme);
			return ret;
		}
	}
	
	public int getColor(String name) {
		int id = mCurrentThemeResource.getIdentifier(name, "color", mCurrentTheme);
		try{
			return mCurrentThemeResource.getColor(id);
		} catch(Resources.NotFoundException ex) {
			String currentTheme = getCurrentTheme();
			setCurrentTheme(DEFAULT_THEME_PACKAGE);
			int ret = getColor(name);
			setCurrentTheme(currentTheme);
			return ret;
		}
	}
}
