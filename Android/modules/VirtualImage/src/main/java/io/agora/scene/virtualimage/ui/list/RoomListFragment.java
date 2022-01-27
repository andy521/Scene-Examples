package io.agora.scene.virtualimage.ui.list;

import android.Manifest;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.Locale;

import io.agora.example.base.BaseRecyclerViewAdapter;
import io.agora.example.base.BaseUtil;
import io.agora.example.base.DividerDecoration;
import io.agora.example.base.OnItemClickListener;
import io.agora.rtc2.RtcEngine;
import io.agora.scene.virtualimage.GlobalViewModel;
import io.agora.scene.virtualimage.R;
import io.agora.scene.virtualimage.base.BaseNavFragment;
import io.agora.scene.virtualimage.bean.RoomInfo;
import io.agora.scene.virtualimage.databinding.VirtualImageFragmentRoomListBinding;
import io.agora.scene.virtualimage.databinding.VirtualImageItemRoomListBinding;
import io.agora.scene.virtualimage.manager.FUManager;
import io.agora.scene.virtualimage.manager.RtcManager;
import io.agora.scene.virtualimage.util.Event;
import io.agora.scene.virtualimage.util.EventObserver;
import io.agora.scene.virtualimage.util.OneUtil;
import io.agora.scene.virtualimage.util.ViewStatus;

import static java.lang.Boolean.TRUE;

public class RoomListFragment extends BaseNavFragment<VirtualImageFragmentRoomListBinding> implements OnItemClickListener<RoomInfo> {

    ////////////////////////////////////// -- PERMISSION --//////////////////////////////////////////////////////////////
    public static final String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    private final BaseUtil.PermissionResultCallback<String[]> callback = new BaseUtil.PermissionResultCallback<String[]>() {
        @Override
        public void onAllPermissionGranted() {
            toNextPage();
        }

        @Override
        public void onPermissionRefused(String[] refusedPermissions) {
            showPermissionAlertDialog();
        }

        @Override
        public void showReasonDialog(String[] refusedPermissions) {
            showPermissionRequestDialog();
        }
    };
    // Must have
    private final ActivityResultLauncher<String[]> requestPermissionLauncher = BaseUtil.registerForActivityResult(RoomListFragment.this, callback);

    ////////////////////////////////////// -- VIEW MODEL --//////////////////////////////////////////////////////////////
    private GlobalViewModel mGlobalModel;
    private RoomListViewModel mViewModel;

    ////////////////////////////////////// -- DATA --//////////////////////////////////////////////////////////////
    private BaseRecyclerViewAdapter<VirtualImageItemRoomListBinding, RoomInfo, RoomListHolder> mAdapter;
    private RoomInfo tempRoom;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setStatusBarStyle(false);
        mViewModel = OneUtil.getViewModel(this, RoomListViewModel.class);

