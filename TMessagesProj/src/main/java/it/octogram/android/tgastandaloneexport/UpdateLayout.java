/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.tgastandaloneexport;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.MediaActionDrawable;
import org.telegram.ui.Components.RadialProgress2;
import org.telegram.ui.IUpdateLayout;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import it.octogram.android.utils.UpdatesManager;

public class UpdateLayout extends IUpdateLayout {

    private FrameLayout updateLayout;
    private RadialProgress2 updateLayoutIcon;
    private SimpleTextView[] updateTextViews;
    private TextView updateSizeTextView;
    private AnimatorSet updateTextAnimator;

    private final Activity activity;
    private final ViewGroup sideMenu;
    private final ViewGroup sideMenuContainer;

    public UpdateLayout(Activity activity, ViewGroup sideMenu, ViewGroup sideMenuContainer) {
        super(activity, sideMenu, sideMenuContainer);
        this.activity = activity;
        this.sideMenu = sideMenu;
        this.sideMenuContainer = sideMenuContainer;
        createUpdateUI();
        onAttached();
    }

    public void createUpdateUI() {
        if (sideMenuContainer == null || updateLayout != null) {
            return;
        }
        updateLayout = new FrameLayout(activity) {

            private final Paint paint = new Paint();
            private final Matrix matrix = new Matrix();
            private LinearGradient updateGradient;
            private int lastGradientWidth;

            @Override
            public void draw(@NonNull Canvas canvas) {
                if (updateGradient != null) {
                    paint.setColor(0xffffffff);
                    paint.setShader(updateGradient);
                    updateGradient.setLocalMatrix(matrix);
                    canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), paint);
                    updateLayoutIcon.setBackgroundGradientDrawable(updateGradient);
                    updateLayoutIcon.draw(canvas);
                }
                super.draw(canvas);
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                int width = MeasureSpec.getSize(widthMeasureSpec);
                if (lastGradientWidth != width) {
                    updateGradient = new LinearGradient(0, 0, width, 0, new int[]{0xff663dff, 0xffcc4499}, new float[]{0.0f, 1.0f}, Shader.TileMode.CLAMP);
                    lastGradientWidth = width;
                }
            }

