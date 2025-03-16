package it.octogram.android.preferences.ui.custom;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;

import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.ui.OctoPrivacySettingsUI;
import it.octogram.android.preferences.ui.components.CustomBottomSheet;

@RequiresApi(api = Build.VERSION_CODES.S)
public class DoubleBottomMigrationBottomSheet extends CustomBottomSheet {

    @Override
    protected boolean canDismissWithSwipe() {
        return false;
    }

    @Override
    protected boolean canDismissWithTouchOutside() {
        return false;
    }

    public DoubleBottomMigrationBottomSheet(BaseFragment fragment) {
        super(fragment.getParentActivity(), false);
        setCanceledOnTouchOutside(false);
        Context context = fragment.getParentActivity();

        FrameLayout frameLayout = new FrameLayout(context);

        ImageView closeView = new ImageView(context);
        closeView.setBackground(Theme.createSelectorDrawable(getThemedColor(Theme.key_listSelector)));
        closeView.setColorFilter(Theme.getColor(Theme.key_sheet_other));
        closeView.setImageResource(R.drawable.ic_layer_close);
        closeView.setOnClickListener((view) -> dismiss());
        int closeViewPadding = dp(8);
        closeView.setPadding(closeViewPadding, closeViewPadding, closeViewPadding, closeViewPadding);
        frameLayout.addView(closeView, LayoutHelper.createFrame(36, 36, Gravity.TOP | Gravity.END, 6, 8, 6, 0));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        frameLayout.addView(linearLayout);

        StickerImageView imageView = new StickerImageView(context, currentAccount);
        imageView.setStickerPackName(OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME);
        imageView.setStickerNum(StickerUi.PRIVACY.getValue());
        imageView.getImageReceiver().setAutoRepeat(1);
        linearLayout.addView(imageView, LayoutHelper.createLinear(144, 144, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 0));

        TextView title = new TextView(context);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        title.setTypeface(AndroidUtilities.bold());
        title.setText(getString(R.string.LockedAccounts_Migration_Title));
        linearLayout.addView(title, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 21, 30, 21, 0));

        TextView description = new TextView(context);
        description.setGravity(Gravity.CENTER);
        description.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        description.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        description.setText(AndroidUtilities.replaceTags(getString(R.string.LockedAccounts_Migration_Desc)));
        linearLayout.addView(description, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 21, 15, 21, 16));

        TextView buttonTextView = new TextView(context);
        buttonTextView.setPadding(dp(34), 0, dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(AndroidUtilities.bold());
        buttonTextView.setText(getString(R.string.GoToSettings));

        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundWhite), 120)));

        linearLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 15, 16, 8));

        buttonTextView.setOnClickListener(view -> {
            fragment.presentFragment(new PreferencesFragment(new OctoPrivacySettingsUI(), "lockedAccounts"));
            dismiss();
        });

        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(frameLayout);
        setCustomView(scrollView);
    }
}