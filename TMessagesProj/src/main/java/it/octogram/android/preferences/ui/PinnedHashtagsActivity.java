package it.octogram.android.preferences.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AdjustPanLayoutHelper;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Adapters.SearchAdapterHelper;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ListView.AdapterWithDiffUtils;
import org.telegram.ui.Components.ProgressButton;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.fragment.PreferencesFragment;


/** @noinspection SequencedCollectionMethodCanBeUsed*/
public class PinnedHashtagsActivity extends BaseFragment implements View.OnClickListener {

    private TextCheckCell enableHashtagsCell;
    private LinearLayout switchLayout;
    private EditTextBoldCursor editText;
    private ButtonWithCounterView actionButton;
    private HashtagSpan currentDeletingSpan;

    private int containerHeight;

    private final ArrayList<HashtagSpan> allSpans = new ArrayList<>();
    private final HashMap<String, HashtagSpan> selectedHashtags = new HashMap<>();
    private ArrayList<SearchAdapterHelper.HashtagObject> recentHashtags = new ArrayList<>();

    private boolean checked = false;

    private PreferencesFragment fragment;
    private SpansContainer spansContainer;

    private ListAdapter recentHashtagsAdapter;
    private final ArrayList<SingleHashtagProperty> oldRecentItems = new ArrayList<>();
    private final ArrayList<SingleHashtagProperty> currentShownRecentItems = new ArrayList<>();

