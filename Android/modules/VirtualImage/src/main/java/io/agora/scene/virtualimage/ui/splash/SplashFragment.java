package io.agora.scene.virtualimage.ui.splash;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.scene.virtualimage.GlobalViewModel;
import io.agora.scene.virtualimage.R;
import io.agora.scene.virtualimage.base.BaseNavFragment;
import io.agora.scene.virtualimage.databinding.VirtualImageFragmentSplashBinding;
import io.agora.scene.virtualimage.util.EventObserver;
import io.agora.scene.virtualimage.util.OneUtil;


public class SplashFragment extends BaseNavFragment<VirtualImageFragmentSplashBinding> {
    private GlobalViewModel globalViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        globalViewModel = OneUtil.getViewModel(requireActivity(), GlobalViewModel.class);

        initListener();
    }

    private void initListener() {
        globalViewModel.isRTMInit().observe(getViewLifecycleOwner(), new EventObserver<>(succeed -> {
            if (succeed == Boolean.TRUE) toRoomListPage();
            else showError();
        }));

        mBinding.btnFgSplash.setOnClickListener(v -> globalViewModel.tryReInitSyncManager(requireContext()));

    }

    private void toRoomListPage() {
        findNavController().popBackStack(R.id.splashFragment, true);
        findNavController().navigate(R.id.roomListFragment);
    }

    private void showError() {
        mBinding.loadingStatus.setVisibility(View.GONE);
        mBinding.errorStatus.setVisibility(View.VISIBLE);
    }
}
