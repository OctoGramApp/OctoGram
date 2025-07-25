/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */
package it.octogram.android.app.ui.cells;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;

import android.content.Context;
import android.graphics.Canvas;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CheckBox2;
import org.telegram.ui.Components.LayoutHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import it.octogram.android.OctoConfig;
import it.octogram.android.crashlytics.CrashViewType;

public class SingleCrashLogPreviewCell extends LinearLayout {

    private final LinearLayout linearLayout;
    private final TextView textView;
    private final TextView crashDateTextView;
    private final CheckBox2 checkBox;

    private boolean needDivider = false;
    private File crashLog;

    public SingleCrashLogPreviewCell(@NonNull Context context) {
        super(context);
        setWillNotDraw(false);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(dp(13), dp(5), dp(13), dp(5));
        RelativeLayout relativeLayout = new RelativeLayout(context);

        var imageView = new AppCompatImageView(context);
        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bot_file));

        checkBox = new CheckBox2(context, 21);
        checkBox.setColor(-1, Theme.key_windowBackgroundWhite, Theme.key_checkboxCheck);
        checkBox.setDrawUnchecked(false);
        checkBox.setDrawBackgroundAsArc(3);
        relativeLayout.addView(checkBox, LayoutHelper.createFrame(24, 24, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 0 : 40, 40, LocaleController.isRTL ? 39 : 0, 0));

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

    public void setData(File crashLog, CrashViewType viewType, boolean divider) {
        this.crashLog = crashLog;
        linearLayout.setVisibility(VISIBLE);

        textView.setText(crashLog.getName());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH);
        String date = dateFormat.format(crashLog.lastModified());

        int dateStringResId = (viewType == CrashViewType.CRASH_LOGS)
                ? R.string.CrashedOnDate
                : R.string.DebugLogsGeneratedOnDate;

        crashDateTextView.setText(formatString(dateStringResId, date));


        needDivider = divider;
        setWillNotDraw(!needDivider);
    }

    public void setSelected(boolean selected, boolean animated) {
        checkBox.setChecked(selected, animated);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider && !OctoConfig.INSTANCE.disableDividers.getValue()) {
            canvas.drawLine(dp(16), getMeasuredHeight() - 1, getMeasuredWidth() - dp(16), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }

    public File getCrashLog() {
        return crashLog;
    }
}
