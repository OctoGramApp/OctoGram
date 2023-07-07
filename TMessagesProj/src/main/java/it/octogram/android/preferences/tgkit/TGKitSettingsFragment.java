/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */
package it.octogram.android.preferences.tgkit;

import static androidx.recyclerview.widget.LinearLayoutManager.VERTICAL;

import android.content.Context;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

import it.octogram.android.preferences.BasePreferencesEntry;
import it.octogram.android.preferences.tgkit.cells.StickerSliderCell;
import it.octogram.android.preferences.tgkit.preference.OctoPreferences;
import it.octogram.android.preferences.tgkit.preference.TGKitPreference;
import it.octogram.android.preferences.tgkit.preference.types.TGKitFooterRow;
import it.octogram.android.preferences.tgkit.preference.types.TGKitListPreference;
import it.octogram.android.preferences.tgkit.preference.types.TGKitSettingsCellRow;
import it.octogram.android.preferences.tgkit.preference.types.TGKitSliderPreference;
import it.octogram.android.preferences.tgkit.preference.types.TGKitStickerHeaderRow;
import it.octogram.android.preferences.tgkit.preference.types.TGKitSwitchPreference;
import it.octogram.android.preferences.tgkit.preference.types.TGKitTextDetailRow;
import it.octogram.android.preferences.tgkit.preference.types.TGKitTextIconRow;

public class TGKitSettingsFragment extends BaseFragment {
    private final OctoPreferences settings;
    private final SparseArray<TGKitPreference> positions = new SparseArray<>();
    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private int rowCount;

    public TGKitSettingsFragment(BasePreferencesEntry entry, Context context) {
        super();
        this.settings = entry.getProcessedPrefs(context);
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        rowCount = 0;

        for (TGKitPreference category : settings.getPreferences()) {
            positions.put(rowCount++, category);
        }

        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(settings.getName());
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setAllowOverlayTitle(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(new LinearLayoutManager(context, VERTICAL, false));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener((view, position, x, y) -> {
            TGKitPreference pref = positions.get(position);
            if (pref instanceof TGKitSwitchPreference) {
                ((TGKitSwitchPreference) pref).contract.toggleValue();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(((TGKitSwitchPreference) pref).contract.getPreferenceValue());
                }
            } else if (pref instanceof TGKitTextIconRow) {
                TGKitTextIconRow preference = ((TGKitTextIconRow) pref);
                if (preference.listener != null) preference.listener.onClick(this);
            } else if (pref instanceof TGKitTextDetailRow) {
                TGKitTextDetailRow preference = ((TGKitTextDetailRow) pref);
                if (preference.listener != null) preference.listener.onClick(this);
            } else if (pref instanceof TGKitFooterRow) {
                TGKitFooterRow preference = ((TGKitFooterRow) pref);
                if (preference.listener != null) preference.listener.onClick(this);
            } else if (pref instanceof TGKitSettingsCellRow) {
                TGKitSettingsCellRow preference = ((TGKitSettingsCellRow) pref);
                if (preference.listener != null) preference.listener.onClick(this);
            } else if (pref instanceof TGKitListPreference) {
                TGKitListPreference preference = ((TGKitListPreference) pref);
                preference.callAction(this, getParentActivity(), view, x, y, () -> {
                    if (view instanceof TextDetailSettingsCell)
                        ((TextDetailSettingsCell) view).setTextAndValue(preference.title, preference.getContract().getValue(), preference.getDivider());
                });
            }
        });

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{TextSettingsCell.class, TextCheckCell.class, HeaderCell.class, NotificationsCheckCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4));

