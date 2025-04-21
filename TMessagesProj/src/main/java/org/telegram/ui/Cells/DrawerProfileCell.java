/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Cells;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.ui.PeerColorActivity.adaptProfileEmojiColor;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.CallLogActivity;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AnimatedFloat;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.MotionBackgroundDrawable;
import org.telegram.ui.Components.Premium.PremiumGradient;
import org.telegram.ui.Components.Premium.StarParticlesView;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.Reactions.AnimatedEmojiEffect;
import org.telegram.ui.Components.Reactions.HwEmojis;
import org.telegram.ui.Components.Reactions.ReactionsLayoutInBubble;
import org.telegram.ui.Components.SnowflakesEffect;
import org.telegram.ui.ContactsActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.Stars.StarGiftPatterns;
import org.telegram.ui.ThemeActivity;

import java.util.ArrayList;

import it.octogram.android.DrawerBackgroundState;
import it.octogram.android.DrawerFavoriteOption;
import it.octogram.android.OctoConfig;
import it.octogram.android.PhoneNumberAlternative;
import it.octogram.android.icons.IconsUtils;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.ui.OctoDrawerSettingsUI;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.network.BrowserUtils;

public class DrawerProfileCell extends FrameLayout implements NotificationCenter.NotificationCenterDelegate {

    private BackupImageView avatarImageView;
    private SimpleTextView nameTextView;
    private TextView phoneTextView;
    private ImageView shadowView;
    private ImageView arrowView;
    private RLottieImageView darkThemeView;
    private static RLottieDrawable sunDrawable;
    private boolean updateRightDrawable = true;
    private Long statusGiftId;
    private AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable status;
    private AnimatedStatusView animatedStatus;

    private Rect srcRect = new Rect();
    private Rect destRect = new Rect();
    private Paint paint = new Paint();
    private Paint backPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Integer currentColor;
    private Integer currentMoonColor;
    private SnowflakesEffect snowflakesEffect;
    private boolean accountsShown;
    private int darkThemeBackgroundColor;
    public static boolean switchingTheme;
    public boolean drawPremium;
    public float drawPremiumProgress;

    private float stateX, stateY;

    StarParticlesView.Drawable starParticlesDrawable;
    PremiumGradient.PremiumGradientTools gradientTools;

    private ImageView darkenBackground;
    private ImageView customMiniIcon;
    private final DrawerLayoutContainer drawerLayoutContainer;
    private final ImageReceiver imageReceiver;
    private Bitmap lastBitmap;
    private final ImageView gradientBackground;
    private boolean avatarAsDrawerBackground = false;

    public boolean isPreviewMode = false;
    private boolean isPreviewModeFirstDraw = true;
    private boolean lastStateShowProfilePic = true;
    private boolean lastStateGradientBackground = true;
    private boolean lastStateDarkenBackground = true;
    private int lastStateFavoriteIcon = -1;
    private ViewPropertyAnimator favoriteIconAnimator;

    private final PremiumDetailsInterface premiumDetailsInterface;

    public DrawerProfileCell(Context context, DrawerLayoutContainer drawerLayoutContainer) {
        super(context);

        this.drawerLayoutContainer = drawerLayoutContainer;

        imageReceiver = new ImageReceiver(this);
        imageReceiver.setCrossfadeWithOldImage(true);
        imageReceiver.setForceCrossfade(true);
        imageReceiver.setDelegate((imageReceiver, set, thumb, memCache) -> {
            if (OctoConfig.INSTANCE.drawerBlurBackground.getValue()) {
                if (thumb) {
                    return;
                }

                ImageReceiver.BitmapHolder holder = imageReceiver.getBitmapSafe();
                if (holder != null) {
                    new Thread(() -> {
                        int blurLevel = OctoConfig.INSTANCE.drawerBlurBackgroundLevel.getValue();
                        blurLevel = Math.max(0, Math.min(99, blurLevel));
                        int width = ((holder.bitmap.getWidth()) * (100 - blurLevel)) / 100;
                        int height = ((holder.bitmap.getHeight()) * (100 - blurLevel)) / 100;
                        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        canvas.drawBitmap(holder.bitmap, null, new Rect(0, 0, width, height), new Paint(Paint.FILTER_BITMAP_FLAG));
                        if (OctoConfig.INSTANCE.drawerBlurBackground.getValue()) {
                            try {
                                Utilities.stackBlurBitmap(bitmap, 3);
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                        }
                        AndroidUtilities.runOnUIThread(() -> {
                            if (lastBitmap != null) {
                                imageReceiver.setCrossfadeWithOldImage(false);
                                imageReceiver.setImageBitmap(new BitmapDrawable(null, lastBitmap));
                            }
                            imageReceiver.setCrossfadeWithOldImage(true);
                            imageReceiver.setImageBitmap(new BitmapDrawable(null, bitmap));
                            lastBitmap = bitmap;
                        });
                    }).start();
                }

                return;
            }

            lastBitmap = null;
        });

        darkenBackground = new ImageView(context);
        addView(darkenBackground, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.BOTTOM));

        gradientBackground = new ImageView(context);
        addView(gradientBackground, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.BOTTOM));

        premiumDetailsInterface = new PremiumDetailsInterface(context);
        addView(premiumDetailsInterface, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        shadowView = new ImageView(context);
        shadowView.setVisibility(INVISIBLE);
        shadowView.setScaleType(ImageView.ScaleType.FIT_XY);
        shadowView.setImageResource(R.drawable.bottom_shadow);
        addView(shadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 70, Gravity.LEFT | Gravity.BOTTOM));

        avatarImageView = new BackupImageView(context);
        avatarImageView.getImageReceiver().setRoundRadius(AndroidUtilities.dp(32));
        addView(avatarImageView, LayoutHelper.createFrame(64, 64, Gravity.LEFT | Gravity.BOTTOM, 16, 0, 0, 67));
        nameTextView = new SimpleTextView(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                if (updateRightDrawable) {
                    updateRightDrawable = false;
                    getEmojiStatusLocation(AndroidUtilities.rectTmp2);
                    animatedStatus.translate(AndroidUtilities.rectTmp2.centerX(), AndroidUtilities.rectTmp2.centerY());
                }
            }

            @Override
            public void invalidate() {
                if (HwEmojis.grab(this)) {
                    return;
                }
                super.invalidate();
            }

            @Override
            public void invalidate(int l, int t, int r, int b) {
                if (HwEmojis.grab(this)) {
                    return;
                }
                super.invalidate(l, t, r, b);
            }

            @Override
            public void invalidateDrawable(Drawable who) {
                if (HwEmojis.grab(this)) {
                    return;
                }
                super.invalidateDrawable(who);
            }

