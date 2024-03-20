/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberTextView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.LaunchActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import it.octogram.android.OctoConfig;
import it.octogram.android.crashlytics.Crashlytics;
import it.octogram.android.preferences.ui.custom.CrashLogCell;

public class CrashesActivity extends BaseFragment {

    private final Object PARTIAL = new Object();

    private final String REPORT_URL = "https://github.com/OctoGramApp/OctoGram/issues/new?assignees=&labels=bug&projects=&template=bug_report.yml&title=%5BBug%5D%3A+%3Ctitle-here%3E";

    private int settingsHeaderRow;
    private int copyInfoRow;
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
        actionBar.setTitle(LocaleController.getString(R.string.CrashHistory));
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
            ActionBarMenu actionNoSelectMenu = actionBar.createMenu();
            actionNoSelectMenu.addItem(MENU_DELETE_ALL, R.drawable.msg_delete);
        }

        ActionBarMenu actionMode = actionBar.createActionMode();
        selectedCountTextView = new NumberTextView(actionMode.getContext());
        selectedCountTextView.setTextSize(18);
        selectedCountTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
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
        warningBuilder.setTitle(LocaleController.getString(R.string.Warning));
        warningBuilder.setPositiveButton(LocaleController.getString("OK", R.string.OK), (dialog1, which1) -> dialog1.dismiss());
        warningBuilder.setMessage(text);
        showDialog(warningBuilder.create());

    }

    private void copyShowBulletin (CharSequence text) {
        if (AndroidUtilities.addToClipboard(text)) {
            BulletinFactory.of(CrashesActivity.this).createCopyBulletin(LocaleController.getString("CrashLogCopied", R.string.CrashLogCopied)).show();
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
        } else if (position == copyLatestCrashRow) {
            File file = Crashlytics.getLatestArchivedCrashFile();
            if (file != null) {
                copyCrashLine(file);
            } else {
                BulletinFactory.of(CrashesActivity.this).createErrorBulletin(LocaleController.getString(R.string.NoCrashLogFound)).show();
            }
        } else if (position >= crashesStartRow && position < crashesEndRow) {
            if (listAdapter.hasSelected()) {
                listAdapter.toggleSelected(position);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), getResourceProvider());
                builder.setTitle("Crash settings");
                CharSequence[] items = new CharSequence[]{
                        LocaleController.getString("OpenCrashLog", R.string.OpenCrashLog),
                        LocaleController.getString("SendCrashLog", R.string.SendCrashLog),
                        LocaleController.getString("CopyCrashLog", R.string.CopyCrashLog),
                        LocaleController.getString("ReportCrash", R.string.ReportCrash),
                };
                int[] icons = new int[]{
                        R.drawable.msg_openin,
                        R.drawable.msg_send,
                        R.drawable.msg_copy,
                        R.drawable.msg_report,
                };
                builder.setItems(items, icons, (dialog, which) -> {
                    if (which == 0) {
                        File file = Crashlytics.getArchivedCrashFiles()[crashesEndRow - position - 1];
                        if (!openLog(file)) {
                            showWarningDialog(LocaleController.getString(R.string.ErrorSendingCrashContent));
                        }
                    } else if (which == 1) {
                        File file = Crashlytics.getArchivedCrashFiles()[crashesEndRow - position - 1];
                        if (!sendLog(file)) {
                            showWarningDialog(LocaleController.getString(R.string.ErrorSendingCrashContent));
                        }
                    } else if (which == 2) {
                        copyCrashLine(position);
                    } else if (which == 3) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(REPORT_URL));
                        context.startActivity(browserIntent);
                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                showDialog(builder.create());
            }
        }
    }

    private void copyCrashLine(int position) {
        File file = Crashlytics.getArchivedCrashFiles()[crashesEndRow - position - 1];
        copyCrashLine(file);
    }

    private void copyCrashLine(File file) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            FileLog.e(e);
            BulletinFactory.of(CrashesActivity.this).createErrorBulletin(LocaleController.getString(R.string.CouldNotCopyFile)).show();
        }

        copyShowBulletin(String.join("\n", lines));
    }

    protected void updateRowsId() {
        rowCount = 0;
        crashesHeaderRow = -1;
        crashesEndRow = -1;
        crashesInfoRow = -1;

        settingsHeaderRow = rowCount++;
        copyInfoRow = rowCount++;
        copyLatestCrashRow = rowCount++;
        settingsShadowRow = rowCount++;

        if (Crashlytics.getArchivedCrashFiles().length > 0) {
            crashesHeaderRow = rowCount++;
            crashesStartRow = rowCount;

            File[] archivedCrashFiles = Crashlytics.getArchivedCrashFiles();
            for (int i = 0; i < archivedCrashFiles.length; i++) {
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
        try {
            File cacheFile = Crashlytics.shareLog(file);
            AndroidUtilities.openForView(cacheFile, cacheFile.getName(), "text/plain", getParentActivity(), getResourceProvider());
            return true;
        } catch (IOException e) {
            Log.e(getClass().getName(), "Error opening crash content", e);
            return false;
        }
    }

    private boolean sendLog(File file) {
        try {
            File cacheFile = Crashlytics.shareLog(file);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(getParentActivity(), ApplicationLoader.getApplicationId() + ".provider", cacheFile);
            } else {
                uri = Uri.fromFile(cacheFile);
            }
            Intent i = new Intent(Intent.ACTION_SEND);
            if (Build.VERSION.SDK_INT >= 24) {
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_STREAM, uri);
            i.setClass(getParentActivity(), LaunchActivity.class);
            getParentActivity().startActivity(i);
            return true;
        } catch (IOException e) {
            Log.e(getClass().getName(), "Error sending crash content", e);
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

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
            Object payload = payloads.isEmpty() ? null : payloads.get(0);
            boolean partial = PARTIAL.equals(payload);

            switch (holder.getItemViewType()) {
                case TYPE_SETTINGS_HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == settingsHeaderRow) {
                        headerCell.setText(LocaleController.getString("Settings", R.string.Settings));
                    } else if (position == crashesHeaderRow) {
                        headerCell.setText(LocaleController.getString(R.string.CrashHistory));
                    }
                    break;
                case TYPE_BUTTON:
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == copyInfoRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.CopyOctogramConfiguration), R.drawable.msg_copy, !OctoConfig.INSTANCE.disableDividers.getValue());
                    } else if (position == copyLatestCrashRow) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.CopyLatestCrashLog), R.drawable.msg_copy, false);
                    }
                    break;
                case TEXT_HINT_WITH_PADDING:
                    TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == crashesInfoRow) {
                        textInfoPrivacyCell.setText(LocaleController.getString(R.string.CrashLogInfo));
                    }
                    break;
                case TYPE_CRASH_FILE:
                    if (position >= crashesStartRow && position < crashesEndRow) {
                        CrashLogCell crashLogCell = (CrashLogCell) holder.itemView;

                        List<File> sorted = Arrays.stream(Crashlytics.getArchivedCrashFiles())
                                .sorted(Comparator.comparingLong(File::lastModified))
                                .collect(Collectors.toList());
                        for (int i = 0; i < sorted.size(); i++) {
                            File file = sorted.get(i);
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
            if (position == copyInfoRow || position == copyLatestCrashRow) {
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
                    builder.setTitle(LocaleController.getString(R.string.BuildAppName));
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), (dialog, which) -> {
                        int unableToDeleteCount = 0;
                        for (File file : toDelete) {
                            if (!file.delete()) {
                                FileLog.e("Could not delete file " + file);
                                unableToDeleteCount++;
                            }
                        }
                        selectedItems.clear();
                        updateRowsId();
                        notifyDataSetChanged();
                        actionBar.hideActionMode();

                        String message;
                        if (toDelete.size() > 1) {
                            message = LocaleController.formatString("CrashesDeleted", R.string.CrashesDeleted, toDelete.size());
                        } else {
                            message = LocaleController.formatString("CrashDeleted", R.string.CrashDeleted);
                        }
                        if (unableToDeleteCount > 0) {
                            message = LocaleController.formatString("CrashesUnableToDelete", R.string.CrashesUnableToDelete, unableToDeleteCount);
                            AlertDialog.Builder unableToDeleteBuilder = new AlertDialog.Builder(context);
                            unableToDeleteBuilder.setTitle(LocaleController.getString(R.string.BuildAppName));
                            unableToDeleteBuilder.setMessage(message);
                            unableToDeleteBuilder.setPositiveButton(LocaleController.getString("OK", R.string.OK), (dialog1, which1) -> dialog1.dismiss());
                        }
                        BulletinFactory.of(CrashesActivity.this).createErrorBulletin(message).show();
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), (dialog, which) -> dialog.dismiss());

                    String message;
                    if (toDelete.size() > 1) {
                        message = LocaleController.formatString("CrashesDeleted", R.string.CrashesDeleteConfirmation, toDelete.size());
                    } else {
                        message = LocaleController.formatString("CrashDeleted", R.string.CrashDeleteConfirmation);
                    }
                    builder.setMessage(message);
                    showDialog(builder.create());
                    break;
                case MENU_DELETE_ALL:
                    AlertDialog.Builder allBuilder = new AlertDialog.Builder(context);
                    allBuilder.setTitle(LocaleController.getString(R.string.BuildAppName));
                    allBuilder.setPositiveButton(LocaleController.getString("OK", R.string.OK), (dialog, which) -> {
                        Crashlytics.deleteCrashLogs();
                        updateRowsId();
                        notifyDataSetChanged();

                        File[] filesToDelete = Crashlytics.getArchivedCrashFiles();
                        String message1;
                        if (filesToDelete.length > 1) {
                            message1 = LocaleController.formatString("CrashesDeleted", R.string.CrashesDeleted, filesToDelete.length);
                        } else {
                            message1 = LocaleController.formatString("CrashDeleted", R.string.CrashDeleted);
                        }
                        BulletinFactory.of(CrashesActivity.this).createErrorBulletin(message1).show();
                    });
                    allBuilder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), (dialog, which) -> dialog.dismiss());

                    File[] filesToDelete = Crashlytics.getArchivedCrashFiles();
                    String allMessage;
                    if (filesToDelete.length > 1) {
                        allMessage = LocaleController.formatString("CrashesDeleted", R.string.CrashesDeleteConfirmation, filesToDelete.length);
                    } else {
                        allMessage = LocaleController.formatString("CrashDeleted", R.string.CrashDeleteConfirmation);
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
    }

}
