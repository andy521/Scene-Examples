package com.faceunity.pta_art.fragment;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.faceunity.pta_art.MainActivity;
import com.faceunity.pta_art.core.AvatarHandle;
import com.faceunity.pta_art.core.FUPTARenderer;
import com.faceunity.pta_art.core.PTACore;
import com.faceunity.pta_art.entity.AvatarPTA;
import com.faceunity.pta_art.renderer.CameraRenderer;

import java.util.List;

import io.agora.scene.virtualimage.manager.FUDemoManager;

/**
 * Created by tujh on 2018/10/19.
 */
public class BaseFragment extends Fragment {
    public static final String TAG = BaseFragment.class.getSimpleName();

    protected IContextCallback mActivity;
    protected FUPTARenderer mFUP2ARenderer;
    protected PTACore mP2ACore;
    protected AvatarHandle mAvatarHandle;
    protected CameraRenderer mCameraRenderer;
    protected List<AvatarPTA> mAvatarP2AS;


    public interface IContextCallback{

        void showHomeFragment();

        AvatarPTA getShowAvatarP2A();

        int getShowIndex();

        void showBaseFragment(String tag);

        PTACore getP2ACore();

        AvatarHandle getAvatarHandle();

        void setCanClick(boolean b, boolean b1);

        void setShowIndex(int position);

        void setCanController(boolean b);

        void setShowAvatarP2A(AvatarPTA avatarP2A);

        void updateAvatarP2As();

        void setGLSurfaceViewSize(boolean b);

        GLSurfaceView getmGLSurfaceView();

        void initDebug(TextView mVersionText);

        FUPTARenderer getFUP2ARenderer();

        CameraRenderer getCameraRenderer();

        List<AvatarPTA> getAvatarP2As();

        AvatarPTA getCurrentDrivenAvatar();

        int getDrivenAvatarShowIndex();

        void setIsAR(boolean isAR);

        void setCurrentDrivenAvatar(AvatarPTA avatarPTA);

        boolean getIsAR();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof MainActivity){
            MainActivity mainActivity = (MainActivity) context;
            mFUP2ARenderer = mainActivity.getFUP2ARenderer();
            mP2ACore = mainActivity.getP2ACore();
            mAvatarHandle = mainActivity.getAvatarHandle();
            mCameraRenderer = mainActivity.getCameraRenderer();
            mAvatarP2AS = mainActivity.getAvatarP2As();
            if(context instanceof IContextCallback){
                mActivity = (IContextCallback) context;
            }
        }else{
            mActivity = FUDemoManager.getInstance().getContextCallbackImple();
            mFUP2ARenderer = FUDemoManager.getInstance().getFUP2ARenderer();
            mP2ACore = FUDemoManager.getInstance().getP2ACore();
            mAvatarHandle = FUDemoManager.getInstance().getAvatarHandle();
            mAvatarP2AS = FUDemoManager.getInstance().getAvatarP2As();
        }

    }

    public void onBackPressed() {
        if(mActivity != null){
            mActivity.showHomeFragment();
        }
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    public CameraRenderer getCameraRenderer() {
        return mCameraRenderer;
    }

    public void setmP2ACore(PTACore mP2ACore) {
        this.mP2ACore = mP2ACore;
    }
}
