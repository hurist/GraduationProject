package com.ffcc66.diary.setting;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.ffcc66.diary.MainActivity;
import com.ffcc66.diary.R;
import com.ffcc66.diary.base.GlobalValues;

public class RemindReceiver extends BroadcastReceiver {

    private NotificationManager notificationManager = null;
    private static final int NOTIFICATION_FLAG = 3;
    private static final String TAG = "RemindReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (intent.getAction().equals(GlobalValues.REMIND_ACTION_REPEAT)) {

            Intent intent1 = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent1, 0);

            Notification.Builder builde = new Notification.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏中的小图片，尺寸一般建议在24×24
                    .setTicker("今天记录生活了吗？") // 设置显示的提示文字
                    .setContentTitle("Diary") // 设置显示的标题
                    .setContentText("您有日记提醒哦") // 消息的详细内容
                    .setContentIntent(pendingIntent) // 关联PendingIntent
                    .setNumber(1);            // 在TextView的右方显示的数字，可以在外部定义一个变量，点击累加setNumber(count),这时显示的和

            builde.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel channel = new NotificationChannel("1", "mine", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
                builde.setChannelId("1");
            }
            manager.notify(NOTIFICATION_FLAG, builde.getNotification());
        }
    }
}
