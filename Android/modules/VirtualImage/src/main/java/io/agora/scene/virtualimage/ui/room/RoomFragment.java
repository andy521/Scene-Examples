package io.agora.scene.virtualimage.ui.room;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.faceunity.pta_art.fragment.EditFaceFragment;

import io.agora.example.base.BaseUtil;
import io.agora.scene.virtualimage.GlobalViewModel;
import io.agora.scene.virtualimage.R;
import io.agora.scene.virtualimage.base.BaseNavFragment;
import io.agora.scene.virtualimage.bean.GameInfo;
import io.agora.scene.virtualimage.bean.RoomInfo;
import io.agora.scene.virtualimage.databinding.VirtualImageFragmentRoomBinding;
import io.agora.scene.virtualimage.util.NormalContainerInsetsListener;
import io.agora.scene.virtualimage.util.OneUtil;
import io.agora.scene.virtualimage.util.ViewStatus;

public class RoomFragment extends BaseNavFragment<VirtualImageFragmentRoomBinding> {
    private boolean amHost;
    private RoomInfo currentRoom;

    private RoomViewModel mViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        GlobalViewModel mGlobalModel = OneUtil.getViewModel(requireActivity(), GlobalViewModel.class);
        // hold current RoomInfo
        if (mGlobalModel.roomInfo.getValue() != null)
            currentRoom = mGlobalModel.roomInfo.getValue().peekContent();
        if (currentRoom == null ) {
            findNavController().navigate(R.id.action_roomFragment_to_roomCreateFragment);
            return null;
        }
        mViewModel = OneUtil.getViewModel(this, RoomViewModel.class, new RoomViewModelFactory(requireContext(), mGlobalModel.localUser, currentRoom));
        //  See if current user is the host
        amHost = currentRoom.getUserId().equals(mViewModel.localUser.getUserId());

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupInsets();

        initView();
        initListener();
        initLiveDataObserver();
    }


    public void initView() {
        if (amHost)
            onSingleHostState();
        else
            onDoubleHostState();
    }

    private void initListener() {

        // 监听"返回键"
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // No-OP
            }
        });

        // 翻转相机 按钮
        mBinding.btnBeauty.setOnClickListener(v -> {
            EditFaceFragment fragment = new EditFaceFragment();
            fragment.setOnCloseListener(() -> {
                mBinding.containerFgRoom.setVisibility(View.VISIBLE);
                getChildFragmentManager().beginTransaction()
                        .remove(fragment)
                        .commit();
            });
            mBinding.containerFgRoom.setVisibility(View.GONE);
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.face_up_container, fragment)
                    .commit();

        });
        mBinding.btnMode.setOnClickListener(v -> {
            // TODO 切换AR和Avatar模式

        });
        // 退出房间按钮
        mBinding.btnEndLiveFgRoom.setOnClickListener(v -> {
            if (amHost)
                RoomFragment.this.showAlertEndLiveDialog();
            else
                findNavController().popBackStack();
        });
        // 停止连麦 按钮
        mBinding.btnEndCallFgRoom.setOnClickListener(v -> RoomFragment.this.showAlertEndCallDialog());

        mBinding.btnMicFgRoom.setOnClickListener(v -> mViewModel.toggleMute());

    }

    private void initLiveDataObserver() {
        // RTC 对方用户
        mViewModel.targetUser().observe(getViewLifecycleOwner(), localUser -> {
            if (localUser != null) {
                mBinding.hostViewFgRoom.setSingleHost(false);
                RoomFragment.this.onDoubleHostState();
                int uid = -1;
                try {
                    uid = Integer.parseInt(localUser.getUserId());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (uid != -1)
                    mViewModel.setupRemoteView(mBinding.hostViewFgRoom.getTargetViewport(),localUser.channelId,  uid);
            } else {
                mBinding.hostViewFgRoom.setSingleHost(true);
                RoomFragment.this.onSingleHostState();
            }
        });

        // RTC 引擎初始化
        mViewModel.setupLocalView(mBinding.hostViewFgRoom.getCurrentViewport());

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

    //<editor-fold desc="Dialog Related">

    /**
     * 终止连线 弹窗
     * 主播：通知 观众 下线==》观众下线 ==》endGame、单用户视图
     * 观众：endGame、退出
     */
    private void showAlertEndCallDialog() {
        new AlertDialog.Builder(requireContext()).setTitle(R.string.virtual_image_end_call)
                .setMessage(R.string.virtual_image_end_call_msg)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (amHost) {
                        mViewModel.endCall();
                    } else {
                        findNavController().popBackStack();
                    }
                })
                .setMessage(R.string.virtual_image_exit_game_msg)
                .setCancelable(false)
                .show();
    }

    /**
     * 退出游戏 弹窗
     * 主播、观众：endGame ==》双人视图
     */
    private void showAlertExitGameDialog() {
        new AlertDialog.Builder(requireContext()).setTitle(R.string.virtual_image_exit_game)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> mViewModel.requestEndGame())
                .setMessage(R.string.virtual_image_exit_game_msg)
                .setCancelable(false)
                .show();
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


    private void onSingleHostState() {
        BaseUtil.logD("onSingleHostState");
        mBinding.hostViewFgRoom.setSingleHost(true);

        mBinding.btnEndLiveFgRoom.setVisibility(View.VISIBLE);
        mBinding.btnMode.setVisibility(View.GONE);
        mBinding.btnEndCallFgRoom.setVisibility(View.GONE);
    }

    private void onDoubleHostState() {
        BaseUtil.logD("onDoubleHostState");
        mBinding.hostViewFgRoom.setSingleHost(false);

        mBinding.btnEndLiveFgRoom.setVisibility(View.GONE);
        mBinding.btnMode.setVisibility(View.VISIBLE);
        mBinding.btnEndCallFgRoom.setVisibility(View.VISIBLE);

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
            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mBinding.hostViewFgRoom.getViewportContainer().getLayoutParams();
            lp.rightMargin = (int) (inset.right + BaseUtil.dp2px(16));
            lp.topMargin = (int) (inset.top + BaseUtil.dp2px(16));
            mBinding.hostViewFgRoom.getViewportContainer().setLayoutParams(lp);

            mBinding.getRoot().setPaddingRelative(inset.left, 0, inset.right, inset.bottom);
            mBinding.faceUpContainer.setPaddingRelative(inset.left, inset.top, inset.right, 0);
            return WindowInsetsCompat.CONSUMED;
        });
    }

}
