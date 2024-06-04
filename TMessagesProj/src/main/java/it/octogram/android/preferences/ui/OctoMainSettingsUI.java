/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.LaunchActivity;

import java.util.Locale;

import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.FooterRow;
import it.octogram.android.preferences.rows.impl.TextDetailRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.preferences.ui.custom.ExportDoneReadyBottomSheet;
import it.octogram.android.utils.AppRestartHelper;
import it.octogram.android.utils.LogsMigrator;
import it.octogram.android.utils.OctoUtils;

public class OctoMainSettingsUI implements PreferencesEntry {

    @Override
    public OctoPreferences getPreferences(PreferencesFragment fragment, Context context) {
        String footer = AndroidUtilities.replaceTags(LocaleController.formatString("OctoMainSettingsFooter", R.string.OctoMainSettingsFooter, BuildConfig.BUILD_VERSION_STRING)).toString();
        String comingSoon = AndroidUtilities.replaceTags(LocaleController.formatString("FeatureCurrentlyUnavailable", R.string.FeatureCurrentlyUnavailable)).toString();
        return OctoPreferences.builder(LocaleController.getString("OctoGramSettings", R.string.OctoGramSettings))
                .sticker(context, R.raw.utyan_robot, true, LocaleController.formatString("OctoMainSettingsHeader", R.string.OctoMainSettingsHeader))
                .category(LocaleController.formatString("Settings", R.string.Settings), category -> {


                    /*if (BuildConfig.DEBUG_PRIVATE_VERSION) {
                        category.row(new TextDetailRow.TextDetailRowBuilder()
                                .onClick(() -> {
                                    throw new RuntimeException("Test crash");
                                })
                                .icon(R.drawable.msg_cancel)
                                .title("Crash the app")
                                .description("Yup, this is literally a crash button")
                                .build());
                    }*/


                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoGeneralSettingsUI())))
                            .icon(R.drawable.msg_media)
                            .title(LocaleController.formatString("General", R.string.General))
                            .build());

                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoTranslatorUI())))
                            .icon(R.drawable.msg_translate)
                            .title(LocaleController.formatString("Translator", R.string.Translator))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoAppearanceUI())))
                            .icon(R.drawable.settings_appearance)
                            .title(LocaleController.formatString("Appearance", R.string.Appearance))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoCameraSettingsUI())))
                            .icon(R.drawable.msg_camera)
                            .title(LocaleController.formatString("ChatCamera", R.string.ChatCamera))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoExperimentsUI())))
                            .icon(R.drawable.outline_science_white)
                            .title(LocaleController.formatString("Experiments", R.string.Experiments))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoUpdatesUI())))
                            .icon(R.drawable.round_update_white_28)
                            .title(LocaleController.formatString("Updates", R.string.Updates))
                            .build());
                })
                .category(LocaleController.formatString("OctoMainSettingsManageCategory", R.string.OctoMainSettingsManageCategory), category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> openExportSettingsProcedure(fragment, context))
                            .icon(R.drawable.msg_customize)
                            .title(LocaleController.formatString("Export", R.string.Export))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> openResetSettingsProcedure(context))
                            .icon(R.drawable.msg_reset)
                            .title(LocaleController.formatString("ResetSettings", R.string.ResetSettings))
                            .build());
                })
                .category(LocaleController.formatString("OctoMainSettingsInfoCategory", R.string.OctoMainSettingsInfoCategory), category -> {
                    category.row(new TextDetailRow.TextDetailRowBuilder()
                            .onClick(() -> fragment.presentFragment(new DatacenterActivity()))
                            .icon(R.drawable.datacenter_status)
                            .title(LocaleController.formatString("DatacenterStatus", R.string.DatacenterStatus))
                            .description(LocaleController.formatString("DatacenterStatus_Desc", R.string.DatacenterStatus_Desc))
                            .build());
                    category.row(new TextDetailRow.TextDetailRowBuilder()
                            .onClick(() -> {
                                LogsMigrator.migrateOldLogs();
                                fragment.presentFragment(new CrashesActivity());
                            })
                            .icon(R.drawable.msg_secret_hw)
                            .title(LocaleController.getString("CrashHistory", R.string.CrashHistory))
                            .description(LocaleController.getString("CrashHistory_Desc", R.string.CrashHistory_Desc))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://%s/privacy", OctoUtils.getDomain())));
                                fragment.getParentActivity().startActivity(browserIntent);
                            })
                            .icon(R.drawable.msg2_policy)
                            .title(LocaleController.formatString("OctoPrivacyPolicy", R.string.OctoPrivacyPolicy))
                            .build());
                    category.row(new TextDetailRow.TextDetailRowBuilder()
                            .onClick(() -> {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://translations.%s", OctoUtils.getDomain())));
                                fragment.getParentActivity().startActivity(browserIntent);
                            })
                            .icon(R.drawable.msg_translate)
                            .title(LocaleController.formatString("TranslateOcto", R.string.TranslateOcto))
                            .description(LocaleController.formatString("TranslateOcto_Desc", R.string.TranslateOcto_Desc))
                            .build());
                })
                .row(new FooterRow.FooterRowBuilder().title(footer).build())
                .build();
    }

    private void openExportSettingsProcedure(PreferencesFragment fragment, Context context) {
        var bottomSheet = new ExportDoneReadyBottomSheet(context, fragment.getParentActivity(), fragment);
        bottomSheet.show();
    }

    private void openResetSettingsProcedure(Context context) {
        AlertDialog.Builder warningBuilder = new AlertDialog.Builder(context);
        warningBuilder.setTitle(LocaleController.getString(R.string.ResetSettingsTitle));
        warningBuilder.setMessage(LocaleController.getString(R.string.ResetSettingsDescription));
        warningBuilder.setPositiveButton(LocaleController.getString("ResetButton", R.string.ResetSettingsButton), (dialog1, which1) -> {
            OctoConfig.INSTANCE.resetConfig();
            AppRestartHelper.triggerRebirth(context, new Intent(context, LaunchActivity.class));
        });
        warningBuilder.setNeutralButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        AlertDialog dialog = warningBuilder.create();

        dialog.setOnShowListener(dialog1 -> {
            TextView positiveButton = (TextView) dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            String defaultText = (String) positiveButton.getText();
            positiveButton.setEnabled(false);

            new CountDownTimer(10000, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                    int currentSeconds = (int) millisUntilFinished / 1000 + 1;
                    positiveButton.setText(String.format(Locale.getDefault(), "%s (%d)", defaultText, currentSeconds));
                }

                @Override
                public void onFinish() {
                    positiveButton.setText(defaultText);
                    positiveButton.setEnabled(true);
                }
            }.start();
        });

        dialog.show();
        dialog.redPositive();
    }
}
