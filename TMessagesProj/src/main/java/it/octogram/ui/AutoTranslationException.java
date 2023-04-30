package it.octogram.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ManageChatTextCell;
import org.telegram.ui.Cells.ManageChatUserCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.GroupCreateActivity;
import org.telegram.ui.ProfileActivity;

import java.util.ArrayList;

import it.octogram.android.translator.AutoTranslateConfig;

public class AutoTranslationException extends BaseSettingsActivity {

    private final boolean isAllow;

    private int addUsersOrGroupsRow;
    private int exceptionsStartRow;
    private int exceptionsEndRow;
    private int dividerRow;
    private int deleteAllExceptionsRow;
    private int divider2Row;

    public AutoTranslationException(boolean isAllow) {
        this.isAllow = isAllow;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        super.onItemClick(view, position, x, y);
        if (position == addUsersOrGroupsRow) {
            Bundle args = new Bundle();
            args.putBoolean(isAllow ? "isAlwaysShare" : "isNeverShare", true);
            args.putInt("chatAddType", 1);
            GroupCreateActivity fragment = new GroupCreateActivity(args);
            fragment.setDelegate(ids -> {
                for (int i = 0; i < ids.size(); i++) {
                    AutoTranslateConfig.setEnabled(ids.get(i), 0, isAllow);
                }
                updateRowsId();
                listAdapter.notifyDataSetChanged();
            });
            presentFragment(fragment);
        } else if (position >= exceptionsStartRow && position < exceptionsEndRow) {
            Bundle args = new Bundle();
            long uid = AutoTranslateConfig.getExceptions(isAllow).get(position - exceptionsStartRow).dialogId;
            if (DialogObject.isUserDialog(uid)) {
                args.putLong("user_id", uid);
            } else {
                args.putLong("chat_id", -uid);
            }
            args.putBoolean("isSettings", true);
            args.putBoolean("isAlwaysShare", isAllow);
            presentFragment(new ProfileActivity(args));
        } else if (position == deleteAllExceptionsRow) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("NotificationsDeleteAllExceptionTitle", R.string.NotificationsDeleteAllExceptionTitle));
            builder.setMessage(LocaleController.getString("NotificationsDeleteAllExceptionAlert", R.string.NotificationsDeleteAllExceptionAlert));
            builder.setPositiveButton(LocaleController.getString("Delete", R.string.Delete), (dialogInterface, i) -> {
                AutoTranslateConfig.removeAllTypeExceptions(isAllow);
                finishFragment();
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            AlertDialog alertDialog = builder.create();
            showDialog(alertDialog);
            TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(Theme.getColor(Theme.key_dialogTextRed));
            }
        }
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();

