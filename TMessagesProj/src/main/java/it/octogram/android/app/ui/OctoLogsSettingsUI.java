package it.octogram.android.app.ui;

import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.Components.ScaleStateListAnimator;
import org.telegram.ui.Components.spoilers.SpoilersTextView;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.app.OctoPreferences;
import it.octogram.android.app.PreferencesEntry;
import it.octogram.android.app.fragment.PreferencesFragment;
import it.octogram.android.app.rows.impl.CustomCellRow;
import it.octogram.android.app.rows.impl.FooterInformativeRow;
import it.octogram.android.app.rows.impl.ShadowRow;
import it.octogram.android.app.rows.impl.TextDetailRow;
import it.octogram.android.app.rows.impl.TextIconRow;
import it.octogram.android.utils.Crashlytics;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.appearance.MessageStringHelper;

public class OctoLogsSettingsUI implements PreferencesEntry {
    private final ConfigProperty<Boolean> canShowItems = new ConfigProperty<>(null, true);
    private final ConfigProperty<Boolean> showCrashedRecently = new ConfigProperty<>(null, false);
    private boolean isDebugLogs = false;
    private boolean forceCrashedRecentlyPage = false;
    private Runnable onCrashedRecentlyHideRunnable;

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        ArrayList<File> files = isDebugLogs ? OctoLogging.getLogFiles() : Crashlytics.getArchivedCrashFiles();
        canShowItems.updateValue(!files.isEmpty());
        showCrashedRecently.updateValue(forceCrashedRecentlyPage);

        return OctoPreferences.builder(getString(isDebugLogs ? R.string.DebugHistory : R.string.CrashHistory))
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.BROKEN, true, getString(R.string.CrashHistory_Desc))
                .row(new CustomCellRow.CustomCellRowBuilder()
                        .layout(getCrashedRecently(fragment, context))
                        .showIf(showCrashedRecently)
                        .build())
                .row(new ShadowRow(showCrashedRecently))
                .category(getString(R.string.ActionsHeader), category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> {
                                try {
                                    String configuration = Crashlytics.getSystemInfo(false);
                                    AndroidUtilities.addToClipboard(configuration);
                                    BulletinFactory.of(fragment).createCopyBulletin(getString(R.string.DeviceInformationsCopied)).show();
                                } catch (IllegalAccessException e) {
                                    BulletinFactory.of(fragment).createErrorBulletin(getString(R.string.ErrorOccurred)).show();
                                }
                            })
                            .icon(R.drawable.msg_copy)
                            .title(getString(R.string.CopySystemInfo))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .isDanger(true)
                            .onClick(() -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle(getString(R.string.BuildAppName));
                                builder.setMessage(formatString(isDebugLogs ? R.string.DebugsDeleteConfirmation : R.string.CrashesDeleteConfirmation, files.size()));
                                builder.setPositiveButton(getString(R.string.DeleteAll), (v, d) -> {
                                    if (isDebugLogs) {
                                        OctoLogging.deleteLogs();
                                    } else {
                                        Crashlytics.deleteCrashLogs();
                                    }
                                    canShowItems.updateValue(false);
                                    fragment.reloadUIAfterValueUpdate();
                                    BulletinFactory.of(fragment).createSuccessBulletin(formatString(isDebugLogs ? R.string.DebugsDeleted : R.string.CrashesDeleted, files.size())).show();
                                });
                                builder.setNegativeButton(getString(R.string.Cancel), null);
                                builder.show().redPositive();
                            })
                            .icon(R.drawable.msg_delete)
                            .title(getString(R.string.DeleteAll))
                            .showIf(canShowItems)
                            .build());
                })
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(MessageStringHelper.getUrlNoUnderlineText(
                                new SpannableString(
                                        MessageStringHelper.fromHtml(
                                                formatString(
                                                        R.string.CrashHistoryReport,
                                                        "<a href='https://t.me/"+OctoConfig.MAIN_CHAT_TAG+"'>@"+OctoConfig.MAIN_CHAT_TAG+"</a>"
                                                )
                                        )
                                )
                        ))
                        .build())
                .category(getString(R.string.ContextMenuElements), canShowItems, category -> {
                    for (File file : files) {
                        category.row(new TextDetailRow.TextDetailRowBuilder()
                                .onClick(() -> Crashlytics.sendLog(fragment, file))
                                .icon(R.drawable.bot_file)
                                .title(Crashlytics.getFileName(file))
                                .description(file.getName())
                                .showIf(canShowItems)
                                .build());
                    }
                })
                .build();
    }

    private LinearLayout getCrashedRecently(PreferencesFragment fragment, Context context) {
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
            textView2.setText(getString(a == 0 ? R.string.OctoSendLastLogShare : R.string.Hide));
            linearLayout.addView(textView2, LayoutHelper.createLinear(0, 44, 0.5f, a == 0 ? 0 : 4, 0, a == 0 ? 4 : 0, 0));

            int finalA = a;

            textView2.setOnClickListener(v -> AndroidUtilities.runOnUIThread(() -> {
                if (finalA == 0) {
                    Crashlytics.sendLastLogFromDeepLink(fragment);
                } else {
                    Crashlytics.resetPendingCrash();
                    showCrashedRecently.updateValue(false);
                    fragment.reloadUIAfterValueUpdate();

                    if (onCrashedRecentlyHideRunnable != null) {
                        onCrashedRecentlyHideRunnable.run();
                    }
                }
            }));
        }

        textView.setText(getString(R.string.OctoCrashedTitle));
        detailTextView.setText(getString(R.string.OctoCrashedLongDesc));

        return layout;
    }

    public void setDebugLogs(boolean debugLogs) {
        isDebugLogs = debugLogs;
    }

    public void setForceCrashedRecentlyPage(boolean forceCrashedRecentlyPage) {
        this.forceCrashedRecentlyPage = forceCrashedRecentlyPage;
    }

    public void setOnCrashedRecentlyHideRunnable(Runnable onCrashedRecentlyHideRunnable) {
        this.onCrashedRecentlyHideRunnable = onCrashedRecentlyHideRunnable;
    }
}
