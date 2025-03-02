/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Build;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.voip.VoIPActionsReceiver;
import org.telegram.messenger.voip.VoIPService;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.LaunchActivity;

import it.octogram.android.logs.OctoLogging;

/**
 * @noinspection unused
 */
public class CustomNotificationStyle extends VoIPService {
    private static void showNotification(VoIPService voIPService, String name, Bitmap photo) {
        Intent intent = new Intent(voIPService, LaunchActivity.class).setAction(voIPService.groupCall != null ? "voip_chat" : "voip");
        if (voIPService.groupCall != null) {
            intent.putExtra("currentAccount", voIPService.getCurrentAccount());
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(voIPService, NotificationsController.OTHER_NOTIFICATIONS_CHANNEL)
                .setContentText(name)
                .setContentIntent(PendingIntent.getActivity(voIPService, 50, intent, PendingIntent.FLAG_MUTABLE));
        if (voIPService.groupCall != null) {
            builder.setContentTitle(ChatObject.isChannelOrGiga(voIPService.getChat()) ? LocaleController.getString(R.string.VoipLiveStream) : LocaleController.getString(R.string.VoipVoiceChat));
            builder.setSmallIcon(voIPService.isMicMute() ? R.drawable.voicechat_muted : R.drawable.voicechat_active);
        } else {
            builder.setContentTitle(LocaleController.getString(R.string.VoipOutgoingCall));
            builder.setSmallIcon(R.drawable.ic_call);
            builder.setOngoing(true);
        }
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setShowWhen(false);
        builder.setColor(0xff282e31);
        builder.setColorized(true);
        Intent endIntent = new Intent(voIPService, VoIPActionsReceiver.class);
        endIntent.setAction(voIPService.getPackageName() + ".END_CALL");
        var caller = new androidx.core.app.Person.Builder()
                .setIcon(IconCompat.createWithAdaptiveBitmap(MediaDataController.convertBitmapToAdaptive(photo)))
                .setName(name)
                .build();
        NotificationCompat.CallStyle callStyle = NotificationCompat.CallStyle.forOngoingCall(caller, PendingIntent.getBroadcast(voIPService, 0, endIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        callStyle.setIsVideo(voIPService.videoCall);
        builder.setStyle(callStyle);
        if (voIPService.groupCall != null) {
            builder.setContentText(ChatObject.isChannelOrGiga(voIPService.getChat()) ? LocaleController.getString(R.string.VoipLiveStream) : LocaleController.getString(R.string.VoipVoiceChat));
        } else {
            builder.setContentText(LocaleController.getString(R.string.VoipOutgoingCall));
        }
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationsController.checkOtherNotificationsChannel();
            builder.setChannelId(NotificationsController.OTHER_NOTIFICATIONS_CHANNEL);
        }
        if (photo != null) {
            builder.setLargeIcon(photo);
        }
        // return builder.build();
    }

    public static void customShowNotificationBuild(VoIPService voIPService, NotificationCompat.Builder builder, String name, Bitmap photo) {
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setShowWhen(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setColor(0xff282e31);
            builder.setColorized(true);
        } else {
            builder.setColor(0xff2ca5e0);
        }
        Intent endIntent = new Intent(voIPService, VoIPActionsReceiver.class);
        endIntent.setAction(voIPService.getPackageName() + ".END_CALL");
        androidx.core.app.Person caller = new androidx.core.app.Person.Builder()
                .setIcon(IconCompat.createWithAdaptiveBitmap(MediaDataController.convertBitmapToAdaptive(photo)))
                .setName(name)
                .build();
        NotificationCompat.CallStyle callStyle = NotificationCompat.CallStyle.forOngoingCall(caller, PendingIntent.getBroadcast(voIPService, 0, endIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        callStyle.setIsVideo(voIPService.videoCall);
        builder.setStyle(callStyle);
        if (voIPService.groupCall != null) {
            builder.setContentText(ChatObject.isChannelOrGiga(voIPService.getChat()) ? LocaleController.getString(R.string.VoipLiveStream) : LocaleController.getString(R.string.VoipVoiceChat));
        } else {
            builder.setContentText(LocaleController.getString(R.string.VoipOutgoingCall));
        }
    }

    public Notification showIncomingNotification(String name, CharSequence subText, TLObject userOrChat, boolean video, int currentAccount) {
        var activity = LaunchActivity.class;
        Intent intent = new Intent(this, activity);
        intent.setAction("voip");

        var builder = new Notification.Builder(this)
                .setContentTitle(video ? LocaleController.getString(R.string.VoipInVideoCallBranding) : LocaleController.getString(R.string.VoipInCallBranding))
                .setContentText(name)
                .setSmallIcon(OctoUtils.getNotificationIcon())
                .setSubText(subText)
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE));

        SharedPreferences preferences = MessagesController.getGlobalNotificationsSettings();
        int chanIndex = preferences.getInt("calls_notification_channel", 0);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // API level 26 and above
            NotificationChannel existingChannel = nm.getNotificationChannel("incoming_calls4" + chanIndex);

            boolean needCreate = true;
            if (existingChannel != null) {
                // Checking getImportance and getSound safely for API level 26+
                if (existingChannel.getImportance() < NotificationManager.IMPORTANCE_HIGH || existingChannel.getSound() != null) {
                    OctoLogging.d("User messed up the notification channel; deleting it and creating a proper one");
                    nm.deleteNotificationChannel("incoming_calls4" + chanIndex);
                    chanIndex++;
                    preferences.edit().putInt("calls_notification_channel", chanIndex).apply();
                } else {
                    needCreate = false;
                }
            }

            if (needCreate) {
                AudioAttributes attrs = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_RING)
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .build();
                NotificationChannel chan = new NotificationChannel("incoming_calls4" + chanIndex, LocaleController.getString(R.string.IncomingCallsSystemSetting), NotificationManager.IMPORTANCE_HIGH);
                chan.setSound(null, attrs);
                chan.setDescription(LocaleController.getString(R.string.IncomingCallsSystemSettingDescription));
                chan.enableVibration(false);
                chan.enableLights(false);
                chan.setBypassDnd(true);
                nm.createNotificationChannel(chan);
            }

            builder.setChannelId("incoming_calls4" + chanIndex);
        }

        String packageName = getPackageName();
        if (packageName == null) {
            OctoLogging.e("Package name is null. Cannot set action for intents.");
            return builder.build();
        }


        Intent endIntent = new Intent(this, VoIPActionsReceiver.class);
        endIntent.setAction(packageName + ".DECLINE_CALL");
        endIntent.putExtra("call_id", getCallID());
        var endTitle = new SpannableString(LocaleController.getString(R.string.VoipDeclineCall));
        endTitle.setSpan(new ForegroundColorSpan(Color.parseColor("#F44336")), 0, endTitle.length(), 0);

        PendingIntent endPendingIntent = PendingIntent.getBroadcast(this, 0, endIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
        builder.addAction(R.drawable.ic_call_end_white_24dp, endTitle, endPendingIntent);

        Intent answerIntent = new Intent(this, VoIPActionsReceiver.class);
        answerIntent.setAction(packageName + ".ANSWER_CALL");
        answerIntent.putExtra("call_id", getCallID());
        var answerTitle = new SpannableString(LocaleController.getString(R.string.VoipAnswerCall));
        answerTitle.setSpan(new ForegroundColorSpan(Color.parseColor("#00AA00")), 0, answerTitle.length(), 0);

        PendingIntent answerPendingIntent = PendingIntent.getBroadcast(this, 0, answerIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
        builder.addAction(R.drawable.ic_call, answerTitle, answerPendingIntent);

        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setShowWhen(false);
        builder.setColor(Color.parseColor("#2ca5e0"));
        builder.setVibrate(new long[0]);
        builder.setCategory(Notification.CATEGORY_CALL);
        builder.setFullScreenIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE), true);

        if (userOrChat instanceof TLRPC.User) {
            TLRPC.User user;
            user = (TLRPC.User) userOrChat;
            if (!TextUtils.isEmpty(user.phone)) {
                builder.addPerson("tel:" + user.phone);
            }
        }

        Bitmap avatar = getRoundAvatarBitmap(this, currentAccount, userOrChat);
        builder.setLargeIcon(avatar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API level 31+
            builder.setColor(Color.parseColor("#282E31"));
            builder.setColorized(true);
            var caller = new android.app.Person.Builder()
                    .setIcon(Icon.createWithAdaptiveBitmap(MediaDataController.convertBitmapToAdaptive(avatar)))
                    .setName(name)
                    .build();
            Notification.CallStyle callStyle = Notification.CallStyle.forIncomingCall(caller, endPendingIntent, answerPendingIntent);
            callStyle.setIsVideo(video);
            builder.setStyle(callStyle);
        } else { // For API level below 31
            RemoteViews customView = new RemoteViews(packageName, LocaleController.isRTL ? R.layout.call_notification_rtl : R.layout.call_notification);
            customView.setTextViewText(R.id.name, name);
            if (TextUtils.isEmpty(subText)) {
                customView.setViewVisibility(R.id.subtitle, View.GONE);
            } else {
                customView.setTextViewText(R.id.title, subText);
            }
            customView.setTextViewText(R.id.answer_text, LocaleController.getString(R.string.VoipAnswerCall));
            customView.setTextViewText(R.id.decline_text, LocaleController.getString(R.string.VoipDeclineCall));
            customView.setImageViewBitmap(R.id.photo, avatar);
            customView.setOnClickPendingIntent(R.id.answer_btn, answerPendingIntent);
            customView.setOnClickPendingIntent(R.id.decline_btn, endPendingIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setCustomHeadsUpContentView(customView);
                builder.setCustomBigContentView(customView);
            }
        }

        return builder.build();
    }
}
