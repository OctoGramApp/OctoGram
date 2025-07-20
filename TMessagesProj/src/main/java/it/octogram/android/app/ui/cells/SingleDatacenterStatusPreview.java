/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui.cells;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CircularProgressDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.TextStyleSpan;

import java.text.DecimalFormat;
import java.util.Locale;

import it.octogram.android.Datacenter;
import it.octogram.android.app.ui.DcStatusActivity;
import it.octogram.android.app.ui.components.RadialProgressView;

@SuppressLint("ViewConstructor")
public class SingleDatacenterStatusPreview extends LinearLayout {
    public static int UNAVAILABLE = 0;
    public static int AVAILABLE = 1;
    public static int SLOW = 2;
    public static int WAITING_FOR_USER = 3;
    public static int DOWNLOADING = 4;
    public static int DOWNLOAD_END = 5;
    public static int DOWNLOAD_FAILED = 6;
    public static int DOWNLOAD_FAILED_TRY_LATER = 7;
    public static int INTERRUPTED = 8;

    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private final RadialProgressView radialProgressView;
    private final AppCompatTextView textView;
    private final AppCompatTextView ipTextView;
    private final AppCompatTextView statusTextView;
    private final AppCompatImageView imageView;
    private final LinearLayout linearLayout;
    private final RelativeLayout relativeLayout;
    private boolean needDivider = false;
    private final boolean isMediaPage;

    private AppCompatImageView altImageView = null;
    private Datacenter dcInfo;

    private int _status;

    private boolean wasWaiting = false;
    private boolean wasDownloading = false;
    private CircularProgressDrawable loadingDrawable;
    private boolean loading = false;
    private float loadingT = 0;

    @SuppressLint("SetTextI18n")
    public SingleDatacenterStatusPreview(DcStatusActivity fragment, Context context, int dcId, boolean isMediaPage) {
        super(context);

        this.isMediaPage = isMediaPage;

        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(dp(13), dp(5), dp(13), dp(5));
        relativeLayout = new RelativeLayout(context);
        radialProgressView = new RadialProgressView(context, Color.TRANSPARENT);
        imageView = new AppCompatImageView(context);
        relativeLayout.addView(radialProgressView, LayoutHelper.createRelative(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        relativeLayout.addView(imageView, LayoutHelper.createRelative(25, 25, RelativeLayout.CENTER_IN_PARENT));
        addView(relativeLayout, LayoutHelper.createLinear(65, 65));

        if (isMediaPage) {
            altImageView = new AppCompatImageView(context);
            altImageView.setAlpha(0f);
            relativeLayout.addView(altImageView, LayoutHelper.createRelative(25, 25, RelativeLayout.CENTER_IN_PARENT));
        }

        linearLayout = new LinearLayout(context);
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
        linearLayout.setOrientation(VERTICAL);
        addView(linearLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        textView = new AppCompatTextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setTextSize(16);
        linearLayout.addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 10, 0, 0, 0));

        ipTextView = new AppCompatTextView(context);
        ipTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        ipTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        ipTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        ipTextView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        linearLayout.addView(ipTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 10, 0, 0, 0));

        statusTextView = new AppCompatTextView(context);
        statusTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        statusTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        statusTextView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        linearLayout.addView(statusTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 10, 0, 0, 0));

