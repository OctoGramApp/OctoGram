/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.Components.ScaleStateListAnimator;
import org.telegram.ui.Components.spoilers.SpoilersTextView;
import org.telegram.ui.LaunchActivity;

import java.util.Locale;

import it.octogram.android.OctoConfig;
import it.octogram.android.app.OctoPreferences;
import it.octogram.android.app.PreferencesEntry;
import it.octogram.android.app.fragment.PreferencesFragment;
import it.octogram.android.app.rows.impl.CustomCellRow;
import it.octogram.android.app.rows.impl.FooterRow;
import it.octogram.android.app.rows.impl.ShadowRow;
import it.octogram.android.app.rows.impl.TextIconRow;
import it.octogram.android.utils.deeplink.DeepLinkDef;
import it.octogram.android.utils.updater.UpdatesManager;

public class OctoInfoSettingsUI implements PreferencesEntry {
    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        TLRPC.Chat pbetaChatInstance = UpdatesManager.INSTANCE.getPrivateBetaChatInstance();

        return OctoPreferences.builder(getString(R.string.OctoInfoSettingsHeader))
                .deepLink(DeepLinkDef.INFO)
                .octoAnimation(getString(R.string.OctoMainSettingsInfoWorld))
                .row(new CustomCellRow.CustomCellRowBuilder()
                        .layout(getPbetaJoinGroupSuggestion(context, fragment, pbetaChatInstance))
                        .build())
                .row(new ShadowRow())
                .category(getString(R.string.OctoMainSettingsChats), category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> MessagesController.getInstance(fragment.getCurrentAccount()).openByUserName(OctoConfig.MAIN_CHANNEL_TAG, fragment, 1))
                            .value("@"+OctoConfig.MAIN_CHANNEL_TAG)
                            .icon(R.drawable.msg_channel)
                            .title(getString(R.string.OfficialChannel))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> MessagesController.getInstance(fragment.getCurrentAccount()).openByUserName(OctoConfig.MAIN_CHAT_TAG, fragment, 1))
                            .value("@"+OctoConfig.MAIN_CHAT_TAG)
                            .icon(R.drawable.msg_groups)
                            .title(getString(R.string.OfficialGroup))
                            .build());
                })
                .category(getString(R.string.OctoMainSettingsInfo), category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> Browser.openUrl(LaunchActivity.instance, Utilities.uriParseSafe(String.format(Locale.US, "https://github.com/%s/tree/%s", OctoConfig.GITHUB_MAIN_REPO, BuildConfig.GIT_COMMIT_HASH))))
                            .icon(R.drawable.outline_source_white_28)
                            .title(getString(R.string.SourceCode))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> Browser.openUrl(LaunchActivity.instance, Utilities.uriParseSafe(String.format(Locale.US, "https://%s/privacy", OctoConfig.MAIN_DOMAIN))))
                            .icon(R.drawable.msg2_policy)
                            .title(getString(R.string.PrivacyPolicy))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> Browser.openUrl(LaunchActivity.instance, Utilities.uriParseSafe(String.format(Locale.US, "https://github.com/%s/blob/main/LICENSE", OctoConfig.GITHUB_MAIN_REPO))))
                            .icon(R.drawable.msg_report_personal)
                            .title(getString(R.string.CodeLicense))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> Browser.openUrl(LaunchActivity.instance, Utilities.uriParseSafe(String.format(Locale.US, "https://%s/translate", OctoConfig.MAIN_DOMAIN))))
                            .icon(R.drawable.msg_translate)
                            .title(getString(R.string.TranslateOcto))
                            .build());
                })
                .row(new FooterRow.FooterRowBuilder().title(String.format(Locale.US, "OctoGram v%s (%s)", BuildConfig.BUILD_VERSION_STRING, BuildConfig.GIT_COMMIT_HASH)).build())
                .build();
    }

    private LinearLayout getPbetaJoinGroupSuggestion(Context context, PreferencesFragment fragment, TLRPC.Chat pbetaChatInstance) {
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

        TextView textViewButton = new TextView(context);
        textViewButton.setBackground(Theme.AdaptiveRipple.filledRectByKey(Theme.key_featuredStickers_addButton, 8));
        ScaleStateListAnimator.apply(textViewButton, 0.02f, 1.5f);
        textViewButton.setLines(1);
        textViewButton.setSingleLine(true);
        textViewButton.setGravity(Gravity.CENTER_HORIZONTAL);
        textViewButton.setEllipsize(TextUtils.TruncateAt.END);
        textViewButton.setGravity(Gravity.CENTER);
        textViewButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText, resourcesProvider));
        textViewButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textViewButton.setTypeface(AndroidUtilities.bold());
        layout.addView(textViewButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44, 21, 16, 21, 15));

        textView.setText(getString(R.string.ContributeUsingPbeta));
        detailTextView.setText(getString(pbetaChatInstance != null ? R.string.ContributeUsingPbeta_Desc_Already : R.string.ContributeUsingPbeta_Desc));
        textViewButton.setText(getString(pbetaChatInstance != null ? R.string.OfficialPbetaChat : R.string.RequestToJoinGroup));

        textViewButton.setOnClickListener(v -> {
            if (pbetaChatInstance != null) {
                boolean hadToSwitch = false;
                int accountId = UpdatesManager.INSTANCE.getFirstAccountId();
                if (UserConfig.selectedAccount != accountId) {
                    hadToSwitch = true;
                    LaunchActivity.instance.switchToAccount(accountId, true);
                }
                AndroidUtilities.runOnUIThread(() -> MessagesController.getInstance(accountId).openChatOrProfileWith(null, pbetaChatInstance, fragment, 1, false), hadToSwitch ? 1500 : 0);
            } else {
                Browser.openUrl(context, "tg://join?invite=" + OctoConfig.PRIVATE_BETA_GROUP_HASH);
            }
        });

        return layout;
    }
}
