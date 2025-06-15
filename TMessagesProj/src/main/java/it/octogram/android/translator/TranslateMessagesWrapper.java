/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.translator;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.LaunchActivity;

import java.util.function.Supplier;

import it.octogram.android.ai.MainAiHelper;
import it.octogram.android.ai.ui.MainAiBottomSheet;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.ui.OctoAiFeaturesUI;

public class TranslateMessagesWrapper {
    public static void fillState(FillStateData data) {
        boolean canTranslateDefault = data.originalSubItem.getVisibility() == View.VISIBLE;
        if (!canTranslateDefault && MainAiHelper.canTranslateMessages()) {
            data.originalSubItem.setVisibility(View.VISIBLE);
            data.originalSubItem.setIcon(R.drawable.cup_star_solar);
            data.originalSubItem.setText(getString(R.string.AiFeatures_Features_TranslateAI));
            data.originalSubItem.setOnClickListener(e -> {
                if (data.onSheetOpen != null) {
                    data.onSheetOpen.run();
                }
                openAiTranslation(data);
            });
        } else if (canTranslateDefault && MainAiHelper.canTranslateMessages()) {
            TranslateMessageSwipeActivity viewToSwipeBack = new TranslateMessageSwipeActivity(data);
            data.originalSubItem.setMinimumWidth(AndroidUtilities.dp(196));
            data.originalSubItem.setRightIcon(R.drawable.msg_arrowright);
            data.originalSubItem.setText(getString(R.string.AiFeatures_Features_TranslateViaGeneric));
            int swipeBackIndex = data.popupWindowLayout.addViewToSwipeBack(viewToSwipeBack.windowLayout);
            data.originalSubItem.openSwipeBackLayout = () -> {
                if (data.popupWindowLayout.getSwipeBack() != null) {
                    data.popupWindowLayout.getSwipeBack().openForeground(swipeBackIndex);
                }
            };
            data.originalSubItem.setOnClickListener(view -> data.originalSubItem.openSwipeBack());
        }
    }

    private static void openAiTranslation(FillStateData data) {
        AndroidUtilities.runOnUIThread(() -> {
            MainAiBottomSheet sheet = new MainAiBottomSheet(data);
            sheet.setDimBehind(!data.supportsActivityRelatedDimBehind);
            sheet.show();
        });
    }

    private static class TranslateMessageSwipeActivity {
        public ActionBarPopupWindow.ActionBarPopupWindowLayout windowLayout;

        public TranslateMessageSwipeActivity(FillStateData data) {
            windowLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(data.context, 0, null);
            windowLayout.setFitItems(true);

            if (data.popupWindowLayout.getSwipeBack() != null) {
                ActionBarMenuSubItem backItem = ActionBarMenuItem.addItem(windowLayout, R.drawable.msg_arrow_back, getString(R.string.Back), false, null);
                backItem.setOnClickListener(view -> data.popupWindowLayout.getSwipeBack().closeForeground());
            }

            ActionBarMenuSubItem item = ActionBarMenuItem.addItem(windowLayout, R.drawable.msg_translate, LocaleController.formatString(R.string.AiFeatures_Features_TranslateVia, TranslationsWrapper.getProviderName()), false, null);
            ActionBarMenuSubItem finalItem = item;
            item.setOnClickListener(view -> {
                if (data.getListener() != null) {
                    data.getListener().onClick(finalItem);
                }
            });

            item = ActionBarMenuItem.addItem(windowLayout, R.drawable.cup_star_solar, getString(R.string.AiFeatures_Features_TranslateAI), false, null);
            item.setOnClickListener(view -> {
                if (data.onSheetOpen != null) {
                    data.onSheetOpen.run();
                }
                openAiTranslation(data);
            });

            FrameLayout gap = new FrameLayout(data.context);
            gap.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuSeparator));
            View gapShadow = new View(data.context);
            gapShadow.setBackground(Theme.getThemedDrawableByKey(data.context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
            gap.addView(gapShadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            gap.setTag(R.id.fit_width_tag, 1);
            windowLayout.addView(gap, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8));

            LinkSpanDrawable.LinksTextView textView = new LinkSpanDrawable.LinksTextView(data.context);
            textView.setTag(R.id.fit_width_tag, 1);
            textView.setPadding(AndroidUtilities.dp(13), 0, AndroidUtilities.dp(13), AndroidUtilities.dp(8));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            textView.setTextColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuItem));
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
            textView.setText(AndroidUtilities.replaceSingleLink(getString(R.string.AiFeatures_Features_TranslateAI_Details), Theme.getColor(Theme.key_windowBackgroundWhiteBlueText), () -> {
                if (data.supportsActivityRelatedDimBehind && data.onSheetClose != null) {
                    data.onSheetClose.run();
                }
                LaunchActivity.instance.presentFragment(new PreferencesFragment(new OctoAiFeaturesUI()));
            }));
            windowLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8, 0, 0));
        }
    }

    public static class FillStateData {
        public Context context;
        public Runnable onSheetOpen;
        public Runnable onSheetClose;
        public Runnable onNewFragmentOpen;
        public boolean noforwards = false;
        public boolean supportsActivityRelatedDimBehind = false;
        public ActionBarPopupWindow.ActionBarPopupWindowLayout popupWindowLayout;
        public ActionBarMenuSubItem originalSubItem;
        public View.OnClickListener listener;
        public Supplier<View.OnClickListener> listenerSupplier;
        public TranslationData translationData;

        public View.OnClickListener getListener() {
            if (listener != null) {
                return listener;
            }

            if (listenerSupplier != null) {
                return listenerSupplier.get();
            }

            return null;
        }
    }

    public static class TranslationData {
        public CharSequence text;
        public Utilities.CallbackReturn<URLSpan, Boolean> onLinkPress;
    }
}
