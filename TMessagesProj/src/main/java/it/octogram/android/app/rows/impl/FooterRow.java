/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.rows.impl;

import android.app.Activity;
import android.view.View;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;

import it.octogram.android.app.PreferenceType;
import it.octogram.android.app.rows.BaseRow;
import it.octogram.android.app.rows.BaseRowBuilder;
import it.octogram.android.app.rows.Clickable;

public class FooterRow extends BaseRow implements Clickable {

    private final Runnable onClick;

    private FooterRow(@Nullable CharSequence title, Runnable onClick) {
        super(title, PreferenceType.FOOTER);
        this.onClick = onClick;
    }

    @Override
    public boolean onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y) {
        if (onClick != null)
            onClick.run();
        return true;
    }

    public static class FooterRowBuilder extends BaseRowBuilder<FooterRow> {
        private Runnable onClick;

        public FooterRowBuilder onClick(Runnable onClick) {
            this.onClick = onClick;
            return this;
        }

        public FooterRow build() {
            return new FooterRow(title, onClick);
        }
    }
}
