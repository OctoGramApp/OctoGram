/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import it.octogram.android.app.ui.components.HintHeader;
import it.octogram.android.utils.translator.localhelper.LocalTranslator;

public class OctoChatsTranslatorModelsUI extends BaseFragment {

    private ListAdapter listAdapter;
    private EmptyView emptyView;

    private ArrayList<String> downloadedModels = new ArrayList<>();
    private boolean isLoading = true;

    private int hintRow;
    private int modelsHeaderRow;
    private int modelsStartRow;
    private int modelsEndRow;
    private int rowCount;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRows();
        loadModels();
        return true;
    }

    @Override
    public View createView(Context context) {
        this.fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(getString(R.string.OnDeviceTranslationModels));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        emptyView = new EmptyView(context, null, EmptyView.STATE_LOADING);
        emptyView.setTitle(getString(R.string.OnDeviceTranslationModels));
        emptyView.setTopImage(R.drawable.solar_stickers_empty, 100);
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listAdapter = new ListAdapter(context);
        RecyclerListView listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(listAdapter);
        listView.setEmptyView(emptyView);
        listView.setVerticalScrollBarEnabled(false);

        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        itemAnimator.setDelayAnimations(false);
        listView.setItemAnimator(itemAnimator);

        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        return fragmentView;
    }

    private void updateRows() {
        rowCount = 0;
        hintRow = rowCount++;
        if (!downloadedModels.isEmpty()) {
            modelsHeaderRow = rowCount++;
            modelsStartRow = rowCount;
            rowCount += downloadedModels.size();
            modelsEndRow = rowCount;
        } else {
            modelsHeaderRow = -1;
            modelsStartRow = -1;
            modelsEndRow = -1;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadModels() {
        isLoading = true;
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }

        LocalTranslator.listDownloadedModels(new LocalTranslator.OnModelsListCallback() {
            @Override
            public void onModelsReceived(ArrayList<String> models) {
                AndroidUtilities.runOnUIThread(() -> {
                    isLoading = false;
                    downloadedModels = models;
                    Collections.sort(downloadedModels, (lang1, lang2) -> {
                        String name1 = getLanguageName(lang1);
                        String name2 = getLanguageName(lang2);
                        return name1.compareTo(name2);
                    });

                    if (models.isEmpty()) {
                        emptyView.setState(EmptyView.STATE_EMPTY);
                        emptyView.setText(getString(R.string.NoModelsDownloaded));
                    }

                    updateRows();
                    if (listAdapter != null) {
                        listAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError() {
                AndroidUtilities.runOnUIThread(() -> {
                    isLoading = false;
                    emptyView.setState(EmptyView.STATE_ERROR);
                    emptyView.setText(getString(R.string.ErrorOccurred));
                    updateRows();
                    if (listAdapter != null) {
                        listAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private String getLanguageName(String langCode) {
        if (langCode == null || langCode.isEmpty()) {
            return "Unknown";
        }
        Locale locale = new Locale(langCode);
        String name = locale.getDisplayName(locale);
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private void confirmDelete(String langCode, String langName) {
        if (getParentActivity() == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(getString(R.string.DeleteModelTitle));
        builder.setMessage(AndroidUtilities.replaceTags(String.format(getString(R.string.DeleteModelMessage), langName)));
        builder.setPositiveButton(getString(R.string.Delete), (dialog, which) -> {
            AlertDialog progressDialog = new AlertDialog(getParentActivity(), 3);
            progressDialog.setCanCancel(false);
            progressDialog.show();

            LocalTranslator.deleteDownloadedModel(langCode, (success) ->
                    AndroidUtilities.runOnUIThread(() -> {
                        progressDialog.dismiss();
                        if (success) {
                            int index = downloadedModels.indexOf(langCode);
                            if (index == -1) {
                                return;
                            }

                            downloadedModels.remove(index);
                            int removedPosition = modelsStartRow + index;
                            boolean wasLastItem = downloadedModels.isEmpty();
                            updateRows();

                            listAdapter.notifyItemRemoved(removedPosition);
                            if (wasLastItem) {
                                int headerPosition = removedPosition - 1;
                                listAdapter.notifyItemRangeRemoved(headerPosition, 2);
                            } else {
                                if (index == downloadedModels.size()) {
                                    listAdapter.notifyItemChanged(removedPosition - 1);
                                }
                            }

                            if (wasLastItem) {
                                emptyView.setState(EmptyView.STATE_EMPTY);
                                emptyView.setText(getString(R.string.NoModelsDownloaded));
                            }
                        } else {
                            if (getParentActivity() == null) {
                                return;
                            }
                            AlertDialog.Builder errorBuilder = new AlertDialog.Builder(getParentActivity());
                            errorBuilder.setTitle(getString(R.string.Error));
                            errorBuilder.setMessage(getString(R.string.FailedToDeleteModel));
                            errorBuilder.setPositiveButton(getString(R.string.OK), null);
                            showDialog(errorBuilder.create());
                        }
                    }));
        });
        builder.setNegativeButton(getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    public static class ModelCell extends FrameLayout {

        private TextView textView;
        private ImageView deleteButton;
        private boolean needDivider;

        public ModelCell(Context context) {
            super(context);
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            textView = new TextView(context);
            textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            textView.setTextSize(16);
            textView.setLines(1);
            textView.setMaxLines(1);
            textView.setSingleLine(true);
            textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 21, 0, 60, 0));

            deleteButton = new ImageView(context);
            deleteButton.setFocusable(false);
            deleteButton.setScaleType(ImageView.ScaleType.CENTER);
            deleteButton.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector)));
            Drawable deleteIcon = context.getResources().getDrawable(R.drawable.msg_clear).mutate();
            deleteIcon.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_color_red), PorterDuff.Mode.MULTIPLY));
            deleteButton.setImageDrawable(deleteIcon);
            deleteButton.setContentDescription(LocaleController.getString(R.string.Delete));
            addView(deleteButton, LayoutHelper.createFrame(48, 48, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL, 6, 0, 6, 0));
        }

        public void setText(String text) {
            textView.setText(text);
        }

        public void setDeleteButtonVisible(boolean visible) {
            deleteButton.setVisibility(visible ? VISIBLE : GONE);
        }

        public void setOnDeleteClickListener(OnClickListener listener) {
            deleteButton.setOnClickListener(listener);
        }

        public void setNeedDivider(boolean value) {
            needDivider = value;
            setWillNotDraw(!needDivider);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(50), MeasureSpec.EXACTLY)
            );
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            if (needDivider) {
                canvas.drawLine(LocaleController.isRTL ? 0 : AndroidUtilities.dp(20), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(20) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
            }
        }
    }

    @SuppressLint("ViewConstructor")
    public static class EmptyView extends LinearLayout {

        public final static int STATE_LOADING = 0;
        public final static int STATE_EMPTY = 1;
        public final static int STATE_ERROR = 2;

        private final ImageView topImageView;
        private final TextView titleTextView;
        private final TextView subtitleTextView;
        private final ProgressBar progressBar;

        public EmptyView(Context context, @Nullable View.OnClickListener retryClickListener, int initialState) {
            super(context);
            setOrientation(VERTICAL);
            setGravity(Gravity.CENTER);
            setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16));

            topImageView = new ImageView(context);
            addView(topImageView, LayoutHelper.createLinear(100, 100, Gravity.CENTER_HORIZONTAL));

            titleTextView = new TextView(context);
            titleTextView.setTextSize(1, 20);
            titleTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            titleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            titleTextView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
            addView(titleTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 0));

            subtitleTextView = new TextView(context);
            subtitleTextView.setTextSize(1, 14);
            subtitleTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            subtitleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            subtitleTextView.setPadding(AndroidUtilities.dp(20), 0, AndroidUtilities.dp(20), 0);
            addView(subtitleTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 8, 0, 0));

            progressBar = new ProgressBar(context);
            addView(progressBar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 0));

            if (retryClickListener != null) {
                TextView retryButton = new TextView(context);
                retryButton.setText(LocaleController.getString(R.string.Retry));
                retryButton.setTextSize(1, 15);
                retryButton.setGravity(Gravity.CENTER);
                retryButton.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
                retryButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
                retryButton.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
                retryButton.setPadding(AndroidUtilities.dp(16), 0, AndroidUtilities.dp(16), 0);
                retryButton.setOnClickListener(retryClickListener);
                addView(retryButton, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 36, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 0));
            }

            setState(initialState);
        }

        public void setState(int state) {
            progressBar.setVisibility(state == STATE_LOADING ? VISIBLE : GONE);
            topImageView.setVisibility(state != STATE_LOADING ? VISIBLE : GONE);
            titleTextView.setVisibility(state != STATE_LOADING ? VISIBLE : GONE);
            subtitleTextView.setVisibility(state != STATE_LOADING ? VISIBLE : GONE);
        }

        public void setTitle(String text) {
            titleTextView.setText(text);
        }

        public void setText(String text) {
            subtitleTextView.setText(text);
        }

        public void setTopImage(int resId, int sizeDp) {
            topImageView.setImageResource(resId);
            topImageView.setLayoutParams(LayoutHelper.createLinear(sizeDp, sizeDp, Gravity.CENTER_HORIZONTAL));
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new HintHeader(mContext, getString(R.string.OnDeviceModelsHint));
                    view.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case 1:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 2:
                    view = new ModelCell(mContext);
                    break;
                case 3:
                default:
                    view = new ShadowSectionCell(mContext);
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 1: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    headerCell.setText(getString(R.string.DownloadedLanguages));
                    break;
                }
                case 2: {
                    ModelCell cell = (ModelCell) holder.itemView;
                    String langCode = downloadedModels.get(position - modelsStartRow);
                    String langName = getLanguageName(langCode);
                    cell.setText(langName);
                    boolean deletable = !langCode.equals("en");
                    cell.setDeleteButtonVisible(deletable);
                    cell.setOnDeleteClickListener(deletable ? v -> confirmDelete(langCode, langName) : null);
                    cell.setOnDeleteClickListener(v -> confirmDelete(langCode, langName));
                    boolean needDivider = position != (modelsEndRow - 1);
                    cell.setNeedDivider(needDivider);
                    break;
                }
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getItemViewType() == 2;
        }

        @Override
        public int getItemCount() {
            if (isLoading) return 0;
            return rowCount;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == hintRow) return 0;
            if (position == modelsHeaderRow) return 1;
            if (position >= modelsStartRow && position < modelsEndRow) return 2;
            return 3;
        }
    }
}