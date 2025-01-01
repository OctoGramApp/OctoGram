/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.rows;

import androidx.annotation.Nullable;

import org.telegram.ui.Components.ListView.AdapterWithDiffUtils;

import java.util.Objects;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.impl.FooterInformativeRow;
import it.octogram.android.preferences.rows.impl.HeaderRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;

public abstract class BaseRow extends AdapterWithDiffUtils.Item {

    @Nullable
    private final CharSequence title;
    @Nullable
    private final String summary;
    private final boolean requiresRestart;
    private final ConfigProperty<Boolean> showIfPreferenceValue;
    private final boolean showIfReverse;
    private boolean divider;
    private final PreferenceType type;
    private final int[] postNotificationName;
    private final boolean premium;
    private final boolean autoShowPremiumAlert;

    private int row;


    public BaseRow(PreferenceType type, ConfigProperty<Boolean> showIf) {
        this(null, null, false, showIf, false, !OctoConfig.INSTANCE.disableDividers.getValue(), type, false);
    }

    public BaseRow(PreferenceType type) {
        this(null, null, false, null, false, !OctoConfig.INSTANCE.disableDividers.getValue(), type, false);
    }

    public BaseRow(@Nullable CharSequence title, PreferenceType type) {
        this(title, null, false, null, false, !OctoConfig.INSTANCE.disableDividers.getValue(), type, false);
    }

    public BaseRow(@Nullable CharSequence title, @Nullable String summary, PreferenceType type) {
        this(title, summary, false, null, false, !OctoConfig.INSTANCE.disableDividers.getValue(), type, false);
    }

    public BaseRow(@Nullable CharSequence title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean showIfReverse, PreferenceType type) {
        this(title, summary, requiresRestart, showIf, showIfReverse, !OctoConfig.INSTANCE.disableDividers.getValue(), type, false);
    }

    public BaseRow(@Nullable CharSequence title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean showIfReverse, boolean divider, PreferenceType type) {
        this(title, summary, requiresRestart, showIf, showIfReverse, divider, type, false);
    }

    public BaseRow(@Nullable CharSequence title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean showIfReverse, boolean divider, PreferenceType type, boolean premium) {
        this(title, summary, requiresRestart, showIf, showIfReverse, divider, type, premium, true);
    }

    public BaseRow(@Nullable CharSequence title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean showIfReverse, boolean divider, PreferenceType type, boolean premium, boolean autoShowPremiumAlert) {
        this(title, summary, requiresRestart, showIf, showIfReverse, divider, type, premium, autoShowPremiumAlert, (int[]) null);
    }

    public BaseRow(@Nullable CharSequence title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean showIfReverse, boolean divider, PreferenceType type, boolean premium, boolean autoShowPremiumAlert, @Nullable int... postNotificationName) {
        super(type.getAdapterType(), false);

        this.title = title;
        this.summary = summary;
        this.requiresRestart = requiresRestart;
        this.showIfPreferenceValue = showIf;
        this.showIfReverse = showIfReverse;
        this.divider = divider;
        this.type = type;
        this.postNotificationName = postNotificationName;
        this.premium = premium;
        this.autoShowPremiumAlert = autoShowPremiumAlert;
    }

    @Nullable
    public CharSequence getTitle() {
        return title;
    }

    @Nullable
    public String getSummary() {
        return summary;
    }

    public boolean doesRequireRestart() {
        return requiresRestart;
    }

    public ConfigProperty<Boolean> getShowIfPreferenceValue() {
        return showIfPreferenceValue;
    }

    public boolean getShowIfReverse() {
        return showIfReverse;
    }

    public boolean getAutoShowPremiumAlert() {
        return autoShowPremiumAlert;
    }

    public boolean hasDivider() {
        return divider;
    }

    public PreferenceType getType() {
        return type;
    }

    public int[] getPostNotificationName() {
        return postNotificationName;
    }

    public void setDivider(boolean divider) {
        this.divider = divider;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public boolean isPremium() {
        return premium;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BaseRow item)) {
            return false;
        }
        if (getType() != item.getType()) {
            return false;
        }
        if (item instanceof HeaderRow || item instanceof SwitchRow || item instanceof FooterInformativeRow) {
            return Objects.equals(item.getTitle(), getTitle());
        }
        return true;
    }
}
