package it.octogram.android.preferences.rows;

import androidx.annotation.Nullable;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.PreferenceType;

public abstract class BaseRow {

    @Nullable
    private final String title;
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

    private boolean currentlyHidden;
    private int row;


    public BaseRow(PreferenceType type, ConfigProperty<Boolean> showIf) {
        this(null, null, false, showIf, false, !OctoConfig.INSTANCE.disableDividers.getValue(), type, false);
    }

    public BaseRow(PreferenceType type) {
        this(null, null, false, null, false, !OctoConfig.INSTANCE.disableDividers.getValue(), type, false);
    }

    public BaseRow(@Nullable String title, PreferenceType type) {
        this(title, null, false, null, false, !OctoConfig.INSTANCE.disableDividers.getValue(), type, false);
    }

    public BaseRow(@Nullable String title, @Nullable String summary, PreferenceType type) {
        this(title, summary, false, null, false, !OctoConfig.INSTANCE.disableDividers.getValue(), type, false);
    }

    public BaseRow(@Nullable String title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean showIfReverse, PreferenceType type) {
        this(title, summary, requiresRestart, showIf, showIfReverse, !OctoConfig.INSTANCE.disableDividers.getValue(), type, false);
    }

    public BaseRow(@Nullable String title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean showIfReverse, boolean divider, PreferenceType type) {
        this(title, summary, requiresRestart, showIf, showIfReverse, divider, type, false);
    }

    public BaseRow(@Nullable String title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean showIfReverse, boolean divider, PreferenceType type, boolean premium) {
        this(title, summary, requiresRestart, showIf, showIfReverse, divider, type, premium, true);
    }

    public BaseRow(@Nullable String title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean showIfReverse, boolean divider, PreferenceType type, boolean premium, boolean autoShowPremiumAlert) {
        this(title, summary, requiresRestart, showIf, showIfReverse, divider, type, premium, autoShowPremiumAlert, (int[]) null);
    }

    public BaseRow(@Nullable String title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean showIfReverse, boolean divider, PreferenceType type, boolean premium, boolean autoShowPremiumAlert, @Nullable int... postNotificationName) {
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

        this.currentlyHidden = showIfPreferenceValue != null && showIfReverse == showIfPreferenceValue.getValue();
    }

    @Nullable
    public String getTitle() {
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

    public boolean isCurrentlyHidden() {
        return currentlyHidden;
    }

    public void setCurrentlyHidden(boolean currentlyHidden) {
        this.currentlyHidden = currentlyHidden;
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
}
