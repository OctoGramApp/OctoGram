/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.fragment;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.ui.PremiumPreviewFragment.PREMIUM_FEATURE_ANIMATED_EMOJI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Premium.StarParticlesView;

import it.octogram.android.OctoColors;
import it.octogram.android.logs.OctoLogging;

@SuppressLint("ViewConstructor")
public class OctoAnimationFragment extends FrameLayout {
    private boolean isAnimating = false;
    private boolean isDownscaled = false;
    private boolean upscaleAfterAnimation = false;
    private boolean easterEggEnabled = false;
    private boolean disableEffect = false;

    private final StarParticlesView particlesView;
    private final ImageView octoImageView;
    private final EasterEggAnimation easterEggAnimation = new EasterEggAnimation();

    public static int sz = 240;
    public static int sz_no_text = 180;
    static final String TAG = "OctoAnimationFragment";

    public static class OctoAnimationScopes {
        public static int OCTO = 1;
        public static int GEMINI = 2;
        public static int CHATGPT = 3;
        public static int OPENROUTER = 4;
    }

    @SuppressLint("ClickableViewAccessibility")
    public OctoAnimationFragment(Context context, TextView textView) {
        this(context, textView, OctoAnimationScopes.OCTO);
    }

    @SuppressLint("ClickableViewAccessibility")
    public OctoAnimationFragment(Context context, TextView textView, int scope) {
        super(context);

        setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        particlesView = new StarParticlesView(context) {
            @Override
            protected int getStarsRectWidth() {
                return getMeasuredWidth() - dp(40);
            }
        };
        particlesView.setClipWithGradient();
        particlesView.drawable.colorKey = Color.parseColor(scope != OctoAnimationScopes.OCTO ? OctoColors.AiColor.getValue() : Theme.isCurrentThemeDark() ? OctoColors.LogoColor.getValue() : OctoColors.LogoColor2.getValue());
        particlesView.drawable.isCircle = scope == OctoAnimationScopes.OCTO;
        particlesView.drawable.centerOffsetY = dp(25);
        particlesView.drawable.minLifeTime = scope != OctoAnimationScopes.OCTO ? 1000 : 2000;
        particlesView.drawable.randLifeTime = scope != OctoAnimationScopes.OCTO ? 500 : 5000;
        particlesView.drawable.useRotate = false;
        particlesView.drawable.updateColorsWithoutTheme();
        addView(particlesView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        int[] clicksCount = {0};
        octoImageView = new ImageView(context);
        octoImageView.setVisibility(GONE);
        octoImageView.setOnTouchListener((view, motionEvent) -> {
            if (easterEggEnabled || disableEffect) {
                return true;
            }

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN && !isAnimating) {
                handleOnLongClickStart();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP && isDownscaled) {
                if (isAnimating) {
                    upscaleAfterAnimation = true;
                    clicksCount[0]++;

                    if (clicksCount[0] >= 5) {
                        easterEggAnimation.enableEasterEgg();
                        clicksCount[0] = 0;
                    }
                } else {
                    clicksCount[0] = 0;
                    handleOnLongClickEnd();
                }
            }

            return true;
        });

        int animationIcon;

        if (scope == OctoAnimationScopes.OCTO) {
            animationIcon = R.drawable.ic_unsized_octo;
        } else if (scope == OctoAnimationScopes.CHATGPT) {
            animationIcon = R.drawable.chatgpt;
        } else if (scope == OctoAnimationScopes.GEMINI) {
            animationIcon = R.drawable.gemini;
        } else if (scope == OctoAnimationScopes.OPENROUTER) {
            animationIcon = R.drawable.openrouter;
        } else {
            OctoLogging.e(TAG, "Unknown scope: " + scope);
            animationIcon = -1;
        }
        octoImageView.setImageResource(animationIcon);

        addView(octoImageView, LayoutHelper.createFrame(140, 140, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 25, 0, 0));

        if (textView != null) {
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 36, 0, 36, 25));
        }

        AndroidUtilities.runOnUIThread(this::firstIconAppear, 200);
    }

    public void setDisableEffect(boolean disableEffect) {
        this.disableEffect = disableEffect;
    }

    public void setEasterEggCallback(OnEasterEggEnabledCallback callback) {
        easterEggAnimation.setEasterEggCallback(callback);
    }

    private void firstIconAppear() {
        isAnimating = true;
        octoImageView.setVisibility(VISIBLE);
        particlesView.flingParticles(360);
        animateClickUp(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isAnimating = false;
            }
        }, true);
    }

    private void handleOnLongClickStart() {
        particlesView.flingParticles(150);

        if (isAnimating) {
            return;
        }

        isAnimating = true;
        isDownscaled = true;
        animateClickDown(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if (!easterEggEnabled) {
                    isAnimating = false;
                }

                if (easterEggEnabled) {
                    easterEggAnimation.handleDisappearStart();
                } else if (upscaleAfterAnimation) {
                    handleOnLongClickEnd();
                }

                //easterEggEnabled = false;
                upscaleAfterAnimation = false;
            }
        });
    }

    private void handleOnLongClickEnd() {
        if (isAnimating) {
            return;
        }

        isAnimating = true;
        isDownscaled = false;
        animateClickUp(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isAnimating = false;
            }
        });
    }

    private void animateClickDown(AnimatorListenerAdapter listener) {
        octoImageView.setScaleX(1);
        octoImageView.setScaleY(1);
        octoImageView.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).setListener(listener).start();
    }

    private void animateClickUp(AnimatorListenerAdapter listener, boolean isFirstAnimation) {
        octoImageView.setScaleX(isFirstAnimation ? 0.6f : 0.9f);
        octoImageView.setScaleY(isFirstAnimation ? 0.6f : 0.9f);

        if (isFirstAnimation) {
            octoImageView.setAlpha(0f);
        }

        octoImageView.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(isFirstAnimation ? 200 : 100).setListener(listener).start();
    }

    private void animateClickUp(AnimatorListenerAdapter listener) {
        animateClickUp(listener, false);
    }

    class EasterEggAnimation {
        private BackupImageView avatarImageView;
        private OnEasterEggEnabledCallback easterEggCallback;

        private void setEasterEggCallback(OnEasterEggEnabledCallback callback) {
            easterEggCallback = callback;
        }

        private void enableEasterEgg() {
            easterEggEnabled = true;

            if (easterEggCallback != null) {
                easterEggCallback.onEasterEggEnabled();
            }
        }

        private void handleDisappearStart() {
            int[] animationsCount = {0};

            AnimatorListenerAdapter animatorListenerAdapter = new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    animationsCount[0]++;

                    if (animationsCount[0] == 2) {
                        handleDisappearCompleted();
                    }
                }
            };

            octoImageView.setScaleX(0.9f);
            octoImageView.setScaleY(0.9f);
            octoImageView.animate().scaleX(0f).scaleY(0f).alpha(0f).setDuration(200).setListener(animatorListenerAdapter).start();

            particlesView.setPaused(true);
            particlesView.setAlpha(1f);
            particlesView.animate().alpha(0f).setDuration(200).setListener(animatorListenerAdapter).start();
        }

        private void handleDisappearCompleted() {
            avatarImageView = new BackupImageView(getContext());
            avatarImageView.getImageReceiver().setRoundRadius(dp(100));
            avatarImageView.setScaleX(0f);
            avatarImageView.setScaleY(0f);
            addView(avatarImageView, octoImageView.getLayoutParams());

            removeView(octoImageView);

            TLRPC.User user = UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser();
            AvatarDrawable avatarDrawable = new AvatarDrawable(user);
            avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
            avatarImageView.setForUserOrChat(user, avatarDrawable, true);

            handleAppearStart();
        }

        private void handleAppearStart() {
            avatarImageView.animate().scaleX(1f).scaleY(1f).setDuration(200).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    particlesView.drawable.type = PREMIUM_FEATURE_ANIMATED_EMOJI;
                    particlesView.drawable.minLifeTime = 1000;
                    particlesView.drawable.randLifeTime = 1000;
                    particlesView.drawable.colorKey = Theme.key_switchTrackChecked;
                    particlesView.drawable.updateColors();
                    particlesView.setPaused(false);
                    particlesView.flingParticles(350);

                    particlesView.setAlpha(0f);
                    particlesView.animate().alpha(1f).setDuration(200).start();
                }
            }).start();
        }
    }

    public interface OnEasterEggEnabledCallback {
        void onEasterEggEnabled();
    }
}
