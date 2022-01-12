package com.faceunity.pta_art.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioGroup;

import androidx.appcompat.widget.AppCompatRadioButton;

public class CustomRadioButtom extends AppCompatRadioButton {
    public CustomRadioButtom(Context context) {
        super(context);
    }

    public CustomRadioButtom(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
        if (!isChecked()) {
            ((RadioGroup) getParent()).clearCheck();
        }
    }
}
