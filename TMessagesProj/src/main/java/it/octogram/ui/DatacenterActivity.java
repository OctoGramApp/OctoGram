package it.octogram.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import it.octogram.android.utils.DCHelper;
import it.octogram.ui.Cells.DatacenterHeader;
import it.octogram.ui.Cells.DatacenterStatus;

public class DatacenterActivity extends BaseFragment {
    private int rowCount;
    private int headerImageRow;
    private int dividerRow;
    private int headerDcListRow;
    private int datacenterStart;
    private ListAdapter listAdapter;
    private DCHelper.DatacenterStatusChecker datacenterStatusChecker;
    private DCHelper.DatacenterList datacenterList;

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
        datacenterStatusChecker = new DCHelper.DatacenterStatusChecker();
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
        headerImageRow = rowCount++;
        dividerRow = rowCount++;
        headerDcListRow = rowCount++;
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
            switch (holder.getItemViewType()) {
                case 2:
                    DatacenterStatus datacenterStatusCell = (DatacenterStatus) holder.itemView;
                    int status = -1;
                    int ping = -1;
                    int dcID = (position - datacenterStart) + 1;
                    if (datacenterList != null) {
                        DCHelper.DatacenterInfo datacenterInfo = datacenterList.getByDc(dcID);
                        if (datacenterInfo != null) {
                            status = datacenterInfo.status;
                            ping = datacenterInfo.ping;
                        }
                    }
                    datacenterStatusCell.setData(dcID, ping, status, true);
                    break;
                case 3:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == headerDcListRow) {
                        headerCell.setText(LocaleController.getString("DatacenterStatus", R.string.DatacenterStatus));
                    }
                    break;
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
            switch (viewType) {
                case 2:
                    view = new DatacenterStatus(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new DatacenterHeader(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                default:
                    view = new ShadowSectionCell(mContext);
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == dividerRow) {
                return 1;
            } else if (position >= datacenterStart) {
                return 2;
            } else if (position == headerDcListRow) {
                return 3;
            } else if (position == headerImageRow) {
                return 4;
            }
            return 1;
        }
    }
}
