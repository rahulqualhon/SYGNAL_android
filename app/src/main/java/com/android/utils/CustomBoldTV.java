package com.android.utils;


import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;


public class CustomBoldTV extends androidx.appcompat.widget.AppCompatTextView {

    public CustomBoldTV(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CustomBoldTV(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomBoldTV(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "Montserrat-Bold.ttf");

            setTypeface(tf);
        }
    }
}