/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui.components;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;

import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.utils.config.ImportSettingsScanHelper;

@SuppressLint("ViewConstructor")
public class ImportSettingsTopLayerCell extends LinearLayout {
    private final Context context;
    private StickerImageView imageView;
    private final TextView captionView;
    private ImageView imageView2;
    private final boolean isOut;

    public ImportSettingsTopLayerCell(Context context, boolean isOut) {
        super(context);
        super.setOrientation(LinearLayout.VERTICAL);
        super.setMinimumWidth(LayoutParams.MATCH_PARENT);

        this.isOut = isOut;
        this.context = context;

        captionView = new TextView(context);
        captionView.setTextColor(Theme.getColor(Theme.key_chats_message));
        captionView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        captionView.setGravity(Gravity.CENTER);
        captionView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());

        addView(captionView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 52, 25, 52, 18));
    }

    public void setCategory(ImportSettingsScanHelper.SettingsScanCategory category) {
        if (imageView != null) {
            removeView(imageView);
            imageView = null;
        }

        if (imageView2 != null) {
            removeView(imageView2);
        }

        if (category == null) {
            imageView = new StickerImageView(getContext(), UserConfig.selectedAccount);
            imageView.setStickerPackName(OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME);
            imageView.setStickerNum(StickerUi.IMPORT_SETTINGS.getValue());
            imageView.getImageReceiver().setAutoRepeat(1);
            captionView.setText(getString(isOut ? R.string.ImportReadyDescription : R.string.ImportReadyDescriptionFromExternal));
            addView(imageView, 0, LayoutHelper.createLinear(104, 104, Gravity.CENTER, 0, 14, 0, 0));
            return;
        }

        imageView2 = new ImageView(context);
        imageView2.setScaleType(ImageView.ScaleType.CENTER);
        imageView2.setImageResource(category.categoryIcon);
        imageView2.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_featuredStickers_buttonText), PorterDuff.Mode.SRC_IN));
        imageView2.setBackground(Theme.createCircleDrawable(dp(80), Theme.getColor(Theme.key_featuredStickers_addButton)));
        addView(imageView2, 0, LayoutHelper.createLinear(80, 80, Gravity.CENTER_HORIZONTAL, 0, 14, 0, 0));

        captionView.setText("Choose which options you need to import from "+category.getName());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );
    }
}
