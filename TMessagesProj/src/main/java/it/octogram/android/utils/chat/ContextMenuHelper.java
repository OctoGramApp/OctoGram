/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.chat;

import android.content.Context;
import android.view.ViewPropertyAnimator;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;

import java.util.ArrayList;

import it.octogram.android.app.ui.cells.ChatSettingsPreviewsCell.ContextMenuPreviewItem;

public class ContextMenuHelper {
    private static final int MAX_SHORTCUTS = 4;
    public static final ContextMenuHelper INSTANCE = new ContextMenuHelper();

    public static boolean mustUseSwipeback() {
        return false;
    }

    public static ArrayList<ContextMenuPreviewItem> fillPreviewMenu(Context context) {
        return new ContextMenuComposer(context).build();
    }

    public static void fillMenu(Context context, ArrayList<CharSequence> items, ArrayList<Integer> options, ArrayList<Integer> icons, OnItemAddReady callback) {
        new ContextMenuComposer(context, items, options, icons, callback);
    }

    private static boolean mustSkipItem(Integer option) {
        return false;
    }

    private static class ContextMenuComposer {
        private final ArrayList<ContextMenuPreviewItem> finalItems = new ArrayList<>();

        public ContextMenuComposer(Context context, ArrayList<CharSequence> items, ArrayList<Integer> options, ArrayList<Integer> icons, @Nullable OnItemAddReady callback) {}

        public ContextMenuComposer(Context context) {}

        private void fillState() {}

        private void fixState() {}

        private void handleItem(int id, int icon, CharSequence name, Object category) {}

        private ArrayList<Integer> getVisibleShortcutsList() {
            return new ArrayList<>();
        }

        private void handleShortcuts() {}

        private ArrayList<Integer> handleSubCategories() {
            return new ArrayList<>();
        }

        private ArrayList<ContextMenuPreviewItem> build() {
            return finalItems;
        }

        private Object composeSubCategoryLayout(Object category) {
            return null;
        }

        private Object getPopupWindowLayout() {
            return null;
        }

        private int getCategoryAvailableOptionsCount(Object category) {
            return 0;
        }
    }

    public interface OnItemAddReady {
        default ActionBarPopupWindow.ActionBarPopupWindowLayout getPopupWindowLayout() { return null; }
        default void onItemAdd(int id, ActionBarMenuSubItem item) {}
        default void onShortcutsAdd(ShortcutsLayout shortcutsLayout) {}
        default void onSeparatorAdd() {}
        default void onItemClick(int id) {}
    }

    public static class ShortcutsLayout extends LinearLayout {
        public ShortcutsLayout(Context context) {
            super(context);
        }

        private boolean isFirstAppear = true;
        private boolean lastDrawState = false;
        private boolean isAnimatingPreview = false;
        private final ArrayList<ViewPropertyAnimator> animators = new ArrayList<>();

        public void fillOptions(ArrayList<CharSequence> items, ArrayList<Integer> options, ArrayList<Integer> icons) {
            fillOptions(items, options, icons, false);
        }

        public void fillOptions(ArrayList<CharSequence> items, ArrayList<Integer> options, ArrayList<Integer> icons, boolean faster) {}

        public void fillPreviewOptions() {}

        public void setOnItemClick(Object onItemClick) {}
    }

    public Object getAccountInstance(int currentAccount) {
        return null;
    }

    public Object getMessagesController(int currentAccount) {
        return null;
    }
}