    public void setFragment(PreferencesFragment fragment) {
        this.fragment = fragment;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View createView(Context context) {
        checked = OctoConfig.INSTANCE.usePinnedHashtagsFeature.getValue();

        actionBar.setTitle(getString(R.string.PinnedHashtags));
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

        actionBar.setLongClickable(true);
        actionBar.setOnLongClickListener(v -> {
            String link = "tg://pinned_hashtags";
            showDialog(new ShareAlert(context, null, link, false, link, false, true));

            return true;
        });

        NestedScrollView scrollView = new NestedScrollView(context);
        scrollView.setFillViewport(true);
        FrameLayout rootLayout = new FrameLayout(context) {
            final AdjustPanLayoutHelper adjustPanLayoutHelper = new AdjustPanLayoutHelper(this) {
                @Override
                protected boolean applyTranslation() {
                    return false;
                }
                @Override
                protected boolean heightAnimationEnabled() {
                    return !AndroidUtilities.isInMultiwindow;
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

        enableHashtagsCell = new TextCheckCell(context);
        enableHashtagsCell.setHeight(56);
        enableHashtagsCell.setBackgroundColor(Theme.getColor(enableHashtagsCell.isChecked() ? Theme.key_windowBackgroundChecked : Theme.key_windowBackgroundUnchecked));
        enableHashtagsCell.setTypeface(AndroidUtilities.bold());
        enableHashtagsCell.setTextAndCheck(getString(R.string.PinnedHashtags_Status), false, false);
        enableHashtagsCell.setColors(Theme.key_windowBackgroundCheckText, Theme.key_switchTrackBlue, Theme.key_switchTrackBlueChecked, Theme.key_switchTrackBlueThumb, Theme.key_switchTrackBlueThumbChecked);
        enableHashtagsCell.setOnClickListener(v -> setCheckedEnableHashtagsCell(!enableHashtagsCell.isChecked(), true, false));
        contentLayout.addView(enableHashtagsCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        TextInfoPrivacyCell infoCell = new TextInfoPrivacyCell(context);
        infoCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
        infoCell.setTopPadding(12);
        infoCell.setBottomPadding(16);
        infoCell.setText(getString(R.string.PinnedHashtags_Description));
        contentLayout.addView(infoCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        HeaderCell headerCell = new HeaderCell(context);
        headerCell.setText(getString(R.string.PinnedHashtags));
        headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        headerCell.setTextSize(15);
        headerCell.setTopMargin(14);

        switchLayout = new LinearLayout(context);
        switchLayout.setOrientation(LinearLayout.VERTICAL);

        contentLayout.addView(switchLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        switchLayout.addView(headerCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        spansContainer = new SpansContainer(context);
        spansContainer.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        switchLayout.addView(spansContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        spansContainer.setOnClickListener(v -> {
            editText.clearFocus();
            editText.requestFocus();
            AndroidUtilities.showKeyboard(editText);
        });

        editText = new EditTextBoldCursor(context) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (currentDeletingSpan != null) {
                    currentDeletingSpan.cancelDeleteAnimation();
                    currentDeletingSpan = null;
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!AndroidUtilities.showKeyboard(this)) {
                        clearFocus();
                        requestFocus();
                    }
                }
                return super.onTouchEvent(event);
            }
        };
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        editText.setHintColor(Theme.getColor(Theme.key_groupcreate_hintText));
        editText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        editText.setCursorColor(Theme.getColor(Theme.key_groupcreate_cursor));
        editText.setCursorWidth(1.5f);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_FILTER | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setSingleLine(true);
        editText.setBackground(null);
        editText.setVerticalScrollBarEnabled(false);
        editText.setHorizontalScrollBarEnabled(false);
        editText.setTextIsSelectable(false);
        editText.setPadding(0, 0, 0, 0);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        editText.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        spansContainer.addView(editText);
        editText.setHintText(getString(R.string.PinnedHashtags_Input));

        editText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {

            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE && !editText.getText().toString().trim().isEmpty()) {
                addHashtag(formatHashtag(editText.getText().toString()));
                return true;
            }

            return false;
        });
        editText.setOnKeyListener(new View.OnKeyListener() {

            private boolean wasEmpty;

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        wasEmpty = editText.length() == 0;
                    } else if (event.getAction() == KeyEvent.ACTION_UP && wasEmpty && !allSpans.isEmpty()) {
                        HashtagSpan span = allSpans.get(allSpans.size() - 1);
                        spansContainer.removeSpan(span);
                        updateRecentHashtags();
                        return true;
                    }
                }
                return false;
            }
        });
        InputFilter filter = (source, start, end, dest, dStart, dEnd) -> {
            for (int i = start; i < end; i++) {
                if (Character.isWhitespace(source.charAt(i)) || source.charAt(i) == '#') {
                    return "";
                }
            }
            return null;
        };
        editText.setFilters(new InputFilter[] { filter, new InputFilter.LengthFilter(25) });

        infoCell = new TextInfoPrivacyCell(context);
        infoCell.setTopPadding(12);
        infoCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
        infoCell.setText(getString(R.string.PinnedHashtags_Input_Desc));
        switchLayout.addView(infoCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));


        RecyclerListView listView = new RecyclerListView(context) {
            @Override
            public void requestLayout() {
                super.requestLayout();
            }
        };
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.setTag(14);
        listView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(recentHashtagsAdapter = new ListAdapter(context));
        listView.setVerticalScrollBarEnabled(false);
        listView.setClipToPadding(true);
        listView.setNestedScrollingEnabled(false);
        listView.setHasFixedSize(false);
        listView.setPadding(0, 0, 0, dp(48+13+13));
        listView.setGlowColor(Theme.getColor(Theme.key_dialogScrollGlow));
        switchLayout.addView(listView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(200);
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        switchLayout.setLayoutTransition(layoutTransition);
        switchLayout.setNestedScrollingEnabled(false);

        actionButton = new ButtonWithCounterView(context, getResourceProvider());
        actionButton.setText(new SpannableStringBuilder(getString(R.string.PinnedHashtags_Apply)), false);
        actionButton.setOnClickListener(v -> {
            JSONArray hashtagsList = grabHashtags();

            if (!editText.getText().toString().trim().isEmpty()) {
                String hashtag = formatHashtag(editText.getText().toString());

                if (!selectedHashtags.containsKey(hashtag) && checkMaxNumberReached(false)) {
                    hashtagsList.put(hashtag);
                }

                editText.setText(null);
            }

            OctoConfig.INSTANCE.usePinnedHashtagsFeature.updateValue(checked && hashtagsList.length() > 0);
            OctoConfig.INSTANCE.pinnedHashtagsList.updateValue(hashtagsList.toString());
            finishFragment();
        });
        FrameLayout buttonContainer = new FrameLayout(context);
        buttonContainer.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        buttonContainer.addView(actionButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.FILL, 13, 13, 13, 13));

        rootLayout.addView(scrollView);
        rootLayout.addView(buttonContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48+13+13, Gravity.BOTTOM));
        rootLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        try {
            String currentList = OctoConfig.INSTANCE.pinnedHashtagsList.getValue();
            JSONArray object = new JSONArray(new JSONTokener(currentList));

            for (int i = 0; i < object.length(); i++) {
                try {
                    String hashtag = object.getString(i).trim().toLowerCase();

                    HashtagSpan span = new HashtagSpan(editText.getContext(), hashtag, false, getResourceProvider());
                    spansContainer.addSpan(span, false);
                    span.setOnClickListener(PinnedHashtagsActivity.this);
                } catch (JSONException ignored) {}

                if (i > 10) {
                    break;
                }
            }
        } catch (JSONException ignored) {}

