/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.fragment;

import static android.widget.LinearLayout.VERTICAL;
import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.ui.Components.LayoutHelper.createLinear;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ListView.AdapterWithDiffUtils;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SlideChooseView;

import java.util.ArrayList;

import it.octogram.android.app.PreferenceType;
import it.octogram.android.app.rows.BaseRow;
import it.octogram.android.app.rows.cells.CustomAIModelCell;
import it.octogram.android.app.rows.cells.SliderCell;
import it.octogram.android.app.rows.impl.CheckboxRow;
import it.octogram.android.app.rows.impl.CustomAIModelRow;
import it.octogram.android.app.rows.impl.CustomCellRow;
import it.octogram.android.app.rows.impl.ExpandableRows;
import it.octogram.android.app.rows.impl.ExpandableRowsChild;
import it.octogram.android.app.rows.impl.FooterInformativeRow;
import it.octogram.android.app.rows.impl.HeaderRow;
import it.octogram.android.app.rows.impl.ListRow;
import it.octogram.android.app.rows.impl.SliderChooseRow;
import it.octogram.android.app.rows.impl.SliderRow;
import it.octogram.android.app.rows.impl.StickerHeaderRow;
import it.octogram.android.app.rows.impl.SwitchRow;
import it.octogram.android.app.rows.impl.TextDetailRow;
import it.octogram.android.app.rows.impl.TextIconRow;
import it.octogram.android.app.ui.components.ExpandableRowIndex;
import it.octogram.android.app.ui.components.SwitchCell;
import it.octogram.android.utils.OctoUtils;

public class ListAdapter extends AdapterWithDiffUtils {

    private final PreferencesFragment fragment;
    private ArrayList<BaseRow> currentShownItems = new ArrayList<>();

    public ListAdapter(PreferencesFragment fragment) {
        this.fragment = fragment;
    }

    public void setItems(ArrayList<? extends Item> oldItems, ArrayList<? extends Item> newItems) {
        ArrayList<BaseRow> baseRowItems = new ArrayList<>();
        for (Item item : newItems) {
            if (item instanceof BaseRow) {
                baseRowItems.add((BaseRow) item);
            }
        }
        this.currentShownItems = baseRowItems;
        super.setItems(oldItems, newItems);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        View view;
        PreferenceType type = PreferenceType.fromAdapterType(viewType);
        if (type == null) {
            throw new RuntimeException("No type found for " + viewType);
        }

        switch (type) {
            case CUSTOM:
            case TEXT_ICON:
                view = new FrameLayout(context);
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                break;
            case SHADOW:
                view = new ShadowSectionCell(context);
                break;
            case EMPTY_CELL:
            case HEADER:
                view = new HeaderCell(context);
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                break;
            case SWITCH:
                view = new TextCheckCell(context);
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                break;
            case TEXT_DETAIL:
                view = new TextDetailSettingsCell(context);
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                break;
            case SLIDER:
                view = new SliderCell(context);
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                break;
            case LIST:
                view = new TextSettingsCell(context, 21);
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                break;
            case SLIDER_CHOOSE:
                view = new SlideChooseView(context) {
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouchEvent(MotionEvent event) {
                        if (fragment.isSelectingItems()) {
                            return false;
                        }

                        return super.onTouchEvent(event);
                    }

                    @Override
                    public boolean isSlidable() {
                        return !fragment.isSelectingItems() && super.isSlidable();
                    }
                };
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                break;
            case FOOTER:
                TextInfoPrivacyCell cell = new TextInfoPrivacyCell(context, 10);
                cell.getTextView().setGravity(Gravity.CENTER_HORIZONTAL);
                cell.getTextView().setTextColor(fragment.getThemedColor(Theme.key_windowBackgroundWhiteGrayText3));
                cell.getTextView().setMovementMethod(null);
                cell.getTextView().setPadding(0, dp(14), 0, dp(14));
                view = cell;
                view.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, fragment.getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                break;
            case FOOTER_INFORMATIVE:
                view = new TextInfoPrivacyCell(context);
                view.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, fragment.getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                break;
            case STICKER_HEADER:
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(VERTICAL);
                view = layout;
                break;
            case CHECKBOX:
                CheckBoxCell checkBoxCell = new CheckBoxCell(context, 4, 21, fragment.getResourceProvider());
                checkBoxCell.getCheckBoxRound().setDrawBackgroundAsArc(14);
                checkBoxCell.getCheckBoxRound().setColor(Theme.key_switch2TrackChecked, Theme.key_radioBackground, Theme.key_checkboxCheck);
                checkBoxCell.setEnabled(true);
                view = checkBoxCell;
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                break;
            case EXPANDABLE_ROWS:
            case EXPANDABLE_ROWS_CHILD:
                view = new SwitchCell(context, fragment);
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                break;
            case CUSTOM_AI_MODEL:
                view = new CustomAIModelCell(context);
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                break;
            default:
                throw new RuntimeException("No view found for " + type);
        }

        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        return new RecyclerListView.Holder(view);
    }

