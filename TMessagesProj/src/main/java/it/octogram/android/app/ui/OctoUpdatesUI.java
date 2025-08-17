/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui;

import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.Components.ScaleStateListAnimator;
import org.telegram.ui.Components.spoilers.SpoilersTextView;
import org.telegram.ui.LaunchActivity;

import java.util.List;
import java.util.Locale;

import it.octogram.android.AutoDownloadUpdate;
import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.app.OctoPreferences;
import it.octogram.android.app.PreferencesEntry;
import it.octogram.android.app.fragment.PreferencesFragment;
import it.octogram.android.app.rows.impl.CustomCellRow;
import it.octogram.android.app.rows.impl.FooterInformativeRow;
import it.octogram.android.app.rows.impl.ListRow;
import it.octogram.android.app.rows.impl.ShadowRow;
import it.octogram.android.app.rows.impl.SwitchRow;
import it.octogram.android.app.ui.cells.UpdatesHeaderCell;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.appearance.PopupChoiceDialogOption;
import it.octogram.android.utils.deeplink.DeepLinkDef;
import it.octogram.android.utils.updater.UpdatesManager;

public class OctoUpdatesUI implements PreferencesEntry {
    private Context context;
    private PreferencesFragment fragment;
    private UpdatesManager.UpdatesManagerCallback updaterCallback1;
    private UpdatesManager.UpdatesManagerCallback updaterCallback2;
    private UpdatesManager.ExtensionUpdateState extensionUpdateState;
    private final static String TAG = "OctoUpdatesUI";

    private final ConfigProperty<Boolean> canShowExtensionUpdateAvailable = new ConfigProperty<>(null, false);

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        this.fragment = fragment;
        this.context = context;

        canShowExtensionUpdateAvailable.updateValue(false);

        TLRPC.Chat pbetaChatInstance = UpdatesManager.INSTANCE.getPrivateBetaChatInstance();
        ConfigProperty<Boolean> isPbetaUser = new ConfigProperty<>(null, pbetaChatInstance != null);

        if (pbetaChatInstance == null && OctoConfig.INSTANCE.receivePBetaUpdates.getValue()) {
            OctoLogging.d(TAG, String.format(Locale.US, "%s: %s LINE: 62", isPbetaUser.getValue(), OctoConfig.INSTANCE.receivePBetaUpdates.getValue()));
            OctoConfig.INSTANCE.receivePBetaUpdates.updateValue(false);
        }

        OctoLogging.d(TAG, String.format(Locale.US, "%s %s: LINE:66", OctoConfig.INSTANCE.receivePBetaUpdates.getValue(), isPbetaUser.getValue()));

