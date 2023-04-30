package it.octogram.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.TopicsController;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Cells.UserCell2;
import org.telegram.ui.Components.FlickerLoadingView;

import java.util.ArrayList;
import java.util.Comparator;

import it.octogram.android.translator.AutoTranslateConfig;
import it.octogram.ui.Cells.EditTopic;

public class AutoTranslateGroupInfo extends BaseSettingsActivity implements NotificationCenter.NotificationCenterDelegate {

    private final TLRPC.Chat chat;
    private final ArrayList<TLRPC.TL_forumTopic> topics = new ArrayList<>();
    private final boolean isAllow;
    private boolean isDefault;
    private final EditExceptionDelegate editExceptionDelegate;

    private int avatarRow;
    private int dividerRow;
    private int headerRow;
    private int startTopicsRow;
    private int endTopicsRow;
    private int topicPlaceholderRow;
    private int topicExceptionHintRow;
    private int removeGroupExceptionRow;
    private int divider2Row;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("ChannelEdit", R.string.ChannelEdit);
    }

    public AutoTranslateGroupInfo(TLRPC.Chat chat, EditExceptionDelegate delegate) {
        this(chat, false, delegate);
    }

    public AutoTranslateGroupInfo(TLRPC.Chat chat, boolean isAllow) {
        this(chat, isAllow, null);
    }

    public AutoTranslateGroupInfo(TLRPC.Chat chat, boolean isAllow, EditExceptionDelegate delegate) {
        this.chat = chat;
        ArrayList<TLRPC.TL_forumTopic> tmp = getMessagesController().getTopicsController().getTopics(chat.id);
        if (tmp != null) {
            tmp.stream().sorted(Comparator.comparing(o -> o.title)).forEach(topics::add);
        }
        this.isAllow = isAllow;
        this.editExceptionDelegate = delegate;
        boolean allowed = AutoTranslateConfig.getExceptionsById(true, -chat.id);
        boolean disabled = AutoTranslateConfig.getExceptionsById(false, -chat.id);
        this.isDefault = !allowed && !disabled;
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        super.onItemClick(view, position, x, y);
        // Start Topics
        if (position >= startTopicsRow && position < endTopicsRow) {
            TLRPC.TL_forumTopic topic = topics.get(position - startTopicsRow);
            AutoTranslateConfig.setEnabled(-chat.id, topic.id, !AutoTranslateConfig.isAutoTranslateEnabled(-chat.id, topic.id));
            listAdapter.notifyItemChanged(position, PARTIAL);
            if (editExceptionDelegate == null) {
                if (AutoTranslateConfig.getExceptions(isAllow).isEmpty()) {
                    hideLastFragment();
                } else {
                    showLastFragment();
                }
            } else {
                editExceptionDelegate.onExceptionChanged();
                if (isDefault) {
                    isDefault = false;
                    listAdapter.notifyItemChanged(removeGroupExceptionRow);
                }
            }
        } else if (position == removeGroupExceptionRow) {
            if (isDefault) return;
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("NotificationsDeleteAllExceptionTitle", R.string.NotificationsDeleteAllExceptionTitle));
            builder.setMessage(LocaleController.formatString("NotificationsDeleteAllExceptionAlert", R.string.NotificationsDeleteAllExceptionAlert, chat.title));
            builder.setPositiveButton(LocaleController.getString("Delete", R.string.Delete), (dialogInterface, i) -> {
                AutoTranslateConfig.removeGroupException(-chat.id);
                if (editExceptionDelegate == null) {
                    if (AutoTranslateConfig.getExceptions(isAllow).isEmpty()) {
                        hideLastFragment();
                    }
                } else {
                    editExceptionDelegate.onExceptionChanged();
                }
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
        startTopicsRow = -1;
        endTopicsRow = -1;
        topicPlaceholderRow = -1;

        avatarRow = rowCount++;
        dividerRow = rowCount++;
        headerRow = rowCount++;
        if (!topics.isEmpty()) {
            startTopicsRow = rowCount;
            rowCount += topics.size();
            endTopicsRow = rowCount;
        }
        if (!getMessagesController().getTopicsController().endIsReached(chat.id) || topics.isEmpty()) {
            topicPlaceholderRow = rowCount++;
        }
        topicExceptionHintRow = rowCount++;
        removeGroupExceptionRow = rowCount++;
        divider2Row = rowCount++;
    }

    @Override
    protected BaseListAdapter createAdapter() {
        return new ListAdapter();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.topicsDidLoaded) {
            Long chatId = (Long) args[0];
            if (this.chat.id == chatId) {
                topics.clear();
                ArrayList<TLRPC.TL_forumTopic> tmp = getMessagesController().getTopicsController().getTopics(chat.id);
                tmp.stream().sorted(Comparator.comparing(o -> o.title)).forEach(topics::add);
                updateRowsId();
                listAdapter.notifyDataSetChanged();
                if (!getMessagesController().getTopicsController().endIsReached(chat.id)) {
                    getMessagesController().getTopicsController().loadTopics(chat.id);
                }
            }
        }
    }

    @Override
    public boolean onFragmentCreate() {
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.topicsDidLoaded);
        getMessagesController().getTopicsController().loadTopics(chat.id, true, TopicsController.LOAD_TYPE_LOAD_NEXT);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.topicsDidLoaded);
    }

    private class ListAdapter extends BaseListAdapter {

        @Override
        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            switch (ViewType.fromInt(holder.getItemViewType())) {
                case CHAT:
                    UserCell2 userCell = (UserCell2) holder.itemView;
                    userCell.setData(chat, null, null, 0);
                    break;
                case HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == headerRow) {
                        headerCell.setText(LocaleController.getString("EditExceptionTitle", R.string.EditExceptionTitle));
                    }
                    break;
                case EDIT_TOPIC:
                    EditTopic topicCell = (EditTopic) holder.itemView;
                    TLRPC.TL_forumTopic topic = topics.get(position - startTopicsRow);
                    topicCell.setData(topic);
                    topicCell.setChecked(AutoTranslateConfig.isAutoTranslateEnabled(-chat.id, topic.id), partial);
                    break;
                case TEXT_HINT_WITH_PADDING:
                    TextInfoPrivacyCell hintCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == topicExceptionHintRow) {
                        hintCell.setText(LocaleController.getString("EditExceptionHint", R.string.EditExceptionHint));
                    }
                    break;
                case SETTINGS:
                    TextSettingsCell settingsCell = (TextSettingsCell) holder.itemView;
                    if (position == removeGroupExceptionRow) {
                        settingsCell.setTag(Theme.key_dialogTextRed);
                        settingsCell.setTextColor(Theme.getColor(Theme.key_dialogTextRed));
                        settingsCell.setCanDisable(true);
                        settingsCell.setText(LocaleController.getString("NotificationsDeleteAllException", R.string.NotificationsDeleteAllException), false);
                    }
                    break;
                case PLACEHOLDER:
                    FlickerLoadingView flickerLoadingView = (FlickerLoadingView) holder.itemView;
                    flickerLoadingView.setViewType(FlickerLoadingView.EDIT_TOPIC_CELL_TYPE);
                    flickerLoadingView.setIsSingleCell(true);
                    break;
            }
        }

        @Override
        protected ViewType getViewType(int position) {
            if (position == avatarRow) {
                return ViewType.CHAT;
            } else if (position == dividerRow || position == divider2Row) {
                return ViewType.SHADOW;
            } else if (position == headerRow) {
                return ViewType.HEADER;
            } else if (position >= startTopicsRow && position < endTopicsRow) {
                return ViewType.EDIT_TOPIC;
            } else if (position == topicExceptionHintRow) {
                return ViewType.TEXT_HINT_WITH_PADDING;
            } else if (position == removeGroupExceptionRow) {
                return ViewType.SETTINGS;
            } else if (position == topicPlaceholderRow) {
                return ViewType.PLACEHOLDER;
            }
            return null;
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.CHAT || viewType == ViewType.SETTINGS && !isDefault;
        }
    }

    public interface EditExceptionDelegate {
        void onExceptionChanged();
    }
}