            @Override
            public void invalidate(Rect dirty) {
                if (HwEmojis.grab(this)) {
                    return;
                }
                super.invalidate(dirty);
            }
        };
        nameTextView.setRightDrawableOnClick(e -> {
            if (LaunchActivity.getLastFragment() instanceof PreferencesFragment) {
                // ignore clicks when in octogram settings
                return;
            }
            if (lastUser != null && lastUser.premium) {
                onPremiumClick();
            }
        });
        nameTextView.setPadding(0, AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4));
        nameTextView.setTextSize(15);
        nameTextView.setTypeface(AndroidUtilities.bold());
        nameTextView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        nameTextView.setEllipsizeByGradient(true);
        nameTextView.setRightDrawableOutside(true);
        addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM, 16, 0, 52, 28));

        phoneTextView = new TextView(context);
        phoneTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        phoneTextView.setLines(1);
        phoneTextView.setMaxLines(1);
        phoneTextView.setSingleLine(true);
        phoneTextView.setGravity(Gravity.LEFT);
        addView(phoneTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM, 16, 0, 52, 9));

        arrowView = new ImageView(context);
        arrowView.setScaleType(ImageView.ScaleType.CENTER);
        arrowView.setImageResource(R.drawable.msg_expand);
        addView(arrowView, LayoutHelper.createFrame(59, 59, Gravity.RIGHT | Gravity.BOTTOM));
        setArrowState(false);

        updateMiniIcon();
        /*boolean playDrawable;
        if (playDrawable = sunDrawable == null) {
            sunDrawable = new RLottieDrawable(R.raw.sun, "" + R.raw.sun, AndroidUtilities.dp(28), AndroidUtilities.dp(28), true, null);
            sunDrawable.setPlayInDirectionOfCustomEndFrame(true);
            if (Theme.isCurrentThemeDay()) {
                sunDrawable.setCustomEndFrame(0);
                sunDrawable.setCurrentFrame(0);
            } else {
                sunDrawable.setCurrentFrame(35);
                sunDrawable.setCustomEndFrame(36);
            }
        }
        darkThemeView = new RLottieImageView(context) {
            @Override
            public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(info);
                if (Theme.isCurrentThemeDark()) {
                    info.setText(LocaleController.getString(R.string.AccDescrSwitchToDayTheme));
                } else {
                    info.setText(LocaleController.getString(R.string.AccDescrSwitchToNightTheme));
                }
            }
        };
        darkThemeView.setFocusable(true);
        darkThemeView.setBackground(Theme.createCircleSelectorDrawable(Theme.getColor(Theme.key_dialogButtonSelector), 0, 0));
        sunDrawable.beginApplyLayerColors();
        int color = Theme.getColor(Theme.key_chats_menuName);
        sunDrawable.setLayerColor("Sunny.**", color);
        sunDrawable.setLayerColor("Path 6.**", color);
        sunDrawable.setLayerColor("Path.**", color);
        sunDrawable.setLayerColor("Path 5.**", color);
        sunDrawable.commitApplyLayerColors();
        darkThemeView.setScaleType(ImageView.ScaleType.CENTER);
        darkThemeView.setAnimation(sunDrawable);
        if (Build.VERSION.SDK_INT >= 21) {
            darkThemeView.setBackgroundDrawable(Theme.createSelectorDrawable(darkThemeBackgroundColor = Theme.getColor(Theme.key_listSelector), 1, AndroidUtilities.dp(17)));
            Theme.setRippleDrawableForceSoftware((RippleDrawable) darkThemeView.getBackground());
        }
        if (!playDrawable && sunDrawable.getCustomEndFrame() != sunDrawable.getCurrentFrame()) {
            darkThemeView.playAnimation();
        }
        darkThemeView.setOnClickListener(v -> {
            if (switchingTheme) {
                return;
            }
            switchingTheme = true;
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("themeconfig", Activity.MODE_PRIVATE);
            String dayThemeName = preferences.getString("lastDayTheme", "Blue");
            if (Theme.getTheme(dayThemeName) == null || Theme.getTheme(dayThemeName).isDark()) {
                dayThemeName = "Blue";
            }
            String nightThemeName = preferences.getString("lastDarkTheme", "Dark Blue");
            if (Theme.getTheme(nightThemeName) == null || !Theme.getTheme(nightThemeName).isDark()) {
                nightThemeName = "Dark Blue";
            }
            Theme.ThemeInfo themeInfo = Theme.getActiveTheme();
            if (dayThemeName.equals(nightThemeName)) {
                if (themeInfo.isDark() || dayThemeName.equals("Dark Blue") || dayThemeName.equals("Night")) {
                    dayThemeName = "Blue";
                } else {
                    nightThemeName = "Dark Blue";
                }
            }

            boolean toDark;
            if (toDark = dayThemeName.equals(themeInfo.getKey())) {
                themeInfo = Theme.getTheme(nightThemeName);
                sunDrawable.setCustomEndFrame(36);
            } else {
                themeInfo = Theme.getTheme(dayThemeName);
                sunDrawable.setCustomEndFrame(0);
            }
            darkThemeView.playAnimation();
            switchTheme(themeInfo, toDark);

            if (drawerLayoutContainer != null) {
                FrameLayout layout = drawerLayoutContainer.getParent() instanceof FrameLayout ? (FrameLayout) drawerLayoutContainer.getParent() : null;
                Theme.turnOffAutoNight(layout, () -> {
                    drawerLayoutContainer.closeDrawer(false);
                    drawerLayoutContainer.presentFragment(new ThemeActivity(ThemeActivity.THEME_TYPE_NIGHT));
                });
            }
        });
        darkThemeView.setOnLongClickListener(e -> {
            if (drawerLayoutContainer != null) {
                drawerLayoutContainer.presentFragment(new ThemeActivity(ThemeActivity.THEME_TYPE_BASIC));
                return true;
            }
            return false;
        });
        addView(darkThemeView, LayoutHelper.createFrame(48, 48, Gravity.RIGHT | Gravity.BOTTOM, 0, 0, 6, 90));*/

        if (Theme.getEventType() == 0 || OctoConfig.INSTANCE.showSnowflakes.getValue()) {
            snowflakesEffect = new SnowflakesEffect(0);
            snowflakesEffect.setColorKey(Theme.key_chats_menuName);
        }

        status = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(this, AndroidUtilities.dp(20));
        nameTextView.setRightDrawable(status);
        animatedStatus = new AnimatedStatusView(context, 20, 60);
        addView(animatedStatus, LayoutHelper.createFrame(20, 20, Gravity.LEFT | Gravity.TOP));
    }

    public void updateImageReceiver(TLRPC.User user) {
        if (OctoConfig.INSTANCE.drawerBackground.getValue() == DrawerBackgroundState.PROFILE_PIC.getValue()) {
            ImageLocation imageLocation = ImageLocation.getForUser(user, ImageLocation.TYPE_BIG);
            avatarAsDrawerBackground = imageLocation != null;
            imageReceiver.setImage(imageLocation, "512_512", null, null, new ColorDrawable(0x00000000), 0, null, user, 1);
        }
    }

    public void updateMiniIcon() {
        View currentView = customMiniIcon != null ? customMiniIcon : darkThemeView;
        final Runnable[] onAnimationEndRunnable = {null};
        boolean hasAnimatedDisappear = false;
        boolean animateAppearWithoutWaiting = false;

        if (favoriteIconAnimator != null) {
            favoriteIconAnimator.cancel();
        }

        if (lastStateFavoriteIcon != preGetNewIcon()) {
            lastStateFavoriteIcon = preGetNewIcon();

            if (isPreviewMode && !isPreviewModeFirstDraw) {
                hasAnimatedDisappear = true;
                animateAppearWithoutWaiting = currentView == null;
            }
        }

        if (hasAnimatedDisappear && !animateAppearWithoutWaiting) {
            currentView.setScaleX(1f);
            currentView.setScaleY(1f);
            currentView.setAlpha(1f);

            final Boolean[] canceled = {false};
            favoriteIconAnimator = currentView.animate().scaleY(1.2f).scaleX(1.2f).alpha(0).setDuration(200).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animation) {

                }

                @Override
                public void onAnimationEnd(@NonNull Animator animation) {
                    if (canceled[0]) {
                        return;
                    }

                    removeView(currentView);
                    customMiniIcon = null;
                    darkThemeView = null;

                    if (onAnimationEndRunnable[0] != null) {
                        onAnimationEndRunnable[0].run();
                    }
                }

                @Override
                public void onAnimationCancel(@NonNull Animator animation) {
                    canceled[0] = true;
                    removeView(currentView);
                }

                @Override
                public void onAnimationRepeat(@NonNull Animator animation) {

                }
            });
            favoriteIconAnimator.start();
        } else if (currentView != null) {
            removeView(currentView);
            customMiniIcon = null;
            darkThemeView = null;
        }

        if (OctoConfig.INSTANCE.drawerFavoriteOption.getValue() == DrawerFavoriteOption.DEFAULT.getValue()) {
            boolean playDrawable = sunDrawable == null;
            if (playDrawable) {
                sunDrawable = new RLottieDrawable(R.raw.sun, "" + R.raw.sun, AndroidUtilities.dp(28), AndroidUtilities.dp(28), true, null);
                sunDrawable.setPlayInDirectionOfCustomEndFrame(true);
                if (Theme.isCurrentThemeDay()) {
                    sunDrawable.setCustomEndFrame(0);
                    sunDrawable.setCurrentFrame(0);
                } else {
                    sunDrawable.setCurrentFrame(35);
                    sunDrawable.setCustomEndFrame(36);
                }
            }
            RLottieImageView currentDarkThemeView = new RLottieImageView(getContext()) {
                @Override
                public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(info);
                    if (Theme.isCurrentThemeDark()) {
                        info.setText(LocaleController.getString("AccDescrSwitchToDayTheme", R.string.AccDescrSwitchToDayTheme));
                    } else {
                        info.setText(LocaleController.getString("AccDescrSwitchToNightTheme", R.string.AccDescrSwitchToNightTheme));
                    }
                }
            };
            currentDarkThemeView.setFocusable(true);
            currentDarkThemeView.setBackground(Theme.createCircleSelectorDrawable(Theme.getColor(Theme.key_dialogButtonSelector), 0, 0));
            sunDrawable.beginApplyLayerColors();
            int color = Theme.getColor(Theme.key_chats_menuName);
            sunDrawable.setLayerColor("Sunny.**", color);
            sunDrawable.setLayerColor("Path 6.**", color);
            sunDrawable.setLayerColor("Path.**", color);
            sunDrawable.setLayerColor("Path 5.**", color);
            sunDrawable.commitApplyLayerColors();
            currentDarkThemeView.setScaleType(ImageView.ScaleType.CENTER);
            currentDarkThemeView.setAnimation(sunDrawable);
            if (Build.VERSION.SDK_INT >= 21) {
                currentDarkThemeView.setBackgroundDrawable(Theme.createSelectorDrawable(darkThemeBackgroundColor = Theme.getColor(Theme.key_listSelector), 1, AndroidUtilities.dp(17)));
                Theme.setRippleDrawableForceSoftware((RippleDrawable) currentDarkThemeView.getBackground());
            }
            if (!playDrawable && sunDrawable.getCustomEndFrame() != sunDrawable.getCurrentFrame()) {
                currentDarkThemeView.playAnimation();
            }
            currentDarkThemeView.setOnClickListener(v -> {
                if (switchingTheme) {
                    return;
                }
                if (LaunchActivity.getLastFragment() instanceof PreferencesFragment) {
                    // ignore clicks when in octogram settings
                    return;
                }
                switchingTheme = true;
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("themeconfig", Activity.MODE_PRIVATE);
                String dayThemeName = preferences.getString("lastDayTheme", "Blue");
                if (Theme.getTheme(dayThemeName) == null || Theme.getTheme(dayThemeName).isDark()) {
                    dayThemeName = "Blue";
                }
                String nightThemeName = preferences.getString("lastDarkTheme", "Dark Blue");
                if (Theme.getTheme(nightThemeName) == null || !Theme.getTheme(nightThemeName).isDark()) {
                    nightThemeName = "Dark Blue";
                }
                Theme.ThemeInfo themeInfo = Theme.getActiveTheme();
                if (dayThemeName.equals(nightThemeName)) {
                    if (themeInfo.isDark() || dayThemeName.equals("Dark Blue") || dayThemeName.equals("Night")) {
                        dayThemeName = "Blue";
                    } else {
                        nightThemeName = "Dark Blue";
                    }
                }

                boolean toDark = dayThemeName.equals(themeInfo.getKey());
                if (toDark) {
                    themeInfo = Theme.getTheme(nightThemeName);
                    sunDrawable.setCustomEndFrame(36);
                } else {
                    themeInfo = Theme.getTheme(dayThemeName);
                    sunDrawable.setCustomEndFrame(0);
                }
                currentDarkThemeView.playAnimation();
                switchTheme(themeInfo, toDark);

                if (drawerLayoutContainer != null) {
                    FrameLayout layout = drawerLayoutContainer.getParent() instanceof FrameLayout ? (FrameLayout) drawerLayoutContainer.getParent() : null;
                    Theme.turnOffAutoNight(layout, () -> {
                        drawerLayoutContainer.closeDrawer(false);
                        drawerLayoutContainer.presentFragment(new ThemeActivity(ThemeActivity.THEME_TYPE_NIGHT));
                    });
                }
            });
            currentDarkThemeView.setOnLongClickListener(e -> {
                if (LaunchActivity.getLastFragment() instanceof PreferencesFragment) {
                    // ignore clicks when in octogram settings
                    return true;
                }
                if (drawerLayoutContainer != null) {
                    drawerLayoutContainer.presentFragment(new ThemeActivity(ThemeActivity.THEME_TYPE_BASIC));
                    return true;
                }
                return false;
            });

            currentDarkThemeView.setOnLongClickListener(v -> {
                if (LaunchActivity.getLastFragment() instanceof PreferencesFragment) {
                    // ignore clicks when in octogram settings
                    return true;
                }
                drawerLayoutContainer.closeDrawer();
                LaunchActivity.instance.presentFragment(new PreferencesFragment(new OctoDrawerSettingsUI()));
                return true;
            });

            if (hasAnimatedDisappear) {
                onAnimationEndRunnable[0] = () -> {
                    addView(currentDarkThemeView, LayoutHelper.createFrame(48, 48, Gravity.RIGHT | Gravity.BOTTOM, 0, 0, 6, 90));
                    darkThemeView = currentDarkThemeView;

                    currentDarkThemeView.setScaleX(0.7f);
                    currentDarkThemeView.setScaleY(0.7f);
                    currentDarkThemeView.setAlpha(0f);

                    final Boolean[] canceled = {false};

                    favoriteIconAnimator = currentDarkThemeView.animate().scaleY(1f).scaleX(1f).alpha(1).setDuration(200).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(@NonNull Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(@NonNull Animator animation) {
                            if (canceled[0]) {
                                return;
                            }

                            favoriteIconAnimator = null;
                        }

                        @Override
                        public void onAnimationCancel(@NonNull Animator animation) {
                            canceled[0] = true;
                            removeView(currentDarkThemeView);
                        }

                        @Override
                        public void onAnimationRepeat(@NonNull Animator animation) {

                        }
                    });
                    favoriteIconAnimator.start();
                };

                if (animateAppearWithoutWaiting) {
                    onAnimationEndRunnable[0].run();
                }
            } else {
                darkThemeView = currentDarkThemeView;
                addView(darkThemeView, LayoutHelper.createFrame(48, 48, Gravity.RIGHT | Gravity.BOTTOM, 0, 0, 6, 90));
            }
        } else if (OctoConfig.INSTANCE.drawerFavoriteOption.getValue() != DrawerFavoriteOption.NONE.getValue()) {
            ImageView currentMiniIcon = new ImageView(getContext());
            currentMiniIcon.setFocusable(false);
            currentMiniIcon.setScaleType(ImageView.ScaleType.CENTER);
            currentMiniIcon.setColorFilter(Theme.getColor(Theme.key_chats_menuName));
            currentMiniIcon.setBackground(Theme.createCircleSelectorDrawable(Theme.getColor(Theme.key_dialogButtonSelector), 0, 0));

            currentMiniIcon.setImageResource(lastStateFavoriteIcon);
            currentMiniIcon.setOnClickListener(v -> {
                if (LaunchActivity.getLastFragment() instanceof PreferencesFragment) {
                    // ignore clicks when in octogram settings
                    return;
                }

                if (OctoConfig.INSTANCE.drawerFavoriteOption.getValue() != DrawerFavoriteOption.TELEGRAM_BROWSER.getValue()) {
                    drawerLayoutContainer.closeDrawer(true);
                }

                Bundle args = new Bundle();
                args.putLong("user_id", UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId());

                if (OctoConfig.INSTANCE.drawerFavoriteOption.getValue() == DrawerFavoriteOption.SAVED_MESSAGES.getValue()) {
                    LaunchActivity.instance.presentFragment(new ChatActivity(args));
                } else if (OctoConfig.INSTANCE.drawerFavoriteOption.getValue() == DrawerFavoriteOption.SETTINGS.getValue()) {
                    LaunchActivity.instance.presentFragment(new ProfileActivity(args, null));
                } else if (OctoConfig.INSTANCE.drawerFavoriteOption.getValue() == DrawerFavoriteOption.CONTACTS.getValue()) {
                    args = new Bundle();
                    args.putBoolean("needFinishFragment", false);
                    LaunchActivity.instance.presentFragment(new ContactsActivity(args));
                } else if (OctoConfig.INSTANCE.drawerFavoriteOption.getValue() == DrawerFavoriteOption.CALLS.getValue()) {
                    LaunchActivity.instance.presentFragment(new CallLogActivity());
                } else if (OctoConfig.INSTANCE.drawerFavoriteOption.getValue() == DrawerFavoriteOption.DOWNLOADS.getValue()) {
                    BaseFragment lastFragment = LaunchActivity.instance.getActionBarLayout().getLastFragment();
                    if (lastFragment instanceof DialogsActivity dialogsActivity) {
                        dialogsActivity.showSearch(true, true, true);
                        dialogsActivity.getActionBar().openSearchField(true);
                    }
                } else if (OctoConfig.INSTANCE.drawerFavoriteOption.getValue() == DrawerFavoriteOption.ARCHIVED_CHATS.getValue()) {
                    args = new Bundle();
                    args.putInt("folderId", 1);
                    LaunchActivity.instance.presentFragment(new DialogsActivity(args));
                } else if (OctoConfig.INSTANCE.drawerFavoriteOption.getValue() == DrawerFavoriteOption.TELEGRAM_BROWSER.getValue()) {
                    BrowserUtils.openBrowserHome(() -> drawerLayoutContainer.closeDrawer(true));
                }
            });

            currentMiniIcon.setOnLongClickListener(v -> {
                if (LaunchActivity.getLastFragment() instanceof PreferencesFragment) {
                    // ignore clicks when in octogram settings
                    return true;
                }

                drawerLayoutContainer.closeDrawer();
                LaunchActivity.instance.presentFragment(new PreferencesFragment(new OctoDrawerSettingsUI()));
                return true;
            });

            if (hasAnimatedDisappear) {
                onAnimationEndRunnable[0] = () -> {
                    addView(currentMiniIcon, LayoutHelper.createFrame(48, 48, Gravity.RIGHT | Gravity.BOTTOM, 0, 0, 6, 90));
                    customMiniIcon = currentMiniIcon;

                    currentMiniIcon.setScaleX(0.7f);
                    currentMiniIcon.setScaleY(0.7f);
                    currentMiniIcon.setAlpha(0f);
                    favoriteIconAnimator = currentMiniIcon.animate().scaleY(1f).scaleX(1f).alpha(1).setDuration(200).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(@NonNull Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(@NonNull Animator animation) {
                            favoriteIconAnimator = null;
                        }

                        @Override
                        public void onAnimationCancel(@NonNull Animator animation) {
                            removeView(currentMiniIcon);
                            favoriteIconAnimator = null;
                        }

                        @Override
                        public void onAnimationRepeat(@NonNull Animator animation) {

                        }
                    });
                    favoriteIconAnimator.start();
                };

                if (animateAppearWithoutWaiting) {
                    onAnimationEndRunnable[0].run();
                }
            } else {
                customMiniIcon = currentMiniIcon;
                addView(customMiniIcon, LayoutHelper.createFrame(48, 48, Gravity.RIGHT | Gravity.BOTTOM, 0, 0, 6, 90));
            }
        }
    }

    private int preGetNewIcon() {
        int drawerFavoriteOptionState = OctoConfig.INSTANCE.drawerFavoriteOption.getValue();

        if (drawerFavoriteOptionState == DrawerFavoriteOption.NONE.getValue()) {
            return 0;
        }

        if (drawerFavoriteOptionState == DrawerFavoriteOption.DEFAULT.getValue()) {
            return 1;
        }

        return IconsUtils.getIconWithEventType(drawerFavoriteOptionState);
    }

    public void updateDarkerBackgroundLevel(int level) {
        if (darkenBackground != null) {
            darkenBackground.setAlpha(level / 255f);
        }
    }

    protected void onPremiumClick() {

    }

    public static class AnimatedStatusView extends View {
        private int stateSize;
        private int effectsSize;
        private int renderedEffectsSize;

        private int animationUniq;
        private ArrayList<Object> animations = new ArrayList<>();

        public AnimatedStatusView(Context context, int stateSize, int effectsSize) {
            super(context);
            this.stateSize = stateSize;
            this.effectsSize = effectsSize;
            this.renderedEffectsSize = effectsSize;
        }

        public AnimatedStatusView(Context context, int stateSize, int effectsSize, int renderedEffectsSize) {
            super(context);
            this.stateSize = stateSize;
            this.effectsSize = effectsSize;
            this.renderedEffectsSize = renderedEffectsSize;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(Math.max(renderedEffectsSize, Math.max(stateSize, effectsSize))), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(Math.max(renderedEffectsSize, Math.max(stateSize, effectsSize))), MeasureSpec.EXACTLY)
            );
        }

        private float y1, y2;

        public void translate(float x, float y) {
            setTranslationX(x - getMeasuredWidth() / 2f);
            setTranslationY((this.y1 = y - getMeasuredHeight() / 2f) + this.y2);
        }

        public void translateY2(float y) {
            setTranslationY(this.y1 + (this.y2 = y));
        }

        @Override
        public void dispatchDraw(@NonNull Canvas canvas) {
            final int renderedEffectsSize = AndroidUtilities.dp(this.renderedEffectsSize);
            final int effectsSize = AndroidUtilities.dp(this.effectsSize);
            for (int i = 0; i < animations.size(); ++i) {
                Object animation = animations.get(i);
                if (animation instanceof ImageReceiver) {
                    ImageReceiver imageReceiver = (ImageReceiver) animation;
                    imageReceiver.setImageCoords(
                            (getMeasuredWidth() - effectsSize) / 2f,
                            (getMeasuredHeight() - effectsSize) / 2f,
                            effectsSize,
                            effectsSize
                    );
                    imageReceiver.draw(canvas);
//                    if (imageReceiver.getLottieAnimation() != null && imageReceiver.getLottieAnimation().isRunning() && imageReceiver.getLottieAnimation().isLastFrame()) {
//                        imageReceiver.onDetachedFromWindow();
//                        animations.remove(imageReceiver);
//                    }
                } else if (animation instanceof AnimatedEmojiEffect) {
                    AnimatedEmojiEffect effect = (AnimatedEmojiEffect) animation;
                    effect.setBounds(
                            (int) ((getMeasuredWidth() - renderedEffectsSize) / 2f),
                            (int) ((getMeasuredHeight() - renderedEffectsSize) / 2f),
                            (int) ((getMeasuredWidth() + renderedEffectsSize) / 2f),
                            (int) ((getMeasuredHeight() + renderedEffectsSize) / 2f)
                    );
                    effect.draw(canvas);
                    if (effect.isDone()) {
                        effect.removeView(this);
                        animations.remove(effect);
                    }
                }
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            detach();
        }

        private void detach() {
            if (!animations.isEmpty()) {
                for (Object obj : animations) {
                    if (obj instanceof ImageReceiver) {
                        ((ImageReceiver) obj).onDetachedFromWindow();
                    } else if (obj instanceof AnimatedEmojiEffect) {
                        ((AnimatedEmojiEffect) obj).removeView(this);
                    }
                }
            }
            animations.clear();
        }

        public void animateChange(ReactionsLayoutInBubble.VisibleReaction react) {
            if (react == null) {
                detach();
                return;
            }

            TLRPC.Document document = null;
            TLRPC.TL_availableReaction r = null;
            if (react.emojicon != null) {
                r = MediaDataController.getInstance(UserConfig.selectedAccount).getReactionsMap().get(react.emojicon);
            }
            if (r == null) {
                document = AnimatedEmojiDrawable.findDocument(UserConfig.selectedAccount, react.documentId);
                if (document != null) {
                    String emojicon = MessageObject.findAnimatedEmojiEmoticon(document, null);
                    if (emojicon != null) {
                        r = MediaDataController.getInstance(UserConfig.selectedAccount).getReactionsMap().get(emojicon);
                    }
                }
            }
            if (document == null && r != null) {
                ImageReceiver imageReceiver = new ImageReceiver();
                imageReceiver.setParentView(this);
                imageReceiver.setUniqKeyPrefix(Integer.toString(animationUniq++));
                imageReceiver.setImage(ImageLocation.getForDocument(r.around_animation), effectsSize + "_" + effectsSize + "_nolimit", null, "tgs", r, 1);
                imageReceiver.setAutoRepeat(0);
                imageReceiver.onAttachedToWindow();
                animations.add(imageReceiver);
                invalidate();
            } else {
                AnimatedEmojiDrawable drawable;
                if (document == null) {
                    drawable = AnimatedEmojiDrawable.make(AnimatedEmojiDrawable.CACHE_TYPE_KEYBOARD, UserConfig.selectedAccount, react.documentId);
                } else {
                    drawable = AnimatedEmojiDrawable.make(AnimatedEmojiDrawable.CACHE_TYPE_KEYBOARD, UserConfig.selectedAccount, document);
                }
                if (color != null) {
                    drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                }
                AnimatedEmojiEffect effect = AnimatedEmojiEffect.createFrom(drawable, false, !drawable.canOverrideColor());
                effect.setView(this);
                animations.add(effect);
                invalidate();
            }
        }

        private Integer color;

        public void setColor(int color) {
            this.color = color;
            final ColorFilter colorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY);
            final ColorFilter colorFilterEmoji = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);
            for (int i = 0; i < animations.size(); ++i) {
                Object animation = animations.get(i);
                if (animation instanceof ImageReceiver) {
                    ((ImageReceiver) animation).setColorFilter(colorFilter);
                } else if (animation instanceof AnimatedEmojiEffect) {
                    ((AnimatedEmojiEffect) animation).animatedEmojiDrawable.setColorFilter(colorFilterEmoji);
                }
            }
        }
    }

    public void animateStateChange(long documentId) {
        animatedStatus.animateChange(ReactionsLayoutInBubble.VisibleReaction.fromCustomEmoji(documentId));
        updateRightDrawable = true;
    }

    public void getEmojiStatusLocation(Rect rect) {
        if (nameTextView.getRightDrawable() == null) {
            rect.set(nameTextView.getWidth() - 1, nameTextView.getHeight() / 2 - 1, nameTextView.getWidth() + 1, nameTextView.getHeight() / 2 + 1);
            return;
        }
        rect.set(nameTextView.getRightDrawable().getBounds());
        rect.offset((int) nameTextView.getX(), (int) nameTextView.getY());
        animatedStatus.translate(rect.centerX(), rect.centerY());
    }

    private void switchTheme(Theme.ThemeInfo themeInfo, boolean toDark) {
        int[] pos = new int[2];
        darkThemeView.getLocationInWindow(pos);
        pos[0] += darkThemeView.getMeasuredWidth() / 2;
        pos[1] += darkThemeView.getMeasuredHeight() / 2;
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needSetDayNightTheme, themeInfo, false, pos, -1, toDark, darkThemeView);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        status.attach();
        updateColors();
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiLoaded);
        for (int i = 0; i < UserConfig.MAX_ACCOUNT_COUNT; i++) {
            NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.currentUserPremiumStatusChanged);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        status.detach();
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiLoaded);
        for (int i = 0; i < UserConfig.MAX_ACCOUNT_COUNT; i++) {
            NotificationCenter.getInstance(i).removeObserver(this, NotificationCenter.currentUserPremiumStatusChanged);
        }
        if (lastAccount >= 0) {
            NotificationCenter.getInstance(lastAccount).removeObserver(this, NotificationCenter.userEmojiStatusUpdated);
            NotificationCenter.getInstance(lastAccount).removeObserver(this, NotificationCenter.updateInterfaces);
            lastAccount = -1;
        }

        if (nameTextView.getRightDrawable() instanceof AnimatedEmojiDrawable.WrapSizeDrawable) {
            Drawable drawable = ((AnimatedEmojiDrawable.WrapSizeDrawable) nameTextView.getRightDrawable()).getDrawable();
            if (drawable instanceof AnimatedEmojiDrawable) {
                ((AnimatedEmojiDrawable) drawable).removeView(nameTextView);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (Build.VERSION.SDK_INT >= 21) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(148) + AndroidUtilities.statusBarHeight, MeasureSpec.EXACTLY));
        } else {
            try {
                super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(148), MeasureSpec.EXACTLY));
            } catch (Exception e) {
                setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.dp(148));
                FileLog.e(e);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (drawPremium) {
            if (starParticlesDrawable == null) {
                starParticlesDrawable = new StarParticlesView.Drawable(15);
                starParticlesDrawable.init();
                starParticlesDrawable.speedScale = 0.8f;
                starParticlesDrawable.minLifeTime = 3000;
            }
            starParticlesDrawable.rect.set(avatarImageView.getLeft(), avatarImageView.getTop(), avatarImageView.getRight(), avatarImageView.getBottom());
            starParticlesDrawable.rect.inset(-AndroidUtilities.dp(20), -AndroidUtilities.dp(20));
            starParticlesDrawable.resetPositions();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable backgroundDrawable = Theme.getCachedWallpaper();
        //int backgroundKey = applyBackground(false);
        //boolean useImageBackground = backgroundKey != Theme.key_chats_menuTopBackground && Theme.isCustomTheme() && !Theme.isPatternWallpaper() && backgroundDrawable != null && !(backgroundDrawable instanceof ColorDrawable) && !(backgroundDrawable instanceof GradientDrawable);
        boolean useImageBackground = backgroundDrawable != null;

        if (OctoConfig.INSTANCE.drawerBackground.getValue() == DrawerBackgroundState.COLOR.getValue() || OctoConfig.INSTANCE.drawerBackground.getValue() == DrawerBackgroundState.TRANSPARENT.getValue() || OctoConfig.INSTANCE.drawerBackground.getValue() == DrawerBackgroundState.PREMIUM_DETAILS.getValue()) {
            useImageBackground = false;
        }

        boolean drawCatsShadow = false;
        int color;
        int darkBackColor = 0;
        if (!avatarAsDrawerBackground && !useImageBackground && Theme.hasThemeKey(Theme.key_chats_menuTopShadowCats)) {
            color = Theme.getColor(Theme.key_chats_menuTopShadowCats);
            drawCatsShadow = true;
        } else {
            if (Theme.hasThemeKey(Theme.key_chats_menuTopShadow)) {
                color = Theme.getColor(Theme.key_chats_menuTopShadow);
            } else {
                color = Theme.getServiceMessageColor() | 0xff000000;
            }
        }

        if (currentColor == null || currentColor != color) {
            currentColor = color;
            shadowView.getDrawable().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        }
        color = Theme.getColor(Theme.key_chats_menuName);
        if ((currentMoonColor == null || currentMoonColor != color) && sunDrawable != null) {
            currentMoonColor = color;
            sunDrawable.beginApplyLayerColors();
            sunDrawable.setLayerColor("Sunny.**", currentMoonColor);
            sunDrawable.setLayerColor("Path 6.**", currentMoonColor);
            sunDrawable.setLayerColor("Path.**", currentMoonColor);
            sunDrawable.setLayerColor("Path 5.**", currentMoonColor);
            sunDrawable.commitApplyLayerColors();
        }
        nameTextView.setTextColor(Theme.getColor(Theme.key_chats_menuName));
        if (useImageBackground || avatarAsDrawerBackground) {
            phoneTextView.setTextColor(Theme.getColor(Theme.key_chats_menuPhone));
            if (shadowView.getVisibility() != VISIBLE) {
                shadowView.setVisibility(VISIBLE);
            }
            if (avatarAsDrawerBackground) {
                imageReceiver.setImageCoords(0, 0, getMeasuredWidth(), getMeasuredHeight());
                imageReceiver.draw(canvas);
                darkBackColor = Theme.getColor(Theme.key_listSelector);
            } else if (backgroundDrawable instanceof ColorDrawable || backgroundDrawable instanceof GradientDrawable || backgroundDrawable instanceof MotionBackgroundDrawable) {
                backgroundDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
                backgroundDrawable.draw(canvas);
                darkBackColor = Theme.getColor(Theme.key_listSelector);
            } else if (backgroundDrawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();

                float scaleX = (float) getMeasuredWidth() / (float) bitmap.getWidth();
                float scaleY = (float) getMeasuredHeight() / (float) bitmap.getHeight();
                float scale = Math.max(scaleX, scaleY);
                int width = (int) (getMeasuredWidth() / scale);
                int height = (int) (getMeasuredHeight() / scale);
                int x = (bitmap.getWidth() - width) / 2;
                int y = (bitmap.getHeight() - height) / 2;
                srcRect.set(x, y, x + width, y + height);
                destRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
                try {
                    canvas.drawBitmap(bitmap, srcRect, destRect, paint);
                } catch (Throwable e) {
                    FileLog.e(e);
                }
                darkBackColor = (Theme.getServiceMessageColor() & 0x00ffffff) | 0x50000000;
            }
        } else {
            int visibility = drawCatsShadow ? VISIBLE : INVISIBLE;
            if (shadowView.getVisibility() != visibility) {
                shadowView.setVisibility(visibility);
            }
            phoneTextView.setTextColor(Theme.getColor(Theme.key_chats_menuPhoneCats));
            super.onDraw(canvas);
            darkBackColor = Theme.getColor(Theme.key_listSelector);
        }

        @SuppressLint("DrawAllocation") GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                new int[] {
                        Theme.getColor(Theme.key_chats_menuBackground),
                        AndroidUtilities.getTransparentColor(Theme.getColor(Theme.key_chats_menuBackground), 0)
                }
        );
        gradientBackground.setBackground(gradient);

        darkenBackground.setBackground(new ColorDrawable(Color.BLACK));


