/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import it.octogram.android.OctoConfig;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CrashLogCell extends LinearLayout {

    private final LinearLayout linearLayout;
    private final TextView textView;
    private final TextView crashDateTextView;

    private boolean needDivider = false;
    private File crashLog;

    public CrashLogCell(@NonNull Context context) {
        super(context);
        setWillNotDraw(false);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(AndroidUtilities.dp(13), AndroidUtilities.dp(5), AndroidUtilities.dp(13), AndroidUtilities.dp(5));
        RelativeLayout relativeLayout = new RelativeLayout(context);

        ImageView imageView = new ImageView(context);
        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bot_file));

        relativeLayout.addView(imageView, LayoutHelper.createRelative(25, 25, RelativeLayout.CENTER_IN_PARENT));
        addView(relativeLayout, LayoutHelper.createLinear(65, 65));

        linearLayout = new LinearLayout(context);
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
        linearLayout.setOrientation(VERTICAL);
        linearLayout.setVisibility(GONE);
        addView(linearLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setTextSize(16);
        linearLayout.addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 10, 0, 0, 0));

        crashDateTextView = new TextView(context);
        crashDateTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        crashDateTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        crashDateTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        crashDateTextView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        linearLayout.addView(crashDateTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 10, 0, 0, 0));
    }

    public void setData(File crashLog, boolean divider) {
        this.crashLog = crashLog;
        linearLayout.setVisibility(VISIBLE);

        textView.setText(crashLog.getName());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH);
        String date = dateFormat.format(crashLog.lastModified());
        crashDateTextView.setText(LocaleController.formatString("CrashedOnDate", R.string.CrashedOnDate, date));

        needDivider = divider;
        setWillNotDraw(!needDivider);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider && !OctoConfig.INSTANCE.disableDividers.getValue()) {
            canvas.drawLine(AndroidUtilities.dp(16), getMeasuredHeight() - 1, getMeasuredWidth() - AndroidUtilities.dp(16), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }

    public File getCrashLog() {
        return crashLog;
    }
}
