package io.agora.scene.virtualimage.ui.create;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.faceunity.pta_art.fragment.EditFaceFragment;

import io.agora.example.base.BaseUtil;
import io.agora.scene.virtualimage.GlobalViewModel;
import io.agora.scene.virtualimage.R;
import io.agora.scene.virtualimage.base.BaseNavFragment;
import io.agora.scene.virtualimage.bean.RoomInfo;
import io.agora.scene.virtualimage.databinding.VirtualImageFragmentCreateRoomBinding;
import io.agora.scene.virtualimage.manager.FUManager;
import io.agora.scene.virtualimage.manager.RtcManager;
import io.agora.scene.virtualimage.util.EventObserver;
import io.agora.scene.virtualimage.util.OneUtil;

public class RoomCreateFragment extends BaseNavFragment<VirtualImageFragmentCreateRoomBinding> {

    private GlobalViewModel mGlobalModel;
    private EditFaceFragment mEditFaceFragment;
    private CardView localCameraView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGlobalModel = OneUtil.getViewModel(requireActivity(), GlobalViewModel.class);
        initListener();

        setupRandomName();

        RtcManager.getInstance().renderLocalAvatarVideo(mBinding.videoContainer);
        FUManager.getInstance().start();
        initLocalCameraView();
    }

    private void initLocalCameraView() {
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(0, 0);
        layoutParams.startToStart = ConstraintSet.PARENT_ID;
        layoutParams.bottomToBottom = ConstraintSet.PARENT_ID;
        layoutParams.matchConstraintPercentWidth = 0.28f;
        layoutParams.dimensionRatio = "105:140";
        layoutParams.bottomMargin = (int) BaseUtil.dp2px(120);
        layoutParams.leftMargin = (int) BaseUtil.dp2px(30);

        localCameraView = new CardView(getContext());
        localCameraView.setCardElevation(BaseUtil.dp2px(4));
        localCameraView.setCardBackgroundColor(Color.GRAY);
        localCameraView.setRadius(BaseUtil.dp2px(8));
        localCameraView.setLayoutParams(layoutParams);
        mBinding.getRoot().addView(localCameraView);

        FrameLayout targetViewport = new FrameLayout(getContext());
        localCameraView.addView(targetViewport);
        RtcManager.getInstance().renderLocalCameraVideo(targetViewport);
    }

    private void initListener() {
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.toolbarFgCreate, (v, insets) -> {
            Insets inset = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            mBinding.getRoot().setPaddingRelative(inset.left,0,inset.right,inset.bottom);
            mBinding.toolbarFgCreate.setPaddingRelative(inset.left,inset.top,inset.right,0);
            return WindowInsetsCompat.CONSUMED;
        });
        // 监听"返回键"
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(mEditFaceFragment != null){
                    mEditFaceFragment.onBackPressed();
                    return;
                }
                navigateToStartPage();
            }
        });
        mBinding.toolbarFgCreate.setNavigationOnClickListener((v) -> navigateToStartPage());
        mBinding.btnRandomFgCreate.setOnClickListener((v) -> setupRandomName());
        mBinding.btnLiveFgCreate.setOnClickListener((v) -> {
            if(mEditFaceFragment != null){
                mEditFaceFragment.onBackPressed();
                return;
            }
            startLive();
        });
        mGlobalModel.roomInfo.observe(getViewLifecycleOwner(), new EventObserver<>(this::onRoomInfoChanged));
        mBinding.btnBeauty.setOnClickListener(v -> {
            localCameraView.setVisibility(View.GONE);
            mEditFaceFragment = new EditFaceFragment();
            mEditFaceFragment.setOnCloseListener(()->{
                setCreateLiveViewsVisible(true);
                if(mEditFaceFragment != null){
                    getChildFragmentManager().beginTransaction()
                            .remove(mEditFaceFragment)
                            .commit();
                    mEditFaceFragment = null;
                }
                localCameraView.setVisibility(View.VISIBLE);
            });
            setCreateLiveViewsVisible(false);
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.bottom_dialog_container, mEditFaceFragment, null)
                    .commit();
        });
    }

    private void setCreateLiveViewsVisible(boolean visible){
        mBinding.toolbarFgCreate.setVisibility(visible ? View.VISIBLE: View.GONE);
        mBinding.titleNameBg.setVisibility(visible ? View.VISIBLE: View.GONE);
        mBinding.titleNameFgCreate.setVisibility(visible ? View.VISIBLE: View.GONE);
        mBinding.nameFgCreate.setVisibility(visible ? View.VISIBLE: View.GONE);
        mBinding.btnRandomFgCreate.setVisibility(visible ? View.VISIBLE: View.GONE);
        mBinding.btnLiveFgCreate.setVisibility(visible ? View.VISIBLE: View.GONE);
        mBinding.btnBeauty.setVisibility(visible ? View.VISIBLE: View.GONE);
    }

    /**
     * 开始直播
     */
    private void startLive() {
        showLoading();
        RoomInfo roomInfo = new RoomInfo(mBinding.nameFgCreate.getText().toString(), mGlobalModel.localUser.getUserId());
        mGlobalModel.createRoom(roomInfo);
    }

    private void onRoomInfoChanged(RoomInfo roomInfo) {
        dismissLoading();
        if (roomInfo == null) {
            BaseUtil.toast(requireContext(),"create failed");
        } else {
            findNavController().popBackStack();
        }
    }

    private void setupRandomName() {
        String currentName = OneUtil.getRandomRoomName();
        mBinding.nameFgCreate.setText(currentName);
    }

    private void navigateToStartPage() {
        RtcManager.getInstance().reset(true);
        findNavController().popBackStack(R.id.roomListFragment, false);
    }
}
