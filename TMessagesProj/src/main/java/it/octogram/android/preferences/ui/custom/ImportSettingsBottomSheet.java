package it.octogram.android.preferences.ui.custom;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.BottomSheetWithRecyclerListView;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CheckBox2;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ListView.AdapterWithDiffUtils;
import org.telegram.ui.Components.Premium.boosts.cells.selector.SelectorBtnCell;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.Switch;
import org.telegram.ui.LaunchActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.ui.components.ImportSettingsTopLayerCell;
import it.octogram.android.utils.AppRestartHelper;
import it.octogram.android.utils.ImportSettingsScanHelper;

public class ImportSettingsBottomSheet extends BottomSheetWithRecyclerListView {
    private Activity originalActivity;
    private final MessageObject message;
    private Adapter adapter;
    private final ImportButton actionButton;
    private final ArrayList<Item> items = new ArrayList<>();
    private final ArrayList<Item> oldItems = new ArrayList<>();
    private static final ArrayList<String> dataToImport = new ArrayList<>();
    private static final ImportSettingsScanHelper settingsScan = new ImportSettingsScanHelper();
    private int totalImportableKeysCounter = 0;
    private int externalKeysUnavailableInScan = 0;
    private static final List<String> expanded = new ArrayList<>();
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_CHECKBOX = 1;
    private static final int VIEW_TYPE_SWITCH = 2;
    private static final int VIEW_TYPE_INFO = 3;

