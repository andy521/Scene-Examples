package io.agora.scene.virtualimage.ui.create;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGlobalModel = OneUtil.getViewModel(requireActivity(), GlobalViewModel.class);
        initListener();

        RtcManager.getInstance().setVideoPreProcess(new RtcManager.VideoPreProcess() {
            @Override
            public RtcManager.ProcessVideoFrame processVideoFrameTex(byte[] img, int texId, float[] texMatrix, int width, int height, int cameraType) {
                FUManager.FuVideoFrame fuVideoFrame = FUManager.getInstance().processVideoFrame(img, texId, texMatrix, width, height, cameraType);
                if(fuVideoFrame == null){
                    return null;
                }
                return new RtcManager.ProcessVideoFrame(fuVideoFrame.texId, fuVideoFrame.texMatrix, fuVideoFrame.width, fuVideoFrame.height,
                        fuVideoFrame.texType == FUManager.TEXTURE_TYPE_OES ? RtcManager.TEXTURE_TYPE_OES : RtcManager.TEXTURE_TYPE_2D);
            }
        });
        RtcManager.getInstance().renderLocalVideo(mBinding.videoContainer, null);
        FUManager.getInstance().reset();
        FUManager.getInstance().initController();
        FUManager.getInstance().setDriveMode();
        FUManager.getInstance().showImage01();

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
                navigateToStartPage();
            }
        });
        mBinding.toolbarFgCreate.setNavigationOnClickListener((v) -> navigateToStartPage());
        mBinding.btnRandomFgCreate.setOnClickListener((v) -> setupRandomName());
        mBinding.btnLiveFgCreate.setOnClickListener((v) -> startLive());
        mGlobalModel.roomInfo.observe(getViewLifecycleOwner(), new EventObserver<>(this::onRoomInfoChanged));
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
        findNavController().popBackStack(R.id.roomListFragment, false);
    }
}
