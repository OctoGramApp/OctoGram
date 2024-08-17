package it.octogram.android.utils;

import it.octogram.android.ConfigProperty;

public class ExpandableRowsOption {
    public ConfigProperty<Boolean> property;
    public String optionTitle;
    public Runnable onClick;

    public ExpandableRowsOption setProperty(ConfigProperty<Boolean> property) {
        this.property = property;
        return this;
    }

    public ExpandableRowsOption setOptionTitle(String optionTitle) {
        this.optionTitle = optionTitle;
        return this;
    }

    public ExpandableRowsOption setOnClick(Runnable onClick) {
        this.onClick = onClick;
        return this;
    }
}
