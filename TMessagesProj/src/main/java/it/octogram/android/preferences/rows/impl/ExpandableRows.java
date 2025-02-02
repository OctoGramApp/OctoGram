/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.rows.impl;

import android.app.Activity;
import android.view.View;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;

import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.BaseRowBuilder;
import it.octogram.android.preferences.rows.Clickable;
import it.octogram.android.utils.ExpandableRowsOption;

public class ExpandableRows extends BaseRow implements Clickable {
    private final int id;
    private final int icon;
    private final CharSequence mainItemTitle;
    private final ArrayList<ExpandableRowsOption> itemsList;
    private final Runnable onSingleStateChange;

    private ExpandableRows(int id, int icon, @Nullable CharSequence title, ArrayList<ExpandableRowsOption> itemsList, Runnable onSingleStateChange) {
        super(PreferenceType.EXPANDABLE_ROWS);
        this.id = id;
        this.icon = icon;
        this.mainItemTitle = title;
        this.itemsList = itemsList;
        this.onSingleStateChange = onSingleStateChange;
    }

    public int getId() {
        return id;
    }

    public int getIcon() {
        return icon;
    }

    public CharSequence getMainItemTitle() {
        return mainItemTitle;
    }

    public ArrayList<ExpandableRowsOption> getItemsList() {
        return itemsList;
    }

    public Runnable getOnSingleStateChange() {
        return onSingleStateChange;
    }

    @Override
    public boolean onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y) {
        return true;
    }

    public static class ExpandableRowsBuilder extends BaseRowBuilder<ExpandableRows> {
        private int id;
        private int icon;
        private CharSequence mainItemTitle;
        private final ArrayList<ExpandableRowsOption> itemsList = new ArrayList<>();
        private Runnable onSingleStateChange;

        public ExpandableRowsBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public ExpandableRowsBuilder setIcon(int icon) {
            this.icon = icon;
            return this;
        }

        public ExpandableRowsBuilder setMainTitle(CharSequence title) {
            mainItemTitle = title;
            return this;
        }

        public ExpandableRowsBuilder addRow(ExpandableRowsOption item) {
            itemsList.add(item);
            return this;
        }

        public ExpandableRowsBuilder setOnSingleStateChange(Runnable run) {
            onSingleStateChange = run;
            return this;
        }

        public ExpandableRows build() {
            return new ExpandableRows(id, icon, mainItemTitle, itemsList, onSingleStateChange);
        }
    }
}
