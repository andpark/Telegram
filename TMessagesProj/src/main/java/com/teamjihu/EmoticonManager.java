package com.teamjihu;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import org.telegram.android.PhoneThemeShopEmoji;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OOO on 2014-10-29.
 */
public class EmoticonManager {
    private static final String CUSTOM_EMOTICON_PERMISSION = "com.teamjihu.emoticon.telegram.v2";

    private PackageManager mPackageManager = null;
    private Context mContext = null;

    public EmoticonManager(Context context) {
        if(mContext != context) {
            mContext = context;
            mPackageManager = mContext.getPackageManager();
        }
    }

    public ArrayList<PhoneThemeShopEmoji> GetEmoticonList() {
        ArrayList<PhoneThemeShopEmoji> emoticons = new ArrayList<PhoneThemeShopEmoji>();
        ArrayList<String> pkgs = new ArrayList<String>();
        getEmoticonList(pkgs);

        for(String pkgName : pkgs) {
            try {
                Resources res = mPackageManager.getResourcesForApplication(pkgName);
                String emoticonPkgName = pkgName;
                String emoticonTitle = res.getString(res.getIdentifier("emoticon_title", "string", pkgName));
                int col = Integer.parseInt(res.getString(res.getIdentifier("col", "string", pkgName)));
                int row = Integer.parseInt(res.getString(res.getIdentifier("row", "string", pkgName)));
                int total = Integer.parseInt(res.getString(res.getIdentifier("total", "string", pkgName)));
                Drawable icon = getIconImage(res, pkgName);
                Drawable emoticonImage = getEmoticonImage(res, pkgName);

                if ( icon == null || emoticonImage == null )
                    continue;;

                PhoneThemeShopEmoji item = new PhoneThemeShopEmoji(emoticonPkgName, emoticonTitle, icon, emoticonImage, col, row, total);
                emoticons.add(item);
            } catch (PackageManager.NameNotFoundException e) {
            } catch(Resources.NotFoundException e) {
            }
        }
        return emoticons;
    }

    public void getEmoticonList(ArrayList<String> pkgs){
        List<PackageInfo> installedList = mPackageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);

        for(PackageInfo pkgInfo : installedList){
            if(pkgInfo.permissions == null)
                continue;

            for(PermissionInfo perm : pkgInfo.permissions){
                if(CUSTOM_EMOTICON_PERMISSION.equals(perm.name)){
                    pkgs.add(pkgInfo.packageName);
                    break;
                }
            }
        }
    }

    public Drawable getEmoticonImage(Resources res, String emoticonPkg) {
        int id = res.getIdentifier("emoticon", "drawable", emoticonPkg);
        try{
            return res.getDrawable(id);
        } catch(Resources.NotFoundException ex) {
            return null;
        }
    }

    public Drawable getIconImage(Resources res, String emoticonPkg) {
        int id = res.getIdentifier("icon", "drawable", emoticonPkg);
        try{
            return res.getDrawable(id);
        } catch(Resources.NotFoundException ex) {
            return null;
        }
    }
}