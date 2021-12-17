package io.agora.sample.rtegame.ui.room;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import io.agora.example.base.BaseRecyclerViewAdapter;
import io.agora.example.base.BaseUtil;
import io.agora.rtc2.RtcEngine;
import io.agora.sample.rtegame.GameApplication;
import io.agora.sample.rtegame.GlobalViewModel;
import io.agora.sample.rtegame.R;
import io.agora.sample.rtegame.base.BaseFragment;
import io.agora.sample.rtegame.bean.GameApplyInfo;
import io.agora.sample.rtegame.bean.GameInfo;
import io.agora.sample.rtegame.bean.GiftInfo;
import io.agora.sample.rtegame.bean.PKApplyInfo;
import io.agora.sample.rtegame.bean.RoomInfo;
import io.agora.sample.rtegame.databinding.FragmentRoomBinding;
import io.agora.sample.rtegame.databinding.ItemRoomMessageBinding;
import io.agora.sample.rtegame.repo.GameRepo;
import io.agora.sample.rtegame.service.MediaProjectService;
import io.agora.sample.rtegame.ui.room.donate.DonateDialog;
import io.agora.sample.rtegame.ui.room.game.GameModeDialog;
import io.agora.sample.rtegame.ui.room.tool.MoreDialog;
import io.agora.sample.rtegame.util.BlurTransformation;
import io.agora.sample.rtegame.util.EventObserver;
import io.agora.sample.rtegame.util.GameUtil;
import io.agora.sample.rtegame.util.GiftUtil;
import io.agora.sample.rtegame.util.ViewStatus;
import io.agora.sample.rtegame.view.LiveHostCardView;
import io.agora.sample.rtegame.view.LiveHostLayout;

public class RoomFragment extends BaseFragment<FragmentRoomBinding> {

    private static final float recyclerViewHeightOnWidthPercent = 116 / 375f;

    private RoomViewModel mViewModel;

    private BaseRecyclerViewAdapter<ItemRoomMessageBinding, CharSequence, MessageHolder> mMessageAdapter;

