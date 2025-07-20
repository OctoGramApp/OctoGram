/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.CheckBox2;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ListView.AdapterWithDiffUtils;
import org.telegram.ui.Components.Premium.boosts.cells.selector.SelectorBtnCell;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.Switch;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import it.octogram.android.OctoConfig;
import it.octogram.android.app.ui.cells.ImportSettingsHeaderCell;
import it.octogram.android.utils.AppRestartHelper;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.account.FingerprintUtils;
import it.octogram.android.utils.config.ImportSettingsScanHelper;

public class ImportSettingsUI extends BaseFragment {
    private MessageObject message;
    private File file;
    private ImportButton actionButton;
    private Adapter adapter;

    private RecyclerListView recyclerListView;
    private final ArrayList<ImportItem> items = new ArrayList<>();
    private final ArrayList<ImportItem> oldItems = new ArrayList<>();

    private final ArrayList<String> mainCategoriesSelection = new ArrayList<>();
    private final ArrayList<String> dataToImport = new ArrayList<>();
    private final ArrayList<String> secureContexts = new ArrayList<>();
    private final ArrayList<String> availableKeysInBackup = new ArrayList<>();

    private ImportSettingsScanHelper.SettingsScanCategory selectedCategory;

    private static boolean hasAuthorizedBiometric = false;

    private int totalImportableKeysCounter = 0;
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_CHECKBOX = 1;
    private static final int VIEW_TYPE_SWITCH = 2;
    private static final int VIEW_TYPE_INFO = 3;
    private static final int VIEW_TYPE_MINIHEADER = 4;
    private static final int VIEW_TYPE_SHADOW = 6;

