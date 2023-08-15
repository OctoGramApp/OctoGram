/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences.rows.impl;

import android.app.Activity;
import android.view.View;
import androidx.annotation.Nullable;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.BaseRowBuilder;
import it.octogram.android.preferences.rows.Clickable;
import org.telegram.ui.ActionBar.BaseFragment;

public class FooterInformativeRow extends BaseRow implements Clickable {

    private final Runnable onClick;

    private FooterInformativeRow(@Nullable String title, Runnable onClick) {
        super(title, PreferenceType.FOOTER_INFORMATIVE);
        this.onClick = onClick;
    }

    @Override
    public boolean onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y) {
        if (onClick != null)
            onClick.run();
        return true;
    }

    public static class FooterInformativeRowBuilder extends BaseRowBuilder<FooterInformativeRow> {
        private Runnable onClick;

        public FooterInformativeRowBuilder onClick(Runnable onClick) {
            this.onClick = onClick;
            return this;
        }

        public FooterInformativeRow build() {
            return new FooterInformativeRow(title, onClick);
        }
    }
}
