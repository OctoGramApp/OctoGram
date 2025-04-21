/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui.components;

import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.collection.LongSparseArray;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.AlertDialog;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import it.octogram.android.OctoConfig;
import it.octogram.android.crashlytics.CrashOption;
import it.octogram.android.logs.OctoLogging;
import it.octogram.android.preferences.ui.custom.CrashLogCell;
import it.octogram.android.utils.OctoUtils;

/** @noinspection BooleanMethodIsAlwaysInverted*/
@SuppressLint({"NotifyDataSetChanged", "ViewConstructor"})
public class CrashManagementComponent extends FrameLayout implements NotificationCenter.NotificationCenterDelegate {

    public static final String REPORT_URL = "https://github.com/OctoGramApp/OctoGram/issues/new?assignees=&labels=bug&projects=&template=bug_report.yml&title=%5BBug%5D%3A+%3Ctitle-here%3E";
    private final Object PARTIAL_UPDATE_FLAG = new Object();
    private static final String TAG = "CrashManagementComponent";
    private final BaseFragment activity;

    private int settingsHeaderRow;
    private int copyLatestCrashRow;
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
    private CharSequence sharingFileLastModified;
    private LongSparseArray<TLRPC.Dialog> sharingDialogs;
    private File[] archivedCrashFiles;
    public List<File> sortedArchivedCrashFiles;

    private final int MENU_DELETE = 1;
    private final int MENU_DELETE_ALL = 2;

    private final ActionBar actionBar;

    public interface CrashManagementComponentDelegate {
        void onCrashActionFinished();
        BaseFragment getParentFragmentForDialogs();
        int getCurrentAccount();
        NotificationCenter getNotificationCenter();
        Theme.ResourcesProvider getResourceProvider();
        File[] getArchivedCrashFiles();
        File getLatestArchivedCrashFile();
        CharSequence getSystemInfo(boolean full, CharSequence lastModified) throws IllegalAccessException;
        File shareLog(File logFile) throws IOException;
        void deleteCrashLogs();
        @Nullable List<CrashOptionItem> getCrashOptionItems();
        @Nullable String getCrashMenuDialogTitle();
    }

    public interface CrashOptionItem {
        CharSequence getText();
        int getIconResId();
        CrashOption getCrashOption();
        void onClick(File file);
    }


    private final CrashManagementComponentDelegate delegate;

    public CrashManagementComponent(@NonNull Context context, BaseFragment activity, @NonNull ActionBar actionBar, @NonNull CrashManagementComponentDelegate delegate) {
        super(context);
        this.context = context;
        this.actionBar = actionBar;
        this.delegate = delegate;
        this.activity = activity;
        initializeComponent();
    }

    private void initializeComponent() {
        archivedCrashFiles = delegate.getArchivedCrashFiles();
        sortedArchivedCrashFiles = getSortedCrashFiles(archivedCrashFiles);
        updateRowsId();
        setupLayout();
        setupActionBar();
        initUpdateReceiver();
    }

