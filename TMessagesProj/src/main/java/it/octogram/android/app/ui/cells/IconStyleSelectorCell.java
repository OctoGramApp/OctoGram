/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */
package it.octogram.android.app.ui.cells;


import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.ExtendedGridLayoutManager;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.ai.icons.AiFeatureIcons;
import it.octogram.android.utils.appearance.FolderIconController;

@SuppressLint("ClickableViewAccessibility")
public class IconStyleSelectorCell extends BottomSheet {

    private OnItemClickListener onItemClickListener;
    private boolean isCustomModelSelector = false;

    public void setOnItemClick(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(String emoticon);
    }

    private final LinearLayout linearLayout;
    private final NestedScrollView scrollView;
    private final int[] location = new int[2];
    private final View shadow;
    private int scrollOffsetY;
    private AnimatorSet shadowAnimation;

    @SuppressLint("NotifyDataSetChanged")
    public IconStyleSelectorCell(Context context) {
        this(context, false);
    }

    @SuppressLint("NotifyDataSetChanged")
    public IconStyleSelectorCell(Context context, boolean isCustomModelSelector) {
        super(context, false);
        setCanceledOnTouchOutside(false);
        setApplyTopPadding(false);
        setApplyBottomPadding(false);

        this.isCustomModelSelector = isCustomModelSelector;
        int colorBackground = Theme.getColor(Theme.key_dialogBackground);

        shadowDrawable = ContextCompat.getDrawable(context, R.drawable.sheet_shadow_round);
        if (shadowDrawable != null) {
            shadowDrawable.mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(colorBackground, PorterDuff.Mode.MULTIPLY));
        }

        var container = getLayoutContainer(context);
        containerView = container;

        scrollView = new NestedScrollView(context) {

            private boolean ignoreLayout;

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int height = MeasureSpec.getSize(heightMeasureSpec);
                measureChildWithMargins(linearLayout, widthMeasureSpec, 0, heightMeasureSpec, 0);
                int contentHeight = linearLayout.getMeasuredHeight();
                int padding = (height / 5 * 2);
                int visiblePart = height - padding;
                if (contentHeight - visiblePart < dp(90) || contentHeight < height / 2 + dp(90)) {
                    padding = height - contentHeight;
                }
                if (padding < 0) {
                    padding = 0;
                }
                if (getPaddingTop() != padding) {
                    ignoreLayout = true;
                    setPadding(0, padding, 0, 0);
                    ignoreLayout = false;
                }
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                updateLayout();
            }

            @Override
            public void requestLayout() {
                if (ignoreLayout) {
                    return;
                }
                super.requestLayout();
            }

