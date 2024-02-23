package it.octogram.android.preferences.ui.custom.doublebottom;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IntDef;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.voip.CellFlickerDrawable;
import org.telegram.ui.PasscodeActivity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
public class AccountProtectionIntro extends BaseFragment {
    private RLottieImageView imageView;
    private TextView buttonTextView;
    private TextView descriptionText;
    private TextView titleTextView;
    private AccountSelectList accountSelectList;
    private boolean flickerButton;
    private final int currentType;

    public static final int CONFIRM_DOUBLE_BOTTOM = 0;
    public static final int SELECT_ACCOUNT = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            CONFIRM_DOUBLE_BOTTOM,
            SELECT_ACCOUNT
    })
    public @interface ActionType {}

    public AccountProtectionIntro(@ActionType int type) {
        super();
        currentType = type;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View createView(Context context) {
        if (actionBar != null) {
            actionBar.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            if (currentType == CONFIRM_DOUBLE_BOTTOM) {
                actionBar.setItemsColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2), false);
                actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarWhiteSelector), false);
            }
            actionBar.setCastShadows(false);
            actionBar.setAddToContainer(false);
            actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        finishFragment();
                    }
                }
            });
        }
        fragmentView = new ViewGroup(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int height = MeasureSpec.getSize(heightMeasureSpec);

                if (actionBar != null) {
                    actionBar.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), heightMeasureSpec);
                }

                // TODO: Switch of different UI
                imageView.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(140), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(140), MeasureSpec.EXACTLY));
                if (width > height) {
                    titleTextView.measure(MeasureSpec.makeMeasureSpec((int) (width * 0.6f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    descriptionText.measure(MeasureSpec.makeMeasureSpec((int) (width * 0.6f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    buttonTextView.measure(MeasureSpec.makeMeasureSpec((int) (width * 0.6f), MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(42), MeasureSpec.EXACTLY));
                    if (currentType == SELECT_ACCOUNT) {
                        accountSelectList.measure(MeasureSpec.makeMeasureSpec((int) (width * 0.6f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    }
                } else {
                    titleTextView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    descriptionText.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    buttonTextView.measure(MeasureSpec.makeMeasureSpec(width - AndroidUtilities.dp(24 * 2), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(50), MeasureSpec.EXACTLY));
                    if (currentType == SELECT_ACCOUNT) {
                        accountSelectList.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    }
                }
                setMeasuredDimension(width, height);
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                if (actionBar != null) {
                    actionBar.layout(0, 0, r, actionBar.getMeasuredHeight());
                }
                int width = r - l;
                int height = b - t;

                int y;
                int x;
                if (r > b) {
                    y = (height - imageView.getMeasuredHeight()) / 2;
                    x = (int) (width * 0.5f - imageView.getMeasuredWidth()) / 2;
                    imageView.layout(x, y, x + imageView.getMeasuredWidth(), y + imageView.getMeasuredHeight());
                    x = (int) (width * 0.4f);
                    y = (int) (height * 0.14f);
                    titleTextView.layout(x, y, x + titleTextView.getMeasuredWidth(), y + titleTextView.getMeasuredHeight());
                    x = (int) (width * 0.4f);
                    y = (int) (height * 0.31f);
                    descriptionText.layout(x, y, x + descriptionText.getMeasuredWidth(), y + descriptionText.getMeasuredHeight());
                    y += descriptionText.getMeasuredHeight() + AndroidUtilities.dp(16);
                    if (currentType == SELECT_ACCOUNT) {
                        accountSelectList.layout(x, y, x + accountSelectList.getMeasuredWidth(), Math.min(y + accountSelectList.getMeasuredHeight(), height));
                    }
                    x = (int) (width * 0.4f + (width * 0.6f - buttonTextView.getMeasuredWidth()) / 2);
                    y = (int) (height * 0.78f);
                } else {
                    if (currentType == SELECT_ACCOUNT) {
                        y = (int) (height * 0.2f);
                    } else {
                        y = (int) (height * 0.3f);
                    }
                    x = (width - imageView.getMeasuredWidth()) / 2;
                    imageView.layout(x, y, x + imageView.getMeasuredWidth(), y + imageView.getMeasuredHeight());
                    y += imageView.getMeasuredHeight() + AndroidUtilities.dp(24);
                    titleTextView.layout(0, y, titleTextView.getMeasuredWidth(), y + titleTextView.getMeasuredHeight());
                    y += (int) (titleTextView.getTextSize() + AndroidUtilities.dp(16));
                    descriptionText.layout(0, y, descriptionText.getMeasuredWidth(), y + descriptionText.getMeasuredHeight());
                    y += descriptionText.getMeasuredHeight() + AndroidUtilities.dp(20);
                    if (currentType == SELECT_ACCOUNT) {
                        accountSelectList.layout(0, y, accountSelectList.getMeasuredWidth(), Math.min(y + accountSelectList.getMeasuredHeight(), height));
                    }
                    x = (width - buttonTextView.getMeasuredWidth()) / 2;
                    y = height - buttonTextView.getMeasuredHeight() - AndroidUtilities.dp(48);
                }
                buttonTextView.layout(x, y, x + buttonTextView.getMeasuredWidth(), y + buttonTextView.getMeasuredHeight());
            }
        };
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        ViewGroup viewGroup = (ViewGroup) fragmentView;
        viewGroup.setOnTouchListener((v, event) -> true);

        if (actionBar != null) {
            viewGroup.addView(actionBar);
        }

        imageView = new RLottieImageView(context);
        viewGroup.addView(imageView);

        titleTextView = new TextView(context);
        titleTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        titleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        titleTextView.setPadding(AndroidUtilities.dp(32), 0, AndroidUtilities.dp(32), 0);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
        viewGroup.addView(titleTextView);

        descriptionText = new TextView(context);
        descriptionText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText6));
        descriptionText.setGravity(Gravity.CENTER_HORIZONTAL);
        descriptionText.setLineSpacing(AndroidUtilities.dp(2), 1);
        descriptionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        descriptionText.setPadding(AndroidUtilities.dp(48), 0, AndroidUtilities.dp(48), 0);
        viewGroup.addView(descriptionText);

        accountSelectList = new AccountSelectList(context) {
            @Override
            public void onItemClick(long accountId) {
                super.onItemClick(accountId);
                presentFragment(new PasscodeActivity(PasscodeActivity.TYPE_SETUP_CODE, accountId), true);
            }
        };
        if (currentType == SELECT_ACCOUNT) {
            viewGroup.addView(accountSelectList);
        }

        buttonTextView = new androidx.appcompat.widget.AppCompatTextView(context) {
            CellFlickerDrawable cellFlickerDrawable;

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                if (flickerButton) {
                    if (cellFlickerDrawable == null) {
                        cellFlickerDrawable = new CellFlickerDrawable();
                        cellFlickerDrawable.drawFrame = false;
                        cellFlickerDrawable.repeatProgress = 2f;
                    }
                    cellFlickerDrawable.setParentWidth(getMeasuredWidth());
                    AndroidUtilities.rectTmp.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
                    cellFlickerDrawable.draw(canvas, AndroidUtilities.rectTmp, AndroidUtilities.dp(4), null);
                    invalidate();
                }
            }
        };

        buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
        if (currentType == CONFIRM_DOUBLE_BOTTOM) {
            viewGroup.addView(buttonTextView);
        }
        buttonTextView.setOnClickListener(v -> {
            if (currentType == CONFIRM_DOUBLE_BOTTOM) {
                presentFragment(new AccountProtectionIntro(SELECT_ACCOUNT), true);
            }
        });
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setOnClickListener(v -> {
            if (!imageView.getAnimatedDrawable().isRunning()) {
                imageView.getAnimatedDrawable().setCurrentFrame(0, false);
                imageView.playAnimation();
            }
        });
        switch (currentType) {
            case CONFIRM_DOUBLE_BOTTOM:
                imageView.setAnimation(R.raw.double_bottom, 200, 200);
                titleTextView.setText(LocaleController.getString("AccountProtection", R.string.AccountProtection));
                descriptionText.setText(LocaleController.getString("AccountProtectionDesc", R.string.AccountProtectionDesc));
                buttonTextView.setText(LocaleController.getString("EnableAccountProtection", R.string.EnableAccountProtection));
                flickerButton = true;
                break;
            case SELECT_ACCOUNT:
                imageView.setAnimation(R.raw.duck_counting, 200, 200);
                titleTextView.setText(LocaleController.getString("SelectAccount", R.string.SelectAccount));
                descriptionText.setText(LocaleController.getString("SelectAccountDesc", R.string.SelectAccountDesc));
        }
        imageView.getAnimatedDrawable().setAutoRepeat(1);
        imageView.playAnimation();
        if (flickerButton) {
            buttonTextView.setPadding(AndroidUtilities.dp(34), AndroidUtilities.dp(8), AndroidUtilities.dp(34), AndroidUtilities.dp(8));
            buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        }
        return fragmentView;
    }
}