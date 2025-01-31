package it.octogram.android.preferences.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.ui.Components.Reactions.ReactionsUtils.createAnimatedEmojiSpan;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AdjustPanLayoutHelper;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AnimatedEmojiSpan;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.EmojiView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Reactions.CustomReactionEditText;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import java.util.ArrayList;

import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.fragment.PreferencesFragment;



public class PinnedEmojisActivity extends BaseFragment {

    private EmojiView emojiView;
    private FrameLayout bottomDialogLayout;
    private TextCheckCell enableReactionsCell;
    private TextCheckCell hideRecentEmojisCell;
    private LinearLayout switchLayout;
    private CustomReactionEditText editText;
    private ButtonWithCounterView actionButton;
    private int keyboardHeight;
    private int keyboardHeightLand;
    private ScrollView scrollView;

    private final ArrayList<String> selectedEmoticonsList = new ArrayList<>();
    private final ArrayList<Long> selectedCustomEmojisList = new ArrayList<>();

    private boolean emojiKeyboardVisible = false;
    private boolean isPaused;

    private boolean checked = false;
    private boolean hideRecentEmojis = false;

    private PreferencesFragment fragment;

    public void setFragment(PreferencesFragment fragment) {
        this.fragment = fragment;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View createView(Context context) {
        checked = OctoConfig.INSTANCE.usePinnedEmojisFeature.getValue();
        hideRecentEmojis = OctoConfig.INSTANCE.hideRecentEmojis.getValue();

        actionBar.setTitle(LocaleController.getString(R.string.PinnedEmojisList));
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (!checkChangesBeforeExit()) {
                        finishFragment();
                    }
                }
            }
        });

        BaseFragment fragment1 = this;
        actionBar.setLongClickable(true);
        actionBar.setOnLongClickListener(v -> {
            AndroidUtilities.addToClipboard("tg://pinned_emojis");
            BulletinFactory.of(fragment1)
                    .createSimpleBulletin(R.raw.copy, getString(R.string.AffiliateProgramLinkCopiedTitle))
                    .show();

            return true;
        });

        scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        FrameLayout rootLayout = new FrameLayout(context) {
            final AdjustPanLayoutHelper adjustPanLayoutHelper = new AdjustPanLayoutHelper(this) {

                @Override
                protected void onTransitionStart(boolean keyboardVisible, int contentHeight) {
                    actionButton.setVisibility(View.VISIBLE);
                    actionButton.animate().alpha(!keyboardVisible ? 1.0f : 0.0f).withEndAction(() -> {
                        if (keyboardVisible) {
                            actionButton.setVisibility(View.INVISIBLE);
                        }
                    }).start();
                }

                @Override
                protected boolean applyTranslation() {
                    return false;
                }

                @Override
                protected boolean heightAnimationEnabled() {
                    INavigationLayout actionBarLayout = getParentLayout();
                    return !inPreviewMode && !AndroidUtilities.isTablet() && !inBubbleMode && !AndroidUtilities.isInMultiwindow && actionBarLayout != null;
                }
            };

            @Override
            protected void onAttachedToWindow() {
                super.onAttachedToWindow();
                adjustPanLayoutHelper.onAttach();
            }

            @Override
            protected void onDetachedFromWindow() {
                super.onDetachedFromWindow();
                adjustPanLayoutHelper.onDetach();
            }
        };

        LinearLayout contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);

        scrollView.addView(contentLayout);

        enableReactionsCell = new TextCheckCell(context);
        enableReactionsCell.setHeight(56);
        enableReactionsCell.setBackgroundColor(Theme.getColor(enableReactionsCell.isChecked() ? Theme.key_windowBackgroundChecked : Theme.key_windowBackgroundUnchecked));
        enableReactionsCell.setTypeface(AndroidUtilities.bold());
        enableReactionsCell.setTextAndCheck(LocaleController.getString(R.string.PinnedEmojisList_Status), false, false);
        enableReactionsCell.setColors(Theme.key_windowBackgroundCheckText, Theme.key_switchTrackBlue, Theme.key_switchTrackBlueChecked, Theme.key_switchTrackBlueThumb, Theme.key_switchTrackBlueThumbChecked);
        enableReactionsCell.setOnClickListener(v -> setCheckedEnableReactionCell(!enableReactionsCell.isChecked(), true, false));
        contentLayout.addView(enableReactionsCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        TextInfoPrivacyCell infoCell = new TextInfoPrivacyCell(context);
        infoCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
        infoCell.setTopPadding(12);
        infoCell.setBottomPadding(16);
        infoCell.setText(LocaleController.getString(R.string.PinnedEmojisList_Description));
        contentLayout.addView(infoCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        HeaderCell headerCell = new HeaderCell(context);
        headerCell.setText(LocaleController.getString(R.string.PinnedEmojisList));
        headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        headerCell.setTextSize(15);
        headerCell.setTopMargin(14);

        switchLayout = new LinearLayout(context);
        switchLayout.setOrientation(LinearLayout.VERTICAL);

        contentLayout.addView(switchLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        switchLayout.addView(headerCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        editText = new CustomReactionEditText(context, getResourceProvider(), 150) {
            @Override
            protected void onLineCountChanged(int oldLineCount, int newLineCount) {
                if (newLineCount > oldLineCount) {
                    scrollView.smoothScrollBy(0, dp(30));
                }
            }

            @Override
            public boolean onTextContextMenuItem(int id) {
                if (id == R.id.menu_delete || id == android.R.id.cut) {
                    editText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                    grabReactions(true);
                    return false;
                } else if (id == android.R.id.paste || id == android.R.id.copy) {
                    return false;
                }
                return super.onTextContextMenuItem(id);
            }
        };
        editText.setIsGenericEmojisUsage(true);
        editText.setOnFocused(this::showKeyboard);

        switchLayout.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(200);
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        switchLayout.setLayoutTransition(layoutTransition);

        ShadowSectionCell shadow = new ShadowSectionCell(context);
        shadow.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
        switchLayout.addView(shadow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        hideRecentEmojisCell = new TextCheckCell(context);
        hideRecentEmojisCell.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
        hideRecentEmojisCell.setTextAndCheck(LocaleController.getString(R.string.PinnedEmojisList_HideRecent), false, false);
        switchLayout.addView(hideRecentEmojisCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        hideRecentEmojisCell.setOnClickListener(v -> {
            hideRecentEmojis = !hideRecentEmojis;
            hideRecentEmojisCell.setChecked(hideRecentEmojis);
        });

        infoCell = new TextInfoPrivacyCell(context);
        infoCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
        infoCell.setTopPadding(12);
        infoCell.setBottomPadding(70);
        infoCell.setText(LocaleController.getString(R.string.PinnedEmojisList_HideRecent_Desc));
        switchLayout.addView(infoCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        actionButton = new ButtonWithCounterView(context, getResourceProvider());
        actionButton.setText(new SpannableStringBuilder(LocaleController.getString(R.string.PinnedEmojisList_Apply)), false);
        actionButton.setOnClickListener(v -> {
            JSONArray reactionsList = grabReactions();

            OctoConfig.INSTANCE.usePinnedEmojisFeature.updateValue(checked && reactionsList.length() > 0);
            OctoConfig.INSTANCE.hideRecentEmojis.updateValue(hideRecentEmojis);
            OctoConfig.INSTANCE.pinnedEmojisList.updateValue(reactionsList.toString());
            finishFragment();
        });
        rootLayout.addView(scrollView);
        rootLayout.addView(actionButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM, 13, 13, 13, 13));
        rootLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        bottomDialogLayout = new FrameLayout(context) {
            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                if (emojiKeyboardVisible && changed) {
                    actionButton.setTranslationY(-bottomDialogLayout.getMeasuredHeight());
                    updateScrollViewMarginBottom(bottomDialogLayout.getMeasuredHeight());
                }
            }
        };
        bottomDialogLayout.setVisibility(View.INVISIBLE);
        rootLayout.addView(bottomDialogLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM));

        try {
            String currentList = OctoConfig.INSTANCE.pinnedEmojisList.getValue();
            JSONArray object = new JSONArray(new JSONTokener(currentList));

            int successHandled = 0;
            for (int i = 0; i < object.length(); i++) {
                try {
                    JSONObject jsonObject = object.getJSONObject(i);
                    if (jsonObject.has("emoticon")) {
                        String emoji = jsonObject.getString("emoticon");
                        CharSequence localCharSequence = Emoji.replaceEmoji(emoji, editText.getPaint().getFontMetricsInt(), false);
                        Spannable spannable = new SpannableString(localCharSequence);
                        spannable.setSpan(emoji, 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        editText.append(spannable);
                        selectedEmoticonsList.add(emoji);
                        successHandled++;
                    } else if (jsonObject.has("document_id")) {
                        Long documentId = jsonObject.getLong("document_id");
                        SpannableString spannable = new SpannableString("e");
                        AnimatedEmojiSpan span = createAnimatedEmojiSpan(null, documentId, editText.getFontMetricsInt());
                        span.cacheType = AnimatedEmojiDrawable.getCacheTypeForEnterView();
                        span.setAdded();
                        span.setForceRemoveAnimations(true);
                        spannable.setSpan(span, 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        selectedCustomEmojisList.add(documentId);
                        editText.append(spannable);
                        successHandled++;
                    }
                } catch (JSONException ignored) { }

                if (successHandled > 25) {
                    break;
                }
            }
        } catch (JSONException ignored) {}

        editText.setSelection(editText.getText().length());
        hideRecentEmojisCell.setChecked(hideRecentEmojis);
        setCheckedEnableReactionCell(checked, false, true);
        editText.addReactionsSpan();

        fragmentView = rootLayout;
        return rootLayout;
    }

    private int getKeyboardHeight() {
        if (keyboardHeight <= 0) {
            if (AndroidUtilities.isTablet()) {
                keyboardHeight = dp(150);
            } else {
                keyboardHeight = MessagesController.getGlobalEmojiSettings().getInt("kbd_height", dp(200));
            }
        }
        if (keyboardHeightLand <= 0) {
            if (AndroidUtilities.isTablet()) {
                keyboardHeightLand = dp(150);
            } else {
                keyboardHeightLand = MessagesController.getGlobalEmojiSettings().getInt("kbd_height_land3", dp(200));
            }
        }
        return (AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y ? keyboardHeightLand : keyboardHeight);
    }

    private void initEmojiView() {
        if (emojiView != null) {
            return;
        }

        BaseFragment fragment = this;
        emojiView = new EmojiView(null, true, false, false, getContext(), false, null, null, true, getResourceProvider(), false);

        emojiView.setDelegate(new EmojiView.EmojiViewDelegate() {
            @Override
            public boolean onBackspace() {
                if (editText.length() == 0) {
                    return false;
                }
                AndroidUtilities.runOnUIThread(() -> {
                    editText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                    grabReactions(true);
                });
                return true;
            }

            @Override
            public void onEmojiSelected(String emoji) {
                if (!checkMaxNumberReached()) {
                    return;
                }

                int i = editText.getSelectionEnd();
                if (i < 0) {
                    i = 0;
                }

                emoji = emoji.replace("\uFE0F", "");

                if (!selectedEmoticonsList.contains(emoji)) {
                    CharSequence localCharSequence = Emoji.replaceEmoji(emoji, editText.getPaint().getFontMetricsInt(), false);
                    Spannable spannable = new SpannableString(localCharSequence);
                    spannable.setSpan(emoji, 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    editText.setText(editText.getText().insert(i, spannable));
                    editText.setSelection(i + spannable.length());
                } else {
                    BulletinFactory.of(fragment)
                            .createSimpleBulletin(R.raw.ic_pin, LocaleController.getString(R.string.PinnedEmojisList_Pinned))
                            .show();
                }
            }

            @Override
            public void onCustomEmojiSelected(long documentId, TLRPC.Document document, String emoticon, boolean isRecent) {
                if (!checkMaxNumberReached()) {
                    return;
                }

                if (!selectedCustomEmojisList.contains(documentId)) {
                    try {
                        int i = editText.getSelectionEnd();
                        if (i < 0) {
                            i = 0;
                        }

                        SpannableString spannable = new SpannableString(emoticon);
                        AnimatedEmojiSpan span = createAnimatedEmojiSpan(document, documentId, editText.getFontMetricsInt());
                        span.cacheType = AnimatedEmojiDrawable.getCacheTypeForEnterView();
                        span.setAdded();
                        span.setForceRemoveAnimations(true);
                        spannable.setSpan(span, 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        selectedCustomEmojisList.add(documentId);
                        editText.setText(editText.getText().insert(i, spannable));
                        editText.setSelection(i + spannable.length());
                    } catch (Exception ignored) { }
                } else {
                    BulletinFactory.of(fragment)
                            .createSimpleBulletin(document, LocaleController.getString(R.string.PinnedEmojisList_Pinned))
                            .show();
                }
            }
        });
        bottomDialogLayout.addView(emojiView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM));

        int newHeight = getKeyboardHeight();

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) emojiView.getLayoutParams();
        if (layoutParams.width != AndroidUtilities.displaySize.x || layoutParams.height != newHeight) {
            layoutParams.width = AndroidUtilities.displaySize.x;
            layoutParams.height = newHeight;
            emojiView.setLayoutParams(layoutParams);
        }
    }

    @Override
    public boolean canBeginSlide() {
        if (checkChangesBeforeExit()) {
            return false;
        }
        return super.canBeginSlide();
    }

    @Override
    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        super.onTransitionAnimationEnd(isOpen, backward);
        if (isOpen && checked) {
            editText.setFocusableInTouchMode(true);
        }
        if (isOpen && !backward) {
            initEmojiView();
            AndroidUtilities.runOnUIThread(() -> NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.startAllHeavyOperations, 512), 200);
        }
    }

    private void setCheckedEnableReactionCell(boolean checked, boolean animated, boolean forced) {
        if (this.checked == checked && !forced) {
            return;
        }

        this.checked = checked;

        enableReactionsCell.setChecked(checked);
        int clr = Theme.getColor(checked ? Theme.key_windowBackgroundChecked : Theme.key_windowBackgroundUnchecked);
        if (animated) {
            if (checked) {
                enableReactionsCell.setBackgroundColorAnimated(true, clr);
            } else {
                enableReactionsCell.setBackgroundColorAnimatedReverse(clr);
            }
        } else {
            enableReactionsCell.setBackgroundColor(clr);
        }

        if (checked) {
            switchLayout.setVisibility(View.VISIBLE);
            actionButton.setVisibility(View.VISIBLE);
            if (animated) {
                actionButton.animate().setListener(null).cancel();
                switchLayout.animate().setListener(null).cancel();
                switchLayout.animate().alpha(1f).setDuration(350).setInterpolator(CubicBezierInterpolator.DEFAULT).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        editText.setFocusableInTouchMode(true);
                    }
                }).start();
                actionButton.animate().alpha(1f).setDuration(350).setInterpolator(CubicBezierInterpolator.DEFAULT).start();
            }
        } else {
            if (animated) {
                closeKeyboard();
                actionButton.animate().setListener(null).cancel();
                switchLayout.animate().setListener(null).cancel();
                actionButton.animate().alpha(0f).setDuration(350).setInterpolator(CubicBezierInterpolator.DEFAULT).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        actionButton.setVisibility(View.INVISIBLE);
                    }
                }).start();
                switchLayout.animate().alpha(0f).setDuration(350).setInterpolator(CubicBezierInterpolator.DEFAULT).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        editText.setFocusableInTouchMode(false);
                        switchLayout.setVisibility(View.INVISIBLE);
                    }
                }).start();
            } else {
                switchLayout.setVisibility(View.INVISIBLE);
                actionButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    private boolean checkMaxNumberReached() {
        if (grabReactions().length() > 25) {
            BulletinFactory.of(this)
                    .createSimpleBulletin(R.raw.chats_infotip, LocaleController.getString(R.string.PinnedEmojisList_Limit))
                    .show();
            return false;
        }

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (fragment != null) {
            fragment.notifyItemChanged(PreferenceType.TEXT_ICON.getAdapterType());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isPaused) {
            isPaused = false;
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            if (emojiKeyboardVisible) {
                editText.removeReactionsSpan(false);
                AndroidUtilities.runOnUIThread(() -> editText.requestFocus(), 250);
            }
        }
    }

    @Override
    public void onPause() {
        isPaused = true;
        editText.setFocusable(false);
        super.onPause();
    }

    @Override
    public boolean onBackPressed() {
        if (closeKeyboard()) {
            return false;
        }
        if (checkChangesBeforeExit()) {
            return false;
        }
        return super.onBackPressed();
    }

    private boolean checkChangesBeforeExit() {
        if (checked != OctoConfig.INSTANCE.usePinnedEmojisFeature.getValue()) {
            OctoConfig.INSTANCE.usePinnedEmojisFeature.updateValue(checked && grabReactions().length() > 0);
        }
        boolean hasChanges = hideRecentEmojis != OctoConfig.INSTANCE.hideRecentEmojis.getValue() || checkChangesInList();

        if (hasChanges) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), getResourceProvider());
            builder.setTitle(LocaleController.getString(R.string.UnsavedChanges));
            String text = LocaleController.getString(R.string.ReactionApplyChangesDialog);
            builder.setMessage(text);
            builder.setPositiveButton(LocaleController.getString(R.string.ApplyTheme), (dialogInterface, i) -> actionButton.performClick());
            builder.setNegativeButton(getString(R.string.Discard), (dialogInterface, i) -> finishFragment());

            AlertDialog dialog = builder.show();
            TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(getThemedColor(Theme.key_text_RedBold));
            }
        }
        return hasChanges;
    }

    private boolean checkChangesInList() {
        ArrayList<String> savedEmoticons = new ArrayList<>();
        ArrayList<Long> savedDocumentIds = new ArrayList<>();
        try {
            String currentList = OctoConfig.INSTANCE.pinnedEmojisList.getValue();
            JSONArray object = new JSONArray(new JSONTokener(currentList));

            for (int i = 0; i < object.length(); i++) {
                try {
                    JSONObject jsonObject = object.getJSONObject(i);
                    if (jsonObject.has("emoticon")) {
                        savedEmoticons.add(jsonObject.getString("emoticon").replace("\uFE0F", ""));
                    } else if (jsonObject.has("document_id")) {
                        savedDocumentIds.add(jsonObject.getLong("document_id"));
                    }
                } catch (JSONException ignored) {}

                if (i > 25) {
                    break;
                }
            }
        } catch (JSONException ignored) {}

        JSONArray currentReactions = grabReactions();
        ArrayList<String> currentEmoticons = new ArrayList<>();
        ArrayList<Long> currentDocumentIds = new ArrayList<>();
        for (int i = 0; i < currentReactions.length(); i++) {
            try {
                JSONObject object = currentReactions.getJSONObject(i);
                if (object.has("document_id")) {
                    currentDocumentIds.add(object.getLong("document_id"));
                } else if (object.has("emoticon")) {
                    currentEmoticons.add(object.getString("emoticon").replace("\uFE0F", ""));
                }
            } catch (JSONException ignored) {}
        }

        if (currentEmoticons.size() != savedEmoticons.size() || currentDocumentIds.size() != savedDocumentIds.size()) {
            return true;
        }

        for (String emoticon : currentEmoticons) {
            if (!savedEmoticons.contains(emoticon)) {
                return true;
            }
        }

        for (String emoticon : savedEmoticons) {
            if (!currentEmoticons.contains(emoticon)) {
                return true;
            }
        }

        for (Long documentId : savedDocumentIds) {
            if (!currentDocumentIds.contains(documentId)) {
                return true;
            }
        }

        for (Long documentId : currentDocumentIds) {
            if (!savedDocumentIds.contains(documentId)) {
                return true;
            }
        }

        return false;
    }

    private JSONArray grabReactions() {
        return grabReactions(false);
    }

    private JSONArray grabReactions(boolean justReload) {
        JSONArray jsonArray = justReload ? null : new JSONArray();

        selectedEmoticonsList.clear();
        selectedCustomEmojisList.clear();

        SpannableStringBuilder spanned = new SpannableStringBuilder(editText.getText());
        Object[] spans = spanned.getSpans(0, spanned.length(), Object.class);
        for (Object span : spans) {
            JSONObject object = new JSONObject();

            if (span instanceof AnimatedEmojiSpan span2) {
                try {
                    if (!justReload) {
                        object.put("document_id", span2.documentId);
                    }
                    selectedCustomEmojisList.add(span2.documentId);
                } catch (JSONException ignored) { }
            } else if (span instanceof CharSequence span2) {
                try {
                    if (!justReload) {
                        object.put("emoticon", span2.toString().replace("\uFE0F", ""));
                    }

                    selectedEmoticonsList.add(span2.toString());
                } catch (JSONException ignored) { }
            }

            if (!justReload && (object.has("document_id") || object.has("emoticon"))) {
                jsonArray.put(object);
            }
        }

        return justReload ? null : jsonArray;
    }

    private void showKeyboard() {
        if (!emojiKeyboardVisible) {
            emojiKeyboardVisible = true;
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.stopAllHeavyOperations, 512);
            updateScrollViewMarginBottom(bottomDialogLayout.getMeasuredHeight());
            bottomDialogLayout.setVisibility(View.VISIBLE);
            bottomDialogLayout.setTranslationY(bottomDialogLayout.getMeasuredHeight());
            bottomDialogLayout.animate().setListener(null).cancel();
            bottomDialogLayout.animate().translationY(0).withLayer().setDuration(350).setInterpolator(CubicBezierInterpolator.DEFAULT).setUpdateListener(animation -> actionButton.setTranslationY(-(float) animation.getAnimatedValue() * bottomDialogLayout.getMeasuredHeight())).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.startAllHeavyOperations, 512);
                }
            }).start();
        }
    }

    private boolean closeKeyboard() {
        if (emojiKeyboardVisible) {
            emojiKeyboardVisible = false;
            if (isClearFocusNotWorking()) {
                switchLayout.setFocusableInTouchMode(true);
                switchLayout.requestFocus();
            } else {
                editText.clearFocus();
            }
            updateScrollViewMarginBottom(0);
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.stopAllHeavyOperations, 512);
            bottomDialogLayout.animate().setListener(null).cancel();
            bottomDialogLayout.animate().translationY(bottomDialogLayout.getMeasuredHeight()).setDuration(350).withLayer().setInterpolator(CubicBezierInterpolator.DEFAULT).setUpdateListener(animation -> actionButton.setTranslationY(-(1f - (float) animation.getAnimatedValue()) * bottomDialogLayout.getMeasuredHeight())).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.startAllHeavyOperations, 512);
                    bottomDialogLayout.setVisibility(View.INVISIBLE);
                    if (isClearFocusNotWorking()) {
                        switchLayout.setFocusableInTouchMode(false);
                    }
                }
            }).start();
            return true;
        }
        return false;
    }

    private boolean isClearFocusNotWorking() {
        return Build.MODEL.toLowerCase().startsWith("zte") && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P;
    }

    private void updateScrollViewMarginBottom(int margin) {
        ViewGroup.MarginLayoutParams marginLayoutParams = ((ViewGroup.MarginLayoutParams) scrollView.getLayoutParams());
        marginLayoutParams.bottomMargin = margin;
        scrollView.setLayoutParams(marginLayoutParams);
    }

    public static String getRowDescription() {
        if (!OctoConfig.INSTANCE.usePinnedEmojisFeature.getValue()) {
            return LocaleController.getString(R.string.PasswordOff);
        }

        try {
            JSONArray jsonArray = new JSONArray(new JSONTokener(OctoConfig.INSTANCE.pinnedEmojisList.getValue()));
            return jsonArray.length() > 0 ? (""+jsonArray.length()) : LocaleController.getString(R.string.PasswordOff);
        } catch (JSONException ignored) {}

        return LocaleController.getString(R.string.PasswordOff);
    }
}
