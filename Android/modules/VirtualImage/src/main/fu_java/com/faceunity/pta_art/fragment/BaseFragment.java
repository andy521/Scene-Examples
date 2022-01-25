package com.faceunity.pta_art.fragment;

import android.content.Context;
import android.view.MotionEvent;

import androidx.fragment.app.Fragment;

import com.faceunity.pta_art.core.AvatarHandle;
import com.faceunity.pta_art.core.FUPTARenderer;
import com.faceunity.pta_art.core.PTACore;
import com.faceunity.pta_art.entity.AvatarPTA;

import java.util.List;

import io.agora.scene.virtualimage.manager.FUManager;

/**
 * Created by tujh on 2018/10/19.
 */
public class BaseFragment extends Fragment {
    public static final String TAG = BaseFragment.class.getSimpleName();

    protected IContextCallback mActivity;
    protected FUPTARenderer mFUP2ARenderer;
    protected PTACore mP2ACore;
    protected AvatarHandle mAvatarHandle;
    protected List<AvatarPTA> mAvatarP2AS;


    public interface IContextCallback{

        AvatarPTA getShowAvatarP2A();

        void setShowAvatarP2A(AvatarPTA avatarP2A);

        void updateAvatarP2As();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = FUManager.getInstance().getContextCallbackImple();
        mFUP2ARenderer = FUManager.getInstance().getFUP2ARenderer();
        mP2ACore = FUManager.getInstance().getP2ACore();
        mAvatarHandle = FUManager.getInstance().getAvatarHandle();
        mAvatarP2AS = FUManager.getInstance().getAvatarP2As();

    }

    public void onBackPressed() {}

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }


}
