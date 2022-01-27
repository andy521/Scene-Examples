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

import java.util.ArrayList;
import java.util.List;

import io.agora.example.base.BaseUtil;
import io.agora.scene.virtualimage.R;

public class HostView extends ConstraintLayout {

    private final FrameLayout currentViewport;
    private final List<FrameLayout> targetViewportList = new ArrayList<>();
    private final List<CardView> viewportContainerList = new ArrayList<>();

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
        safeAddView(currentViewport);

        int lastOneViewId = View.NO_ID;
        for (int i = 0; i < 1; i++) {
            LayoutParams layoutParams = new LayoutParams(0, 0);
            layoutParams.endToEnd = lastOneViewId == View.NO_ID ? ConstraintSet.PARENT_ID: lastOneViewId;
            layoutParams.topToTop = lastOneViewId == View.NO_ID ? ConstraintSet.PARENT_ID: lastOneViewId;
            layoutParams.matchConstraintPercentWidth = 0.28f;
            layoutParams.dimensionRatio = "105:140";

            lastOneViewId = View.generateViewId();
            CardView viewportContainer = new CardView(context);
            viewportContainer.setCardElevation(BaseUtil.dp2px(4));
            viewportContainer.setCardBackgroundColor(Color.GRAY);
            viewportContainer.setRadius(BaseUtil.dp2px(8));
            viewportContainer.setLayoutParams(layoutParams);
            viewportContainer.setId(lastOneViewId);

            TextView viewportTextView = new TextView(context);
            viewportTextView.setText(R.string.virtual_image_remote_video);
            viewportTextView.setTextColor(Color.WHITE);
            viewportTextView.setGravity(Gravity.CENTER);
            viewportContainer.addView(viewportTextView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            viewportContainerList.add(viewportContainer);
            safeAddView(viewportContainer);

            FrameLayout targetViewport = new FrameLayout(context);
            targetViewportList.add(targetViewport);
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

    public void setViewportContainersVisible(boolean visible){
        for (CardView cardView : viewportContainerList) {
            cardView.setVisibility(visible ? View.VISIBLE: View.GONE);
        }
    }

    @NonNull
    public FrameLayout getCurrentViewport() {
        return currentViewport;
    }

    @NonNull
    public FrameLayout getTargetViewport(int index) {
        if(index >= targetViewportList.size() || index < 0){
            throw new RuntimeException("getTargetViewport index=" + index + " out of the list size");
        }
        return targetViewportList.get(index);
    }

    @NonNull
    public CardView getViewportContainer(int index) {
        if(index >= viewportContainerList.size() || index < 0){
            throw new RuntimeException("getViewportContainer index=" + index + " out of the list size");
        }
        return viewportContainerList.get(index);
    }

    public void setViewportTarget(int index, boolean attach){
        if(index >= targetViewportList.size() || index < 0){
            throw new RuntimeException("removeViewportTarget index=" + index + " out of the list size");
        }
        FrameLayout targetViewport = targetViewportList.get(index);
        FrameLayout viewportContainer = viewportContainerList.get(index);
        if(attach){
            safeAddView(viewportContainer, targetViewport);
        }else{
            safeDetachView(targetViewport);
        }
    }
}