    // 请求权限
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private RoomInfo currentRoom;
    private boolean aMHost;
    private boolean shouldShowInputBox = false;
    private AlertDialog currentDialog;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // During Live we limit the orientation
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        GlobalViewModel mGlobalModel = GameUtil.getViewModel(requireActivity(), GlobalViewModel.class);
        // hold current RoomInfo
        if (mGlobalModel.roomInfo.getValue() != null)
            currentRoom = mGlobalModel.roomInfo.getValue().peekContent();
        if (currentRoom == null) {
            findNavController().navigate(R.id.action_roomFragment_to_roomCreateFragment);
            return null;
        }
        mViewModel = GameUtil.getViewModel(this, RoomViewModel.class, new RoomViewModelFactory(requireContext(), currentRoom));
        //  See if current user is the host
        aMHost = currentRoom.getUserId().equals(GameApplication.getInstance().user.getUserId());
        if (aMHost)
            mGlobalModel.focused.observe(getViewLifecycleOwner(), mViewModel::handleScreenCapture);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
        initListener();
    }

    @Override
    public void onDestroy() {
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        stopScreenCaptureService();
        super.onDestroy();
    }

    private void initView() {
        Glide.with(this).load(GameApplication.getInstance().user.getAvatar())
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher_round).into(mBinding.layoutRoomInfo.avatarHostFgRoom);
        mBinding.layoutRoomInfo.nameHostFgRoom.setText(currentRoom.getTempUserName());

        // config game view
        new Handler(Looper.getMainLooper()).post(() -> {
            if (mBinding != null) {
                ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mBinding.recyclerViewFgRoom.getLayoutParams();
                lp.matchConstraintPercentHeight = 1F;
                lp.height = (int) (mBinding.getRoot().getMeasuredWidth() * recyclerViewHeightOnWidthPercent);
                mBinding.recyclerViewFgRoom.setLayoutParams(lp);

                // GameView = WebView || 屏幕共享View
                /////
                int dp12 = (int) BaseUtil.dp2px(12);
                // 设置 Game View 距离顶部距离 (+ 12dp)
                int topMargin = (int) (mBinding.layoutRoomInfo.getRoot().getBottom() + dp12);
                // Game Mode 下摄像头预览距离底部距离
                int marginBottom = mBinding.getRoot().getMeasuredHeight() - mBinding.btnExitFgRoom.getTop() + dp12;
                // Game View 高度
                int height = mBinding.btnExitFgRoom.getTop() - lp.height - dp12 * 2 - topMargin;
                mBinding.hostContainerFgRoom.initParams(!aMHost, topMargin, height, marginBottom, mBinding.recyclerViewFgRoom.getPaddingLeft());
            }
        });

        mMessageAdapter = new BaseRecyclerViewAdapter<>(null, MessageHolder.class);
        mBinding.recyclerViewFgRoom.setAdapter(mMessageAdapter);
        // Android 12 over_scroll animation is phenomenon
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            mBinding.recyclerViewFgRoom.setOverScrollMode(View.OVER_SCROLL_NEVER);

        hideBtnByCurrentRole();
    }

    private void hideBtnByCurrentRole() {
        mBinding.btnGameFgRoom.setVisibility(aMHost ? View.VISIBLE : View.GONE);
        mBinding.btnMoreFgRoom.setVisibility(aMHost ? View.VISIBLE : View.GONE);
        mBinding.btnDonateFgRoom.setVisibility(aMHost ? View.GONE : View.VISIBLE);
    }

    private void initListener() {
        // handle request screen record callback
        // since onActivityResult() is deprecated
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                if (mBinding.hostContainerFgRoom.webViewHostView != null) {
                    Rect rect = new Rect();
                    mBinding.hostContainerFgRoom.webViewHostView.getHitRect(rect);
                    mViewModel.startScreenCapture(result.getData(), rect);
                }
            }
        });

        handleWindowInset();

        // 本地消息
        mBinding.editTextFgRoom.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String msg = v.getText().toString().trim();
                if (!msg.isEmpty())
                    insertUserMessage(msg);
                BaseUtil.hideKeyboard(requireActivity().getWindow(), v);
                v.setText("");
            }
            return false;
        });
        // "更多"弹窗
        mBinding.btnMoreFgRoom.setOnClickListener(v -> new MoreDialog().show(getChildFragmentManager(), MoreDialog.TAG));
        // "游戏"弹窗
        mBinding.btnGameFgRoom.setOnClickListener(v -> new GameModeDialog().show(getChildFragmentManager(), GameModeDialog.TAG));
        // "退出游戏"按钮
        mBinding.btnExitGameFgRoom.setOnClickListener(v -> mViewModel.requestExitGame());
        // "终止连麦"按钮
        mBinding.btnExitPkFgRoom.setOnClickListener(v -> mViewModel.endPK());
        // "礼物"弹窗
        mBinding.btnDonateFgRoom.setOnClickListener(v -> new DonateDialog().show(getChildFragmentManager(), DonateDialog.TAG));
        // "退出直播间"按钮点击事件
        mBinding.btnExitFgRoom.setOnClickListener(v -> requireActivity().onBackPressed());
        mBinding.editTextFgRoom.setShowSoftInputOnFocus(true);
        // 显示键盘按钮
        mBinding.inputFgRoom.setOnClickListener(v -> {
            shouldShowInputBox = true;
            BaseUtil.showKeyboardCompat(mBinding.inputLayoutFgRoom);
        });
        // RTC engine 初始化监听
        mViewModel.mEngine().observe(getViewLifecycleOwner(), this::onRTCInit);
        // 连麦成功《==》主播上线
        mViewModel.subRoomInfo().observe(getViewLifecycleOwner(), this::onSubHostJoin);
        // 礼物监听
        mViewModel.gift().observe(getViewLifecycleOwner(), new EventObserver<>(this::onGiftUpdated));

        // 主播，监听连麦信息
        if (aMHost) {
            // 游戏开始
            mViewModel.applyInfo().observe(getViewLifecycleOwner(), this::onPKApplyInfoChanged);
            mViewModel.currentGame().observe(getViewLifecycleOwner(), this::onGameChanged);
        } else {
            mViewModel.gameShareInfo().observe(getViewLifecycleOwner(), this::onGameShareInfoChanged);
            mViewModel.localHostId().observe(getViewLifecycleOwner(), this::onLocalHostJoin);
        }

        mViewModel.viewStatus().observe(getViewLifecycleOwner(), viewStatus -> {
            if (viewStatus instanceof ViewStatus.Error)
                insertNewMessage(((ViewStatus.Error) viewStatus).msg);
        });
    }

    private void onGameShareInfoChanged(GameInfo gameInfo) {
        if (gameInfo == null) return;
        if (gameInfo.getStatus() == GameInfo.START) {
            GameUtil.currentGame = GameRepo.getGameDetail(gameInfo.getGameId());
            insertNewMessage("加载远端游戏画面");
            needGameView(true);
            if (mBinding.hostContainerFgRoom.gameHostView != null) {
                mViewModel.setupScreenView(mBinding.hostContainerFgRoom.gameHostView.renderTextureView, gameInfo.getGameUid());
            }
        } else if (gameInfo.getStatus() == GameInfo.END) {
            insertNewMessage("停止远端游戏画面");
            needGameView(false);
        }
    }

    //<editor-fold desc="邀请 相关">
    private void onPKApplyInfoChanged(PKApplyInfo pkApplyInfo) {
        if (currentDialog != null) currentDialog.dismiss();
        if (pkApplyInfo == null) return;
        if (pkApplyInfo.getStatus() == PKApplyInfo.APPLYING) {
            showPKDialog(pkApplyInfo);
        } else if (pkApplyInfo.getStatus() == PKApplyInfo.REFUSED) {
            insertNewMessage("邀请已拒绝");
        }
    }

    /**
     * 仅主播调用
     */
    private void showPKDialog(PKApplyInfo pkApplyInfo) {
        if (pkApplyInfo.getRoomId().equals(currentRoom.getId())) {
            insertNewMessage("你画我猜即将开始，等待其他玩家...");
            currentDialog = new AlertDialog.Builder(requireContext()).setMessage("你画我猜即将开始，等待其他玩家...").setCancelable(false)
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        mViewModel.cancelApplyPK(pkApplyInfo);
                        dialog.dismiss();
                    }).show();
        } else {
            currentDialog = new AlertDialog.Builder(requireContext()).setMessage("您的好友邀请您加入游戏").setCancelable(false)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        mViewModel.acceptApplyPK(pkApplyInfo);
                        dialog.dismiss();
                    })
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        mViewModel.cancelApplyPK(pkApplyInfo);
                        dialog.dismiss();
                    }).show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="游戏相关">
    private void onGameChanged(GameApplyInfo currentGame) {
        if (currentGame == null) return;
        BaseUtil.logD("game status->" + currentGame.getStatus());

        GameUtil.currentGame = GameRepo.getGameDetail(currentGame.getGameId());
        if (!aMHost) {
            return;
        }
        if (currentGame.getStatus() == GameApplyInfo.PLAYING) {
            needGameView(true);
            WebView webView = mBinding.hostContainerFgRoom.webViewHostView;
            if (webView != null) {
                mViewModel.startGame(currentGame, webView);
            }
            startScreenCaptureService();
        } else if (currentGame.getStatus() == GameApplyInfo.END) {
            needGameView(false);
            stopScreenCaptureService();
            mViewModel.endScreenCapture();
        }
    }

    private void needGameView(boolean need) {
        if (need) {
            mBinding.hostContainerFgRoom.createDefaultGameView();
            onLayoutTypeChanged(LiveHostLayout.Type.DOUBLE_IN_GAME);
        } else {
            mBinding.hostContainerFgRoom.removeGameView();
            onLayoutTypeChanged(LiveHostLayout.Type.DOUBLE);
        }
    }
    //</editor-fold>


    /**
     * 主播 || 自己送的==》消息提示
     * 不是主播 ==》显示特效
     */
    private void onGiftUpdated(GiftInfo giftInfo) {
        if (giftInfo == null) return;

        mBinding.giftImageFgRoom.setVisibility(View.VISIBLE);

        String giftDesc = GiftUtil.getGiftDesc(requireContext(), giftInfo);
        if (giftDesc != null) insertNewMessage(giftDesc);

        if (!aMHost) {
            int giftId = GiftUtil.getGiftIdFromGiftInfo(requireContext(), giftInfo);
            Glide.with(this).asGif().load(GiftUtil.getGifByGiftId(giftId))
                    .listener(new RequestListener<GifDrawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                            mBinding.giftImageFgRoom.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                            resource.setLoopCount(1);
                            resource.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                                @Override
                                public void onAnimationEnd(Drawable drawable) {
                                    super.onAnimationEnd(drawable);
                                    mBinding.giftImageFgRoom.setVisibility(View.GONE);
                                }
                            });
                            return false;
                        }
                    })
                    .into(mBinding.giftImageFgRoom);
        }
    }

    /**
     * RTC 初始化成功
     */
    private void onRTCInit(RtcEngine engine) {
        BaseUtil.logD("onRTCInit");
        if (engine == null) findNavController().popBackStack();
        else {
            insertNewMessage("RTC 初始化成功");
            // 如果是房主，创建View开始直播
            if (aMHost) initLocalView();

            mViewModel.joinRoom(GameApplication.getInstance().user);
        }
    }

    /**
     * 本地 View 初始化
     * 仅主播本人调用
     */
    @MainThread
    private void initLocalView() {
        LiveHostCardView view = mBinding.hostContainerFgRoom.createHostView();
        onLayoutTypeChanged(LiveHostLayout.Type.HOST_ONLY);
        mViewModel.setupLocalView(view.renderTextureView, GameApplication.getInstance().user);
        insertNewMessage("画面加载完成");
    }

    /**
     * 主播上线
     */
    @MainThread
    private void onLocalHostJoin(Integer uid) {
        BaseUtil.logD("uid:" + uid);
        if (uid == null) return;
        insertNewMessage("正在加载主播【" + currentRoom.getTempUserName() + "】视频");
        LiveHostLayout liveHost = mBinding.hostContainerFgRoom;

        LiveHostCardView view = liveHost.createHostView();
        onLayoutTypeChanged(liveHost.getChildCount() == 1 ? LiveHostLayout.Type.HOST_ONLY : liveHost.getType());
        mViewModel.setupRemoteView(view.renderTextureView, currentRoom, true);
    }

    /**
     * 连麦主播上线
     */
    @MainThread
    private void onSubHostJoin(@Nullable RoomInfo subRoomInfo) {
        BaseUtil.logD("room status->" + (subRoomInfo == null));
        LiveHostLayout container = mBinding.hostContainerFgRoom;
        // remove subHostView
        if (subRoomInfo == null) {
            insertNewMessage("连麦结束");
            onLayoutTypeChanged(LiveHostLayout.Type.HOST_ONLY);
        } else {
            insertNewMessage("正在加载连麦主播【" + subRoomInfo.getTempUserName() + "】视频");
            LiveHostCardView view = container.createSubHostView();
            container.setType(container.isCurrentlyInGame() ? LiveHostLayout.Type.DOUBLE_IN_GAME : LiveHostLayout.Type.DOUBLE);
            mViewModel.setupRemoteView(view.renderTextureView, subRoomInfo, false);
        }
    }

    /**
     * 用户发送消息
     * 直播间滚动消息
     */
    private void insertUserMessage(String msg) {
        Spanned userMessage = Html.fromHtml(getString(R.string.user_msg, mViewModel.localUser.getName(), msg));
        insertNewMessage(userMessage);
    }

    /**
     * 直播间滚动消息
     */
    private void insertNewMessage(CharSequence msg) {
        mMessageAdapter.addItem(msg);
        int count = mMessageAdapter.getItemCount();
        if (count > 0)
            mBinding.recyclerViewFgRoom.smoothScrollToPosition(count - 1);
    }

    private void startScreenCaptureService() {
        Intent mediaProjectionIntent = new Intent(requireActivity(), MediaProjectService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(mediaProjectionIntent);
        } else {
            requireContext().startService(mediaProjectionIntent);
        }

        MediaProjectionManager mpm = (MediaProjectionManager) requireContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent intent = mpm.createScreenCaptureIntent();
        activityResultLauncher.launch(intent);
    }

    private void stopScreenCaptureService() {
        Intent mediaProjectionIntent = new Intent(requireActivity(), MediaProjectService.class);
        requireContext().stopService(mediaProjectionIntent);
    }

    private void onLayoutTypeChanged(LiveHostLayout.Type type) {
        BaseUtil.logD("onLayoutTypeChanged:" + type.ordinal());
        mBinding.hostContainerFgRoom.setType(type);
        adjustMessageWidth(type == LiveHostLayout.Type.HOST_ONLY);

        if (type == LiveHostLayout.Type.DOUBLE_IN_GAME) {
            if (GameUtil.currentGame != null)
                setRoomBgd(false, GameUtil.getGameBgdByGameId(GameUtil.currentGame.getGameId()));
        } else {
            setRoomBgd(true, GameUtil.getBgdByRoomBgdId(currentRoom.getBackgroundId()));
        }
        if (aMHost) {
            if (type == LiveHostLayout.Type.HOST_ONLY) {
                mBinding.btnExitPkFgRoom.setVisibility(View.GONE);
            } else if (type == LiveHostLayout.Type.DOUBLE) {
                mBinding.btnGameFgRoom.setVisibility(View.VISIBLE);
                mBinding.btnExitGameFgRoom.setVisibility(View.GONE);
                mBinding.btnExitPkFgRoom.setVisibility(View.VISIBLE);
            } else {
                mBinding.btnGameFgRoom.setVisibility(View.GONE);
                mBinding.btnExitGameFgRoom.setVisibility(View.VISIBLE);
                mBinding.btnExitPkFgRoom.setVisibility(View.GONE);
            }
        }
    }

    private void adjustMessageWidth(boolean fullWidth) {
        int leftPadding = mBinding.recyclerViewFgRoom.getPaddingLeft();
        int desiredPaddingEnd = fullWidth ? leftPadding : 0;
        mBinding.recyclerViewFgRoom.setPaddingRelative(leftPadding, 0, desiredPaddingEnd, 0);
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mBinding.recyclerViewFgRoom.getLayoutParams();
        lp.matchConstraintPercentWidth = fullWidth ? 1 : 0.5f;
        mBinding.recyclerViewFgRoom.setLayoutParams(lp);
    }

    private void setRoomBgd(boolean blurring, @DrawableRes int drawableId) {
        RequestBuilder<Drawable> load = Glide.with(this).asDrawable().load(drawableId);
        if (blurring)
            load = load.apply(RequestOptions.bitmapTransform(new BlurTransformation(requireContext())));

        load.into(new CustomTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                mBinding.getRoot().setBackground(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });

    }

    private void handleWindowInset() {
        // 沉浸处理
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.getRoot(), (v, insets) -> {
            Insets inset = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            BaseUtil.logD("inset changed->>" + inset.toString());
            // 整体留白
            mBinding.containerOverlayFgRoom.setPadding(inset.left, inset.top, inset.right, inset.bottom);
            // 输入框显隐及位置偏移
            boolean imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            if (imeVisible) {
                if (shouldShowInputBox) {
                    mBinding.inputLayoutFgRoom.setVisibility(View.VISIBLE);
                    mBinding.inputLayoutFgRoom.requestFocus();
                    int desiredY = -insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                    mBinding.inputLayoutFgRoom.setTranslationY(desiredY);
                }
            } else {
                shouldShowInputBox = false;
                if (mBinding.inputLayoutFgRoom.getVisibility() == View.VISIBLE) {
                    mBinding.inputLayoutFgRoom.setVisibility(View.GONE);
                    mBinding.inputLayoutFgRoom.clearFocus();
                }
            }
            return WindowInsetsCompat.CONSUMED;
        });
    }

}
