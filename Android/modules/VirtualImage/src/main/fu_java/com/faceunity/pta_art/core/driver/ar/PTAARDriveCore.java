package com.faceunity.pta_art.core.driver.ar;

import android.content.Context;

import com.faceunity.pta_art.core.FUPTARenderer;
import com.faceunity.pta_art.core.base.BaseCore;

/**
 * AR场景
 * Created by tujh on 2018/12/17.
 */
public class PTAARDriveCore extends BaseCore {
    private static final String TAG = PTAARDriveCore.class.getSimpleName();

    private AvatarARDriveHandle avatarARHandle;


    public PTAARDriveCore(Context context, FUPTARenderer fuP2ARenderer) {
        super(context, fuP2ARenderer);

    }

    public AvatarARDriveHandle createAvatarARHandle() {
        return avatarARHandle = new AvatarARDriveHandle(this, mFUItemHandler);
    }


    @Override
    public void release() {
        avatarARHandle.setModelmat(true);
        avatarARHandle.release();
    }


    @Override
    public void unBind() {
        if (avatarARHandle != null)
            avatarARHandle.unBindAll();
    }

    @Override
    public void bind() {
        if (avatarARHandle != null)
            avatarARHandle.bindAll();
    }
}
