/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.CircularProgressDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.FilledTabsView;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.Components.TimerParticles;
import org.telegram.ui.Components.ViewPagerFixed;
import org.telegram.ui.LaunchActivity;

import java.util.HashMap;
import java.util.Locale;

import it.octogram.android.OctoConfig;
import it.octogram.android.WebPages;
import it.octogram.android.WebPagesCategory;
import it.octogram.android.app.ui.cells.DcStatusHeaderCell;
import it.octogram.android.app.ui.cells.SingleDatacenterStatusPreview;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.appearance.MessageStringHelper;
import it.octogram.android.utils.deeplink.DeepLinkDef;
import it.octogram.android.utils.media.DcMediaController;
import it.octogram.android.utils.network.DatacenterController;
import it.octogram.android.utils.network.WebPingController;

public class DcStatusActivity extends BaseFragment {

    public static final int PAGE_NETWORK = 0;
    public static final int PAGE_MEDIA = 1;
    public static final int PAGE_WEB = 2;

    private DcStatusActivity fragment;

    public Page networkPage;
    public Page mediaPage;
    public Page webPage;

    private String parameter;

    public Page getCurrentPage() {
        return viewPager.getCurrentPosition() == 0 ? networkPage : (viewPager.getCurrentPosition() == 1 ? mediaPage : webPage);
    }

    public class Page extends FrameLayout {

        private final int type;
        private final ScrollView scrollView;
        private final DcStatusHeaderCell dcStatusHeaderCell;
        private ReloadStateCell reloadStateCell = null;
        private DatacenterController.DatacenterStatusChecker datacenterStatusChecker;
        private final HashMap<Integer, SingleDatacenterStatusPreview> viewsAssoc = new HashMap<>();
        private final HashMap<Integer, WebPageDetailsCell> viewsAssocWebPage = new HashMap<>();

        public DcMediaController datacenterMediaController;

        private WebPingController webPingController;

        private boolean waitingForUserStart = true;
        private long lastActivityStart = 0;

        public Page(@NonNull Context context, int type) {
            super(context);

            this.type = type;

            scrollView = new ScrollView(context);
            scrollView.setFillViewport(true);

            LinearLayout contentLayout = new LinearLayout(context);
            contentLayout.setOrientation(LinearLayout.VERTICAL);

            dcStatusHeaderCell = new DcStatusHeaderCell(context, type, this::startMonitor);
            contentLayout.addView(dcStatusHeaderCell);

            HeaderCell headerCell;

            if (type != PAGE_WEB) {
                headerCell = new HeaderCell(context);
                headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                headerCell.setText(getString(type == PAGE_NETWORK ? R.string.DatacenterStatus : R.string.DatacenterStatus_Media));
                contentLayout.addView(headerCell);

                for (int i = 1; i <= 5; i++) {
                    SingleDatacenterStatusPreview view = new SingleDatacenterStatusPreview(fragment, context, i, type == PAGE_MEDIA);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    viewsAssoc.put(i, view);
                    contentLayout.addView(view);
                }
            } else {
                int categoryId = -1;
                WebPageDetailsCell lastView = null;
                for (WebPages page : WebPages.getEntries()) {
                    if (page.getCategory() != categoryId) {
                        if (lastView != null) {
                            lastView.setNeedDivider(false);
                        }

                        categoryId = page.getCategory();
                        if (categoryId != 1) {
                            contentLayout.addView(new ShadowSectionCell(context));
                        }

                        headerCell = new HeaderCell(context);
                        headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                        headerCell.setText(WebPagesCategory.Companion.getPageInfo(categoryId).getText());
                        contentLayout.addView(headerCell);
                    }

                    WebPageDetailsCell view = new WebPageDetailsCell(context);
                    view.setData(page, !OctoConfig.INSTANCE.disableDividers.getValue());
                    viewsAssocWebPage.put(page.getId(), view);
                    contentLayout.addView(view);
                    lastView = view;
                }
            }

            if (type == PAGE_NETWORK) {
                contentLayout.addView(new ShadowSectionCell(context));

                headerCell = new HeaderCell(context);
                headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                headerCell.setText(getString(R.string.DatacenterStatusSection_NextUpdate));
                contentLayout.addView(headerCell);

                contentLayout.addView(reloadStateCell = new ReloadStateCell(context));
            }

            var htmlParsed = new SpannableString(MessageStringHelper.fromHtml(formatString(R.string.DatacenterStatusSection_TermsAccept, OctoUtils.getDomain())));
            TextInfoPrivacyCell textInfo = new TextInfoPrivacyCell(context);
            textInfo.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, getThemedColor(Theme.key_windowBackgroundGrayShadow)));
            textInfo.setText(MessageStringHelper.getUrlNoUnderlineText(htmlParsed));
            contentLayout.addView(textInfo);

