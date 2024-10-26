/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.preferences.ui;

import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.LaunchActivity;

import java.util.Locale;

import it.octogram.android.NewFeaturesBadgeId;
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
        var footer = AndroidUtilities.replaceTags(formatString(R.string.OctoMainSettingsFooter, BuildConfig.BUILD_VERSION_STRING)).toString();
        return OctoPreferences.builder(getString(R.string.OctoGramSettings))
                .octoAnimation(getString(R.string.OctoMainSettingsHeader))
                .category(getString(R.string.Settings), category -> {
                    if ("it.octogram.android.beta".equals(ApplicationLoader.applicationContext.getPackageName())) {
                        category.row(new TextDetailRow.TextDetailRowBuilder()
                                .onClick(() -> {
                                    throw new RuntimeException("Test crash");
                                })
                                .icon(R.drawable.msg_cancel)
                                .title("Crash the app")
                                .description("Yup, this is literally a crash button")
                                .build());
                    }
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoGeneralSettingsUI())))
                            .icon(R.drawable.msg_media)
                            .isNew(NewFeaturesBadgeId.GENERAL_BADGE.getId())
                            .title(getString(R.string.General))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoTranslatorUI())))
                            .icon(R.drawable.msg_translate)
                            .title(getString(R.string.Translator))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoAppearanceUI())))
                            .icon(R.drawable.settings_appearance)
                            .isNew(NewFeaturesBadgeId.APPEARANCE_BADGE.getId())
                            .title(getString(R.string.Appearance))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoCameraSettingsUI())))
                            .icon(R.drawable.msg_camera)
                            .title(getString(R.string.ChatCamera))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoExperimentsUI())))
                            .icon(R.drawable.outline_science_white)
                            .isNew(NewFeaturesBadgeId.EXPERIMENTAL_BADGE.getId())
                            .title(getString(R.string.Experiments))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoUpdatesUI())))
                            .icon(R.drawable.round_update_white_28)
                            .isNew(NewFeaturesBadgeId.UPDATES_BADGE.getId())
                            .title(getString(R.string.Updates))
                            .build());
                })
                .category(getString(R.string.OctoMainSettingsManageCategory), category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> openExportSettingsProcedure(fragment, context))
                            .icon(R.drawable.msg_customize)
                            .title(getString(R.string.Export))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> openResetSettingsProcedure(context))
                            .icon(R.drawable.msg_reset)
                            .title(getString(R.string.ResetSettings))
                            .build());
                })
                .category(getString(R.string.OctoMainSettingsInfoCategory), category -> {
                    category.row(new TextDetailRow.TextDetailRowBuilder()
                            .onClick(() -> fragment.presentFragment(new DatacenterActivity()))
                            .icon(R.drawable.datacenter_status)
                            .title(getString(R.string.DatacenterStatus))
                            .description(getString(R.string.DatacenterStatus_Desc))
                            .build());
                    category.row(new TextDetailRow.TextDetailRowBuilder()
                            .onClick(() -> {
                                LogsMigrator.migrateOldLogs();
                                fragment.presentFragment(new CrashesActivity());
                            })
                            .icon(R.drawable.msg_secret_hw)
                            .title(getString(R.string.CrashHistory))
                            .description(getString(R.string.CrashHistory_Desc))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> Browser.openUrl(LaunchActivity.instance, Uri.parse(String.format("https://%s/privacy", OctoUtils.getDomain()))))
                            .icon(R.drawable.msg2_policy)
                            .title(getString(R.string.OctoPrivacyPolicy))
                            .build());
                    category.row(new TextDetailRow.TextDetailRowBuilder()
                            .onClick(() -> Browser.openUrl(LaunchActivity.instance, Uri.parse(String.format("https://translations.%s", OctoUtils.getDomain()))))
                            .icon(R.drawable.msg_translate)
                            .title(getString(R.string.TranslateOcto))
                            .description(getString(R.string.TranslateOcto_Desc))
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
        warningBuilder.setTitle(getString(R.string.ResetSettingsTitle));
        warningBuilder.setMessage(getString(R.string.ResetSettingsDescription));
        warningBuilder.setPositiveButton(getString(R.string.ResetSettingsButton), (dialog1, which1) -> {
            OctoConfig.INSTANCE.resetConfig();
            AppRestartHelper.triggerRebirth(context, new Intent(context, LaunchActivity.class));
        });
        warningBuilder.setNeutralButton(getString(R.string.Cancel), null);
        AlertDialog dialog = warningBuilder.create();

        dialog.setOnShowListener(dialog1 -> {
            TextView positiveButton = (TextView) dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            String defaultText = (String) positiveButton.getText();
            positiveButton.setEnabled(false);

            new CountDownTimer(10000, 1000) {
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
