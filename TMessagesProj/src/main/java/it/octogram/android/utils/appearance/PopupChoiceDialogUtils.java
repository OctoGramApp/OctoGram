/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.appearance;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CheckBox2;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadioButton;
import org.telegram.ui.Components.SeekBarView;
import org.telegram.ui.Components.Switch;

import java.util.List;

import it.octogram.android.preferences.ui.custom.FolderTypeSelector;

public class PopupChoiceDialogUtils {
    public static Dialog createChoiceDialog(Activity parentActivity, List<PopupChoiceDialogOption> options, final CharSequence title, final int selected, final DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
        builder.setTitle(title);

        final LinearLayout linearLayout = new LinearLayout(parentActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        builder.setView(linearLayout);

        for (PopupChoiceDialogOption option : options) {
            CustomRadioDataCell cell = new CustomRadioDataCell(parentActivity.getBaseContext());
            cell.setPadding(dp(4), 0, dp(4), 0);
            cell.setData(option.itemTitle, option.itemDescription, selected == option.id, option.itemIcon);
            cell.setTag(option.id);
            cell.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), Theme.RIPPLE_MASK_ALL));
            cell.setCheckColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_dialogRadioBackgroundChecked));
            linearLayout.addView(cell);
            cell.setOnClickListener(v -> {
                int sel = (Integer) v.getTag();
                builder.getDismissRunnable().run();
                listener.onClick(null, sel);
            });

            if (option.hasCustomEntity()) {
                LinearLayout tempLayout = new LinearLayout(builder.getContext());
                tempLayout.setOrientation(LinearLayout.HORIZONTAL);

                if (option.itemSwitchIconUI != null) {
                    Switch switchView = getSwitch(builder.getContext(), option.itemSwitchIconUI.getValue());
                    Switch checkedView = getSwitch(builder.getContext(), option.itemSwitchIconUI.getValue());
                    checkedView.setChecked(true, false);

                    tempLayout.addView(switchView, LayoutHelper.createFrame(37, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.LEFT, 0, 0, 10, 0));
                    tempLayout.addView(checkedView, LayoutHelper.createFrame(37, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.RIGHT));
                } else if (option.itemCheckboxIconUI != null) {
                    CheckBox2 checkBoxView = getCheckbox(builder.getContext(), option.itemCheckboxIconUI.getValue());
                    CheckBox2 checkedView = getCheckbox(builder.getContext(), option.itemCheckboxIconUI.getValue());
                    checkedView.setChecked(true, false);

                    tempLayout.addView(checkBoxView, LayoutHelper.createFrame(23, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.LEFT, 0, 0, 10, 0));
                    tempLayout.addView(checkedView, LayoutHelper.createFrame(23, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.RIGHT));
                } else if (option.itemSliderIconUI != null) {
                    tempLayout.addView(getSeekBar(builder.getContext(), option.itemSliderIconUI.getValue()), LayoutHelper.createFrame(125, 44, Gravity.CENTER_VERTICAL | Gravity.RIGHT));
                } else if (option.tabStyleIconUI != null) {
                    tempLayout.addView(new FolderTypeSelector(builder.getContext(), true, option.tabStyleIconUI), LayoutHelper.createFrame(125, 44, Gravity.CENTER_VERTICAL | Gravity.RIGHT));
                } else if (option.tabModeIconUI != null) {
                    tempLayout.addView(new FolderTypeSelector(builder.getContext(), true, option.tabModeIconUI), LayoutHelper.createFrame(125, 44, Gravity.CENTER_VERTICAL | Gravity.RIGHT));
                }

                cell.addView(tempLayout, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, (option.tabStyleIconUI != null || option.tabModeIconUI != null) ? 44 : 50, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT), 0, 0, 18, 0));
            }

            if (!option.clickable) {
                cell.setClickable(false);
                cell.setEnabled(false);
                cell.setAlpha(0.5f);
            }
        }

        builder.setNegativeButton(getString(R.string.Cancel), null);

        return builder.create();
    }

    private static Switch getSwitch(Context context, int switchIconUI) {
        Switch switchView = new Switch(context, switchIconUI);
        switchView.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
        switchView.setEnabled(false);
        return switchView;
    }

    private static CheckBox2 getCheckbox(Context context, int checkBoxIconUI) {
        CheckBox2 checkBoxView = new CheckBox2(context, 21, checkBoxIconUI);
        checkBoxView.setColor(Theme.key_radioBackgroundChecked, Theme.key_checkboxDisabled, Theme.key_checkboxCheck);
        checkBoxView.setDrawUnchecked(true);
        checkBoxView.setDrawBackgroundAsArc(10);
        return checkBoxView;
    }

    private static SeekBarView getSeekBar(Context context, int sliderIconUI) {
        SeekBarView sizeBar = new SeekBarView(context);
        sizeBar.setReportChanges(false);
        sizeBar.setPreviewingState(sliderIconUI);
        sizeBar.setEnabled(false);
        sizeBar.setProgress(0.5f);
        return sizeBar;
    }


    public static class CustomRadioDataCell extends FrameLayout {

        private final ImageView imageView;
        private final TextView textView;
        private final TextView text2View;
        private final RadioButton radioButton;
        private final Theme.ResourcesProvider resourcesProvider;

        public int heightDp = 50;

        public CustomRadioDataCell(Context context) {
            this(context, null);
        }

        public CustomRadioDataCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;

            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_dialogIcon), PorterDuff.Mode.MULTIPLY));
            addView(imageView, LayoutHelper.createFrame(22, 22, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 0 : 18), 14, (LocaleController.isRTL ? 18 : 0), 0));

            radioButton = new RadioButton(context);
            radioButton.setSize(AndroidUtilities.dp(20));
            radioButton.setColor(getThemedColor(Theme.key_dialogRadioBackground), getThemedColor(Theme.key_dialogRadioBackgroundChecked));
            addView(radioButton, LayoutHelper.createFrame(22, 22, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 0 : 18), 14, (LocaleController.isRTL ? 18 : 0), 0));

            textView = new TextView(context);
            textView.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setLines(1);
            textView.setMaxLines(1);
            textView.setSingleLine(true);
            textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 21 : 51), 13, (LocaleController.isRTL ? 51 : 21), 0));

            text2View = new TextView(context);
            text2View.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteGrayText));
            text2View.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            text2View.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            text2View.setVisibility(View.GONE);
            addView(text2View, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 21 : 51), 13 + 16 + 8, (LocaleController.isRTL ? 51 : 21), 0));
        }

        boolean hasIcon = false;

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (text2View.getVisibility() == View.VISIBLE) {
                text2View.measure(
                        MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec) - AndroidUtilities.dp(21 + 51) - (hasIcon ? AndroidUtilities.dp(10) : 0), MeasureSpec.EXACTLY),
                        heightMeasureSpec
                );
            }
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(heightDp) + (text2View.getVisibility() == View.VISIBLE ? AndroidUtilities.dp(4) + text2View.getMeasuredHeight() : 0), MeasureSpec.EXACTLY)
            );
        }

        public void setCheckColor(int color1, int color2) {
            radioButton.setColor(color1, color2);
        }

        public void setData(CharSequence title, CharSequence description, boolean checked, int resId) {
            textView.setText(title);
            text2View.setText(description);
            text2View.setVisibility(description == null ? View.GONE : View.VISIBLE);
            radioButton.setVisibility(resId == 0 ? View.VISIBLE : View.GONE);
            radioButton.setChecked(checked, false);
            imageView.setVisibility(resId == 0 ? View.GONE : View.VISIBLE);
            textView.setTranslationX(resId == 0 ? 0 : AndroidUtilities.dp(10));
            text2View.setTranslationX(resId == 0 ? 0 : AndroidUtilities.dp(10));
            if (resId != 0) {
                imageView.setImageResource(resId);
            }
            hasIcon = resId != 0;
        }

        public void setChecked(boolean checked, boolean animated) {
            radioButton.setChecked(checked, animated);
        }

        public boolean isChecked() {
            return radioButton.isChecked();
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            info.setClassName("android.widget.RadioButton");
            info.setCheckable(true);
            info.setChecked(radioButton.isChecked());
        }

        private int getThemedColor(int key) {
            return Theme.getColor(key, resourcesProvider);
        }
    }

}
