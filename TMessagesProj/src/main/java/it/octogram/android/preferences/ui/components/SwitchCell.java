/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui.components;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.ui.Components.LayoutHelper.createFrame;
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

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CheckBox2;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Switch;

import java.text.MessageFormat;

import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.ExpandableRows;
import it.octogram.android.preferences.rows.impl.ExpandableRowsChild;
import it.octogram.android.utils.config.ExpandableRowsOption;

@SuppressLint("ViewConstructor")
public class SwitchCell extends FrameLayout {

    public final Switch switchView;
    public final CheckBox2 checkBoxView;
    private final ImageView imageView;
    private final AvatarDrawable avatarDrawable;
    private final BackupImageView backupImageView;
    private final TextView textView;
    private final AnimatedTextView countTextView;
    private final ImageView arrowView;
    private final LinearLayout textViewLayout;
    private final PreferencesFragment fragment;

    private boolean needDivider, needLine;
    private boolean isExpanded;
    private boolean isSwitch;
    private ExpandableRows _expandableRows;
    private ExpandableRowsChild _item;
    private boolean isSelectingItems = false;
    private boolean hasAddedUserData = false;
    private boolean _isLocked = false;

    public SwitchCell(Context context, PreferencesFragment fragment) {
        super(context);
        this.fragment = fragment;

        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        imageView = new ImageView(context);
        imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
        imageView.setVisibility(View.GONE);
        addView(imageView, createFrame(24, 24, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), 20, 0, 20, 0));

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

