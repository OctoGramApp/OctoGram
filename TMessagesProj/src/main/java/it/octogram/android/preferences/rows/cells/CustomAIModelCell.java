/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.rows.cells;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.ui.Components.LayoutHelper.createLinear;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import org.telegram.messenger.Emoji;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.function.Consumer;

import it.octogram.android.OctoConfig;
import it.octogram.android.ai.helper.CustomModelsHelper;
import it.octogram.android.ai.icons.AiFeatureIcons;

@SuppressLint("ViewConstructor")
public class CustomAIModelCell extends FrameLayout {

    private final LinearLayout textViewLayout;
    private final TextView textView;
    private final ImageView imageView;
    private final ImageView optionsImageView;

    private boolean needDivider;
    private String modelID = null;
    private Consumer<View> onShowOptions;

    public CustomAIModelCell(Context context) {
        super(context);

        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);

        imageView = new ImageView(context);
        imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
        addView(imageView, LayoutHelper.createFrame(24, 24, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), 20, 0, 20, 0));

        textView = new AppCompatTextView(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec) - dp(52), MeasureSpec.AT_MOST);
                }
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        };
        textView.setLines(1);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        textView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);

        ImageView arrowView = new ImageView(context);
        arrowView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), PorterDuff.Mode.MULTIPLY));
        arrowView.setImageResource(R.drawable.msg_arrow_forward);

        textViewLayout = new LinearLayout(context);
        textViewLayout.setOrientation(LinearLayout.HORIZONTAL);
        textViewLayout.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);

        boolean isRTL = LocaleController.isRTL;
        textViewLayout.addView(textView, createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));
        textViewLayout.addView(arrowView, createLinear(16, 16, 0, Gravity.CENTER_VERTICAL, isRTL ? 0 : 2, 0, 0, 0));

        addView(textViewLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), 73, 0, 8, 0));

        optionsImageView = new ImageView(context);
        optionsImageView.setFocusable(false);
        optionsImageView.setScaleType(ImageView.ScaleType.CENTER);
        optionsImageView.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector)));
        optionsImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_stickers_menu), PorterDuff.Mode.MULTIPLY));
        optionsImageView.setImageResource(R.drawable.msg_actions);
        optionsImageView.setOnClickListener(v -> showOptions());
        optionsImageView.setContentDescription(getString(R.string.AccDescrMoreOptions));
        addView(optionsImageView, LayoutHelper.createFrame(40, 40, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL, 6, 0, 6, 0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(dp(50), MeasureSpec.EXACTLY)
        );
    }

    public void setData(String modelID, boolean needDivider, Consumer<View> onShowOptions) {
        this.modelID = modelID;
        this.onShowOptions = onShowOptions;
        optionsImageView.setVisibility(onShowOptions == null ? View.GONE : View.VISIBLE);

        CustomModelsHelper.CustomModel model = CustomModelsHelper.getModelById(modelID);
        if (model != null) {
            if (!model.title.trim().isEmpty()) {
                CharSequence modelTitle = model.title.trim();
                try {
                    modelTitle = Emoji.replaceEmoji(modelTitle, textView.getPaint().getFontMetricsInt(), false);
                } catch (Exception ignore) {}
                textView.setText(modelTitle);
            } else {
                textView.setText(formatString(R.string.AiFeatures_CustomModels_ModelID, modelID));
            }

            imageView.setImageResource(AiFeatureIcons.getModelIcon(model.icon));
        }

        this.needDivider = needDivider;
        invalidate();
    }

    public void showOptions() {
        if (modelID == null || onShowOptions == null) {
            return;
        }

        onShowOptions.accept(this);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (LocaleController.isRTL) {
            if (needDivider && !OctoConfig.INSTANCE.disableDividers.getValue()) {
                canvas.drawLine(getMeasuredWidth() - dp(64) + (textView.getTranslationX() < 0 ? dp(-32) : 0), getMeasuredHeight() - 1, 0, getMeasuredHeight() - 1, Theme.dividerPaint);
            }
        } else {
            if (needDivider && !OctoConfig.INSTANCE.disableDividers.getValue()) {
                canvas.drawLine(dp(64) + textView.getTranslationX(), getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, Theme.dividerPaint);
            }
        }
    }
}