        LinearLayout cell = this;
        setFocusable(true);
        setClickable(true);
        setEnabled(true);
        setOnClickListener(v -> {
            if (loading || dcInfo == null || (isMediaPage && fragment.mediaPage.isMonitoring())) {
                return;
            }

            ItemOptions options = ItemOptions.makeOptions(fragment, cell);
            options.addIf(isMediaPage && !wasWaiting, R.drawable.menu_browser_refresh, getString(R.string.Refresh), () -> fragment.mediaPage.datacenterMediaController.startFetching(dcInfo.getDcId()));
            options.addIf(isMediaPage && wasWaiting, R.drawable.media_photo_flash_on2, getString(R.string.DatacenterStatusSection_Start_Media), () -> fragment.mediaPage.datacenterMediaController.startFetching(dcInfo.getDcId()));
            options.add(R.drawable.msg_copy, getString(R.string.DatacenterStatusSection_CopyIP), () -> {
                AndroidUtilities.addToClipboard(dcInfo.getIp());
                BulletinFactory.of(fragment).createCopyBulletin(getString(R.string.DatacenterStatusSection_CopyIP_Text)).show();
            });
            if (isMediaPage) {
                options.addGap();
                if (_status == DOWNLOAD_FAILED_TRY_LATER) {
                    options.addText(getString(R.string.DatacenterStatusSection_DwFailed_DiscoverMode_Text), 13, dp(250));
                } else {
                    options.addText(formatString(R.string.DatacenterStatusSection_Status_Downloads, dcInfo.getDcId() == 3 ? 1 : 10), 13, dp(200));
                }
            }
            if (LocaleController.isRTL) {
                options.setGravity(Gravity.LEFT);
            }
            options.show();
        });

        setWillNotDraw(false);