        countTextView = new AnimatedTextView(context, false, true, true);
        countTextView.setAnimationProperties(.35f, 0, 200, CubicBezierInterpolator.EASE_OUT_QUINT);
        countTextView.setTypeface(AndroidUtilities.bold());
        countTextView.setTextSize(dp(14));
        countTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        countTextView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);

        arrowView = new ImageView(context);
        arrowView.setVisibility(GONE);
        arrowView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), PorterDuff.Mode.MULTIPLY));
        arrowView.setImageResource(R.drawable.arrow_more);

        textViewLayout = new LinearLayout(context);
        textViewLayout.setOrientation(LinearLayout.HORIZONTAL);
        textViewLayout.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);

        avatarDrawable = new AvatarDrawable();
        avatarDrawable.setTextSize(dp(12));

        backupImageView = new BackupImageView(context);
        backupImageView.setRoundRadius(dp(18));

        boolean isRTL = LocaleController.isRTL;
        textViewLayout.addView(backupImageView, createLinear(36, 36, Gravity.CENTER_VERTICAL, 0, 0, 0, 0));
        textViewLayout.addView(textView, createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));
        textViewLayout.addView(countTextView, createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, Gravity.CENTER_VERTICAL, isRTL ? 6 : 0, 0, isRTL ? 0 : 6, 0));
        textViewLayout.addView(arrowView, createLinear(16, 16, 0, Gravity.CENTER_VERTICAL, isRTL ? 0 : 2, 0, 0, 0));

        addView(textViewLayout, createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), 73, 0, 8, 0));

        switchView = new Switch(context);
        switchView.setVisibility(GONE);
        switchView.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
        switchView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        addView(switchView, createFrame(37, 50, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT), 19, 0, 19, 0));

        checkBoxView = new CheckBox2(context, 21);
        checkBoxView.setColor(Theme.key_radioBackgroundChecked, Theme.key_checkboxDisabled, Theme.key_checkboxCheck);
        checkBoxView.setDrawUnchecked(true);
        checkBoxView.setChecked(true, false);
        checkBoxView.setDrawBackgroundAsArc(10);
        checkBoxView.setVisibility(GONE);
        checkBoxView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        addView(checkBoxView, createFrame(21, 21, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), LocaleController.isRTL ? 0 : 64, 0, LocaleController.isRTL ? 64 : 0, 0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(dp(50), MeasureSpec.EXACTLY)
        );
    }

    public void setAsSwitch(ExpandableRows expandableRows) {
        isSwitch = true;
        _expandableRows = expandableRows;

        int selectedOptions = 0;
        for (ExpandableRowsOption item : expandableRows.getItemsList()) {
            if (item.property.getValue()) {
                selectedOptions++;
            }
        }

        checkBoxView.setVisibility(GONE);
        imageView.setVisibility(VISIBLE);
        imageView.setImageResource(expandableRows.getIcon());
        textView.setText(expandableRows.getMainItemTitle());
        countTextView.setVisibility(VISIBLE);
        arrowView.setVisibility(VISIBLE);
        textView.setTranslationX(0);
        switchView.setVisibility(VISIBLE);
        backupImageView.setVisibility(GONE);
        switchView.setChecked(selectedOptions > 0, true);

        boolean currentExpanded = fragment.getExpandedRowIds().contains(expandableRows.getId());
        if (isExpanded != currentExpanded) {
            isExpanded = currentExpanded;
            arrowView.animate().rotation(isExpanded ? 180 : 0).setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).setDuration(240).start();
        }

        countTextView.setText(MessageFormat.format(" {0}/{1}", selectedOptions, expandableRows.getItemsList().size()));

        ((MarginLayoutParams) textViewLayout.getLayoutParams()).rightMargin = dp((LocaleController.isRTL ? 64 : 75) + 4);

        needLine = !expandableRows.getItemsList().isEmpty() && !expandableRows.isMainSwitchHidden();
        needDivider = expandableRows.hasDivider();
        setWillNotDraw(false);

        boolean isLocked = fragment.isExpandableRowsLockedInternal(expandableRows);
        if (_isLocked != isLocked) {
            _isLocked = isLocked;
            arrowView.setScaleX(isLocked ? 0.8f : 1f);
            arrowView.setScaleY(isLocked ? 0.8f : 1f);
            arrowView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(isLocked ? Theme.key_stickers_menu : Theme.key_windowBackgroundWhiteBlackText), PorterDuff.Mode.MULTIPLY));
            arrowView.setImageResource(isLocked ? R.drawable.other_lockedfolders2 : R.drawable.arrow_more);
            arrowView.setTranslationX(isLocked ? dp(2) : 0);
        }
    }

    public void setAsCheckbox(ExpandableRowsChild expandableRow) {
        isSwitch = false;
        _item = expandableRow;

        imageView.setVisibility(GONE);
        switchView.setVisibility(GONE);
        countTextView.setVisibility(GONE);
        arrowView.setVisibility(GONE);
        checkBoxView.setVisibility(VISIBLE);
        checkBoxView.setChecked(expandableRow.getItem().property.getValue(), true);

        if (expandableRow.getItem().hasAccount()) {
            if (!hasAddedUserData) {
                int account = expandableRow.getItem().accountId;
                TLRPC.User user = UserConfig.getInstance(account).getCurrentUser();
                avatarDrawable.setInfo(account, user);
                textView.setText(ContactsController.formatName(user.first_name, user.last_name));
                backupImageView.getImageReceiver().setCurrentAccount(account);
                backupImageView.setForUserOrChat(user, avatarDrawable);
                backupImageView.setVisibility(VISIBLE);
                hasAddedUserData = true;

                CharSequence text = user.first_name;
                try {
                    text = Emoji.replaceEmoji(text, textView.getPaint().getFontMetricsInt(), false);
                } catch (Exception ignore) {
                }
                textView.setText(text);

                backupImageView.setTranslationX(dp(41) * (LocaleController.isRTL ? -2.2f : 1));
                textView.setTranslationX(dp(55) * (LocaleController.isRTL ? -2.2f : 1));
            }
        } else {
            backupImageView.setVisibility(GONE);
            textView.setText(expandableRow.getItem().optionTitle);
            textView.setTranslationX(dp(41) * (LocaleController.isRTL ? -2.2f : 1));
        }

        needLine = false;
        needDivider = expandableRow.hasDivider();
        setWillNotDraw(!expandableRow.hasDivider());
    }

    public void reload() {
        if (isSwitch) {
            setAsSwitch(_expandableRows);
        } else {
            setAsCheckbox(_item);
        }
    }

    public void setIsSelectingItems(boolean isSelectingItems) {
        this.isSelectingItems = isSelectingItems;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (LocaleController.isRTL) {
            if (needLine && !isSelectingItems) {
                float x = dp(19 + 37 + 19);
                canvas.drawRect(x - dp(0.66f), (getMeasuredHeight() - dp(20)) / 2f, x, (getMeasuredHeight() + dp(20)) / 2f, Theme.dividerPaint);
            }
            if (needDivider && !OctoConfig.INSTANCE.disableDividers.getValue()) {
                canvas.drawLine(getMeasuredWidth() - dp(64) + ((hasAddedUserData ? backupImageView : textView).getTranslationX() < 0 ? dp(-32) : 0), getMeasuredHeight() - 1, 0, getMeasuredHeight() - 1, Theme.dividerPaint);
            }
        } else {
            if (needLine && !isSelectingItems) {
                float x = getMeasuredWidth() - dp(19 + 37 + 19);
                canvas.drawRect(x - dp(0.66f), (getMeasuredHeight() - dp(20)) / 2f, x, (getMeasuredHeight() + dp(20)) / 2f, Theme.dividerPaint);
            }
            if (needDivider && !OctoConfig.INSTANCE.disableDividers.getValue()) {
                canvas.drawLine(dp(64) + (hasAddedUserData ? backupImageView : textView).getTranslationX(), getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, Theme.dividerPaint);
            }
        }
    }
}