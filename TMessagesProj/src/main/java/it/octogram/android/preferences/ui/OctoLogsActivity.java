/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui;

import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.Utilities;
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
import it.octogram.android.crashlytics.CrashViewType;
import it.octogram.android.crashlytics.Crashlytics;
import it.octogram.android.logs.OctoLogging;
import it.octogram.android.preferences.ui.custom.CrashLogCell;

@SuppressWarnings("rawtypes")
@SuppressLint({"NotifyDataSetChanged", "ViewConstructor"})
public class OctoLogsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {
    private final Object PARTIAL_UPDATE_FLAG = new Object();
    public static final String REPORT_URL = "https://github.com/OctoGramApp/OctoGram/issues/new?assignees=&labels=bug&projects=&template=bug_report.yml&title=%5BBug%5D%3A+%3Ctitle-here%3E";
    private static final String TAG = "OctoLogsActivity";

    private int settingsHeaderRow;
    private int copySystemInfoRow;
    private int copyLatestLogRow;
    private int settingsShadowRow;
    private int logsHeaderRow;
    private int logsStartRow;
    private int logsEndRow;
    private int logsInfoRow;

    protected int rowCount;
    protected Context context;
    protected RecyclerListView listView;
    private ListAdapter listAdapter;
    private NumberTextView selectedCountTextView;

    private String sharingFullLocation;
    private String sharingFileName;
    private LongSparseArray<TLRPC.Dialog> sharingDialogs;

    private File[] archivedLogFiles;
    public List<File> sortedArchivedLogFiles;

    private final int MENU_DELETE = 1;
    private final int MENU_DELETE_ALL = 2;

    private final CrashViewType currentViewType;

    public OctoLogsActivity(CrashViewType type) {
        super();
        this.currentViewType = type;
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        loadLogFiles();
        updateRowsId();
        initUpdateReceiver();
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        stopUpdateReceiver();
    }

    private void loadLogFiles() {
        switch (currentViewType) {
            case CRASH_LOGS:
                archivedLogFiles = Crashlytics.getArchivedCrashFiles();
                break;
            case DEBUG_LOGS:
                archivedLogFiles = OctoLogging.getLogFiles();
                break;
        }
        sortedArchivedLogFiles = getSortedLogFiles(archivedLogFiles);
    }

