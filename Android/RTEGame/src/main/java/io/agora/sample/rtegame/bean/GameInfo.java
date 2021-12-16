package io.agora.sample.rtegame.bean;

import androidx.annotation.NonNull;

public class GameInfo {
    public static final int START = 2;
    public static final int END = 3;

    //        1 - 未开始, 2 - 进行中, 3 - 已结束 (需要游戏有一个加载完成的回调)
    private int status;
    //        屏幕共享对应的uid
    private final int gameUid;
    // 游戏ID
    private final int gameId;

    public GameInfo(int status, int gameUid, int gameId) {
        this.status = status;
        this.gameUid = gameUid;
        this.gameId = gameId;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public int getGameUid() {
        return gameUid;
    }

    public int getGameId() {
        return gameId;
    }

    @Override
    public String toString() {
        return "GameInfo{" +
                "status=" + status +
                ", gameUid=" + gameUid +
                ", gameId='" + gameId + '\'' +
                '}';
    }
}
