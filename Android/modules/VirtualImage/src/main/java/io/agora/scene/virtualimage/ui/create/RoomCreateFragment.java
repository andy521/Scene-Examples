package io.agora.scene.virtualimage.ui.create;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGlobalModel = OneUtil.getViewModel(requireActivity(), GlobalViewModel.class);
        initListener();

        RtcManager.getInstance().renderLocalVideo(mBinding.videoContainer, null);
        FUManager.getInstance().start();

        setupRandomName();
    }

    private void initListener() {
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.getRoot(), (v, insets) -> {
            Insets inset = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            mBinding.getRoot().setPaddingRelative(inset.left,inset.top,inset.right,inset.bottom);
//            // 顶部
//            mBinding.toolbarFgCreate.setPadding(0,inset.top,0,0);
//            // 底部
//            ConstraintLayout.LayoutParams lpBtn = (ConstraintLayout.LayoutParams) mBinding.btnLiveFgCreate.getLayoutParams();
//            lpBtn.bottomMargin = inset.bottom + ((int) BaseUtil.dp2px(36));
//            mBinding.btnLiveFgCreate.setLayoutParams(lpBtn);

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
            mEditFaceFragment = new EditFaceFragment();
            mEditFaceFragment.setOnCloseListener(()->{
                setCreateLiveViewsVisible(true);
                if(mEditFaceFragment != null){
                    getChildFragmentManager().beginTransaction()
                            .remove(mEditFaceFragment)
                            .commit();
                    mEditFaceFragment = null;
                }
            });
            setCreateLiveViewsVisible(false);
            getChildFragmentManager().beginTransaction()
                    .add(R.id.bottom_dialog_container, mEditFaceFragment, null)
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
