package io.agora.uiwidget.function;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.uiwidget.R;

public class PreviewControlView extends BaseFunctionView{
    public PreviewControlView(@NonNull Context context) {
        this(context, null);
    }

    public PreviewControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreviewControlView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.preview_control_layout, this, true);
    }



}