        return themeDescriptions;
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;

        ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0: {
                    holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                }
                case 1: {
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    textCell.setCanDisable(false);

                    TGKitSettingsCellRow pref = (TGKitSettingsCellRow) positions.get(position);
                    textCell.setTextColor(pref.textColor);
                    textCell.setText(pref.title, pref.divider);

                    break;
                }
                case 2: {
                    ((HeaderCell) holder.itemView).setText(positions.get(position).title);
                    break;
                }
                case 3: {
                    TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                    TGKitSwitchPreference pref = (TGKitSwitchPreference) positions.get(position);
                    if (pref.summary != null) {
                        checkCell.setTextAndValueAndCheck(pref.title, pref.summary, pref.contract.getPreferenceValue(), true, pref.divider);
                    } else {
                        checkCell.setTextAndCheck(pref.title, pref.contract.getPreferenceValue(), pref.divider);
                    }
                    break;
                }
                case 4: {
                    ((TGKitTextDetailRow) positions.get(position)).bindCell((TextDetailSettingsCell) holder.itemView);
                    TextDetailSettingsCell settingsCell = (TextDetailSettingsCell) holder.itemView;
                    settingsCell.setMultilineDetail(true);
                    ((TGKitTextDetailRow) positions.get(position)).bindCell(settingsCell);
                    break;
                }
                case 5: {
                    TextCell cell = (TextCell) holder.itemView;
                    ((TGKitTextIconRow) positions.get(position)).bindCell(cell);
                    break;
                }
                case 6: {
                    ((StickerSliderCell) holder.itemView).setContract(((TGKitSliderPreference) positions.get(position)).contract);
                    break;
                }
                case 7: {
                    TextDetailSettingsCell settingsCell = (TextDetailSettingsCell) holder.itemView;
                    TGKitListPreference pref = (TGKitListPreference) positions.get(position);
                    settingsCell.setMultilineDetail(true);
                    settingsCell.setTextAndValue(pref.title, pref.getContract().getValue(), pref.getDivider());
                    break;
                }
                case 8:
                case 14: {
                    ((TextInfoPrivacyCell) holder.itemView).setText(positions.get(position).title);
                    break;
                }
                case 15: {
                    TGKitStickerHeaderRow pref = (TGKitStickerHeaderRow) positions.get(position);
                    LinearLayout linearLayout = (LinearLayout) holder.itemView;

                    linearLayout.removeAllViews();

                    linearLayout.addView(pref.getStickerView(), LayoutHelper.createLinear(120, 120, Gravity.CENTER_HORIZONTAL, 0, 20, 0, 0));

                    if (pref.getDescription() != null) {
                        TextView textView = new TextView(mContext);
                        textView.setGravity(Gravity.CENTER_HORIZONTAL);
                        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                        textView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
                        textView.setHighlightColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkSelection));
                        textView.setText(pref.getDescription());
                        textView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
                        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 36, 26, 36, 0));
                    }
                }
            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            int viewType = holder.getItemViewType();
            if (viewType == 3) {
                int position = holder.getAdapterPosition();
                TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                checkCell.setChecked(((TGKitSwitchPreference) positions.get(position)).contract.getPreferenceValue());
            } else if (viewType == 7) {
                int position = holder.getAdapterPosition();
                TextDetailSettingsCell checkCell = (TextDetailSettingsCell) holder.itemView;
                TGKitListPreference pref = ((TGKitListPreference) positions.get(position));
                checkCell.setTextAndValue(pref.title, pref.getContract().getValue(), pref.getDivider());
            }
        }

        public boolean isRowEnabled(int position) {
            return positions.get(position).getType().enabled;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return isRowEnabled(holder.getAdapterPosition());
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case 0:
                    view = new ShadowSectionCell(mContext);
                    break;
                case 1:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 2:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                case 7:
                    view = new TextDetailSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 5:
                    view = new TextCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 6:
                    view = new StickerSliderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 8:
                    view = new TextInfoPrivacyCell(mContext);
                    break;
                case 14:
                    TextInfoPrivacyCell cell = new TextInfoPrivacyCell(mContext, 10);
                    cell.getTextView().setGravity(Gravity.CENTER_HORIZONTAL);
                    cell.getTextView().setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteGrayText3));
                    cell.getTextView().setMovementMethod(null);
                    cell.getTextView().setPadding(0, AndroidUtilities.dp(14), 0, AndroidUtilities.dp(14));
                    view = cell;
                    view.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                    break;
                case 15:
                    LinearLayout layout = new LinearLayout(mContext);
                    layout.setGravity(Gravity.CENTER_HORIZONTAL);
                    layout.setOrientation(VERTICAL);
                    view = layout;
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            if (view == null) {
                throw new RuntimeException("wrong viewType");
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            return positions.get(position).getType().adapterType;
        }
    }
}