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
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;

import it.octogram.android.OctoConfig;

public class ImportSettingsDialog extends BottomSheet {
    private final int difference;
    private final BaseFragment fragment;

    public ImportSettingsDialog(BaseFragment fragment, MessageObject messageObject) {
        super(fragment.getParentActivity(), false, fragment.getResourceProvider());
        this.fragment = fragment;
        Activity activity = fragment.getParentActivity();
        difference = OctoConfig.getDifferenceBetweenCurrentConfig(messageObject).size();
        FrameLayout frameLayout = new FrameLayout(activity);
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        frameLayout.addView(linearLayout);

        StickerImageView imageView = new StickerImageView(activity, currentAccount);
        imageView.setStickerPackName("AniDucks");
        if (OctoConfig.isLegacy(messageObject)) {
            imageView.setStickerNum(16);
        } else {
            imageView.setStickerNum(5);
        }
        imageView.getImageReceiver().setAutoRepeat(1);
        linearLayout.addView(imageView, LayoutHelper.createLinear(144, 144, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 0));

        TextView title = new TextView(activity);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        title.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        title.setText(LocaleController.getString("ImportSettingsAlert", R.string.ImportSettingsAlert));
        linearLayout.addView(title, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 21, 20, 21, 0));

        TextView description = new TextView(activity);
        description.setGravity(Gravity.CENTER_HORIZONTAL);
        description.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        description.setTextColor(getThemedColor(Theme.key_dialogTextGray3));
        if (difference == 1) {
            description.setText(AndroidUtilities.replaceTags(LocaleController.getString("ImportingChangesOne", R.string.ImportingChangesOne)));
        } else {
            description.setText(AndroidUtilities.replaceTags(LocaleController.formatString("ImportingChangesOthers", R.string.ImportingChangesOthers, difference)));
        }
        linearLayout.addView(description, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 21, 15, 21, 16));

        TextView buttonTextView = new TextView(activity);
        buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        buttonTextView.setText(LocaleController.getString("ImportSettings", R.string.ImportSettings));
        buttonTextView.setOnClickListener(view -> {
            dismiss();
            int differenceUI = OctoConfig.getDifferenceUI(messageObject);
            OctoConfig.restoreBackup(messageObject);
            fragment.getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
            fragment.getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            fragment.getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            OctoConfig.doRebuildUIWithDiff(differenceUI, fragment.getParentLayout());
            BulletinFactory.of(fragment).createSimpleBulletin(R.raw.forward, LocaleController.getString("SettingsImported", R.string.SettingsImported)).show();
        });

        buttonTextView.setTextColor(getThemedColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6), getThemedColor(Theme.key_featuredStickers_addButton), ColorUtils.setAlphaComponent(getThemedColor(Theme.key_windowBackgroundWhite), 120)));

        linearLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 15, 16, 8));

        TextView textView = new TextView(activity);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setText(LocaleController.getString("Cancel", R.string.Cancel));
        textView.setTextColor(getThemedColor(Theme.key_featuredStickers_addButton));
        textView.setOnClickListener(view -> dismiss());
        linearLayout.addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 0, 16, 0));

        ScrollView scrollView = new ScrollView(activity);
        scrollView.addView(frameLayout);
        setCustomView(scrollView);
    }

    public void checkCanShowDialog() {
        if (difference > 0) {
            show();
        } else {
            AndroidUtilities.runOnUIThread(() -> BulletinFactory.of(fragment).createSimpleBulletin(R.raw.error, LocaleController.getString("SameSettings", R.string.SameSettings), true).show());
        }
    }
}
