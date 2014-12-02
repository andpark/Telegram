package org.telegram.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.teamjihu.LockManager;
import com.teamjihu.ThemeManager;

import org.telegram.android.LocaleController;
import org.telegram.messenger.phonethemeshop.R;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.Timer;
import java.util.TimerTask;

public class LockActivity extends BaseFragment {
    private ThemeManager themeManager;
    private LayoutInflater INFLATER;
    private TextView txtPasswordComment;
    private ImageView[] imgCircles = new ImageView[4];
    private String inputPassword = "";
    private boolean fromSettingFragment = false;

    public LockActivity() {}

    public LockActivity(boolean _fromSettingFragment) {
        fromSettingFragment = _fromSettingFragment;
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        INFLATER = inflater;
        if (fragmentView == null) {
            themeManager = new ThemeManager(getParentActivity());
            actionBar.setVisibility(View.GONE);
            this.swipeBackEnabled = false;

            fragmentView = inflater.inflate(R.layout.lock_layout, container, false);

            txtPasswordComment = (TextView)fragmentView.findViewById(R.id.txtPasswordComment);
            imgCircles[0] = (ImageView)fragmentView.findViewById(R.id.lock_circle1);
            imgCircles[1] = (ImageView)fragmentView.findViewById(R.id.lock_circle2);
            imgCircles[2] = (ImageView)fragmentView.findViewById(R.id.lock_circle3);
            imgCircles[3] = (ImageView)fragmentView.findViewById(R.id.lock_circle4);

            fragmentView.findViewById(R.id.lock_btn_number1).setOnClickListener(lockBtnNumberOnClickListener);
            fragmentView.findViewById(R.id.lock_btn_number2).setOnClickListener(lockBtnNumberOnClickListener);
            fragmentView.findViewById(R.id.lock_btn_number3).setOnClickListener(lockBtnNumberOnClickListener);
            fragmentView.findViewById(R.id.lock_btn_number4).setOnClickListener(lockBtnNumberOnClickListener);
            fragmentView.findViewById(R.id.lock_btn_number5).setOnClickListener(lockBtnNumberOnClickListener);
            fragmentView.findViewById(R.id.lock_btn_number6).setOnClickListener(lockBtnNumberOnClickListener);
            fragmentView.findViewById(R.id.lock_btn_number7).setOnClickListener(lockBtnNumberOnClickListener);
            fragmentView.findViewById(R.id.lock_btn_number8).setOnClickListener(lockBtnNumberOnClickListener);
            fragmentView.findViewById(R.id.lock_btn_number9).setOnClickListener(lockBtnNumberOnClickListener);
            fragmentView.findViewById(R.id.lock_btn_number0).setOnClickListener(lockBtnNumberOnClickListener);

            fragmentView.findViewById(R.id.lock_btn_del).setOnClickListener(lockBtnDelOnClickListener);
        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }

        return fragmentView;
    }

    private void fillCircle( int strLen ) {
        for ( int i = 0; i < imgCircles.length; i++ ) {
            if ( i < strLen )
                imgCircles[i].setImageResource(R.drawable.ic_lock_circle_on);
            else
                imgCircles[i].setImageResource(R.drawable.ic_lock_circle_off);
        }
    }

    View.OnClickListener lockBtnNumberOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if ( inputPassword.length() < 4 ) {
                inputPassword += view.getTag().toString();
                fillCircle(inputPassword.length());
                if (inputPassword.length() == 4) {
                    final Handler handler = new Handler();
                    Timer t = new Timer();
                    t.schedule(new TimerTask() {
                        public void run() {
                            handler.post(new Runnable() {
                                public void run() {
                                    if (LockManager.isCorrectPassword(inputPassword)) {
                                        inputPassword = "";
                                        LockManager.Unlock();
                                        if ( fromSettingFragment )
                                            presentFragment(new LockSettingActivity(), true);
                                        else
                                            LockActivity.this.finishFragment(false);
                                    } else {
                                        inputPassword = "";
                                        fillCircle(inputPassword.length());
                                        txtPasswordComment.setText(LocaleController.getString("incorrect_password", R.string.incorrect_password));
                                        Vibrator vibe = (Vibrator) getParentActivity().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                        vibe.vibrate(500);
                                    }
                                }
                            });
                        }
                    }, 300);
                }
            }
        }
    };

    View.OnClickListener lockBtnDelOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if ( inputPassword.length() > 0  && inputPassword.length() < 4) {
                inputPassword = inputPassword.substring(0, inputPassword.length() - 1);
                fillCircle(inputPassword.length());
            }
        }
    };

    @Override
    public void onResume() {
        fillCircle(inputPassword.length());
    }

    @Override
    public boolean onBackPressed() {
        getParentActivity().finish();
        return false;
    }
}
