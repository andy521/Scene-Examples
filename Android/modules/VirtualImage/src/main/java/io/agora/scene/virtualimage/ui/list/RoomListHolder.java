package io.agora.scene.virtualimage.ui.list;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.example.base.BaseRecyclerViewAdapter;
import io.agora.example.base.BaseUtil;
import io.agora.scene.virtualimage.bean.RoomInfo;
import io.agora.scene.virtualimage.databinding.VirtualImageItemRoomListBinding;
import io.agora.scene.virtualimage.util.OneUtil;

public class RoomListHolder extends BaseRecyclerViewAdapter.BaseViewHolder<VirtualImageItemRoomListBinding, RoomInfo> {
    public RoomListHolder(@NonNull VirtualImageItemRoomListBinding mBinding) {
        super(mBinding);
    }

    @Override
    public void binding(@Nullable RoomInfo room, int selectedIndex) {
        if (room != null){
            mBinding.titleItemRoomList.setText(room.getRoomName());
            mBinding.bgdItemRoomList.setImageTintList(BaseUtil.getScrimColorSelector(itemView.getContext()));
            mBinding.bgdItemRoomList.setImageResource(OneUtil.getBgdByRoomBgdId(room.getBackgroundId()));
        }
    }
}
