package it.octogram.android.preferences.ui.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.HashMap;
import java.util.Map;

import it.octogram.android.MediaFilter;
import it.octogram.android.OctoConfig;

@SuppressLint({"ClickableViewAccessibility", "ViewConstructor"})
public class CustomMediaFilterDialog extends LinearLayout {
    private ActionBarPopupWindow selectPopupWindow;
    private final TLRPC.Chat currentChat;

    public CustomMediaFilterDialog(Context parentActivity, Theme.ResourcesProvider resourcesProvider, TLRPC.Chat currentChat, CustomMediaFilterDelegate callback) {
        super(parentActivity);
        setOrientation(VERTICAL);

        this.currentChat = currentChat;

        ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout2 = getActionBarPopupWindowLayout(parentActivity, resourcesProvider);

        int currentSelectedId = OctoConfig.INSTANCE.mediaFiltering.getValue();

        int[] filtersList = getMediaFiltersList();
        for (int id : filtersList) {
            if (id != 0 && !canShowFilter(id)) {
                continue;
            }

            ActionBarMenuSubItem filterItem = new ActionBarMenuSubItem(getContext(), true, id == 0, id == filtersList.length - 1, resourcesProvider);
            filterItem.setTextAndIcon(LocaleController.getString(getTitleByMediaFilterId(id)), currentSelectedId == id ? 0 : getIconByMediaFilterId(id));
            filterItem.setMinimumWidth(AndroidUtilities.dp(196));
            filterItem.setOnClickListener(v -> {
                selectPopupWindow.dismiss();
                OctoConfig.INSTANCE.mediaFiltering.updateValue(id);
                callback.onSelectedData(getIconByMediaFilterId(id, true));
            });

            if (currentSelectedId == id) {
                filterItem.setChecked(true);
                filterItem.setClickable(false);
                filterItem.setEnabled(false);
            }

            popupLayout2.addView(filterItem, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
        }

        addView(popupLayout2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    @NonNull
    private ActionBarPopupWindow.ActionBarPopupWindowLayout getActionBarPopupWindowLayout(Context parentActivity, Theme.ResourcesProvider resourcesProvider) {
        ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout2 = new ActionBarPopupWindow.ActionBarPopupWindowLayout(parentActivity, resourcesProvider);
        popupLayout2.setAnimationEnabled(false);
        popupLayout2.setOnTouchListener(new OnTouchListener() {

            private final Rect popupRect = new Rect();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    if (selectPopupWindow != null && selectPopupWindow.isShowing()) {
                        v.getHitRect(popupRect);
                        if (!popupRect.contains((int) event.getX(), (int) event.getY())) {
                            selectPopupWindow.dismiss();
                        }
                    }
                }
                return false;
            }
        });
        popupLayout2.setDispatchKeyEventListener(keyEvent -> {
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getRepeatCount() == 0 && selectPopupWindow != null && selectPopupWindow.isShowing()) {
                selectPopupWindow.dismiss();
            }
        });
        popupLayout2.setShownFromBottom(false);
        return popupLayout2;
    }

    public void setSelectPopupWindow(ActionBarPopupWindow selectPopupWindow) {
        this.selectPopupWindow = selectPopupWindow;
    }

    private int[] getMediaFiltersList() {
        return new int[]{
                MediaFilter.ALL.getValue(),
                MediaFilter.PHOTOS.getValue(),
                MediaFilter.VIDEOS.getValue(),
                MediaFilter.VOICE_MESSAGES.getValue(),
                MediaFilter.VIDEO_MESSAGES.getValue(),
                MediaFilter.FILES.getValue(),
                MediaFilter.MUSIC.getValue(),
                MediaFilter.GIFS.getValue(),
                MediaFilter.LOCATIONS.getValue(),
                MediaFilter.CONTACTS.getValue(),
                MediaFilter.MENTIONS.getValue(),
                MediaFilter.URL.getValue(),
                MediaFilter.PINNED_MESSAGES.getValue(),
                MediaFilter.CHAT_PHOTOS.getValue()
        };
    }

