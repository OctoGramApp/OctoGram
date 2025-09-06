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

import android.content.Context;
import android.graphics.Canvas;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;
import org.telegram.ui.Components.voip.CellFlickerDrawable;

import java.util.concurrent.atomic.AtomicBoolean;

import it.octogram.android.AiProvidersDetails;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.app.fragment.PreferencesFragment;
import it.octogram.android.app.ui.bottomsheets.AiProvidersConfigBottomSheet;
import it.octogram.android.app.ui.components.FreeSpan;
import it.octogram.android.utils.ai.MainAiHelper;
import it.octogram.android.utils.appearance.MessageStringHelper;

public class OctoChatsAiFeaturesIntroUI extends BaseFragment {
    private AppCompatTextView buttonTextView;
    private LinearLayout vg1;
    private LinearLayout vg2;

    private StickerImageView imageView1;
    private TextView titleTextView1;
    private TextView descriptionText1;
    private TextView descriptionSubText1;

    private StickerImageView imageView2;
    private TextView titleTextView2;
    private TextView descriptionText2;

    //1
    private LinearLayout descriptionLayout;

    //2
    private LinearLayout providersLayout;

    private String parameter = null;

    public OctoChatsAiFeaturesIntroUI() {
        super();
    }

    public OctoChatsAiFeaturesIntroUI(String parameter) {
        super();
        this.parameter = parameter;
    }