            scrollView.addView(contentLayout);
            setWillNotDraw(false);

            addView(scrollView);
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

            if (type == PAGE_NETWORK) {
                datacenterStatusChecker = new DatacenterController.DatacenterStatusChecker();
                datacenterStatusChecker.setOnUpdate(new DatacenterController.DatacenterStatusChecker.UpdateCallback() {
                    @Override
                    public void onUpdate(int dcId, int status, int parameter) {
                        SingleDatacenterStatusPreview view = viewsAssoc.get(dcId);
                        if (view != null) {
                            AndroidUtilities.runOnUIThread(() -> view.setData(status, parameter, dcId != 5));
                        }
                    }

                    @Override
                    public void onNewCycle() {
                        AndroidUtilities.runOnUIThread(() -> reloadStateCell.onUpdated());
                    }
                });
            } else if (type == PAGE_MEDIA) {
                datacenterMediaController = new DcMediaController();
                datacenterMediaController.setCallback(new DcMediaController.OnFetchResult() {
                    @Override
                    public void onUpdate(int dcId, int status, int parameter, int parsedDcs) {
                        SingleDatacenterStatusPreview view = viewsAssoc.get(dcId);
                        if (view != null) {
                            AndroidUtilities.runOnUIThread(() -> view.setData(status, parameter, dcId != 5));
                        }

                        if (parsedDcs == 5) {
                            AndroidUtilities.runOnUIThread(() -> forceStopMonitor());
                        }
                    }

                    @Override
                    public void onFailed() {
                        AndroidUtilities.runOnUIThread(() -> forceStopMonitor());

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), getResourceProvider());
                        builder.setTitle(getString(R.string.AppName));
                        builder.setMessage(getString(R.string.DatacenterStatusSection_Failed));
                        builder.setNegativeButton(getString(R.string.OK), null);
                        builder.show();
                    }
                });
            } else {
                webPingController = new WebPingController();
                webPingController.setCallback(new WebPingController.UpdateCallback() {
                    @Override
                    public void onUpdate(int pageId, int status, int parameter) {
                        WebPageDetailsCell view = viewsAssocWebPage.get(pageId);
                        if (view != null) {
                            AndroidUtilities.runOnUIThread(() -> view.setStatus(status, parameter));
                        }
                    }

                    @Override
                    public void onCompletedCycle() {
                        AndroidUtilities.runOnUIThread(() -> forceStopMonitor());
                    }
                });
            }
        }

        private void startMonitor() {
            startMonitor(false);
        }

        private void startMonitor(boolean forced) {
            if (type == PAGE_MEDIA && needsPermissions()) {
                waitingForUserStart = true;
                return;
            }

            if (waitingForUserStart) {
                int connectionState = ConnectionsManager.getInstance(UserConfig.selectedAccount).getConnectionState();

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), getResourceProvider());
                if (!forced && connectionState != ConnectionsManager.ConnectionStateConnected && connectionState != ConnectionsManager.ConnectionStateUpdating) {
                    builder.setTitle(getString(R.string.DatacenterStatusSection_InternetIssue));
                    builder.setMessage(getString(R.string.DatacenterStatusSection_InternetIssue_Desc));
                    builder.setPositiveButton(getString(R.string.Proceed), (dialogInterface, i) -> startMonitor(true));
                    builder.setNegativeButton(getString(R.string.Cancel), null);
                    builder.show();
                    return;
                } else if (type == PAGE_MEDIA && ConnectionsManager.native_isTestBackend(UserConfig.selectedAccount) != 0) {
                    builder.setTitle(getString(R.string.DatacenterStatusSection_TestBackend));
                    builder.setMessage(getString(R.string.DatacenterStatusSection_TestBackend_Desc));
                    builder.setPositiveButton(getString(R.string.OK), null);
                    builder.show();
                    return;
                }

                if (((System.currentTimeMillis() - lastActivityStart) / 1000) < (type == PAGE_MEDIA ? 5 : 1)) {
                    if (type == PAGE_MEDIA) {
                        builder.setTitle(getString(R.string.AppName));
                        builder.setMessage(getString(R.string.DatacenterStatusSection_WaitPlease_Desc));
                        builder.setPositiveButton(getString(R.string.OK), null);
                        builder.show();
                    }
                    return;
                }
            }

            if (type == PAGE_NETWORK) {
                if (datacenterStatusChecker == null) {
                    return;
                }

                if (!waitingForUserStart) {
                    datacenterStatusChecker.stop();
                    reloadStateCell.onPaused();
                } else {
                    datacenterStatusChecker.runListener();
                }
            } else if (type == PAGE_MEDIA) {
                if (datacenterMediaController == null) {
                    return;
                }

                if (!waitingForUserStart) {
                    datacenterMediaController.destroy();
                } else {
                    datacenterMediaController.startFetching();
                }
            } else if (type == PAGE_WEB) {
                if (webPingController == null) {
                    return;
                }

                if (!waitingForUserStart) {
                    webPingController.stopMonitor();
                } else {
                    webPingController.pingSites();
                }
            }

            waitingForUserStart = !waitingForUserStart;
            dcStatusHeaderCell.updateStatus(waitingForUserStart);

            if (!waitingForUserStart) {
                lastActivityStart = System.currentTimeMillis();
            }
        }

        private boolean needsPermissions() {
            if (Build.VERSION.SDK_INT >= 23 && (Build.VERSION.SDK_INT <= 28 || BuildVars.NO_SCOPED_STORAGE) && getParentActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                getParentActivity().requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4);
                return true;
            }

            return false;
        }

        public void forceStopMonitor() {
            if (!waitingForUserStart) {
                startMonitor();
            }
        }

        public boolean isMonitoring() {
            return !waitingForUserStart;
        }

        private int actionBarHeight;

        @Override
        protected void dispatchDraw(@NonNull Canvas canvas) {
            super.dispatchDraw(canvas);
            if (getParentLayout() != null) {
                getParentLayout().drawHeaderShadow(canvas, actionBarHeight);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            actionBarHeight = ActionBar.getCurrentActionBarHeight() + AndroidUtilities.statusBarHeight;
            ((MarginLayoutParams) scrollView.getLayoutParams()).topMargin = actionBarHeight;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private static class ReloadStateCell extends FrameLayout {
        private final AnimatedTextView titleView;
        private final AnimatedTextView subtitleView;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        RectF rectF = new RectF();

        boolean drawDivider;
        private final Drawable updateDrawable;
        private final Drawable starDrawable;


        public ReloadStateCell(@NonNull Context context) {
            super(context);

            paint2.setStyle(Paint.Style.STROKE);
            paint2.setStrokeCap(Paint.Cap.ROUND);

            LinearLayout textLayout = new LinearLayout(context);
            textLayout.setOrientation(LinearLayout.VERTICAL);
            addView(textLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 45 * 2, 0, 30, 0));

            titleView = new AnimatedTextView(context);
            titleView.setTextSize(dp(16));
            titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            textLayout.addView(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, dp(8)));

            subtitleView = new AnimatedTextView(context);
            subtitleView.setTextSize(dp(13));
            subtitleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            textLayout.addView(subtitleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, dp(5), 0, 2, 0, 0));

            updateDrawable = ContextCompat.getDrawable(context, R.drawable.pip_replay_large);
            if (updateDrawable != null) {
                updateDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_featuredStickers_buttonText), PorterDuff.Mode.SRC_IN));
            }

            starDrawable = ContextCompat.getDrawable(context, R.drawable.star_24px);
            if (starDrawable != null) {
                starDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_featuredStickers_buttonText), PorterDuff.Mode.SRC_IN));
            }

            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            setWillNotDraw(false);
            onPaused();
        }

        boolean timerRunning = false;
        private long startedAt;

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(dp(60), MeasureSpec.EXACTLY));
            paint2.setStrokeWidth(dp(2));
        }

        private final TimerParticles timerParticles = new TimerParticles();
        private int lastUpdatedSeconds = 60;

        public void onUpdated() {
            timerRunning = true;
            startedAt = System.currentTimeMillis();
            lastUpdatedSeconds = 60;
            titleView.setText(LocaleController.getInstance().getFormatterDayWithSeconds().format(startedAt + lastUpdatedSeconds * 1000L), true);
            subtitleView.setText(formatString(R.string.DatacenterStatusSection_NextUpdate_InSeconds, lastUpdatedSeconds), true);
            invalidate();
        }

        public void onPaused() {
            timerRunning = false;
            titleView.setText(getString(R.string.DatacenterStatusSection_NextUpdate_Waiting), true);
            subtitleView.setText(getString(R.string.DatacenterStatusSection_NextUpdate_Waiting_Desc), true);
            invalidate();
        }

        private float expandableState = 0;

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            int cX = dp(45);
            int cY = getMeasuredHeight() / 2;

            int secondsLeft = Utilities.clamp((int) ((startedAt + 60 * 1000L - System.currentTimeMillis()) / 1000), 60, 0);
            if (timerRunning) {
                if (lastUpdatedSeconds != secondsLeft) {
                    lastUpdatedSeconds = secondsLeft;
                    subtitleView.setText(formatString(R.string.DatacenterStatusSection_NextUpdate_InSeconds, lastUpdatedSeconds), true);
                }

                if (expandableState != 1f) {
                    expandableState += 16f / 250f;
                    if (expandableState > 1f) {
                        expandableState = 1f;
                    }
                }
            } else if (expandableState != 0f) {
                expandableState -= 16f / 250f;
                if (expandableState < 0f) {
                    expandableState = 0f;
                }
            }

            float progress = secondsLeft / 60f;

            paint.setColor(Theme.getColor(Theme.key_featuredStickers_addButton));
            canvas.drawCircle(cX, cY, dp(32) / 2f + dp(5) * (1f - expandableState), paint);
            paint2.setColor(Theme.getColor(Theme.key_featuredStickers_addButton));
            paint2.setAlpha((int) (expandableState * 255));
            rectF.set(cX - dp(20), cY - dp(20), cX + dp(20), cY + dp(20));
            canvas.drawArc(rectF, -90, -progress * 360, false, paint2);
            timerParticles.draw(canvas, paint2, rectF, -progress * 360, expandableState);

            updateDrawable.setBounds(cX - dp(12), cY - dp(12), cX + dp(12), cY + dp(12));
            updateDrawable.setAlpha((int) (expandableState * 255));
            updateDrawable.draw(canvas);

            starDrawable.setBounds(cX - dp(12), cY - dp(12), cX + dp(12), cY + dp(12));
            starDrawable.setAlpha(255 - (int) (expandableState * 255));
            starDrawable.draw(canvas);

            if (drawDivider) {
                canvas.drawLine(dp(70), getMeasuredHeight() - 1, getMeasuredWidth() + dp(23), getMeasuredHeight(), Theme.dividerPaint);
            }

            invalidate();
        }
    }

    public class WebPageDetailsCell extends TextCell {
        private int _status = WebPingController.WAITING_FOR_USER;
        private ValueAnimator loadingAnimator;
        private CircularProgressDrawable loadingDrawable;
        private boolean loading = false;
        private float loadingT;
        private Drawable resIdDrawable;
        private WebPages page;

        public WebPageDetailsCell(Context context) {
            super(context);
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            textView.setAlpha(0.3f);
            valueTextView.setAlpha(0.3f);

            TextCell cell = this;

            setOnClickListener(v -> {
                if (loading || webPage.isMonitoring()) {
                    return;
                }

                ItemOptions options = ItemOptions.makeOptions(DcStatusActivity.this.fragment, cell);
                options.addIf(_status != WebPingController.WAITING_FOR_USER, R.drawable.menu_browser_refresh, getString(R.string.Refresh), () -> webPage.webPingController.pingSites(page.getId()));
                options.addIf(_status == WebPingController.WAITING_FOR_USER, R.drawable.media_photo_flash_on2, getString(R.string.DatacenterStatusSection_Start_Ping), () -> webPage.webPingController.pingSites(page.getId()));
                options.addIf(page != null, R.drawable.open_in_new_24px, getString(R.string.OpenInExternalApp), () -> Browser.openUrl(context, page.getWebsite()));
                if (LocaleController.isRTL) {
                    options.setGravity(Gravity.LEFT);
                }
                options.show();
            });
        }

        public void setData(WebPages page, boolean divider) {
            this.page = page;
            setTextAndValueAndIcon(page.getPageName(), "", R.drawable.msg2_help, divider); // random icon
            imageView.setAlpha(0f);
            resIdDrawable = ContextCompat.getDrawable(getContext(), page.getIcon());
            if (resIdDrawable != null) {
                resIdDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.SRC_IN));
            }
            invalidate();
        }

        public void setStatus(int status, int parameter) {
            if (_status == status) {
                return;
            }

            boolean wasHidden = _status == WebPingController.WAITING_FOR_USER;

            _status = status;

            if (wasHidden) {
                ValueAnimator cellUpdate = ValueAnimator.ofFloat(0.3f, 1f);
                cellUpdate.addUpdateListener(a -> {
                    float alpha = (float) a.getAnimatedValue();

                    textView.setAlpha(alpha);
                    valueTextView.setAlpha(alpha);
                });
                cellUpdate.setDuration(250);
                cellUpdate.start();
            }

            String statusText = null;
            int colorKey = 0;
            if (status == WebPingController.PINGING) {
                statusText = "Ping...";
                colorKey = Theme.key_windowBackgroundWhiteGrayText;
            } else if (status == WebPingController.CONNECTED) {
                statusText = "OK";
                colorKey = Theme.key_windowBackgroundWhiteGreenText;
            } else if (status == WebPingController.FAILED) {
                statusText = getString(R.string.Unavailable);
                colorKey = Theme.key_windowBackgroundWhiteGrayText;
            } else if (status == WebPingController.INTERRUPTED) {
                statusText = getString(R.string.DatacenterStatusSection_Interrupted);
                colorKey = Theme.key_windowBackgroundWhiteGrayText;
            }

            if (parameter > 0 && status == WebPingController.CONNECTED) {
                statusText += " (" + parameter + " ms)";
            }

            if (parameter > 0 && status == WebPingController.FAILED) {
                statusText += " (" + parameter + ")";
            }

            setLoading(status == WebPingController.PINGING);
            setValue(statusText, !wasHidden);
            valueTextView.setTextColor(Theme.getColor(colorKey), !wasHidden);
        }

        public void setLoading(boolean loading) {
            if (this.loading != loading) {
                if (loadingAnimator != null) {
                    loadingAnimator.cancel();
                    loadingAnimator = null;
                }
                this.loading = loading;
                loadingAnimator = ValueAnimator.ofFloat(loadingT, loading ? 1 : 0);
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

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float centerX = imageView.getLeft() + (imageView.getRight() - imageView.getLeft()) / 2f;
            float centerY = getMeasuredHeight() / 2f;

            if (resIdDrawable != null) {
                resIdDrawable.setBounds(
                        (int) centerX - dp(13),
                        (int) centerY - dp(13),
                        (int) centerX + dp(13),
                        (int) centerY + dp(13)
                );
                resIdDrawable.setAlpha(255 - (int) (255 * loadingT));
                resIdDrawable.draw(canvas);
            }

            if (loadingT > 0) {
                if (loadingDrawable == null) {
                    loadingDrawable = new CircularProgressDrawable(dp(13), dp(1.5f), Theme.getColor(Theme.key_featuredStickers_addButton));
                }
                loadingDrawable.setBounds(
                        (int) (centerX - loadingDrawable.getIntrinsicWidth() / 2f),
                        (int) (centerY - loadingDrawable.getIntrinsicHeight() / 2f),
                        (int) (centerX + loadingDrawable.getIntrinsicWidth() / 2f),
                        (int) (centerY + loadingDrawable.getIntrinsicHeight() / 2f)
                );
                loadingDrawable.setAlpha((int) (0xFF * loadingT));
                loadingDrawable.draw(canvas);
                invalidate();
            }
        }
    }


    private ViewPagerFixed viewPager;

    private ImageView backButton;
    private ImageView termsButton;

    private FrameLayout actionBarContainer;
    private FilledTabsView tabsView;

    @Override
    public View createView(Context context) {
        fragment = this;

        networkPage = new Page(context, PAGE_NETWORK);
        mediaPage = new Page(context, PAGE_MEDIA);
        webPage = new Page(context, PAGE_WEB);

        actionBar.setCastShadows(false);
        actionBar.setVisibility(View.GONE);
        actionBar.setAllowOverlayTitle(false);

        FrameLayout frameLayout = getFrameLayout(context);


        viewPager = new ViewPagerFixed(context) {
            @Override
            public void onTabAnimationUpdate(boolean manual) {
                tabsView.setSelected(viewPager.getPositionAnimated());
            }

            @Override
            protected void onScrollEnd() {
                super.onScrollEnd();
                int tab = viewPager.getCurrentPosition();
                if (tab != PAGE_NETWORK) {
                    networkPage.forceStopMonitor();
                }
                if (tab != PAGE_MEDIA) {
                    mediaPage.forceStopMonitor();
                }
                if (tab != PAGE_WEB) {
                    webPage.forceStopMonitor();
                }
            }
        };
        viewPager.setAdapter(new ViewPagerFixed.Adapter() {
            @Override
            public int getItemCount() {
                return 3;
            }

            @Override
            public View createView(int viewType) {
                if (viewType == PAGE_NETWORK) return networkPage;
                if (viewType == PAGE_MEDIA) return mediaPage;
                if (viewType == PAGE_WEB) return webPage;
                return null;
            }

            @Override
            public int getItemViewType(int position) {
                return position;
            }

            @Override
            public void bindView(View view, int position, int viewType) {

            }
        });
        frameLayout.addView(viewPager, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));

        actionBarContainer = new FrameLayout(context);
        actionBarContainer.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault, getResourceProvider()));
        frameLayout.addView(actionBarContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL));


        actionBarContainer.setLongClickable(true);
        actionBarContainer.setOnLongClickListener(v -> {
            String link = String.format(Locale.US, "tg://%s", DeepLinkDef.DC_STATUS);
            if (viewPager.getCurrentPosition() != PAGE_NETWORK) {
                link += viewPager.getCurrentPosition() == PAGE_MEDIA ? "?t=media" : "?t=web";
            }
            showDialog(new ShareAlert(context, null, link, false, link, false, true));
            return true;
        });

        tabsView = new FilledTabsView(context);
        tabsView.setTabs(
                getString(R.string.DatacenterStatusSection_Tab_Network),
                getString(R.string.DatacenterStatusSection_Tab_Media),
                getString(R.string.DatacenterStatusSection_Tab_Web)
        );
        tabsView.onTabSelected(tab -> {
            if (viewPager != null) {
                viewPager.scrollToPosition(tab);
            }
            if (tab != PAGE_NETWORK) {
                networkPage.forceStopMonitor();
            }
            if (tab != PAGE_MEDIA) {
                mediaPage.forceStopMonitor();
            }
            if (tab != PAGE_WEB) {
                webPage.forceStopMonitor();
            }
        });
        tabsView.setBackgroundColor(AndroidUtilities.computePerceivedBrightness(Theme.getColor(Theme.key_actionBarDefault, getResourceProvider())) > .721f ?
                Theme.getColor(Theme.key_actionBarDefaultIcon, getResourceProvider()) :
                Theme.adaptHSV(Theme.getColor(Theme.key_actionBarDefault, getResourceProvider()), +.08f, -.08f));
        actionBarContainer.addView(tabsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 40, Gravity.CENTER));

        backButton = new ImageView(context);
        backButton.setScaleType(ImageView.ScaleType.CENTER);
        backButton.setBackground(Theme.createSelectorDrawable(getThemedColor(Theme.key_actionBarWhiteSelector), Theme.RIPPLE_MASK_CIRCLE_20DP));
        backButton.setImageResource(R.drawable.ic_ab_back);
        backButton.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_actionBarDefaultIcon), PorterDuff.Mode.SRC_IN));
        backButton.setOnClickListener(v -> {
            if (onBackPressed()) {
                finishFragment();
            }
        });
        actionBarContainer.addView(backButton, LayoutHelper.createFrame(54, 54, Gravity.LEFT | Gravity.CENTER_VERTICAL));

        termsButton = new ImageView(context);
        termsButton.setScaleType(ImageView.ScaleType.CENTER);
        termsButton.setBackground(Theme.createSelectorDrawable(getThemedColor(Theme.key_actionBarWhiteSelector), Theme.RIPPLE_MASK_CIRCLE_20DP));
        termsButton.setImageResource(R.drawable.msg_info);
        termsButton.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_actionBarDefaultIcon), PorterDuff.Mode.SRC_IN));
        termsButton.setOnClickListener(v -> Browser.openUrl(LaunchActivity.instance, Utilities.uriParseSafe(String.format(Locale.US, "https://%s/dcterms", OctoUtils.getDomain()))));
        actionBarContainer.addView(termsButton, LayoutHelper.createFrame(54, 54, Gravity.RIGHT | Gravity.CENTER_VERTICAL));

        FrameLayout contentView;
        fragmentView = contentView = frameLayout;

        if (parameter != null) {
            if (parameter.equalsIgnoreCase("media")) {
                tabsView.setSelected(PAGE_MEDIA);
                viewPager.setPosition(PAGE_MEDIA);
            } else if (parameter.equalsIgnoreCase("web")) {
                tabsView.setSelected(PAGE_WEB);
                viewPager.setPosition(PAGE_WEB);
            }
        }

        return contentView;
    }

    @Override
    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        super.onTransitionAnimationEnd(isOpen, backward);
        if (isOpen) {
            networkPage.dcStatusHeaderCell.invalidate();
        }
    }

    @NonNull
    private FrameLayout getFrameLayout(Context context) {
        FrameLayout frameLayout = new FrameLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                if (actionBarContainer != null) {
                    actionBarContainer.getLayoutParams().height = ActionBar.getCurrentActionBarHeight() + AndroidUtilities.statusBarHeight;
                    ((MarginLayoutParams) backButton.getLayoutParams()).topMargin = AndroidUtilities.statusBarHeight / 2;
                    ((MarginLayoutParams) tabsView.getLayoutParams()).topMargin = AndroidUtilities.statusBarHeight / 2;
                    ((MarginLayoutParams) termsButton.getLayoutParams()).topMargin = AndroidUtilities.statusBarHeight / 2;
                }
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        };
        frameLayout.setFitsSystemWindows(true);
        return frameLayout;
    }

    public void handleParameter(String parameter) {
        this.parameter = parameter;
    }

    private boolean hasActivityInProgress() {
        return networkPage.isMonitoring() || mediaPage.isMonitoring() || webPage.isMonitoring();
    }

    @Override
    public boolean onBackPressed() {
        if (hasActivityInProgress()) {
            showActivityAlert();
            return false;
        }
        return super.onBackPressed();
    }

    private void showActivityAlert() {
        if (getVisibleDialog() != null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), getResourceProvider());
        builder.setTitle(getString(R.string.DatacenterStatusSection_Close));
        builder.setMessage(getString(R.string.DatacenterStatusSection_Close_Desc));
        builder.setPositiveButton(getString(R.string.Stop), (dialogInterface, i) -> {
            networkPage.forceStopMonitor();
            mediaPage.forceStopMonitor();
            webPage.forceStopMonitor();
            finishFragment();
        });
        builder.setNegativeButton(getString(R.string.Discard), (dialogInterface, i) -> finishFragment());

        AlertDialog dialog = builder.show();
        TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (button != null) {
            button.setTextColor(getThemedColor(Theme.key_text_RedBold));
        }
    }

    @Override
    public boolean isSwipeBackEnabled(MotionEvent event) {
        if (hasActivityInProgress()) {
            return false;
        }
        if (viewPager.getCurrentPosition() > PAGE_NETWORK) {
            return false;
        }
        return super.isSwipeBackEnabled(event);
    }

    public void updateLightStatusBar() {
        if (getParentActivity() == null) return;
        AndroidUtilities.setLightStatusBar(getParentActivity().getWindow(), isLightStatusBar());
    }
}