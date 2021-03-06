/*
 * This is the source code of Telegram for Android v. 1.4.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.ui.ActionBar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.teamjihu.ThemeManager;

import org.telegram.android.AndroidUtilities;

public class ActionBarMenu extends LinearLayout {

    protected ActionBar parentActionBar;

    ThemeManager themeManager;

    public ActionBarMenu(Context context, ActionBar layer) {

        super(context);
        themeManager = new ThemeManager(context);
        setOrientation(LinearLayout.HORIZONTAL);
        parentActionBar = layer;
    }

    public ActionBarMenu(Context context) {
        super(context);
    }

    public ActionBarMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ActionBarMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public View addItemResource(int id, int resourceId) {
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(resourceId, null);
        view.setTag(id);
        addView(view);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)view.getLayoutParams();
        layoutParams.height = FrameLayout.LayoutParams.FILL_PARENT;
        if ( parentActionBar.itemsBackgroundResourceId == -1 )
            themeManager.setBackgroundDrawable(view, themeManager.getDrawable(parentActionBar.itemsBackgroundStr, false));
        else
            view.setBackgroundResource(parentActionBar.itemsBackgroundResourceId);
        view.setLayoutParams(layoutParams);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick((Integer)view.getTag());
            }
        });
        return view;
    }

    public ActionBarMenuItem addItem(int id, Drawable drawable) {
        if ( parentActionBar.itemsBackgroundResourceId == -1 )
            return addItem(id, 0, parentActionBar.itemsBackgroundStr, drawable, AndroidUtilities.dp(48));
        else
            return addItem(id, 0, parentActionBar.itemsBackgroundResourceId, drawable, AndroidUtilities.dp(48));
    }

    public ActionBarMenuItem addItem(int id, int icon) {
        if ( parentActionBar.itemsBackgroundResourceId == -1 )
            return addItem(id, icon, parentActionBar.itemsBackgroundStr);
        else
            return addItem(id, icon, parentActionBar.itemsBackgroundResourceId);
    }

    public ActionBarMenuItem addItem(int id, int icon, int backgroundResource) {
        return addItem(id, icon, backgroundResource, null, AndroidUtilities.dp(48));
    }

    public ActionBarMenuItem addItem(int id, int icon, String backgroundStr) {
        return addItem(id, icon, backgroundStr, null, AndroidUtilities.dp(48));
    }

    public ActionBarMenuItem addItemWithWidth(int id, int icon, int width) {
        if ( parentActionBar.itemsBackgroundResourceId == -1 )
            return addItem(id, icon, parentActionBar.itemsBackgroundStr, null, width);
        else
            return addItem(id, icon, parentActionBar.itemsBackgroundResourceId, null, width);
    }

    public ActionBarMenuItem addItem(int id, int icon, int backgroundResource, Drawable drawable, int width) {
        ActionBarMenuItem menuItem = new ActionBarMenuItem(getContext(), this, backgroundResource);
        menuItem.setTag(id);
        menuItem.setScaleType(ImageView.ScaleType.CENTER);
        if (drawable != null) {
            menuItem.setImageDrawable(drawable);
        } else {
            menuItem.setImageResource(icon);
        }
        addView(menuItem);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)menuItem.getLayoutParams();
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.width = width;
        menuItem.setLayoutParams(layoutParams);
        menuItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ActionBarMenuItem item = (ActionBarMenuItem)view;
                if (item.hasSubMenu()) {
                    if (parentActionBar.actionBarMenuOnItemClick.canOpenMenu()) {
                        item.toggleSubMenu();
                    }
                } else if (item.isSearchField()) {
                    parentActionBar.onSearchFieldVisibilityChanged(item.toggleSearch());
                } else {
                    onItemClick((Integer)view.getTag());
                }
            }
        });
        return menuItem;
    }

    public ActionBarMenuItem addItem(int id, int icon, String backgroundStr, Drawable drawable, int width) {
        ActionBarMenuItem menuItem = new ActionBarMenuItem(getContext(), this, backgroundStr);
        menuItem.setTag(id);
        menuItem.setScaleType(ImageView.ScaleType.CENTER);
        if (drawable != null) {
            menuItem.setImageDrawable(drawable);
        } else {
            menuItem.setImageResource(icon);
        }
        addView(menuItem);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)menuItem.getLayoutParams();
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.width = width;
        menuItem.setLayoutParams(layoutParams);
        menuItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ActionBarMenuItem item = (ActionBarMenuItem)view;
                if (item.hasSubMenu()) {
                    if (parentActionBar.actionBarMenuOnItemClick.canOpenMenu()) {
                        item.toggleSubMenu();
                    }
                } else if (item.isSearchField()) {
                    parentActionBar.onSearchFieldVisibilityChanged(item.toggleSearch());
                } else {
                    onItemClick((Integer)view.getTag());
                }
            }
        });
        return menuItem;
    }

    public void hideAllPopupMenus() {
        for (int a = 0; a < getChildCount(); a++) {
            View view = getChildAt(a);
            if (view instanceof ActionBarMenuItem) {
                ((ActionBarMenuItem)view).closeSubMenu();
            }
        }
    }

    public void onItemClick(int id) {
        if (parentActionBar.actionBarMenuOnItemClick != null) {
            parentActionBar.actionBarMenuOnItemClick.onItemClick(id);
        }
    }

    public void clearItems() {
        for (int a = 0; a < getChildCount(); a++) {
            View view = getChildAt(a);
            removeView(view);
        }
    }

    public void onMenuButtonPressed() {
        for (int a = 0; a < getChildCount(); a++) {
            View view = getChildAt(a);
            if (view instanceof ActionBarMenuItem) {
                ActionBarMenuItem item = (ActionBarMenuItem)view;
                if (item.hasSubMenu() && item.getVisibility() == VISIBLE) {
                    item.toggleSubMenu();
                    break;
                }
            }
        }
    }

    public void closeSearchField() {
        for (int a = 0; a < getChildCount(); a++) {
            View view = getChildAt(a);
            if (view instanceof ActionBarMenuItem) {
                ActionBarMenuItem item = (ActionBarMenuItem)view;
                if (item.isSearchField()) {
                    parentActionBar.onSearchFieldVisibilityChanged(item.toggleSearch());
                }
            }
        }
    }

    public ActionBarMenuItem getItem(int id) {
        View v = findViewWithTag(id);
        if (v instanceof ActionBarMenuItem) {
            return (ActionBarMenuItem)v;
        }
        return null;
    }
}
