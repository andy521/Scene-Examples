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
                io.agora.sample.singlehostlive.R.drawable.single_host_live_poster,
                io.agora.sample.singlehostlive.RoomListActivity.class
        ));
        moduleInfos.add(new ModuleInfo(
                io.agora.superapp.R.string.superapp_app_name,
                io.agora.superapp.R.string.superapp_app_name,
                io.agora.superapp.R.drawable.home_category_multi,
                io.agora.superapp.view.RoomListActivity.class
        ));
    }


}
