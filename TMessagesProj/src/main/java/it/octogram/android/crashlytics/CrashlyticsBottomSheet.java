/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.crashlytics;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.io.IOException;

public class CrashlyticsBottomSheet extends BottomSheet {

    private static boolean shown = false;

    private CrashlyticsBottomSheet(BaseFragment fragment) {
        super(fragment.getParentActivity(), false);

        Activity activity = fragment.getParentActivity();
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        StickerImageView imageView = new StickerImageView(activity, currentAccount);
        imageView.setStickerPackName("UtyaDuck");
        imageView.setStickerNum(5);
        imageView.getImageReceiver().setAutoRepeat(1);
        linearLayout.addView(imageView, LayoutHelper.createLinear(144, 144, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 16));

        TextView textView = new TextView(activity);
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        textView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(LocaleController.getString(R.string.OctoCrashedTitle));
        textView.setPadding(AndroidUtilities.dp(30), 0, AndroidUtilities.dp(30), 0);
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        textView = new TextView(activity);
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(R.string.OctoCrashedSubTitle);
        textView.setPadding(AndroidUtilities.dp(30), AndroidUtilities.dp(10), AndroidUtilities.dp(30), AndroidUtilities.dp(21));
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        TextView buttonTextView = new TextView(activity);
        buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        buttonTextView.setText(R.string.SendLogsButton);
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundWhite), 120)));
        buttonTextView.setOnClickListener(view -> {
            if (!sendLogs(activity)) {
                // Show popup error
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(LocaleController.getString(R.string.Warning));
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), (dialog, which) -> {
                    dialog.dismiss();
                    dismiss();
                });
                builder.setMessage(LocaleController.getString(R.string.ErrorSendingCrashContent));
                fragment.showDialog(builder.create());
            }
        });
        linearLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 15, 16, 8));

        textView = new TextView(activity);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setText(R.string.CancelArchiveButton);
        textView.setTextColor(Theme.getColor(Theme.key_color_red));
        textView.setOnClickListener(view -> {
            Crashlytics.archiveLatestCrash();
            dismiss();
        });
        linearLayout.addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 0, 16, 0));

        setCustomView(linearLayout);
        setCancelable(false);
    }

    private boolean sendLogs(Activity activity) {
        String crashText = "App Version: " + BuildConfig.BUILD_VERSION_STRING + " (" + BuildVars.BUILD_VERSION + ")\n" +
                "#crash\n" +
                "Crash description: Please describe what you were doing when the crash happened.\n" +
                "How to reproduce: Please describe how to reproduce the crash.\n";
        try {
            File cacheFile = Crashlytics.shareLogs();
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(activity, ApplicationLoader.getApplicationId() + ".provider", cacheFile);
            } else {
                uri = Uri.fromFile(cacheFile);
            }
            Intent i = new Intent(Intent.ACTION_SEND);
            if (Build.VERSION.SDK_INT >= 24) {
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_SUBJECT, crashText);
            i.putExtra(Intent.EXTRA_STREAM, uri);
            i.setClass(activity, LaunchActivity.class);
            activity.startActivity(i);
            dismiss();
            return true;
        } catch (IOException e) {
            Log.e("OctoCrashlytics", "Error sending crash content", e);
            return false;
        }
    }

    public static void showCrash(BaseFragment fragment) {
        if (shown) {
            return;
        }
        CrashlyticsBottomSheet bottomSheet = new CrashlyticsBottomSheet(fragment);
        bottomSheet.show();
        shown = true;
    }

    @Override
    public void show() {
        if (shown) {
            return;
        }
        shown = true;
        super.show();
    }
}