    public void setData(MessageObject message1, File file1) {
        message = message1;
        file = file1;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(getString(R.string.ImportReady));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (selectedCategory != null) {
                        destroySelection();
                    } else {
                        finishFragment();
                    }
                } else if (id == 71 && message != null) {
                    finishFragment();
                    if (getParentActivity() != null) {
                        AndroidUtilities.openForView(message, getParentActivity(), null, false);
                    }
                }
            }
        });

        if (message != null) {
            ActionBarMenu menu = actionBar.createMenu();
            ActionBarMenuItem menuItem = menu.addItem(71, R.drawable.msg_openin);
            menuItem.setContentDescription(getString(R.string.ImportReadyOpenFile));
        }

        initDataToImportList();

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        recyclerListView = new RecyclerListView(context);
        recyclerListView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerListView.setAdapter(adapter = new Adapter());
        recyclerListView.setOnItemClickListener((view, position, x, y) -> {
            if (items.isEmpty() || (view == null) || (position < 0) || (position >= items.size())) {
                return;
            }

            final ImportItem item = items.get(position);

            if (item.viewType == VIEW_TYPE_HEADER || item.viewType == VIEW_TYPE_MINIHEADER || item.viewType == VIEW_TYPE_SHADOW || item.viewType == VIEW_TYPE_INFO) {
                return;
            }

            handleOnClickPosition(view, item, x);
        });
        DefaultItemAnimator itemAnimator = getDefaultItemAnimator();
        recyclerListView.setItemAnimator(itemAnimator);
        frameLayout.addView(recyclerListView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, 0, 0, 0, 0, 48 + 10 + 10));

        SelectorBtnCell buttonContainer = new SelectorBtnCell(getContext(), null,null);
        buttonContainer.setClickable(true);
        buttonContainer.setOrientation(LinearLayout.VERTICAL);
        buttonContainer.setPadding(dp(10), dp(0), dp(10), dp(10));
        buttonContainer.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));

        actionButton = new ImportButton(context);
        actionButton.setOnClickListener(e -> {
            if (selectedCategory != null) {
                destroySelection();
            } else {
                executeFileImport();
            }
        });
        buttonContainer.addView(actionButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 10, 0, 0));

        frameLayout.addView(buttonContainer, LayoutHelper.createFrameMarginPx(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0, 0, 0));

        updateItems();
        if (!secureContexts.isEmpty()) {
            reloadActionButton();
        }

        return fragmentView;
    }

    @Override
    public void onBeginSlide() {
        if (selectedCategory != null) {
            return;
        }

        super.onBeginSlide();
    }

    @Override
    public boolean canBeginSlide() {
        return selectedCategory == null;
    }

    @Override
    public boolean onBackPressed() {
        if (selectedCategory != null) {
            destroySelection();
            return false;
        }

        return super.onBackPressed();
    }

    private void initSelection(ImportSettingsScanHelper.SettingsScanCategory category) {
        initSelection(category, false);
    }

    private void initSelection(ImportSettingsScanHelper.SettingsScanCategory category, boolean fromBack) {
        selectedCategory = category;
        updateItems();
        reloadActionButton();
        actionBar.setTitleAnimated(category.getName(), !fromBack, 300);
    }

    private void destroySelection() {
        if (selectedCategory != null && selectedCategory.refersTo != null) {
            initSelection(selectedCategory.refersTo, true);
            return;
        }

        selectedCategory = null;
        updateItems();
        reloadActionButton();
        actionBar.setTitleAnimated(getString(R.string.ImportReady), false, 300);
    }

    @NonNull
    private DefaultItemAnimator getDefaultItemAnimator() {
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);
        itemAnimator.setDelayAnimations(false);
        itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        itemAnimator.setDurations(350);
        return itemAnimator;
    }

    private void initDataToImportList() {
        fillAvailableKeys();
        totalImportableKeysCounter = 0;

        for (ImportSettingsScanHelper.SettingsScanCategory category : ImportSettingsScanHelper.INSTANCE.categories) {
            boolean hasAvailableKey = false;
            for (ImportSettingsScanHelper.SettingsScanOption option : category.options) {
                if (!option.isTitle && option.property != null && availableKeysInBackup.contains(option.property.getKey())) {
                    hasAvailableKey = true;
                    if (!category.isSecureContext) {
                        dataToImport.add(option.property.getKey());
                    }
                    totalImportableKeysCounter++;
                }
            }
            if (hasAvailableKey) {
                if (!category.isSecureContext) {
                    mainCategoriesSelection.add(category.categoryId);
                } else {
                    secureContexts.add(category.categoryId);
                }
            }
        }

        if (availableKeysInBackup.contains("ai_models")) {
            mainCategoriesSelection.add("ai_models");
            dataToImport.add("ai_models");
            totalImportableKeysCounter++;
        }
    }

    private void fillAvailableKeys() {
        File file1 = file;
        if (message != null) {
            file1 = OctoUtils.getFileContentFromMessage(message);
        }

        try (FileInputStream downloadedFileStream = new FileInputStream(file1)) {
            StringBuilder jsonStringBuilder = new StringBuilder();
            int character;
            while ((character = downloadedFileStream.read()) != -1) {
                jsonStringBuilder.append((char) character);
            }

            downloadedFileStream.close();

            JSONObject result = new JSONObject(new JSONTokener(jsonStringBuilder.toString()));

            for (Iterator<String> it = result.keys(); it.hasNext(); ) {
                String key = it.next();
                availableKeysInBackup.add(key);
            }
        } catch (IOException | JSONException ignored) {
        }
    }

    private void updateItems() {
        oldItems.clear();
        oldItems.addAll(items);
        items.clear();

        Runnable clearItems = () -> {
            if (items.size() < 2) {
                return;
            }

            ImportItem lastItem = items.get(items.size() - 1);
            ImportItem previousItem = items.get(items.size() - 2);

            if (lastItem.viewType == VIEW_TYPE_SHADOW) {
                items.remove(lastItem);
            } else if (lastItem.viewType == VIEW_TYPE_MINIHEADER && previousItem.viewType == VIEW_TYPE_SHADOW) {
                items.removeAll(Arrays.asList(lastItem, previousItem));
            }
        };

        if (selectedCategory != null) {
            ImportItem mainTitleCategory = ImportItem.asSwitch(selectedCategory.categoryId, selectedCategory.categoryIcon, selectedCategory.getName());
            items.add(mainTitleCategory);
        }

        items.add(ImportItem.asHeader(selectedCategory));

        if (selectedCategory != null) {
            ArrayList<ImportSettingsScanHelper.SettingsScanCategory> subCategories = ImportSettingsScanHelper.INSTANCE.getSubCategories(selectedCategory);
            if (!subCategories.isEmpty()) {
                ImportItem subMiniHeader = ImportItem.asMiniHeader("Sub categories");
                items.add(subMiniHeader);
                boolean hasCategories = false;
                for (ImportSettingsScanHelper.SettingsScanCategory category : subCategories) {
                    if (isCategoryAvailable(category)) {
                        hasCategories = true;
                        ImportItem mainTitleCategory = ImportItem.asSwitch(category.categoryId, category.categoryIcon, category.getName());
                        items.add(mainTitleCategory);
                    }
                }
                if (hasCategories) {
                    items.add(ImportItem.asShadow());
                } else {
                    items.remove(subMiniHeader);
                }
            }
        }

        for (ImportSettingsScanHelper.SettingsScanCategory category : ImportSettingsScanHelper.INSTANCE.categories) {
            if ((selectedCategory != null && selectedCategory != category) || !isCategoryAvailable(category)) {
                continue;
            }

            if (selectedCategory == null && category.refersTo == null) {
                ImportItem mainTitleCategory = ImportItem.asSwitch(category.categoryId, category.categoryIcon, category.getName());
                items.add(mainTitleCategory);
            }

            if (selectedCategory == category) {// && mainCategoriesSelection.contains(category.categoryId)
                for (ImportSettingsScanHelper.SettingsScanOption option : category.options) {
                    if (option.isTitle) {
                        clearItems.run();
                        items.add(ImportItem.asShadow());
                        items.add(ImportItem.asMiniHeader(option.getName()));
                    }

                    if (!option.isTitle && option.property != null && availableKeysInBackup.contains(option.property.getKey())) {
                        items.add(ImportItem.asCheckbox(option.property.getKey(), option.getName()));
                    }
                }
            }

            clearItems.run();
        }

        if (availableKeysInBackup.contains("ai_models") && selectedCategory == null) {
            items.add(ImportItem.asSwitch("ai_models", R.drawable.aifeatures_solar, getString(R.string.AiFeatures_CustomModels_Full)));
        }

        adapter.setItems(oldItems, items);
    }

    private boolean isCategoryAvailable(ImportSettingsScanHelper.SettingsScanCategory category) {
        for (ImportSettingsScanHelper.SettingsScanOption option : category.options) {
            if (!option.isTitle && option.property != null && availableKeysInBackup.contains(option.property.getKey())) {
                return true;
            }
        }

        ArrayList<ImportSettingsScanHelper.SettingsScanCategory> subCategories = ImportSettingsScanHelper.INSTANCE.getSubCategories(category);
        if (!subCategories.isEmpty()) {
            for (ImportSettingsScanHelper.SettingsScanCategory subCategory : subCategories) {
                if (isCategoryAvailable(subCategory)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void executeFileImport() {
        if (dataToImport.isEmpty()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getParentActivity());
            alertDialogBuilder.setTitle(getString(R.string.ImportReadyImportFailedZeroTitle));
            alertDialogBuilder.setMessage(getString(R.string.ImportReadyImportFailedZeroCaption));
            alertDialogBuilder.setPositiveButton("OK", null);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return;
        }

        int changedOptions = 0;
        if (message != null) {
            changedOptions = OctoConfig.INSTANCE.importMessageExport(message, dataToImport);
        } else if (file != null) {
            changedOptions = OctoConfig.INSTANCE.importFileExport(file, dataToImport);

            if (file.getAbsolutePath().startsWith(AndroidUtilities.getCacheDir().getAbsolutePath())) {
                if (file.delete()) {
                    OctoLogging.d("ImportSettings", "File has been deleted after import: " + file.getAbsolutePath());
                }

            }
        }

        if (changedOptions > 0) {
            Runnable restart = () -> {
                AlertDialog progressDialog = new AlertDialog(LaunchActivity.instance, AlertDialog.ALERT_TYPE_SPINNER);
                progressDialog.setCanCancel(false);
                progressDialog.show();
                AppRestartHelper.triggerRebirth(getContext(), new Intent(getContext(), LaunchActivity.class));
            };

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getParentActivity());
            alertDialogBuilder.setTitle(getString(R.string.ImportReadyImportDonePopup));
            alertDialogBuilder.setMessage(formatString(R.string.ImportReadyImportDone, changedOptions));
            alertDialogBuilder.setPositiveButton("OK", (dialog, v) -> restart.run());
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelDialog(false);
            alertDialog.setCanCancel(false);
            alertDialog.setOnCancelListener(dialog -> restart.run());
            alertDialog.show();
        }
    }

    private void handleOnClickPosition(View view, ImportItem item, float x) {
        if (item.viewType == VIEW_TYPE_SWITCH) {
            if (!hasAuthorizedBiometric && secureContexts.contains(item.id)) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getParentActivity());
                alertDialogBuilder.setTitle(getString(R.string.Warning));
                alertDialogBuilder.setMessage(getString(R.string.ImportReadyImportSecureContext2));
                alertDialogBuilder.setPositiveButton(getString(R.string.Proceed), (dialog, which) -> FingerprintUtils.checkFingerprint(getContext(), FingerprintUtils.FingerprintAction.IMPORT_SETTINGS, true, () -> {
                    hasAuthorizedBiometric = true;
                    handleOnClickPosition(view, item, x);
                }));
                alertDialogBuilder.setNegativeButton(getString(R.string.Cancel), null);
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.redPositive();
                alertDialog.show();
                return;
            }

            if ((selectedCategory == null || !Objects.equals(selectedCategory.categoryId, item.id)) && ImportSettingsScanHelper.INSTANCE.getCategoryById(item.id) != null && (LocaleController.isRTL ? x > dp(19 + 37 + 19) : x < view.getMeasuredWidth() - dp(19 + 37 + 19))) {
                initSelection(ImportSettingsScanHelper.INSTANCE.getCategoryById(item.id));
                return;
            }

            if (mainCategoriesSelection.contains(item.id)) {
                mainCategoriesSelection.remove(item.id);
            } else {
                mainCategoriesSelection.add(item.id);
            }

            ImportSettingsScanHelper.SettingsScanCategory category = ImportSettingsScanHelper.INSTANCE.getCategoryById(item.id);
            if (category != null) {
                boolean mustBeEnabled = mainCategoriesSelection.contains(item.id);
                ArrayList<ImportSettingsScanHelper.SettingsScanCategory> categories = new ArrayList<>();
                categories.add(category);
                categories.addAll(ImportSettingsScanHelper.INSTANCE.getSubCategories(category));

                for (ImportSettingsScanHelper.SettingsScanCategory singleCategory : categories) {
                    if (singleCategory != category) {
                        if (mustBeEnabled && !mainCategoriesSelection.contains(singleCategory.categoryId)) {
                            mainCategoriesSelection.add(singleCategory.categoryId);
                        } else if (!mustBeEnabled) {
                            mainCategoriesSelection.remove(singleCategory.categoryId);
                        }
                    }

                    for (ImportSettingsScanHelper.SettingsScanOption option : singleCategory.options) {
                        if (!option.isTitle && option.property != null && availableKeysInBackup.contains(option.property.getKey())) {
                            if (mustBeEnabled && !dataToImport.contains(option.property.getKey())) {
                                dataToImport.add(option.property.getKey());
                            } else if (!mustBeEnabled) {
                                dataToImport.remove(option.property.getKey());
                            }
                        }
                    }
                }

                if (category.refersTo != null) {
                    if (mustBeEnabled && !mainCategoriesSelection.contains(category.refersTo.categoryId)) {
                        mainCategoriesSelection.add(category.refersTo.categoryId);
                    } else if (!mustBeEnabled) {
                        categories.clear();
                        categories.add(category.refersTo);
                        categories.addAll(ImportSettingsScanHelper.INSTANCE.getSubCategories(category.refersTo));

                        boolean hasEnabledOptions = false;
                        for (ImportSettingsScanHelper.SettingsScanCategory singleCategory : categories) {
                            for (ImportSettingsScanHelper.SettingsScanOption option : singleCategory.options) {
                                if (!option.isTitle && option.property != null && availableKeysInBackup.contains(option.property.getKey()) && dataToImport.contains(option.property.getKey())) {
                                    hasEnabledOptions = true;
                                    break;
                                }
                            }
                        }
                        if (hasEnabledOptions && !mainCategoriesSelection.contains(category.refersTo.categoryId)) {
                            mainCategoriesSelection.add(category.refersTo.categoryId);
                        } else if (!hasEnabledOptions) {
                            mainCategoriesSelection.remove(category.refersTo.categoryId);
                        }
                    }
                }
            } else {
                if (mainCategoriesSelection.contains(item.id) && !dataToImport.contains(item.id)) {
                    dataToImport.add(item.id);
                } else if (!mainCategoriesSelection.contains(item.id)) {
                    dataToImport.remove(item.id);
                }
            }

            updateItems();
            reloadActionButton();
        } else if (item.viewType == VIEW_TYPE_CHECKBOX) {
            if (dataToImport.contains(item.id)) {
                dataToImport.remove(item.id);
            } else {
                dataToImport.add(item.id);
            }

            fixItemRelationIdState(item.id);
            updateItems();
            reloadActionButton();
        }
    }

    private void reloadActionButton() {
        int dataToImportSize = dataToImport.size();
        actionButton.setSize(dataToImportSize == totalImportableKeysCounter, dataToImportSize, totalImportableKeysCounter);
    }

    private void fixItemRelationIdState(String itemRelationId) {
        for (ImportSettingsScanHelper.SettingsScanCategory category : ImportSettingsScanHelper.INSTANCE.categories) {
            boolean hasFoundOption = false;
            boolean isOneOptionSelected = false;
            ImportSettingsScanHelper.SettingsScanCategory foundCategory = null;

            for (ImportSettingsScanHelper.SettingsScanOption option : category.options) {
                if (!option.isTitle && option.property != null && availableKeysInBackup.contains(option.property.getKey())) {
                    if (Objects.requireNonNull(option.property.getKey()).equals(itemRelationId)) {
                        hasFoundOption = true;
                        foundCategory = category;
                    }
                    if (dataToImport.contains(option.property.getKey())) {
                        isOneOptionSelected = true;
                    }
                }
            }

            if (!isOneOptionSelected && hasFoundOption) {
                for (ImportSettingsScanHelper.SettingsScanCategory singleCategory : ImportSettingsScanHelper.INSTANCE.getSubCategories(foundCategory)) {
                    for (ImportSettingsScanHelper.SettingsScanOption option : singleCategory.options) {
                        if (!option.isTitle && option.property != null && availableKeysInBackup.contains(option.property.getKey()) && dataToImport.contains(option.property.getKey())) {
                            isOneOptionSelected = true;
                            break;
                        }
                    }
                }
            }

            if (hasFoundOption) {
                if (foundCategory.refersTo != null) {
                    if (isOneOptionSelected && !mainCategoriesSelection.contains(foundCategory.refersTo.categoryId)) {
                        mainCategoriesSelection.add(foundCategory.refersTo.categoryId);
                    } else if (!isOneOptionSelected) {
                        mainCategoriesSelection.remove(foundCategory.refersTo.categoryId);
                    }
                }

                if (isOneOptionSelected && !mainCategoriesSelection.contains(category.categoryId)) {
                    mainCategoriesSelection.add(category.categoryId);
                } else if (!isOneOptionSelected) {
                    mainCategoriesSelection.remove(category.categoryId);
                }

                break;
            }
        }
    }

    private class Adapter extends AdapterWithDiffUtils {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final Context context = parent.getContext();
            View view = null;

            if (viewType == VIEW_TYPE_HEADER) {
                view = new ImportSettingsHeaderCell(context, message == null || message.isOut());
            } else if (viewType == VIEW_TYPE_CHECKBOX || viewType == VIEW_TYPE_SWITCH) {
                view = new SwitchCell(context);
            } else if (viewType == VIEW_TYPE_INFO) {
                view = new TextInfoPrivacyCell(context) {
                    @Override
                    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
                        super.onInitializeAccessibilityNodeInfo(info);

                        info.setEnabled(true);
                    }

                    @Override
                    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
                        super.onPopulateAccessibilityEvent(event);

                        event.setContentDescription(getTextView().getText());
                        setContentDescription(getTextView().getText());
                    }
                };
            } else if (viewType == VIEW_TYPE_MINIHEADER) {
                view = new HeaderCell(context);
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            } else if (viewType == VIEW_TYPE_SHADOW) {
                view = new ShadowSectionCell(context);
            }

            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (position < 0 || position >= items.size()) {
                return;
            }

            final ImportItem item = items.get(position);
            final int viewType = holder.getItemViewType();

            if (viewType == VIEW_TYPE_CHECKBOX || viewType == VIEW_TYPE_SWITCH) {
                final boolean divider = position + 1 < items.size() && viewType != VIEW_TYPE_SWITCH;
                SwitchCell switchCell = (SwitchCell) holder.itemView;
                switchCell.set(item, divider);
            } else if (viewType == VIEW_TYPE_HEADER) {
                ImportSettingsHeaderCell view = (ImportSettingsHeaderCell) holder.itemView;
                view.setCategory(item.category);
            } else if (viewType == VIEW_TYPE_MINIHEADER) {
                HeaderCell headerCellView = (HeaderCell) holder.itemView;
                headerCellView.setText(item.text);
            } else if (viewType == VIEW_TYPE_INFO) {
                TextInfoPrivacyCell textInfoPrivacyCell = (TextInfoPrivacyCell) holder.itemView;
                if (TextUtils.isEmpty(item.text)) {
                    textInfoPrivacyCell.setFixedSize(12);
                } else {
                    textInfoPrivacyCell.setFixedSize(0);
                }
                textInfoPrivacyCell.setText(item.text);
                textInfoPrivacyCell.setContentDescription(item.text);
                boolean top = position > 0 && items.get(position - 1).viewType != VIEW_TYPE_INFO;
                boolean bottom = position + 1 < items.size() && items.get(position + 1).viewType != VIEW_TYPE_INFO;
                if (top && bottom) {
                    textInfoPrivacyCell.setBackground(Theme.getThemedDrawableByKey(getContext(), R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                } else if (top) {
                    textInfoPrivacyCell.setBackground(Theme.getThemedDrawableByKey(getContext(), R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                } else if (bottom) {
                    textInfoPrivacyCell.setBackground(Theme.getThemedDrawableByKey(getContext(), R.drawable.greydivider_top, Theme.key_windowBackgroundGrayShadow));
                } else {
                    textInfoPrivacyCell.setBackground(null);
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).viewType;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getItemViewType() == VIEW_TYPE_CHECKBOX || holder.getItemViewType() == VIEW_TYPE_SWITCH;
        }
    }

    private static class ImportItem extends AdapterWithDiffUtils.Item {
        private final String id;
        public CharSequence text;
        public int iconResId;
        private final ImportSettingsScanHelper.SettingsScanCategory category;

        private ImportItem(int viewType, String id, CharSequence text, int iconResId, ImportSettingsScanHelper.SettingsScanCategory category) {
            super(viewType, false);
            this.id = id;
            this.text = text;
            this.iconResId = iconResId;
            this.category = category;
        }

        public static ImportItem asHeader(ImportSettingsScanHelper.SettingsScanCategory category) {
            return new ImportItem(VIEW_TYPE_HEADER, null, null, 0, category);
        }

        public static ImportItem asMiniHeader(CharSequence text) {
            return new ImportItem(VIEW_TYPE_MINIHEADER, null, text, 0, null);
        }

        public static ImportItem asCheckbox(String id, CharSequence text) {
            return new ImportItem(VIEW_TYPE_CHECKBOX, id, text, 0, null);
        }

        public static ImportItem asSwitch(String id, int iconResId, CharSequence text) {
            return new ImportItem(VIEW_TYPE_SWITCH, id, text, iconResId, null);
        }

        public static ImportItem asShadow() {
            return new ImportItem(VIEW_TYPE_SHADOW, null, null, 0, null);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ImportItem item)) {
                return false;
            }
            if (item.viewType != viewType) {
                return false;
            }
            if (viewType == VIEW_TYPE_HEADER) {
                return Objects.equals(item.category, category);
            }
            if (viewType == VIEW_TYPE_SWITCH) {
                return TextUtils.equals(item.id, id);
            }
            if (viewType == VIEW_TYPE_INFO || viewType == VIEW_TYPE_MINIHEADER) {
                //return TextUtils.equals(item.text, text);
            }
            return true;
        }
    }

    private class SwitchCell extends FrameLayout {

        private final ImageView imageView;
        private final LinearLayout textViewLayout;
        private final TextView textView;
        private final AnimatedTextView countTextView;
        private final ImageView arrowView;
        private Switch switchView;
        private final CheckBox2 checkBoxView;

        private boolean needDivider, needLine;

        public SwitchCell(Context context) {
            super(context);

            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            imageView = new ImageView(context);
            imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
            imageView.setVisibility(View.GONE);
            addView(imageView, LayoutHelper.createFrame(24, 24, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), 20, 0, 20, 0));

            textView = new AppCompatTextView(context) {
                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
                        widthMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec) - dp(52), MeasureSpec.AT_MOST);
                    }
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            };
            textView.setLines(1);
            textView.setSingleLine(true);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            textView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            textView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);

            countTextView = new AnimatedTextView(context, false, true, true);
            countTextView.setAnimationProperties(.35f, 0, 200, CubicBezierInterpolator.EASE_OUT_QUINT);
            countTextView.setTypeface(AndroidUtilities.bold());
            countTextView.setTextSize(dp(14));
            countTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            countTextView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);

            arrowView = new ImageView(context);
            arrowView.setVisibility(GONE);
            arrowView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), PorterDuff.Mode.MULTIPLY));
            arrowView.setImageResource(R.drawable.msg_arrow_forward);

            textViewLayout = new LinearLayout(context);
            textViewLayout.setOrientation(LinearLayout.HORIZONTAL);
            textViewLayout.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            if (LocaleController.isRTL) {
                textViewLayout.addView(arrowView, LayoutHelper.createLinear(16, 16, 0, Gravity.CENTER_VERTICAL, 0, 0, 6, 0));
                textViewLayout.addView(countTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, Gravity.CENTER_VERTICAL, 0, 0, 6, 0));
                textViewLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));
            } else {
                textViewLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));
                textViewLayout.addView(countTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, Gravity.CENTER_VERTICAL, 6, 0, 0, 0));
                textViewLayout.addView(arrowView, LayoutHelper.createLinear(16, 16, 0, Gravity.CENTER_VERTICAL, 2, 0, 0, 0));
            }
            addView(textViewLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), 64, 0, 8, 0));

            switchView = new Switch(context);
            switchView.setVisibility(GONE);
            switchView.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
            switchView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            addView(switchView, LayoutHelper.createFrame(37, 50, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT), 19, 0, 19, 0));

            checkBoxView = new CheckBox2(context, 21);
            checkBoxView.setColor(Theme.key_radioBackgroundChecked, Theme.key_checkboxDisabled, Theme.key_checkboxCheck);
            checkBoxView.setDrawUnchecked(true);
            checkBoxView.setChecked(true, false);
            checkBoxView.setDrawBackgroundAsArc(10);
            checkBoxView.setVisibility(GONE);
            checkBoxView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            addView(checkBoxView, LayoutHelper.createFrame(21, 21, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), 19, 0, LocaleController.isRTL ? 64 : 0, 0));

            setFocusable(true);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(dp(50), MeasureSpec.EXACTLY)
            );
        }

        private boolean _isLocked = false;
        private boolean _initData = false;
        private boolean _wasBigSwitchChecked = false;

        public void set(ImportItem item, boolean divider) {
            if (item.viewType == VIEW_TYPE_SWITCH) {
                ImportSettingsScanHelper.SettingsScanCategory category = ImportSettingsScanHelper.INSTANCE.getCategoryById(item.id);
                boolean hasInnerData = category != null;
                boolean asBigSwitch = selectedCategory != null && Objects.equals(item.id, selectedCategory.categoryId);

                checkBoxView.setVisibility(GONE);
                imageView.setVisibility(VISIBLE);
                imageView.setImageResource(item.iconResId);
                textView.setText(item.text);
                textView.setTranslationX(0);
                switchView.setVisibility(VISIBLE);
                switchView.setChecked(mainCategoriesSelection.contains(item.id), true);
                needLine = hasInnerData && !asBigSwitch;

                boolean needAnimateColorChange = false;
                int fromColor = 0, toColor = 0;

                if ((asBigSwitch && !_initData) || (!asBigSwitch && _initData)) {
                    _initData = asBigSwitch;
                    needAnimateColorChange = true;
                    int c1 = Theme.getColor(Theme.key_windowBackgroundWhite);
                    int c2 = Theme.getColor(switchView.isChecked() ? Theme.key_windowBackgroundChecked : Theme.key_windowBackgroundUnchecked);

                    int index = indexOfChild(switchView);
                    ViewGroup.LayoutParams layoutParams = switchView.getLayoutParams();
                    removeView(switchView);

                    _wasBigSwitchChecked = switchView.isChecked();
                    switchView = new Switch(getContext());
                    switchView.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
                    switchView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
                    if (asBigSwitch) {
                        fromColor = c1;
                        toColor = c2;
                        switchView.setColors(Theme.key_switchTrackBlue, Theme.key_switchTrackBlueChecked, Theme.key_switchTrackBlueThumb, Theme.key_switchTrackBlueThumbChecked);
                    } else {
                        fromColor = c2;
                        toColor = c1;
                        switchView.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
                    }
                    switchView.setChecked(mainCategoriesSelection.contains(item.id), true);
                    addView(switchView, index == -1 ? 0 : index, layoutParams);

                    textView.setTypeface(asBigSwitch ? AndroidUtilities.bold() : AndroidUtilities.mediumTypeface);
                    textView.setTextColor(Theme.getColor(asBigSwitch ? Theme.key_windowBackgroundCheckText : Theme.key_windowBackgroundWhiteBlackText));
                    imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(asBigSwitch ? Theme.key_windowBackgroundCheckText : Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
                } else if (asBigSwitch && _wasBigSwitchChecked != switchView.isChecked()) {
                    _wasBigSwitchChecked = switchView.isChecked();
                    fromColor = Theme.getColor(switchView.isChecked() ? Theme.key_windowBackgroundUnchecked : Theme.key_windowBackgroundChecked);
                    toColor = Theme.getColor(switchView.isChecked() ? Theme.key_windowBackgroundChecked : Theme.key_windowBackgroundUnchecked);
                    needAnimateColorChange = true;
                }

                if (needAnimateColorChange) {
                    int finalFromColor = fromColor;
                    int finalToColor = toColor;

                    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
                    animator.addUpdateListener(animation -> {
                        float value = (float) animation.getAnimatedValue();
                        setBackgroundColor(ColorUtils.blendARGB(finalFromColor, finalToColor, value));
                    });
                    animator.setDuration(200);
                    animator.start();
                }

                boolean isLocked;
                if (secureContexts.contains(item.id)) {
                    isLocked = !hasAuthorizedBiometric;

                    arrowView.setVisibility((hasInnerData || isLocked) ? VISIBLE : GONE);
                    arrowView.setScaleX(isLocked ? 0.8f : 1f);
                    arrowView.setScaleY(isLocked ? 0.8f : 1f);

                    if (_isLocked != isLocked) {
                        _isLocked = isLocked;
                        arrowView.setColorFilter(new PorterDuffColorFilter(Theme.getColor((isLocked || !hasInnerData) ? Theme.key_stickers_menu : Theme.key_windowBackgroundWhiteBlackText), PorterDuff.Mode.MULTIPLY));
                        arrowView.setImageResource((isLocked || !hasInnerData) ? R.drawable.other_lockedfolders2 : R.drawable.msg_arrow_forward);
                        arrowView.setTranslationX((isLocked || !hasInnerData) ? dp(2) : 0);
                    }
                } else {
                    arrowView.setVisibility(hasInnerData ? VISIBLE : GONE);
                    arrowView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), PorterDuff.Mode.MULTIPLY));
                    arrowView.setImageResource(R.drawable.msg_arrow_forward);
                    arrowView.setTranslationX(0);
                }

                if (hasInnerData) {
                    countTextView.setVisibility(VISIBLE);
                    int selectedOptionsOnTotal = 0;
                    int totalOptions = 0;

                    ArrayList<ImportSettingsScanHelper.SettingsScanCategory> categories = new ArrayList<>();
                    categories.add(category);
                    categories.addAll(ImportSettingsScanHelper.INSTANCE.getSubCategories(category));

                    for (ImportSettingsScanHelper.SettingsScanCategory singleCategory : categories) {
                        for (ImportSettingsScanHelper.SettingsScanOption option : singleCategory.options) {
                            if (!option.isTitle && option.property != null && availableKeysInBackup.contains(option.property.getKey())) {
                                if (dataToImport.contains(option.property.getKey())) {
                                    selectedOptionsOnTotal++;
                                }
                                totalOptions++;
                            }
                        }
                    }

                    countTextView.setText(selectedOptionsOnTotal + "/" + totalOptions);
                } else {
                    countTextView.setVisibility(GONE);
                }
            } else {
                checkBoxView.setVisibility(VISIBLE);
                checkBoxView.setChecked(dataToImport.contains(item.id), true);
                imageView.setVisibility(GONE);
                switchView.setVisibility(GONE);
                countTextView.setVisibility(GONE);
                arrowView.setVisibility(GONE);
                textView.setText(item.text);
                //textView.setTranslationX(dp(41) * (LocaleController.isRTL ? -2.2f : 1));
                needLine = false;
            }

            ((MarginLayoutParams) textViewLayout.getLayoutParams()).rightMargin = dp(8);

            setWillNotDraw(!((needDivider = divider) || needLine));
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            if (LocaleController.isRTL) {
                if (needLine) {
                    float x = dp(19 + 37 + 19);
                    canvas.drawRect(x - dp(0.66f), (getMeasuredHeight() - dp(20)) / 2f, x, (getMeasuredHeight() + dp(20)) / 2f, Theme.dividerPaint);
                }
                if (needDivider && !OctoConfig.INSTANCE.disableDividers.getValue()) {
                    canvas.drawLine(getMeasuredWidth() - dp(64) + (textView.getTranslationX() < 0 ? dp(-32) : 0), getMeasuredHeight() - 1, 0, getMeasuredHeight() - 1, Theme.dividerPaint);
                }
            } else {
                if (needLine) {
                    float x = getMeasuredWidth() - dp(19 + 37 + 19);
                    canvas.drawRect(x - dp(0.66f), (getMeasuredHeight() - dp(20)) / 2f, x, (getMeasuredHeight() + dp(20)) / 2f, Theme.dividerPaint);
                }
                if (needDivider && !OctoConfig.INSTANCE.disableDividers.getValue()) {
                    canvas.drawLine(dp(64) + textView.getTranslationX(), getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, Theme.dividerPaint);
                }
            }
        }
    }

    private class ImportButton extends FrameLayout {
        FrameLayout button;
        AnimatedTextView.AnimatedTextDrawable textView;
        AnimatedTextView.AnimatedTextDrawable valueTextView;
        TextView rtlTextView;
        OnClickListener currentListener;

        public ImportButton(Context context) {
            super(context);

            button = new FrameLayout(context) {
                @Override
                protected void dispatchDraw(@NonNull Canvas canvas) {
                    final int margin = dp(8);
                    int x = (getMeasuredWidth() - margin - (int) valueTextView.getCurrentWidth() + (int) textView.getCurrentWidth()) / 2;

                    if (LocaleController.isRTL) {
                        super.dispatchDraw(canvas);
                    } else {
                        textView.setBounds(0, 0, x, getHeight());
                        textView.draw(canvas);

                        valueTextView.setBounds(x + dp(8), 0, getWidth(), getHeight());
                        valueTextView.draw(canvas);
                    }
                }

                @Override
                protected boolean verifyDrawable(@NonNull Drawable who) {
                    return who == valueTextView || who == textView || super.verifyDrawable(who);
                }

                @Override
                public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(info);
                    info.setClassName("android.widget.Button");
                }

                @Override
                public boolean onInterceptTouchEvent(MotionEvent ev) {
                    super.onInterceptTouchEvent(ev);
                    return true;
                }
            };
            button.setBackground(Theme.AdaptiveRipple.filledRectByKey(Theme.key_featuredStickers_addButton, 8));
            button.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);

            if (LocaleController.isRTL) {
                rtlTextView = new TextView(context);
                rtlTextView.setText(getString(R.string.ImportReadyImport));
                rtlTextView.setGravity(Gravity.CENTER);
                rtlTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                rtlTextView.setTypeface(AndroidUtilities.bold());
                rtlTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
                button.addView(rtlTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER));
            }

            textView = new AnimatedTextView.AnimatedTextDrawable(true, true, true);
            textView.setAnimationProperties(.25f, 0, 300, CubicBezierInterpolator.EASE_OUT_QUINT);
            textView.setCallback(button);
            textView.setTextSize(dp(14));
            textView.setText(getString(R.string.ImportReadyImport));
            textView.setGravity(Gravity.RIGHT);
            textView.setTypeface(AndroidUtilities.bold());
            textView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));

            valueTextView = new AnimatedTextView.AnimatedTextDrawable(true, true, true);
            valueTextView.setAnimationProperties(.25f, 0, 300, CubicBezierInterpolator.EASE_OUT_QUINT);
            valueTextView.setCallback(button);
            valueTextView.setTextSize(dp(14));
            valueTextView.setTypeface(AndroidUtilities.bold());
            valueTextView.setTextColor(Theme.blendOver(Theme.getColor(Theme.key_featuredStickers_addButton), Theme.multAlpha(Theme.getColor(Theme.key_featuredStickers_buttonText), .7f)));
            valueTextView.setText("");

            button.setContentDescription(TextUtils.concat(textView.getText(), "\t", valueTextView.getText()));

            addView(button, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.FILL));
        }

        @Override
        public void setOnClickListener(OnClickListener listener) {
            currentListener = listener;
            button.setOnClickListener(listener);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    heightMeasureSpec
            );
        }

        public void setSize(boolean allSelected, long size, long total) {
            if (selectedCategory != null) {
                textView.setText("Save");
                valueTextView.setText("");
            } else {
                textView.setText((
                        allSelected ?
                                getString(R.string.ImportReadyImport) :
                                getString(R.string.ImportReadyImportSelected)
                ));
                valueTextView.setText(size <= 0 || allSelected ? "" : (size + "/" + total));
            }

            setDisabled(size <= 0 && selectedCategory == null);
            button.invalidate();

            button.setContentDescription(TextUtils.concat(textView.getText(), "\t", valueTextView.getText()));

            if (currentListener != null) {
                button.setOnClickListener(currentListener);
            }
        }

        public void setDisabled(boolean disabled) {
            button.animate().cancel();
            button.animate().alpha(disabled ? .65f : 1f).start();
            button.setClickable(!disabled);
        }
    }
}
