package io.agora.scene.virtualimage.ui.room;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.faceunity.pta_art.fragment.EditFaceFragment;

import io.agora.example.base.BaseUtil;
import io.agora.scene.virtualimage.GlobalViewModel;
import io.agora.scene.virtualimage.R;
import io.agora.scene.virtualimage.base.BaseNavFragment;
import io.agora.scene.virtualimage.bean.GameInfo;
import io.agora.scene.virtualimage.bean.LocalUser;
import io.agora.scene.virtualimage.bean.RoomInfo;
import io.agora.scene.virtualimage.databinding.VirtualImageFragmentRoomBinding;
import io.agora.scene.virtualimage.manager.FUManager;
import io.agora.scene.virtualimage.manager.RtcManager;
import io.agora.scene.virtualimage.ui.HostView;
import io.agora.scene.virtualimage.util.NormalContainerInsetsListener;
import io.agora.scene.virtualimage.util.OneUtil;
import io.agora.scene.virtualimage.util.ViewStatus;

public class RoomFragment extends BaseNavFragment<VirtualImageFragmentRoomBinding> {
    private RoomInfo currentRoom;

    private RoomViewModel mViewModel;
    private EditFaceFragment mEditFaceFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        GlobalViewModel mGlobalModel = OneUtil.getViewModel(requireActivity(), GlobalViewModel.class);
        // hold current RoomInfo
        if (mGlobalModel.roomInfo.getValue() != null)
            currentRoom = mGlobalModel.roomInfo.getValue().peekContent();
        if (currentRoom == null) {
            findNavController().navigate(R.id.action_roomFragment_to_roomCreateFragment);
            return null;
        }
        mViewModel = OneUtil.getViewModel(this, RoomViewModel.class, new RoomViewModelFactory(requireContext(), mGlobalModel.localUser, currentRoom));

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupInsets();

        initListener();
        initLiveDataObserver();
    }


    private void initListener() {
        // 监听"返回键"
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mEditFaceFragment != null) {
                    mEditFaceFragment.onBackPressed();
                } else {
                    showAlertEndLiveDialog();
                }
            }
        });

        // 翻转相机 按钮
        mBinding.btnBeauty.setOnClickListener(v -> {
            boolean arMode = FUManager.getInstance().isARMode();
            if (arMode) {
                Toast.makeText(getContext(), R.string.room_mode_switch_error, Toast.LENGTH_LONG).show();
                return;
            }


            mEditFaceFragment = new EditFaceFragment();
            mEditFaceFragment.setOnCloseListener(() -> {
                mBinding.hostViewFgRoom.setViewportContainersVisible(true);
                mBinding.containerFgRoom.setVisibility(View.VISIBLE);
                mBinding.hostViewFgRoom.getCurrentViewport().setVisibility(View.GONE);
                mViewModel.setupLocalView(mBinding.hostViewFgRoom.getTargetViewport(0));

                getChildFragmentManager().beginTransaction()
                        .remove(mEditFaceFragment)
                        .commit();
                mEditFaceFragment = null;
            });

            mBinding.containerFgRoom.setVisibility(View.GONE);
            mBinding.hostViewFgRoom.setViewportContainersVisible(false);
            mBinding.hostViewFgRoom.getCurrentViewport().setVisibility(View.VISIBLE);
            mViewModel.setupLocalView(mBinding.hostViewFgRoom.getCurrentViewport());

            getChildFragmentManager().beginTransaction()
                    .replace(R.id.face_up_container, mEditFaceFragment)
                    .commit();

        });
        mBinding.btnMode.setOnClickListener(v -> {
            // 切换AR和Avatar模式
            if(!FUManager.getInstance().isARMode() && RtcManager.getInstance().isPublishAvatarTrack()){
                FUManager.getInstance().switchMode();
            }else if( FUManager.getInstance().isARMode() && RtcManager.getInstance().isPublishAvatarTrack()){
                RtcManager.getInstance().updateChannelTrack(mViewModel.currentRoom.getId(), false);
            }else {
                RtcManager.getInstance().updateChannelTrack(mViewModel.currentRoom.getId(), true);
                FUManager.getInstance().switchMode();
            }
        });
        // 停止连麦 按钮
        mBinding.btnEndCallFgRoom.setOnClickListener(v -> RoomFragment.this.showAlertEndLiveDialog());

        mBinding.btnMicFgRoom.setOnClickListener(v -> mViewModel.toggleMute());

    }

    private void initLiveDataObserver() {
        // RTC 对方用户
        mViewModel.targetUser().observe(getViewLifecycleOwner(), localUsers -> {
            if (localUsers != null) {
                for (int i = 0; i < HostView.ViewportRemoteCount; i++) {
                    LocalUser user = null;
                    if (i < localUsers.size()) {
                        user = localUsers.get(i);
                    }
                    if (user == null) {
                        mBinding.hostViewFgRoom.setViewportTarget(i + 1, false);
                    } else {
                        mBinding.hostViewFgRoom.setViewportTarget(i + 1, true);
                        int uid = -1;
                        try {
                            uid = Integer.parseInt(user.getUserId());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        if (uid != -1) {
                            mViewModel.setupRemoteView(mBinding.hostViewFgRoom.getTargetViewport(i + 1), user.channelId, uid);
                        }
                    }
                }
            } else {
                mBinding.hostViewFgRoom.removeAllViewportTarget();
            }
        });

        // RTC 引擎初始化
        setStatusBarStyle(true);
        mBinding.hostViewFgRoom.setViewportTarget(0, true);
        mViewModel.setupLocalView(mBinding.hostViewFgRoom.getTargetViewport(0));

        // 当前游戏信息
        mViewModel.gameInfo().observe(getViewLifecycleOwner(), this::onGameStatusChanged);

        // 静音状态
        mViewModel.isLocalMicMuted.observe(getViewLifecycleOwner(), isMute -> {
            int resId = isMute ? R.drawable.virtual_image_ic_microphone_off : R.drawable.virtual_image_ic_microphone;

            mBinding.btnMicFgRoom.setIconResource(resId);
        });

        mViewModel.viewStatus().observe(getViewLifecycleOwner(), viewStatus -> {
            if (viewStatus instanceof ViewStatus.Error) {
                BaseUtil.toast(requireContext(), "对方已挂断");
                findNavController().popBackStack();
            }
        });
    }

    /**
     * 关闭直播间 弹窗
     */
    private void showAlertEndLiveDialog() {
        new AlertDialog.Builder(requireContext()).setTitle(R.string.virtual_image_exit_room)
                .setMessage(R.string.virtual_image_exit_room_msg)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> findNavController().popBackStack())
                .setCancelable(false)
                .show();
    }


    private void onGameStatusChanged(@NonNull GameInfo gameInfo) {
        BaseUtil.logD("onGameStatusChanged:" + gameInfo.getStatus());
        boolean btnGameEnabled = gameInfo.getStatus() != GameInfo.START;
        mBinding.btnMode.setEnabled(btnGameEnabled);
        mBinding.btnMode.setAlpha(btnGameEnabled ? 1f : 0.5f);
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.containerFgRoom, new NormalContainerInsetsListener());
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.hostViewFgRoom, (v, insets) -> {
            Insets inset = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            mBinding.getRoot().setPaddingRelative(inset.left, 0, inset.right, inset.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

}
