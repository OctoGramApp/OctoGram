package it.octogram.android.app.ui.bottomsheets;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.CodeHighlighting;
import org.telegram.messenger.MessageObject;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BottomSheetWithRecyclerListView;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ListView.AdapterWithDiffUtils;
import org.telegram.ui.Components.Premium.boosts.cells.selector.SelectorBtnCell;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import java.util.ArrayList;

public class MessageJsonBottomSheet extends BottomSheetWithRecyclerListView {

    private ListAdapter adapter;

    private final SelectorBtnCell buttonContainer;

    private final ArrayList<ItemInner> items = new ArrayList<>();

    private SpannableString spannableString = null;

    public MessageJsonBottomSheet(BaseFragment fragment, MessageObject messageObject) {
        super(fragment.getContext(), fragment, false, false, false, false, ActionBarType.SLIDING, fragment.getResourceProvider());
        setSlidingActionBar();

        topPadding = 0.35f;
        fixNavigationBar();
        setShowHandle(true);

        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);
        itemAnimator.setDelayAnimations(false);
        itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        itemAnimator.setDurations(350);
        recyclerListView.setItemAnimator(itemAnimator);

        updateRows(false);

        buttonContainer = new SelectorBtnCell(getContext(), resourcesProvider, null);
        buttonContainer.setClickable(true);
        buttonContainer.setOrientation(LinearLayout.VERTICAL);
        buttonContainer.setPadding(dp(10), dp(10), dp(10), dp(10));
        buttonContainer.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground, resourcesProvider));
        ButtonWithCounterView actionButton = new ButtonWithCounterView(getContext(), resourcesProvider);
        actionButton.setText("Close", false);
        actionButton.setOnClickListener(v -> dismiss());
        buttonContainer.addView(actionButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));
        containerView.addView(buttonContainer, LayoutHelper.createFrameMarginPx(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, backgroundPaddingLeft, 0, backgroundPaddingLeft, 0));

        recyclerListView.setPadding(backgroundPaddingLeft, 0, backgroundPaddingLeft, dp(68));

        AndroidUtilities.runOnUIThread(() -> {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String finalJsonString = gson.toJson(messageObject.messageOwner);

            SpannableString[] sb = new SpannableString[1];
            new CountDownTimer(400, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                    sb[0] = CodeHighlighting.getHighlighted(finalJsonString, "json");
                }

                @Override
                public void onFinish() {
                    spannableString = sb[0];
                    updateRows(true);
                }
            }.start();
        }, 30);
    }

    @Override
    protected CharSequence getTitle() {
        return "Message JSON";
    }

    @Override
    protected RecyclerListView.SelectionAdapter createAdapter(RecyclerListView listView) {
        return adapter = new ListAdapter(getBaseFragment().getContext());
    }

    public void updateRows(boolean animated) {
        ArrayList<ItemInner> oldItems = new ArrayList<>(items);
        items.clear();

        if (spannableString != null) {
            items.add(ItemInner.asJson());
        } else {
            items.add(ItemInner.asSpinner());
        }

        if (adapter != null) {
            if (animated) {
                adapter.setItems(oldItems, items);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onSmoothContainerViewLayout(float ty) {
        super.onSmoothContainerViewLayout(ty);
        buttonContainer.setTranslationY(-ty);
    }

    @Override
    protected boolean canDismissWithSwipe() {
        return !recyclerListView.canScrollVertically(-1);
    }

    private static final int VIEW_TYPE_SPINNER = 1;
    private static final int VIEW_TYPE_JSON = 2;

    private static class ItemInner extends AdapterWithDiffUtils.Item {
        public ItemInner(int viewType) {
            super(viewType, false);
        }

        public static ItemInner asSpinner() {
            return new ItemInner(VIEW_TYPE_SPINNER);
        }
        public static ItemInner asJson() {
            return new ItemInner(VIEW_TYPE_JSON);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof ItemInner other)) {
                return false;
            }
            return other.viewType == viewType;
        }
    }
    private class ListAdapter extends AdapterWithDiffUtils {

        private final Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return false;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case VIEW_TYPE_SPINNER:
                    LinearLayout layout = new LinearLayout(mContext);
                    layout.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setGravity(Gravity.CENTER);

                    ProgressBar progressBar = new ProgressBar(mContext);
                    layout.addView(progressBar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 20, 0, 20));

                    view = layout;
                    break;
                case VIEW_TYPE_JSON:
                    TextView textView = new TextView(mContext);
                    textView.setPadding(dp(22), dp(12), dp(22), dp(6));
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                    textView.setTypeface(AndroidUtilities.bold());
                    textView.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
                    view = textView;
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ItemInner item = items.get(position);
            if (item == null) {
                return;
            }
            if (holder.getItemViewType() == VIEW_TYPE_JSON) {
                TextView cell = (TextView) holder.itemView;
                cell.setText(spannableString);
            }
        }

        @Override
        public int getItemViewType(int position) {
            ItemInner item = items.get(position);
            return item.viewType;
        }
    }
}

