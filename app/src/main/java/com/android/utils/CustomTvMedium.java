package com.android.utils;


import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class CustomTvMedium extends androidx.appcompat.widget.AppCompatTextView {

    public CustomTvMedium(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CustomTvMedium(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomTvMedium(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "Montserrat-Medium.ttf");
            setTypeface(tf);
        }
    }
}
