package com.example.medicationadherence.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

public class DisableableScrollView extends NestedScrollView {
    private boolean scrollEnabled = false;

    public DisableableScrollView(@NonNull Context context) {
        super(context);
    }

    public DisableableScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DisableableScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScrollEnabled(boolean scrollEnabled) {
        if(!scrollEnabled)
            scrollTo(0,0);
        this.scrollEnabled = scrollEnabled;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(ev.getAction() > 0 && ev.getAction() < 4 && !scrollEnabled)
            return false;
        return super.onTouchEvent(ev);
    }
}