        mGlobalModel = OneUtil.getViewModel(requireActivity(), GlobalViewModel.class);
        mGlobalModel.clearRoomInfo();
        // FIXME To avoid APP be killed in background
        //       Apparently this is gonna trigger a second request
        //       Works well, so be it
        mGlobalModel.isRTMInit().observe(getViewLifecycleOwner(), new EventObserver<>(aBoolean -> {
            if (aBoolean == TRUE) mViewModel.fetchRoomList();
        }));
        initView();
        initListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBinding.getRoot().postDelayed(() -> mViewModel.fetchRoomList(), 800);
    }

    private void initView() {
        try {
            mBinding.tvVersion.setText(
                    String.format(Locale.US,
                            "VERSION : %s",
                    requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0).versionName)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBinding.recyclerViewFgList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mAdapter = new BaseRecyclerViewAdapter<>(null, this, RoomListHolder.class);
        mBinding.recyclerViewFgList.setAdapter(mAdapter);
        mBinding.recyclerViewFgList.addItemDecoration(new DividerDecoration(2));
        mBinding.swipeFgList.setProgressViewOffset(true, 0, mBinding.swipeFgList.getProgressViewEndOffset());
        mBinding.swipeFgList.setColorSchemeResources(R.color.virtual_image_btn_gradient_start_color, R.color.virtual_image_btn_gradient_end_color);
        int backgroundColor = OneUtil.getMaterialBackgroundColor(BaseUtil.getColorInt(requireContext(), R.attr.colorSurface));
        mBinding.swipeFgList.setProgressBackgroundColorSchemeColor(backgroundColor);
    }

    private void initListener() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
//                android.os.Process.killProcess(android.os.Process.myPid());
//                Runtime.getRuntime().gc();
                FUManager.getInstance().stop();
                RtcEngine.destroy();
                requireActivity().finish();
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.getRoot(), (v, insets) -> {
            Insets inset = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 顶部
            mBinding.appBarFgList.setPadding(0, inset.top, 0, 0);
            // 底部
            CoordinatorLayout.LayoutParams lpBtn = (CoordinatorLayout.LayoutParams) mBinding.btnCreateFgList.getLayoutParams();
            lpBtn.bottomMargin = inset.bottom + ((int) BaseUtil.dp2px(24));
            mBinding.btnCreateFgList.setLayoutParams(lpBtn);

            return WindowInsetsCompat.CONSUMED;
        });

        // "创建房间"按钮
        mBinding.btnCreateFgList.setOnClickListener((v) -> {
            tempRoom = null;
            BaseUtil.checkPermissionBeforeNextOP(this, requestPermissionLauncher, permissions, callback);
        });
        // 下拉刷新监听
        mBinding.swipeFgList.setOnRefreshListener(() -> mViewModel.fetchRoomList());
        // 状态监听
        mViewModel.viewStatus().observe(getViewLifecycleOwner(), this::onViewStatusChanged);
        // 房间列表数据监听
        mViewModel.roomList().observe(getViewLifecycleOwner(), resList -> {
            onListStatus(resList.isEmpty());
            mAdapter.submitListAndPurge(resList);
        });

        initRtcAnFU();
    }

    private void initRtcAnFU() {
        BaseUtil.PermissionResultCallback<String[]> callback = new BaseUtil.PermissionResultCallback<String[]>() {
            @Override
            public void onAllPermissionGranted() {
                RtcManager.getInstance().init(getContext(), getString(R.string.rtc_app_id), null);
                FUManager.getInstance().initialize(getContext());
            }

            @Override
            public void onPermissionRefused(String[] refusedPermissions) {
                showPermissionAlertDialog();
            }

            @Override
            public void showReasonDialog(String[] refusedPermissions) {
                showPermissionRequestDialog();
            }
        };
        ActivityResultLauncher<String[]> requestPermissionLauncher = BaseUtil.registerForActivityResult(RoomListFragment.this, callback);
        BaseUtil.checkPermissionBeforeNextOP(this, requestPermissionLauncher, permissions, callback);
    }

    private void onViewStatusChanged(ViewStatus viewStatus) {
        if (viewStatus instanceof ViewStatus.Loading) {
            mBinding.swipeFgList.setRefreshing(true);
        } else if (viewStatus instanceof ViewStatus.Done)
            mBinding.swipeFgList.setRefreshing(false);
        else if (viewStatus instanceof ViewStatus.Error) {
            mBinding.swipeFgList.setRefreshing(false);
            BaseUtil.toast(requireContext(), ((ViewStatus.Error) viewStatus).msg);
        }
    }

    @Override
    public void onItemClick(@NonNull RoomInfo data, @NonNull View view, int position, long viewType) {
        tempRoom = data;
        BaseUtil.checkPermissionBeforeNextOP(this, requestPermissionLauncher, permissions, callback);
    }

    public void onListStatus(boolean empty) {
        mBinding.emptyViewFgList.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void toNextPage() {
        if (tempRoom != null)
            mGlobalModel.roomInfo.setValue(new Event<>(tempRoom));

        findNavController().navigate(R.id.action_roomListFragment_to_roomFragment);
    }

    private void showPermissionAlertDialog() {
        new AlertDialog.Builder(requireContext()).setMessage(R.string.virtual_image_permission_refused)
                .setPositiveButton(android.R.string.ok, null).show();
    }

    private void showPermissionRequestDialog() {
        new AlertDialog.Builder(requireContext()).setMessage(R.string.virtual_image_permission_alert).setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, ((dialogInterface, i) -> requestPermissionLauncher.launch(permissions))).show();
    }
}
