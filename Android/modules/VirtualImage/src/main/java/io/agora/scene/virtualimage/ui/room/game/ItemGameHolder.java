package io.agora.scene.virtualimage.ui.room.game;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.example.base.BaseRecyclerViewAdapter;
import io.agora.scene.virtualimage.R;
import io.agora.scene.virtualimage.bean.AgoraGame;
import io.agora.scene.virtualimage.databinding.VirtualImageItemGameBinding;

public class ItemGameHolder extends BaseRecyclerViewAdapter.BaseViewHolder<VirtualImageItemGameBinding, AgoraGame> {

    public ItemGameHolder(@NonNull VirtualImageItemGameBinding mBinding) {
        super(mBinding);
    }

    @Override
    public void binding(@Nullable AgoraGame data, int selectedIndex) {
        if (data == null) return;
        mBinding.getRoot().setText(data.getGameName());
        mBinding.getRoot().setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.virtual_image_ic_game_1,0,0);
    }
}
