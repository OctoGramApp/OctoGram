/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;

import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;

@SuppressLint("ViewConstructor")
public class ImportSettingsTopLayerCell extends LinearLayout {
    public ImportSettingsTopLayerCell(Context context, boolean isOut) {
        super(context);
        super.setOrientation(LinearLayout.VERTICAL);
        super.setMinimumWidth(LayoutParams.MATCH_PARENT);

        StickerImageView imageView = new StickerImageView(getContext(), UserConfig.selectedAccount);
        imageView.setStickerPackName(OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME);
        imageView.setStickerNum(StickerUi.IMPORT_SETTINGS.getValue());
        imageView.getImageReceiver().setAutoRepeat(1);

        TextView textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        textView.setTypeface(AndroidUtilities.bold());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(LocaleController.getString(R.string.ImportReady));
        textView.setPadding(AndroidUtilities.dp(30), 0, AndroidUtilities.dp(30), 0);

        TextView captionView = new TextView(context);
        captionView.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        captionView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        captionView.setGravity(Gravity.CENTER_HORIZONTAL);
        captionView.setText(LocaleController.getString(isOut ? R.string.ImportReadyDescription : R.string.ImportReadyDescriptionFromExternal));
        captionView.setPadding(AndroidUtilities.dp(30), AndroidUtilities.dp(10), AndroidUtilities.dp(30), AndroidUtilities.dp(21));

        addView(imageView, LayoutHelper.createLinear(144, 144, Gravity.CENTER, 0, 16, 0, 16));
        addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        addView(captionView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );
    }
}
