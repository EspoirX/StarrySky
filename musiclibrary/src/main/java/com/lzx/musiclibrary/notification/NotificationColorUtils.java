package com.lzx.musiclibrary.notification;

import android.app.Notification;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.util.concurrent.CountDownLatch;

/**
 * Created by xian on 2018/2/20.
 */

public class NotificationColorUtils {
    public static String NOTIFICATION_TITLE = "nice_music_title";
    public static String NOTIFICATION_CONTENT = "nice_music_content";
    public static int COLOR_UNDEF = 987654321;
    private static final double COLOR_THRESHOLD = 180.0D;
    public static int NOTIFICATION_TITLE_COLOR = Color.parseColor("#de000000");
    public static int NOTIFICATION_LINE2_COLOR = Color.parseColor("#8a000000");
    public static int EVENTCONTENT_TITLE_COLOR = -1;
    public static int EVENTCONTENT_COLOR = Color.parseColor("#b3ffffff");
    private static TextView titleView = null;
    private static TextView contentView = null;
    private static NotificationColorUtils.NotificationColorModel mNotificationColorModel;

    public NotificationColorUtils() {
    }

    public static void setTitleTextColor(Context context, RemoteViews remoteView, int viewId) {
        if(mNotificationColorModel == null) {
            isDarkNotificationBar(context);
        }

        if(mNotificationColorModel.getTitleColor() == COLOR_UNDEF && Build.VERSION.SDK_INT >= 21) {
            if(mNotificationColorModel.isDarkNotificationBg()) {
                mNotificationColorModel.setTitleColor(EVENTCONTENT_TITLE_COLOR);
            } else {
                mNotificationColorModel.setTitleColor(NOTIFICATION_TITLE_COLOR);
            }
        }

        remoteView.setTextColor(viewId, mNotificationColorModel.getTitleColor());
    }

    public static void setContentTextColor(Context context, RemoteViews remoteView, int viewId) {
        if(mNotificationColorModel == null) {
            isDarkNotificationBar(context);
        }

        if(mNotificationColorModel.getContentColor() == COLOR_UNDEF && Build.VERSION.SDK_INT >= 21) {
            if(mNotificationColorModel.isDarkNotificationBg()) {
                mNotificationColorModel.setContentColor(EVENTCONTENT_COLOR);
            } else {
                mNotificationColorModel.setContentColor(NOTIFICATION_LINE2_COLOR);
            }
        }

        remoteView.setTextColor(viewId, mNotificationColorModel.getContentColor());
    }