            @Override
            protected void onScrollChanged(int l, int t, int old_l, int old_t) {
                super.onScrollChanged(l, t, old_l, old_t);
                updateLayout();
            }
        };
        scrollView.setFillViewport(true);
        scrollView.setWillNotDraw(false);
        scrollView.setClipToPadding(false);
        scrollView.setVerticalScrollBarEnabled(false);
        container.addView(scrollView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP, 0, 0, 0, 0));

        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(linearLayout, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));


        var header = new LinearLayout(context);
        header.setOrientation(LinearLayout.VERTICAL);

        TextView titleView = new TextView(context);
        titleView.setPivotX(LocaleController.isRTL ? titleView.getWidth() : 0);
        titleView.setPivotY(0);
        titleView.setLines(1);
        titleView.setText(getString(R.string.ChooseFolderIcon));
        titleView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        titleView.setTypeface(AndroidUtilities.bold());
        titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp(19));
        header.addView(titleView, LayoutHelper.createLinear(
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.WRAP_CONTENT,
                Gravity.FILL_HORIZONTAL | Gravity.TOP,
                22, 22, 22, 0
        ));

        var gridAdapter = new GridAdapter();
        var recyclerListView = new RecyclerListView(context) {
            @Override
            protected void onMeasure(int widthSpec, int heightSpec) {
                super.onMeasure(widthSpec, heightSpec);
                gridAdapter.notifyDataSetChanged();
                OctoLogging.e("IconStyleSelectorCell", "dstchang");
            }
        };
        recyclerListView.setLayoutParams(new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        recyclerListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerListView.setLayoutManager(new ExtendedGridLayoutManager(recyclerListView.getContext(), isCustomModelSelector ? 9 : 6));
        var padding = dp(10);
        recyclerListView.setPadding(padding, padding, padding, padding);
        recyclerListView.setAdapter(gridAdapter);
        recyclerListView.setSelectorType(3);
        recyclerListView.setSelectorDrawableColor(Theme.getColor(Theme.key_listSelector));
        recyclerListView.setOnItemClickListener((view, position) -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(OctoUtils.safeToString(view.getTag()));
            }
            dismiss();
        });
        header.addView(recyclerListView);

        linearLayout.addView(header);
        var frameLayoutParams = new FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, 70, Gravity.BOTTOM | Gravity.LEFT);
        shadow = new View(context);
        shadow.setBackgroundColor(Theme.getColor(Theme.key_dialogShadowLine));
        shadow.setAlpha(0.0f);
        shadow.setTag(1);
        container.addView(shadow, frameLayoutParams);
    }

    private @NonNull FrameLayout getLayoutContainer(Context context) {
        FrameLayout container = new FrameLayout(context) {
            @Override
            public void setTranslationY(float translationY) {
                super.setTranslationY(translationY);
                updateLayout();
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN && scrollOffsetY != 0 && ev.getY() < scrollOffsetY) {
                    dismiss();
                    return true;
                }
                return super.onInterceptTouchEvent(ev);
            }

            @Override
            public boolean onTouchEvent(MotionEvent e) {
                return !isDismissed() && super.onTouchEvent(e);
            }

            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                int top = (int) (scrollOffsetY - backgroundPaddingTop - getTranslationY());
                if (shadowDrawable != null) {
                    shadowDrawable.setBounds(0, top, getMeasuredWidth(), getMeasuredHeight());
                    shadowDrawable.draw(canvas);
                }
            }
        };
        container.setWillNotDraw(false);
        return container;
    }

    protected void onItemClick(String emoticon) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(emoticon);
        }
    }

    private void runShadowAnimation(final boolean show) {
        if (show && shadow.getTag() != null || !show && shadow.getTag() == null) {
            shadow.setTag(show ? null : 1);
            if (show) {
                shadow.setVisibility(View.VISIBLE);
            }
            if (shadowAnimation != null) {
                shadowAnimation.cancel();
            }
            shadowAnimation = new AnimatorSet();
            shadowAnimation.playTogether(ObjectAnimator.ofFloat(shadow, View.ALPHA, show ? 1.0f : 0.0f));
            shadowAnimation.setDuration(150);
            shadowAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (shadowAnimation != null && shadowAnimation.equals(animation)) {
                        if (!show) {
                            shadow.setVisibility(View.INVISIBLE);
                        }
                        shadowAnimation = null;
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    if (shadowAnimation != null && shadowAnimation.equals(animation)) {
                        shadowAnimation = null;
                    }
                }
            });
            shadowAnimation.start();
        }
    }

    private void updateLayout() {
        View child = linearLayout.getChildAt(0);
        child.getLocationInWindow(location);
        int top = location[1];
        int newOffset = Math.max(top, 0);
        runShadowAnimation(!(location[1] + linearLayout.getMeasuredHeight() <= container.getMeasuredHeight() - dp(113) + containerView.getTranslationY()));
        if (scrollOffsetY != newOffset) {
            scrollOffsetY = newOffset;
            scrollView.invalidate();
        }
    }

    @Override
    protected boolean canDismissWithSwipe() {
        return false;
    }

    private class GridAdapter extends RecyclerListView.SelectionAdapter {
        private final String[] icons = (isCustomModelSelector ? AiFeatureIcons.getAiIcons() : FolderIconController.folderIcons).keySet().toArray(new String[0]);

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            var imageView = new AppCompatImageView(parent.getContext());
            int containerWidth = parent.getMeasuredWidth();
            int iconSize = (int) (containerWidth / (isCustomModelSelector ? 9.0f : 6.0f) * 0.9f);
            imageView.setLayoutParams(new RecyclerView.LayoutParams(iconSize, iconSize));
            imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
            imageView.setPadding(dp(10), dp(10), dp(10), dp(10));
            return new RecyclerListView.Holder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            var imageView = (AppCompatImageView) holder.itemView;
            imageView.setTag(icons[position]);
            imageView.setImageResource(isCustomModelSelector ? AiFeatureIcons.getModelIcon(icons[position]) : FolderIconController.getTabIcon(icons[position]));
        }

        @Override
        public int getItemCount() {
            return icons.length;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }
    }
}
