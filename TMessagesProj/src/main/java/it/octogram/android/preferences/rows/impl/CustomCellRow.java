/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.preferences.rows.impl;

import android.app.Activity;
import android.view.View;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;

import it.octogram.android.ConfigProperty;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.BaseRowBuilder;
import it.octogram.android.preferences.rows.Clickable;

public class CustomCellRow extends BaseRow implements Clickable {

    private final Runnable onClick;
    private final View layout;

    private CustomCellRow(@Nullable CharSequence title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean showIfReverse, boolean divider, Runnable onClick, View layout) {
        super(title, summary, requiresRestart, showIf, showIfReverse, divider, PreferenceType.CUSTOM);
        this.onClick = onClick;
        this.layout = layout;
    }

    @Override
    public boolean onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y) {
        if (onClick != null)
            onClick.run();
        return true;
    }

    public View getLayout() {
        return layout;
    }

    public static class CustomCellRowBuilder extends BaseRowBuilder<CustomCellRow> {

        private Runnable onClick;
        private View layout;

        public CustomCellRowBuilder layout(View layout) {
            this.layout = layout;
            return this;
        }

        public CustomCellRowBuilder onClick(Runnable onClick) {
            this.onClick = onClick;
            return this;
        }

        @Override
        public CustomCellRow build() {
            return new CustomCellRow(title, description, requiresRestart, showIf, showIfReverse, divider, onClick, layout);
        }
    }
}
