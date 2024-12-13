/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.preferences.ui.custom;

import static org.telegram.messenger.AndroidUtilities.bold;
import static org.telegram.messenger.LocaleController.getString;

import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;

import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.preferences.ui.components.CustomBottomSheet;
import it.octogram.android.theme.MonetIconController;

public class MonetAndroidFixDialog extends CustomBottomSheet {

    public MonetAndroidFixDialog(BaseFragment fragment) {
        super(fragment.getParentActivity(), false);
        var activity = fragment.getParentActivity();
        var frameLayout = new FrameLayout(activity);
        var linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        frameLayout.addView(linearLayout);

        var imageView = new StickerImageView(activity, currentAccount);
        imageView.setStickerPackName(OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME);
        imageView.setStickerNum(StickerUi.MONET_DIALOG.getValue());
        imageView.getImageReceiver().setAutoRepeat(1);
        linearLayout.addView(imageView, LayoutHelper.createLinear(144, 144, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 0));

        var title = new AppCompatTextView(activity);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        title.setTypeface(bold());
        title.setText(getString(R.string.ChangeIcon));
        linearLayout.addView(title, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 21, 20, 21, 0));

        var description = new AppCompatTextView(activity);
        description.setGravity(Gravity.CENTER_HORIZONTAL);
        description.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        description.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        description.setText(getString(R.string.ChangeIconDesc));
        linearLayout.addView(description, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 21, 15, 21, 16));

        var buttonTextView = new AppCompatTextView(activity);
        buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(bold());
        buttonTextView.setText(getString(R.string.Change));
        buttonTextView.setOnClickListener(view -> {
            dismiss();
            MonetIconController.INSTANCE.switchToMonet();
            var progressDialog = new AlertDialog(fragment.getParentActivity(), 3);
            progressDialog.show();
            AndroidUtilities.runOnUIThread(progressDialog::dismiss, 2000);
        });

        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundWhite), 120)));

        linearLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 15, 16, 8));

        var scrollView = new ScrollView(activity);
        scrollView.addView(frameLayout);
        setCustomView(scrollView);
        setCancelable(false);
    }

    public static void showDialog(BaseFragment fragment) {
        try {
            new MonetAndroidFixDialog(fragment).show();
        } catch (Exception ignored) {}
    }

    @Override
    protected boolean canDismissWithSwipe() {
        return false;
    }

    @Override
    protected boolean canDismissWithTouchOutside() {
        return false;
    }
}