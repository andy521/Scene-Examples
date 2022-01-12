package com.faceunity.pta_art;

import android.content.Context;

import com.faceunity.pta_art.constant.ColorConstant;
import com.faceunity.pta_art.core.authpack;
import com.faceunity.pta_art.core.client.PTAClientWrapper;
import com.faceunity.pta_art.utils.sta.TtsEngineUtils;
import com.faceunity.pta_art.web.OkHttpUtils;
import com.faceunity.pta_helper.FUAuthCheck;

public class FUDemoInitializer {
    public static Context mContext;

    public static void init(Context context){
        mContext = context.getApplicationContext();
        OkHttpUtils.initOkHttpUtils(OkHttpUtils.initOkHttpClient());

        //初始化P2A Helper ----工具类
        FUAuthCheck.fuP2ASetAuth(authpack.A());
        long endInitP2ATime = System.currentTimeMillis();

        PTAClientWrapper.setupData(context);
        PTAClientWrapper.setupStyleData(context);

        TtsEngineUtils.getInstance().init(context);

        //风格选择后初始化 P2A client
        ColorConstant.init(context);
    }
}
