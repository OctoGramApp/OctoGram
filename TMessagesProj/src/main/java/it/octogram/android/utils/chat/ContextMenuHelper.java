package it.octogram.android.utils.chat;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AnimatedEmojiSpan;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;

import it.octogram.android.ContextMenuBriefingState;
import it.octogram.android.OctoConfig;
import it.octogram.android.ai.MainAiHelper;
import it.octogram.android.preferences.ui.components.ChatSettingsPreviews;

public class ContextMenuHelper {
    public static final ContextMenuHelper INSTANCE = new ContextMenuHelper();

    public static boolean mustUseSwipeback() {
        return false;
    }

    public static ArrayList<ChatSettingsPreviews.ContextMenuPreviewItem> fillPreviewMenu(Context context) {
        return new ArrayList<>();
    }

    public static void fillMenu(Context context, ArrayList<CharSequence> items, ArrayList<Integer> options, ArrayList<Integer> icons, OnItemAddReady callback) {
    }

    public interface OnItemAddReady {
        ActionBarPopupWindow.ActionBarPopupWindowLayout getPopupWindowLayout();
        void onItemAdd(int id, ActionBarMenuSubItem item);
        void onShortcutsAdd(ShortcutsLayout shortcutsLayout);
        void onSeparatorAdd();
        void onItemClick(int id);
    }

    public static class ShortcutsLayout extends LinearLayout {
        public ShortcutsLayout(Context context) {
            super(context);
        }

        public void fillOptions(ArrayList<CharSequence> items, ArrayList<Integer> options, ArrayList<Integer> icons) {
        }

        public void fillOptions(ArrayList<CharSequence> items, ArrayList<Integer> options, ArrayList<Integer> icons, boolean faster) {
        }

        public void fillPreviewOptions() {
        }

        public void setOnItemClick(Utilities.Callback<Integer> onItemClick) {
        }
    }

    public AccountInstance getAccountInstance(int currentAccount) {
        return null;
    }

    public MessagesController getMessagesController(int currentAccount) {
        return null;
    }
}