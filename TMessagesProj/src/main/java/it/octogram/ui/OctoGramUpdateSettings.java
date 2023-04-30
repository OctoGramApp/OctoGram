package it.octogram.ui;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;

import it.octogram.android.OctoConfig;
import it.octogram.android.StoreUtils;
import it.octogram.android.http.FileDownloader;
import it.octogram.android.magic.OWLENC;
import it.octogram.android.updates.AppDownloader;
import it.octogram.android.updates.PlayStoreAPI;
import it.octogram.android.updates.UpdateManager;
import it.octogram.ui.Cells.UpdateAvailableCell;
import it.octogram.ui.Cells.UpdateCheckCell;

public class OctoGramUpdateSettings extends BaseSettingsActivity {

    private int updateSectionAvailableRow;
    private int updateSectionDividerRow;
    private int updateSectionHeader;
    private int updateCheckRow;
    private int betaUpdatesRow;
    private int notifyWhenAvailableRow;
    private int apkChannelRow;
    private boolean checkingUpdates;
    private TextCheckCell changeBetaMode;

    private OWLENC.UpdateAvailable updateAvailable;
    private UpdateCheckCell updateCheckCell;
    private UpdateAvailableCell updateCell;

    @Override
    public boolean onFragmentCreate() {
        if (OctoConfig.updateData.isPresent()) {
            updateAvailable = OctoConfig.updateData.get();
            if (updateAvailable.isReminded()) {
                updateAvailable = null;
            } else if (updateAvailable.version <= BuildVars.BUILD_VERSION) {
                updateAvailable = null;
                OctoConfig.saveUpdateStatus(0);
            }
        }
        AppDownloader.setListener("settings", new AppDownloader.UpdateListener() {
            @Override
            public void onPreStart() {
            }

            @Override
            public void onProgressChange(int percentage, long downBytes, long totBytes) {
                if (updateCell != null) {
                    updateCell.setPercentage(percentage, downBytes, totBytes);
                }
            }

            @Override
            public void onFinished() {
                if (!StoreUtils.isFromPlayStore()) changeBetaMode.setEnabled(!AppDownloader.updateDownloaded(), null);
                updateCheckCell.setCanCheckForUpdate(!AppDownloader.updateDownloaded() && !PlayStoreAPI.isRunningDownload());
                if (PlayStoreAPI.updateDownloaded()) {
                    updateCheckCell.setDownloaded();
                }
                if (updateCell != null) {
                    if (AppDownloader.updateDownloaded()) {
                        updateCell.setInstallMode();
                    } else {
                        updateCell.setConfirmMode();
                    }
                }
            }
        });
        return super.onFragmentCreate();
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("OctoUpdates", R.string.OctoUpdates);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == betaUpdatesRow) {
            if (!UpdateManager.updateDownloaded() && !checkingUpdates) {
                OctoConfig.toggleBetaUpdates();
                FileDownloader.cancel("appUpdate");
                UpdateManager.deleteUpdate();
                listAdapter.notifyItemChanged(apkChannelRow, PARTIAL);
                if (updateAvailable != null) {
                    OctoConfig.remindUpdate(updateAvailable.version);
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                    updateAvailable = null;
                    listAdapter.notifyItemRangeRemoved(updateSectionAvailableRow, 2);
                    listAdapter.notifyItemRangeChanged(updateSectionAvailableRow, 1);
                    updateRowsId();
                }
                checkUpdates();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(OctoConfig.betaUpdates);
                }
            }
        } else if (position == notifyWhenAvailableRow) {
            OctoConfig.toggleNotifyUpdates();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OctoConfig.notifyUpdates);
            }
        } else if (position == apkChannelRow) {
            MessagesController.getInstance(currentAccount).openByUserName(UpdateManager.getApkChannel(), this, 1);
        }
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();
        updateSectionAvailableRow = -1;
        updateSectionDividerRow = -1;
        betaUpdatesRow = -1;

        if (updateAvailable != null && !StoreUtils.isDownloadedFromAnyStore()) {
            updateSectionAvailableRow = rowCount++;
            updateSectionDividerRow = rowCount++;
        }

        updateSectionHeader = rowCount++;
        updateCheckRow = rowCount++;
        if (!StoreUtils.isDownloadedFromAnyStore()) {
            betaUpdatesRow = rowCount++;
        }
        notifyWhenAvailableRow = rowCount++;
        apkChannelRow = rowCount++;
    }

    @Override
    protected BaseListAdapter createAdapter() {
        return new ListAdapter();
    }

    private class ListAdapter extends BaseListAdapter {

        @Override
        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            switch (ViewType.fromInt(holder.getItemViewType())) {
                case SHADOW:
                    holder.itemView.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case UPDATE:
                    UpdateAvailableCell updateCell = (UpdateAvailableCell) holder.itemView;
                    OctoGramUpdateSettings.this.updateCell = updateCell;
                    updateCell.setUpdate(
                            updateAvailable.title,
                            updateAvailable.description,
                            updateAvailable.note,
                            updateAvailable.banner
                    );
                    if (AppDownloader.isRunningDownload()) {
                        updateCell.setDownloadMode();
                    } else {
                        if (AppDownloader.updateDownloaded()) {
                            updateCell.setInstallMode();
                        } else {
                            updateCell.setConfirmMode();
                        }
                    }
                    updateCell.setPercentage(AppDownloader.getDownloadProgress(), AppDownloader.downloadedBytes(), AppDownloader.totalBytes());
                    break;
                case HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == updateSectionHeader) {
                        headerCell.setText(LocaleController.getString("InAppUpdates", R.string.InAppUpdates));
                    }
                    break;
                case UPDATE_CHECK:
                    UpdateCheckCell updateCheckCell = (UpdateCheckCell) holder.itemView;
                    updateCheckCell.loadLastStatus();
                    updateCheckCell.setCanCheckForUpdate(!AppDownloader.updateDownloaded() && !PlayStoreAPI.isRunningDownload());
                    if (PlayStoreAPI.updateDownloaded()) {
                        updateCheckCell.setDownloaded();
                    }
                    break;
                case SWITCH:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(!AppDownloader.updateDownloaded() || position != betaUpdatesRow, null);
                    if (position == betaUpdatesRow) {
                        changeBetaMode = textCheckCell;
                        changeBetaMode.setTextAndValueAndCheck(LocaleController.getString("InstallBetas", R.string.InstallBetas), LocaleController.getString("InstallBetasDesc", R.string.InstallBetasDesc), OctoConfig.betaUpdates, true, true);
                    } else if (position == notifyWhenAvailableRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("AutoUpdate", R.string.AutoUpdate), LocaleController.getString("AutoUpdatePrompt", R.string.AutoUpdatePrompt), OctoConfig.notifyUpdates, true, true);
                    }
                    break;
                case TEXT_CELL:
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == apkChannelRow) {
                        textCell.setTextAndValue(LocaleController.getString("APKsChannel", R.string.APKsChannel), "@" + UpdateManager.getApkChannel(), partial, false);
                    }
                    break;
            }
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.SWITCH || viewType == ViewType.TEXT_CELL;
        }

        @Override
        protected View onCreateViewHolder(ViewType viewType) {
            View view = null;
            switch (viewType) {
                case UPDATE:
                    view = new UpdateAvailableCell(context) {
                        @Override
                        protected void onInstallUpdate() {
                            super.onInstallUpdate();
                            UpdateManager.installUpdate(getParentActivity());
                        }

                        @Override
                        protected void onConfirmUpdate() {
                            super.onConfirmUpdate();
                            if (!FileDownloader.isRunningDownload("appUpdate")) {
                                if (FileDownloader.downloadFile(context, "appUpdate", UpdateManager.apkFile(), updateAvailable.fileLink))
                                    OctoConfig.saveOldVersion(updateAvailable.version);
                                updateCell.setDownloadMode();
                            }
                        }

                        @Override
                        protected void onRemindUpdate() {
                            super.onRemindUpdate();
                            updateCheckCell.setCheckTime();
                            if (updateAvailable != null) {
                                UpdateManager.deleteUpdate();
                                OctoConfig.remindUpdate(updateAvailable.version);
                                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                                updateCheckCell.setCheckTime();
                                updateAvailable = null;
                                listAdapter.notifyItemRangeRemoved(updateSectionAvailableRow, 2);
                                listAdapter.notifyItemRangeChanged(updateSectionAvailableRow, 1);
                                updateRowsId();
                            }
                        }
                    };
                    break;
                case UPDATE_CHECK:
                    view = new UpdateCheckCell(context, true) {
                        @Override
                        protected void onCheckUpdate() {
                            super.onCheckUpdate();
                            if (PlayStoreAPI.updateDownloaded()) {
                                PlayStoreAPI.installUpdate();
                            } else if (StoreUtils.isFromPlayStore() && !PlayStoreAPI.updateDownloaded() && OctoConfig.lastUpdateStatus == 1 && updateAvailable != null) {
                                PlayStoreAPI.openUpdatePopup(getParentActivity());
                            } else if (!AppDownloader.updateDownloaded()) {
                                checkUpdates();
                            }
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    updateCheckCell = (UpdateCheckCell) view;
                    break;
            }
            return view;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position == updateSectionDividerRow) {
                return ViewType.SHADOW;
            } else if (position == updateSectionAvailableRow) {
                return ViewType.UPDATE;
            } else if (position == updateSectionHeader) {
                return ViewType.HEADER;
            } else if (position == updateCheckRow) {
                return ViewType.UPDATE_CHECK;
            } else if (position == betaUpdatesRow || position == notifyWhenAvailableRow) {
                return ViewType.SWITCH;
            } else if (position == apkChannelRow) {
                return ViewType.TEXT_CELL;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }

    private void checkUpdates() {
        updateCheckCell.setCheckingStatus();
        UpdateManager.checkUpdates(new UpdateManager.UpdateCallback() {
            @Override
            public void onSuccess(Object updateResult) {
                checkingUpdates = false;
                OctoConfig.saveLastUpdateCheck();
                if (updateResult instanceof OWLENC.UpdateAvailable) {
                    if (updateAvailable == null) {
                        OctoConfig.saveUpdateStatus(1);
                        OctoConfig.remindUpdate(-1);
                        updateAvailable = (OWLENC.UpdateAvailable) updateResult;
                        OctoConfig.updateData.set(updateAvailable);
                        OctoConfig.applyUpdateData();
                        if (StoreUtils.isFromPlayStore()) {
                            PlayStoreAPI.openUpdatePopup(getParentActivity());
                        } else {
                            listAdapter.notifyItemRangeInserted(updateSectionAvailableRow, 2);
                            listAdapter.notifyItemRangeChanged(updateSectionAvailableRow, 1);
                            updateRowsId();
                        }
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                    }
                    updateCheckCell.setUpdateAvailableStatus();
                } else {
                    OctoConfig.saveUpdateStatus(0);
                    updateCheckCell.setCheckTime();
                    if (updateAvailable != null) {
                        OctoConfig.updateData.set(null);
                        OctoConfig.applyUpdateData();
                        updateAvailable = null;
                        if (!StoreUtils.isFromPlayStore()) {
                            listAdapter.notifyItemRangeRemoved(updateSectionAvailableRow, 2);
                            listAdapter.notifyItemRangeChanged(updateSectionAvailableRow, 1);
                            updateRowsId();
                        }
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                FileLog.e(e);
                OctoConfig.saveUpdateStatus(2);
                updateCheckCell.setFailedStatus();
            }
        });
    }
}