    public static synchronized boolean isDarkNotificationBar(final Context context) {
        if(mNotificationColorModel == null) {
            mNotificationColorModel = new NotificationColorUtils.NotificationColorModel();
            boolean isInMainThread = Looper.myLooper() == Looper.getMainLooper();
            CountDownLatch countDownLatch = null;
            if(!isInMainThread) {
                countDownLatch = new CountDownLatch(1);
            }

            final CountDownLatch finalCountDownLatch = countDownLatch;
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        int notiTextColor = NotificationColorUtils.getNotificationColor(context);
                        if(notiTextColor == NotificationColorUtils.COLOR_UNDEF) {
                            NotificationColorUtils.mNotificationColorModel.setTitleColor(NotificationColorUtils.COLOR_UNDEF);
                            NotificationColorUtils.mNotificationColorModel.setContentColor(NotificationColorUtils.COLOR_UNDEF);
                            NotificationColorUtils.mNotificationColorModel.setDarkNotificationBg(true);
                        } else {
                            boolean isDark = !NotificationColorUtils.isTextColorSimilar(-16777216, notiTextColor);
                            NotificationColorUtils.mNotificationColorModel.setDarkNotificationBg(isDark);
                        }
                    } catch (Exception var3) {
                        NotificationColorUtils.mNotificationColorModel.setTitleColor(NotificationColorUtils.COLOR_UNDEF);
                        NotificationColorUtils.mNotificationColorModel.setContentColor(NotificationColorUtils.COLOR_UNDEF);
                        NotificationColorUtils.mNotificationColorModel.setDarkNotificationBg(true);
                    }

                    if(NotificationColorUtils.mNotificationColorModel.getTitleColor() == NotificationColorUtils.COLOR_UNDEF && Build.VERSION.SDK_INT >= 21) {
                        if(NotificationColorUtils.mNotificationColorModel.isDarkNotificationBg()) {
                            NotificationColorUtils.mNotificationColorModel.setTitleColor(NotificationColorUtils.EVENTCONTENT_TITLE_COLOR);
                        } else {
                            NotificationColorUtils.mNotificationColorModel.setTitleColor(NotificationColorUtils.NOTIFICATION_TITLE_COLOR);
                        }
                    }

                    if(NotificationColorUtils.mNotificationColorModel.getContentColor() == NotificationColorUtils.COLOR_UNDEF && Build.VERSION.SDK_INT >= 21) {
                        if(NotificationColorUtils.mNotificationColorModel.isDarkNotificationBg()) {
                            NotificationColorUtils.mNotificationColorModel.setContentColor(NotificationColorUtils.EVENTCONTENT_COLOR);
                        } else {
                            NotificationColorUtils.mNotificationColorModel.setContentColor(NotificationColorUtils.NOTIFICATION_LINE2_COLOR);
                        }
                    }

                    if(finalCountDownLatch != null) {
                        finalCountDownLatch.countDown();
                    }

                }
            };
            if(isInMainThread) {
                runnable.run();
            } else {
                (new Handler(Looper.getMainLooper())).post(runnable);
                if(countDownLatch != null) {
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException var6) {
                        var6.printStackTrace();
                    }
                }
            }
        }

        return mNotificationColorModel.isDarkNotificationBg();
    }

    private static int getNotificationColor(Context context) {
        int smallIcon = 0;

        try {
            Resources res = context.getResources();
            smallIcon = res.getIdentifier("notification_default", "drawable", context.getPackageName());
        } catch (Exception var8) {
            var8.printStackTrace();
        }

        NotificationCompat.Builder mBuilder = (new NotificationCompat.Builder(context)).setSmallIcon(smallIcon).setContentTitle(NOTIFICATION_TITLE).setContentText(NOTIFICATION_CONTENT);
        Notification notification = mBuilder.build();
        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        ViewGroup viewGroup = (ViewGroup)notification.contentView.apply(context, layout);
        getTextView(viewGroup, NOTIFICATION_TITLE, NOTIFICATION_CONTENT);
        if(titleView == null) {
            return COLOR_UNDEF;
        } else {
            int color = titleView.getCurrentTextColor();
            mNotificationColorModel.setTitleColor(color);
            if(contentView != null) {
                int contentColor = contentView.getCurrentTextColor();
                mNotificationColorModel.setContentColor(contentColor);
            }

            return color;
        }
    }

    private static TextView getTextView(ViewGroup viewGroup, String textTitle, String content) {
        if(viewGroup == null) {
            return null;
        } else {
            int count = viewGroup.getChildCount();

            for(int i = 0; i < count; ++i) {
                View view = viewGroup.getChildAt(i);
                if(view instanceof TextView) {
                    TextView newDtv = (TextView)view;
                    if(newDtv.getText().equals(NOTIFICATION_TITLE)) {
                        titleView = newDtv;
                    }

                    if(newDtv.getText().equals(NOTIFICATION_CONTENT)) {
                        contentView = newDtv;
                    }
                } else if(view instanceof ViewGroup) {
                    getTextView((ViewGroup)view, textTitle, content);
                }
            }

            return null;
        }
    }

    private static boolean isTextColorSimilar(int baseColor, int color) {
        int simpleBaseColor = baseColor | -16777216;
        int simpleColor = color | -16777216;
        int baseRed = Color.red(simpleBaseColor) - Color.red(simpleColor);
        int baseGreen = Color.green(simpleBaseColor) - Color.green(simpleColor);
        int baseBlue = Color.blue(simpleBaseColor) - Color.blue(simpleColor);
        double value = Math.sqrt((double)(baseRed * baseRed + baseGreen * baseGreen + baseBlue * baseBlue));
        return value < 180.0D;
    }

    static class NotificationColorModel {
        private int titleColor;
        private int contentColor;
        private boolean isDarkNotificationBg;

        NotificationColorModel() {
            this.titleColor = NotificationColorUtils.COLOR_UNDEF;
            this.contentColor = NotificationColorUtils.COLOR_UNDEF;
            this.isDarkNotificationBg = true;
        }

        public int getTitleColor() {
            return this.titleColor;
        }

        public void setTitleColor(int titleColor) {
            this.titleColor = titleColor;
        }

        public int getContentColor() {
            return this.contentColor;
        }

        public void setContentColor(int contentColor) {
            this.contentColor = contentColor;
        }

        public boolean isDarkNotificationBg() {
            return this.isDarkNotificationBg;
        }

        public void setDarkNotificationBg(boolean darkNotificationBg) {
            this.isDarkNotificationBg = darkNotificationBg;
        }
    }
}
