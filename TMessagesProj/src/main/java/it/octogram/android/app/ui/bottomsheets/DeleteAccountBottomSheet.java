/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui.bottomsheets;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarsImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;
import org.telegram.ui.TwoStepVerificationActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.utils.OctoLogging;

public class DeleteAccountBottomSheet extends BottomSheet {

    private int selectedPosition;
    private final BaseFragment fragment;

    public DeleteAccountBottomSheet(Context context, BaseFragment fragment, DeleteAccountBottomSheetInterface callback) {
        super(context, true);
        setApplyBottomPadding(false);
        setApplyTopPadding(false);
        fixNavigationBar(getThemedColor(Theme.key_windowBackgroundWhite));

        this.fragment = fragment;

        TextView textView;

        FrameLayout frameLayout = new FrameLayout(getContext());

        ViewPager viewPager = new ViewPager(getContext()) {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouchEvent(MotionEvent ev) {
                return false;
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                return false;
            }

            {
                try {
                    Class<?> viewpager = ViewPager.class;
                    Field scroller = viewpager.getDeclaredField("mScroller");
                    scroller.setAccessible(true);
                    Scroller scroller1 = new Scroller(getContext()) {
                        @Override
                        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
                            super.startScroll(startX, startY, dx, dy, 3 * duration);
                        }
                    };
                    scroller.set(this, scroller1);
                } catch (Exception e) {
                    OctoLogging.e(e);
                }
            }
        };
        viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        viewPager.setOffscreenPageLimit(0);
        PagerAdapter pagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return CurrentStep.MESSAGE_HISTORY + 1;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                ViewPage viewPage = new ViewPage(getContext(), position);
                container.addView(viewPage);
                return viewPage;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }
        };
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(selectedPosition = CurrentStep.FREE_CLOUD_STORAGE);
        frameLayout.addView(viewPager, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 300, 0, 0, 18, 0, 0));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(frameLayout);

        TextView buttonTextView = new TextView(context);
        buttonTextView.setPadding(dp(34), 0, dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(AndroidUtilities.bold());
        buttonTextView.setText(getString(R.string.DeleteAccountMainButton));
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundWhite), 120)));
        buttonTextView.setOnClickListener(view -> {
            dismiss();
            callback.onDismiss();
        });
        linearLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 15, 16, 8));

        textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setText(getString(R.string.DeleteAccountContinue));
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        textView.setOnClickListener(view -> {
            if (selectedPosition == CurrentStep.MESSAGE_HISTORY) {
                dismiss();
                showLastDeleteStep();
            } else {
                viewPager.setCurrentItem(++selectedPosition);
            }
        });
        linearLayout.addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 0, 16, 0));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                View child = viewPager.getChildAt(1);
                /*
                 ALWAYS CONSIDER CHILD AT POSITION 1
                 because every time you change from section B to C, the section A is destroyed
                 so viewPager childs are ALWAYS just two.
                */
                if (child != null) {
                    child.measure(
                            View.MeasureSpec.makeMeasureSpec(viewPager.getWidth(), View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    );

                    int currentHeight = viewPager.getLayoutParams().height;
                    int newHeight = child.getMeasuredHeight();

                    if (newHeight != currentHeight) {
                        ValueAnimator animator = ValueAnimator.ofInt(currentHeight, newHeight);
                        animator.addUpdateListener(valueAnimator -> {
                            int animatedValue = (Integer) valueAnimator.getAnimatedValue();
                            ViewGroup.LayoutParams layoutParams = viewPager.getLayoutParams();
                            layoutParams.height = animatedValue;
                            viewPager.setLayoutParams(layoutParams);
                        });
                        animator.setDuration(300);
                        animator.start();
                    }
                }

                if (position == CurrentStep.MESSAGE_HISTORY) {
                    textView.setEnabled(false);
                    String defaultText = getString(R.string.DeleteAccountContinueDelete);

                    new CountDownTimer(30000, 100) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            int currentSeconds = (int) millisUntilFinished / 1000 + 1;
                            textView.setText(String.format(Locale.getDefault(), "%s (%d)", defaultText, currentSeconds));
                        }

                        @Override
                        public void onFinish() {
                            textView.setText(defaultText);
                            textView.setEnabled(true);
                            textView.setTextColor(Theme.getColor(Theme.key_text_RedBold));
                        }
                    }.start();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setCustomView(linearLayout);
    }

    private StickerUi getStickerForStepId(int stepId) {
        return switch (stepId) {
            case CurrentStep.FREE_CLOUD_STORAGE -> StickerUi.SIZE;
            case CurrentStep.GROUPS_AND_CHANNELS -> StickerUi.GROUPS_AND_CHANNELS;
            default -> StickerUi.PRIVATE;
        };
    }

    private String getTitleForStepId(int stepId) {
        return switch (stepId) {
            case CurrentStep.FREE_CLOUD_STORAGE -> getString(R.string.DeleteAccountStep1Title);
            case CurrentStep.GROUPS_AND_CHANNELS -> getString(R.string.DeleteAccountStep2Title);
            default -> getString(R.string.DeleteAccountStep3Title);
        };
    }

    private String getDescriptionForStepId(int stepId) {
        return switch (stepId) {
            case CurrentStep.FREE_CLOUD_STORAGE ->
                    getString(R.string.DeleteAccountStep1Description);
            case CurrentStep.GROUPS_AND_CHANNELS ->
                    getString(R.string.DeleteAccountStep2Description);
            default -> getString(R.string.DeleteAccountStep3Description);
        };
    }

    private void showLastDeleteStep() {
        TwoStepVerificationActivity twoStepFragment = new TwoStepVerificationActivity();
        twoStepFragment.setDelegate(0, password -> {
            twoStepFragment.needHideProgress();
            twoStepFragment.finishFragment();

            AlertDialog.Builder warningBuilder = new AlertDialog.Builder(getContext());
            warningBuilder.setTitle(getString(R.string.DeleteAccount));
            warningBuilder.setMessage(getString(R.string.DeleteAccountLastPopup));
            warningBuilder.setPositiveButton(getString(R.string.DeleteAccount), (dialog1, which1) -> {
                AlertDialog progressDialog = new AlertDialog(getContext(), AlertDialog.ALERT_TYPE_SPINNER);
                progressDialog.setCanCancel(false);

                TL_account.deleteAccount req = new TL_account.deleteAccount();
                req.reason = String.format(Locale.US, "deletion requested by the user via %s - request made after 2fa confirmation", OctoConfig.MAIN_DOMAIN);
                ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                    if (response instanceof TLRPC.TL_boolTrue) {
                        AccountInstance accountInstance = AccountInstance.getInstance(currentAccount);
                        if (accountInstance.getUserConfig().getClientUserId() != 0) {
                            accountInstance.getUserConfig().clearConfig();
                            accountInstance.getMessagesController().performLogout(0);
                        }
                    } else if (error == null || error.code != -1000) {
                        String errorText = getString(R.string.ErrorOccurred);
                        if (error != null) {
                            errorText += "\n" + error.text;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle(getString(R.string.AppName));
                        builder.setMessage(errorText);
                        builder.setPositiveButton(getString(R.string.OK), null);
                        builder.show();
                    }
                }));
            });
            warningBuilder.setNegativeButton(getString(R.string.Cancel), null);
            AlertDialog warningDialog = warningBuilder.create();

            warningDialog.setOnShowListener(dialog1 -> {
                TextView positiveButton = (TextView) warningDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                String defaultText = (String) positiveButton.getText();
                positiveButton.setEnabled(false);

                new CountDownTimer(15000, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        int currentSeconds = (int) millisUntilFinished / 1000 + 1;
                        positiveButton.setText(String.format(Locale.getDefault(), "%s (%d)", defaultText, currentSeconds));
                    }

                    @Override
                    public void onFinish() {
                        positiveButton.setText(defaultText);
                        positiveButton.setEnabled(true);
                    }
                }.start();
            });

            warningBuilder.show();
            warningDialog.redPositive();
        });
        fragment.presentFragment(twoStepFragment);
    }

    public interface DeleteAccountBottomSheetInterface {
        void onDismiss();
    }

    public static class CurrentStep {
        public static final int FREE_CLOUD_STORAGE = 0;
        public static final int GROUPS_AND_CHANNELS = 1;
        public static final int MESSAGE_HISTORY = 2;
    }

    private class ViewPage extends LinearLayout {

        public ViewPage(Context context, int p) {
            super(context);
            setOrientation(VERTICAL);

            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            StickerImageView imageView = new StickerImageView(context, UserConfig.selectedAccount);
            imageView.setStickerPackName(OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME);
            imageView.setStickerNum(getStickerForStepId(p).getValue());
            imageView.getImageReceiver().setAutoRepeat(1);
            linearLayout.addView(imageView, LayoutHelper.createLinear(144, 144, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 16));

            TextView textView = new TextView(context);
            textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            textView.setTypeface(AndroidUtilities.bold());
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setText(getTitleForStepId(p));
            textView.setPadding(dp(30), 0, dp(30), 0);
            linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            textView = new TextView(context);
            textView.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setText(getDescriptionForStepId(p));
            textView.setPadding(dp(30), dp(10), dp(30), dp(21));
            linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            if (p == CurrentStep.GROUPS_AND_CHANNELS || p == CurrentStep.MESSAGE_HISTORY) {
                boolean useUsersList = p == CurrentStep.MESSAGE_HISTORY;
                ConcurrentHashMap<Long, TLRPC.User> usersList = new ConcurrentHashMap<>();
                ConcurrentHashMap<Long, TLRPC.Chat> chatsList = new ConcurrentHashMap<>();
                int currentItemCounter;

                MessagesController cInst = MessagesController.getInstance(currentAccount);

                if (useUsersList) {
                    ArrayList<TLRPC.Dialog> dialogs = cInst.dialogsUsersOnly;
                    currentItemCounter = dialogs.size();
                    for (TLRPC.Dialog dialog : dialogs) {
                        TLRPC.User user = cInst.getUser(dialog.id);

                        if (user == null) {
                            continue;
                        }

                        if (UserObject.isReplyUser(user) || UserObject.isDeleted(user) || user.bot) {
                            continue;
                        }

                        if (usersList.size() >= 3) {
                            break;
                        }

                        usersList.put(dialog.id, user);
                    }
                } else {
                    chatsList = cInst.getChats();
                    currentItemCounter = chatsList.size();
                }

                if ((useUsersList && usersList.size() >= 3) || (!useUsersList && chatsList.size() >= 3)) {
                    float factor = 0.65f;
                    int avatarSize = 38;
                    AvatarsImageView avatarsImageView = new AvatarsImageView(context, false);
                    avatarsImageView.setAvatarsTextSize(dp(20));
                    avatarsImageView.setSize(dp(avatarSize));
                    avatarsImageView.setStepFactor(factor);

                    int i = 0;
                    if (useUsersList) {
                        for (TLRPC.User user : usersList.values()) {
                            if (i >= 3) {
                                break;
                            }

                            avatarsImageView.setObject(i, currentAccount, user);
                            i++;
                        }
                    } else {
                        for (TLRPC.Chat chat : chatsList.values()) {
                            if (i >= 3) {
                                break;
                            }

                            avatarsImageView.setObject(i, currentAccount, chat);
                            i++;
                        }
                    }

                    avatarsImageView.setCount(i);
                    avatarsImageView.commitTransition(false);
                    int avatarContainerWidth = (int) (avatarSize + (i - 1) * (avatarSize * factor + 1));
                    linearLayout.addView(avatarsImageView, LayoutHelper.createLinear(avatarContainerWidth, 44, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 15, 0, 4));

                    textView = new TextView(context);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
                    textView.setTextColor(getThemedColor(Theme.key_dialogTextGray3));
                    textView.setGravity(Gravity.CENTER);

                    if (useUsersList) {
                        textView.setText(formatString(R.string.DeleteAccountStep3Counter, currentItemCounter));
                    } else {
                        textView.setText(formatString(R.string.DeleteAccountStep2Counter, currentItemCounter));
                    }

                    linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 10, 0, 10, 24));
                }
            }

            addView(linearLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0, 0, 0));
        }
    }
}
