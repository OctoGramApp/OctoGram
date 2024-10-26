package it.octogram.android.preferences.ui.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;

import java.util.Objects;

import it.octogram.android.Datacenter;
import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.ui.components.RadialProgressView;
import it.octogram.android.utils.DatacenterController;

public class DatacenterBottomSheet extends BottomSheet {
    public DatacenterBottomSheet(BaseFragment fragment, Datacenter generalInfo, DatacenterController.DCStatus datacenterInfo) {
        super(fragment.getParentActivity(), false);
        Context context = fragment.getParentActivity();
        setOpenNoDelay(true);
        fixNavigationBar();

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        float scaleLevel = (float) 33 / 25;
        RelativeLayout relativeLayout = new RelativeLayout(context);
        RadialProgressView radialProgressView = new RadialProgressView(context, Color.TRANSPARENT);
        radialProgressView.setDrawRadialBackShadow(false);
        radialProgressView.setScaleY(scaleLevel);
        radialProgressView.setScaleX(scaleLevel);
        AppCompatImageView imageView = new AppCompatImageView(context);
        relativeLayout.addView(radialProgressView, LayoutHelper.createRelative(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        relativeLayout.addView(imageView, LayoutHelper.createRelative(35, 35, RelativeLayout.CENTER_IN_PARENT));
        linearLayout.addView(relativeLayout, LayoutHelper.createLinear(70, 70, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 0));

        TextView nameView = new TextView(context);
        nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        nameView.setTypeface(AndroidUtilities.bold());
        nameView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        nameView.setGravity(Gravity.CENTER);
        linearLayout.addView(nameView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 21, 12, 21, 0));

        TextView statusView = new TextView(context);
        statusView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        statusView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        statusView.setGravity(Gravity.CENTER);
        linearLayout.addView(statusView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 21, 4, 21, 21));

        Drawable d = ContextCompat.getDrawable(getContext(), generalInfo.getIcon());
        if (d != null) {
            d.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhite), PorterDuff.Mode.SRC_ATOP));
            imageView.setImageBitmap(drawableToBitmap(d));
        }
        imageView.setBackgroundResource(generalInfo.getIcon());
        radialProgressView.setColor(generalInfo.getColor());

        nameView.setText(generalInfo.getDcName());

        String statusText;
        int colorKey;
        if (datacenterInfo.status == 0) {
            statusText = LocaleController.getString(R.string.Unavailable);
            colorKey = Theme.key_windowBackgroundWhiteGrayText;
        } else if (datacenterInfo.status == 1) {
            statusText = LocaleController.getString(R.string.Available);
            colorKey = Theme.key_windowBackgroundWhiteGreenText;
        } else {
            statusText = LocaleController.getString(R.string.SpeedSlow);
            colorKey = Theme.key_statisticChartLine_orange;
        }
        statusView.setText(statusText);
        statusView.setTextColor(Theme.getColor(colorKey));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(datacenterInfo.ping);
        stringBuilder.append("ms");
        Drawable drawable = Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.menu_hashtag)).mutate();
        drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.SRC_IN));
        ItemView pingItemView = new ItemView(context, false);
        pingItemView.needDivider = true;
        pingItemView.iconView.setImageDrawable(drawable);
        pingItemView.valueText.setText(stringBuilder);
        pingItemView.descriptionText.setText(LocaleController.getString(R.string.DatacenterStatusSheetPing));
        linearLayout.addView(pingItemView);

        drawable = Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.menu_feature_links)).mutate();
        drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.SRC_IN));
        ItemView ipItemView = new ItemView(context, false);
        ipItemView.iconView.setImageDrawable(drawable);
        ipItemView.valueText.setText(generalInfo.getIp());
        ipItemView.descriptionText.setText(LocaleController.getString(R.string.DatacenterStatusSheetIP));
        applyCopyItem(ipItemView, generalInfo.getIp());
        linearLayout.addView(ipItemView);

        ItemView prevItem = ipItemView;
        if (datacenterInfo.lastLag != 0) {
            String lastLagString = LocaleController.formatDateTime(datacenterInfo.lastLag, true);
            drawable = Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.menu_views_recent)).mutate();
            drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.SRC_IN));
            ItemView lastLag = new ItemView(context, false);
            lastLag.iconView.setImageDrawable(drawable);
            lastLag.valueText.setText(lastLagString);
            lastLag.descriptionText.setText(LocaleController.getString(R.string.DatacenterStatusSheetLastLag));
            applyCopyItem(lastLag, lastLagString);
            linearLayout.addView(lastLag);

            prevItem.needDivider = true;
            prevItem = lastLag;
        }

        if (datacenterInfo.lastDown != 0) {
            String lastDownString = LocaleController.formatDateTime(datacenterInfo.lastDown, true);
            drawable = Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.menu_views_recent)).mutate();
            drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.SRC_IN));
            ItemView lastDown = new ItemView(context, false);
            lastDown.iconView.setImageDrawable(drawable);
            lastDown.valueText.setText(lastDownString);
            lastDown.descriptionText.setText(LocaleController.getString(R.string.DatacenterStatusSheetLastDowntime));
            applyCopyItem(lastDown, lastDownString);
            linearLayout.addView(lastDown);

            prevItem.needDivider = true;
        }

        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(linearLayout);
        setCustomView(scrollView);
    }

    public static Bitmap drawableToBitmap(@NonNull Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void applyCopyItem(ItemView item, String copyValue) {
        item.setOnClickListener(view -> copyText(copyValue));
        item.setOnLongClickListener(view -> {
            copyText(copyValue);
            return true;
        });
    }

    private void copyText(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setItems(new CharSequence[]{LocaleController.getString(R.string.Copy)}, (dialogInterface, i) -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("label", text);
            clipboard.setPrimaryClip(clip);
            BulletinFactory.of(getContainer(), null).createCopyBulletin(LocaleController.getString(R.string.TextCopied)).show();
        });
        builder.show();
    }

    private static class ItemView extends FrameLayout {

        ImageView iconView;
        TextView valueText;
        TextView descriptionText;
        boolean needDivider = false;

        public ItemView(Context context, boolean needSwitch) {
            super(context);
            iconView = new ImageView(context);
            iconView.setScaleType(ImageView.ScaleType.CENTER);
            addView(iconView, LayoutHelper.createFrame(32, 32, 0, 12, 4, 0, 0));

            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 64, 4, 0, 4));

            valueText = new TextView(context);
            valueText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            valueText.setGravity(Gravity.LEFT);
            valueText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            linearLayout.addView(valueText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, needSwitch ? 64 : 0, 0));

            descriptionText = new TextView(context);
            descriptionText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            descriptionText.setGravity(Gravity.LEFT);
            descriptionText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            linearLayout.addView(descriptionText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 4, needSwitch ? 64 : 0, 0));
            setPadding(0, AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4));
        }

        @Override
        protected void dispatchDraw(@NonNull Canvas canvas) {
            super.dispatchDraw(canvas);
            if (needDivider && !OctoConfig.INSTANCE.disableDividers.getValue()) {
                canvas.drawRect(AndroidUtilities.dp(64), getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight(), Theme.dividerPaint);
            }
        }
    }
}