    @Override
    @SuppressLint("RecyclerView")
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position < 0 || position >= currentShownItems.size()) {
            return;
        }

        PreferenceType type = PreferenceType.fromAdapterType(holder.getItemViewType());
        if (type == null) {
            throw new RuntimeException("No type found for " + holder.getItemViewType());
        }

        ArrayList<View> additionalItems = new ArrayList<>();

        switch (type) {
            case CUSTOM:
                FrameLayout frameLayout = (FrameLayout) holder.itemView;

                // add custom view
                CustomCellRow row = (CustomCellRow) currentShownItems.get(position);

                if (!row.avoidReDraw() || frameLayout.getChildCount() == 0) {
                    // remove the current view if it exists
                    if (frameLayout.getChildCount() > 0) {
                        frameLayout.removeAllViews();
                    }

                    if (row.getLayout() != null) {
                        View customView = row.getLayout();
                        ViewGroup parent = (ViewGroup) customView.getParent();
                        if (parent != null) {
                            parent.removeView(customView);
                        }
                        frameLayout.addView(customView);

                        if (row.isEnabled()) {
                            frameLayout.setClickable(true);
                            frameLayout.setOnClickListener((v) -> {

                            });
                        }
                    }
                }

                break;
            case SHADOW:
                holder.itemView.setBackground(Theme.getThemedDrawable(fragment.getContext(), R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                break;
            case EMPTY_CELL:
            case HEADER:
                HeaderRow headerRow = (HeaderRow) currentShownItems.get(position);
                HeaderCell headerCell = (HeaderCell) holder.itemView;
                headerCell.setText(headerRow.getTitle());
                if (!headerRow.getUseHeaderStyle()) {
                    headerCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    headerCell.setTextSize(16);
                    headerCell.getTextView().setTypeface(null);
                    headerCell.setTopMargin(10);
                }
                break;
            case SWITCH:
                TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                SwitchRow switchRow = (SwitchRow) currentShownItems.get(position);
                if (switchRow.isMainPageAction()) {
                    int clr = Theme.getColor(switchRow.getPreferenceValue() ? Theme.key_windowBackgroundChecked : Theme.key_windowBackgroundUnchecked);

                    if (!switchRow.getCachedState().isHasInitData()) {
                        checkCell.setHeight(56);
                        checkCell.setBackgroundColor(clr);
                        checkCell.setTypeface(AndroidUtilities.bold());
                        checkCell.setColors(Theme.key_windowBackgroundCheckText, Theme.key_switchTrackBlue, Theme.key_switchTrackBlueChecked, Theme.key_switchTrackBlueThumb, Theme.key_switchTrackBlueThumbChecked);
                        switchRow.getCachedState().setHasInitData(true);
                    } else if (switchRow.getCachedState().getLastState() != switchRow.getPreferenceValue()) {
                        if (switchRow.getPreferenceValue()) {
                            checkCell.setBackgroundColorAnimated(true, clr);
                        } else {
                            checkCell.setBackgroundColorAnimatedReverse(clr);
                        }
                    }

                    switchRow.getCachedState().setLastState(switchRow.getPreferenceValue());
                }
                if (switchRow.getSummary() != null) {
                    checkCell.setTextAndValueAndCheck(OctoUtils.safeToString(switchRow.getTitle()), switchRow.getSummary(), switchRow.getPreferenceValue(), true, switchRow.hasDivider());
                } else {
                    checkCell.setTextAndCheck(switchRow.getTitle(), switchRow.getPreferenceValue(), switchRow.hasDivider());
                }
                checkCell.setCheckBoxIcon(switchRow.isPremium() || switchRow.isLocked() ? R.drawable.permission_locked : 0);
                additionalItems.add(checkCell.getCheckBox());
                break;
            case TEXT_DETAIL:
                TextDetailRow textDetailRow = (TextDetailRow) currentShownItems.get(position);
                textDetailRow.bindCell((TextDetailSettingsCell) holder.itemView);
                TextDetailSettingsCell settingsCell = (TextDetailSettingsCell) holder.itemView;
                settingsCell.setMultilineDetail(true);
                textDetailRow.bindCell(settingsCell);
                if (!settingsCell.isMultiline()) {
                    additionalItems.add(settingsCell.getValueTextView());
                }
                break;
            case TEXT_ICON:
                FrameLayout l = ((FrameLayout) holder.itemView);
                // remove the current view if it exists
                if (l.getChildCount() > 0) {
                    l.removeAllViews();
                }
                // add custom view
                TextIconRow textIconRow = (TextIconRow) currentShownItems.get(position);
                TextCell v = new TextCell(fragment.getContext(), 23, false, textIconRow.getPreference() != null, fragment.getResourceProvider());
                l.addView(v);

                if (textIconRow.isBlue()) {
                    v.setColors(Theme.key_windowBackgroundWhiteBlueText4, Theme.key_windowBackgroundWhiteBlueText4);
                }

                textIconRow.bindCell(v);
                break;
            case SLIDER:
                ((SliderCell) holder.itemView).setSliderRow((SliderRow) currentShownItems.get(position));
                ((SliderCell) holder.itemView).setSlideable(!fragment.isSelectingItems());
                break;
            case LIST:
                TextSettingsCell listCell = (TextSettingsCell) holder.itemView;
                ListRow listRow = (ListRow) currentShownItems.get(position);
                listCell.setTextAndValue(listRow.getTitle(), Emoji.replaceEmoji(listRow.getTextFromInteger(listRow.getCurrentValue().getValue()), listCell.getValueTextView().getPaint().getFontMetricsInt(), false), listRow.hasDivider());
                additionalItems.add(listCell.getValueTextView());

                if (listRow.getIcon() != -1) {
                    listCell.setIcon(listRow.getIcon());
                }
                break;
            case SLIDER_CHOOSE:
                SlideChooseView slideChooseView = (SlideChooseView) holder.itemView;
                SliderChooseRow sliderRow = (SliderChooseRow) currentShownItems.get(position);
                slideChooseView.setCallback(new SlideChooseView.Callback() {
                    @Override
                    public void onOptionSelected(int index) {
                        sliderRow.getPreferenceValue().updateValue(sliderRow.getOptions().get(index).first);

                        if (sliderRow.getOnUpdate() != null) {
                            sliderRow.getOnUpdate().run();
                        }
                    }

                    @Override
                    public void onTouchEnd() {
                        if (sliderRow.getOnTouchEnd() != null) {
                            sliderRow.getOnTouchEnd().run();
                        }
                    }
                });
                slideChooseView.setOptions(sliderRow.getIntValue(), sliderRow.getValues());
                break;
            case FOOTER_INFORMATIVE:
                ((TextInfoPrivacyCell) holder.itemView).setText(((FooterInformativeRow) currentShownItems.get(position)).getDynamicTitle());
                break;
            case FOOTER:
                ((TextInfoPrivacyCell) holder.itemView).setText(currentShownItems.get(position).getTitle());
                break;
            case STICKER_HEADER:
                StickerHeaderRow pref = (StickerHeaderRow) currentShownItems.get(position);
                LinearLayout linearLayout = (LinearLayout) holder.itemView;

                if (linearLayout.getChildCount() == 0) {
                    if (!pref.getUseOctoAnimation()) {
                        View stickerView = (View) pref.getStickerView();
                        linearLayout.addView(stickerView, createLinear(104, 104, Gravity.CENTER_HORIZONTAL, 0, 14, 0, 0));
                    }

                    TextView messageTextView = null;
                    if (pref.getSummary() != null) {
                        messageTextView = new TextView(fragment.getContext());
                        messageTextView.setTextColor(Theme.getColor(Theme.key_chats_message));
                        messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                        messageTextView.setGravity(Gravity.CENTER);
                        messageTextView.setText(pref.getSummary());
                        messageTextView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());

                        if (!pref.getUseOctoAnimation()) {
                            linearLayout.addView(messageTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 52, 25, 52, 18));
                        }
                    }

                    if (pref.getUseOctoAnimation()) {
                        OctoAnimationFragment octoFragment = new OctoAnimationFragment(fragment.getContext(), messageTextView);
                        octoFragment.setEasterEggCallback(fragment::animateFireworksOverlay);
                        linearLayout.addView(octoFragment, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, OctoAnimationFragment.sz, Gravity.CENTER_HORIZONTAL));
                    }
                }

                break;
            case CHECKBOX:
                CheckBoxCell checkboxCell = (CheckBoxCell) holder.itemView;
                CheckboxRow checkboxRow = (CheckboxRow) currentShownItems.get(position);
                checkboxCell.setText(checkboxRow.getTitle(), checkboxRow.getSummary(), checkboxRow.getPreferenceValue(), checkboxRow.hasDivider(), true);
                checkboxCell.setIcon(checkboxRow.isPremium() ? R.drawable.permission_locked : 0);
                break;
            case EXPANDABLE_ROWS:
                SwitchCell switchCell = (SwitchCell) holder.itemView;
                ExpandableRows expandableRows = (ExpandableRows) currentShownItems.get(position);

                if (fragment.getExpandableIndexes().get(expandableRows.getId()) == null) {
                    fragment.getExpandableIndexes().put(expandableRows.getId(), new ExpandableRowIndex(switchCell, expandableRows.getOnSingleStateChange()));
                }

                switchCell.setAsSwitch(expandableRows);
                switchCell.setIsSelectingItems(fragment.isSelectingItems());
                if (expandableRows.isMainSwitchHidden()) {
                    switchCell.switchView.setVisibility(View.GONE);
                } else {
                    additionalItems.add(switchCell.switchView);
                }
                break;
            case EXPANDABLE_ROWS_CHILD:
                SwitchCell switchCellChild = (SwitchCell) holder.itemView;
                ExpandableRowsChild expandableRowsChild = (ExpandableRowsChild) currentShownItems.get(position);

                ExpandableRowIndex index = fragment.getExpandableIndexes().get(expandableRowsChild.getRefersToId());
                if (index != null) {
                    index.addSecondaryCell(switchCellChild);
                }

                switchCellChild.setAsCheckbox(expandableRowsChild);
                switchCellChild.setIsSelectingItems(fragment.isSelectingItems());
                additionalItems.add(switchCellChild.checkBoxView);
                break;
            case CUSTOM_AI_MODEL:
                CustomAIModelCell customAIModelCell = (CustomAIModelCell) holder.itemView;
                CustomAIModelRow customAIModelRow = (CustomAIModelRow) currentShownItems.get(position);
                customAIModelCell.setData(customAIModelRow.getModelID(), customAIModelRow.hasDivider(), customAIModelRow.getOnShowOptions());
                break;
            default:
                throw new RuntimeException("No view found for " + type);
        }

        for (View view : additionalItems) {
            if (view != null) {
                view.setVisibility(fragment.isSelectingItems() ? View.INVISIBLE : View.VISIBLE);
            }
        }

        additionalItems.clear();
    }

    @Override
    public int getItemCount() {
        return currentShownItems.size();
    }

    @Override
    public boolean isEnabled(RecyclerView.ViewHolder holder) {
        BaseRow item = currentShownItems.get(holder.getAdapterPosition());
        if (item instanceof CustomCellRow v) {
            return v.isEnabled();
        }

        return item.getType().isEnabled();
    }

    @Override
    public int getItemViewType(int position) {
        return currentShownItems.get(position).getType().getAdapterType();
    }
}