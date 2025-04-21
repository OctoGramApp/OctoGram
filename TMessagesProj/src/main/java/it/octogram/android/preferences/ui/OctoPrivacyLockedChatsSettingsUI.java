/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.ListView.AdapterWithDiffUtils;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.UsersSelectActivity;

import java.util.ArrayList;
import java.util.Objects;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.deeplink.DeepLinkDef;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.CustomCellRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.preferences.ui.components.LockedChatsHelp;
import it.octogram.android.utils.account.FingerprintUtils;

public class OctoPrivacyLockedChatsSettingsUI implements PreferencesEntry {
    private PreferencesFragment fragment;
    private ListAdapter chatsAdapter;

    private final ArrayList<LockedChatProperty> oldRecentItems = new ArrayList<>();
    private final ArrayList<LockedChatProperty> currentShownChats = new ArrayList<>();
    private final ConfigProperty<Boolean> showHeader = new ConfigProperty<>(null, true);
    private final ConfigProperty<Boolean> showMoreSettingsOption = new ConfigProperty<>(null, false);

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        this.fragment = fragment;

        if (!OctoConfig.INSTANCE.hasShownLockedChatsTip.getValue()) {
            OctoConfig.INSTANCE.hasShownLockedChatsTip.updateValue(true);
            AndroidUtilities.runOnUIThread(() -> showLockedHelp(context), 300);
        }

        showHeader.updateValue(!FingerprintUtils.hasLockedChats());
        AndroidUtilities.runOnUIThread(this::updateLockedChats, 200);

