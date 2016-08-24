package com.comtrade.gcb.client.gyft;

import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Created by muros on 1.8.2016.
 */
public class GyftUtil {

    public static String createCardUrl(String host, String root, String cardKey) {
        String fullURLString = String.format(
                "%s%s%s?reveal=true",
                host,
                root,
                cardKey
        );

        return fullURLString;
    }

    public static String createRootUrl(String host, String root, String method) {
        String rootURLString = String.format("%s%s%s?" , host, root, method);

        return rootURLString;
    }

    public static String createFullUrl(String host, String root, String method, String apiKey, String apiSecret, String timestamp)
            throws Exception {
        String rootURLString = createRootUrl(host, root, method);
        String fullURLString = appendCredentials(rootURLString, apiKey, apiSecret, timestamp);

        return fullURLString;
    }

    public static String appendCredentials(String prebuid, String apiKey, String apiSecret, String timestamp) throws  Exception {
        String signature = computeSig(apiKey, apiSecret, timestamp);
        String fullURLString = String.format("%s&api_key=%s&sig=%s", prebuid, apiKey, signature);

        return fullURLString;
    }

    public static String createTimestamp() {
        long time = System.currentTimeMillis();
        String timestamp = Long.toString(Math.round(time / 1000.0));

        return timestamp;
    }

    public static String computeSig(String apiKey, String apiSecret, String timestamp) throws Exception {

        String stringToSign = apiKey + apiSecret + timestamp;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(stringToSign.getBytes("UTF-8"));
        String signature = hexEncode(hash);

        return signature;
    }

    private static String hexEncode(final byte[] bytes) {
        StringBuilder hexEncode = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) hexEncode.append('0');
            hexEncode.append(hex);
        }
        return hexEncode.toString();
    }
}
