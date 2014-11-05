/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.teamjihu.ThemeManager;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.ContactsController;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.android.MediaController;
import org.telegram.messenger.BuildVars;
import org.telegram.android.LocaleController;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.SerializedData;
import org.telegram.messenger.TLClassStore;
import org.telegram.messenger.TLObject;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.ConnectionsManager;
import org.telegram.messenger.FileLog;
import org.telegram.android.MessagesController;
import org.telegram.android.MessagesStorage;
import org.telegram.android.NotificationCenter;
import org.telegram.messenger.phonethemeshop.R;
import org.telegram.messenger.RPCRequest;
import org.telegram.messenger.UserConfig;
import org.telegram.android.MessageObject;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Views.ActionBar.ActionBarLayer;
import org.telegram.ui.Views.AvatarUpdater;
import org.telegram.ui.Views.BackupImageView;
import org.telegram.ui.Views.ActionBar.BaseFragment;

import java.io.File;
import java.util.ArrayList;

public class SettingsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, PhotoViewer.PhotoViewerProvider {
    private ListView listView;
    private ListAdapter listAdapter;
    private AvatarUpdater avatarUpdater = new AvatarUpdater();

    private int profileRow;
    private int numberSectionRow;
    private int numberRow;
    private int settingsSectionRow;
    private int textSizeRow;
    private int enableAnimationsRow;
    private int notificationRow;
    private int blockedRow;
    private int backgroundRow;
    private int supportSectionRow;
    //private int askQuestionRow;
    private int logoutRow;
    private int sendLogsRow;
    private int clearLogsRow;
    private int switchBackendButtonRow;
    private int messagesSectionRow;
    private int sendByEnterRow;
    private int terminateSessionsRow;
    private int mediaDownloadSection;
    private int mobileDownloadRow;
    private int wifiDownloadRow;
    private int roamingDownloadRow;
    private int saveToGalleryRow;
    private int telegramFaqRow;
    private int languageRow;
    private int versionRow;
    private int contactsSectionRow;
    private int contactsReimportRow;
    private int contactsSortRow;
    private int selectThemeRow;
    private int manageEmoticonRow;
    private int rowCount;

    private ThemeManager themeManager;