        addUsersOrGroupsRow = rowCount++;
        exceptionsStartRow = rowCount;
        rowCount += AutoTranslateConfig.getExceptions(isAllow).size();
        exceptionsEndRow = rowCount;
        dividerRow = rowCount++;
        deleteAllExceptionsRow = rowCount++;
        divider2Row = rowCount++;
    }

    @Override
    protected String getActionBarTitle() {
        return isAllow ? LocaleController.getString("AlwaysAllow", R.string.AlwaysAllow) : LocaleController.getString("NeverAllow", R.string.NeverAllow);
    }

    @Override
    protected BaseListAdapter createAdapter() {
        return new ListAdapter();
    }

    private class ListAdapter extends BaseListAdapter {

        @Override
        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            switch (ViewType.fromInt(holder.getItemViewType())) {
                case ADD_EXCEPTION:
                    ManageChatTextCell actionCell = (ManageChatTextCell) holder.itemView;
                    if (position == addUsersOrGroupsRow) {
                        actionCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                        actionCell.setText(LocaleController.getString("PrivacyAddAnException", R.string.PrivacyAddAnException), null, R.drawable.msg_contact_add, false);
                    }
                    break;
                case MANAGE_CHAT:
                    ManageChatUserCell userCell = (ManageChatUserCell) holder.itemView;
                    AutoTranslateConfig.AutoTranslateException info = AutoTranslateConfig.getExceptions(isAllow).get(position - exceptionsStartRow);
                    userCell.setDelegate((cell, click) -> {
                        if (click) {
                            final ArrayList<String> items = new ArrayList<>();
                            final ArrayList<Integer> actions = new ArrayList<>();
                            final ArrayList<Integer> icons = new ArrayList<>();
                            if (info.chat instanceof TLRPC.Chat && ((TLRPC.Chat) info.chat).forum) {
                                items.add(LocaleController.getString("EditTopicsException", R.string.EditTopicsException));
                                actions.add(0);
                                icons.add(R.drawable.msg_edit);
                            }
                            items.add(LocaleController.getString("EditDeleteException", R.string.EditDeleteException));
                            icons.add(R.drawable.msg_delete);
                            actions.add(1);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                            builder.setItems(items.toArray(new CharSequence[actions.size()]), AndroidUtilities.toIntArray(icons), (dialogInterface, i) -> {
                                if (actions.get(i) == 0) {
                                    AutoTranslateGroupInfo autoTranslateGroupInfo = new AutoTranslateGroupInfo((TLRPC.Chat) info.chat, isAllow);
                                    presentFragment(autoTranslateGroupInfo);
                                } else if (actions.get(i) == 1) {
                                    showConfirmDelete(position, info);
                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            showDialog(alertDialog);
                            alertDialog.setItemColor(items.size() - 1, Theme.getColor(Theme.key_dialogTextRed), Theme.getColor(Theme.key_dialogRedIcon));
                        }
                        return true;
                    });
                    userCell.setData(info.chat, null, null, position != exceptionsEndRow - 1);
                    break;
                case SETTINGS:
                    TextSettingsCell settingsCell = (TextSettingsCell) holder.itemView;
                    if (position == deleteAllExceptionsRow) {
                        settingsCell.setTag(Theme.key_dialogTextRed);
                        settingsCell.setTextColor(Theme.getColor(Theme.key_dialogTextRed));
                        settingsCell.setText(LocaleController.getString("NotificationsDeleteAllException", R.string.NotificationsDeleteAllException), false);
                    }
            }
        }

        private void showConfirmDelete(int position, AutoTranslateConfig.AutoTranslateException info) {
            AlertDialog.Builder builder2 = new AlertDialog.Builder(getParentActivity());
            builder2.setTitle(LocaleController.getString("EditDeleteException", R.string.EditDeleteException));
            String chatTitle;
            if (info.chat instanceof TLRPC.Chat) {
                chatTitle = ((TLRPC.Chat) info.chat).title;
            } else {
                chatTitle = ContactsController.formatName(((TLRPC.User) info.chat).first_name, ((TLRPC.User) info.chat).last_name);
            }
            builder2.setMessage(LocaleController.formatString("EditRemoveExceptionText", R.string.EditRemoveExceptionText, chatTitle));
            builder2.setPositiveButton(LocaleController.getString("Delete", R.string.Delete), (dialogInterface2, i2) -> {
                if (info.chat instanceof TLRPC.Chat) {
                    AutoTranslateConfig.removeGroupException(info.dialogId);
                } else {
                    AutoTranslateConfig.setDefault(info.dialogId, 0);
                }
                if (AutoTranslateConfig.getExceptions(isAllow).isEmpty()) {
                    finishFragment();
                } else {
                    listAdapter.notifyItemRemoved(position);
                    updateRowsId();
                }
            });
            builder2.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            AlertDialog alertDialog = builder2.create();
            showDialog(alertDialog);
            TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(Theme.getColor(Theme.key_dialogTextRed));
            }
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.ADD_EXCEPTION || viewType == ViewType.MANAGE_CHAT || viewType == ViewType.SETTINGS;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position == addUsersOrGroupsRow) {
                return ViewType.ADD_EXCEPTION;
            } else if (position >= exceptionsStartRow && position < exceptionsEndRow) {
                return ViewType.MANAGE_CHAT;
            } else if (position == dividerRow || position == divider2Row) {
                return ViewType.SHADOW;
            } else if (position == deleteAllExceptionsRow) {
                return ViewType.SETTINGS;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }
}