        return OctoPreferences.builder(getString(R.string.Updates))
                .deepLink(DeepLinkDef.UPDATE)
                .row(new CustomCellRow.CustomCellRowBuilder()
                        .layout(new UpdatesHeaderCell(context))
                        .avoidReDraw(true)
                        .build())
                .row(new ShadowRow())
                .row(new CustomCellRow.CustomCellRowBuilder()
                        .layout(getExtensionUpdateAvailable())
                        .showIf(canShowExtensionUpdateAvailable)
                        .build())
                .row(new ShadowRow(canShowExtensionUpdateAvailable))
                .category(R.string.UpdatesOptions, category -> {
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.autoDownloadUpdatesStatus)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(AutoDownloadUpdate.ALWAYS.getValue())
                                            .setItemTitle(getString(R.string.UpdatesSettingsAutoDownloadAlways))
                                            .setItemDescription(getString(R.string.UpdatesSettingsAutoDownloadAlwaysDesc)),
                                    new PopupChoiceDialogOption()
                                            .setId(AutoDownloadUpdate.ONLY_ON_WIFI.getValue())
                                            .setItemTitle(getString(R.string.UpdatesSettingsAutoDownloadWifi)),
                                    new PopupChoiceDialogOption()
                                            .setId(AutoDownloadUpdate.NEVER.getValue())
                                            .setItemTitle(getString(R.string.UpdatesSettingsAutoDownloadNever))
                            ))
                            .title(getString(R.string.UpdatesSettingsAutoDownload))
                            .build()
                    );
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.autoCheckUpdateStatus)
                            .title(getString(R.string.UpdatesSettingsAuto))
                            .showIf(OctoConfig.INSTANCE.receivePBetaUpdates, true)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> UpdatesManager.INSTANCE.checkForUpdates())
                            .preferenceValue(OctoConfig.INSTANCE.preferBetaVersion)
                            .title(getString(R.string.UpdatesSettingsBeta))
                            .showIf(OctoConfig.INSTANCE.receivePBetaUpdates, true)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> UpdatesManager.INSTANCE.checkForUpdates())
                            .preferenceValue(OctoConfig.INSTANCE.receivePBetaUpdates)
                            .title(getString(R.string.UpdatesSettingsPBeta))
                            .showIf(isPbetaUser)
                            .build());
                })
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(getString(R.string.UpdatesSettingsAutoDescription))
                        .showIf(isPbetaUser, true)
                        .build())
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(formatString(R.string.UpdatesSettingsPbetaDescription, getString(R.string.UpdatesSettingsAuto).toLowerCase()))
                        .showIf(isPbetaUser)
                        .build())
                .build();
    }

    @Override
    public void onFragmentCreate() {
        UpdatesManager.INSTANCE.addCallback(updaterCallback1 = new UpdatesManager.UpdatesManagerCallback() {
            @Override
            public boolean onGetStateAfterAdd() {
                return true;
            }

            @Override
            public void onExtensionUpdateAvailable(UpdatesManager.ExtensionUpdateState state, boolean hasAlsoClassicUpdate) {
                extensionUpdateState = state;
                boolean wasEnabled = canShowExtensionUpdateAvailable.getValue();
                canShowExtensionUpdateAvailable.updateValue(true);
                if (!wasEnabled) {
                    fragment.reloadUIAfterValueUpdate();
                }
            }

            @Override
            public void onNoExtensionUpdateAvailable(boolean hasAlsoClassicUpdate) {
                extensionUpdateState = null;
                boolean wasEnabled = canShowExtensionUpdateAvailable.getValue();
                canShowExtensionUpdateAvailable.updateValue(false);
                if (wasEnabled) {
                    fragment.reloadUIAfterValueUpdate();
                }
            }
        });

        UpdatesManager.INSTANCE.addCallback(updaterCallback2 = new UpdatesManager.UpdatesManagerCallback() {
            @Override
            public void onNoUpdateAvailable() {
                AndroidUtilities.runOnUIThread(() -> BulletinFactory.of(fragment).createSimpleBulletin(R.raw.done, getString(R.string.UpdatesSettingsCheckUpdated)).show());
            }
        });
    }

    @Override
    public void onFragmentDestroy() {
        if (updaterCallback1 != null) {
            UpdatesManager.INSTANCE.removeCallback(updaterCallback1);
            updaterCallback1 = null;
        }
        if (updaterCallback2 != null) {
            UpdatesManager.INSTANCE.removeCallback(updaterCallback2);
            updaterCallback2 = null;
        }
    }

    private LinearLayout getExtensionUpdateAvailable() {
        Theme.ResourcesProvider resourcesProvider = fragment.getResourceProvider();

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        SpoilersTextView textView = new SpoilersTextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setTypeface(AndroidUtilities.bold());
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader, resourcesProvider));
        layout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 21, 15, 21, 0));

        LinkSpanDrawable.LinksTextView detailTextView = new LinkSpanDrawable.LinksTextView(context, resourcesProvider);
        detailTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
        detailTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        detailTextView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText, resourcesProvider));
        detailTextView.setHighlightColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkSelection, resourcesProvider));
        detailTextView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
        detailTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        layout.addView(detailTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 21, 14, 21, 0));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        layout.addView(linearLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44, 21, 16, 21, 15));

        for (int a = 0; a < 2; a++) {
            TextView textView2 = new TextView(context);
            textView2.setBackground(Theme.AdaptiveRipple.filledRectByKey(Theme.key_featuredStickers_addButton, 8));
            ScaleStateListAnimator.apply(textView2, 0.02f, 1.5f);
            textView2.setLines(1);
            textView2.setSingleLine(true);
            textView2.setGravity(Gravity.CENTER_HORIZONTAL);
            textView2.setEllipsize(TextUtils.TruncateAt.END);
            textView2.setGravity(Gravity.CENTER);
            textView2.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText, resourcesProvider));
            textView2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            textView2.setTypeface(AndroidUtilities.bold());
            textView2.setText(getString(a == 0 ? R.string.UpdatesSettingsCheckButtonUpdate : R.string.Hide));
            linearLayout.addView(textView2, LayoutHelper.createLinear(0, 44, 0.5f, a == 0 ? 0 : 4, 0, a == 0 ? 4 : 0, 0));

            int finalA = a;

            textView2.setOnClickListener(v -> AndroidUtilities.runOnUIThread(() -> {
                if (extensionUpdateState == null) {
                    canShowExtensionUpdateAvailable.updateValue(false);
                    fragment.reloadUIAfterValueUpdate();
                } else {
                    if (finalA == 1) {
                        OctoConfig.INSTANCE.ignoredExtensionUpdateVersion.updateValue(extensionUpdateState.versionCode());
                        UpdatesManager.INSTANCE.checkForUpdates();
                        canShowExtensionUpdateAvailable.updateValue(false);
                        fragment.reloadUIAfterValueUpdate();
                        AndroidUtilities.runOnUIThread(() -> BulletinFactory.of(fragment).createSimpleBulletin(R.raw.done, getString(R.string.UpdatesSettingsExtensionUpdate_Ignored)).show());
                    } else {
                        Browser.openUrl(LaunchActivity.instance, String.format(Locale.US, "https://t.me/%s/%d", extensionUpdateState.channelUsername(), extensionUpdateState.messageID()));
                    }
                }
            }));
        }

        textView.setText(getString(R.string.UpdatesSettingsExtensionUpdate));
        detailTextView.setText(getString(R.string.UpdatesSettingsExtensionUpdate_Desc));

        return layout;
    }
}
