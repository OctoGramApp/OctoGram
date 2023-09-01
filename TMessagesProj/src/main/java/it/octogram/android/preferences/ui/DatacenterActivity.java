/*
 * Thanks to OwlGram.
 */

package it.octogram.android.preferences.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.octogram.android.preferences.ui.custom.DatacenterStatus;
import it.octogram.android.utils.DatacenterController;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

public class DatacenterActivity extends BaseFragment {
    private int rowCount;
    private int datacenterStart;
    private ListAdapter listAdapter;
    private DatacenterController.DatacenterStatusChecker datacenterStatusChecker;
    private DatacenterController.DCInfo datacenterList;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRowsId(true);
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (datacenterStatusChecker != null) {
            datacenterStatusChecker.stop(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (datacenterStatusChecker != null) {
            datacenterStatusChecker.stop(false);
        }
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("DatacenterStatus", R.string.DatacenterStatus));
        actionBar.setAllowOverlayTitle(false);
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        listAdapter = new ListAdapter(context);
        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        RecyclerListView listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(listAdapter);
        if (listView.getItemAnimator() != null) {
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        datacenterStatusChecker = new DatacenterController.DatacenterStatusChecker();
        datacenterStatusChecker.setOnUpdate(result -> {
            datacenterList = result;
            for (int i = 0; i < 5; i++) {
                listAdapter.notifyItemChanged(datacenterStart + i, new Object());
                updateRowsId(false);
            }
        });
        datacenterStatusChecker.runListener();
        return fragmentView;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        if (datacenterStatusChecker != null) {
            datacenterStatusChecker.runListener();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId(boolean notify) {
        rowCount = 0;
        datacenterStart = rowCount;
        rowCount += 5;
        if (listAdapter != null && notify) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private final Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder.getItemViewType() == 2) {
                DatacenterStatus datacenterStatusCell = (DatacenterStatus) holder.itemView;
                int status = -1;
                int ping = -1;
                int dcID = (position - datacenterStart) + 1;
                if (datacenterList != null) {
                    DatacenterController.DCStatus datacenterInfo = datacenterList.getByDc(dcID);
                    if (datacenterInfo != null) {
                        status = datacenterInfo.dc_status;
                        ping = datacenterInfo.ping;
                    }
                }
                datacenterStatusCell.setData(dcID, ping, status, true);
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 2;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == 2) {
                view = new DatacenterStatus(mContext);
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            } else {
                view = new ShadowSectionCell(mContext);
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position >= datacenterStart) {
                return 2;
            }
            return 1;
        }
    }
}
