package com.teamjihu;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public class ThemeManager {
	private static final String CUSTOM_PERMISSION = "kr.co.iconnect.inputmethod.ikeypad.theme.V1";
	private PackageManager mPackageManager = null;
	private String mCurrentTheme = null;
	private Resources mCurrentThemeResource = null;
	
	public ThemeManager(PackageManager pm) {
		mPackageManager = pm;
	}
	
	
	public void setCurrentTheme(String themePkgName) {
		mCurrentTheme = themePkgName;
		
		try {
			mCurrentThemeResource = mPackageManager.getResourcesForApplication(mCurrentTheme);
		} catch (NameNotFoundException e) {
			setCurrentTheme("com.teamjihu.taroplayer");
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
			setCurrentTheme("com.teamjihu.taroplayer");
			Drawable ret = getDrawable(name, false);
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
			setCurrentTheme("com.teamjihu.taroplayer");
			int ret = getColor(name);
			setCurrentTheme(currentTheme);
			return ret;
		}
	}
}
