package io.agora.sample.rtegame.ui.splashpage;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.sample.rtegame.GlobalViewModel;
import io.agora.sample.rtegame.R;
import io.agora.sample.rtegame.base.BaseFragment;
import io.agora.sample.rtegame.databinding.FragmentSplashBinding;
import io.agora.sample.rtegame.util.EventObserver;
import io.agora.sample.rtegame.util.GameUtil;


public class SplashFragment extends BaseFragment<FragmentSplashBinding> {
    private GlobalViewModel globalViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        globalViewModel = GameUtil.getViewModel(requireActivity(), GlobalViewModel.class);

        initListener();
    }

    private void initListener() {
        globalViewModel.isRTMInit().observe(getViewLifecycleOwner(), new EventObserver<>(succeed -> {
            if (succeed == Boolean.TRUE) toRoomListPage();
            else showError();
        }));

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
