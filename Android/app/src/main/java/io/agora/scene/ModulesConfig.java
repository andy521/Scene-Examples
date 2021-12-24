package io.agora.scene;

import java.util.ArrayList;
import java.util.List;

public class ModulesConfig {

    private static volatile ModulesConfig INSTANCE = null;

    public static ModulesConfig getInstance() {
        if (INSTANCE == null) {
            synchronized (ModulesConfig.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ModulesConfig();
                }
            }
        }
        return INSTANCE;
    }

    public final List<ModuleInfo> moduleInfos = new ArrayList<>();

    private ModulesConfig() {
        moduleInfos.add(new ModuleInfo(
                io.agora.sample.singlehostlive.R.string.single_host_live_app_name,
                io.agora.sample.singlehostlive.R.string.single_host_live_description,
                io.agora.sample.singlehostlive.R.drawable.home_category_single,
                io.agora.sample.singlehostlive.RoomListActivity.class
        ));
        moduleInfos.add(new ModuleInfo(
                io.agora.livepk.R.string.livepk_app_name,
                io.agora.livepk.R.string.livepk_description,
                io.agora.livepk.R.drawable.home_category_pk,
                io.agora.livepk.activity.LivePKListActivity.class
        ));
        moduleInfos.add(new ModuleInfo(
                io.agora.sample.breakoutroom.R.string.breakoutroom_app_name,
                io.agora.sample.breakoutroom.R.string.breakoutroom_description,
                io.agora.sample.breakoutroom.R.drawable.home_category_multi,
                io.agora.sample.breakoutroom.ui.MainActivity.class
        ));
    }


}
