package it.octogram.ui.Cells;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.Date;

import it.octogram.android.OctoConfig;
import it.octogram.android.StoreUtils;
import it.octogram.android.updates.UpdateManager;

@SuppressLint("ViewConstructor")
public class UpdateCheckCell extends RelativeLayout {
    private final TextView titleTextView, valueTextView, checkUpdateButton;
    private final boolean needDivider;
    private boolean canCheckForUpdate = true;

    @SuppressLint("ClickableViewAccessibility")
    public UpdateCheckCell(Context context, boolean divider) {
        super(context);
        needDivider = divider;

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        titleTextView = new TextView(context);
        titleTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        titleTextView.setMaxLines(Integer.MAX_VALUE);
        titleTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        int linkTextColorKey = Theme.key_windowBackgroundWhiteLinkText;
        titleTextView.setLinkTextColor(Theme.getColor(linkTextColorKey));
        titleTextView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        layout.addView(titleTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 23, 8, 23, 0));

        valueTextView = new TextView(context);
        valueTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        valueTextView.setLines(1);
        valueTextView.setMaxLines(1);
        valueTextView.setSingleLine(true);
        valueTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        valueTextView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        layout.addView(valueTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 23, 5, 23, 8));

        LinearLayout layoutRight = new LinearLayout(context);
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        layoutTransition.setDuration(LayoutTransition.CHANGING, 100);
        layoutRight.setLayoutTransition(layoutTransition);
        layoutRight.setGravity(LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT);

        checkUpdateButton = new TextView(context);
        checkUpdateButton.setText(LocaleController.getString("CheckUpdates", R.string.CheckUpdates));
        checkUpdateButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        checkUpdateButton.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(16), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
        checkUpdateButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        checkUpdateButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        checkUpdateButton.setGravity(Gravity.CENTER);
        checkUpdateButton.setOnTouchListener((View view, MotionEvent motionEvent) -> {
            if (!canCheckForUpdate) {
                return true;
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                onCheckUpdate();
            }
            return false;
        });
        checkUpdateButton.setClickable(true);
        checkUpdateButton.setPadding(AndroidUtilities.dp(14), 0, AndroidUtilities.dp(14), 0);
        layoutRight.addView(checkUpdateButton, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 32));

        addView(layout, LayoutHelper.createRelative(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 0, LocaleController.isRTL ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_PARENT_LEFT));
        addView(layoutRight, LayoutHelper.createRelative(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? 22 : 0, 0, LocaleController.isRTL ? 0 : 22, 0, RelativeLayout.CENTER_VERTICAL | (LocaleController.isRTL ? RelativeLayout.ALIGN_PARENT_LEFT : RelativeLayout.ALIGN_PARENT_RIGHT)));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider) {
            canvas.drawLine(
                    LocaleController.isRTL ? 0 : AndroidUtilities.dp(20),
                    getMeasuredHeight() - 1,
                    getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(20) : 0),
                    getMeasuredHeight() - 1,
                    Theme.dividerPaint
            );
        }
    }

    public void setCheckTime() {
        long date = OctoConfig.lastUpdateCheck;
        checkUpdateButton.setText(LocaleController.getString("CheckUpdates", R.string.CheckUpdates));
        setTime(date);
        canCheckForUpdate = true;
        updateTitle();
    }

    private void updateTitle() {
        long date = OctoConfig.lastUpdateCheck;
        boolean isUpdateAvailable = UpdateManager.isAvailableUpdate();
        if (isUpdateAvailable) {
            titleTextView.setText(LocaleController.getString("UpdateAvailable", R.string.UpdateAvailable));
        } else if (date != 0) {
            titleTextView.setText(LocaleController.getString("NoUpdateAvailable", R.string.NoUpdateAvailable));
        } else {
            titleTextView.setText(LocaleController.getString("NeverChecked", R.string.NeverChecked));
        }
    }

    private void setTime(long date) {
        String dateString;
        if (date != 0) {
            dateString = LocaleController.formatString("formatDateAtTime", R.string.formatDateAtTime, LocaleController.getInstance().formatterYear.format(new Date(date)), LocaleController.getInstance().formatterDay.format(new Date(date)));
        } else {
            dateString = LocaleController.getString("LastCheckNever", R.string.LastCheckNever);
        }
        valueTextView.setText(LocaleController.formatString("LastCheck", R.string.LastCheck, dateString));
    }

    public void setCheckingStatus() {
        checkUpdateButton.setText(LocaleController.getString("Checking", R.string.Checking));
        setTime(OctoConfig.lastUpdateCheck);
        canCheckForUpdate = false;
    }

    public void setUpdateAvailableStatus() {
        if (StoreUtils.isFromPlayStore()) {
            checkUpdateButton.setText(LocaleController.getString("DownloadUpdate", R.string.DownloadUpdate));
        } else {
            checkUpdateButton.setText(LocaleController.getString("CheckUpdates", R.string.CheckUpdates));
        }
        setTime(OctoConfig.lastUpdateCheck);
        updateTitle();
        canCheckForUpdate = true;
    }

    public void setDownloaded() {
        checkUpdateButton.setText(LocaleController.getString("InstallUpdate", R.string.InstallUpdate));
    }

    public void setFailedStatus() {
        valueTextView.setText(LocaleController.getString("CheckFailed", R.string.CheckFailed));
        checkUpdateButton.setText(LocaleController.getString("CheckUpdates", R.string.CheckUpdates));
        canCheckForUpdate = true;
        updateTitle();
    }

    public void setCanCheckForUpdate(boolean canCheckForUpdate) {
        this.canCheckForUpdate = canCheckForUpdate;
        checkUpdateButton.setAlpha(canCheckForUpdate ? 1.0f : 0.5f);
        titleTextView.setAlpha(canCheckForUpdate ? 1.0f : 0.5f);
        valueTextView.setAlpha(canCheckForUpdate ? 1.0f : 0.5f);
    }

    public void loadLastStatus() {
        switch (OctoConfig.lastUpdateStatus) {
            case 1:
                if (UpdateManager.isAvailableUpdate()) {
                    setUpdateAvailableStatus();
                } else {
                    setCheckTime();
                }
                break;
            case 2:
                setFailedStatus();
                break;
            default:
                setCheckTime();
                break;
        }
    }

    protected void onCheckUpdate() {
    }
}