    private static class LinkMovementMethodMy extends LinkMovementMethod {
        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            try {
                return super.onTouchEvent(widget, buffer, event);
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
            return false;
        }
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        avatarUpdater.parentFragment = this;
        avatarUpdater.delegate = new AvatarUpdater.AvatarUpdaterDelegate() {
            @Override
            public void didUploadedPhoto(TLRPC.InputFile file, TLRPC.PhotoSize small, TLRPC.PhotoSize big) {
                TLRPC.TL_photos_uploadProfilePhoto req = new TLRPC.TL_photos_uploadProfilePhoto();
                req.caption = "";
                req.crop = new TLRPC.TL_inputPhotoCropAuto();
                req.file = file;
                req.geo_point = new TLRPC.TL_inputGeoPointEmpty();
                ConnectionsManager.getInstance().performRpc(req, new RPCRequest.RPCRequestDelegate() {
                    @Override
                    public void run(TLObject response, TLRPC.TL_error error) {
                        if (error == null) {
                            TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                            if (user == null) {
                                user = UserConfig.getCurrentUser();
                                if (user == null) {
                                    return;
                                }
                                MessagesController.getInstance().putUser(user, false);
                            } else {
                                UserConfig.setCurrentUser(user);
                            }
                            if (user == null) {
                                return;
                            }
                            TLRPC.TL_photos_photo photo = (TLRPC.TL_photos_photo)response;
                            ArrayList<TLRPC.PhotoSize> sizes = photo.photo.sizes;
                            TLRPC.PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(sizes, 100);
                            TLRPC.PhotoSize bigSize = FileLoader.getClosestPhotoSizeWithSize(sizes, 1000);
                            user.photo = new TLRPC.TL_userProfilePhoto();
                            user.photo.photo_id = photo.photo.id;
                            if (smallSize != null) {
                                user.photo.photo_small = smallSize.location;
                            }
                            if (bigSize != null) {
                                user.photo.photo_big = bigSize.location;
                            } else if (smallSize != null) {
                                user.photo.photo_small = smallSize.location;
                            }
                            MessagesStorage.getInstance().clearUserPhotos(user.id);
                            ArrayList<TLRPC.User> users = new ArrayList<TLRPC.User>();
                            users.add(user);
                            MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                            AndroidUtilities.RunOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
                                    UserConfig.saveConfig(true);
                                }
                            });
                        }
                    }
                });
            }
        };
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);


        rowCount = 0;
        profileRow = rowCount++;
        numberSectionRow = rowCount++;
        numberRow = rowCount++;
        settingsSectionRow = rowCount++;
        selectThemeRow = rowCount++;
        manageEmoticonRow = rowCount++;
        enableAnimationsRow = rowCount++;
        languageRow = rowCount++;
        notificationRow = rowCount++;
        blockedRow = rowCount++;
        backgroundRow = rowCount++;
        terminateSessionsRow = rowCount++;
        mediaDownloadSection = rowCount++;
        mobileDownloadRow = rowCount++;
        wifiDownloadRow = rowCount++;
        roamingDownloadRow = rowCount++;
        saveToGalleryRow = rowCount++;
        messagesSectionRow = rowCount++;
        textSizeRow = rowCount++;
        sendByEnterRow = rowCount++;
        //contactsSectionRow = rowCount++;
        //contactsSortRow = rowCount++;
        //contactsReimportRow = rowCount++;
        supportSectionRow = rowCount++;
        if (BuildVars.DEBUG_VERSION) {
            sendLogsRow = rowCount++;
            clearLogsRow = rowCount++;
            switchBackendButtonRow = rowCount++;
        }
        telegramFaqRow = rowCount++;
        //askQuestionRow = rowCount++;
        logoutRow = rowCount++;
        versionRow = rowCount++;

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        avatarUpdater.clear();
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        if (fragmentView == null) {
            themeManager = new ThemeManager(getParentActivity());
            //actionBarLayer.setDisplayHomeAsUpEnabled(true, R.drawable.ic_ab_back);
            actionBarLayer.setDisplayHomeAsUpEnabled(true, themeManager.getDrawable("ic_ab_back", false));
            actionBarLayer.setBackOverlay(R.layout.updating_state_layout);
            actionBarLayer.setTitle(LocaleController.getString("Settings", R.string.Settings));
            actionBarLayer.setActionBarMenuOnItemClick(new ActionBarLayer.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        finishFragment();
                    }
                }
            });

            fragmentView = inflater.inflate(R.layout.settings_layout, container, false);
            listAdapter = new ListAdapter(getParentActivity());
            listView = (ListView)fragmentView.findViewById(R.id.listView);
            listView.setAdapter(listAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                    if (i == textSizeRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("TextSize", R.string.TextSize));
                        builder.setItems(new CharSequence[] {
                                String.format("%d", 12),
                                String.format("%d", 13),
                                String.format("%d", 14),
                                String.format("%d", 15),
                                String.format("%d", 16),
                                String.format("%d", 17),
                                String.format("%d", 18),
                                String.format("%d", 19),
                                String.format("%d", 20),
                                String.format("%d", 21),
                                String.format("%d", 22),
                                String.format("%d", 23),
                                String.format("%d", 24),
                                String.format("%d", 25),
                                String.format("%d", 26),
                                String.format("%d", 27),
                                String.format("%d", 28),
                                String.format("%d", 29),
                                String.format("%d", 30)}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt("fons_size", 12 + which);
                                MessagesController.getInstance().fontSize = 12 + which;
                                editor.commit();
                                if (listView != null) {
                                    listView.invalidateViews();
                                }
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showAlertDialog(builder);
                    } else if (i == enableAnimationsRow) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                        boolean animations = preferences.getBoolean("view_animations", true);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("view_animations", !animations);
                        editor.commit();
                        if (listView != null) {
                            listView.invalidateViews();
                        }
                    } else if (i == notificationRow) {
                        presentFragment(new SettingsNotificationsActivity());
                    } else if (i == blockedRow) {
                        presentFragment(new SettingsBlockedUsersActivity());
                    } else if (i == backgroundRow) {
                        presentFragment(new SettingsWallpapersActivity());
                    } else if (i == selectThemeRow) {
                        presentFragment(new SettingsThemeActivity());
                    } else if (i == manageEmoticonRow) {
                        presentFragment(new SettingsEmoticonActivity());
                    }
                    /*else if (i == askQuestionRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        final TextView message = new TextView(getParentActivity());
                        message.setText(Html.fromHtml(LocaleController.getString("AskAQuestionInfo", R.string.AskAQuestionInfo)));
                        message.setTextSize(18);
                        message.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(5), AndroidUtilities.dp(8), AndroidUtilities.dp(6));
                        message.setMovementMethod(new LinkMovementMethodMy());

                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setView(message);
                        builder.setPositiveButton(LocaleController.getString("AskButton", R.string.AskButton), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                performAskAQuestion();
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showAlertDialog(builder);
                    }*/
                    else if (i == sendLogsRow) {
                        sendLogs();
                    } else if (i == clearLogsRow) {
                        FileLog.cleanupLogs();
                    } else if (i == sendByEnterRow) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                        boolean send = preferences.getBoolean("send_by_enter", false);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("send_by_enter", !send);
                        editor.commit();
                        if (listView != null) {
                            listView.invalidateViews();
                        }
                    } else if (i == saveToGalleryRow) {
                        MediaController.getInstance().toggleSaveToGallery();
                        if (listView != null) {
                            listView.invalidateViews();
                        }
                    } else if (i == terminateSessionsRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setMessage(LocaleController.getString("AreYouSureSessions", R.string.AreYouSureSessions));
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                TLRPC.TL_auth_resetAuthorizations req = new TLRPC.TL_auth_resetAuthorizations();
                                ConnectionsManager.getInstance().performRpc(req, new RPCRequest.RPCRequestDelegate() {
                                    @Override
                                    public void run(final TLObject response, final TLRPC.TL_error error) {
                                        AndroidUtilities.RunOnUIThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (getParentActivity() == null) {
                                                    return;
                                                }
                                                if (error == null && response instanceof TLRPC.TL_boolTrue) {
                                                    Toast toast = Toast.makeText(getParentActivity(), LocaleController.getString("TerminateAllSessions", R.string.TerminateAllSessions), Toast.LENGTH_SHORT);
                                                    toast.show();
                                                } else {
                                                    Toast toast = Toast.makeText(getParentActivity(), LocaleController.getString("UnknownError", R.string.UnknownError), Toast.LENGTH_SHORT);
                                                    toast.show();
                                                }
                                            }
                                        });
                                        UserConfig.registeredForPush = false;
                                        UserConfig.registeredForInternalPush = false;
                                        UserConfig.saveConfig(false);
                                        MessagesController.getInstance().registerForPush(UserConfig.pushString);
                                        ConnectionsManager.getInstance().initPushConnection();
                                    }
                                });
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showAlertDialog(builder);
                    } else if (i == languageRow) {
                        presentFragment(new LanguageSelectActivity());
                    } else if (i == switchBackendButtonRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setMessage(LocaleController.getString("AreYouSure", R.string.AreYouSure));
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ConnectionsManager.getInstance().switchBackend();
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showAlertDialog(builder);
                    } else if (i == telegramFaqRow) {
                        try {
                            Intent pickIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(LocaleController.getString("TelegramFaqUrl", R.string.TelegramFaqUrl)));
                            getParentActivity().startActivity(pickIntent);
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }
                    } else if (i == contactsReimportRow) {

                    } else if (i == contactsSortRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("SortBy", R.string.SortBy));
                        builder.setItems(new CharSequence[] {
                                LocaleController.getString("Default", R.string.Default),
                                LocaleController.getString("SortFirstName", R.string.SortFirstName),
                                LocaleController.getString("SortLastName", R.string.SortLastName)
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt("sortContactsBy", which);
                                editor.commit();
                                if (listView != null) {
                                    listView.invalidateViews();
                                }
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showAlertDialog(builder);
                    } else if (i == wifiDownloadRow || i == mobileDownloadRow || i == roamingDownloadRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());

                        int mask = 0;
                        if (i == mobileDownloadRow) {
                            builder.setTitle(LocaleController.getString("WhenUsingMobileData", R.string.WhenUsingMobileData));
                            mask = MediaController.getInstance().mobileDataDownloadMask;
                        } else if (i == wifiDownloadRow) {
                            builder.setTitle(LocaleController.getString("WhenConnectedOnWiFi", R.string.WhenConnectedOnWiFi));
                            mask = MediaController.getInstance().wifiDownloadMask;
                        } else if (i == roamingDownloadRow) {
                            builder.setTitle(LocaleController.getString("WhenRoaming", R.string.WhenRoaming));
                            mask = MediaController.getInstance().roamingDownloadMask;
                        }
                        builder.setMultiChoiceItems(
                                new CharSequence[]{LocaleController.getString("AttachPhoto", R.string.AttachPhoto), LocaleController.getString("AttachAudio", R.string.AttachAudio), LocaleController.getString("AttachAudio", R.string.AttachVideo), LocaleController.getString("AttachAudio", R.string.AttachDocument)},
                                new boolean[]{(mask & MediaController.AUTODOWNLOAD_MASK_PHOTO) != 0, (mask & MediaController.AUTODOWNLOAD_MASK_AUDIO) != 0, (mask & MediaController.AUTODOWNLOAD_MASK_VIDEO) != 0, (mask & MediaController.AUTODOWNLOAD_MASK_DOCUMENT) != 0},
                                new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        int mask = 0;
                                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = preferences.edit();
                                        if (i == mobileDownloadRow) {
                                            mask = MediaController.getInstance().mobileDataDownloadMask;
                                        } else if (i == wifiDownloadRow) {
                                            mask = MediaController.getInstance().wifiDownloadMask;
                                        } else if (i == roamingDownloadRow) {
                                            mask = MediaController.getInstance().roamingDownloadMask;
                                        }

                                        int maskDiff = 0;
                                        if (which == 0) {
                                            maskDiff = MediaController.AUTODOWNLOAD_MASK_PHOTO;
                                        } else if (which == 1) {
                                            maskDiff = MediaController.AUTODOWNLOAD_MASK_AUDIO;
                                        } else if (which == 2) {
                                            maskDiff = MediaController.AUTODOWNLOAD_MASK_VIDEO;
                                        } else if (which == 3) {
                                            maskDiff = MediaController.AUTODOWNLOAD_MASK_DOCUMENT;
                                        }

                                        if (isChecked) {
                                            mask |= maskDiff;
                                        } else {
                                            mask &= ~maskDiff;
                                        }

                                        if (i == mobileDownloadRow) {
                                            editor.putInt("mobileDataDownloadMask", mask);
                                            mask = MediaController.getInstance().mobileDataDownloadMask = mask;
                                        } else if (i == wifiDownloadRow) {
                                            editor.putInt("wifiDownloadMask", mask);
                                            MediaController.getInstance().wifiDownloadMask = mask;
                                        } else if (i == roamingDownloadRow) {
                                            editor.putInt("roamingDownloadMask", mask);
                                            MediaController.getInstance().roamingDownloadMask = mask;
                                        }
                                        editor.commit();
                                        if (listView != null) {
                                            listView.invalidateViews();
                                        }
                                    }
                                });
                        builder.setNegativeButton(LocaleController.getString("OK", R.string.OK), null);
                        showAlertDialog(builder);
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

    @Override
    protected void onDialogDismiss() {
        MediaController.getInstance().checkAutodownloadSettings();
    }

    @Override
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
        if (fileLocation == null) {
            return null;
        }
        TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
        if (user != null && user.photo != null && user.photo.photo_big != null) {
            TLRPC.FileLocation photoBig = user.photo.photo_big;
            if (photoBig.local_id == fileLocation.local_id && photoBig.volume_id == fileLocation.volume_id && photoBig.dc_id == fileLocation.dc_id) {
                int count = listView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View view = listView.getChildAt(a);
                    BackupImageView avatarImage = (BackupImageView)view.findViewById(R.id.settings_avatar_image);
                    if (avatarImage != null) {
                        int coords[] = new int[2];
                        avatarImage.getLocationInWindow(coords);
                        PhotoViewer.PlaceProviderObject object = new PhotoViewer.PlaceProviderObject();
                        object.viewX = coords[0];
                        object.viewY = coords[1] - AndroidUtilities.statusBarHeight;
                        object.parentView = listView;
                        object.imageReceiver = avatarImage.imageReceiver;
                        object.user_id = UserConfig.getClientUserId();
                        object.thumb = object.imageReceiver.getBitmap();
                        object.size = -1;
                        return object;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void willSwitchFromPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) { }

    @Override
    public void willHidePhotoViewer() { }

    @Override
    public boolean isPhotoChecked(int index) { return false; }

    @Override
    public void setPhotoChecked(int index) { }

    @Override
    public void cancelButtonPressed() { }

    @Override
    public void sendButtonPressed(int index) { }

    @Override
    public int getSelectedCount() { return 0; }

    public void performAskAQuestion() {
        final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        int uid = preferences.getInt("support_id", 0);
        TLRPC.User supportUser = null;
        if (uid != 0) {
            supportUser = MessagesController.getInstance().getUser(uid);
            if (supportUser == null) {
                String userString = preferences.getString("support_user", null);
                if (userString != null) {
                    try {
                        byte[] datacentersBytes = Base64.decode(userString, Base64.DEFAULT);
                        if (datacentersBytes != null) {
                            SerializedData data = new SerializedData(datacentersBytes);
                            supportUser = (TLRPC.User)TLClassStore.Instance().TLdeserialize(data, data.readInt32());
                            if (supportUser != null && supportUser.id == 333000) {
                                supportUser = null;
                            }
                        }
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                        supportUser = null;
                    }
                }
            }
        }
        if (supportUser == null) {
            final ProgressDialog progressDialog = new ProgressDialog(getParentActivity());
            progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
            TLRPC.TL_help_getSupport req = new TLRPC.TL_help_getSupport();
            ConnectionsManager.getInstance().performRpc(req, new RPCRequest.RPCRequestDelegate() {
                @Override
                public void run(TLObject response, TLRPC.TL_error error) {
                    if (error == null) {

                        final TLRPC.TL_help_support res = (TLRPC.TL_help_support)response;
                        AndroidUtilities.RunOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt("support_id", res.user.id);
                                SerializedData data = new SerializedData();
                                res.user.serializeToStream(data);
                                editor.putString("support_user", Base64.encodeToString(data.toByteArray(), Base64.DEFAULT));
                                editor.commit();
                                try {
                                    progressDialog.dismiss();
                                } catch (Exception e) {
                                    FileLog.e("tmessages", e);
                                }
                                ArrayList<TLRPC.User> users = new ArrayList<TLRPC.User>();
                                users.add(res.user);
                                MessagesStorage.getInstance().putUsersAndChats(users, null, true, true);
                                MessagesController.getInstance().putUser(res.user, false);
                                Bundle args = new Bundle();
                                args.putInt("user_id", res.user.id);
                                presentFragment(new ChatActivity(args));
                            }
                        });
                    } else {
                        AndroidUtilities.RunOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    progressDialog.dismiss();
                                } catch (Exception e) {
                                    FileLog.e("tmessages", e);
                                }
                            }
                        });
                    }
                }
            });
        } else {
            MessagesController.getInstance().putUser(supportUser, true);
            Bundle args = new Bundle();
            args.putInt("user_id", supportUser.id);
            presentFragment(new ChatActivity(args));
        }
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        avatarUpdater.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void saveSelfArgs(Bundle args) {
        if (avatarUpdater != null && avatarUpdater.currentPicturePath != null) {
            args.putString("path", avatarUpdater.currentPicturePath);
        }
    }

    @Override
    public void restoreSelfArgs(Bundle args) {
        if (avatarUpdater != null) {
            avatarUpdater.currentPicturePath = args.getString("path");
        }
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.updateInterfaces) {
            int mask = (Integer)args[0];
            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0) {
                if (listView != null) {
                    listView.invalidateViews();
                }
            }
        }
    }

    private void sendLogs() {
        try {
            ArrayList<Uri> uris = new ArrayList<Uri>();
            File sdCard = ApplicationLoader.applicationContext.getExternalFilesDir(null);
            File dir = new File (sdCard.getAbsolutePath() + "/logs");
            File[] files = dir.listFiles();
            for (File file : files) {
                uris.add(Uri.fromFile(file));
            }

            if (uris.isEmpty()) {
                return;
            }
            Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
            i.setType("message/rfc822") ;
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{BuildVars.SEND_LOGS_EMAIL});
            i.putExtra(Intent.EXTRA_SUBJECT, "last logs");
            i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            getParentActivity().startActivity(Intent.createChooser(i, "Select email application."));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int i) {
            return i == textSizeRow || i == enableAnimationsRow || i == blockedRow || i == notificationRow || i == backgroundRow ||
                    /*i == askQuestionRow ||*/ i == sendLogsRow || i == sendByEnterRow || i == terminateSessionsRow || i == wifiDownloadRow ||
                    i == mobileDownloadRow || i == clearLogsRow || i == roamingDownloadRow || i == languageRow ||
                    i == switchBackendButtonRow || i == telegramFaqRow || i == contactsSortRow || i == contactsReimportRow || i == saveToGalleryRow || i == selectThemeRow ||
                    i == manageEmoticonRow;
        }

        @Override
        public int getCount() {
            return rowCount;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            int type = getItemViewType(i);
            if (type == 0) {
                if (view == null) {
                    LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.settings_name_layout, viewGroup, false);

                    ImageButton button = (ImageButton)view.findViewById(R.id.settings_edit_name);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            presentFragment(new SettingsChangeNameActivity());
                        }
                    });

                    final ImageButton button2 = (ImageButton)view.findViewById(R.id.settings_change_avatar_button);
                    button2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (getParentActivity() == null) {
                                return;
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());

                            CharSequence[] items;

                            TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                            if (user == null) {
                                user = UserConfig.getCurrentUser();
                            }
                            if (user == null) {
                                return;
                            }
                            boolean fullMenu = false;
                            if (user.photo != null && user.photo.photo_big != null && !(user.photo instanceof TLRPC.TL_userProfilePhotoEmpty)) {
                                items = new CharSequence[] {LocaleController.getString("OpenPhoto", R.string.OpenPhoto), LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley), LocaleController.getString("DeletePhoto", R.string.DeletePhoto)};
                                fullMenu = true;
                            } else {
                                items = new CharSequence[] {LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley)};
                            }

                            final boolean full = fullMenu;
                            builder.setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (i == 0 && full) {
                                        TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                                        if (user != null && user.photo != null && user.photo.photo_big != null) {
                                            PhotoViewer.getInstance().setParentActivity(getParentActivity());
                                            PhotoViewer.getInstance().openPhoto(user.photo.photo_big, SettingsActivity.this);
                                        }
                                    } else if (i == 0 && !full || i == 1 && full) {
                                        avatarUpdater.openCamera();
                                    } else if (i == 1 && !full || i == 2 && full) {
                                        avatarUpdater.openGallery();
                                    } else if (i == 3) {
                                        MessagesController.getInstance().deleteUserPhoto(null);
                                    }
                                }
                            });
                            showAlertDialog(builder);
                        }
                    });
                }
                TextView textView = (TextView)view.findViewById(R.id.settings_online);
                textView.setText(LocaleController.getString("Online", R.string.Online));

                textView = (TextView)view.findViewById(R.id.settings_name);
                Typeface typeface = AndroidUtilities.getTypeface("fonts/rmedium.ttf");
                textView.setTypeface(typeface);
                TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                if (user == null) {
                    user = UserConfig.getCurrentUser();
                }
                if (user != null) {
                    textView.setText(ContactsController.formatName(user.first_name, user.last_name));
                    BackupImageView avatarImage = (BackupImageView)view.findViewById(R.id.settings_avatar_image);
                    avatarImage.processDetach = false;
                    TLRPC.FileLocation photo = null;
                    TLRPC.FileLocation photoBig = null;
                    if (user.photo != null) {
                        photo = user.photo.photo_small;
                        photoBig = user.photo.photo_big;
                    }
                    //avatarImage.setImage(photo, "50_50", AndroidUtilities.getUserAvatarForId(user.id));
                    String placeHolderId = AndroidUtilities.getUserAvatarForId(user.id);

                    Drawable d = themeManager.getDrawable(placeHolderId, false);
                    Bitmap bm = ((BitmapDrawable)d).getBitmap();
                    Bitmap bm2 = Bitmap.createScaledBitmap(bm, 50, 50, true);
                    avatarImage.setImage(photo, "50_50", bm2);

                    avatarImage.imageReceiver.setVisible(!PhotoViewer.getInstance().isShowingImage(photoBig), false);
                }
                return view;
            } else if (type == 1) {
                if (view == null) {
                    LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.settings_section_layout, viewGroup, false);
                }
                TextView textView = (TextView)view.findViewById(R.id.settings_section_text);
                if (i == numberSectionRow) {
                    textView.setText(LocaleController.getString("YourPhoneNumber", R.string.YourPhoneNumber));
                } else if (i == settingsSectionRow) {
                    textView.setText(LocaleController.getString("SETTINGS", R.string.SETTINGS));
                } else if (i == supportSectionRow) {
                    textView.setText(LocaleController.getString("Support", R.string.Support));
                } else if (i == messagesSectionRow) {
                    textView.setText(LocaleController.getString("MessagesSettings", R.string.MessagesSettings));
                } else if (i == mediaDownloadSection) {
                    textView.setText(LocaleController.getString("AutomaticMediaDownload", R.string.AutomaticMediaDownload));
                } else if (i == contactsSectionRow) {
                    textView.setText(LocaleController.getString("Contacts", R.string.Contacts).toUpperCase());
                }
            } else if (type == 2) {
                if (view == null) {
                    LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.settings_row_button_layout, viewGroup, false);
                }
                TextView textView = (TextView)view.findViewById(R.id.settings_row_text);
                View divider = view.findViewById(R.id.settings_row_divider);
                if (i == numberRow) {
                    TLRPC.User user = UserConfig.getCurrentUser();
                    if (user != null && user.phone != null && user.phone.length() != 0) {
                        textView.setText(PhoneFormat.getInstance().format("+" + user.phone));
                    } else {
                        textView.setText("Unknown");
                    }
                    divider.setVisibility(View.INVISIBLE);
                } else if (i == notificationRow) {
                    textView.setText(LocaleController.getString("NotificationsAndSounds", R.string.NotificationsAndSounds));
                    divider.setVisibility(View.VISIBLE);
                } else if (i == blockedRow) {
                    textView.setText(LocaleController.getString("BlockedUsers", R.string.BlockedUsers));
                    divider.setVisibility(backgroundRow != 0 ? View.VISIBLE : View.INVISIBLE);
                } else if (i == backgroundRow) {
                    textView.setText(LocaleController.getString("ChatBackground", R.string.ChatBackground));
                    divider.setVisibility(View.VISIBLE);
                } else if (i == sendLogsRow) {
                    textView.setText("Send Logs");
                    divider.setVisibility(View.VISIBLE);
                } else if (i == clearLogsRow) {
                    textView.setText("Clear Logs");
                    divider.setVisibility(View.VISIBLE);
                }
                /*else if (i == askQuestionRow) {
                    textView.setText(LocaleController.getString("AskAQuestion", R.string.AskAQuestion));
                    divider.setVisibility(View.INVISIBLE);
                }*/
                else if (i == terminateSessionsRow) {
                    textView.setText(LocaleController.getString("TerminateAllSessions", R.string.TerminateAllSessions));
                    divider.setVisibility(View.INVISIBLE);
                } else if (i == switchBackendButtonRow) {
                    textView.setText("Switch Backend");
                    divider.setVisibility(View.VISIBLE);
                } else if (i == telegramFaqRow) {
                    textView.setText(LocaleController.getString("TelegramFAQ", R.string.TelegramFaq));
                    divider.setVisibility(View.VISIBLE);
                } else if (i == contactsReimportRow) {
                    textView.setText(LocaleController.getString("ImportContacts", R.string.ImportContacts));
                    divider.setVisibility(View.INVISIBLE);
                } else if(i == manageEmoticonRow) {
                    textView.setText(LocaleController.getString("ManageEmoticon", R.string.ManageEmoticon));
                    divider.setVisibility(View.VISIBLE);
                }
            } else if (type == 3) {
                if (view == null) {
                    LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.settings_row_check_layout, viewGroup, false);
                }
                TextView textView = (TextView)view.findViewById(R.id.settings_row_text);
                View divider = view.findViewById(R.id.settings_row_divider);
                ImageView checkButton = (ImageView)view.findViewById(R.id.settings_row_check_button);
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                if (i == enableAnimationsRow) {
                    textView.setText(LocaleController.getString("EnableAnimations", R.string.EnableAnimations));
                    divider.setVisibility(View.VISIBLE);
                    boolean enabled = preferences.getBoolean("view_animations", true);
                    if (enabled) {
                        checkButton.setImageResource(R.drawable.btn_check_on);
                    } else {
                        checkButton.setImageResource(R.drawable.btn_check_off);
                    }
                } else if (i == sendByEnterRow) {
                    textView.setText(LocaleController.getString("SendByEnter", R.string.SendByEnter));
                    divider.setVisibility(View.INVISIBLE);
                    boolean enabled = preferences.getBoolean("send_by_enter", false);
                    if (enabled) {
                        checkButton.setImageResource(R.drawable.btn_check_on);
                    } else {
                        checkButton.setImageResource(R.drawable.btn_check_off);
                    }
                } else if (i == saveToGalleryRow) {
                    textView.setText(LocaleController.getString("SaveToGallerySettings", R.string.SaveToGallerySettings));
                    divider.setVisibility(View.INVISIBLE);
                    if (MediaController.getInstance().canSaveToGallery()) {
                        checkButton.setImageResource(R.drawable.btn_check_on);
                    } else {
                        checkButton.setImageResource(R.drawable.btn_check_off);
                    }
                }
            } else if (type == 4) {
                if (view == null) {
                    LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.settings_logout_button, viewGroup, false);
                    TextView textView = (TextView)view.findViewById(R.id.settings_row_text);
                    textView.setText(LocaleController.getString("LogOut", R.string.LogOut));
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (getParentActivity() == null) {
                                return;
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                            builder.setMessage(LocaleController.getString("AreYouSureLogout", R.string.AreYouSureLogout));
                            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.clear().commit();
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.appDidLogout);
                                    MessagesController.getInstance().unregistedPush();
                                    MessagesController.getInstance().logOut();
                                    UserConfig.clearConfig();
                                    MessagesStorage.getInstance().cleanUp(false);
                                    MessagesController.getInstance().cleanUp();
                                    ContactsController.getInstance().deleteAllAppAccounts();
                                }
                            });
                            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                            showAlertDialog(builder);
                        }
                    });
                }
            } else if (type == 5) {
                if (view == null) {
                    LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.user_profile_leftright_row_layout, viewGroup, false);
                }
                TextView textView = (TextView)view.findViewById(R.id.settings_row_text);
                TextView detailTextView = (TextView)view.findViewById(R.id.settings_row_text_detail);
                View divider = view.findViewById(R.id.settings_row_divider);
                if (i == textSizeRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    int size = preferences.getInt("fons_size", AndroidUtilities.isTablet() ? 18 : 16);
                    detailTextView.setText(String.format("%d", size));
                    textView.setText(LocaleController.getString("TextSize", R.string.TextSize));
                    divider.setVisibility(View.VISIBLE);
                } else if (i == languageRow) {
                    detailTextView.setText(LocaleController.getCurrentLanguageName());
                    textView.setText(LocaleController.getString("Language", R.string.Language));
                    divider.setVisibility(View.VISIBLE);
                } else if (i == contactsSortRow) {
                    textView.setText(LocaleController.getString("SortBy", R.string.SortBy));
                    divider.setVisibility(View.VISIBLE);
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    int sort = preferences.getInt("sortContactsBy", 0);
                    if (sort == 0) {
                        detailTextView.setText(LocaleController.getString("Default", R.string.Default));
                    } else if (sort == 1) {
                        detailTextView.setText(LocaleController.getString("FirstName", R.string.SortFirstName));
                    } else if (sort == 2) {
                        detailTextView.setText(LocaleController.getString("LastName", R.string.SortLastName));
                    }
                }
            } else if (type == 6) {
                if (view == null) {
                    LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.settings_row_version, viewGroup, false);
                    TextView textView = (TextView)view.findViewById(R.id.settings_row_text);
                    try {
                        PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                        //textView.setText(String.format(Locale.US, "Telegram for Android v%s (%d)", pInfo.versionName, pInfo.versionCode));
                        textView.setText("Themegram for Android");
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }
            } else if (type == 7) {
                if (view == null) {
                    LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.settings_row_detail_layout, viewGroup, false);
                }
                TextView textView = (TextView)view.findViewById(R.id.settings_row_text);
                TextView textViewDetail = (TextView)view.findViewById(R.id.settings_row_text_detail);
                View divider = view.findViewById(R.id.settings_row_divider);

                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                int mask = 0;
                String text = "";
                if (i == mobileDownloadRow) {
                    textView.setText(LocaleController.getString("WhenUsingMobileData", R.string.WhenUsingMobileData));
                    divider.setVisibility(View.VISIBLE);
                    mask = MediaController.getInstance().mobileDataDownloadMask;
                } else if (i == wifiDownloadRow) {
                    textView.setText(LocaleController.getString("WhenConnectedOnWiFi", R.string.WhenConnectedOnWiFi));
                    divider.setVisibility(View.VISIBLE);
                    mask = MediaController.getInstance().wifiDownloadMask;
                } else if (i == roamingDownloadRow) {
                    textView.setText(LocaleController.getString("WhenRoaming", R.string.WhenRoaming));
                    divider.setVisibility(View.VISIBLE);
                    mask = MediaController.getInstance().roamingDownloadMask;
                } else if(i == selectThemeRow) {
                    textView.setText(LocaleController.getString("SelectTheme", R.string.SelectTheme));
                    divider.setVisibility(View.VISIBLE);
                    text = themeManager.getCurrentThemeName();
                }
                if ((mask & MediaController.AUTODOWNLOAD_MASK_PHOTO) != 0) {
                    text += LocaleController.getString("AttachPhoto", R.string.AttachPhoto);
                }
                if ((mask & MediaController.AUTODOWNLOAD_MASK_AUDIO) != 0) {
                    if (text.length() != 0) {
                        text += ", ";
                    }
                    text += LocaleController.getString("AttachAudio", R.string.AttachAudio);
                }
                if ((mask & MediaController.AUTODOWNLOAD_MASK_VIDEO) != 0) {
                    if (text.length() != 0) {
                        text += ", ";
                    }
                    text += LocaleController.getString("AttachVideo", R.string.AttachVideo);
                }
                if ((mask & MediaController.AUTODOWNLOAD_MASK_DOCUMENT) != 0) {
                    if (text.length() != 0) {
                        text += ", ";
                    }
                    text += LocaleController.getString("AttachDocument", R.string.AttachDocument);
                }
                if (text.length() == 0) {
                    text = LocaleController.getString("NoMediaAutoDownload", R.string.NoMediaAutoDownload);
                }
                textViewDetail.setText(text);
            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if (i == profileRow) {
                return 0;
            } else if (i == numberSectionRow || i == settingsSectionRow || i == supportSectionRow || i == messagesSectionRow || i == mediaDownloadSection || i == contactsSectionRow) {
                return 1;
            } else if (i == textSizeRow || i == languageRow || i == contactsSortRow) {
                return 5;
            } else if (i == enableAnimationsRow || i == sendByEnterRow || i == saveToGalleryRow) {
                return 3;
            } else if (i == numberRow || i == notificationRow || i == blockedRow || i == backgroundRow /*|| i == askQuestionRow*/ || i == sendLogsRow || i == terminateSessionsRow || i == clearLogsRow || i == switchBackendButtonRow || i == telegramFaqRow || i == contactsReimportRow || i == manageEmoticonRow) {
                return 2;
            } else if (i == logoutRow) {
                return 4;
            } else if (i == versionRow) {
                return 6;
            } else if (i == wifiDownloadRow || i == mobileDownloadRow || i == roamingDownloadRow || i == selectThemeRow) {
                return 7;
            } else {
                return 2;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 8;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}