    public ImportSettingsBottomSheet(BaseFragment fragment, MessageObject message1) {
        super(fragment.getContext(), fragment, false, false, false, true, ActionBarType.FADING, fragment.getResourceProvider());

        items.clear();
        oldItems.clear();
        expanded.clear();

        initDataToImportList();

        Context context = fragment.getContext();
        message = message1;

        setShowHandle(true);
        fixNavigationBar();

        recyclerListView.setLayoutManager(new LinearLayoutManager(context));
        recyclerListView.setPadding(backgroundPaddingLeft, headerTotalHeight, backgroundPaddingLeft, dp(116));
        recyclerListView.setOnItemClickListener((view, position, x, y) -> {
            if ((view == null) || (position < 0) || (position - 1 >= items.size())) {
                return;
            }

            final Item item = items.get(position - 1);

            if (item.viewType == VIEW_TYPE_HEADER) {
                return;
            }

            handleOnClickPosition(view, item, x);
        });
        this.takeTranslationIntoAccount = true;
        DefaultItemAnimator itemAnimator = getDefaultItemAnimator();
        recyclerListView.setItemAnimator(itemAnimator);

        SelectorBtnCell buttonContainer = new SelectorBtnCell(getContext(), resourcesProvider, null);
        buttonContainer.setClickable(true);
        buttonContainer.setOrientation(LinearLayout.VERTICAL);
        buttonContainer.setPadding(dp(10), dp(0), dp(10), dp(10));
        buttonContainer.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground, resourcesProvider));

        actionButton = new ImportButton(context);
        actionButton.setOnClickListener(e -> executeFileImport(message));
        buttonContainer.addView(actionButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));

        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setText(LocaleController.getString(R.string.ImportReadyOpenFile));
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        textView.setOnClickListener(view -> {
            dismiss();
            if (originalActivity != null) {
                AndroidUtilities.openForView(message, originalActivity, null, false);
            }
        });
        buttonContainer.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));
        containerView.addView(buttonContainer, LayoutHelper.createFrameMarginPx(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, backgroundPaddingLeft, 0, backgroundPaddingLeft, 0));

        updateItems();
    }

    @NonNull
    private DefaultItemAnimator getDefaultItemAnimator() {
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator() {
            @Override
            protected void onMoveAnimationUpdate(RecyclerView.ViewHolder holder) {
                super.onMoveAnimationUpdate(holder);
                containerView.invalidate();
            }
        };
        itemAnimator.setSupportsChangeAnimations(false);
        itemAnimator.setDelayAnimations(false);
        itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        itemAnimator.setDurations(350);
        return itemAnimator;
    }

    private void initDataToImportList() {
        dataToImport.clear();
        externalKeysUnavailableInScan = 0;
        totalImportableKeysCounter = 0;

        for (ImportSettingsScanHelper.SettingsScanCategory category : settingsScan.categories) {
            dataToImport.add(category.categoryId);
            for (ImportSettingsScanHelper.SettingsScanOption option : category.options) {
                dataToImport.add(option.optionKey);
                totalImportableKeysCounter++;
            }
        }

        for (Field field : OctoConfig.INSTANCE.getClass().getDeclaredFields()) {
            if (field.getType().equals(ConfigProperty.class)) {
                try {
                    ConfigProperty<?> configProperty = (ConfigProperty<?>) field.get(OctoConfig.INSTANCE);
                    if (configProperty == null) {
                        continue;
                    }

                    String fieldName = configProperty.getKey();
                    // здесь фильтрация является основополагающей, иначе будут показаны дополнительные элементы, которые не следует импортировать.
                    if (!dataToImport.contains(fieldName) && !settingsScan.excludedOptions.contains(fieldName)) {
                        dataToImport.add(fieldName);
                        externalKeysUnavailableInScan++;
                        totalImportableKeysCounter++;

                        if (BuildConfig.DEBUG) {
                            Log.d("ImportSettings", "Unknown dataset option is going to be imported:" + fieldName);
                        }
                    }
                } catch (IllegalAccessException e) {
                    Log.e(getClass().getName(), "Error getting settings state during import", e);
                }
            }
        }
    }

    private void executeFileImport(MessageObject message) {
        if (dataToImport.isEmpty()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(originalActivity);
            alertDialogBuilder.setTitle(LocaleController.getString(R.string.ImportReadyImportFailedZeroTitle));
            alertDialogBuilder.setMessage(LocaleController.getString(R.string.ImportReadyImportFailedZeroCaption));
            alertDialogBuilder.setPositiveButton("OK", null);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return;
        }

        int changedOptions = OctoConfig.INSTANCE.importMessageExport(message, dataToImport, settingsScan.excludedOptions);

        if (changedOptions > 0) {
            boolean isReloadRequested = false;
            for (ImportSettingsScanHelper.SettingsScanCategory category : settingsScan.categories) {
                for (ImportSettingsScanHelper.SettingsScanOption option : category.options) {
                    if (dataToImport.contains(option.optionKey) && option.optionRequiresRestart) {
                        isReloadRequested = true;
                        break;
                    }
                }
            }

            if (isReloadRequested) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(originalActivity);
                alertDialogBuilder.setTitle(LocaleController.getString(R.string.ImportReadyImportDonePopup));
                alertDialogBuilder.setMessage(LocaleController.getString(R.string.ImportReadyImportDonePopupDescription));
                alertDialogBuilder.setPositiveButton("OK", (dialog, v) -> AppRestartHelper.triggerRebirth(getContext(), new Intent(getContext(), LaunchActivity.class)));
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            } else {
                dismiss();
                BulletinFactory.of(getBaseFragment()).createSimpleBulletin(R.raw.info, LocaleController.formatString(R.string.ImportReadyImportDone, changedOptions)).show();
            }
        }
    }

    @Override
    protected CharSequence getTitle() {
        return LocaleController.getString(R.string.ImportReady);
    }

    @Override
    protected RecyclerListView.SelectionAdapter createAdapter(RecyclerListView listView) {
        return adapter = new Adapter();
    }

    public void setOriginalActivity(Activity originalActivity) {
        this.originalActivity = originalActivity;
    }

    private void handleOnClickPosition(View view, Item item, float x) {
        if (item.viewType == VIEW_TYPE_SWITCH) {
            if (item.hasInnerData() && (LocaleController.isRTL ? x > dp(19 + 37 + 19) : x < view.getMeasuredWidth() - dp(19 + 37 + 19))) {
                if (!expanded.contains(item.itemRelationId)) {
                    expanded.add(item.itemRelationId);
                } else {
                    expanded.remove(item.itemRelationId);
                }

                updateItems();
                return;
            }

            ImportSettingsScanHelper.SettingsScanCategory category = settingsScan.getCategoryById(item.itemRelationId);
            if (category != null) {
                if (dataToImport.contains(item.itemRelationId)) {
                    dataToImport.remove(item.itemRelationId);
                } else {
                    dataToImport.add(item.itemRelationId);
                }

                boolean mustBeEnabled = dataToImport.contains(item.itemRelationId);
                for (ImportSettingsScanHelper.SettingsScanOption option : category.options) {
                    if (mustBeEnabled && !dataToImport.contains(option.optionKey)) {
                        dataToImport.add(option.optionKey);
                    } else if (!mustBeEnabled) {
                        dataToImport.remove(option.optionKey);
                    }
                }

                updateItems();
                reloadActionButtonSize();
            }
        } else if (item.viewType == VIEW_TYPE_CHECKBOX) {
            if (dataToImport.contains(item.itemRelationId)) {
                dataToImport.remove(item.itemRelationId);
            } else {
                dataToImport.add(item.itemRelationId);
            }

            fixItemRelationIdState(item.itemRelationId);
            updateItems();
            reloadActionButtonSize();
        }
    }

    private void reloadActionButtonSize() {
        int dataToImportSize = dataToImport.size();

        // totalImportableKeysCounter не содержит родительских значений.
        // поэтому из счетчика dataToImportSize мы должны удалить их перед сравнением.

        for (ImportSettingsScanHelper.SettingsScanCategory category : settingsScan.categories) {
            if (dataToImport.contains(category.categoryId)) {
                dataToImportSize--;
            }
        }

        actionButton.setSize(dataToImportSize == totalImportableKeysCounter, dataToImportSize, totalImportableKeysCounter);
    }

    private void fixItemRelationIdState(String itemRelationId) {
        for (ImportSettingsScanHelper.SettingsScanCategory category : settingsScan.categories) {
            boolean hasSelectedOption = false;
            boolean isOneOptionSelected = false;
            for (ImportSettingsScanHelper.SettingsScanOption option : category.options) {
                if (option.optionKey.equals(itemRelationId)) {
                    hasSelectedOption = true;
                }
                if (dataToImport.contains(option.optionKey)) {
                    isOneOptionSelected = true;
                }
            }

            if (hasSelectedOption) {
                if (isOneOptionSelected && !dataToImport.contains(category.categoryId)) {
                    dataToImport.add(category.categoryId);
                } else if (!isOneOptionSelected) {
                    dataToImport.remove(category.categoryId);
                }

                break;
            }
        }
    }

    private void updateItems() {
        oldItems.clear();
        oldItems.addAll(items);
        items.clear();

        items.add(Item.asHeader());

        for (ImportSettingsScanHelper.SettingsScanCategory category : settingsScan.categories) {
            items.add(Item.asSwitch(category.categoryIcon, category.getName(), category.categoryId));

            if (expanded.contains(category.categoryId)) {
                for (ImportSettingsScanHelper.SettingsScanOption option : category.options) {
                    items.add(Item.asCheckbox(option.getName(), option.optionKey));
                }
            }
        }

        if (externalKeysUnavailableInScan > 0) {
            items.add(Item.asInfo(LocaleController.formatString("ImportReadyOtherOptions", R.string.ImportReadyOtherOptions, externalKeysUnavailableInScan)));
        }

        adapter.setItems(oldItems, items);
    }

    @Override
    public void show() {
        super.show();
        Bulletin.hideVisible();
    }

    private class Adapter extends AdapterWithDiffUtils {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final Context context = parent.getContext();
            View view = null;

            if (viewType == VIEW_TYPE_HEADER) {
                view = new ImportSettingsTopLayerCell(context, message.isOut());
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
            }

            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (position < 0 || position >= items.size()) {
                return;
            }

            final ImportSettingsBottomSheet.Item item = items.get(position);
            final int viewType = holder.getItemViewType();

            if (viewType == VIEW_TYPE_CHECKBOX || viewType == VIEW_TYPE_SWITCH) {
                final boolean divider = position + 1 < items.size() && viewType != VIEW_TYPE_SWITCH;
                SwitchCell switchCell = (SwitchCell) holder.itemView;
                switchCell.set(item, divider);
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

    private static class Item extends AdapterWithDiffUtils.Item {
        public CharSequence text;
        public int iconResId;
        public String itemRelationId;

        private Item(int viewType, CharSequence text, int iconResId, String relationId) {
            super(viewType, false);
            this.text = text;
            this.iconResId = iconResId;
            this.itemRelationId = relationId;
        }

        private boolean hasInnerData() {
            boolean hasInnerData = false;
            for (ImportSettingsScanHelper.SettingsScanCategory category : settingsScan.categories) {
                if (category.categoryId.equals(this.itemRelationId)) {
                    hasInnerData = !category.options.isEmpty();
                    break;
                }
            }
            return hasInnerData;
        }

        public static Item asHeader() {
            return new Item(VIEW_TYPE_HEADER, null, 0, null);
        }

        public static Item asCheckbox(CharSequence text, String relationId) {
            return new Item(VIEW_TYPE_CHECKBOX, text, 0, relationId);
        }

        public static Item asSwitch(int iconResId, CharSequence text, String relationId) {
            return new Item(VIEW_TYPE_SWITCH, text, iconResId, relationId);
        }

        public static Item asInfo(CharSequence text) {
            return new Item(VIEW_TYPE_INFO, text, 0, "");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Item item)) {
                return false;
            }
            if (item.viewType != viewType) {
                return false;
            }
            if (viewType == VIEW_TYPE_HEADER) {
                return true;
            }
            if (viewType == VIEW_TYPE_SWITCH) {
                if (item.iconResId != iconResId) {
                    return false;
                }
            }
            if (viewType == VIEW_TYPE_SWITCH || viewType == VIEW_TYPE_CHECKBOX) {
                if (!Objects.equals(item.itemRelationId, itemRelationId)) {
                    return false;
                }
            }
            if (viewType == VIEW_TYPE_INFO) {
                return TextUtils.equals(item.text, text);
            }
            return true;
        }
    }

    private static class SwitchCell extends FrameLayout {

        private final ImageView imageView;
        private final LinearLayout textViewLayout;
        private final TextView textView;
        private final AnimatedTextView countTextView;
        private final ImageView arrowView;
        private final Switch switchView;
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
            arrowView.setImageResource(R.drawable.arrow_more);

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
            addView(checkBoxView, LayoutHelper.createFrame(21, 21, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), LocaleController.isRTL ? 0 : 64, 0, LocaleController.isRTL ? 64 : 0, 0));

            setFocusable(true);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(dp(50), MeasureSpec.EXACTLY)
            );
        }

        private boolean isExpanded;

        public void set(Item item, boolean divider) {
            if (item.viewType == VIEW_TYPE_SWITCH) {
                checkBoxView.setVisibility(GONE);
                imageView.setVisibility(VISIBLE);
                imageView.setImageResource(item.iconResId);
                textView.setText(item.text);
                boolean containing = item.hasInnerData();
                if (containing) {
                    countTextView.setVisibility(VISIBLE);
                    arrowView.setVisibility(VISIBLE);
                } else {
                    countTextView.setVisibility(GONE);
                    arrowView.setVisibility(GONE);
                }
                textView.setTranslationX(0);
                switchView.setVisibility(VISIBLE);
                switchView.setChecked(dataToImport.contains(item.itemRelationId), true);
                needLine = containing;

                boolean currentExpanded = expanded.contains(item.itemRelationId);
                if (isExpanded != currentExpanded) {
                    isExpanded = currentExpanded;
                    arrowView.animate().rotation(isExpanded ? 180 : 0).setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).setDuration(240).start();
                }

                ImportSettingsScanHelper.SettingsScanCategory category = settingsScan.getCategoryById(item.itemRelationId);
                int selectedOptionsOnTotal = 0;
                int totalOptions = category.options.size();

                for (ImportSettingsScanHelper.SettingsScanOption option : category.options) {
                    if (dataToImport.contains(option.optionKey)) {
                        selectedOptionsOnTotal++;
                    }
                }
                countTextView.setText(selectedOptionsOnTotal + "/" + totalOptions);
            } else {
                checkBoxView.setVisibility(VISIBLE);
                checkBoxView.setChecked(dataToImport.contains(item.itemRelationId), true);
                imageView.setVisibility(GONE);
                switchView.setVisibility(GONE);
                countTextView.setVisibility(GONE);
                arrowView.setVisibility(GONE);
                textView.setText(item.text);
                textView.setTranslationX(dp(41) * (LocaleController.isRTL ? -2.2f : 1));
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

    public static class ImportButton extends FrameLayout {
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
                rtlTextView.setText(LocaleController.getString(R.string.ImportReadyImport));
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
            textView.setText(LocaleController.getString(R.string.ImportReadyImport));
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
            textView.setText((
                    allSelected ?
                            LocaleController.getString(R.string.ImportReadyImport) :
                            LocaleController.getString(R.string.ImportReadyImportSelected)
            ));
            valueTextView.setText(size <= 0 || allSelected ? "" : (size + "/" + total));
            setDisabled(size <= 0);
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