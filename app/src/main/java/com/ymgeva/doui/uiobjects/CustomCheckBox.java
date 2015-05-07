package com.ymgeva.doui.uiobjects;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageView;

/**
 * Created by Yoav on 5/7/15.
 */
public class CustomCheckBox extends ImageView implements Checkable {

    private boolean mChecked;
    private Drawable mDrawable;

    public CustomCheckBox(Context context) {
        super(context);
        mDrawable = getDrawable();
    }

    public CustomCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDrawable = getDrawable();
    }

    public CustomCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
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
        setImageDrawable(b ? mDrawable : null);
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
