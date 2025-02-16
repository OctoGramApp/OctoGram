/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberTextView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ShareAlert;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import it.octogram.android.OctoConfig;
import it.octogram.android.crashlytics.CrashOption;
import it.octogram.android.crashlytics.Crashlytics;
import it.octogram.android.logs.OctoLogging;
import it.octogram.android.preferences.ui.custom.CrashLogCell;

@SuppressLint("NotifyDataSetChanged")
public class OctoLogsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private final Object PARTIAL = new Object();

    private int settingsHeaderRow;
    private int copyInfoRow;
    private int settingsShadowRow;
    private int crashesHeaderRow;
    private int crashesStartRow;
    private int crashesEndRow;
    private int crashesInfoRow;

    protected int rowCount;
    protected Context context;
    protected RecyclerListView listView;
    private ListAdapter listAdapter;
    private NumberTextView selectedCountTextView;

    private String sharingFullLocation;
    private String sharingFileName;
    private LongSparseArray<TLRPC.Dialog> sharingDialogs;

    private final int MENU_DELETE = 1;
    private final int MENU_DELETE_ALL = 2;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRowsId();
        return true;
    }

    @Override
    public View createView(Context context) {
        this.context = context;
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle("OctoGram Logs");
        actionBar.setAllowOverlayTitle(true);
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        FrameLayout frameLayout = (FrameLayout) fragmentView;
        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);

        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        itemAnimator.setDelayAnimations(false);
        listView.setItemAnimator(itemAnimator);

        listAdapter = new ListAdapter(context);
        listView.setAdapter(listAdapter);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener(this::onItemClick);
        listView.setOnItemLongClickListener((view, position) -> {
            if (position >= crashesStartRow && position < crashesEndRow) {
                listAdapter.toggleSelected(position);
                return true;
            }
            return false;
        });
        actionBar.setBackButtonDrawable(new BackDrawable(false));

        if (Crashlytics.getArchivedCrashFiles().length > 0) {
            var actionNoSelectMenu = actionBar.createMenu();
            actionNoSelectMenu.addItem(MENU_DELETE_ALL, R.drawable.msg_delete);
        }

        var actionMode = actionBar.createActionMode();
        selectedCountTextView = new NumberTextView(actionMode.getContext());
        selectedCountTextView.setTextSize(18);
        selectedCountTextView.setTypeface(AndroidUtilities.bold());
        selectedCountTextView.setTextColor(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon));
        actionMode.addView(selectedCountTextView, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f, 72, 0, 0, 0));
        actionMode.addItemWithWidth(MENU_DELETE, R.drawable.msg_delete, AndroidUtilities.dp(54));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (onBackPressed()) {
                        finishFragment();
                    }
                } else {
                    if (id == MENU_DELETE || id == MENU_DELETE_ALL) {
                        listAdapter.processSelectionMenu(id);
                    }
                }
            }
        });
        return fragmentView;
    }

    private void showWarningDialog(String text) {
        AlertDialog.Builder warningBuilder = new AlertDialog.Builder(getContext());
        warningBuilder.setTitle(getString(R.string.Warning));
        warningBuilder.setPositiveButton(getString(R.string.OK), (dialog1, which1) -> dialog1.dismiss());
        warningBuilder.setMessage(text);
        showDialog(warningBuilder.create());

    }

    private void copyShowBulletin (CharSequence text) {
        if (AndroidUtilities.addToClipboard(text)) {
            BulletinFactory.of(this).createCopyBulletin("Configuration copied to clipboard").show();
        }
    }


    private void onItemClick(View view, int position, float x, float y) {
        if (position == copyInfoRow) {
            try {
                String configuration = Crashlytics.getSystemInfo();
                copyShowBulletin(configuration);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else if (position >= crashesStartRow && position < crashesEndRow) {
            if (listAdapter.hasSelected()) {
                listAdapter.toggleSelected(position);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), getResourceProvider());
                builder.setTitle("Logs Options");
                var items = new CharSequence[]{
                        "Open log",
                        "Send log",
                };
                var icons = new int[]{
                        R.drawable.msg_openin,
                        R.drawable.msg_send,
                };
                builder.setItems(items, icons, (dialog, which) -> {
                    var file =  OctoLogging.getLogFiles()[crashesEndRow - position - 1];
                    switch (CrashOption.Companion.fromValue(which)) {
                        case CrashOption.OPEN_LOG -> {
                            if (!openLog(file)) {
                                showWarningDialog(getString(R.string.ErrorSendingCrashContent));
                            }
                        }
                        case CrashOption.SEND_LOG -> {
                            if (!sendLog(file)) {
                                showWarningDialog(getString(R.string.ErrorSendingCrashContent));
                            }
                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.Cancel), null);
                showDialog(builder.create());
            }
        }
    }

    protected void updateRowsId() {
        rowCount = 0;
        crashesHeaderRow = -1;
        crashesEndRow = -1;
        crashesInfoRow = -1;

        settingsHeaderRow = rowCount++;
        copyInfoRow = rowCount++;
        settingsShadowRow = rowCount++;

        var logs = OctoLogging.getLogFiles();
        if (logs.length > 0) {
            crashesHeaderRow = rowCount++;
            crashesStartRow = rowCount;

            for (int i = 0; i < logs.length; i++) {
                rowCount++;
                crashesEndRow = rowCount;
            }
        }

        crashesInfoRow = rowCount++;
    }

    @Override
    public boolean onBackPressed() {
        if (listAdapter.hasSelected()) {
            listAdapter.clearSelected();
            return false;
        }
        return super.onBackPressed();
    }

    private boolean openLog(File file) {
        File cacheFile = OctoLogging.shareLogFile(file.getAbsoluteFile());
        if (cacheFile != null) {
            AndroidUtilities.openForView(cacheFile, cacheFile.getName(), OctoConfig.CRASH_MIME_TYPE, getParentActivity(), getResourceProvider(), false);
            return true;
        }
        return false;
    }

    private boolean sendLog(File file) {
        File cacheFile = OctoLogging.shareLogFile(file);
        if (cacheFile != null) {

            ShareAlert shAlert = new ShareAlert(getParentActivity(), null, null, false, null, false) {

                @Override
                protected void onSend(LongSparseArray<TLRPC.Dialog> didS, int count, TLRPC.TL_forumTopic topic) {
                    sharingFileName = cacheFile.getName();
                    sharingFullLocation = cacheFile.getAbsolutePath();
                    sharingDialogs = didS;

                    var instance = FileLoader.getInstance(getCurrentAccount());

                    initUpdateReceiver();
                    instance.uploadFile(cacheFile.getPath(), false, true, ConnectionsManager.FileTypeFile);
                }
            };
            shAlert.show();

            return true;
        }
        return false;
    }

    /** @noinspection rawtypes*/
    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private final Context mContext;
        private final SparseBooleanArray selectedItems = new SparseBooleanArray();

        private final int TYPE_SETTINGS_HEADER = 0;
        private final int TYPE_BUTTON = 1;
        private final int TYPE_SHADOW = 2;
        private final int TYPE_CRASH_FILE = 3;
        private final int TEXT_HINT_WITH_PADDING = 4;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @NonNull
        @NotNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case TYPE_SETTINGS_HEADER:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case TYPE_BUTTON:
                    view = new TextCell(context, 23, false, false, getResourceProvider());
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case TYPE_SHADOW:
                    view = new ShadowSectionCell(mContext);
                    break;
                case TYPE_CRASH_FILE:
                    view = new CrashLogCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case TEXT_HINT_WITH_PADDING:
                    view = new TextInfoPrivacyCell(context);
                    break;
            }
            if (view == null) throw new IllegalArgumentException("Invalid viewType " + viewType);
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        /** @noinspection SequencedCollectionMethodCanBeUsed*/
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
            Object payload = payloads.isEmpty() ? null : payloads.get(0);
            boolean partial = PARTIAL.equals(payload);

            switch (holder.getItemViewType()) {
                case TYPE_SETTINGS_HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == settingsHeaderRow) {
                        headerCell.setText("Options");
                    } else if (position == crashesHeaderRow) {
                        headerCell.setText("Logs");
                    }
                    break;
                case TYPE_BUTTON:
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == copyInfoRow) {
                        textCell.setTextAndIcon(getString(R.string.CopyOctogramConfiguration), R.drawable.msg_copy, !OctoConfig.INSTANCE.disableDividers.getValue());
                    }
                    break;
                case TEXT_HINT_WITH_PADDING:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == crashesInfoRow) {
                        textInfoPrivacyCell.setText("OctoGram logs are stored in the app's internal storage. You can send them to the developers for analysis.");
                    }
                    break;
                case TYPE_CRASH_FILE:
                    if (position >= crashesStartRow && position < crashesEndRow) {
                        CrashLogCell crashLogCell = (CrashLogCell) holder.itemView;

                        var sorted = Arrays.stream(OctoLogging.getLogFiles())
                                .sorted(Comparator.comparingLong(File::lastModified))
                                .collect(Collectors.toList());
                        for (int i = 0; i < sorted.size(); i++) {
                            var file = sorted.get(i);
                            if (crashesEndRow - position - 1 == i) {
                                crashLogCell.setData(file, true);
                                crashLogCell.setSelected(selectedItems.get(position, false), partial);
                                break;
                            }
                        }

                    }
                    break;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemViewType(int position) {
            if (position == copyInfoRow) {
                return TYPE_BUTTON;
            } else if (position >= crashesStartRow && position < crashesEndRow) {
                return TYPE_CRASH_FILE;
            } else if (position == settingsHeaderRow || position == crashesHeaderRow) {
                return TYPE_SETTINGS_HEADER;
            } else if (position == settingsShadowRow) {
                return TYPE_SHADOW;
            } else if (position == crashesInfoRow) {
                return TEXT_HINT_WITH_PADDING;
            }
            throw new IllegalArgumentException("Invalid position " + position);
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getItemViewType() == TYPE_CRASH_FILE || holder.getItemViewType() == TYPE_BUTTON;
        }

        public void toggleSelected(int position) {
            selectedItems.put(position, !selectedItems.get(position, false));
            notifyItemRangeChanged(crashesStartRow, crashesEndRow - crashesStartRow, PARTIAL);
            checkActionMode();
        }

        public boolean hasSelected() {
            return selectedItems.indexOfValue(true) != -1;
        }

        public void clearSelected() {
            selectedItems.clear();
            notifyItemRangeChanged(crashesStartRow, crashesEndRow - crashesStartRow, PARTIAL);
            checkActionMode();
        }

        private void checkActionMode() {
            int selectedCount = getSelectedCount();
            boolean actionModeShowed = actionBar.isActionModeShowed();
            if (selectedCount > 0) {
                selectedCountTextView.setNumber(selectedCount, actionModeShowed);
                if (!actionModeShowed) {
                    actionBar.showActionMode();
                }
            } else if (actionModeShowed) {
                actionBar.hideActionMode();
            }
        }

        private void processSelectionMenu(int id) {
            switch (id) {
                case MENU_DELETE:
                    List<File> toDelete = new ArrayList<>();
                    for (int i = 0; i < selectedItems.size(); i++) {
                        if (selectedItems.valueAt(i)) {
                            RecyclerView.ViewHolder viewHolder = listView.findViewHolderForAdapterPosition(selectedItems.keyAt(i));
                            if (viewHolder == null) continue;
                            CrashLogCell crashLogCell = (CrashLogCell) viewHolder.itemView;
                            File file = crashLogCell.getCrashLog();
                            toDelete.add(file);
                        }
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(getString(R.string.BuildAppName));
                    builder.setPositiveButton(getString(R.string.OK), (dialog, which) -> {
                        int unableToDeleteCount = 0;
                        for (File file : toDelete) {
                            if (!file.delete()) {
                                OctoLogging.e("Could not delete file " + file);
                                unableToDeleteCount++;
                            }
                        }
                        selectedItems.clear();
                        updateRowsId();
                        notifyDataSetChanged();
                        actionBar.hideActionMode();

                        String message;
                        if (toDelete.size() > 1) {
                            message = String.format(Locale.US, "Logs deleted: %d", toDelete.size());
                        } else {
                            message = "Log deleted";
                        }
                        if (unableToDeleteCount > 0) {
                            message = String.format("Could not delete %s logs", unableToDeleteCount);
                            AlertDialog.Builder unableToDeleteBuilder = new AlertDialog.Builder(context);
                            unableToDeleteBuilder.setTitle(getString(R.string.BuildAppName));
                            unableToDeleteBuilder.setMessage(message);
                            unableToDeleteBuilder.setPositiveButton(getString(R.string.OK), (dialog1, which1) -> dialog1.dismiss());
                        }
                        BulletinFactory.of(OctoLogsActivity.this).createErrorBulletin(message).show();
                    });
                    builder.setNegativeButton(getString(R.string.Cancel), (dialog, which) -> dialog.dismiss());

                    String message;
                    if (toDelete.size() > 1) {
                        message = String.format("Do you want to delete %s logs? This action is irreversible", toDelete.size());
                    } else {
                        message = "Do you want to delete one log? This action is irreversible";
                    }
                    builder.setMessage(message);
                    showDialog(builder.create());
                    break;
                case MENU_DELETE_ALL:
                    AlertDialog.Builder allBuilder = new AlertDialog.Builder(context);
                    allBuilder.setTitle(getString(R.string.BuildAppName));
                    allBuilder.setPositiveButton(getString(R.string.OK), (dialog, which) -> {
                        OctoLogging.deleteLogs();
                        updateRowsId();
                        notifyDataSetChanged();

                        var filesToDelete = OctoLogging.getLogFiles();
                        String message1;
                        if (filesToDelete.length > 1) {
                            message1 = String.format("%s logs deleted", filesToDelete.length);
                        } else {
                            message1 = "One log deleted";
                        }
                        BulletinFactory.of(OctoLogsActivity.this).createErrorBulletin(message1).show();
                    });
                    allBuilder.setNegativeButton(getString(R.string.Cancel), (dialog, which) -> dialog.dismiss());

                    File[] filesToDelete = OctoLogging.getLogFiles();
                    String allMessage;
                    if (filesToDelete.length > 1) {
                        allMessage = String.format("Do you want to delete %s logs? This action is irreversible", filesToDelete.length);
                    } else {
                        allMessage = "Do you want to delete one log? This action is irreversible";
                    }
                    allBuilder.setMessage(allMessage);
                    showDialog(allBuilder.create());
                    break;
            }
        }

        public int getSelectedCount() {
            int count = 0;
            for (int i = 0, size = selectedItems.size(); i < size; i++) {
                if (selectedItems.valueAt(i)) {
                    count++;
                }
            }
            return count;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void notifyDataSetChanged() {
            if (listView.isComputingLayout()) {
                listView.post(this::notifyDataSetChanged);
                return;
            }
            super.notifyDataSetChanged();
        }

        @Override
        public void notifyItemChanged(int position) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemChanged(position));
                return;
            }
            super.notifyItemChanged(position);
        }

        @Override
        public void notifyItemChanged(int position, @Nullable Object payload) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemChanged(position, payload));
                return;
            }
            super.notifyItemChanged(position, payload);
        }

        @Override
        public void notifyItemRangeChanged(int positionStart, int itemCount) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemRangeChanged(positionStart, itemCount));
                return;
            }
            super.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void notifyItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemRangeChanged(positionStart, itemCount, payload));
                return;
            }
            super.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void notifyItemInserted(int position) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemInserted(position));
                return;
            }
            super.notifyItemInserted(position);
        }

        @Override
        public void notifyItemMoved(int fromPosition, int toPosition) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemMoved(fromPosition, toPosition));
                return;
            }
            super.notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void notifyItemRangeInserted(int positionStart, int itemCount) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemRangeInserted(positionStart, itemCount));
                return;
            }
            super.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void notifyItemRangeRemoved(int positionStart, int itemCount) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemRangeRemoved(positionStart, itemCount));
                return;
            }
            super.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void notifyItemRemoved(int position) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemRemoved(position));
                return;
            }
            super.notifyItemRemoved(position);
        }
    }


    private void initUpdateReceiver() {
        NotificationCenter.getInstance(getCurrentAccount()).addObserver(this, NotificationCenter.fileUploaded);
        NotificationCenter.getInstance(getCurrentAccount()).addObserver(this, NotificationCenter.fileUploadFailed);
    }

    private void stopUpdateReceiver() {
        NotificationCenter.getInstance(getCurrentAccount()).removeObserver(this, NotificationCenter.fileUploaded);
        NotificationCenter.getInstance(getCurrentAccount()).removeObserver(this, NotificationCenter.fileUploadFailed);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.fileUploaded) {
            String location = (String) args[0];
            TLRPC.InputFile inputFile = (TLRPC.InputFile) args[1];

            if (inputFile == null) {
                return;
            }

            if (!Objects.equals(location, sharingFullLocation)) {
                return;
            }

            stopUpdateReceiver();

            AndroidUtilities.runOnUIThread(() -> BulletinFactory.of(this).createSimpleBulletin(R.raw.forward, "Logs has been sent successfully.").show());

            TLRPC.TL_documentAttributeFilename attr = new TLRPC.TL_documentAttributeFilename();
            attr.file_name = sharingFileName;

            TLRPC.TL_inputMediaUploadedDocument inputMediaDocument = new TLRPC.TL_inputMediaUploadedDocument();
            inputMediaDocument.file = inputFile;
            inputMediaDocument.attributes.add(attr);
            inputMediaDocument.mime_type = OctoConfig.CRASH_MIME_TYPE;

            for (int i = 0; i < sharingDialogs.size(); i++) {
                TLRPC.TL_messages_sendMedia req = new TLRPC.TL_messages_sendMedia();
                req.peer = MessagesController.getInstance(currentAccount).getInputPeer(sharingDialogs.keyAt(i));
                req.random_id = SendMessagesHelper.getInstance(currentAccount).getNextRandomId();
                req.silent = true;
                req.media = inputMediaDocument;
                ConnectionsManager.getInstance(currentAccount).sendRequest(req, null);
            }
        } else if (id == NotificationCenter.fileUploadFailed) {
            String location = (String) args[0];

            if (!Objects.equals(location, sharingFullLocation)) {
                return;
            }

            stopUpdateReceiver();
        }
    }
}