    @Override
    public View createView(Context context) {
        this.context = context;

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        switch (currentViewType) {
            case CRASH_LOGS:
                actionBar.setTitle(getString(R.string.CrashHistory));
                break;
            case DEBUG_LOGS:
                actionBar.setTitle(getString(R.string.DebugHistory));
                break;
        }
        actionBar.setAllowOverlayTitle(true);
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setBackButtonDrawable(new BackDrawable(false));

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
            if (position >= logsStartRow && position < logsEndRow) {
                listAdapter.toggleItemSelected(position);
                return true;
            }
            return false;
        });

        if (archivedLogFiles != null && archivedLogFiles.length > 0) {
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
                OctoLogging.d(TAG, "ActionBar item clicked: " + id);
                if (id == -1) {
                    if (onBackPressed()) {
                        finishFragment();
                    }
                } else if (id == MENU_DELETE || id == MENU_DELETE_ALL) {
                    listAdapter.processMenuItem(id);
                }
            }
        });

        return fragmentView;
    }

    private List<File> getSortedLogFiles(File[] files) {
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }
        return Arrays.stream(files)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .collect(Collectors.toList());
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
        showDialog(warningBuilder.create());
        OctoLogging.d(TAG, "Warning dialog shown: " + text);
    }

    private void copyTextToClipboard(CharSequence text) {
        if (AndroidUtilities.addToClipboard(text)) {
            String bulletinText = (currentViewType == CrashViewType.CRASH_LOGS) ?
                    getString(R.string.CrashLogCopied) :
                    getString(R.string.DebugLogCopied);
            BulletinFactory.of(this).createCopyBulletin(bulletinText).show();
            OctoLogging.d(TAG, "Log copied to clipboard and bulletin shown for type: " + currentViewType);
        } else {
            OctoLogging.w(TAG, "Failed to copy text to clipboard for type: " + currentViewType);
        }
    }

    private void onItemClick(View view, int position, float x, float y) {
        OctoLogging.d(TAG, "onItemClick at position: " + position + " for type: " + currentViewType);
        if (listView == null) {
            OctoLogging.w(TAG, "Views not initialized yet in onItemClick");
            return;
        }

        if (position == copySystemInfoRow && currentViewType == CrashViewType.DEBUG_LOGS) {
            try {
                String configuration = Crashlytics.getSystemInfo(false);
                copyTextToClipboard(configuration);
            } catch (IllegalAccessException e) {
                OctoLogging.e(TAG, "Error getting system info", e);
                BulletinFactory.of(this).createErrorBulletin(getString(R.string.ErrorOccurred)).show();
            }
        } else if (position == copyLatestLogRow) {
            File file = null;
            switch (currentViewType) {
                case CRASH_LOGS:
                    file = Crashlytics.getLatestArchivedCrashFile();
                    break;
                case DEBUG_LOGS:
                    if (sortedArchivedLogFiles != null && !sortedArchivedLogFiles.isEmpty()) {
                        file = sortedArchivedLogFiles.get(0);
                    }
                    break;
            }

            if (file != null) {
                copyLogFileContent(file);
            } else {
                String noLogFoundString = (currentViewType == CrashViewType.CRASH_LOGS) ?
                        getString(R.string.NoCrashLogFound) :
                        getString(R.string.NoDebugLogFound);
                BulletinFactory.of(this).createErrorBulletin(noLogFoundString).show();
                OctoLogging.w(TAG, "No log file found for 'Copy Latest Log' for type: " + currentViewType);
            }
        } else if (position >= logsStartRow && position < logsEndRow) {
            if (listAdapter.isItemSelected()) {
                listAdapter.toggleItemSelected(position);
            } else {
                if (getParentActivity() == null) {
                    OctoLogging.w(TAG, "Parent activity is null, cannot show log options dialog");
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), getResourceProvider());
                builder.setTitle("Log Options");

                List<CharSequence> items = new ArrayList<>();
                List<Integer> icons = new ArrayList<>();
                List<CrashOption> options = new ArrayList<>();

                items.add(getString((currentViewType == CrashViewType.CRASH_LOGS) ?
                        R.string.OpenCrashLog : R.string.OpenDebugLog));
                icons.add(R.drawable.msg_openin);
                options.add(CrashOption.OPEN_LOG);

                items.add(getString((currentViewType == CrashViewType.CRASH_LOGS) ?
                        R.string.SendCrashLog : R.string.SendDebugLog));
                icons.add(R.drawable.msg_send);
                options.add(CrashOption.SEND_LOG);

                items.add(getString((currentViewType == CrashViewType.CRASH_LOGS) ?
                        R.string.CopyCrashLog : R.string.CopyDebugLog));
                icons.add(R.drawable.msg_copy);
                options.add(CrashOption.COPY_CRASH_LINE);

                if (currentViewType == CrashViewType.CRASH_LOGS) {
                    items.add(getString(R.string.ReportCrash));
                    icons.add(R.drawable.msg_report);
                    options.add(CrashOption.OPEN_REPORT_URL);
                }

                builder.setItems(items.toArray(new CharSequence[0]), icons.stream().mapToInt(i -> i).toArray(), (dialog, which) -> {
                    if (sortedArchivedLogFiles == null || position - logsStartRow < 0 || position - logsStartRow >= sortedArchivedLogFiles.size()) {
                        OctoLogging.w(TAG, "Invalid position or sortedArchivedLogFiles is null for log option: " + position + " for type: " + currentViewType);
                        return;
                    }
                    File file = sortedArchivedLogFiles.get(position - logsStartRow);
                    if (which < 0 || which >= options.size()) {
                        OctoLogging.w(TAG, "Invalid option index: " + which + " for type: " + currentViewType);
                        return;
                    }
                    CrashOption selectedOption = options.get(which);

                    switch (selectedOption) {
                        case CrashOption.OPEN_LOG -> {
                            if (file != null && openLogFile(file)) {
                                showWarningDialog("Error opening log file");
                            }
                        }
                        case CrashOption.SEND_LOG -> {
                            if (file != null && !sendLogFile(file)) {
                                showWarningDialog("Error sending log file");
                            }
                        }
                        case CrashOption.COPY_CRASH_LINE -> copyLogFileContent(file);
                        case CrashOption.OPEN_REPORT_URL -> {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Utilities.uriParseSafe(REPORT_URL));
                            if (context != null)
                                context.startActivity(browserIntent);
                        }
                    }
                });

                builder.setNegativeButton(getString(R.string.Cancel), null);
                showDialog(builder.create());
                OctoLogging.d(TAG, "Log options dialog shown for position: " + position + " for type: " + currentViewType);
            }
        }
    }

    public void copyLogFileContent(File file) {
        if (file == null) {
            OctoLogging.w(TAG, "File is null, cannot copy log content for type: " + currentViewType);
            return;
        }
        StringBuilder content = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            OctoLogging.e(TAG, "IO error reading log file for copy for type: " + currentViewType, e);
            String errorString = (currentViewType == CrashViewType.CRASH_LOGS) ?
                    getString(R.string.CouldNotCopyFile) :
                    getString(R.string.CouldNotCopyDebugFile);
            BulletinFactory.of(this).createErrorBulletin(errorString).show();
            return;
        }

        copyTextToClipboard(content.toString());
    }

    protected void updateRowsId() {
        OctoLogging.d(TAG, "updateRowsId for type: " + currentViewType);
        rowCount = 0;

        settingsHeaderRow = -1;
        copySystemInfoRow = -1;
        copyLatestLogRow = -1;
        settingsShadowRow = -1;
        logsHeaderRow = -1;
        logsStartRow = -1;
        logsEndRow = -1;
        logsInfoRow = -1;

        settingsHeaderRow = rowCount++;

        if (currentViewType == CrashViewType.CRASH_LOGS) {
            copyLatestLogRow = rowCount++;
        } else if (currentViewType == CrashViewType.DEBUG_LOGS) {
            copySystemInfoRow = rowCount++;
        }

        logsInfoRow = rowCount++;

        if (sortedArchivedLogFiles != null && !sortedArchivedLogFiles.isEmpty()) {
            logsHeaderRow = rowCount++;
            logsStartRow = rowCount;
            for (int i = 0; i < sortedArchivedLogFiles.size(); i++) {
                rowCount++;
            }
            logsEndRow = rowCount;
        }

        settingsShadowRow = rowCount++;

        OctoLogging.d(TAG, "Rows IDs updated. Row count: " + rowCount + ", Logs end row: " + logsEndRow + " for type: " + currentViewType);
    }

    @Override
    public boolean onBackPressed() {
        OctoLogging.d(TAG, "onBackPressed for type: " + currentViewType);
        if (listAdapter != null && listAdapter.isItemSelected()) {
            listAdapter.clearSelection();
            return false;
        }
        return super.onBackPressed();
    }

    public boolean openLogFile(File file) {
        if (file == null) {
            OctoLogging.w(TAG, "File is null, cannot open log for type: " + currentViewType);
            return false;
        }
        try {
            File cacheFile = Crashlytics.shareLog(file.getAbsoluteFile());
            if (getParentActivity() != null) {
                boolean openFailed = AndroidUtilities.openForView(cacheFile, cacheFile.getName(), "text/plain", getParentActivity(), getResourceProvider(), false);
                if (openFailed) {
                    OctoLogging.e(TAG, "Failed to open log file using openForView: " + file.getName() + " for type: " + currentViewType);
                    return true;
                }
            }
            OctoLogging.d(TAG, "Log opened for viewing: " + file.getName() + " for type: " + currentViewType);
            return false;
        } catch (IOException e) {
            OctoLogging.e(TAG, "IO error opening log for type: " + currentViewType, e);
            return true;
        }
    }

    public boolean sendLogFile(File file) {
        if (file == null || getParentActivity() == null) {
            OctoLogging.w(TAG, "File or Parent activity is null, cannot send log for type: " + currentViewType);
            return false;
        }
        try {
            File cacheFile = Crashlytics.shareLog(file);

            ShareAlert shareAlert = new ShareAlert(getParentActivity(), null, null, false, null, false, true) {
                @Override
                protected void onSend(LongSparseArray<TLRPC.Dialog> didS, int count, TLRPC.TL_forumTopic topic, boolean showToast) {
                    sharingFileName = cacheFile.getName();
                    sharingFullLocation = cacheFile.getAbsolutePath();
                    sharingDialogs = didS;

                    FileLoader instance = FileLoader.getInstance(getCurrentAccount());

                    instance.uploadFile(cacheFile.getPath(), false, true, ConnectionsManager.FileTypeFile);
                    OctoLogging.d(TAG, "File upload initiated for sharing log: " + sharingFileName + " for type: " + currentViewType);
                }
            };
            shareAlert.show();
            OctoLogging.d(TAG, "Share alert shown for sending log for type: " + currentViewType);

            return true;
        } catch (IOException e) {
            OctoLogging.e(TAG, "IO error preparing log for sending for type: " + currentViewType, e);
            return false;
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private final Context mContext;
        private final SparseBooleanArray selectedItems = new SparseBooleanArray();

        private final int TYPE_SETTINGS_HEADER = 0;
        private final int TYPE_BUTTON = 1;
        private final int TYPE_SHADOW = 2;
        private final int TYPE_LOG_FILE = 3;
        private final int TEXT_HINT_WITH_PADDING = 4;

        public ListAdapter(Context context) {
            mContext = context;
            OctoLogging.d(TAG, "ListAdapter created for type: " + currentViewType);
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
                case TYPE_LOG_FILE:
                    view = new CrashLogCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case TEXT_HINT_WITH_PADDING:
                    view = new TextInfoPrivacyCell(context);
                    break;
            }
            if (view == null) {
                OctoLogging.e(TAG, "onCreateViewHolder: Invalid viewType " + viewType + " for type: " + currentViewType);
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
                        if (currentViewType == CrashViewType.DEBUG_LOGS) {
                            headerCell.setText(getString(R.string.Info));
                        } else {
                            headerCell.setText(getString(R.string.Settings));
                        }
                    } else if (position == logsHeaderRow) {
                        switch (currentViewType) {
                            case CRASH_LOGS:
                                headerCell.setText(getString(R.string.CrashHistory));
                                break;
                            case DEBUG_LOGS:
                                headerCell.setText(getString(R.string.DebugHistory));
                                break;
                        }
                    }
                    break;
                case TYPE_BUTTON:
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == copySystemInfoRow && currentViewType == CrashViewType.DEBUG_LOGS) {
                        textCell.setTextAndIcon(getString(R.string.CopySystemInfo), R.drawable.msg_copy, true);
                    } else if (position == copyLatestLogRow) {
                        textCell.setTextAndIcon(getString(R.string.CopyLatestCrashLog), R.drawable.msg_copy, false);
                    }
                    break;
                case TEXT_HINT_WITH_PADDING:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == logsInfoRow) {
                        int formatStringResId = (currentViewType == CrashViewType.CRASH_LOGS) ?
                                R.string.CrashLogInfo :
                                R.string.DebugLogInfo;
                        String formattedText = getString(formatStringResId);
                        textInfoPrivacyCell.setText(formattedText);
                    }
                    break;
                case TYPE_LOG_FILE:
                    if (position >= logsStartRow && position < logsEndRow) {
                        CrashLogCell crashLogCell = (CrashLogCell) holder.itemView;

                        if (sortedArchivedLogFiles != null && position - logsStartRow < sortedArchivedLogFiles.size()) {
                            File file = sortedArchivedLogFiles.get(position - logsStartRow);
                            crashLogCell.setData(file, currentViewType, true);
                            crashLogCell.setSelected(selectedItems.get(position, false), partialUpdate);
                        } else {
                            OctoLogging.w(TAG, "onBindViewHolder: Invalid index for sortedArchivedLogFiles at position " + position + " for type: " + currentViewType);
                        }
                    }
                    break;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
            onBindViewHolder(holder, position, new ArrayList<>());
        }

        @Override
        public int getItemViewType(int position) {
            if (position == copySystemInfoRow || position == copyLatestLogRow) {
                return TYPE_BUTTON;
            } else if (position >= logsStartRow && position < logsEndRow) {
                return TYPE_LOG_FILE;
            } else if (position == settingsHeaderRow || position == logsHeaderRow) {
                return TYPE_SETTINGS_HEADER;
            } else if (position == settingsShadowRow) {
                return TYPE_SHADOW;
            } else if (position == logsInfoRow) {
                return TEXT_HINT_WITH_PADDING;
            }
            OctoLogging.e(TAG, "getItemViewType: Invalid position " + position + " for type: " + currentViewType);
            throw new IllegalArgumentException("Invalid position " + position);
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            int viewType = holder.getItemViewType();
            boolean isButtonClickable = (viewType == TYPE_BUTTON &&
                    ((position == copySystemInfoRow && currentViewType == CrashViewType.DEBUG_LOGS) ||
                            (position == copyLatestLogRow)));
            boolean isLogFileClickable = viewType == TYPE_LOG_FILE;

            return isButtonClickable || isLogFileClickable;
        }


        public void toggleItemSelected(int position) {
            selectedItems.put(position, !selectedItems.get(position, false));
            notifyItemRangeChanged(logsStartRow, logsEndRow - logsStartRow, PARTIAL_UPDATE_FLAG);
            updateActionMode();
            OctoLogging.d(TAG, "Item at position " + position + " selection toggled. Selected: " + selectedItems.get(position, false) + " for type: " + currentViewType);
        }

        public boolean isItemSelected() {
            return selectedItems.indexOfValue(true) != -1;
        }

        public void clearSelection() {
            selectedItems.clear();
            notifyItemRangeChanged(logsStartRow, logsEndRow - logsStartRow, PARTIAL_UPDATE_FLAG);
            updateActionMode();
            OctoLogging.d(TAG, "Selection cleared for type: " + currentViewType);
        }

        private void updateActionMode() {
            int selectedCount = getSelectedItemCount();
            boolean actionModeVisible = actionBar.isActionModeShowed();
            if (selectedCount > 0) {
                selectedCountTextView.setNumber(selectedCount, actionModeVisible);
                if (!actionModeVisible) {
                    actionBar.showActionMode();
                    OctoLogging.d(TAG, "Action mode shown with selected count: " + selectedCount + " for type: " + currentViewType);
                }
            } else if (actionModeVisible) {
                actionBar.hideActionMode();
                OctoLogging.d(TAG, "Action mode hidden for type: " + currentViewType);
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        private void processMenuItem(int id) {
            OctoLogging.d(TAG, "processMenuItem with id: " + id + " for type: " + currentViewType);
            switch (id) {
                case MENU_DELETE:
                    List<File> filesToDelete = new ArrayList<>();
                    for (int i = 0; i < selectedItems.size(); i++) {
                        if (selectedItems.valueAt(i)) {
                            int position = selectedItems.keyAt(i);
                            if (position >= logsStartRow && position < logsEndRow) {
                                int fileIndex = position - logsStartRow;
                                if (fileIndex < sortedArchivedLogFiles.size()) {
                                    File file = sortedArchivedLogFiles.get(fileIndex);
                                    if (file != null)
                                        filesToDelete.add(file);
                                } else {
                                    OctoLogging.w(TAG, "Invalid file index " + fileIndex + " for position " + position + " in selectedItems for type: " + currentViewType);
                                }
                            } else {
                                OctoLogging.w(TAG, "Selected position " + position + " is outside logs list range for type: " + currentViewType);
                            }
                        }
                    }

                    if (filesToDelete.isEmpty()) {
                        String noLogsSelectedMsg = (currentViewType == CrashViewType.CRASH_LOGS) ?
                                getString(R.string.NoLogsSelectedForDeletion) :
                                getString(R.string.NoDebugLogsSelectedForDeletion);
                        BulletinFactory.of(OctoLogsActivity.this).createErrorBulletin(noLogsSelectedMsg).show();
                        clearSelection();
                        return;
                    }

                    if (context == null) {
                        OctoLogging.w(TAG, "Context is null, cannot show delete confirmation dialog for type: " + currentViewType);
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(getString(R.string.BuildAppName));
                    builder.setPositiveButton(getString(R.string.OK), (dialog, which) -> {
                        int deletionFailedCount = 0;
                        for (File file : filesToDelete) {
                            if (file != null) {
                                if (!file.delete()) {
                                    FileLog.e("Could not delete file " + file.getAbsolutePath());
                                    deletionFailedCount++;
                                } else {
                                    OctoLogging.d(TAG, "Deleted log file: " + file.getName() + " for type: " + currentViewType);
                                }
                            }
                        }
                        loadLogFiles();
                        selectedItems.clear();
                        updateRowsId();
                        notifyDataSetChanged();
                        actionBar.hideActionMode();

                        String message;
                        int count = filesToDelete.size();

                        if (deletionFailedCount > 0) {
                            message = (currentViewType == CrashViewType.CRASH_LOGS) ?
                                    formatString(R.string.CrashesUnableToDelete, deletionFailedCount) :
                                    formatString(R.string.DebugsUnableToDelete, deletionFailedCount);
                            AlertDialog.Builder unableToDeleteBuilder = new AlertDialog.Builder(context);
                            unableToDeleteBuilder.setTitle(getString(R.string.BuildAppName));
                            unableToDeleteBuilder.setMessage(message);
                            unableToDeleteBuilder.setPositiveButton(getString(R.string.OK), (dialog1, which1) -> dialog1.dismiss());
                            showDialog(unableToDeleteBuilder.create());
                        } else {
                            if (count > 1) {
                                message = (currentViewType == CrashViewType.CRASH_LOGS) ?
                                        formatString(R.string.CrashesDeleted, count) :
                                        formatString(R.string.DebugsDeleted, count);
                            } else {
                                message = (currentViewType == CrashViewType.CRASH_LOGS) ?
                                        getString(R.string.CrashDeleted) :
                                        getString(R.string.DebugDeleted);
                            }
                            BulletinFactory.of(OctoLogsActivity.this).createErrorBulletin(message).show();
                            OctoLogging.w(TAG, "Logs deleted successfully. Message: " + message + " for type: " + currentViewType);
                        }
                    });
                    builder.setNegativeButton(getString(R.string.Cancel), (dialog, which) -> dialog.dismiss());

                    String confirmationMessage;
                    if (filesToDelete.size() > 1) {
                        confirmationMessage = (currentViewType == CrashViewType.CRASH_LOGS) ?
                                formatString(R.string.CrashesDeleteConfirmation, filesToDelete.size()) :
                                formatString(R.string.DebugsDeleteConfirmation, filesToDelete.size());
                    } else {
                        confirmationMessage = (currentViewType == CrashViewType.CRASH_LOGS) ?
                                getString(R.string.CrashDeleteConfirmation) :
                                getString(R.string.DebugDeleteConfirmation);
                    }
                    builder.setMessage(confirmationMessage);
                    showDialog(builder.create());
                    OctoLogging.d(TAG, "Delete confirmation dialog shown for " + filesToDelete.size() + " selected logs for type: " + currentViewType);
                    break;
                case MENU_DELETE_ALL:
                    if (archivedLogFiles == null || archivedLogFiles.length == 0) {
                        String noLogsFoundMsg = (currentViewType == CrashViewType.CRASH_LOGS) ?
                                getString(R.string.NoCrashLogFound) :
                                getString(R.string.NoDebugLogFound);
                        BulletinFactory.of(OctoLogsActivity.this).createErrorBulletin(noLogsFoundMsg).show();
                        return;
                    }


                    if (context == null) {
                        OctoLogging.w(TAG, "Context is null, cannot show delete all confirmation dialog for type: " + currentViewType);
                        return;
                    }
                    AlertDialog.Builder allBuilder = new AlertDialog.Builder(context);
                    allBuilder.setTitle(getString(R.string.BuildAppName));
                    allBuilder.setPositiveButton(getString(R.string.OK), (dialog, which) -> {
                        switch (currentViewType) {
                            case CRASH_LOGS:
                                Crashlytics.deleteCrashLogs();
                                break;
                            case DEBUG_LOGS:
                                OctoLogging.deleteLogs();
                                break;
                        }
                        loadLogFiles();
                        updateRowsId();
                        notifyDataSetChanged();
                        actionBar.hideActionMode();

                        String message1 = (currentViewType == CrashViewType.CRASH_LOGS) ?
                                getString(R.string.AllCrashDeleted) :
                                getString(R.string.AllDebugDeleted);

                        BulletinFactory.of(OctoLogsActivity.this).createErrorBulletin(message1).show();
                        OctoLogging.w(TAG, "All logs deleted. Message: " + message1 + " for type: " + currentViewType);
                    });
                    allBuilder.setNegativeButton(getString(R.string.Cancel), (dialog, which) -> dialog.dismiss());

                    File[] filesToDeleteAllConfirmation = archivedLogFiles;
                    int countForConfirmation = filesToDeleteAllConfirmation != null ? filesToDeleteAllConfirmation.length : 0;
                    String allMessage = (currentViewType == CrashViewType.CRASH_LOGS) ?
                            formatString(R.string.CrashesDeleteConfirmation, countForConfirmation) :
                            formatString(R.string.DebugsDeleteConfirmation, countForConfirmation);

                    allBuilder.setMessage(allMessage);
                    showDialog(allBuilder.create());
                    OctoLogging.d(TAG, "Delete all confirmation dialog shown for " + countForConfirmation + " logs for type: " + currentViewType);
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
            if (listView != null && listView.isComputingLayout()) {
                listView.post(this::notifyDataSetChanged);
                return;
            }
            super.notifyDataSetChanged();
            OctoLogging.d(TAG, "notifyDataSetChanged for type: " + currentViewType);
        }

        @Override
        public void notifyItemChanged(int position) {
            if (listView != null && listView.isComputingLayout()) {
                listView.post(() -> notifyItemChanged(position));
                return;
            }
            super.notifyItemChanged(position);
            OctoLogging.d(TAG, "notifyItemChanged at position: " + position + " for type: " + currentViewType);
        }

        @Override
        public void notifyItemChanged(int position, @Nullable Object payload) {
            if (listView != null && listView.isComputingLayout()) {
                listView.post(() -> notifyItemChanged(position, payload));
                return;
            }
            super.notifyItemChanged(position, payload);
            OctoLogging.d(TAG, "notifyItemChanged at position: " + position + " with payload: " + payload + " for type: " + currentViewType);
        }

        @Override
        public void notifyItemRangeChanged(int positionStart, int itemCount) {
            if (listView != null && listView.isComputingLayout()) {
                listView.post(() -> notifyItemRangeChanged(positionStart, itemCount));
                return;
            }
            super.notifyItemRangeChanged(positionStart, itemCount);
            OctoLogging.d(TAG, "notifyItemRangeChanged from: " + positionStart + ", count: " + itemCount + " for type: " + currentViewType);
        }

        @Override
        public void notifyItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            if (listView != null && listView.isComputingLayout()) {
                listView.post(() -> notifyItemRangeChanged(positionStart, itemCount, payload));
                return;
            }
            super.notifyItemRangeChanged(positionStart, itemCount, payload);
            OctoLogging.d(TAG, "notifyItemRangeChanged from: " + positionStart + ", count: " + itemCount + " with payload: " + payload + " for type: " + currentViewType);
        }

        @Override
        public void notifyItemInserted(int position) {
            if (listView != null && listView.isComputingLayout()) {
                listView.post(() -> notifyItemInserted(position));
                return;
            }
            super.notifyItemInserted(position);
            OctoLogging.d(TAG, "notifyItemInserted at position: " + position + " for type: " + currentViewType);
        }

        @Override
        public void notifyItemMoved(int fromPosition, int toPosition) {
            if (listView != null && listView.isComputingLayout()) {
                listView.post(() -> notifyItemMoved(fromPosition, toPosition));
                return;
            }
            super.notifyItemMoved(fromPosition, toPosition);
            OctoLogging.d(TAG, "notifyItemMoved from: " + fromPosition + " to: " + toPosition + " for type: " + currentViewType);
        }

        @Override
        public void notifyItemRangeInserted(int positionStart, int itemCount) {
            if (listView != null && listView.isComputingLayout()) {
                listView.post(() -> notifyItemRangeInserted(positionStart, itemCount));
                return;
            }
            super.notifyItemRangeInserted(positionStart, itemCount);
            OctoLogging.d(TAG, "notifyItemRangeInserted from: " + positionStart + ", count: " + itemCount + " for type: " + currentViewType);
        }

        @Override
        public void notifyItemRangeRemoved(int positionStart, int itemCount) {
            if (listView != null && listView.isComputingLayout()) {
                listView.post(() -> notifyItemRangeRemoved(positionStart, itemCount));
                return;
            }
            super.notifyItemRangeRemoved(positionStart, itemCount);
            OctoLogging.d(TAG, "notifyItemRangeRemoved from: " + positionStart + ", count: " + itemCount + " for type: " + currentViewType);
        }

        @Override
        public void notifyItemRemoved(int position) {
            if (listView != null && listView.isComputingLayout()) {
                listView.post(() -> notifyItemRemoved(position));
                return;
            }
            super.notifyItemRemoved(position);
            OctoLogging.d(TAG, "notifyItemRemoved at position: " + position + " for type: " + currentViewType);
        }
    }

    private void initUpdateReceiver() {
        NotificationCenter.getInstance(getCurrentAccount()).addObserver(this, NotificationCenter.fileUploaded);
        NotificationCenter.getInstance(getCurrentAccount()).addObserver(this, NotificationCenter.fileUploadFailed);
        OctoLogging.d(TAG, "Update receiver initialized for type: " + currentViewType);
    }

    private void stopUpdateReceiver() {
        NotificationCenter.getInstance(getCurrentAccount()).removeObserver(this, NotificationCenter.fileUploaded);
        NotificationCenter.getInstance(getCurrentAccount()).removeObserver(this, NotificationCenter.fileUploadFailed);
        OctoLogging.d(TAG, "Update receiver stopped for type: " + currentViewType);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        OctoLogging.d(TAG, "didReceivedNotification with id: " + id + ", account: " + account + " for type: " + currentViewType);
        if (id == NotificationCenter.fileUploaded) {
            String location = (String) args[0];
            TLRPC.InputFile inputFile = (TLRPC.InputFile) args[1];

            if (inputFile == null) {
                OctoLogging.w(TAG, "inputFile is null in fileUploaded notification for type: " + currentViewType);
                return;
            }

            if (!Objects.equals(location, sharingFullLocation)) {
                OctoLogging.w(TAG, "Uploaded file location does not match sharing location for type: " + currentViewType);
                return;
            }

            String sendDoneText = (currentViewType == CrashViewType.CRASH_LOGS) ?
                    getString(R.string.SendCrashLogDone) :
                    getString(R.string.SendDebugLogDone);
            AndroidUtilities.runOnUIThread(() -> BulletinFactory.of(this).createSimpleBulletin(R.raw.forward, sendDoneText).show());
            OctoLogging.d(TAG, "Log sent successfully bulletin shown for type: " + currentViewType);

            TLRPC.TL_documentAttributeFilename attr = new TLRPC.TL_documentAttributeFilename();
            attr.file_name = sharingFileName;

            TLRPC.TL_inputMediaUploadedDocument inputMediaDocument = new TLRPC.TL_inputMediaUploadedDocument();
            inputMediaDocument.file = inputFile;
            inputMediaDocument.attributes.add(attr);
            inputMediaDocument.mime_type = OctoConfig.CRASH_MIME_TYPE;

            if (sharingDialogs != null) {
                for (int i = 0; i < sharingDialogs.size(); i++) {
                    TLRPC.TL_messages_sendMedia req = new TLRPC.TL_messages_sendMedia();
                    req.peer = MessagesController.getInstance(getCurrentAccount()).getInputPeer(sharingDialogs.keyAt(i));
                    req.random_id = SendMessagesHelper.getInstance(getCurrentAccount()).getNextRandomId();
                    try {
                        if (currentViewType == CrashViewType.CRASH_LOGS) {
                            req.message = Crashlytics.getSystemInfo(false);
                        } else {
                            req.message = "";
                        }
                    } catch (IllegalAccessException ignore) {
                        req.message = "";
                    }
                    req.silent = true;
                    req.media = inputMediaDocument;
                    ConnectionsManager.getInstance(getCurrentAccount()).sendRequest(req, null);
                }
                sharingDialogs = null;
                sharingFullLocation = null;
                sharingFileName = null;
            }


        } else if (id == NotificationCenter.fileUploadFailed) {
            String location = (String) args[0];

            if (!Objects.equals(location, sharingFullLocation)) {
                OctoLogging.w(TAG, "Failed upload file location does not match sharing location for type: " + currentViewType);
                return;
            }

            OctoLogging.w(TAG, "File upload failed for sharing log for type: " + currentViewType);
            AndroidUtilities.runOnUIThread(() -> BulletinFactory.of(this).createErrorBulletin(getString(R.string.ErrorOccurred)).show());

            sharingFullLocation = null;
            sharingFileName = null;
            sharingDialogs = null;
        }
    }
}