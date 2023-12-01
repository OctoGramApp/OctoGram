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

public class DrawerElementsSettingsUI implements PreferencesEntry {
    @Override
    public OctoPreferences getPreferences(PreferencesFragment fragment, Context context) {
        return OctoPreferences.builder(LocaleController.getString("DrawerElements", R.string.DrawerElements))
                .category(LocaleController.getString("DrawerElements", R.string.DrawerElements), category -> {
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.drawerChangeStatus)
                            .title(LocaleController.getString(R.string.SetEmojiStatus))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.drawerMyStories)
                            .title(LocaleController.getString(R.string.ProfileMyStories))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.drawerNewGroup)
                            .title(LocaleController.getString(R.string.NewGroup))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.drawerNewChannel)
                            .title(LocaleController.getString(R.string.NewChannel))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.drawerContacts)
                            .title(LocaleController.getString(R.string.Contacts))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.drawerCalls)
                            .title(LocaleController.getString(R.string.Calls))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.drawerPeopleNearby)
                            .title(LocaleController.getString(R.string.PeopleNearby))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.drawerSavedMessages)
                            .title(LocaleController.getString(R.string.SavedMessages))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.drawerOctogramSettings)
                            .title(String.format("%s Settings", LocaleController.getString(R.string.AppName)))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.drawerDatacenterInfo)
                            .title(LocaleController.getString(R.string.DatacenterStatus))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.drawerInviteFriends)
                            .title(LocaleController.getString(R.string.InviteFriends))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.drawerTelegramFeatures)
                            .title(LocaleController.getString(R.string.TelegramFeatures))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                }).build();
    }
}
