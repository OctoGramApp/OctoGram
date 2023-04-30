package it.octogram.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.PasscodeActivity;

import java.util.ArrayList;

import it.octogram.android.PasscodeController;

public class AccountProtectionSettings extends BaseSettingsActivity {
    private int dbAnRow;
    private int hintRow;
    private int accountsHeaderRow;
    private int accountsStartRow;
    private int accountsEndRow;
    private int accountsDetailsRow;
    private int disableAccountProtectionRow;
    private final ArrayList<TLRPC.User> accounts = new ArrayList<>();

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("AccountProtection", R.string.AccountProtection);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        super.onItemClick(view, position, x, y);
        if (position == disableAccountProtectionRow) {
            AlertDialog alertDialog = new AlertDialog.Builder(getParentActivity())
                    .setTitle(LocaleController.getString("DisableAccountProtection", R.string.DisableAccountProtection))
                    .setMessage(LocaleController.getString("DisableAccountProtectionAlert", R.string.DisableAccountProtectionAlert))
                    .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                    .setPositiveButton(LocaleController.getString(R.string.DisablePasscodeTurnOff), (dialog, which) -> {
                        PasscodeController.disableAccountProtection();
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetPasscode);
                        reloadMainInfo();
                        finishFragment();
                    }).create();
            alertDialog.show();
            ((TextView) alertDialog.getButton(Dialog.BUTTON_POSITIVE)).setTextColor(Theme.getColor(Theme.key_dialogTextRed));
        } else if (position >= accountsStartRow && position < accountsEndRow) {
            TLRPC.User user = accounts.get(position - accountsStartRow);
            if (PasscodeController.isProtectedAccount(user.id)) {
                final ArrayList<String> items = new ArrayList<>();
                final ArrayList<Integer> icons = new ArrayList<>();
                final ArrayList<Integer> actions = new ArrayList<>();

                items.add(LocaleController.getString("ChangePasscode", R.string.ChangePasscode));
                icons.add(R.drawable.edit_passcode);
                actions.add(0);
                items.add(LocaleController.getString("DisablePasscode", R.string.DisablePasscode));
                icons.add(R.drawable.msg_disable);
                actions.add(1);

                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setItems(items.toArray(new CharSequence[actions.size()]), AndroidUtilities.toIntArray(icons), (dialogInterface, i) -> {
                    if (actions.get(i) == 0) {
                        presentFragment(new PasscodeActivity(PasscodeActivity.TYPE_SETUP_CODE, user.id));
                    } else if (actions.get(i) == 1) {
                        AlertDialog alertDialog = new AlertDialog.Builder(getParentActivity())
                                .setTitle(LocaleController.getString("DisablePasscode", R.string.DisablePasscode))
                                .setMessage(LocaleController.getString("DisablePasscodeConfirmMessage", R.string.DisablePasscodeConfirmMessage))
                                .setNegativeButton(LocaleController.getString(R.string.Cancel), null)
                                .setPositiveButton(LocaleController.getString(R.string.DisablePasscodeTurnOff), (dialog, which) -> {
                                    PasscodeController.removePasscodeForAccount(user.id);
                                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetPasscode);
                                    reloadMainInfo();
                                    updateRowsId();
                                    if (!PasscodeController.existAtLeastOnePasscode())
                                        finishFragment();
                                }).create();
                        alertDialog.show();
                        ((TextView) alertDialog.getButton(Dialog.BUTTON_POSITIVE)).setTextColor(Theme.getColor(Theme.key_dialogTextRed));
                    }
                });
                AlertDialog alertDialog = builder.create();
                showDialog(alertDialog);
                alertDialog.setItemColor(items.size() - 1, Theme.getColor(Theme.key_dialogTextRed), Theme.getColor(Theme.key_dialogRedIcon));
            } else {
                presentFragment(new PasscodeActivity(PasscodeActivity.TYPE_SETUP_CODE, user.id));
            }
        }
    }

    private int getActiveAccounts() {
        accounts.clear();
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            TLRPC.User u = AccountInstance.getInstance(a).getUserConfig().getCurrentUser();
            if (u != null) {
                if (u.id == UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId()) {
                    accounts.add(0, u);
                } else {
                    accounts.add(u);
                }
            }
        }
        return accounts.size();
    }

    protected void updateRowsId() {
        super.updateRowsId();
        dbAnRow = rowCount++;
        hintRow = rowCount++;
        accountsHeaderRow = rowCount++;
        accountsStartRow = rowCount;
        rowCount += getActiveAccounts();
        accountsEndRow = rowCount;
        accountsDetailsRow = rowCount++;
        disableAccountProtectionRow = rowCount++;
    }

    @Override
    protected BaseListAdapter createAdapter() {
        return new ListAdapter();
    }

    private class ListAdapter extends BaseListAdapter {

        @Override
        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            switch (ViewType.fromInt(holder.getItemViewType())) {
                case TEXT_HINT:
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == hintRow) {
                        cell.setText(LocaleController.getString("AccountProtectionHint1", R.string.AccountProtectionHint1));
                        cell.setBackground(null);
                        cell.getTextView().setGravity(Gravity.CENTER_HORIZONTAL);
                    } else if (position == accountsDetailsRow) {
                        cell.setText(LocaleController.getString("AccountProtectionHint2", R.string.AccountProtectionHint2));
                        cell.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                        cell.getTextView().setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
                    }
                    break;
                case STICKER_HOLDER:
                    RLottieImageHolderView holderView = (RLottieImageHolderView) holder.itemView;
                    holderView.imageView.setAnimation(R.raw.double_bottom, 100, 100);
                    holderView.imageView.getAnimatedDrawable().setAutoRepeat(1);
                    holderView.imageView.playAnimation();
                    break;
                case HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == accountsHeaderRow) {
                        headerCell.setText(LocaleController.getString("AllAccounts", R.string.AllAccounts));
                    }
                    break;
                case ACCOUNT:
                    int accountNum = position - accountsStartRow;
                    TLRPC.User user = accounts.get(accountNum);
                    UserCell userCell = (UserCell) holder.itemView;
                    userCell.setCheckedRight(PasscodeController.isProtectedAccount(user.id));
                    userCell.setData(user, null, null, 0, accountNum != accounts.size() - 1);
                    break;
                case SETTINGS:
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    if (position == disableAccountProtectionRow) {
                        textCell.setText(LocaleController.getString("DisableAccountProtection", R.string.DisableAccountProtection), false);
                        textCell.setTag(Theme.key_dialogTextRed);
                        textCell.setTextColor(Theme.getColor(Theme.key_dialogTextRed));
                    }
                    break;
            }
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.ACCOUNT || viewType == ViewType.SETTINGS;
        }

        @Override
        protected View onCreateViewHolder(ViewType viewType) {
            View view = null;
            if (viewType == ViewType.STICKER_HOLDER) {
                view = new RLottieImageHolderView(context);
            }
            return view;
        }

        @Override
        protected ViewType getViewType(int position) {
            if (position == dbAnRow) {
                return ViewType.STICKER_HOLDER;
            } else if (position == accountsHeaderRow) {
                return ViewType.HEADER;
            } else if (position >= accountsStartRow && position < accountsEndRow) {
                return ViewType.ACCOUNT;
            } else if (position == disableAccountProtectionRow) {
                return ViewType.SETTINGS;
            } else if (position == hintRow || position == accountsDetailsRow) {
                return ViewType.TEXT_HINT;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }

    private final static class RLottieImageHolderView extends FrameLayout {
        private final RLottieImageView imageView;

        private RLottieImageHolderView(@NonNull Context context) {
            super(context);
            imageView = new RLottieImageView(context);
            int size = AndroidUtilities.dp(120);
            LayoutParams params = new LayoutParams(size, size);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            addView(imageView, params);

            setPadding(0, AndroidUtilities.dp(32), 0, 0);
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }
}
