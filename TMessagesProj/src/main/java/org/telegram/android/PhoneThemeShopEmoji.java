package org.telegram.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by OOO on 2014-10-28.
 */
public class PhoneThemeShopEmoji {
    public Drawable emojis;
    public int col;
    public int row;
    public int total;
    private int twidth;
    private int theight;
    private int width;
    private int height;
    private Bitmap bmp;
    private Paint paint = new Paint();

    public PhoneThemeShopEmoji (Drawable _emojis, int _col, int _row, int _total ) {
        paint.setFilterBitmap(true);

        emojis = _emojis;
        col = _col;
        row = _row;
        total = _total;
        twidth = emojis.getIntrinsicWidth();
        theight = emojis.getIntrinsicHeight();
        width = twidth / col;
        height = theight / row;
        bmp = drawableToBitmap(emojis);
    }

    private static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public Bitmap getBitmap( int index ) {
        Bitmap targetBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        targetBitmap = Bitmap.createBitmap(bmp, ( index % col ) * width, ( index / row ) * height, width, height);

        return targetBitmap;
    }
}