        loadData(dcId);
    }

    private void loadData(int dcId) {
        dcInfo = Datacenter.Companion.getDcInfo(dcId);

        Drawable d = ContextCompat.getDrawable(getContext(), dcInfo.getIcon());
        if (d != null) {
            d.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhite), PorterDuff.Mode.SRC_ATOP));
            imageView.setImageBitmap(drawableToBitmap(d));

            if (altImageView != null) {
                d.setColorFilter(new PorterDuffColorFilter(dcInfo.getColor(), PorterDuff.Mode.SRC_ATOP));
                altImageView.setImageBitmap(drawableToBitmap(d));
            }
        }
        imageView.setBackgroundResource(dcInfo.getIcon());
        if (altImageView != null) {
            altImageView.setBackgroundResource(dcInfo.getIcon());
        }

        radialProgressView.setColor(dcInfo.getColor());

        TextStyleSpan.TextStyleRun run = new TextStyleSpan.TextStyleRun();
        run.flags |= TextStyleSpan.FLAG_STYLE_BOLD;
        TextStyleSpan mSpan = new TextStyleSpan(run);
        String DC_NAME = dcInfo.getDcName();
        if (dcId != -1) {
            DC_NAME = String.format(Locale.ENGLISH, "%s - DC%d", DC_NAME, dcId);
        }
        SpannableString spannableString = new SpannableString(DC_NAME);
        spannableString.setSpan(mSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        textView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        textView.setText(spannableString);
        ipTextView.setText(dcInfo.getIp());

        setData(WAITING_FOR_USER, 0, dcId != 5);
    }

    public void setData(int status, int parameter, boolean needDivider) {
        if (_status == status && status != DOWNLOADING && status != SLOW && status != AVAILABLE) {
            return;
        }
        _status = status;

        this.needDivider = needDivider;

        String statusText = null;
        int colorKey = 0;
        if (status == UNAVAILABLE && !isMediaPage) {
            statusText = getString(R.string.Unavailable);
            colorKey = Theme.key_windowBackgroundWhiteGrayText;
        } else if (status == AVAILABLE && !isMediaPage) {
            statusText = getString(R.string.Available);
            colorKey = Theme.key_windowBackgroundWhiteGreenText;
        } else if (status == SLOW && !isMediaPage) {
            statusText = getString(R.string.SpeedSlow);
            colorKey = Theme.key_statisticChartLine_orange;
        } else if (status == DOWNLOADING && isMediaPage) {
            statusText = getString(R.string.Downloading);
            colorKey = Theme.key_windowBackgroundWhiteGrayText;
        } else if (status == DOWNLOAD_END && isMediaPage) {
            statusText = getString(R.string.DatacenterStatusSection_DwCompleted);
            colorKey = Theme.key_windowBackgroundWhiteGreenText;
        } else if ((status == DOWNLOAD_FAILED || status == DOWNLOAD_FAILED_TRY_LATER) && isMediaPage) {
            statusText = getString(R.string.DatacenterStatusSection_DwFailed);
            colorKey = Theme.key_windowBackgroundWhiteGrayText;

            if (status == DOWNLOAD_FAILED_TRY_LATER) {
                statusText += " — " + getString(R.string.DatacenterStatusSection_DwFailed_DiscoverMode);
            }
        } else if (status == INTERRUPTED && isMediaPage) {
            statusText = getString(R.string.DatacenterStatusSection_Interrupted);
            colorKey = Theme.key_windowBackgroundWhiteGrayText;
        }

        setLoading(status == DOWNLOADING);

        if (statusText != null && colorKey != 0) {
            if ((status == AVAILABLE || status == SLOW) && !isMediaPage) {
                statusText += ", " + formatString(R.string.Ping, parameter);
            }
            if (status == DOWNLOADING && isMediaPage) {
                statusText += ", " + parameter + "%";
            }
            if (status == DOWNLOAD_END && isMediaPage) {
                statusText += ", " + decimalFormat.format(10f / parameter) + " MB/s";
            }
            statusTextView.setVisibility(VISIBLE);
            statusTextView.setText(statusText);
            statusTextView.setTextColor(Theme.getColor(colorKey));
        }

        boolean isWaiting = status == WAITING_FOR_USER;
        if (isWaiting != wasWaiting) {
            wasWaiting = isWaiting;
            linearLayout.animate().setDuration(250).cancel();
            linearLayout.setAlpha(isWaiting ? 1f : 0.3f);
            linearLayout.animate().alpha(isWaiting ? 0.3f : 1f).setDuration(250).start();
        }

        boolean isDownloading = status == DOWNLOADING;
        if (isDownloading != wasDownloading && isMediaPage) {
            wasDownloading = isDownloading;
            radialProgressView.setPaused(isDownloading);
        }
    }

    public static Bitmap drawableToBitmap(@NonNull Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider) {
            canvas.drawLine(
                    LocaleController.isRTL ? 0 : dp(20),
                    getMeasuredHeight() - 1,
                    getMeasuredWidth() - (LocaleController.isRTL ? dp(20) : 0),
                    getMeasuredHeight() - 1,
                    Theme.dividerPaint
            );
        }

        if (loadingT > 0 && isMediaPage) {
            float centerX = relativeLayout.getLeft() + (relativeLayout.getRight() - relativeLayout.getLeft()) / 2f;
            float centerY = relativeLayout.getTop() + (relativeLayout.getBottom() - relativeLayout.getTop()) / 2f;

            if (loadingDrawable == null) {
                loadingDrawable = new CircularProgressDrawable(
                        dp((relativeLayout.getRight() - relativeLayout.getLeft()) / 3f),
                        dp(2.5f),
                        Theme.getColor(Theme.key_windowBackgroundWhiteBlackText)
                );
            }
            if (dcInfo != null) {
                loadingDrawable.setColor(dcInfo.getColor());
            }
            loadingDrawable.setBounds(
                    (int) (centerX - loadingDrawable.getIntrinsicWidth() / 2f),
                    (int) (centerY - loadingDrawable.getIntrinsicHeight() / 2f),
                    (int) (centerX + loadingDrawable.getIntrinsicWidth() / 2f),
                    (int) (centerY + loadingDrawable.getIntrinsicHeight() / 2f)
            );
            loadingDrawable.setAlpha((int) (0xFF * loadingT));
            radialProgressView.setAlpha(1 - loadingT);
            altImageView.setAlpha(loadingT);
            loadingDrawable.draw(canvas);
            invalidate();
        }
    }

    private ValueAnimator loadingAnimator;

    public void setLoading(boolean loading) {
        if (!isMediaPage) {
            return;
        }

        if (this.loading != loading) {
            if (loadingAnimator != null) {
                loadingAnimator.cancel();
                loadingAnimator = null;
            }
            var _loading = this.loading = loading;
            loadingAnimator = ValueAnimator.ofFloat(loadingT, _loading ? 1 : 0);
            loadingAnimator.addUpdateListener(anm -> {
                loadingT = (float) anm.getAnimatedValue();
                invalidate();
            });
            loadingAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loadingT = loading ? 1 : 0;
                    invalidate();
                }
            });
            loadingAnimator.setDuration(320);
            loadingAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
            loadingAnimator.start();
        }
    }
}