    @Override
    public View createView(Context context) {
        if (actionBar != null) {
            actionBar.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            actionBar.setItemsColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2), false);
            actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarWhiteSelector), false);
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

                vg1.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                vg2.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));

                imageView1.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(140), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(140), MeasureSpec.EXACTLY));
                imageView2.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(140), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(140), MeasureSpec.EXACTLY));

                if (width > height) {
                    titleTextView1.measure(MeasureSpec.makeMeasureSpec((int) (width * 0.6f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    descriptionText1.measure(MeasureSpec.makeMeasureSpec((int) (width * 0.6f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    descriptionLayout.measure(MeasureSpec.makeMeasureSpec((int) (width * 0.6f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    descriptionSubText1.measure(MeasureSpec.makeMeasureSpec((int) (width * 0.6f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    titleTextView2.measure(MeasureSpec.makeMeasureSpec((int) (width * 0.6f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    descriptionText2.measure(MeasureSpec.makeMeasureSpec((int) (width * 0.6f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    providersLayout.measure(MeasureSpec.makeMeasureSpec((int) (width * 0.6f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    buttonTextView.measure(MeasureSpec.makeMeasureSpec((int) (width * 0.6f) - AndroidUtilities.dp(24 * 2), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(50), MeasureSpec.EXACTLY));
                } else {
                    titleTextView1.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    descriptionText1.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    descriptionLayout.measure(MeasureSpec.makeMeasureSpec((int) (width * 0.8f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    descriptionSubText1.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    titleTextView2.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    descriptionText2.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    providersLayout.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                    buttonTextView.measure(MeasureSpec.makeMeasureSpec(width - AndroidUtilities.dp(24 * 2), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(50), MeasureSpec.EXACTLY));
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

                vg1.layout(0, 0, width, height);
                vg2.layout(0, 0, width, height);

                int y;
                int x;
                if (width > height) {
                    y = (height - imageView1.getMeasuredHeight()) / 2;
                    x = (int) (width * 0.4f - imageView1.getMeasuredWidth()) / 2;
                    imageView1.layout(x, y, x + imageView1.getMeasuredWidth(), y + imageView1.getMeasuredHeight());
                    imageView2.layout(x, y, x + imageView1.getMeasuredWidth(), y + imageView1.getMeasuredHeight());
                    x = (int) ((width * 0.4f) + (width * 0.6f - titleTextView1.getMeasuredWidth()) / 2f);
                    y = (int) (height / 2f - titleTextView1.getTextSize() / 2 - descriptionText1.getMeasuredHeight() / 2f - descriptionLayout.getMeasuredHeight() / 2f - descriptionSubText1.getMeasuredHeight() / 2f - buttonTextView.getMeasuredHeight() / 2f - AndroidUtilities.dp(16 + 16 + 16 + 16) / 2f);
                    titleTextView1.layout(x, y, x+titleTextView1.getMeasuredWidth(), y + titleTextView1.getMeasuredHeight());
                    x = (int) ((width * 0.4f) + (width * 0.6f - descriptionText1.getMeasuredWidth()) / 2f);
                    y += (int) titleTextView1.getTextSize() + AndroidUtilities.dp(16);
                    descriptionText1.layout(x, y, x+descriptionText1.getMeasuredWidth(), y + descriptionText1.getMeasuredHeight());
                    x = (int) ((width * 0.4f) + ((width * 0.6f) - descriptionLayout.getMeasuredWidth()) / 2f);
                    y += descriptionText1.getMeasuredHeight() + AndroidUtilities.dp(16);
                    descriptionLayout.layout(x, y, x + descriptionLayout.getMeasuredWidth(), y + descriptionLayout.getMeasuredHeight());
                    x = (int) ((width * 0.4f) + ((width * 0.6f) - descriptionSubText1.getMeasuredWidth()) / 2f);
                    y += descriptionLayout.getMeasuredHeight() + AndroidUtilities.dp(16);
                    descriptionSubText1.layout(x, y, x+descriptionSubText1.getMeasuredWidth(), y + descriptionSubText1.getMeasuredHeight());

                    x = (int) ((width * 0.4f) + ((width * 0.6f) - buttonTextView.getMeasuredWidth()) / 2f);
                    y += descriptionSubText1.getMeasuredHeight() + AndroidUtilities.dp(16);
                    buttonTextView.layout(x, y, x + buttonTextView.getMeasuredWidth(), y + buttonTextView.getMeasuredHeight());

                    x = (int) ((width * 0.4f) + (width * 0.6f - titleTextView2.getMeasuredWidth()) / 2f);
                    y = (int) (height / 2f - titleTextView2.getTextSize() / 2 - descriptionText2.getMeasuredHeight() / 2f - providersLayout.getMeasuredHeight() / 2f - AndroidUtilities.dp(16 + 16) / 2f);
                    titleTextView2.layout(x, y, x+titleTextView2.getMeasuredWidth(), y + titleTextView2.getMeasuredHeight());
                    x = (int) ((width * 0.4f) + (width * 0.6f - descriptionText2.getMeasuredWidth()) / 2f);
                    y += (int) titleTextView1.getTextSize() + AndroidUtilities.dp(16);
                    descriptionText2.layout(x, y, x+descriptionText2.getMeasuredWidth(), y + descriptionText2.getMeasuredHeight());
                    x = (int) ((width * 0.4f) + ((width * 0.6f) - providersLayout.getMeasuredWidth()) / 2f);
                    y += descriptionText2.getMeasuredHeight() + AndroidUtilities.dp(16);
                    providersLayout.layout(x, y, x + providersLayout.getMeasuredWidth(), y + providersLayout.getMeasuredHeight());
                } else {
                    y = (int) (height / 2f - imageView1.getMeasuredHeight() / 2f - titleTextView1.getTextSize() / 2 - descriptionText1.getMeasuredHeight() / 2f - descriptionLayout.getMeasuredHeight() / 2f - AndroidUtilities.dp(24 + 16 + 16 + 16) / 2f);
                    x = (width - imageView1.getMeasuredWidth()) / 2;
                    imageView1.layout(x, y, x + imageView1.getMeasuredWidth(), y + imageView1.getMeasuredHeight());
                    y += imageView1.getMeasuredHeight() + AndroidUtilities.dp(24);
                    titleTextView1.layout(0, y, titleTextView1.getMeasuredWidth(), y + titleTextView1.getMeasuredHeight());
                    y += (int) titleTextView1.getTextSize() + AndroidUtilities.dp(16);
                    descriptionText1.layout(0, y, descriptionText1.getMeasuredWidth(), y + descriptionText1.getMeasuredHeight());
                    x = (getMeasuredWidth() - descriptionLayout.getMeasuredWidth()) / 2;
                    y += descriptionText1.getMeasuredHeight() + AndroidUtilities.dp(16);
                    descriptionLayout.layout(x, y, x + descriptionLayout.getMeasuredWidth(), y + descriptionLayout.getMeasuredHeight());

                    y = (int) (height / 2f - imageView2.getMeasuredHeight() / 2f - titleTextView2.getTextSize() / 2 - descriptionText2.getMeasuredHeight() / 2f - providersLayout.getMeasuredHeight() / 2f - AndroidUtilities.dp(24 + 16 + 16) / 2f);
                    x = (width - imageView2.getMeasuredWidth()) / 2;
                    imageView2.layout(x, y, x + imageView2.getMeasuredWidth(), y + imageView2.getMeasuredHeight());
                    y += imageView2.getMeasuredHeight() + AndroidUtilities.dp(24);
                    titleTextView2.layout(0, y, titleTextView2.getMeasuredWidth(), y + titleTextView2.getMeasuredHeight());
                    y += (int) titleTextView2.getTextSize() + AndroidUtilities.dp(16);
                    descriptionText2.layout(0, y, descriptionText2.getMeasuredWidth(), y + descriptionText2.getMeasuredHeight());
                    x = (getMeasuredWidth() - providersLayout.getMeasuredWidth()) / 2;
                    y += descriptionText2.getMeasuredHeight() + AndroidUtilities.dp(16);
                    providersLayout.layout(x, y, x + providersLayout.getMeasuredWidth(), y + providersLayout.getMeasuredHeight());

                    y = height - descriptionSubText1.getMeasuredHeight() - AndroidUtilities.dp(15);
                    descriptionSubText1.layout(0, y, descriptionSubText1.getMeasuredWidth(), y + descriptionSubText1.getMeasuredHeight());

                    x = (width - buttonTextView.getMeasuredWidth()) / 2;
                    y -= buttonTextView.getMeasuredHeight() + AndroidUtilities.dp(15);
                    buttonTextView.layout(x, y, x + buttonTextView.getMeasuredWidth(), y + buttonTextView.getMeasuredHeight());
                }
            }
        };
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        ViewGroup viewGroup = (ViewGroup) fragmentView;
        viewGroup.setOnTouchListener((v, event) -> true);

        if (actionBar != null) {
            viewGroup.addView(actionBar);
        }

        viewGroup.addView(vg1 = handleVg1(context));
        viewGroup.addView(vg2 = handleVg2(context));

        return fragmentView;
    }

    private LinearLayout handleVg1(Context context) {
        LinearLayout vg1 = new LinearLayout(context);
        vg1.setVisibility(View.VISIBLE);

        imageView1 = new StickerImageView(context, UserConfig.selectedAccount);
        imageView1.setStickerPackName(OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME);
        imageView1.setStickerNum(StickerUi.TRANSLATOR_GEMINI.getValue());
        imageView1.getImageReceiver().setAutoRepeat(1);
        imageView1.setFocusable(false);
        vg1.addView(imageView1);

        titleTextView1 = new TextView(context);
        titleTextView1.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleTextView1.setGravity(Gravity.CENTER_HORIZONTAL);
        titleTextView1.setPadding(AndroidUtilities.dp(32), 0, AndroidUtilities.dp(32), 0);
        titleTextView1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
        vg1.addView(titleTextView1);

        descriptionText1 = new TextView(context);
        descriptionText1.setTextColor(ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_dialogTextBlack), 100));
        descriptionText1.setGravity(Gravity.CENTER_HORIZONTAL);
        descriptionText1.setLineSpacing(AndroidUtilities.dp(2), 1);
        descriptionText1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        descriptionText1.setPadding(AndroidUtilities.dp(48), 0, AndroidUtilities.dp(48), 0);
        vg1.addView(descriptionText1);

        descriptionLayout = new LinearLayout(context);
        descriptionLayout.setOrientation(LinearLayout.VERTICAL);
        descriptionLayout.setPadding(AndroidUtilities.dp(24), 0, AndroidUtilities.dp(24), 0);
        descriptionLayout.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        vg1.addView(descriptionLayout);

        descriptionLayout.addView(makeHint(R.drawable.photo_paint_brush, R.string.AiFeatures_Features_AskOnPhoto), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 0, 0, 0, 10));
        descriptionLayout.addView(makeHint(R.drawable.msg_translate, R.string.AiFeatures_Features_TranslateAI2), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 0, 0, 0, 10));
        descriptionLayout.addView(makeHint(R.drawable.menu_feature_transfer, R.string.AiFeatures_Features_TranscribeAI));

        descriptionSubText1 = new TextView(context);
        descriptionSubText1.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText6));
        descriptionSubText1.setGravity(Gravity.CENTER_HORIZONTAL);
        descriptionSubText1.setLineSpacing(AndroidUtilities.dp(2), 1);
        descriptionSubText1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        descriptionSubText1.setPadding(AndroidUtilities.dp(48), 0, AndroidUtilities.dp(48), 0);
        descriptionSubText1.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
        descriptionSubText1.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
        vg1.addView(descriptionSubText1);

        titleTextView1.setText(LocaleController.getString(R.string.AiFeatures_Brief));
        descriptionText1.setText(LocaleController.getString(R.string.AiFeatures_PreDistribInitial));
        descriptionSubText1.setText(MessageStringHelper.fromHtml(formatString(R.string.AiFeatures_EnableOptionsTerms_Desc2, OctoConfig.MAIN_DOMAIN)));

        AtomicBoolean animated = new AtomicBoolean(false);
        buttonTextView = new AppCompatTextView(context) {
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
                cellFlickerDrawable.draw(canvas, AndroidUtilities.rectTmp, AndroidUtilities.dp(4), null);
                invalidate();
            }
        };
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(AndroidUtilities.bold());
        buttonTextView.setBackground(Theme.AdaptiveRipple.filledRectByKey(Theme.key_featuredStickers_addButton, 6));
        buttonTextView.setPadding(AndroidUtilities.dp(34), AndroidUtilities.dp(8), AndroidUtilities.dp(34), AndroidUtilities.dp(8));
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        buttonTextView.setText(MainAiHelper.hasAvailableProviders() ? R.string.AiFeatures_Brief_StartUsing : R.string.Next);
        buttonTextView.setOnClickListener(v -> {
            if (MainAiHelper.hasAvailableProviders()) {
                OctoConfig.INSTANCE.aiFeaturesAcceptedTerms.updateValue(true);
                OctoChatsAiFeaturesUI ui = new OctoChatsAiFeaturesUI();
                ui.setShowEnabledBulletin(true);
                presentFragment(new PreferencesFragment(ui, parameter), true);
                return;
            }

            if (animated.get()) {
                return;
            }
            animated.set(true);

            vg1.animate().alpha(0f).translationX(-100).withEndAction(() -> vg1.setVisibility(View.GONE)).setDuration(150).setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).start();
            vg2.setAlpha(0f);
            vg2.setTranslationX(100);
            vg2.setVisibility(View.VISIBLE);
            vg2.animate().alpha(1f).translationX(0).setDuration(150).setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).start();
            buttonTextView.setClickable(false);
            buttonTextView.setEnabled(false);
        });
        vg1.addView(buttonTextView);

        return vg1;
    }

    private LinearLayout handleVg2(Context context) {
        LinearLayout vg2 = new LinearLayout(context);
        vg2.setVisibility(View.GONE);

        imageView2 = new StickerImageView(context, UserConfig.selectedAccount);
        imageView2.setStickerPackName(OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME);
        imageView2.setStickerNum(StickerUi.MAIN.getValue());
        imageView2.getImageReceiver().setAutoRepeat(1);
        imageView2.setFocusable(false);
        vg2.addView(imageView2);

        titleTextView2 = new TextView(context);
        titleTextView2.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleTextView2.setGravity(Gravity.CENTER_HORIZONTAL);
        titleTextView2.setPadding(AndroidUtilities.dp(32), 0, AndroidUtilities.dp(32), 0);
        titleTextView2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
        vg2.addView(titleTextView2);

        descriptionText2 = new TextView(context);
        descriptionText2.setTextColor(ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_dialogTextBlack), 100));
        descriptionText2.setGravity(Gravity.CENTER_HORIZONTAL);
        descriptionText2.setLineSpacing(AndroidUtilities.dp(2), 1);
        descriptionText2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        descriptionText2.setPadding(AndroidUtilities.dp(48), 0, AndroidUtilities.dp(48), 0);
        vg2.addView(descriptionText2);

        providersLayout = new LinearLayout(context);
        providersLayout.setOrientation(LinearLayout.VERTICAL);
        providersLayout.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        vg2.addView(providersLayout);

        int i = 0;
        for (AiProvidersDetails provider : AiProvidersDetails.getEntries()) {
            i++;
            boolean isAdded = MainAiHelper.isProviderAvailable(provider);
            if (!isAdded) {
                TextCell v = new TextCell(context, 23, false, false, getResourceProvider());
                v.setBackground(Theme.getSelectorDrawable(false));
                v.setOnClickListener(view -> new AiProvidersConfigBottomSheet(context, OctoChatsAiFeaturesIntroUI.this, provider, new AiProvidersConfigBottomSheet.AiConfigInterface() {
                    @Override
                    public void onStateUpdated() {
                        if (MainAiHelper.hasAvailableProviders()) {
                            OctoChatsAiFeaturesUI ui = new OctoChatsAiFeaturesUI();
                            ui.setShowEnabledBulletin(true);
                            presentFragment(new PreferencesFragment(ui, parameter), true);
                        }
                    }

                    @Override
                    public boolean canShowSuccessBulletin() {
                        return false;
                    }
                }).show());
                v.setColors(Theme.key_windowBackgroundWhiteBlueText4, Theme.key_windowBackgroundWhiteBlueText4);

                CharSequence text = provider.getUseThisProviderString();
                if (provider.isSuggested()) {
                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                    spannableStringBuilder.append(text);
                    spannableStringBuilder.append("   d");
                    FreeSpan span = new FreeSpan(10);
                    span.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4));
                    span.setText(LocaleController.getString(R.string.AiFeatures_PreDistrib_Suggested).toUpperCase());
                    spannableStringBuilder.setSpan(span, spannableStringBuilder.length() - 1, spannableStringBuilder.length(), 0);
                    text = spannableStringBuilder;
                }

                v.setTextAndIcon(text, R.drawable.msg_add, !OctoConfig.INSTANCE.disableDividers.getValue() && i != AiProvidersDetails.getEntries().size());
                providersLayout.addView(v);
            }
        }

        titleTextView2.setText(LocaleController.getString(R.string.AiFeatures_Brief));
        descriptionText2.setText(LocaleController.getString(R.string.AiFeatures_PreDistrib));

        return vg2;
    }

    private FrameLayout makeHint(int resId, int title) {
        FrameLayout hint = new FrameLayout(getContext());

        ImageView imageView = new ImageView(getContext());
        imageView.setColorFilter(Theme.getColor(Theme.key_dialogTextBlack, getResourceProvider()));
        imageView.setImageResource(resId);
        hint.addView(imageView, LayoutHelper.createFrame(24, 24, Gravity.LEFT | Gravity.TOP, 0, 0, 0, 0));

        TextView textView1 = new TextView(getContext());
        textView1.setTextColor(Theme.getColor(Theme.key_dialogTextBlack, getResourceProvider()));
        textView1.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp(14));
        textView1.setText(LocaleController.getString(title));
        hint.addView(textView1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 41, 0, 0, 0));

        return hint;
    }

    @Override
    public boolean isLightStatusBar() {
        int color = Theme.getColor(Theme.key_windowBackgroundWhite, null, true);
        return ColorUtils.calculateLuminance(color) > 0.7f;
    }
}
