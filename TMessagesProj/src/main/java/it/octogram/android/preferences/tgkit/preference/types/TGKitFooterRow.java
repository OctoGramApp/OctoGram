/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */
package it.octogram.android.preferences.tgkit.preference.types;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;

import it.octogram.android.preferences.tgkit.preference.TGKitPreference;

public class TGKitFooterRow extends TGKitPreference {

    public TGKitFooterRow(String text, @Nullable TGTIListener listener) {
        this.title = text;
        this.listener = listener;
    }

    @Nullable
    public TGTIListener listener;

    @Override
    public TGPType getType() {
        return TGPType.FOOTER;
    }

    public interface TGTIListener {
        void onClick(BaseFragment bf);
    }

}