    private boolean canShowFilter(int id) {
        if (id == MediaFilter.MENTIONS.getValue()) {
            return currentChat != null && (!ChatObject.isChannel(currentChat) || currentChat.megagroup);
            // just groups and supergroups
        }

        if (id == MediaFilter.CHAT_PHOTOS.getValue() || id == MediaFilter.PINNED_MESSAGES.getValue()) {
            return currentChat != null; // groups/supergroups/channels/megagroup
        }

        return true;
    }

    private int getIconByMediaFilterId(int id, boolean isOutsideContextMenu) {
        // Initialize the HashMap with media filter IDs and their corresponding drawable resources
        Map<Integer, Integer> mediaFilterToIconMap = new HashMap<>();
        mediaFilterToIconMap.put(MediaFilter.PHOTOS.getValue(), R.drawable.msg_photos);
        mediaFilterToIconMap.put(MediaFilter.VIDEOS.getValue(), R.drawable.msg_videocall);
        mediaFilterToIconMap.put(MediaFilter.VOICE_MESSAGES.getValue(), R.drawable.msg_voice_unmuted);
        mediaFilterToIconMap.put(MediaFilter.VIDEO_MESSAGES.getValue(), R.drawable.msg_video);
        mediaFilterToIconMap.put(MediaFilter.FILES.getValue(), R.drawable.msg_view_file);
        mediaFilterToIconMap.put(MediaFilter.MUSIC.getValue(), R.drawable.msg_played);
        mediaFilterToIconMap.put(MediaFilter.GIFS.getValue(), R.drawable.msg_gif);
        mediaFilterToIconMap.put(MediaFilter.LOCATIONS.getValue(), R.drawable.msg_location);
        mediaFilterToIconMap.put(MediaFilter.CONTACTS.getValue(), R.drawable.msg_contacts);
        mediaFilterToIconMap.put(MediaFilter.MENTIONS.getValue(), R.drawable.msg_mention);
        mediaFilterToIconMap.put(MediaFilter.URL.getValue(), R.drawable.msg_link2);
        mediaFilterToIconMap.put(MediaFilter.PINNED_MESSAGES.getValue(), R.drawable.msg_pinnedlist);
        mediaFilterToIconMap.put(MediaFilter.CHAT_PHOTOS.getValue(), R.drawable.msg_edit);

        Integer icon = mediaFilterToIconMap.get(id);
        return icon != null ? icon : (isOutsideContextMenu ? R.drawable.msg_download_settings : R.drawable.msg_cancel);
    }

    private int getIconByMediaFilterId(int id) {
        return getIconByMediaFilterId(id, false);
    }

    private String getTitleByMediaFilterId(int id) {
        Map<Integer, String> titlesMap = new HashMap<>();
        titlesMap.put(MediaFilter.PHOTOS.getValue(), "MediaFilterItemPhotos");
        titlesMap.put(MediaFilter.VIDEOS.getValue(), "MediaFilterItemVideos");
        titlesMap.put(MediaFilter.VOICE_MESSAGES.getValue(), "MediaFilterItemVoiceM");
        titlesMap.put(MediaFilter.VIDEO_MESSAGES.getValue(), "MediaFilterItemVideoM");
        titlesMap.put(MediaFilter.FILES.getValue(), "MediaFilterItemFiles");
        titlesMap.put(MediaFilter.MUSIC.getValue(), "MediaFilterItemMusic");
        titlesMap.put(MediaFilter.GIFS.getValue(), "MediaFilterItemGifs");
        titlesMap.put(MediaFilter.LOCATIONS.getValue(), "MediaFilterItemLocations");
        titlesMap.put(MediaFilter.CONTACTS.getValue(), "MediaFilterItemContacts");
        titlesMap.put(MediaFilter.MENTIONS.getValue(), "MediaFilterItemMentions");
        titlesMap.put(MediaFilter.URL.getValue(), "MediaFilterItemUrl");
        titlesMap.put(MediaFilter.PINNED_MESSAGES.getValue(), "MediaFilterItemPinnedM");
        titlesMap.put(MediaFilter.CHAT_PHOTOS.getValue(), "MediaFilterItemChatPhotos");
        return titlesMap.getOrDefault(id, "MediaFilterItemAll");
    }

    public interface CustomMediaFilterDelegate {
        void onSelectedData(int newResIcon);
    }
}