        loadRecentHashtags();
        setCheckedEnableHashtagsCell(checked, false, true);

        fragmentView = rootLayout;
        return rootLayout;
    }

    @Override
    public boolean canBeginSlide() {
        if (checkChangesBeforeExit()) {
            return false;
        }
        return super.canBeginSlide();
    }

    private String formatHashtag(String hashtag) {
        return hashtag.trim().toLowerCase().replaceAll(" ", "").replaceAll("#", "");
    }

    private void addHashtag(String hashtag) {
        if (selectedHashtags.containsKey(hashtag)) {
            BulletinFactory.of(this)
                    .createErrorBulletin(getString(R.string.PinnedHashtags_Pinned))
                    .show();
            return;
        }

        if (!checkMaxNumberReached()) {
            return;
        }

        HashtagSpan span = new HashtagSpan(editText.getContext(), hashtag, false, getResourceProvider());
        spansContainer.addSpan(span, true);
        span.setOnClickListener(PinnedHashtagsActivity.this);

        editText.setText(null);
        updateRecentHashtags();
    }

    private void updateRecentHashtags() {
        oldRecentItems.clear();
        oldRecentItems.addAll(currentShownRecentItems);
        currentShownRecentItems.clear();

        if (selectedHashtags.size() >= 10) {
            recentHashtagsAdapter.setItems(oldRecentItems, currentShownRecentItems);
            return;
        }

        currentShownRecentItems.add(new SingleHashtagProperty(SingleHashtagProperty.HEADER));
        for (SearchAdapterHelper.HashtagObject hashtagObject : recentHashtags) {
            if (!selectedHashtags.containsKey(formatHashtag(hashtagObject.hashtag))) {
                SingleHashtagProperty property = new SingleHashtagProperty(SingleHashtagProperty.ITEM);
                property.setHashtag(hashtagObject.hashtag);
                currentShownRecentItems.add(property);
            }
            if (currentShownRecentItems.size() > 5) {
                break;
            }
        }

        if (currentShownRecentItems.size() == 1) { // has only header
            currentShownRecentItems.clear();
        }

        recentHashtagsAdapter.setItems(oldRecentItems, currentShownRecentItems);
    }

    private void loadRecentHashtags() {
        SearchAdapterHelper helper = new SearchAdapterHelper(true);
        helper.setDelegate(new SearchAdapterHelper.SearchAdapterHelperDelegate() {
            @Override
            public void onDataSetChanged(int searchId) {

            }

            @Override
            public void onSetHashtags(ArrayList<SearchAdapterHelper.HashtagObject> arrayList, HashMap<String, SearchAdapterHelper.HashtagObject> hashMap) {
                recentHashtags = arrayList;

                if (!recentHashtags.isEmpty()) {
                    updateRecentHashtags();
                }
            }
        });
        helper.loadRecentHashtags();
    }

    private void setCheckedEnableHashtagsCell(boolean checked, boolean animated, boolean forced) {
        if (this.checked == checked && !forced) {
            return;
        }

        this.checked = checked;

        enableHashtagsCell.setChecked(checked);
        int clr = Theme.getColor(checked ? Theme.key_windowBackgroundChecked : Theme.key_windowBackgroundUnchecked);
        if (animated) {
            if (checked) {
                enableHashtagsCell.setBackgroundColorAnimated(true, clr);
            } else {
                enableHashtagsCell.setBackgroundColorAnimatedReverse(clr);
            }
        } else {
            enableHashtagsCell.setBackgroundColor(clr);
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
                AndroidUtilities.hideKeyboard(editText);
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

    private boolean checkMaxNumberReached(boolean showBulletin) {
        if (selectedHashtags.size() >= 10) {
            if (showBulletin) {
                BulletinFactory.of(this)
                        .createSimpleBulletin(R.raw.chats_infotip, getString(R.string.PinnedHashtags_Limit))
                        .show();
            }
            return false;
        }

        return true;
    }

    private boolean checkMaxNumberReached() {
        return checkMaxNumberReached(true);
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (fragment != null) {
            fragment.notifyItemChanged(PreferenceType.TEXT_ICON.getAdapterType());
        }
    }

    @Override
    public boolean onBackPressed() {
        if (checkChangesBeforeExit()) {
            return false;
        }
        return super.onBackPressed();
    }

    private boolean checkChangesInList() {
        ArrayList<String> currentPinnedHashtags = new ArrayList<>();
        try {
            String currentList = OctoConfig.INSTANCE.pinnedHashtagsList.getValue();
            JSONArray object = new JSONArray(new JSONTokener(currentList));

            for (int i = 0; i < object.length(); i++) {
                try {
                    currentPinnedHashtags.add(object.getString(i));
                } catch (JSONException ignored) {}

                if (i > 10) {
                    break;
                }
            }
        } catch (JSONException ignored) {}

        if (currentPinnedHashtags.size() != selectedHashtags.size()) {
            return true;
        }

        for (HashMap.Entry<String, HashtagSpan> entry : selectedHashtags.entrySet()) {
            if (!currentPinnedHashtags.contains(entry.getKey())) {
                return true;
            }
        }

        return false;
    }

    private boolean checkChangesBeforeExit() {
        if (checked != OctoConfig.INSTANCE.usePinnedHashtagsFeature.getValue()) {
            OctoConfig.INSTANCE.usePinnedHashtagsFeature.updateValue(checked && grabHashtags().length() > 0);
        }

        boolean hasChanges = checkChangesInList();
        if (hasChanges) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), getResourceProvider());
            builder.setTitle(getString(R.string.UnsavedChanges));
            String text = getString(R.string.PinnedHashtags_Apply_Dialog);
            builder.setMessage(text);
            builder.setPositiveButton(getString(R.string.ApplyTheme), (dialogInterface, i) -> actionButton.performClick());
            builder.setNegativeButton(getString(R.string.Discard), (dialogInterface, i) -> finishFragment());

            AlertDialog dialog = builder.show();
            TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(getThemedColor(Theme.key_text_RedBold));
            }
        }
        return hasChanges;
    }

    private JSONArray grabHashtags() {
        JSONArray jsonArray = new JSONArray();
        for (HashMap.Entry<String, HashtagSpan> entry : selectedHashtags.entrySet()) {
            jsonArray.put(entry.getKey());
        }
        return jsonArray;
    }

    @Keep
    public void setContainerHeight(int value) {
        containerHeight = value;
        if (spansContainer != null) {
            spansContainer.requestLayout();
        }
    }

    @Keep
    public int getContainerHeight() {
        return containerHeight;
    }

    @Override
    public void onClick(View v) {
        HashtagSpan span = (HashtagSpan) v;
        if (span.isDeleting()) {
            currentDeletingSpan = null;
            spansContainer.removeSpan(span);
            updateRecentHashtags();
        } else {
            if (currentDeletingSpan != null) {
                currentDeletingSpan.cancelDeleteAnimation();
            }
            currentDeletingSpan = span;
            span.startDeleteAnimation();
        }
    }

    private class SpansContainer extends ViewGroup {

        private AnimatorSet currentAnimation;
        private boolean animationStarted;
        private final ArrayList<Animator> animators = new ArrayList<>();
        private View addingSpan;
        private View removingSpan;

        private boolean ignoreScrollEvent = false;


        public SpansContainer(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int count = getChildCount();
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int maxWidth = width - dp(26);
            int currentLineWidth = 0;
            int y = dp(10);
            int allCurrentLineWidth = 0;
            int allY = dp(10);
            int x;
            for (int a = 0; a < count; a++) {
                View child = getChildAt(a);
                if (!(child instanceof HashtagSpan)) {
                    continue;
                }
                child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(dp(32), MeasureSpec.EXACTLY));
                if (child != removingSpan && currentLineWidth + child.getMeasuredWidth() > maxWidth) {
                    y += child.getMeasuredHeight() + dp(8);
                    currentLineWidth = 0;
                }
                if (allCurrentLineWidth + child.getMeasuredWidth() > maxWidth) {
                    allY += child.getMeasuredHeight() + dp(8);
                    allCurrentLineWidth = 0;
                }
                x = dp(16) + currentLineWidth;
                if (!animationStarted) {
                    if (child == removingSpan) {
                        child.setTranslationX(dp(16) + allCurrentLineWidth);
                        child.setTranslationY(allY);
                    } else if (removingSpan != null) {
                        if (child.getTranslationX() != x) {
                            animators.add(ObjectAnimator.ofFloat(child, View.TRANSLATION_X, x));
                        }
                        if (child.getTranslationY() != y) {
                            animators.add(ObjectAnimator.ofFloat(child, View.TRANSLATION_Y, y));
                        }
                    } else {
                        child.setTranslationX(x);
                        child.setTranslationY(y);
                    }
                }
                if (child != removingSpan) {
                    currentLineWidth += child.getMeasuredWidth() + dp(9);
                }
                allCurrentLineWidth += child.getMeasuredWidth() + dp(9);
            }
            int minWidth;
            if (AndroidUtilities.isTablet()) {
                minWidth = dp(530 - 26 - 18 - 57 * 2) / 3;
            } else {
                minWidth = (Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - dp(26 + 18 + 57 * 2)) / 3;
            }
            if (maxWidth - currentLineWidth < minWidth) {
                currentLineWidth = 0;
                y += dp(32 + 8);
            }
            if (maxWidth - allCurrentLineWidth < minWidth) {
                allY += dp(32 + 8);
            }
            editText.measure(MeasureSpec.makeMeasureSpec(maxWidth - currentLineWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(dp(32), MeasureSpec.EXACTLY));
            if (!animationStarted) {
                int currentHeight = allY + dp(32 + 10);
                int fieldX = currentLineWidth + dp(23);
                if (currentAnimation != null) {
                    int resultHeight = y + dp(32 + 10);
                    if (containerHeight != resultHeight) {
                        animators.add(ObjectAnimator.ofInt(PinnedHashtagsActivity.this, "containerHeight", resultHeight));
                    }
                    if (editText.getTranslationX() != fieldX) {
                        animators.add(ObjectAnimator.ofFloat(editText, View.TRANSLATION_X, fieldX));
                    }
                    if (editText.getTranslationY() != y) {
                        animators.add(ObjectAnimator.ofFloat(editText, View.TRANSLATION_Y, y));
                    }
                    editText.setAllowDrawCursor(false);
                    currentAnimation.playTogether(animators);
                    currentAnimation.start();
                    animationStarted = true;
                } else {
                    containerHeight = currentHeight;
                    editText.setTranslationX(fieldX);
                    editText.setTranslationY(y);
                }
            } else if (currentAnimation != null) {
                if (!ignoreScrollEvent && removingSpan == null) {
                    editText.bringPointIntoView(editText.getSelectionStart());
                }
            }
            setMeasuredDimension(width, containerHeight);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            int count = getChildCount();
            for (int a = 0; a < count; a++) {
                View child = getChildAt(a);
                child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
            }
        }

        public void addSpan(final HashtagSpan span, boolean animated) {
            allSpans.add(span);
            String uid = span.getUid();
            selectedHashtags.put(uid, span);

            editText.setHintVisible(false, TextUtils.isEmpty(editText.getText()));
            if (currentAnimation != null && currentAnimation.isRunning()) {
                currentAnimation.setupEndValues();
                currentAnimation.cancel();
            }
            animationStarted = false;
            if (animated) {
                currentAnimation = new AnimatorSet();
                currentAnimation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        addingSpan = null;
                        currentAnimation = null;
                        animationStarted = false;
                        editText.setAllowDrawCursor(true);
                    }
                });
                currentAnimation.setDuration(150);
                addingSpan = span;
                animators.clear();
                animators.add(ObjectAnimator.ofFloat(addingSpan, View.SCALE_X, 0.01f, 1.0f));
                animators.add(ObjectAnimator.ofFloat(addingSpan, View.SCALE_Y, 0.01f, 1.0f));
                animators.add(ObjectAnimator.ofFloat(addingSpan, View.ALPHA, 0.0f, 1.0f));
            }
            addView(span);
        }

        public void removeSpan(HashtagSpan span) {
            ignoreScrollEvent = true;
            String uid = span.getUid();
            selectedHashtags.remove(uid);
            allSpans.remove(span);
            span.setOnClickListener(null);

            if (currentAnimation != null) {
                removeView(removingSpan);
                currentAnimation.cancel();
            }
            animationStarted = false;
            currentAnimation = new AnimatorSet();
            currentAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    removeView(span);
                    removingSpan = null;
                    currentAnimation = null;
                    animationStarted = false;
                    editText.setAllowDrawCursor(true);
                    if (allSpans.isEmpty()) {
                        editText.setHintVisible(true, true);
                    }
                }
            });
            currentAnimation.setDuration(150);
            removingSpan = span;
            animators.clear();
            animators.add(ObjectAnimator.ofFloat(span, View.SCALE_X, 1.0f, 0.01f));
            animators.add(ObjectAnimator.ofFloat(span, View.SCALE_Y, 1.0f, 0.01f));
            animators.add(ObjectAnimator.ofFloat(span, View.ALPHA, 1.0f, 0.0f));
            requestLayout();
        }
    }

    public static class HashtagSpan extends View {

        private final String uid;
        private static final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private static final Paint backPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Drawable deleteDrawable;
        private final RectF rect = new RectF();
        private final ImageReceiver imageReceiver;
        private final StaticLayout nameLayout;
        private final AvatarDrawable avatarDrawable;
        private int textWidth;
        private float textX;
        private float progress;
        private boolean deleting;
        private long lastUpdateTime;
        private final int[] colors = new int[8];
        private final Theme.ResourcesProvider resourcesProvider;
        private final boolean small;

        public HashtagSpan(Context context) {
            this(context, null, false, null);
        }

        public HashtagSpan(Context context, String hashtag, boolean small, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            this.small = small;

            deleteDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.delete, context.getTheme());
            textPaint.setTextSize(dp(small ? 13 : 14));

            avatarDrawable = new AvatarDrawable();
            avatarDrawable.setTextSize(dp(20));
            avatarDrawable.setAvatarType(AvatarDrawable.AVATAR_TYPE_HASHTAGS);
            uid = hashtag;

            imageReceiver = new ImageReceiver();
            imageReceiver.setRoundRadius(dp(16));
            imageReceiver.setParentView(this);
            imageReceiver.setImageCoords(0, 0, dp(small ? 28 : 32), dp(small ? 28 : 32));
            imageReceiver.setImage(null, "50_50", avatarDrawable, 0, null, null, 1);

            int maxNameWidth;
            if (AndroidUtilities.isTablet()) {
                maxNameWidth = dp(530 - (small ? 28 : 32) - 18 - 57 * 2) / 2;
            } else {
                maxNameWidth = (Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - dp((small ? 28 : 32) + 18 + 57 * 2)) / 2;
            }

            hashtag = hashtag.replace('\n', ' ');
            CharSequence name = hashtag;
            name = Emoji.replaceEmoji(name, textPaint.getFontMetricsInt(), false);
            name = TextUtils.ellipsize(name, textPaint, maxNameWidth, TextUtils.TruncateAt.END);
            nameLayout = new StaticLayout(name, textPaint, 1000, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            if (nameLayout.getLineCount() > 0) {
                textWidth = (int) Math.ceil(nameLayout.getLineWidth(0));
                textX = -nameLayout.getLineLeft(0);
            }
            updateColors();

            NotificationCenter.listenEmojiLoading(this);
        }

        public void updateColors() {
            int color = avatarDrawable.getColor();
            int back = Theme.getColor(Theme.key_groupcreate_spanBackground, resourcesProvider);
            int delete = Theme.getColor(Theme.key_groupcreate_spanDelete, resourcesProvider);
            colors[0] = Color.red(back);
            colors[1] = Color.red(color);
            colors[2] = Color.green(back);
            colors[3] = Color.green(color);
            colors[4] = Color.blue(back);
            colors[5] = Color.blue(color);
            colors[6] = Color.alpha(back);
            colors[7] = Color.alpha(color);
            deleteDrawable.setColorFilter(new PorterDuffColorFilter(delete, PorterDuff.Mode.MULTIPLY));
            backPaint.setColor(back);
        }

        public boolean isDeleting() {
            return deleting;
        }

        public void startDeleteAnimation() {
            if (deleting) {
                return;
            }
            deleting = true;
            lastUpdateTime = System.currentTimeMillis();
            invalidate();
        }

        public void cancelDeleteAnimation() {
            if (!deleting) {
                return;
            }
            deleting = false;
            lastUpdateTime = System.currentTimeMillis();
            invalidate();
        }

        public String getUid() {
            return uid;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(dp((small ? 28 - 8 : 32) + 25) + textWidth, dp(small ? 28 : 32));
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            if (deleting && progress != 1.0f || !deleting && progress != 0.0f) {
                long newTime = System.currentTimeMillis();
                long dt = newTime - lastUpdateTime;
                if (dt < 0 || dt > 17) {
                    dt = 17;
                }
                if (deleting) {
                    progress += dt / 120.0f;
                    if (progress >= 1.0f) {
                        progress = 1.0f;
                    }
                } else {
                    progress -= dt / 120.0f;
                    if (progress < 0.0f) {
                        progress = 0.0f;
                    }
                }
                invalidate();
            }
            canvas.save();
            rect.set(0, 0, getMeasuredWidth(), dp(small ? 28 : 32));
            backPaint.setColor(Color.argb(colors[6] + (int) ((colors[7] - colors[6]) * progress), colors[0] + (int) ((colors[1] - colors[0]) * progress), colors[2] + (int) ((colors[3] - colors[2]) * progress), colors[4] + (int) ((colors[5] - colors[4]) * progress)));
            canvas.drawRoundRect(rect, dp(small ? 14 : 16), dp(small ? 14 : 16), backPaint);
            if (progress != 1f) {
                imageReceiver.draw(canvas);
            }
            if (progress != 0) {
                int color = avatarDrawable.getColor();
                float alpha = Color.alpha(color) / 255.0f;
                backPaint.setColor(color);
                backPaint.setAlpha((int) (255 * progress * alpha));
                canvas.drawCircle(dp(small ? 14 : 16), dp(small ? 14 : 16), dp(small ? 14 : 16), backPaint);
                canvas.save();
                canvas.rotate(45 * (1.0f - progress), dp(16), dp(16));
                deleteDrawable.setBounds(dp(small ? 9 : 11), dp(small ? 9 : 11), dp(small ? 19 : 21), dp(small ? 19 : 21));
                deleteDrawable.setAlpha((int) (255 * progress));
                deleteDrawable.draw(canvas);
                canvas.restore();
            }
            canvas.translate(textX + dp((small ? 26 : 32) + 9), dp(small ? 6 : 8));
            int text = Theme.getColor(Theme.key_groupcreate_spanText, resourcesProvider);
            int textSelected = Theme.getColor(Theme.key_avatar_text, resourcesProvider);
            textPaint.setColor(ColorUtils.blendARGB(text, textSelected, progress));

            nameLayout.draw(canvas);
            canvas.restore();
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            info.setText(nameLayout.getText());
            if (isDeleting())
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK.getId(), getString(R.string.Delete)));
        }
    }

    private class ListAdapter extends AdapterWithDiffUtils {

        private final Context context;

        public ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return false;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == SingleHashtagProperty.HEADER) {
                HeaderCell recentHashtagsHeaderCell = new HeaderCell(context);
                recentHashtagsHeaderCell.setText(getString(R.string.PinnedHashtags_Recent));
                recentHashtagsHeaderCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                recentHashtagsHeaderCell.setTextSize(15);
                recentHashtagsHeaderCell.setTopMargin(14);
                return new RecyclerListView.Holder(recentHashtagsHeaderCell);
            }
            return new RecyclerListView.Holder(new SuggestedHashtagCell(context));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (position < currentShownRecentItems.size()) {
                SingleHashtagProperty property = currentShownRecentItems.get(position);
                if (property != null && property.viewType != SingleHashtagProperty.HEADER) {
                    ((SuggestedHashtagCell) holder.itemView).setHashtag(property.getHashtag(), position != currentShownRecentItems.size() - 1);
                }
            }
        }

        @Override
        public int getItemCount() {
            return currentShownRecentItems.size();
        }

        @Override
        public int getItemViewType(int position) {
            return currentShownRecentItems.get(position).viewType;
        }
    }

    private class SuggestedHashtagCell extends FrameLayout {
        private String hashtag = "";
        private final TextView textView;
        private final ProgressButton addButton;
        private boolean needDivider;

        public SuggestedHashtagCell(Context context) {
            super(context);

            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            textView = new TextView(context);
            textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setLines(1);
            textView.setMaxLines(1);
            textView.setSingleLine(true);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), 22, 0, 22, 0));

            addButton = new ProgressButton(context);
            addButton.setText(getString(R.string.Add));
            addButton.setOnClickListener(v -> {
                if (!hashtag.isEmpty()) {
                    addHashtag(formatHashtag(hashtag));
                }
            });
            addButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
            addButton.setProgressColor(Theme.getColor(Theme.key_featuredStickers_buttonProgress));
            addButton.setBackgroundRoundRect(Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed));
            addView(addButton, LayoutHelper.createFrameRelatively(LayoutHelper.WRAP_CONTENT, 28, Gravity.CENTER_VERTICAL | Gravity.END, 0, 0, 14, 0));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), dp(50));
            measureChildWithMargins(addButton, widthMeasureSpec, 0, heightMeasureSpec, 0);
            measureChildWithMargins(textView, widthMeasureSpec, addButton.getMeasuredWidth(), heightMeasureSpec, 0);
        }

        public void setHashtag(String text, boolean divider) {
            needDivider = divider;
            hashtag = text;
            setWillNotDraw(!needDivider);

            textView.setText(text);
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            if (needDivider && !OctoConfig.INSTANCE.disableDividers.getValue()) {
                canvas.drawLine(0, getHeight() - 1, getWidth() - getPaddingRight(), getHeight() - 1, Theme.dividerPaint);
            }
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            info.setEnabled(true);
            info.setText(addButton.getText());
            info.setClassName("android.widget.Button");
        }
    }

    private static class SingleHashtagProperty extends AdapterWithDiffUtils.Item {
        public String hashtag;
        public static int ITEM = 0;
        public static int HEADER = 1;

        public SingleHashtagProperty(int viewType) {
            super(viewType, true);
        }

        public void setHashtag(String hashtag) {
            this.hashtag = hashtag;
        }

        public String getHashtag() {
            return hashtag;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof SingleHashtagProperty item)) {
                return false;
            }
            if (item.viewType == HEADER) {
                return true;
            }
            if (!Objects.equals(viewType, item.viewType)) {
                return false;
            }
            return Objects.equals(getHashtag(), item.getHashtag());
        }
    }

    public static String getRowDescription() {
        if (!OctoConfig.INSTANCE.usePinnedHashtagsFeature.getValue()) {
            return getString(R.string.PasswordOff);
        }

        try {
            JSONArray jsonArray = new JSONArray(new JSONTokener(OctoConfig.INSTANCE.pinnedHashtagsList.getValue()));
            return jsonArray.length() > 0 ? (""+jsonArray.length()) : getString(R.string.PasswordOff);
        } catch (JSONException ignored) {}

        return getString(R.string.PasswordOff);
    }
}
