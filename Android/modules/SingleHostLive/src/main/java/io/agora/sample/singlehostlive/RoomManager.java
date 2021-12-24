package io.agora.sample.singlehostlive;

import android.view.View;

import androidx.annotation.DrawableRes;

public class RoomManager {




    public static class RoomInfo {
        public String roomName;
        public int userCount;
        public int bgColor;

        public RoomInfo(String roomName, int userCount, int bgColor) {
            this.roomName = roomName;
            this.userCount = userCount;
            this.bgColor = bgColor;
        }

    }

    public static class MessageInfo{
        public String userName;
        public String content;
        public @DrawableRes int giftIcon = View.NO_ID;

        public MessageInfo(String userName, String content) {
            this.userName = userName;
            this.content = content;
        }

        public MessageInfo(String userName, String content, int giftIcon) {
            this.userName = userName;
            this.content = content;
            this.giftIcon = giftIcon;
        }

    }

    public static class UserInfo {

    }
}
