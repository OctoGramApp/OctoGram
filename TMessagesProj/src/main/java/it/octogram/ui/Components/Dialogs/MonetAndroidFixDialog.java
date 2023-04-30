package it.octogram.ui.Components.Dialogs;

import android.app.Activity;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;

import it.octogram.android.MonetIconController;

public class MonetAndroidFixDialog extends OnceBottomSheet {

    public MonetAndroidFixDialog(BaseFragment fragment) {
        super(fragment.getParentActivity(), false);
        Activity activity = fragment.getParentActivity();
        FrameLayout frameLayout = new FrameLayout(activity);
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        frameLayout.addView(linearLayout);

        StickerImageView imageView = new StickerImageView(activity, currentAccount);
        imageView.setStickerPackName("UtyaDuck");
        imageView.setStickerNum(34);
        imageView.getImageReceiver().setAutoRepeat(1);
        linearLayout.addView(imageView, LayoutHelper.createLinear(144, 144, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 0));

        TextView title = new TextView(activity);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        title.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        title.setText(LocaleController.getString("ChangeIcon", R.string.ChangeIcon));
        linearLayout.addView(title, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 21, 20, 21, 0));

        TextView description = new TextView(activity);
        description.setGravity(Gravity.CENTER_HORIZONTAL);
        description.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        description.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        description.setText(LocaleController.getString("ChangeIconDesc", R.string.ChangeIconDesc));
        linearLayout.addView(description, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 21, 15, 21, 16));

        TextView buttonTextView = new TextView(activity);
        buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        buttonTextView.setText(LocaleController.getString("Change", R.string.Change));
        buttonTextView.setOnClickListener(view -> {
            dismiss();
            MonetIconController.switchToMonet();
            AlertDialog progressDialog = new AlertDialog(fragment.getParentActivity(), 3);
            progressDialog.show();
            AndroidUtilities.runOnUIThread(progressDialog::dismiss, 2000);
        });

        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundWhite), 120)));

        linearLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 15, 16, 8));

        ScrollView scrollView = new ScrollView(activity);
        scrollView.addView(frameLayout);
        setCustomView(scrollView);
    }

    public static void checkBottomSheet(BaseFragment fragment) {
        try {
            MonetAndroidFixDialog dialog = new MonetAndroidFixDialog(fragment);
            dialog.setCancelable(false);
            dialog.show();
        } catch (Exception ignored) {
        }
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
