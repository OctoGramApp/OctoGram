package it.octogram.android.preferences.ui.custom.doublebottom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

public class AccountSelector extends FrameLayout {

    private final BackupImageView imageView;
    private final AvatarDrawable avatarDrawable;
    private final SimpleTextView textView;
    private final SimpleTextView detailTextView;
    private long accountId;
    private boolean canceledClick;

    @SuppressLint("ClickableViewAccessibility")
    public AccountSelector(Context context) {
        super(context);
        setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(10), AndroidUtilities.dp(16), AndroidUtilities.dp(10));
        addView(relativeLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        avatarDrawable = new AvatarDrawable();
        avatarDrawable.setTextSize(AndroidUtilities.dp(22));

        imageView = new BackupImageView(context);
        imageView.setRoundRadius(AndroidUtilities.dp(22));
        relativeLayout.addView(imageView, LayoutHelper.createRelative(44, 44, RelativeLayout.CENTER_VERTICAL));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
        relativeLayout.addView(linearLayout, LayoutHelper.createRelative(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 44 + 16, 0, 0, 0, RelativeLayout.CENTER_VERTICAL));

        textView = new SimpleTextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setTextSize(16);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setGravity(Gravity.LEFT);
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        detailTextView = new SimpleTextView(context);
        detailTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        detailTextView.setTextSize(15);
        detailTextView.setGravity(Gravity.CENTER_VERTICAL);
        detailTextView.setGravity(Gravity.LEFT);
        linearLayout.addView(detailTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 3, 0, 0));

        ImageView chevronRight = new ImageView(context);
        chevronRight.setImageResource(R.drawable.msg_inputarrow);
        chevronRight.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(AndroidUtilities.dp(20), AndroidUtilities.dp(20));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        relativeLayout.addView(chevronRight, layoutParams);

        TextView rippleButton = new TextView(context);
        rippleButton.setOnTouchListener((View view, MotionEvent motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP && !canceledClick) {
                onClick(accountId);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                if (motionEvent.getY() > rippleButton.getBottom() || motionEvent.getY() < rippleButton.getTop() || motionEvent.getX() > rippleButton.getRight() || motionEvent.getX() < rippleButton.getLeft()) {
                    canceledClick = true;
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                canceledClick = false;
            }
            return false;
        });
        rippleButton.setClickable(true);
        rippleButton.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), 7, AndroidUtilities.dp(5)));
        addView(rippleButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
    }

    public void setAccount(int account) {
        TLRPC.User user = UserConfig.getInstance(account).getCurrentUser();
        accountId = user.id;
        avatarDrawable.setInfo(user);
        imageView.getImageReceiver().setCurrentAccount(account);
        imageView.setForUserOrChat(user, avatarDrawable);
        textView.setText(ContactsController.formatName(user.first_name, user.last_name));
        int currentAccount = UserConfig.selectedAccount;
        if (user.id == UserConfig.getInstance(currentAccount).getClientUserId() || user.status != null && user.status.expires > ConnectionsManager.getInstance(currentAccount).getCurrentTime() || MessagesController.getInstance(currentAccount).onlinePrivacy.containsKey(user.id)) {
            detailTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
            detailTextView.setText(LocaleController.getString("Online", R.string.Online));
        } else {
            detailTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            detailTextView.setText(LocaleController.formatUserStatus(currentAccount, user));
        }
    }

    public void onClick(long accountId) {}
}