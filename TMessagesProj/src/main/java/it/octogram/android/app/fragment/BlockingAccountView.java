/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.fragment;


import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LiteMode;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Premium.StarParticlesView;
import org.telegram.ui.Components.voip.CellFlickerDrawable;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.LogoutActivity;

import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.account.FingerprintUtils;

public class BlockingAccountView extends ViewGroup {
    private final TextView buttonTextView;
    private final TextView descriptionText;
    private BlockingViewDelegate delegate;

    private final StarParticlesView particlesView;
    private final LinearLayout chipLayout;
    private final BackupImageView mainImageView;
    private final TextView chipTextView;
    private ActionBar actionBar = null;

    private Bitmap blurBitmap;
    private BitmapShader blurBitmapShader;
    private Paint blurBitmapPaint;
    private final Paint darkenShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Matrix blurMatrix;

    private final int originalHandledAccount;
    private int currentHandlingAccount;
    private final boolean isPageView;
    private boolean hasBlur = true;

    private float scrimViewOpacity = 0f;

    @SuppressLint("ClickableViewAccessibility")
    public BlockingAccountView(Context context, boolean isPageView) {
        super(context);

        this.isPageView = isPageView;

        originalHandledAccount = UserConfig.selectedAccount;
        currentHandlingAccount = originalHandledAccount;

        setOnTouchListener((v, event) -> true);

        if (isPageView) {
            actionBar = new ActionBar(context, null);
            actionBar.setBackgroundColor(Color.TRANSPARENT);
            actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSelector), false);
            actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarActionModeDefaultSelector), true);
            actionBar.setItemsColor(Theme.getColor(Theme.key_actionBarDefaultIcon), false);
            actionBar.setItemsColor(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon), true);
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            actionBar.setAllowOverlayTitle(true);

            actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        delegate.destroy();
                    }
                }
            });

            addView(actionBar);
        }

        particlesView = new StarParticlesView(context);
        particlesView.setClipWithGradient();
        particlesView.drawable.colorKey = Theme.getColor(Theme.key_featuredStickers_addButton);
        particlesView.drawable.isCircle = true;
        particlesView.drawable.centerOffsetY = dp(25);
        particlesView.drawable.minLifeTime = 2000;
        particlesView.drawable.randLifeTime = 3000;
        particlesView.drawable.size1 = 16;
        particlesView.drawable.useRotate = false;
        particlesView.drawable.updateColorsWithoutTheme();
        if (!isPageView) {
            addView(particlesView);
        }

        mainImageView = new BackupImageView(context);
        mainImageView.setBlurAllowed(true);
        mainImageView.setRoundRadius(dp(80));

        if (!isPageView) {
            addView(mainImageView);
        }

        chipLayout = new LinearLayout(context);
        chipLayout.setOrientation(LinearLayout.HORIZONTAL);
        chipLayout.setBackground(Theme.createSimpleSelectorRoundRectDrawable(dp(28), Color.TRANSPARENT, Theme.blendOver(Theme.getColor(Theme.key_windowBackgroundGray), Theme.getColor(Theme.key_listSelector))));
        chipTextView = new TextView(context);
        chipTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
        chipTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        chipLayout.addView(chipTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 6, 0, 0, 0));
        ImageView selectView = new ImageView(context);
        selectView.setScaleType(ImageView.ScaleType.CENTER);
        selectView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextGray3), PorterDuff.Mode.SRC_IN));
        selectView.setImageResource(R.drawable.arrows_select);
        chipLayout.setOnClickListener((l) -> {
            if (isPageView) {
                return;
            }

            boolean hasSingleAccount = true;
            boolean hasDefinedAccount = false;
            ItemOptions i = ItemOptions.makeOptions(BlockingAccountView.this, null, chipLayout);
            for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                TLRPC.User u = UserConfig.getInstance(a).getCurrentUser();
                if (u != null) {
                    int finalA = a;
                    i.addChatForLockedAccounts(u, currentHandlingAccount == a, () -> {
                        if (currentHandlingAccount == finalA) {
                            return;
                        }
                        currentHandlingAccount = finalA;

                        if (FingerprintUtils.hasLockedAccounts() && FingerprintUtils.hasFingerprintCached() && FingerprintUtils.isAccountLocked(u.id)) {
                            updateChipAccount(u);
                        } else {
                            handleSuccessState();
                        }
                    });
                    if (!hasDefinedAccount) {
                        hasDefinedAccount = true;
                    } else {
                        hasSingleAccount = false;
                    }
                }
            }


            i.addGap();
            i.add(R.drawable.logout_24px, getString(R.string.LogOut), true, this::handleLogout);
            if (hasSingleAccount) {
                i.addText(getString(R.string.ThisAccountLocked_Logout_Desc), 13);
            }

            i.setDrawScrim(false)
                    .setDimAlpha(0)
                    .setGravity(Gravity.RIGHT)
                    .translate(dp(24), 0)
                    .show();
        });
        if (!isPageView) {
            chipLayout.addView(selectView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 2, 0, 5, 0));
        }
        addView(chipLayout);

        descriptionText = new TextView(context);
        descriptionText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText6));
        descriptionText.setGravity(Gravity.CENTER_HORIZONTAL);
        descriptionText.setLineSpacing(dp(2), 1);
        descriptionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        descriptionText.setPadding(dp(48), 0, dp(48), 0);
        descriptionText.setText(getString(R.string.ThisAccountLocked_Desc));
        addView(descriptionText);

        buttonTextView = new androidx.appcompat.widget.AppCompatTextView(context) {
            CellFlickerDrawable cellFlickerDrawable;

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                if (cellFlickerDrawable == null) {
                    cellFlickerDrawable = new CellFlickerDrawable();
                    cellFlickerDrawable.drawFrame = false;
                    cellFlickerDrawable.repeatProgress = 2f;
                }
                cellFlickerDrawable.setParentWidth(getMeasuredWidth());
                AndroidUtilities.rectTmp.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
                cellFlickerDrawable.draw(canvas, AndroidUtilities.rectTmp, dp(4), null);
                invalidate();
            }
        };
        buttonTextView.setPadding(dp(34), 0, dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(AndroidUtilities.bold());
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
        final SpannableStringBuilder sb = new SpannableStringBuilder("U  ");
        final ColoredImageSpan span = new ColoredImageSpan(R.drawable.menu_unlock);
        sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.append(new SpannableStringBuilder(getString(R.string.ThisAccountLocked_Unlock)));
        buttonTextView.setText(sb);
        buttonTextView.setOnClickListener(v -> askForFingerprint());
        buttonTextView.setPadding(dp(34), dp(8), dp(34), dp(8));
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);

        if (!isPageView) {
            addView(buttonTextView);
            updateChipAccount(UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser());
        }

        particlesView.setAlpha(0f);
        mainImageView.setAlpha(0f);
        chipLayout.setAlpha(0f);
        descriptionText.setAlpha(0f);

        prepareBlur();
        setWillNotDraw(false);

        AndroidUtilities.runOnUIThread(() -> {
            if (!isPageView) {
                ValueAnimator animation = ValueAnimator.ofFloat(0.01f, 1.5f);
                animation.addUpdateListener(animation1 -> {
                    float value = (float) animation1.getAnimatedValue();
                    if (value <= 0.3f) {
                        particlesView.setAlpha(value / 0.3f);
                    }
                    if (value >= 0.3f && value <= 0.6f) {
                        mainImageView.setAlpha((value - 0.3f) / 0.3f);
                    }
                    if (value >= 0.6f && value <= 1f) {
                        float currentPercent = (value - 0.6f) / 0.4f;
                        chipLayout.setAlpha(currentPercent);
                        chipLayout.setTranslationY(dp(10) * currentPercent);
                    }
                    if (value >= 1f && value <= 1.5f) {
                        float currentPercent = (value - 1f) / 0.5f;
                        descriptionText.setAlpha(currentPercent);
                        descriptionText.setTranslationY(dp(15) * currentPercent);
                    }
                });
                animation.setInterpolator(CubicBezierInterpolator.EASE_IN);
                animation.setDuration(700);
                animation.start();
            }

            if (!SharedConfig.appLocked) {
                askForFingerprint();
            }
        }, 200);
    }

    private void prepareBlur() {
        darkenShadowPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        darkenShadowPaint.setAlpha(30);

        if (!hasBlur || (!LiteMode.isEnabled(LiteMode.FLAG_CHAT_BLUR) && !LiteMode.isEnabled(LiteMode.FLAG_CHAT_BACKGROUND))) {
            hasBlur = false;
            scrimViewOpacity = 1f;
            invalidate();
            return;
        }

        setVisibility(INVISIBLE);
        AndroidUtilities.makeGlobalBlurBitmap(bitmap -> {
            setVisibility(VISIBLE);
            blurBitmap = bitmap;

            blurBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            blurBitmapPaint.setShader(blurBitmapShader = new BitmapShader(blurBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            blurMatrix = new Matrix();

            invalidate();
        }, 14);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        darkenShadowPaint.setAlpha((int) (255 * scrimViewOpacity));
        canvas.drawRect(0, 0, getWidth(), getHeight(), darkenShadowPaint);

        if (hasBlur) {
            blurMatrix.reset();
            final float s = (float) getWidth() / blurBitmap.getWidth();
            blurMatrix.postScale(s, s);
            blurBitmapShader.setLocalMatrix(blurMatrix);

            blurBitmapPaint.setAlpha((int) (255 * scrimViewOpacity));
            canvas.drawRect(0, 0, getWidth(), getHeight(), blurBitmapPaint);

            if (scrimViewOpacity <= 1f) {
                scrimViewOpacity += 16 / 150f;
                scrimViewOpacity = Utilities.clamp(scrimViewOpacity, 1f, 0f);
                invalidate();
            }
        }
    }

    private void updateChipAccount(TLRPC.User user) {
        if (isPageView) {
            return;
        }
        if (mainImageView != null) {
            AvatarDrawable avatarDrawable1 = new AvatarDrawable();
            avatarDrawable1.setInfo(user);
            mainImageView.setForUserOrChat(user, avatarDrawable1);
            mainImageView.setHasBlur(true);
        }
        if (chipTextView != null) {
            chipTextView.setText(OctoUtils.createSpoiledName(UserObject.getUserName(user)));
        }
    }

    private void handleLogout() {
        if (isPageView) {
            return;
        }
        LogoutActivity.makeLogOutDialog(LaunchActivity.instance, UserConfig.selectedAccount).show();
    }

    private void askForFingerprint() {
        FingerprintUtils.checkFingerprint(ApplicationLoader.applicationContext, isPageView ? FingerprintUtils.FingerprintAction.OPEN_PAGE : FingerprintUtils.FingerprintAction.UNLOCK_ACCOUNT, true, new FingerprintUtils.FingerprintResult() {
            @Override
            public void onSuccess() {
                handleSuccessState(true);
            }

            @Override
            public void onError(int error) {
                if (isPageView) {
                    delegate.destroy();
                }
            }
        });
    }

    private void handleSuccessState() {
        handleSuccessState(false);
    }

    private void handleSuccessState(boolean withFingerprint) {
        if (currentHandlingAccount != originalHandledAccount && !isPageView) {
            BaseFragment lastFragment = LaunchActivity.instance.getActionBarLayout().getSafeLastFragment();
            if (lastFragment instanceof DialogsActivity f2 && f2.getArguments() != null && f2.getArguments().getBoolean("allowSwitchAccount")) {
                DialogsActivity.DialogsActivityDelegate oldDelegate = f2.getDelegate();
                Bundle args = f2.getArguments();
                LaunchActivity.instance.switchToAccount(currentHandlingAccount, true);

                DialogsActivity dialogsActivity = new DialogsActivity(args);
                dialogsActivity.setDelegate(oldDelegate);
                LaunchActivity.instance.presentFragment(dialogsActivity, false, true);
            } else {
                LaunchActivity.instance.switchToAccount(currentHandlingAccount, true);
            }

            AlertDialog progressDialog = new AlertDialog(LaunchActivity.instance, AlertDialog.ALERT_TYPE_SPINNER);
            progressDialog.setCanCancel(false);
            progressDialog.show();

            AndroidUtilities.runOnUIThread(() -> {
                progressDialog.dismiss();

                if (withFingerprint) {
                    delegate.onUnlock();
                } else {
                    delegate.destroy();
                }
            }, 400);
        } else {
            if (withFingerprint || isPageView) {
                delegate.onUnlock();
            } else {
                delegate.destroy();
            }
        }
    }

    public void setDelegate(BlockingViewDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (actionBar != null) {
            actionBar.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(dp(30), MeasureSpec.EXACTLY));
        }

        if (isPageView) {
            setMeasuredDimension(width, height);
            return;
        }

        particlesView.measure(MeasureSpec.makeMeasureSpec(width - dp(10), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(dp(170), MeasureSpec.EXACTLY));
        mainImageView.measure(MeasureSpec.makeMeasureSpec(dp(140), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(dp(140), MeasureSpec.EXACTLY));
        chipLayout.measure(MeasureSpec.makeMeasureSpec(width - dp(24), MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(dp(40), MeasureSpec.EXACTLY));
        if (width > height) {
            descriptionText.measure(MeasureSpec.makeMeasureSpec((int) (width * 0.6f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
            buttonTextView.measure(MeasureSpec.makeMeasureSpec((int) (width * 0.6f), MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(dp(42), MeasureSpec.EXACTLY));
        } else {
            descriptionText.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
            buttonTextView.measure(MeasureSpec.makeMeasureSpec(width - dp(24 * 2), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(dp(50), MeasureSpec.EXACTLY));
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int height = b - t;

        if (actionBar != null) {
            actionBar.layout(0, 0, actionBar.getMeasuredWidth(), actionBar.getMeasuredHeight());
        }

        if (isPageView) {
            return;
        }

        int y;
        int x;
        if (r > b) {
            x = (int) (width * 0.5f - particlesView.getMeasuredWidth()) / 2;
            y = (height - particlesView.getMeasuredHeight()) / 2;
            particlesView.layout(x, y, x + particlesView.getMeasuredWidth(), y + particlesView.getMeasuredHeight());

            y = (height - mainImageView.getMeasuredHeight()) / 2;
            x = (int) (width * 0.5f - mainImageView.getMeasuredWidth()) / 2;
            mainImageView.layout(x, y, x + mainImageView.getMeasuredWidth(), y + mainImageView.getMeasuredHeight());
            x = (int) (width * 0.4f) + descriptionText.getMeasuredWidth() / 2 - chipLayout.getMeasuredWidth() / 2;
            y = (int) (height * 0.3f);
            chipLayout.layout(x, y, x + chipLayout.getMeasuredWidth(), y + chipLayout.getMeasuredHeight());
            x = (int) (width * 0.4f);
            y += chipLayout.getMeasuredHeight() + dp(2);
            descriptionText.layout(x, y, x + descriptionText.getMeasuredWidth(), y + descriptionText.getMeasuredHeight());
            x = (int) (width * 0.4f + (width * 0.6f - buttonTextView.getMeasuredWidth()) / 2);
            y = (int) (height * 0.78f);
        } else {
            x = dp(5);
            y = (int) (height * 0.3f) + mainImageView.getMeasuredHeight() / 2 - particlesView.getMeasuredHeight() / 2;
            particlesView.layout(x, y, x + particlesView.getMeasuredWidth(), y + particlesView.getMeasuredHeight());

            y = (int) (height * 0.3f);
            x = (width - mainImageView.getMeasuredWidth()) / 2;
            mainImageView.layout(x, y, x + mainImageView.getMeasuredWidth(), y + mainImageView.getMeasuredHeight());
            y += mainImageView.getMeasuredHeight() + dp(24);
            x = (width - chipLayout.getMeasuredWidth()) / 2;
            chipLayout.layout(x, y, x + chipLayout.getMeasuredWidth(), y + chipLayout.getMeasuredHeight());
            y += chipLayout.getMeasuredHeight() + dp(5);
            descriptionText.layout(0, y, descriptionText.getMeasuredWidth(), y + descriptionText.getMeasuredHeight());
            x = (width - buttonTextView.getMeasuredWidth()) / 2;
            y = height - buttonTextView.getMeasuredHeight() - dp(48);
        }
        buttonTextView.layout(x, y, x + buttonTextView.getMeasuredWidth(), y + buttonTextView.getMeasuredHeight());
    }

    public interface BlockingViewDelegate {
        void onUnlock();

        void destroy();
    }
}