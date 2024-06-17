package it.octogram.android.preferences.ui.custom.doublebottom;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.LayoutHelper;

public class AccountSelectList extends ScrollView {
    public AccountSelectList(Context context) {
        super(context);
        setPadding(AndroidUtilities.dp(30), 0, AndroidUtilities.dp(30), 0);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        addView(linearLayout);
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            TLRPC.User u = AccountInstance.getInstance(a).getUserConfig().getCurrentUser();
            if (u != null) {
                AccountSelector item = new AccountSelector(context) {
                    @Override
                    public void onClick(long accountId) {
                        super.onClick(accountId);
                        AccountSelectList.this.onItemClick(accountId);
                    }
                };
                item.setAccount(a);
                linearLayout.addView(item, UserConfig.selectedAccount == a ? 0 : -1);
            }
        }
        for (int a = 0; a < linearLayout.getChildCount(); a++) {
            AccountSelector cell = (AccountSelector) linearLayout.getChildAt(a);
            LinearLayout.LayoutParams layoutParams = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
            boolean isLast = a == linearLayout.getChildCount() - 1;
            if (!isLast) {
                layoutParams.bottomMargin = AndroidUtilities.dp(8);
            }
            cell.setLayoutParams(layoutParams);
        }
    }

    public void onItemClick(long accountId) {}
}