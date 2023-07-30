package it.octogram.android.preferences.rows;

import androidx.annotation.Nullable;

import it.octogram.android.ConfigProperty;
import it.octogram.android.preferences.PreferenceType;

public abstract class BaseRow {

    @Nullable
    private final String title;
    @Nullable
    private final String summary;
    private final boolean requiresRestart;
    private final ConfigProperty<Boolean> showIfPreferenceValue;
    private boolean divider;
    private final PreferenceType type;

    private boolean currentlyHidden;
    private int row;

    public BaseRow(PreferenceType type) {
        this(null, null, false, null, true, type);
    }

    public BaseRow(@Nullable String title, PreferenceType type) {
        this(title, null, false, null, true, type);
    }

    public BaseRow(@Nullable String title, @Nullable String summary, PreferenceType type) {
        this(title, summary, false, null, true, type);
    }

    public BaseRow(@Nullable String title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, PreferenceType type) {
        this(title, summary, requiresRestart, showIf, true, type);
    }

    public BaseRow(@Nullable String title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean divider, PreferenceType type) {
        this.title = title;
        this.summary = summary;
        this.requiresRestart = requiresRestart;
        this.showIfPreferenceValue = showIf;
        this.divider = divider;
        this.type = type;

        this.currentlyHidden = showIfPreferenceValue != null && !showIfPreferenceValue.getValue();
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

    public boolean hasDivider() {
        return divider;
    }

    public PreferenceType getType() {
        return type;
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
}
