/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;

import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.CheckboxRow;

public class ContextElementsSettingsUI implements PreferencesEntry {
    @Override
    public OctoPreferences getPreferences(PreferencesFragment fragment, Context context) {
        return OctoPreferences.builder(LocaleController.getString("ContextElements", R.string.ContextElements))
                .category(LocaleController.getString("ContextElements", R.string.ContextElements), category -> {
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.contextClearFromCache)
                            .title(LocaleController.getString(R.string.ClearFromCache))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.contextCopyPhoto)
                            .title(LocaleController.getString(R.string.CopyPhoto))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.contextSaveMessage)
                            .title(LocaleController.getString(R.string.AddToSavedMessages))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.contextReportMessage)
                            .title(LocaleController.getString(R.string.ReportChat))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.contextMessageDetails)
                            .title(LocaleController.getString(R.string.MessageDetails))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.contextNoQuoteForward)
                            .title(LocaleController.getString(R.string.NoQuoteForward))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                }).build();
    }
}
