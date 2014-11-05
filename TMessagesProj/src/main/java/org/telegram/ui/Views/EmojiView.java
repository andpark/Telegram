/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.ui.Views;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teamjihu.EmoticonManager;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.Emoji;
import org.telegram.android.LocaleController;
import org.telegram.android.PhoneThemeShopEmoji;
import org.telegram.messenger.phonethemeshop.R;
import org.telegram.ui.ChatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class EmojiView extends LinearLayout {
    private ArrayList<EmojiGridAdapter> adapters = new ArrayList<EmojiGridAdapter>();
    private List<Drawable> iconsDrawable = new ArrayList<Drawable>();
    private int[] icons = {
            R.drawable.ic_emoji_recent,
            R.drawable.ic_emoji_smile,
            R.drawable.ic_emoji_flower,
            R.drawable.ic_emoji_bell,
            R.drawable.ic_emoji_car,
            R.drawable.ic_emoji_symbol };
    private Listener listener;
    private ViewPager pager;
    private FrameLayout recentsWrap;
    private ArrayList<GridView> views = new ArrayList<GridView>();

    EmoticonManager emoticonManager;
    ChatActivity chatActivity;

    private List<PhoneThemeShopEmoji> PhoneThemeShopEmojiList = new ArrayList<PhoneThemeShopEmoji>();

    public EmojiView(Context paramContext) {
        super(paramContext);
        emoticonManager = new EmoticonManager(paramContext);
        init();
    }

    public EmojiView(Context paramContext, ChatActivity _chatActivity) {
        super(paramContext);
        emoticonManager = new EmoticonManager(paramContext);
        chatActivity = _chatActivity;
        init();
    }

    public EmojiView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        emoticonManager = new EmoticonManager(paramContext);
        init();
    }

    public EmojiView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        emoticonManager = new EmoticonManager(paramContext);
        init();
    }

    private void addToRecent(long paramLong) {
        if (this.pager.getCurrentItem() == 0) {
            return;
        }
        ArrayList<Long> localArrayList = new ArrayList<Long>();
        long[] currentRecent = Emoji.data[0];
        boolean was = false;
        for (long aCurrentRecent : currentRecent) {
            if (paramLong == aCurrentRecent) {
                localArrayList.add(0, paramLong);
                was = true;
            } else {
                localArrayList.add(aCurrentRecent);
            }
        }
        if (!was) {
            localArrayList.add(0, paramLong);
        }
        Emoji.data[0] = new long[Math.min(localArrayList.size(), 50)];
        for (int q = 0; q < Emoji.data[0].length; q++) {
            Emoji.data[0][q] = localArrayList.get(q);
        }
        adapters.get(0).data = Emoji.data[0];
        adapters.get(0).notifyDataSetChanged();
        saveRecents();
    }

    private String convert(long paramLong) {
        String str = "";
        for (int i = 0; ; i++) {
            if (i >= 4) {
                return str;
            }
            int j = (int)(0xFFFF & paramLong >> 16 * (3 - i));
            if (j != 0) {
                str = str + (char)j;
            }
        }
    }

    private void getPhoneThemeShopEmoji() {
        for ( int i = 0; i < icons.length; i++ )
            iconsDrawable.add(getResources().getDrawable(icons[i]));

        List<PhoneThemeShopEmoji> emoticonList = emoticonManager.GetEmoticonList();

        for ( int i = emoticonList.size() - 1 ; i >= 0; i-- ) {
            iconsDrawable.add(1, emoticonList.get(i).icon);
            PhoneThemeShopEmojiList.add(0, emoticonList.get(i));
        }
    }

    private void init() {
        getPhoneThemeShopEmoji();
        setOrientation(LinearLayout.VERTICAL);

        Emoji.emojiTotalCnt = ( Emoji.data.length + PhoneThemeShopEmojiList.size() );

        for (int i = 0; i < Emoji.emojiTotalCnt; i++) {
            boolean isPhoneThemeShopEmoji = false;
            if ( i != 0 && i <= PhoneThemeShopEmojiList.size() )
                isPhoneThemeShopEmoji = true;

            GridView gridView = new GridView(getContext());
            if (AndroidUtilities.isTablet()) {
                gridView.setColumnWidth(AndroidUtilities.dp(( isPhoneThemeShopEmoji ) ? 120 : 60));
            } else {
                gridView.setColumnWidth(AndroidUtilities.dp(( isPhoneThemeShopEmoji ) ? 90 : 45));
            }
            gridView.setNumColumns(-1);
            views.add(gridView);

            EmojiGridAdapter localEmojiGridAdapter;
            if ( isPhoneThemeShopEmoji )
                localEmojiGridAdapter = new EmojiGridAdapter(null, PhoneThemeShopEmojiList.get(i - 1), isPhoneThemeShopEmoji);
            else {
                if ( i == 0 )
                    localEmojiGridAdapter = new EmojiGridAdapter(Emoji.data[i], null, isPhoneThemeShopEmoji);
                else
                    localEmojiGridAdapter = new EmojiGridAdapter(Emoji.data[i - PhoneThemeShopEmojiList.size()], null, isPhoneThemeShopEmoji);
            }
            gridView.setAdapter(localEmojiGridAdapter);
            adapters.add(localEmojiGridAdapter);
        }

        setBackgroundDrawable(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] { -14145496, -16777216 }));
        pager = new ViewPager(getContext());
        pager.setAdapter(new EmojiPagesAdapter());
        PagerSlidingTabStrip tabs = new PagerSlidingTabStrip(getContext());
        tabs.setViewPager(pager);
        tabs.setShouldExpand(true);
        tabs.setIndicatorColor(0xff33b5e5);
        tabs.setIndicatorHeight(AndroidUtilities.dpf(2.0f));
        tabs.setUnderlineHeight(AndroidUtilities.dpf(2.0f));
        tabs.setUnderlineColor(0x66000000);
        tabs.setTabBackground(0);
        LinearLayout localLinearLayout = new LinearLayout(getContext());
        localLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        localLinearLayout.addView(tabs, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
        ImageView localImageView = new ImageView(getContext());
        localImageView.setImageResource(R.drawable.ic_emoji_backspace);
        localImageView.setScaleType(ImageView.ScaleType.CENTER);
        localImageView.setBackgroundResource(R.drawable.bg_emoji_bs);
        localImageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (EmojiView.this.listener != null) {
                    EmojiView.this.listener.onBackspace();
                }
            }
        });
        localLinearLayout.addView(localImageView, new LinearLayout.LayoutParams(AndroidUtilities.dp(61), LayoutParams.MATCH_PARENT));
        recentsWrap = new FrameLayout(getContext());
        recentsWrap.addView(views.get(0));
        TextView localTextView = new TextView(getContext());
        localTextView.setText(LocaleController.getString("NoRecent", R.string.NoRecent));
        localTextView.setTextSize(18.0f);
        localTextView.setTextColor(-7829368);
        localTextView.setGravity(17);
        recentsWrap.addView(localTextView);
        views.get(0).setEmptyView(localTextView);
        addView(localLinearLayout, new LinearLayout.LayoutParams(-1, AndroidUtilities.dpf(48.0f)));
        addView(pager);
        loadRecents();
        if (Emoji.data[0] == null || Emoji.data[0].length == 0) {
            pager.setCurrentItem(1);
        }
    }

    private void saveRecents() {
        ArrayList<Long> localArrayList = new ArrayList<Long>();
        long[] arrayOfLong = Emoji.data[0];
        int i = arrayOfLong.length;
        for (int j = 0; ; j++) {
            if (j >= i) {
                getContext().getSharedPreferences("emoji", 0).edit().putString("recents", TextUtils.join(",", localArrayList)).commit();
                return;
            }
            localArrayList.add(arrayOfLong[j]);
        }
    }

    public void loadRecents() {
        String str = getContext().getSharedPreferences("emoji", 0).getString("recents", "");
        String[] arrayOfString = null;
        if ((str != null) && (str.length() > 0)) {
            arrayOfString = str.split(",");
            Emoji.data[0] = new long[arrayOfString.length];
        }
        if (arrayOfString != null) {
            for (int i = 0; i < arrayOfString.length; i++) {
                Emoji.data[0][i] = Long.parseLong(arrayOfString[i]);
            }
            adapters.get(0).data = Emoji.data[0];
            adapters.get(0).notifyDataSetChanged();
        }
    }

    public void onMeasure(int paramInt1, int paramInt2) {
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt2), MeasureSpec.EXACTLY));
    }

    public void setListener(Listener paramListener) {
        this.listener = paramListener;
    }

    public void invalidateViews() {
        for (GridView gridView : views) {
            if (gridView != null) {
                gridView.invalidateViews();
            }
        }
    }

    private class EmojiGridAdapter extends BaseAdapter {
        long[] data;
        PhoneThemeShopEmoji phoneThemeShopEmoji;
        boolean isPhoneThemeShopEmoji;

        public EmojiGridAdapter(long[] arg2, PhoneThemeShopEmoji _phoneThemeShopEmoji, boolean _isPhoneThemeShopEmoji) {
            this.data = arg2;
            phoneThemeShopEmoji = _phoneThemeShopEmoji;
            isPhoneThemeShopEmoji = _isPhoneThemeShopEmoji;
        }

        public int getCount() {
            int cnt = 0;

            //return data.length;
            return ( isPhoneThemeShopEmoji ) ? phoneThemeShopEmoji.total : data.length;
        }

        public Object getItem(int i) {
            return null;
        }

        public long getItemId(int i) {
            //return data[i];
            return ( isPhoneThemeShopEmoji ) ? i : data[i];
        }

        public View getView(int i, View view, ViewGroup paramViewGroup) {
            ImageView imageView = (ImageView)view;
            if (imageView == null) {
                imageView = new ImageView(EmojiView.this.getContext()) {
                    public void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2) {
                        setMeasuredDimension(View.MeasureSpec.getSize(paramAnonymousInt1), View.MeasureSpec.getSize(paramAnonymousInt1));
                    }
                };
                imageView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        if ( isPhoneThemeShopEmoji ) {
                            if ( chatActivity != null ) {
                                ArrayList<String> photos = new ArrayList<String>();
                                //photos.add("/storage/emulated/0/Download/KakaoTalk_20140815_150707302.png");
                                File tempFile = null;
                                Bitmap bmp = (Bitmap)view.getTag();
                                try {
                                    //tempFile = File.createTempFile("tempEmojiFile.png", null, getContext().getCacheDir());
                                    tempFile = File.createTempFile("tempEmojiFile.jpg", null, getContext().getCacheDir());
                                    OutputStream outputStream = new FileOutputStream(tempFile);
                                    //bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                                    outputStream.flush();
                                    outputStream.close();

                                    photos.add(tempFile.getPath());
                                } catch ( IOException ie ) {
                                    ie.printStackTrace();
                                }
                                chatActivity.didSelectPhotos(photos);
                            }
                        } else {
                            if (EmojiView.this.listener != null) {
                                EmojiView.this.listener.onEmojiSelected(EmojiView.this.convert((Long) view.getTag()));
                            }
                            EmojiView.this.addToRecent((Long) view.getTag());
                        }
                    }
                });
                imageView.setBackgroundResource(R.drawable.list_selector);
                imageView.setScaleType(ImageView.ScaleType.CENTER);
            }
            if ( isPhoneThemeShopEmoji ) {
                Bitmap bmp = phoneThemeShopEmoji.getBitmap(i);
                imageView.setImageBitmap(bmp);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setTag(bmp);
            } else {
                imageView.setImageDrawable(Emoji.getEmojiBigDrawable(data[i]));
                imageView.setTag(data[i]);
            }
            return imageView;
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }

    private class EmojiPagesAdapter extends PagerAdapter implements PagerSlidingTabStrip.IconTabProvider {

        private EmojiPagesAdapter() {
        }

        public void destroyItem(ViewGroup paramViewGroup, int paramInt, Object paramObject) {
            View localObject;
            if (paramInt == 0) {
                localObject = EmojiView.this.recentsWrap;
            } else {
                localObject = EmojiView.this.views.get(paramInt);
            }
            paramViewGroup.removeView(localObject);
        }

        public int getCount() {
            return EmojiView.this.views.size();
        }

        //public int getPageIconResId(int paramInt) {
        public Drawable getPageIconResId(int paramInt) {

            //return EmojiView.this.icons[paramInt];
            return EmojiView.this.iconsDrawable.get(paramInt);
        }

        public Object instantiateItem(ViewGroup paramViewGroup, int paramInt) {
            View localObject;
            if (paramInt == 0) {
                localObject = EmojiView.this.recentsWrap;
            } else {
                localObject = EmojiView.this.views.get(paramInt);
            }
            paramViewGroup.addView(localObject);
            return localObject;
        }

        public boolean isViewFromObject(View paramView, Object paramObject) {
            return paramView == paramObject;
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }

    public static abstract interface Listener {
        public abstract void onBackspace();
        public abstract void onEmojiSelected(String paramString);
    }
}