            @Override
            protected void onDetachedFromWindow() {
                super.onDetachedFromWindow();
                onDetached();
            }
        };
        updateLayout.setWillNotDraw(false);
        updateLayout.setVisibility(View.INVISIBLE);
        updateLayout.setTranslationY(dp(44));
        updateLayout.setBackground(Theme.getSelectorDrawable(0x40ffffff, false));
        sideMenuContainer.addView(updateLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44, Gravity.LEFT | Gravity.BOTTOM));
        updateLayout.setOnClickListener(v -> UpdatesManager.INSTANCE.onUpdateButtonPressed());
        updateLayoutIcon = new RadialProgress2(updateLayout);
        updateLayoutIcon.setColors(0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff);
        updateLayoutIcon.setProgressRect(dp(22), dp(11), dp(22 + 22), dp(11 + 22));
        updateLayoutIcon.setCircleRadius(dp(11));
        updateLayoutIcon.setAsMini();

        updateTextViews = new SimpleTextView[2];
        for (int i = 0; i < 2; ++i) {
            updateTextViews[i] = new SimpleTextView(activity);
            updateTextViews[i].setTextSize(15);
            updateTextViews[i].setTypeface(AndroidUtilities.bold());
            updateTextViews[i].setTextColor(0xffffffff);
            updateTextViews[i].setGravity(Gravity.LEFT);
            updateLayout.addView(updateTextViews[i], LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 74, 0, 0, 0));
        }
        updateTextViews[0].setText(getString(R.string.AppUpdate));
        updateTextViews[1].setAlpha(0f);
        updateTextViews[1].setVisibility(View.GONE);

        updateSizeTextView = new TextView(activity);
        updateSizeTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        updateSizeTextView.setTypeface(AndroidUtilities.bold());
        updateSizeTextView.setGravity(Gravity.RIGHT);
        updateSizeTextView.setTextColor(0xffffffff);
        updateLayout.addView(updateSizeTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.RIGHT, 0, 0, 17, 0));
    }

    private UpdatesManager.UpdatesManagerCallback callback;

    private void onAttached() {
        AtomicBoolean isFirstDataResult = new AtomicBoolean(true);
        UpdatesManager.INSTANCE.addCallback(callback = new UpdatesManager.UpdatesManagerCallback() {
            private boolean showSize = false;

            @Override
            public boolean onGetStateAfterAdd() {
                return true;
            }

            @Override
            public void onNoUpdateAvailable() {
                AndroidUtilities.runOnUIThread(() -> {
                    if (updateLayout == null || updateLayout.getTag() == null) {
                        return;
                    }
                    updateLayout.setTag(null);
                    if (!isFirstDataResult.get()) {
                        updateLayout.animate().translationY(dp(44)).setInterpolator(CubicBezierInterpolator.EASE_OUT).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (updateLayout.getTag() == null) {
                                    updateLayout.setVisibility(View.INVISIBLE);
                                }
                            }
                        }).setDuration(180).start();
                    } else {
                        updateLayout.setTranslationY(dp(44));
                        updateLayout.setVisibility(View.INVISIBLE);
                    }
                    sideMenu.setPadding(0, 0, 0, 0);
                    isFirstDataResult.set(false);
                });
            }

            @Override
            public void onUpdateAvailable(TLRPC.TL_help_appUpdate update) {
                AndroidUtilities.runOnUIThread(() -> {
                    boolean animated = !isFirstDataResult.get();
                    updateLayoutIcon.setIcon(MediaActionDrawable.ICON_DOWNLOAD, true, animated);
                    setUpdateText(getString(R.string.AppUpdate), animated);
                    showSize = true;
                    restartSizing(animated);
                });
            }

            @Override
            public void onUpdateDownloading(float percent) {
                AndroidUtilities.runOnUIThread(() -> {
                    boolean animated = !isFirstDataResult.get();
                    updateLayoutIcon.setIcon(MediaActionDrawable.ICON_CANCEL, true, animated);
                    updateLayoutIcon.setProgress(percent, animated);
                    updateTextViews[0].setText(formatString(R.string.AppUpdateDownloading, (int) (percent * 100)));
                    showSize = false;
                    restartSizing(animated);
                });
            }

            @Override
            public void onUpdateReady() {
                AndroidUtilities.runOnUIThread(() -> {
                    boolean animated = !isFirstDataResult.get();
                    updateLayoutIcon.setIcon(MediaActionDrawable.ICON_UPDATE, true, animated);
                    setUpdateText(getString(R.string.AppUpdateNow), animated);
                    showSize = false;
                    restartSizing(true);
                });
            }

            private void restartSizing(boolean animated) {
                isFirstDataResult.set(false);
                if (showSize) {
                    if (updateSizeTextView.getTag() != null) {
                        if (animated) {
                            updateSizeTextView.setTag(null);
                            updateSizeTextView.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setDuration(180).start();
                        } else {
                            updateSizeTextView.setAlpha(1.0f);
                            updateSizeTextView.setScaleX(1.0f);
                            updateSizeTextView.setScaleY(1.0f);
                        }
                    }
                } else {
                    if (updateSizeTextView.getTag() == null) {
                        if (animated) {
                            updateSizeTextView.setTag(1);
                            updateSizeTextView.animate().alpha(0.0f).scaleX(0.0f).scaleY(0.0f).setDuration(180).start();
                        } else {
                            updateSizeTextView.setAlpha(0.0f);
                            updateSizeTextView.setScaleX(0.0f);
                            updateSizeTextView.setScaleY(0.0f);
                        }
                    }
                }
                if (updateLayout.getTag() != null) {
                    return;
                }
                updateLayout.setVisibility(View.VISIBLE);
                updateLayout.setTag(1);
                if (animated) {
                    updateLayout.animate().translationY(0).setInterpolator(CubicBezierInterpolator.EASE_OUT).setListener(null).setDuration(180).start();
                } else {
                    updateLayout.setTranslationY(0);
                }
                sideMenu.setPadding(0, 0, 0, dp(44));
            }
        });
    }

    private void onDetached() {
        UpdatesManager.INSTANCE.removeCallback(callback);
    }

    private void setUpdateText(String text, boolean animate) {
        if (TextUtils.equals(updateTextViews[0].getText(), text)) {
            return;
        }
        if (updateTextAnimator != null) {
            updateTextAnimator.cancel();
            updateTextAnimator = null;
        }

        if (animate) {
            updateTextViews[1].setText(updateTextViews[0].getText());
            updateTextViews[0].setText(text);

            updateTextViews[0].setAlpha(0);
            updateTextViews[1].setAlpha(1);
            updateTextViews[0].setVisibility(View.VISIBLE);
            updateTextViews[1].setVisibility(View.VISIBLE);

            ArrayList<Animator> arrayList = new ArrayList<>();
            arrayList.add(ObjectAnimator.ofFloat(updateTextViews[1], View.ALPHA, 0));
            arrayList.add(ObjectAnimator.ofFloat(updateTextViews[0], View.ALPHA, 1));

            updateTextAnimator = new AnimatorSet();
            updateTextAnimator.playTogether(arrayList);
            updateTextAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (updateTextAnimator == animation) {
                        updateTextViews[1].setVisibility(View.GONE);
                        updateTextAnimator = null;
                    }
                }
            });
            updateTextAnimator.setDuration(320);
            updateTextAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
            updateTextAnimator.start();
        } else {
            updateTextViews[0].setText(text);
            updateTextViews[0].setAlpha(1);
            updateTextViews[0].setVisibility(View.VISIBLE);
            updateTextViews[1].setVisibility(View.GONE);
        }
    }
}