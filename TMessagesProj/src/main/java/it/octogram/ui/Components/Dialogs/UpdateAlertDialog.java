package it.octogram.ui.Components.Dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

import it.octogram.android.OctoConfig;
import it.octogram.android.http.FileDownloader;
import it.octogram.android.magic.OWLENC;
import it.octogram.android.updates.UpdateManager;

public class UpdateAlertDialog extends BottomSheet {

    OWLENC.UpdateAvailable update;
    private final LaunchActivity parentActivity;

    public UpdateAlertDialog(LaunchActivity activity, OWLENC.UpdateAvailable updateAvailable) {
        super(activity, false);
        update = updateAvailable;
        parentActivity = activity;
        int colorText = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText);

        FrameLayout frameLayout = new FrameLayout(activity);
        BackupImageView backupImageView = new BackupImageView(parentActivity) {
            @SuppressLint("DrawAllocation")
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                int colorBackground = Theme.getColor(Theme.key_dialogBackground);
                GradientDrawable gd1 = new GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[]{colorBackground, AndroidUtilities.getTransparentColor(colorBackground, 0)});
                gd1.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
                gd1.draw(canvas);
                GradientDrawable gd2 = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{colorBackground, AndroidUtilities.getTransparentColor(colorBackground, 0)});
                gd2.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
                gd2.draw(canvas);
                Paint paint = new Paint();
                paint.setColor(AndroidUtilities.getTransparentColor(colorBackground, 0.3F));
                canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), paint);
                Drawable mask = parentActivity.getResources().getDrawable(R.drawable.sheet_shadow_round).mutate();
                ((NinePatchDrawable) mask).getPaint().setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                mask.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
                mask.draw(canvas);
            }
        };
        backupImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        backupImageView.setImage(update.banner, null, null);
        frameLayout.addView(backupImageView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 270));

        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(backgroundPaddingLeft, AndroidUtilities.dp(8) + backgroundPaddingTop - 1, backgroundPaddingLeft, AndroidUtilities.dp(8));
        frameLayout.addView(linearLayout);

        LinearLayout linearLayout2 = new LinearLayout(activity);
        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams3.setMargins(AndroidUtilities.dp(25), 0, AndroidUtilities.dp(75), 0);
        linearLayout2.setLayoutParams(layoutParams3);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);

        TextView updateTitle = new TextView(activity);
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams4.setMargins(0, AndroidUtilities.dp(25), 0, 0);
        updateTitle.setLayoutParams(layoutParams4);
        updateTitle.setTextColor(colorText);
        updateTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);
        setTextEntities(updateTitle, updateAvailable.title);
        linearLayout2.addView(updateTitle);

        TextView descMessage = new TextView(activity);
        LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams5.setMargins(0, AndroidUtilities.dp(10), 0, 0);
        descMessage.setLayoutParams(layoutParams5);
        descMessage.setTextColor(colorText);
        descMessage.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
        descMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        descMessage.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
        descMessage.setGravity(Gravity.LEFT | Gravity.TOP);
        descMessage.setLineSpacing(AndroidUtilities.dp(2), 1.0f);
        setTextEntities(descMessage, updateAvailable.description);
        linearLayout2.addView(descMessage);

        TextView note = new TextView(activity);
        LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams6.setMargins(0, AndroidUtilities.dp(20), 0, 0);
        note.setLayoutParams(layoutParams6);
        note.setTextColor(colorText);
        note.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
        note.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        note.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
        note.setGravity(Gravity.LEFT | Gravity.TOP);
        note.setLineSpacing(AndroidUtilities.dp(2), 1.0f);
        setTextEntities(note, "<b>" + LocaleController.getString("UpdateNote", R.string.UpdateNote) + "</b> " + updateAvailable.note);
        linearLayout2.addView(note);

        linearLayout.addView(linearLayout2);

        BottomSheetCell doneButton = new BottomSheetCell(activity, false);
        doneButton.setText(LocaleController.formatString("AppUpdateDownloadNow", R.string.AppUpdateDownloadNow), false);
        doneButton.background.setOnClickListener(v -> {
            if (FileDownloader.downloadFile(activity, "appUpdate", UpdateManager.apkFile(), updateAvailable.fileLink))
                OctoConfig.saveOldVersion(updateAvailable.version);
            dismiss();
        });
        linearLayout.addView(doneButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50, 0, AndroidUtilities.dp(8), 0, 0));

        BottomSheetCell scheduleButton = new BottomSheetCell(activity, true);
        scheduleButton.setText(LocaleController.getString("AppUpdateRemindMeLater", R.string.AppUpdateRemindMeLater), false);
        scheduleButton.background.setOnClickListener(v -> {
            OctoConfig.remindUpdate(updateAvailable.version);
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
            dismiss();
        });
        linearLayout.addView(scheduleButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50));

        ScrollView scrollView = new ScrollView(activity);
        scrollView.addView(frameLayout);
        setCustomView(scrollView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        containerView.setPadding(0, 0, 0, containerView.getPaddingBottom());
    }

    private void setTextEntities(TextView tv, String text) {
        text = text.replace("\n", "<br>");
        tv.setText(AndroidUtilities.fromHtml(text));
    }

    private static class BottomSheetCell extends FrameLayout {

        private final View background;
        private final TextView[] textView = new TextView[2];
        private final boolean hasBackground;

        public BottomSheetCell(Context context, boolean withoutBackground) {
            super(context);

            hasBackground = !withoutBackground;
            setBackground(null);

            background = new View(context);
            if (hasBackground) {
                background.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
            }
            addView(background, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, 0, 16, withoutBackground ? 0 : 16, 16, 16));

            for (int a = 0; a < 2; a++) {
                textView[a] = new TextView(context);
                textView[a].setLines(1);
                textView[a].setSingleLine(true);
                textView[a].setGravity(Gravity.CENTER_HORIZONTAL);
                textView[a].setEllipsize(TextUtils.TruncateAt.END);
                textView[a].setGravity(Gravity.CENTER);
                if (hasBackground) {
                    textView[a].setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
                    textView[a].setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                } else {
                    textView[a].setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
                }
                textView[a].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                textView[a].setPadding(0, 0, 0, hasBackground ? 0 : AndroidUtilities.dp(13));
                addView(textView[a], LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
                if (a == 1) {
                    textView[a].setAlpha(0.0f);
                }
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(hasBackground ? 80 : 50), MeasureSpec.EXACTLY));
        }

        public void setText(CharSequence text, boolean animated) {
            if (!animated) {
                textView[0].setText(text);
            } else {
                textView[1].setText(text);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.setDuration(180);
                animatorSet.setInterpolator(CubicBezierInterpolator.EASE_OUT);
                animatorSet.playTogether(
                        ObjectAnimator.ofFloat(textView[0], View.ALPHA, 1.0f, 0.0f),
                        ObjectAnimator.ofFloat(textView[0], View.TRANSLATION_Y, 0, -AndroidUtilities.dp(10)),
                        ObjectAnimator.ofFloat(textView[1], View.ALPHA, 0.0f, 1.0f),
                        ObjectAnimator.ofFloat(textView[1], View.TRANSLATION_Y, AndroidUtilities.dp(10), 0)
                );
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        TextView temp = textView[0];
                        textView[0] = textView[1];
                        textView[1] = temp;
                    }
                });
                animatorSet.start();
            }
        }
    }
}
