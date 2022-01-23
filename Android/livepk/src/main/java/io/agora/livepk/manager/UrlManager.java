package io.agora.livepk.manager;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class UrlManager {

    public static final AbsUrl  WanSuUrl = new AbsUrl("网宿") {
        @Override
        public String getPushUrl(String streamName) {
            return String.format(Locale.US, "rtmp://examplepush.agoramde.agoraio.cn/live/%s", streamName);
        }

        @Override
        public String getPullUrl(String streamName) {
            return String.format(Locale.US, "http://examplepull.agoramde.agoraio.cn/live/%s.flv", streamName);
        }
    };

    public static final AbsUrl JinShangUrl = new AbsUrl("金山") {
        @Override
        public String getPushUrl(String streamName) {
            return String.format(Locale.US, "rtmp://webdemo-push.agora.io/lbhd/%s", streamName);
        }

        @Override
        public String getPullUrl(String streamName) {
            return String.format(Locale.US, "http://webdemo-pull.agora.io/lbhd/%s.flv", streamName);
        }
    };

    public static final AbsUrl TenXunUrl = new AbsUrl("腾讯") {
        @Override
        public String getPushUrl(String streamName) {
            return getSafeUrl("hsP2t2CcM5WfpkJQSaJN", streamName, System.currentTimeMillis() / 1000 + 24 * 60 * 60);
        }

        @Override
        public String getPullUrl(String streamName) {
            return String.format(Locale.US, "rtmp://pull.webdemo.agoraio.cn/lbhd/%s", streamName);
        }
    };

    public static AbsUrl sUrl = TenXunUrl;

    private static final char[] DIGITS_LOWER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static String getSafeUrl(String key, String streamName, long txTime) {
        String input = new StringBuilder().
                append(key).
                append(streamName).
                append(Long.toHexString(txTime).toUpperCase()).toString();

        String txSecret = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            txSecret  = byteArrayToHexString(
                    messageDigest.digest(input.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return txSecret == null ? "" :
                new StringBuilder().
                        append("rtmp://push.webdemo.agoraio.cn/lbhd/").
                        append(streamName + "?").
                        append("txSecret=").
                        append(txSecret).
                        append("&").
                        append("txTime=").
                        append(Long.toHexString(txTime).toUpperCase()).
                        toString();
    }

    private static String byteArrayToHexString(byte[] data) {
        char[] out = new char[data.length << 1];

        for (int i = 0, j = 0; i < data.length; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }
        return new String(out);
    }


    public static abstract class AbsUrl {
        public final String name;
        private AbsUrl(String name){
            this.name = name;
        }
        public abstract String getPushUrl(String streamName);
        public abstract String getPullUrl(String streamName);
    }
}
