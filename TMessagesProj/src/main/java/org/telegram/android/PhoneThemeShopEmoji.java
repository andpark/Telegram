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
    public Drawable icon;
    public Drawable emojis;
    public int col;
    public int row;
    public int total;
    public String pkg;
    public String name;
    private int twidth;
    private int theight;
    private int width;
    private int height;
    private Bitmap bmp;
    private Paint paint = new Paint();
    private final int MAX_WIDTH_HEIGHT = 120;

    public PhoneThemeShopEmoji (String _pkg, String _name, Drawable _icon, Drawable _emojis, int _col, int _row, int _total ) {
        paint.setFilterBitmap(true);

        pkg = _pkg;
        name = _name;
        icon = _icon;
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
        int _width = MAX_WIDTH_HEIGHT;
        int _height = MAX_WIDTH_HEIGHT;

        if ( width > height )
            _height = _height * height / width;
        else
            _width = _width * width / height;

        Bitmap targetBitmap = Bitmap.createBitmap(bmp, ( index % col ) * width, ( index / col ) * height, width, height);
        //targetBitmap = Bitmap.createScaledBitmap(targetBitmap, _width, _height, true);

        return targetBitmap;
    }
}
