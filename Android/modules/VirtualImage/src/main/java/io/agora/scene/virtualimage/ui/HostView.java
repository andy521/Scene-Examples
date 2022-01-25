package io.agora.scene.virtualimage.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import io.agora.example.base.BaseUtil;
import io.agora.scene.virtualimage.R;

public class HostView extends ConstraintLayout {

    private boolean isSingleHost = true;

    private final FrameLayout currentViewport;
    private final FrameLayout targetViewport;

    private final CardView viewportContainer;

    public HostView(@NonNull Context context) {
        this(context, null);
    }

    public HostView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HostView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HostView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        currentViewport = new FrameLayout(context);
        currentViewport.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        targetViewport = new FrameLayout(context);
        currentViewport.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // Init CardView
        LayoutParams layoutParams = new LayoutParams(0, 0);
        layoutParams.endToEnd = ConstraintSet.PARENT_ID;
        layoutParams.topToTop = ConstraintSet.PARENT_ID;
        layoutParams.matchConstraintPercentWidth = 0.28f;
        layoutParams.dimensionRatio = "105:140";

        viewportContainer = new CardView(context);
        viewportContainer.setCardElevation(BaseUtil.dp2px(4));
        viewportContainer.setCardBackgroundColor(Color.GRAY);
        viewportContainer.setRadius(BaseUtil.dp2px(8));
        viewportContainer.setLayoutParams(layoutParams);

        TextView viewportTextView = new TextView(context);
        viewportTextView.setText(R.string.virtual_image_remote_video);
        viewportTextView.setTextColor(Color.WHITE);
        viewportTextView.setGravity(Gravity.CENTER);
        viewportContainer.addView(viewportTextView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void configViewForCurrentState() {
        if (isSingleHost) { // 只有一个人
            safeAddView(currentViewport);
            safeAddView(viewportContainer);
            safeDetachView(targetViewport);
        } else { // 两个人
            safeAddView(currentViewport);
            safeAddView(viewportContainer);
            safeAddView(viewportContainer, targetViewport);
        }
    }

    private void safeAddView(@NonNull View view) {
        safeAddView(this, view);
    }

    private void safeAddView(@NonNull ViewGroup parent, @NonNull View view) {
        if (view.getParent() != null) {
            if (view.getParent() != parent) {
                ((ViewGroup) view.getParent()).removeView(view);
                parent.addView(view);
            }
        } else
            parent.addView(view);
    }

    private void safeDetachView(View view) {
        if (view.getParent() instanceof ViewGroup){
            ((ViewGroup)view.getParent()).removeView(view);
        }
    }

    public boolean isSingleHost() {
        return isSingleHost;
    }

    public void setSingleHost(boolean singleHost) {
        isSingleHost = singleHost;
        configViewForCurrentState();
    }

    public void setViewportContainerVisible(boolean visible){
        viewportContainer.setVisibility(visible ? View.VISIBLE: View.GONE);
    }

    @NonNull
    public FrameLayout getCurrentViewport() {
        return currentViewport;
    }

    @NonNull
    public FrameLayout getTargetViewport() {
        return targetViewport;
    }

    @NonNull
    public CardView getViewportContainer() {
        return viewportContainer;
    }
}
