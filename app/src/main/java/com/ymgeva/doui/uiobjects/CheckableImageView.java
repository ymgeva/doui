package com.ymgeva.doui.uiobjects;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageView;

/**
 * Created by Yoav on 5/7/15.
 */
public class CheckableImageView extends ImageView implements Checkable {

    private boolean mChecked;
    private Drawable mDrawable;

    public CheckableImageView(Context context) {
        super(context);
        mDrawable = getDrawable();
    }

    public CheckableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDrawable = getDrawable();
    }

    public CheckableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDrawable = getDrawable();
    }

    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
    }

    @Override
    public void setChecked(boolean b) {
        mChecked = b;
        setAlpha(b ? 1f : 0.2f);
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }
}
