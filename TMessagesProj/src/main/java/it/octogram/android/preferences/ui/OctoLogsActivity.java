/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import it.octogram.android.OctoConfig;
import it.octogram.android.crashlytics.CrashOption;
import it.octogram.android.crashlytics.Crashlytics;
import it.octogram.android.logs.OctoLogging;
import it.octogram.android.preferences.ui.custom.CrashLogCell;

@SuppressLint("NotifyDataSetChanged")
public class OctoLogsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {
    private static final String TAG = "OctoLogsActivity";
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
    private final File[] archivedCrashFile = OctoLogging.getLogFiles();
    private List<File> sortedArchivedCrashFiles;

    private final int MENU_DELETE = 1;
    private final int MENU_DELETE_ALL = 2;

    @Override
    public boolean onFragmentCreate() {
        OctoLogging.d(TAG, "onFragmentCreate");
        super.onFragmentCreate();
        if (archivedCrashFile != null && archivedCrashFile.length > 0) {
            sortedArchivedCrashFiles = Arrays.stream(archivedCrashFile)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingLong(File::lastModified).reversed())
                    .collect(Collectors.toList());
        } else {
            sortedArchivedCrashFiles = new ArrayList<>();
        }
        updateRowsId();
        return true;
    }

    @Override
    public View createView(Context context) {
        OctoLogging.d(TAG, "createView");
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

        if (archivedCrashFile != null && archivedCrashFile.length > 0) {
            var actionNoSelectMenu = actionBar.createMenu();
            actionNoSelectMenu.addItem(MENU_DELETE_ALL, R.drawable.msg_delete);
        }

        var actionMode = actionBar.createActionMode();
        selectedCountTextView = new NumberTextView(actionMode.getContext());
        selectedCountTextView.setTextSize(18);
        selectedCountTextView.setTypeface(AndroidUtilities.bold());
        selectedCountTextView.setTextColor(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon));
        actionMode.addView(selectedCountTextView, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f, 72, 0, 0, 0));
        actionMode.addItemWithWidth(MENU_DELETE, R.drawable.msg_delete, dp(54));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                OctoLogging.d(TAG, "ActionBar item clicked: " + id);
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
        if (getContext() == null) {
            OctoLogging.w(TAG, "Context is null, cannot show warning dialog");
            return;
        }
        AlertDialog.Builder warningBuilder = new AlertDialog.Builder(getContext());
        warningBuilder.setTitle(getString(R.string.Warning));
        warningBuilder.setPositiveButton(getString(R.string.OK), (dialog1, which1) -> dialog1.dismiss());
        warningBuilder.setMessage(text);
        showDialog(warningBuilder.create());
        OctoLogging.d(TAG, "Warning dialog shown: " + text);

    }

    private void copyShowBulletin(CharSequence text) {
        if (AndroidUtilities.addToClipboard(text)) {
            BulletinFactory.of(this).createCopyBulletin(getString(R.string.CrashLogCopied)).show();
            OctoLogging.d(TAG, "Log copied to clipboard and bulletin shown");
        } else {
            OctoLogging.w(TAG, "Failed to copy text to clipboard");
        }
    }


    private void onItemClick(View view, int position, float x, float y) {
        OctoLogging.d(TAG, "onItemClick at position: " + position);
        if (position == copyInfoRow) {
            try {
                var configuration = Crashlytics.getSystemInfo();
                copyShowBulletin(configuration);
            } catch (IllegalAccessException e) {
                OctoLogging.e(TAG, "Error getting system info", e);
                throw new RuntimeException(e);
            }
        } else if (position >= crashesStartRow && position < crashesEndRow) {
            if (listAdapter.hasSelected()) {
                listAdapter.toggleSelected(position);
            } else {
                if (getParentActivity() == null) {
                    OctoLogging.w(TAG, "Parent activity is null, cannot show crash settings dialog");
                    return;
                }
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
                    if (sortedArchivedCrashFiles == null) {
                        OctoLogging.w(TAG, "sortedArchivedCrashFiles is null, cannot process crash option");
                        return;
                    }
                    var file = sortedArchivedCrashFiles.get(position - crashesStartRow);
                    switch (CrashOption.Companion.fromValue(which)) {
                        case CrashOption.OPEN_LOG -> {
                            if (file != null && !openLog(file)) {
                                showWarningDialog(getString(R.string.ErrorSendingCrashContent));
                            }
                        }
                        case CrashOption.SEND_LOG -> {
                            if (file != null && !sendLog(file)) {
                                showWarningDialog(getString(R.string.ErrorSendingCrashContent));
                            }
                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.Cancel), null);
                showDialog(builder.create());
                OctoLogging.d(TAG, "Crash settings dialog shown for position: " + position);
            }
        }
    }

    protected void updateRowsId() {
        OctoLogging.d(TAG, "updateRowsId");
        rowCount = 0;
        crashesHeaderRow = -1;
        crashesEndRow = -1;
        crashesInfoRow = -1;

        settingsHeaderRow = rowCount++;
        copyInfoRow = rowCount++;
        settingsShadowRow = rowCount++;

        if (sortedArchivedCrashFiles != null && !sortedArchivedCrashFiles.isEmpty()) { // Check sorted list
            crashesHeaderRow = rowCount++;
            crashesStartRow = rowCount;

            for (int i = 0; i < sortedArchivedCrashFiles.size(); i++) {
                rowCount++;
                crashesEndRow = rowCount;
            }
        }

        crashesInfoRow = rowCount++;
        OctoLogging.d(TAG, "Rows IDs updated. Row count: " + rowCount + ", Crashes end row: " + crashesEndRow);
    }

    @Override
    public boolean onBackPressed() {
        OctoLogging.d(TAG, "onBackPressed");
        if (listAdapter.hasSelected()) {
            listAdapter.clearSelected();
            return false;
        }
        return super.onBackPressed();
    }

    private boolean openLog(File file) {
        if (file == null) {
            OctoLogging.w(TAG, "File is null, cannot open log");
            return false;
        }
        try {
            File cacheFile = Crashlytics.shareLog(file.getAbsoluteFile());
            if (getParentActivity() != null)
                AndroidUtilities.openForView(cacheFile, cacheFile.getName(), OctoConfig.CRASH_MIME_TYPE, getParentActivity(), getResourceProvider(), false);
            OctoLogging.d(TAG, "Crash log opened for viewing: " + file.getName());
            return true;
        } catch (IOException e) {
            OctoLogging.e(TAG, "IO error opening crash log", e);
            return false;
        }
    }

    private boolean sendLog(File file) {
        if (file == null || getParentActivity() == null) {
            OctoLogging.w(TAG, "File or Parent activity is null, cannot send log");
            return false;
        }
        try {
            File cacheFile = OctoLogging.shareLogFile(file);

            ShareAlert shAlert = new ShareAlert(getParentActivity(), null, null, false, null, false, true) {

                @Override
                protected void onSend(LongSparseArray<TLRPC.Dialog> didS, int count, TLRPC.TL_forumTopic topic, boolean showToast) {
                    sharingFullLocation = cacheFile.getAbsolutePath();
                    sharingDialogs = didS;
                    if (!showToast) return;
                    super.onSend(sharingDialogs, count, topic, showToast);
                    sharingFileName = cacheFile.getName();

                    var instance = FileLoader.getInstance(getCurrentAccount());

                    initUpdateReceiver();
                    instance.uploadFile(cacheFile.getPath(), false, true, ConnectionsManager.FileTypeFile);
                    OctoLogging.d(TAG, "File upload initiated for sharing crash log: " + sharingFileName);
                }
            };
            shAlert.show();
            OctoLogging.d(TAG, "Share alert shown for sending crash log");

            return true;
        } catch (IOException e) {
            OctoLogging.e(TAG, "IO error preparing crash log for sending", e);
            return false;
        }
    }

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
            OctoLogging.d(TAG, "ListAdapter created");
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
            if (view == null) {
                OctoLogging.e(TAG, "onCreateViewHolder: Invalid viewType " + viewType);
                throw new IllegalArgumentException("Invalid viewType " + viewType);
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
            //noinspection SequencedCollectionMethodCanBeUsed
            Object payload = payloads.isEmpty() ? null : payloads.get(0);
            boolean partial = PARTIAL.equals(payload);

            switch (holder.getItemViewType()) {
                case TYPE_SETTINGS_HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == settingsHeaderRow) {
                        headerCell.setText(getString(R.string.Settings));
                    } else if (position == crashesHeaderRow) {
                        headerCell.setText(getString(R.string.CrashHistory));
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
                        textInfoPrivacyCell.setText(getString(R.string.CrashLogInfo));
                    }
                    break;
                case TYPE_CRASH_FILE:
                    if (position >= crashesStartRow && position < crashesEndRow) {
                        CrashLogCell crashLogCell = (CrashLogCell) holder.itemView;

                        if (sortedArchivedCrashFiles != null) {
                            File file = sortedArchivedCrashFiles.get(position - crashesStartRow);
                            crashLogCell.setData(file, true);
                            crashLogCell.setSelected(selectedItems.get(position, false), partial);
                        }
                    }
                    break;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
            OctoLogging.d(TAG, "onBindViewHolder at position: " + position);
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
            OctoLogging.e(TAG, "getItemViewType: Invalid position " + position);
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
            OctoLogging.d(TAG, "Item at position " + position + " selection toggled. Selected: " + selectedItems.get(position, false));
        }

        public boolean hasSelected() {
            return selectedItems.indexOfValue(true) != -1;
        }

        public void clearSelected() {
            selectedItems.clear();
            notifyItemRangeChanged(crashesStartRow, crashesEndRow - crashesStartRow, PARTIAL);
            checkActionMode();
            OctoLogging.d(TAG, "Selection cleared");
        }

        private void checkActionMode() {
            int selectedCount = getSelectedCount();
            boolean actionModeShowed = actionBar.isActionModeShowed();
            if (selectedCount > 0) {
                selectedCountTextView.setNumber(selectedCount, actionModeShowed);
                if (!actionModeShowed) {
                    actionBar.showActionMode();
                    OctoLogging.d(TAG, "Action mode shown with selected count: " + selectedCount);
                }
            } else if (actionModeShowed) {
                actionBar.hideActionMode();
                OctoLogging.d(TAG, "Action mode hidden");
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        private void processSelectionMenu(int id) {
            OctoLogging.d(TAG, "processSelectionMenu with id: " + id);
            switch (id) {
                case MENU_DELETE:
                    List<File> toDelete = new ArrayList<>();
                    for (int i = 0; i < selectedItems.size(); i++) {
                        if (selectedItems.valueAt(i)) {
                            RecyclerView.ViewHolder viewHolder = listView.findViewHolderForAdapterPosition(selectedItems.keyAt(i));
                            if (viewHolder == null) {
                                OctoLogging.w(TAG, "ViewHolder is null for position: " + selectedItems.keyAt(i));
                                continue;
                            }
                            CrashLogCell crashLogCell = (CrashLogCell) viewHolder.itemView;
                            File file = crashLogCell.getCrashLog();
                            if (file != null)
                                toDelete.add(file);
                        }
                    }
                    if (getContext() == null) {
                        OctoLogging.w(TAG, "Context is null, cannot show delete confirmation dialog");
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(getString(R.string.BuildAppName));
                    builder.setPositiveButton(getString(R.string.OK), (dialog, which) -> {
                        int unableToDeleteCount = 0;
                        for (File file : toDelete) {
                            if (file != null && !file.delete()) {
                                OctoLogging.e(TAG, "Could not delete file " + file);
                                unableToDeleteCount++;
                            }
                        }
                        selectedItems.clear();
                        updateRowsId();
                        notifyDataSetChanged();
                        actionBar.hideActionMode();

                        String message;
                        if (toDelete.size() > 1) {
                            message = formatString(R.string.CrashesDeleted, toDelete.size());
                        } else {
                            message = getString(R.string.CrashDeleted);
                        }
                        if (unableToDeleteCount > 0) {
                            message = formatString(R.string.CrashesUnableToDelete, unableToDeleteCount);
                            AlertDialog.Builder unableToDeleteBuilder = new AlertDialog.Builder(getContext());
                            unableToDeleteBuilder.setTitle(getString(R.string.BuildAppName));
                            unableToDeleteBuilder.setMessage(message);
                            unableToDeleteBuilder.setPositiveButton(getString(R.string.OK), (dialog1, which1) -> dialog1.dismiss());
                        }
                        BulletinFactory.of(OctoLogsActivity.this).createErrorBulletin(message).show();
                        OctoLogging.w(TAG, "Crash logs deleted. Message: " + message);
                    });
                    builder.setNegativeButton(getString(R.string.Cancel), (dialog, which) -> dialog.dismiss());

                    String message;
                    if (toDelete.size() > 1) {
                        message = formatString(R.string.CrashesDeleteConfirmation, toDelete.size());
                    } else {
                        message = getString(R.string.CrashDeleteConfirmation);
                    }
                    builder.setMessage(message);
                    showDialog(builder.create());
                    OctoLogging.d(TAG, "Delete confirmation dialog shown for selected crashes");
                    break;
                case MENU_DELETE_ALL:
                    if (getContext() == null) {
                        OctoLogging.w(TAG, "Context is null, cannot show delete all confirmation dialog");
                        return;
                    }
                    AlertDialog.Builder allBuilder = new AlertDialog.Builder(getContext());
                    allBuilder.setTitle(getString(R.string.BuildAppName));
                    allBuilder.setPositiveButton(getString(R.string.OK), (dialog, which) -> {
                        OctoLogging.deleteLogs();
                        updateRowsId();
                        notifyDataSetChanged();

                        File[] filesToDelete = archivedCrashFile;
                        String message1;
                        if (filesToDelete != null && filesToDelete.length > 1) {
                            message1 = formatString(R.string.CrashesDeleted, filesToDelete.length);
                        } else {
                            message1 = getString(R.string.CrashDeleted);
                        }
                        BulletinFactory.of(OctoLogsActivity.this).createErrorBulletin(message1).show();
                        OctoLogging.w(TAG, "All crash logs deleted. Message: " + message1);
                    });
                    allBuilder.setNegativeButton(getString(R.string.Cancel), (dialog, which) -> dialog.dismiss());

                    File[] filesToDeleteAll = archivedCrashFile;
                    String allMessage;
                    if (filesToDeleteAll != null && filesToDeleteAll.length > 1) {
                        allMessage = formatString(R.string.CrashesDeleteConfirmation, filesToDeleteAll.length);
                    } else {
                        allMessage = getString(R.string.CrashDeleteConfirmation);
                    }
                    allBuilder.setMessage(allMessage);
                    showDialog(allBuilder.create());
                    OctoLogging.d(TAG, "Delete all confirmation dialog shown");
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
            OctoLogging.d(TAG, "notifyDataSetChanged");
        }

        @Override
        public void notifyItemChanged(int position) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemChanged(position));
                return;
            }
            super.notifyItemChanged(position);
            OctoLogging.d(TAG, "notifyItemChanged at position: " + position);
        }

        @Override
        public void notifyItemChanged(int position, @Nullable Object payload) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemChanged(position, payload));
                return;
            }
            super.notifyItemChanged(position, payload);
            OctoLogging.d(TAG, "notifyItemChanged at position: " + position + " with payload: " + payload);
        }

        @Override
        public void notifyItemRangeChanged(int positionStart, int itemCount) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemRangeChanged(positionStart, itemCount));
                return;
            }
            super.notifyItemRangeChanged(positionStart, itemCount);
            OctoLogging.d(TAG, "notifyItemRangeChanged from: " + positionStart + ", count: " + itemCount);
        }

        @Override
        public void notifyItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemRangeChanged(positionStart, itemCount, payload));
                return;
            }
            super.notifyItemRangeChanged(positionStart, itemCount, payload);
            OctoLogging.d(TAG, "notifyItemRangeChanged from: " + positionStart + ", count: " + itemCount + " with payload: " + payload);
        }

        @Override
        public void notifyItemInserted(int position) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemInserted(position));
                return;
            }
            super.notifyItemInserted(position);
            OctoLogging.d(TAG, "notifyItemInserted at position: " + position);
        }

        @Override
        public void notifyItemMoved(int fromPosition, int toPosition) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemMoved(fromPosition, toPosition));
                return;
            }
            super.notifyItemMoved(fromPosition, toPosition);
            OctoLogging.d(TAG, "notifyItemMoved from: " + fromPosition + " to: " + toPosition);
        }

        @Override
        public void notifyItemRangeInserted(int positionStart, int itemCount) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemRangeInserted(positionStart, itemCount));
                return;
            }
            super.notifyItemRangeInserted(positionStart, itemCount);
            OctoLogging.d(TAG, "notifyItemRangeInserted from: " + positionStart + ", count: " + itemCount);
        }

        @Override
        public void notifyItemRangeRemoved(int positionStart, int itemCount) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemRangeRemoved(positionStart, itemCount));
                return;
            }
            super.notifyItemRangeRemoved(positionStart, itemCount);
            OctoLogging.d(TAG, "notifyItemRangeRemoved from: " + positionStart + ", count: " + itemCount);
        }

        @Override
        public void notifyItemRemoved(int position) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemRemoved(position));
                return;
            }
            super.notifyItemRemoved(position);
            OctoLogging.d(TAG, "notifyItemRemoved at position: " + position);
        }
    }


    private void initUpdateReceiver() {
        getNotificationCenter().addObserver(this, NotificationCenter.fileUploaded);
        getNotificationCenter().addObserver(this, NotificationCenter.fileUploadFailed);
        OctoLogging.d(TAG, "Update receiver initialized");
    }

    private void stopUpdateReceiver() {
        getNotificationCenter().removeObserver(this, NotificationCenter.fileUploaded);
        getNotificationCenter().removeObserver(this, NotificationCenter.fileUploadFailed);
        OctoLogging.d(TAG, "Update receiver stopped");
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        OctoLogging.d(TAG, "didReceivedNotification with id: " + id + ", account: " + account);
        if (id == NotificationCenter.fileUploaded) {
            String location = (String) args[0];
            TLRPC.InputFile inputFile = (TLRPC.InputFile) args[1];

            if (inputFile == null) {
                OctoLogging.w(TAG, "inputFile is null in fileUploaded notification");
                return;
            }

            if (!Objects.equals(location, sharingFullLocation)) {
                OctoLogging.w(TAG, "Uploaded file location does not match sharing location");
                return;
            }

            stopUpdateReceiver();

            AndroidUtilities.runOnUIThread(() -> BulletinFactory.of(this).createSimpleBulletin(R.raw.forward, getString(R.string.SendCrashLogDone)).show());
            OctoLogging.d(TAG, "Crash log sent successfully bulletin shown");

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
                OctoLogging.w(TAG, "Failed upload file location does not match sharing location");
                return;
            }

            stopUpdateReceiver();
            OctoLogging.w(TAG, "File upload failed for sharing crash log");
        }
    }
}
