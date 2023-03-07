/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-07-15 07:33:28
 */

package com.hinnka.tsbrowser.util;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import com.hinnka.tsbrowser.R;


public class NotificationUtils {
    public static String getDownloadChannel(Context context) {
        return createChannel(context, "download", context.getString(R.string.download), NotificationManager.IMPORTANCE_LOW);
    }
    public static String createChannel(Context context, String channelID, String channelNAME, int level) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(channelID, channelNAME, level);
            manager.createNotificationChannel(channel);
            return channelID;
        } else {
            return null;
        }
    }
}
