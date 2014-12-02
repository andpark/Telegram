package org.telegram.ui;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.teamjihu.LockManager;
import com.teamjihu.ThemeManager;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;
import org.telegram.messenger.phonethemeshop.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Views.Switch;

public class LockSettingActivity extends BaseFragment {
    private ThemeManager themeManager;
    private LayoutInflater INFLATER;
    private FrameLayout turnOnLockRow;
    private TextView turnOnLock;
    private FrameLayout changeLockPasswordRow;
    private TextView changeLockPassword;
    private TextView txtLockSettingComment;
    public Switch checkBox;

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        INFLATER = inflater;
        if (fragmentView == null) {
            themeManager = new ThemeManager(getParentActivity());
            actionBar.setBackButtonDrawable(themeManager.getDrawable("ic_ab_back", false));
            actionBar.setTitle(LocaleController.getString("PasswordLock", R.string.PasswordLock));
            actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        finishFragment();
                    }
                }
            });

            fragmentView = inflater.inflate(R.layout.lock_setting_activity, container, false);

            turnOnLockRow = (FrameLayout)fragmentView.findViewById(R.id.turnOnLockRow);
            turnOnLockRow.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if ( LockManager.isLocked() ) {
                        LockManager.Off();
                        checkBox.setChecked(LockManager.isLocked());
                    } else {
                        presentFragment(new LockChangePasswordActivity(LockSettingActivity.this));
                    }
                }
            });
            changeLockPasswordRow = (FrameLayout)fragmentView.findViewById(R.id.changeLockPasswordRow);
            changeLockPasswordRow.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if ( LockManager.isLocked() )
                        presentFragment(new LockChangePasswordActivity(LockSettingActivity.this));
                }
            });
            turnOnLock = (TextView)fragmentView.findViewById(R.id.turnOnLock);
            turnOnLock.setText(LocaleController.getString("TurnOnLock", R.string.TurnOnLock));
            changeLockPassword = (TextView)fragmentView.findViewById(R.id.changeLockPassword);
            changeLockPassword.setText(LocaleController.getString("ChangeLockPassword", R.string.ChangeLockPassword));
            txtLockSettingComment = (TextView)fragmentView.findViewById(R.id.txtLockSettingComment);
            txtLockSettingComment.setText(LocaleController.getString("use_password_cannot_popup", R.string.use_password_cannot_popup));

            checkBox = new Switch(getParentActivity());
            checkBox.setDuplicateParentStateEnabled(false);
            checkBox.setFocusable(false);
            checkBox.setFocusableInTouchMode(false);
            checkBox.setClickable(false);
            ((FrameLayout) fragmentView.findViewById(R.id.turnOnLockRow)).addView(checkBox);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) checkBox.getLayoutParams();
            layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.rightMargin = AndroidUtilities.dp(15);
            layoutParams.gravity = (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL;
            checkBox.setLayoutParams(layoutParams);

            checkBox.setChecked(LockManager.isLocked());
        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }

        return fragmentView;
    }
}