        return OctoPreferences.builder(getString(R.string.LockedChats))
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.CHATS_PRIVACY, true, getString(R.string.LockedChats_Options_Desc), showHeader)
                .deepLink(DeepLinkDef.PRIVACY_CHATS)
                .addContextMenuItem(new OctoPreferences.OctoContextMenuElement(R.drawable.msg_help, getString(R.string.HowDoesItWork), () -> showLockedHelp(context)))
                .category(R.string.UpdatesOptions, category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.lockedChatsHideChats)
                            .title(getString(R.string.LockedChats_Options_HideChats))
                            .description(getString(R.string.LockedChats_Options_HideChats_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.lockedChatsLockScreenshots)
                            .title(getString(R.string.LockedChats_Options_LockScreenshots))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .isBlue(true)
                            .onClick(() -> {
                                OctoPrivacySettingsUI ui = new OctoPrivacySettingsUI();
                                ui.setFromLockedChatsSettingsUI(true);
                                fragment.presentFragment(new PreferencesFragment(ui));
                            })
                            .icon(R.drawable.msg_customize)
                            .title(getString(R.string.LockedChats_Options_MoreSettings))
                            .showIf(showMoreSettingsOption)
                            .build());
                })
                .category(R.string.Notifications, category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.lockedChatsShowNotifications)
                            .title(getString(R.string.LockedChats_Options_ShowNotifications))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.lockedChatsSpoilerNotifications)
                            .title(getString(R.string.LockedChats_Options_SpoilerContent))
                            .description(getString(R.string.LockedChats_Options_SpoilerContent_Desc))
                            .showIf(OctoConfig.INSTANCE.lockedChatsShowNotifications)
                            .build());
                })
                .category(R.string.LockedChats, category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .isBlue(true)
                            .onClick(this::runUsersSelectActivity)
                            .icon(R.drawable.msg_edit)
                            .title(getString(R.string.LockedChatsHelpHintEditList))
                            .build());
                    category.row(new CustomCellRow.CustomCellRowBuilder().layout(generateView(context)).build());
                })
                .build();
    }

    public void setShowMoreSettingsOption(boolean state) {
        showMoreSettingsOption.updateValue(state);
    }

    private RecyclerListView generateView(Context context) {
        RecyclerListView listView = new RecyclerListView(context) {
            @Override
            public void requestLayout() {
                super.requestLayout();
            }
        };
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position < currentShownChats.size()) {
                LockedChatProperty property = currentShownChats.get(position);
                FingerprintUtils.LockedChat lockedChat = property.getLockedChat();
                ItemOptions options = ItemOptions.makeOptions(fragment, view);
                options.add(R.drawable.menu_unlock, getString(R.string.UnLockChat), () -> {
                    FingerprintUtils.lockChat(lockedChat, false);
                    optimizedRemoveLockedChat(property);
                });
                if (LocaleController.isRTL) {
                    options.setGravity(Gravity.LEFT);
                }
                options.show();
            }
        });
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.setTag(14);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(chatsAdapter = new ListAdapter(context));
        listView.setVerticalScrollBarEnabled(false);
        listView.setClipToPadding(true);
        listView.setNestedScrollingEnabled(false);
        listView.setHasFixedSize(false);
        listView.setPadding(0, 0, 0, 0);
        listView.setGlowColor(Theme.getColor(Theme.key_dialogScrollGlow));
        return listView;
    }

    private void updateLockedChats() {
        oldRecentItems.clear();
        oldRecentItems.addAll(currentShownChats);
        currentShownChats.clear();

        for (FingerprintUtils.LockedChat chat : FingerprintUtils.getLockedChats()) {
            LockedChatProperty property = new LockedChatProperty();
            property.setLockedChat(chat);
            currentShownChats.add(property);
        }

        chatsAdapter.setItems(oldRecentItems, currentShownChats);

        if (showHeader.getValue() != currentShownChats.isEmpty()) {
            showHeader.updateValue(currentShownChats.isEmpty());
            fragment.reloadUIAfterValueUpdate();
        }
    }

    private void optimizedRemoveLockedChat(LockedChatProperty property) {
        if (!currentShownChats.contains(property)) {
            return;
        }

        oldRecentItems.clear();
        oldRecentItems.addAll(currentShownChats);
        currentShownChats.remove(property);
        chatsAdapter.setItems(oldRecentItems, currentShownChats);

        if (showHeader.getValue() != currentShownChats.isEmpty()) {
            showHeader.updateValue(currentShownChats.isEmpty());
            fragment.reloadUIAfterValueUpdate();
        }
    }

    @Override
    public boolean isLockedContent() {
        return true;
    }

    private void showLockedHelp(Context context) {
        BottomSheet[] bottomSheet = new BottomSheet[1];
        LockedChatsHelp lockedChatsHelp = new LockedChatsHelp(context, null, () -> {
            if (bottomSheet[0] != null) {
                bottomSheet[0].dismiss();
                bottomSheet[0] = null;
            }
        }, () -> {
            if (bottomSheet[0] != null) {
                bottomSheet[0].dismiss();
                bottomSheet[0] = null;
            }
            AndroidUtilities.runOnUIThread(this::runUsersSelectActivity, 300);
        });
        lockedChatsHelp.asSettingsUI();
        bottomSheet[0] = new BottomSheet.Builder(context, false, null)
                .setCustomView(lockedChatsHelp, Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                .show();
        bottomSheet[0].fixNavigationBar(Theme.getColor(Theme.key_dialogBackground));
    }

    private void runUsersSelectActivity() {
        UsersSelectActivity activity = getUsersSelectActivity();
        activity.setDelegate((ids, flags) -> {
            FingerprintUtils.clearLockedChats();
            FingerprintUtils.lockChatsMultiFromIDs(ids, true);
            updateLockedChats();
        });
        fragment.presentFragment(activity);
    }

    @NonNull
    private static UsersSelectActivity getUsersSelectActivity() {
        UsersSelectActivity activity = new UsersSelectActivity(true, FingerprintUtils.getLockedChatsIds(), 0);
        activity.asLockedChats();
        return activity;
    }

    private class ListAdapter extends AdapterWithDiffUtils {

        private final Context context;

        public ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getItemViewType() == 0;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ProfileSearchCell view = new ProfileSearchCell(context);
            view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (position < currentShownChats.size()) {
                LockedChatProperty property = currentShownChats.get(position);
                ProfileSearchCell profileSearchCell = (ProfileSearchCell) holder.itemView;

                boolean self = property.getLockedChat().user() != null && property.getLockedChat().user().self;
                String name = null;
                if (self) {
                    name = getString(R.string.SavedMessages);
                }
                profileSearchCell.setData(property.getLockedChat().chat() != null ? property.getLockedChat().chat() : property.getLockedChat().user(), null, name, null, false, self);
                profileSearchCell.useSeparator = true;
            }
        }

        @Override
        public int getItemCount() {
            return currentShownChats.size();
        }

        @Override
        public int getItemViewType(int position) {
            return currentShownChats.get(position).viewType;
        }
    }

    private static class LockedChatProperty extends AdapterWithDiffUtils.Item {
        private FingerprintUtils.LockedChat lockedChat;

        public LockedChatProperty() {
            super(0, false);
        }

        public void setLockedChat(FingerprintUtils.LockedChat lockedChat) {
            this.lockedChat = lockedChat;
        }

        public FingerprintUtils.LockedChat getLockedChat() {
            return lockedChat;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof LockedChatProperty item)) {
                return false;
            }
            if (!Objects.equals(viewType, item.viewType)) {
                return false;
            }
//            if (getLockedChat().chat() != null && item.getLockedChat().chat() != null) {
//                return getLockedChat().chat().id == item.getLockedChat().chat().id;
//            }
//            if (getLockedChat().user() != null && item.getLockedChat().user() != null) {
//                return getLockedChat().user().id == item.getLockedChat().user().id;
//            }
            return false;
        }
    }
}