    private List<File> getSortedCrashFiles(File[] files) {
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }
        return Arrays.stream(files)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .collect(Collectors.toList());
    }


    private void setupLayout() {
        setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);

        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        itemAnimator.setDelayAnimations(false);
        listView.setItemAnimator(itemAnimator);

        listAdapter = new ListAdapter(context);
        listView.setAdapter(listAdapter);
        addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener(this::onItemClick);
        listView.setOnItemLongClickListener((view, position) -> {
            if (position >= crashesStartRow && position < crashesEndRow) {
                listAdapter.toggleItemSelected(position);
                return true;
            }
            return false;
        });
    }

    private void setupActionBar() {
        if (archivedCrashFiles != null && archivedCrashFiles.length > 0) {
            ActionBarMenu actionNoSelectMenu = actionBar.createMenu();
            actionNoSelectMenu.addItem(MENU_DELETE_ALL, R.drawable.msg_delete);
        }

        ActionBarMenu actionMode = actionBar.createActionMode();
        selectedCountTextView = new NumberTextView(actionMode.getContext());
        selectedCountTextView.setTextSize(18);
        selectedCountTextView.setTypeface(AndroidUtilities.bold());
        selectedCountTextView.setTextColor(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon));
        actionMode.addView(selectedCountTextView, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f, 72, 0, 0, 0));
        actionMode.addItemWithWidth(MENU_DELETE, R.drawable.msg_delete, AndroidUtilities.dp(54));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                OctoLogging.d(TAG, "ActionBar item clicked: " + id);
                if (id == -1) {
                    activity.finishFragment();
                } else if (id == MENU_DELETE || id == MENU_DELETE_ALL) {
                    listAdapter.processMenuItem(id);
                }
            }
        });
    }


    public void showWarningDialog(String text) {
        if (context == null) {
            OctoLogging.w(TAG, "Context is null, cannot show warning dialog");
            return;
        }
        AlertDialog.Builder warningBuilder = new AlertDialog.Builder(context);
        warningBuilder.setTitle(getString(R.string.Warning));
        warningBuilder.setPositiveButton(getString(R.string.OK), (dialog1, which1) -> dialog1.dismiss());
        warningBuilder.setMessage(text);
        if (delegate != null && delegate.getParentFragmentForDialogs() != null) {
            delegate.getParentFragmentForDialogs().showDialog(warningBuilder.create());
        } else {
            warningBuilder.create().show();
        }
        OctoLogging.d(TAG, "Warning dialog shown: " + text);

    }

    private void copyTextToClipboard(CharSequence text) {
        if (AndroidUtilities.addToClipboard(text)) {
            BulletinFactory.of(delegate.getParentFragmentForDialogs()).createCopyBulletin(getString(R.string.CrashLogCopied)).show();
            OctoLogging.d(TAG, "Crash log copied to clipboard and bulletin shown");
        } else {
            OctoLogging.w(TAG, "Failed to copy text to clipboard");
        }
    }


    private void onItemClick(View view, int position, float x, float y) {
        OctoLogging.d(TAG, "onItemClick at position: " + position);
        if (position == copyLatestCrashRow) {
            File file = delegate.getLatestArchivedCrashFile();
            if (file != null) {
                copyCrashLogFile(file);
            } else {
                BulletinFactory.of(delegate.getParentFragmentForDialogs()).createErrorBulletin(getString(R.string.NoCrashLogFound)).show();
                OctoLogging.w(TAG, "No crash log file found for 'Copy Latest Crash Log'");
            }
        } else if (position >= crashesStartRow && position < crashesEndRow) {
            if (listAdapter.isItemSelected()) {
                listAdapter.toggleItemSelected(position);
            } else {
                if (delegate == null || delegate.getParentFragmentForDialogs() == null) {
                    OctoLogging.w(TAG, "Parent fragment is null, cannot show crash settings dialog");
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(delegate.getParentFragmentForDialogs().getParentActivity(), delegate.getResourceProvider());
                String dialogTitle = delegate.getCrashMenuDialogTitle();
                builder.setTitle(dialogTitle != null ? dialogTitle : "Logs");

                List<CrashOptionItem> customItems = delegate.getCrashOptionItems();
                CharSequence[] items;
                int[] icons;

                if (customItems != null && !customItems.isEmpty()) {
                    items = new CharSequence[customItems.size()];
                    icons = new int[customItems.size()];
                    for (int i = 0; i < customItems.size(); i++) {
                        items[i] = customItems.get(i).getText();
                        icons[i] = customItems.get(i).getIconResId();
                    }
                    builder.setItems(items, icons, (dialog, which) -> {
                        if (sortedArchivedCrashFiles == null) {
                            OctoLogging.w(TAG, "sortedArchivedCrashFiles is null, cannot process crash option");
                            return;
                        }
                        File file = sortedArchivedCrashFiles.get(position - crashesStartRow);
                        if (customItems.size() > which && which >= 0) {
                            customItems.get(which).onClick(file);
                        }
                    });

                } else {
                    items = new CharSequence[]{
                            getString(R.string.OpenCrashLog),
                            getString(R.string.SendCrashLog),
                            getString(R.string.CopyCrashLog),
                            getString(R.string.ReportCrash),
                    };
                    icons = new int[]{
                            R.drawable.msg_openin,
                            R.drawable.msg_send,
                            R.drawable.msg_copy,
                            R.drawable.msg_report,
                    };
                    builder.setItems(items, icons, (dialog, which) -> {
                        if (sortedArchivedCrashFiles == null) {
                            OctoLogging.w(TAG, "sortedArchivedCrashFiles is null, cannot process crash option");
                            return;
                        }
                        File file = sortedArchivedCrashFiles.get(position - crashesStartRow);
                        switch (CrashOption.fromValue(which)) {
                            case CrashOption.OPEN_LOG -> {
                                if (file != null && openCrashLogFile(file)) {
                                    showWarningDialog(getString(R.string.ErrorSendingCrashContent));
                                }
                            }
                            case CrashOption.SEND_LOG -> {
                                if (file != null && !sendCrashLogFile(file)) {
                                    showWarningDialog(getString(R.string.ErrorSendingCrashContent));
                                }
                            }
                            case CrashOption.COPY_CRASH_LINE -> copyCrashLogContent(position);
                            case CrashOption.OPEN_REPORT_URL -> {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Utilities.uriParseSafe(REPORT_URL));
                                if (context != null)
                                    context.startActivity(browserIntent);
                            }
                        }
                    });
                }


                builder.setNegativeButton(getString(R.string.Cancel), null);
                delegate.getParentFragmentForDialogs().showDialog(builder.create());
                OctoLogging.d(TAG, "Crash settings dialog shown for position: " + position);
            }
        }
    }

    public CrashManagementComponent.CrashOptionItem createItem(
            @StringRes int labelRes,
            @DrawableRes int iconRes,
            CrashOption option,
            Consumer<File> onClickAction
    ) {
        return new CrashManagementComponent.CrashOptionItem() {
            @Override
            public CharSequence getText() {
                return getString(labelRes);
            }

            @Override
            public int getIconResId() {
                return iconRes;
            }

            @Override
            public CrashOption getCrashOption() {
                return option;
            }

            @Override
            public void onClick(File file) {
                onClickAction.accept(file);
            }
        };
    }


    private void copyCrashLogContent(int position) {
        if (sortedArchivedCrashFiles == null || position < crashesStartRow || position >= crashesEndRow) {
            OctoLogging.w(TAG, "Invalid position or sortedArchivedCrashFiles is null for copyCrashLine: " + position);
            return;
        }
        File file = sortedArchivedCrashFiles.get(position - crashesStartRow);
        copyCrashLogFile(file);
    }

    public void copyCrashLogFile(File file) {
        if (file == null) {
            OctoLogging.w(TAG, "File is null, cannot copy crash line");
            return;
        }
        StringBuilder content = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            OctoLogging.e(TAG, "IO error reading crash file for copy", e);
            BulletinFactory.of(delegate.getParentFragmentForDialogs()).createErrorBulletin(getString(R.string.CouldNotCopyFile)).show();
        }

        copyTextToClipboard(content.toString());
    }

    protected void updateRowsId() {
        OctoLogging.d(TAG, "updateRowsId");
        rowCount = 0;
        crashesHeaderRow = -1;
        crashesEndRow = -1;

        settingsHeaderRow = rowCount++;
        copyLatestCrashRow = rowCount++;
        settingsShadowRow = rowCount++;

        if (sortedArchivedCrashFiles != null && !sortedArchivedCrashFiles.isEmpty()) {
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

    public boolean onBackPressed() {
        OctoLogging.d(TAG, "onBackPressed");
        if (listAdapter.isItemSelected()) {
            listAdapter.clearSelection();
            return false;
        }
        return true;
    }

    public boolean openCrashLogFile(File file) {
        if (file == null) {
            OctoLogging.w(TAG, "File is null, cannot open log");
            return true;
        }
        try {
            File cacheFile = delegate.shareLog(file.getAbsoluteFile());
            if (delegate.getParentFragmentForDialogs() != null && delegate.getParentFragmentForDialogs().getParentActivity() != null)
                AndroidUtilities.openForView(cacheFile, cacheFile.getName(), "text/plain", delegate.getParentFragmentForDialogs().getParentActivity(), delegate.getResourceProvider(), false);
            OctoLogging.d(TAG, "Crash log opened for viewing: " + file.getName());
            return false;
        } catch (IOException e) {
            OctoLogging.e(TAG, "IO error opening crash log", e);
            return true;
        }
    }

    public boolean sendCrashLogFile(File file) {
        if (file == null || delegate == null || delegate.getParentFragmentForDialogs() == null || delegate.getParentFragmentForDialogs().getParentActivity() == null) {
            OctoLogging.w(TAG, "File or Parent activity is null, cannot send log");
            return false;
        }
        try {
            File cacheFile = delegate.shareLog(file);

            ShareAlert shareAlert = new ShareAlert(activity.getParentActivity(), null, null, false, null, false, true) {

                @Override
                protected void onSend(LongSparseArray<TLRPC.Dialog> didS, int count, TLRPC.TL_forumTopic topic, boolean showToast) {
                    sharingFileName = cacheFile.getName();
                    sharingFullLocation = cacheFile.getAbsolutePath();
                    sharingFileLastModified = cacheFile.getName().replace("_", " ").replace(".log", "").trim();
                    sharingDialogs = didS;

                    FileLoader instance = FileLoader.getInstance(getCurrentAccount());

                    initUpdateReceiver();
                    instance.uploadFile(cacheFile.getPath(), false, true, ConnectionsManager.FileTypeFile);
                    OctoLogging.d(TAG, "File upload initiated for sharing crash log: " + sharingFileName);
                }
            };
            shareAlert.show();
            OctoLogging.d(TAG, "Share alert shown for sending crash log");

            return true;
        } catch (IOException e) {
            OctoLogging.e(TAG, "IO error preparing crash log for sending", e);
            return false;
        }
    }

    /** @noinspection SequencedCollectionMethodCanBeUsed*/
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
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case TYPE_SETTINGS_HEADER:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case TYPE_BUTTON:
                    view = new TextCell(context, 23, false, false, delegate.getResourceProvider());
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
            Object payload = payloads.isEmpty() ? null : payloads.get(0);
            boolean partialUpdate = PARTIAL_UPDATE_FLAG.equals(payload);

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
                    if (position == copyLatestCrashRow) {
                        textCell.setTextAndIcon(getString(R.string.CopyLatestCrashLog), R.drawable.msg_copy, false);
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
                            crashLogCell.setSelected(selectedItems.get(position, false), partialUpdate);
                        }
                    }
                    break;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            OctoLogging.d(TAG, "onBindViewHolder at position: " + position);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == copyLatestCrashRow) {
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

        public void toggleItemSelected(int position) {
            selectedItems.put(position, !selectedItems.get(position, false));
            notifyItemRangeChanged(crashesStartRow, crashesEndRow - crashesStartRow, PARTIAL_UPDATE_FLAG);
            updateActionMode();
            OctoLogging.d(TAG, "Item at position " + position + " selection toggled. Selected: " + selectedItems.get(position, false));
        }

        public boolean isItemSelected() {
            return selectedItems.indexOfValue(true) != -1;
        }

        public void clearSelection() {
            selectedItems.clear();
            notifyItemRangeChanged(crashesStartRow, crashesEndRow - crashesStartRow, PARTIAL_UPDATE_FLAG);
            updateActionMode();
            OctoLogging.d(TAG, "Selection cleared");
        }

        private void updateActionMode() {
            int selectedCount = getSelectedItemCount();
            boolean actionModeVisible = actionBar.isActionModeShowed();
            if (selectedCount > 0) {
                selectedCountTextView.setNumber(selectedCount, actionModeVisible);
                if (!actionModeVisible) {
                    actionBar.showActionMode();
                    OctoLogging.d(TAG, "Action mode shown with selected count: " + selectedCount);
                }
            } else if (actionModeVisible) {
                actionBar.hideActionMode();
                OctoLogging.d(TAG, "Action mode hidden");
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        private void processMenuItem(int id) {
            OctoLogging.d(TAG, "processSelectionMenu with id: " + id);
            switch (id) {
                case MENU_DELETE:
                    List<File> filesToDelete = new ArrayList<>();
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
                                filesToDelete.add(file);
                        }
                    }
                    if (context == null) {
                        OctoLogging.w(TAG, "Context is null, cannot show delete confirmation dialog");
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(getString(R.string.BuildAppName));
                    builder.setPositiveButton(getString(R.string.OK), (dialog, which) -> {
                        int deletionFailedCount = 0;
                        for (File file : filesToDelete) {
                            if (file != null && !file.delete()) {
                                OctoLogging.e(TAG, "Could not delete file " + file);
                                deletionFailedCount++;
                            }
                        }
                        selectedItems.clear();
                        updateRowsId();
                        notifyDataSetChanged();
                        actionBar.hideActionMode();

                        String message;
                        if (filesToDelete.size() > 1) {
                            message = formatString(R.string.CrashesDeleted, filesToDelete.size());
                        } else {
                            message = getString(R.string.CrashDeleted);
                        }
                        if (deletionFailedCount > 0) {
                            message = formatString(R.string.CrashesUnableToDelete, deletionFailedCount);
                            AlertDialog.Builder unableToDeleteBuilder = new AlertDialog.Builder(context);
                            unableToDeleteBuilder.setTitle(getString(R.string.BuildAppName));
                            unableToDeleteBuilder.setMessage(message);
                            unableToDeleteBuilder.setPositiveButton(getString(R.string.OK), (dialog1, which1) -> dialog1.dismiss());
                        }
                        BulletinFactory.of(delegate.getParentFragmentForDialogs()).createErrorBulletin(message).show();
                        OctoLogging.w(TAG, "Crash logs deleted. Message: " + message);
                        delegate.onCrashActionFinished();
                    });
                    builder.setNegativeButton(getString(R.string.Cancel), (dialog, which) -> dialog.dismiss());

                    String confirmationMessage;
                    if (filesToDelete.size() > 1) {
                        confirmationMessage = formatString(R.string.CrashesDeleteConfirmation, filesToDelete.size());
                    } else {
                        confirmationMessage = getString(R.string.CrashDeleteConfirmation);
                    }
                    builder.setMessage(confirmationMessage);
                    if (delegate != null && delegate.getParentFragmentForDialogs() != null) {
                        delegate.getParentFragmentForDialogs().showDialog(builder.create());
                    } else {
                        builder.create().show();
                    }
                    OctoLogging.d(TAG, "Delete confirmation dialog shown for selected crashes");
                    break;
                case MENU_DELETE_ALL:
                    if (context == null) {
                        OctoLogging.w(TAG, "Context is null, cannot show delete all confirmation dialog");
                        return;
                    }
                    AlertDialog.Builder allBuilder = new AlertDialog.Builder(context);
                    allBuilder.setTitle(getString(R.string.BuildAppName));
                    allBuilder.setPositiveButton(getString(R.string.OK), (dialog, which) -> {
                        delegate.deleteCrashLogs();
                        archivedCrashFiles = delegate.getArchivedCrashFiles();
                        sortedArchivedCrashFiles = getSortedCrashFiles(archivedCrashFiles);
                        updateRowsId();
                        notifyDataSetChanged();

                        File[] filesToDeleteAll = archivedCrashFiles;
                        String message1;
                        if (filesToDeleteAll != null && filesToDeleteAll.length > 1) {
                            message1 = formatString(R.string.CrashesDeleted, filesToDeleteAll.length);
                        } else {
                            message1 = getString(R.string.CrashDeleted);
                        }
                        BulletinFactory.of(delegate.getParentFragmentForDialogs()).createErrorBulletin(message1).show();
                        OctoLogging.w(TAG, "All crash logs deleted. Message: " + message1);
                        delegate.onCrashActionFinished();
                    });
                    allBuilder.setNegativeButton(getString(R.string.Cancel), (dialog, which) -> dialog.dismiss());

                    File[] filesToDeleteAllConfirmation = archivedCrashFiles;
                    String allMessage;
                    if (filesToDeleteAllConfirmation != null && filesToDeleteAllConfirmation.length > 1) {
                        allMessage = formatString(R.string.CrashesDeleteConfirmation, filesToDeleteAllConfirmation.length);
                    } else {
                        allMessage = getString(R.string.CrashDeleteConfirmation);
                    }
                    allBuilder.setMessage(allMessage);
                    if (delegate != null && delegate.getParentFragmentForDialogs() != null) {
                        delegate.getParentFragmentForDialogs().showDialog(allBuilder.create());
                    } else {
                        allBuilder.create().show();
                    }
                    OctoLogging.d(TAG, "Delete all confirmation dialog shown");
                    break;
            }
        }

        public int getSelectedItemCount() {
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
        delegate.getNotificationCenter().addObserver(this, NotificationCenter.fileUploaded);
        delegate.getNotificationCenter().addObserver(this, NotificationCenter.fileUploadFailed);
        OctoLogging.d(TAG, "Update receiver initialized");
    }

    private void stopUpdateReceiver() {
        delegate.getNotificationCenter().removeObserver(this, NotificationCenter.fileUploaded);
        delegate.getNotificationCenter().removeObserver(this, NotificationCenter.fileUploadFailed);
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

            BulletinFactory.of(activity).createSimpleBulletin(R.raw.forward, getString(R.string.SendCrashLogDone)).show();
            OctoLogging.d(TAG, "Crash log sent successfully bulletin shown");

            TLRPC.TL_documentAttributeFilename attr = new TLRPC.TL_documentAttributeFilename();
            attr.file_name = sharingFileName;

            TLRPC.TL_inputMediaUploadedDocument inputMediaDocument = new TLRPC.TL_inputMediaUploadedDocument();
            inputMediaDocument.file = inputFile;
            inputMediaDocument.attributes.add(attr);
            inputMediaDocument.mime_type = OctoConfig.CRASH_MIME_TYPE;

            for (int i = 0; i < sharingDialogs.size(); i++) {
                TLRPC.TL_messages_sendMedia req = new TLRPC.TL_messages_sendMedia();
                req.peer = MessagesController.getInstance(delegate.getCurrentAccount()).getInputPeer(sharingDialogs.keyAt(i));
                req.random_id = SendMessagesHelper.getInstance(delegate.getCurrentAccount()).getNextRandomId();
                try {
                    req.message = OctoUtils.safeToString(delegate.getSystemInfo(false, sharingFileLastModified));
                } catch (IllegalAccessException e) {
                    req.message = "";
                    OctoLogging.w(TAG, "IllegalAccessException getting system info for message", e);
                }
                req.silent = true;
                req.media = inputMediaDocument;
                ConnectionsManager.getInstance(delegate.getCurrentAccount()).sendRequest(req, null);
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