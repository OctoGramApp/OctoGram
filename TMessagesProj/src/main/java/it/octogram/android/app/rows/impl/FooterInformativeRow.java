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

import it.octogram.android.ConfigProperty;
import it.octogram.android.app.PreferenceType;
import it.octogram.android.app.rows.BaseRow;
import it.octogram.android.app.rows.BaseRowBuilder;
import it.octogram.android.app.rows.Clickable;

public class FooterInformativeRow extends BaseRow implements Clickable {

    private final Runnable onClick;
    private final OnDynamicDataUpdate dynamicDataUpdate;

    public interface OnDynamicDataUpdate {
        String getTitle();
    }

    private FooterInformativeRow(@Nullable CharSequence title, Runnable onClick, ConfigProperty<Boolean> showIf, boolean showIfReverse, OnDynamicDataUpdate dynamicDataUpdate) {
        super(title, null, false, showIf, showIfReverse, PreferenceType.FOOTER_INFORMATIVE);
        this.dynamicDataUpdate = dynamicDataUpdate;
        this.onClick = onClick;
    }

    @Nullable
    public CharSequence getDynamicTitle() {
        if (dynamicDataUpdate != null) {
            setTitle(dynamicDataUpdate.getTitle());
        }

        return super.getTitle();
    }

    @Override
    public boolean onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y) {
        if (onClick != null)
            onClick.run();
        return true;
    }

    public static class FooterInformativeRowBuilder extends BaseRowBuilder<FooterInformativeRow> {
        private Runnable onClick;
        private OnDynamicDataUpdate dynamicDataUpdate;

        public FooterInformativeRowBuilder onClick(Runnable onClick) {
            this.onClick = onClick;
            return this;
        }


        public FooterInformativeRowBuilder setDynamicDataUpdate(OnDynamicDataUpdate dynamicDataUpdate) {
            this.dynamicDataUpdate = dynamicDataUpdate;
            return this;
        }

        public FooterInformativeRow build() {
            return new FooterInformativeRow(title, onClick, showIf, showIfReverse, dynamicDataUpdate);
        }
    }
}