//        if (darkBackColor != 0) {
//            if (darkBackColor != darkThemeBackgroundColor) {
//                backPaint.setColor(darkThemeBackgroundColor = darkBackColor);
//                if (Build.VERSION.SDK_INT >= 21) {
//                    Theme.setSelectorDrawableColor(darkThemeView.getBackground(), darkThemeBackgroundColor = darkBackColor, true);
//                }
//            }
//            if (useImageBackground && backgroundDrawable instanceof BitmapDrawable) {
//                canvas.drawCircle(darkThemeView.getX() + darkThemeView.getMeasuredWidth() / 2, darkThemeView.getY() + darkThemeView.getMeasuredHeight() / 2, AndroidUtilities.dp(17), backPaint);
//            }
//        }
        if (drawPremium && drawPremiumProgress != 1f) {
            drawPremiumProgress += 16 / 220f;
        } else if (!drawPremium && drawPremiumProgress != 0) {
            drawPremiumProgress -= 16 / 220f;
        }
        drawPremiumProgress = Utilities.clamp(drawPremiumProgress, 1f, 0);
        if (drawPremiumProgress != 0) {
            if (gradientTools == null) {
                gradientTools = new PremiumGradient.PremiumGradientTools(Theme.key_premiumGradientBottomSheet1, Theme.key_premiumGradientBottomSheet2, Theme.key_premiumGradientBottomSheet3, -1);
                gradientTools.x1 = 0;
                gradientTools.y1 = 1.1f;
                gradientTools.x2 = 1.5f;
                gradientTools.y2 = -0.2f;
                gradientTools.exactly = true;
            }
            gradientTools.gradientMatrix(0, 0, getMeasuredWidth(), getMeasuredHeight(), 0, 0);
            gradientTools.paint.setAlpha((int) (drawPremiumProgress * 255));
            canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), gradientTools.paint);
            if (starParticlesDrawable != null) {
                starParticlesDrawable.onDraw(canvas, drawPremiumProgress);
            }
            invalidate();
        }

        if (snowflakesEffect != null) {
            snowflakesEffect.onDraw(this, canvas);
        }
    }

    public boolean isInAvatar(float x, float y) {
        if (avatarAsDrawerBackground) {
            return y <= arrowView.getTop();
        }

        return x >= avatarImageView.getLeft() && x <= avatarImageView.getRight() && y >= avatarImageView.getTop() && y <= avatarImageView.getBottom();
    }

    public boolean hasAvatar() {
        return avatarImageView.getImageReceiver().hasNotThumb();
    }

    public boolean isAccountsShown() {
        return accountsShown;
    }

    public void setAccountsShown(boolean value, boolean animated) {
        if (accountsShown == value) {
            return;
        }
        accountsShown = value;
        setArrowState(animated);
    }

    private int lastAccount = -1;
    private TLRPC.User lastUser = null;
    private Drawable premiumStar = null;

    @SuppressLint("SetTextI18n")
    public void setUser(TLRPC.User user, boolean accounts) {
        int account = UserConfig.selectedAccount;
        if (account != lastAccount) {
            if (lastAccount >= 0) {
                NotificationCenter.getInstance(lastAccount).removeObserver(this, NotificationCenter.userEmojiStatusUpdated);
                NotificationCenter.getInstance(lastAccount).removeObserver(this, NotificationCenter.updateInterfaces);
            }
            NotificationCenter.getInstance(lastAccount = account).addObserver(this, NotificationCenter.userEmojiStatusUpdated);
            NotificationCenter.getInstance(lastAccount = account).addObserver(this, NotificationCenter.updateInterfaces);
        }
        lastUser = user;
        if (user == null) {
            return;
        }
        accountsShown = accounts;
        setArrowState(false);
        CharSequence text = UserObject.getUserName(user);
        try {
            text = Emoji.replaceEmoji(text, nameTextView.getPaint().getFontMetricsInt(), false);
        } catch (Exception ignore) {}

        drawPremium = false;//user.premium;
        nameTextView.setText(text);
        statusGiftId = null;
        Long emojiStatusId = UserObject.getEmojiStatusDocumentId(user);
        if (emojiStatusId != null) {
            final boolean isCollectible = user.emoji_status instanceof TLRPC.TL_emojiStatusCollectible;
            animatedStatus.animate().alpha(1).setDuration(200).start();
            nameTextView.setDrawablePadding(AndroidUtilities.dp(4));
            status.set(emojiStatusId, true);
            if (isCollectible) {
                statusGiftId = ((TLRPC.TL_emojiStatusCollectible) user.emoji_status).collectible_id;
            }
            status.setParticles(isCollectible, true);
        } else if (user.premium) {
            animatedStatus.animate().alpha(1).setDuration(200).start();
            nameTextView.setDrawablePadding(AndroidUtilities.dp(4));
            if (premiumStar == null) {
                premiumStar = getResources().getDrawable(R.drawable.msg_premium_liststar).mutate();
            }
            premiumStar.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_menuPhoneCats), PorterDuff.Mode.MULTIPLY));
            status.set(premiumStar, true);
            status.setParticles(false, true);
        } else {
            animatedStatus.animateChange(null);
            animatedStatus.animate().alpha(0).setDuration(200).start();
            status.set((Drawable) null, true);
            status.setParticles(false, true);
        }
        animatedStatus.setColor(Theme.getColor(Theme.isCurrentThemeDark() ? Theme.key_chats_verifiedBackground : Theme.key_chats_menuPhoneCats));
        status.setColor(Theme.getColor(Theme.isCurrentThemeDark() ? Theme.key_chats_verifiedBackground : Theme.key_chats_menuPhoneCats));
        if (OctoConfig.INSTANCE.hidePhoneNumber.getValue()) {
            var phoneNumberAlternative = OctoConfig.INSTANCE.phoneNumberAlternative.getValue();
            switch (PhoneNumberAlternative.Companion.fromInt(phoneNumberAlternative)) {
                case SHOW_USERNAME:
                    phoneTextView.setText((user.username != null && !user.username.isEmpty()) ?
                            String.format("@%s", user.username) :
                            LocaleController.getString("MobileHidden", R.string.MobileHidden));
                    break;
                case SHOW_FAKE_PHONE_NUMBER:
                    var phoneNumber = user.phone;
                    var callingCodeInfo = PhoneFormat.getInstance().findCallingCodeInfo(phoneNumber);
                    phoneTextView.setText((callingCodeInfo != null) ?
                            String.format("+%s %s", callingCodeInfo.callingCode, OctoUtils.phoneNumberReplacer(phoneNumber, callingCodeInfo.callingCode)) :
                            LocaleController.getString("MobileHidden", R.string.MobileHidden));
                    break;
                default:
                    phoneTextView.setText(LocaleController.getString("MobileHidden", R.string.MobileHidden));
                    break;
            }
        } else {
            phoneTextView.setText(PhoneFormat.getInstance().format("+" + user.phone));
        }
        AvatarDrawable avatarDrawable = new AvatarDrawable(user);
        avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
        avatarImageView.setForUserOrChat(user, avatarDrawable);

        if (OctoConfig.INSTANCE.drawerBackground.getValue() == DrawerBackgroundState.PROFILE_PIC.getValue()) {
            ImageLocation imageLocation = ImageLocation.getForUser(user, ImageLocation.TYPE_BIG);
            avatarAsDrawerBackground = imageLocation != null;
            imageReceiver.setImage(imageLocation, "512_512", null, null, new ColorDrawable(0x00000000), 0, null, user, 1);
        } else {
            avatarAsDrawerBackground = false;
        }

        boolean drawerShowProfilePic = OctoConfig.INSTANCE.drawerShowProfilePic.getValue();
        boolean drawerGradientBackground = OctoConfig.INSTANCE.drawerGradientBackground.getValue();
        boolean drawerDarkenBackground = OctoConfig.INSTANCE.drawerDarkenBackground.getValue();
        if (isPreviewMode) {
            avatarImageView.setVisibility(VISIBLE);
            gradientBackground.setVisibility(VISIBLE);
            darkenBackground.setVisibility(VISIBLE);

            if (isPreviewModeFirstDraw || lastStateShowProfilePic != drawerShowProfilePic) {
                float scaleInitialValue = drawerShowProfilePic ? 0.8f : 1f;
                float scaleFinalValue = drawerShowProfilePic ? 1f : 0.8f;
                float alphaInitialValue = drawerShowProfilePic ? 0 : 1f;
                float alphaFinalValue = drawerShowProfilePic ? 1f : 0;

                avatarImageView.setScaleX(isPreviewModeFirstDraw ? scaleFinalValue : scaleInitialValue);
                avatarImageView.setScaleY(isPreviewModeFirstDraw ? scaleFinalValue : scaleInitialValue);
                avatarImageView.setAlpha(isPreviewModeFirstDraw ? alphaFinalValue : alphaInitialValue);

                if (!isPreviewModeFirstDraw) {
                    avatarImageView.animate().scaleX(scaleFinalValue).scaleY(scaleFinalValue).alpha(alphaFinalValue).setDuration(200).start();
                }

                lastStateShowProfilePic = drawerShowProfilePic;
            }

            if (isPreviewModeFirstDraw || lastStateGradientBackground != drawerGradientBackground) {
                float translateInitialValue = drawerGradientBackground ? 200 : 0;
                float translateFinalValue = drawerGradientBackground ? 0 : 200;
                float alphaInitialValue = drawerGradientBackground ? 0 : 1f;
                float alphaFinalValue = drawerGradientBackground ? 1f : 0;

                gradientBackground.setTranslationY(isPreviewModeFirstDraw ? translateFinalValue : translateInitialValue);
                gradientBackground.setAlpha(isPreviewModeFirstDraw ? alphaFinalValue : alphaInitialValue);

                if (!isPreviewModeFirstDraw) {
                    gradientBackground.animate().translationY(translateFinalValue).alpha(alphaFinalValue).setDuration(200).start();
                }

                lastStateGradientBackground = drawerGradientBackground;
            }

            if (isPreviewModeFirstDraw || lastStateDarkenBackground != drawerDarkenBackground) {
                float selectedAlpha = OctoConfig.INSTANCE.drawerDarkenBackgroundLevel.getValue() / 255f;
                float alphaInitialValue = drawerDarkenBackground ? 0 : selectedAlpha;
                float alphaFinalValue = drawerDarkenBackground ? selectedAlpha : 0;

                darkenBackground.setAlpha(isPreviewModeFirstDraw ? alphaFinalValue : alphaInitialValue);

                if (!isPreviewModeFirstDraw) {
                    darkenBackground.animate().alpha(alphaFinalValue).setDuration(200).start();
                }

                lastStateDarkenBackground = drawerDarkenBackground;
            }

            isPreviewModeFirstDraw = false;
        } else {
            avatarImageView.setVisibility(drawerShowProfilePic ? VISIBLE : GONE);
            gradientBackground.setVisibility(drawerGradientBackground ? VISIBLE : GONE);
            darkenBackground.setVisibility(drawerDarkenBackground ? VISIBLE : GONE);
            darkenBackground.setAlpha(OctoConfig.INSTANCE.drawerDarkenBackgroundLevel.getValue() / 255f);
        }

        applyBackground(true);
        updateRightDrawable = true;
        invalidate();
    }

    public Integer applyBackground(boolean force) {
        Integer currentTag = (Integer) getTag();
        int backgroundKey = Theme.hasThemeKey(Theme.key_chats_menuTopBackground) && Theme.getColor(Theme.key_chats_menuTopBackground) != 0 ? Theme.key_chats_menuTopBackground : Theme.key_chats_menuTopBackgroundCats;
        boolean appliedColors = false;

        if (OctoConfig.INSTANCE.drawerBackground.getValue() == DrawerBackgroundState.PREMIUM_DETAILS.getValue()) {
            backgroundKey = Theme.key_avatar_backgroundActionBarBlue;

            if (UserConfig.getInstance(UserConfig.selectedAccount).isPremium()) {
                TLRPC.User user = AccountInstance.getInstance(UserConfig.selectedAccount).getUserConfig().getCurrentUser();
                MessagesController.PeerColors peerColors = MessagesController.getInstance(UserConfig.selectedAccount).profilePeerColors;
                MessagesController.PeerColor peerColor = peerColors == null ? null : peerColors.getColor(UserObject.getProfileColorId(user));

                premiumDetailsInterface.set(UserObject.getProfileEmojiId(user), user != null && user.emoji_status instanceof TLRPC.TL_emojiStatusCollectible);

                if (peerColor != null) {
                    appliedColors = true;
                    backgroundKey = Theme.key_checkbox;
                    setTag(backgroundKey);
                    setBackground(new GradientDrawable(
                            GradientDrawable.Orientation.BOTTOM_TOP,
                            new int[]{
                                    peerColor.getBgColor1(Theme.isCurrentThemeDark()),
                                    peerColor.getBgColor2(Theme.isCurrentThemeDark())
                            }
                    ));

                    if (peerColor.patternColor != 0) {
                        premiumDetailsInterface.setColor(peerColor.patternColor);
                    } else {
                        premiumDetailsInterface.setColor(adaptProfileEmojiColor(peerColor.getBgColor1(Theme.isCurrentThemeDark())));
                    }
                } else {
                    premiumDetailsInterface.setColor(adaptProfileEmojiColor(Theme.getColor(Theme.key_actionBarDefault)));
                }
            }
        } else if (OctoConfig.INSTANCE.drawerBackground.getValue() == DrawerBackgroundState.TRANSPARENT.getValue()) {
            backgroundKey = Theme.key_chats_menuBackground;
        } else if (OctoConfig.INSTANCE.drawerBackground.getValue() == DrawerBackgroundState.COLOR.getValue()) {
            backgroundKey = Theme.key_switchTrackChecked;
        }

        if ((force || currentTag == null || backgroundKey != currentTag) && !appliedColors) {
            setBackgroundColor(Theme.getColor(backgroundKey));
            setTag(backgroundKey);
        }
        return backgroundKey;
    }

    public void updateColors() {
        if (snowflakesEffect != null) {
            snowflakesEffect.updateColors();
        }
        if (animatedStatus != null) {
            animatedStatus.setColor(Theme.getColor(Theme.isCurrentThemeDark() ? Theme.key_chats_verifiedBackground : Theme.key_chats_menuPhoneCats));
        }
        if (status != null) {
            status.setColor(Theme.getColor(Theme.isCurrentThemeDark() ? Theme.key_chats_verifiedBackground : Theme.key_chats_menuPhoneCats));
        }
    }

    private void setArrowState(boolean animated) {
        final float rotation = accountsShown ? 180.0f : 0.0f;
        if (animated) {
            arrowView.animate().rotation(rotation).setDuration(220).setInterpolator(CubicBezierInterpolator.EASE_OUT).start();
        } else {
            arrowView.animate().cancel();
            arrowView.setRotation(rotation);
        }
        arrowView.setContentDescription(accountsShown ? LocaleController.getString(R.string.AccDescrHideAccounts) : LocaleController.getString(R.string.AccDescrShowAccounts));
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.emojiLoaded) {
            nameTextView.invalidate();
        } else if (id == NotificationCenter.userEmojiStatusUpdated) {
            setUser((TLRPC.User) args[0], accountsShown);
        } else if (id == NotificationCenter.currentUserPremiumStatusChanged) {
            setUser(UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser(), accountsShown);
        } else if (id == NotificationCenter.updateInterfaces) {
            int flags = (int) args[0];
            if ((flags & MessagesController.UPDATE_MASK_NAME) != 0 || (flags & MessagesController.UPDATE_MASK_AVATAR) != 0 ||
                    (flags & MessagesController.UPDATE_MASK_STATUS) != 0 || (flags & MessagesController.UPDATE_MASK_PHONE) != 0 ||
                    (flags & MessagesController.UPDATE_MASK_EMOJI_STATUS) != 0) {
                setUser(UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser(), accountsShown);
            }
        }
    }

    public AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable getEmojiStatusDrawable() {
        return status;
    }

    public Long getEmojiStatusGiftId() {
        return statusGiftId;
    }

    public View getEmojiStatusDrawableParent() {
        return nameTextView;
    }

    public void updateSunDrawable(boolean toDark) {
        if (sunDrawable != null) {
            if (toDark) {
                sunDrawable.setCustomEndFrame(36);
            } else {
                sunDrawable.setCustomEndFrame(0);
            }
        }
        if (darkThemeView != null) {
            darkThemeView.playAnimation();
        }
    }

    private static class PremiumDetailsInterface extends FrameLayout {
        public PremiumDetailsInterface(@NonNull Context context) {
            super(context);
            setWillNotDraw(false);
        }

        public final AnimatedFloat emojiLoadedT = new AnimatedFloat(this, 0, 440, CubicBezierInterpolator.EASE_OUT_QUINT);
        public final AnimatedFloat emojiFullT = new AnimatedFloat(this, 0, 440, CubicBezierInterpolator.EASE_OUT_QUINT);

        private AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable emoji = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(this, false, dp(20), AnimatedEmojiDrawable.CACHE_TYPE_ALERT_PREVIEW_STATIC);
        private long _documentId = 0;
        private boolean emojiIsCollectible;

        public void set(long documentId, boolean isCollectible) {
            if (_documentId == documentId) {
                return;
            }

            emoji.set(documentId, true);
            emojiIsCollectible = isCollectible;
            _documentId = documentId;
        }

        public void setColor(int color) {
            emoji.setColor(color);
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            emoji.attach();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            emoji.detach();
        }

        private boolean emojiLoaded;
        private boolean isEmojiLoaded() {
            if (emojiLoaded) {
                return true;
            }
            if (emoji != null && emoji.getDrawable() instanceof AnimatedEmojiDrawable drawable && drawable.getImageReceiver() != null && drawable.getImageReceiver().hasImageLoaded()) {
                return emojiLoaded = true;
            }
            return false;
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);

            if (OctoConfig.INSTANCE.drawerBackground.getValue() == DrawerBackgroundState.PREMIUM_DETAILS.getValue()) {
                float loadedState = emojiLoadedT.set(isEmojiLoaded());
                float full = emojiFullT.set(emojiIsCollectible);
                if (loadedState > 0) {
                    StarGiftPatterns.drawProfilePattern(canvas, emoji, getWidth(), getHeight(), 1.0f, full);
                }
            }
        }
    }
}
