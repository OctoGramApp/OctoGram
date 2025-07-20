/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.ai;

import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.os.Bundle;
import android.text.style.URLSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

import it.octogram.android.AiModelMessagesState;
import it.octogram.android.AiModelType;
import it.octogram.android.OctoConfig;
import it.octogram.android.utils.ai.icons.AiFeatureIcons;
import it.octogram.android.utils.ai.ui.MainAiBottomSheet;

public class CustomModelsMenuWrapper {
    public static void initState(FillStateData data) {
        if (!MainAiHelper.canUseAiFeatures() || (data.isInputBox && (data.messageText == null || data.messageText.toString().trim().isEmpty()))) {
            data.hideButton();
            return;
        }

        AiModelMessagesState state = getAvailableStates(data);
        if (state == null && !data.isChat && !data.isInputBox) {
            data.hideButton();
            return;
        }

        ArrayList<String> availableModels = getEligibleModels(data, state);
        if (availableModels.isEmpty() && !data.canAskOnMedia() && !data.canTranslate()) {
            data.hideButton();
            return;
        }

        data.originalSubItem.setMinimumWidth(AndroidUtilities.dp(196));
        data.applyColors();

        if ((availableModels.size() + (data.canAskOnMedia() ? 1 : 0) + (data.canTranslate() ? 1 : 0)) == 1) {
            data.originalSubItem.setRightIcon(R.drawable.msg_arrowright);
            data.originalSubItem.setSubtext(getString(R.string.AiFeatures_Brief));
            if (!availableModels.isEmpty()) {
                String modelID = availableModels.get(0);
                CustomModelsHelper.CustomModel model = CustomModelsHelper.getModelById(modelID);
                if (model != null) {
                    String modelTitle = formatString(R.string.AiFeatures_CustomModels_ModelID, modelID);
                    if (!model.title.trim().isEmpty()) {
                        modelTitle = model.title.trim();
                    }

                    data.originalSubItem.setIcon(AiFeatureIcons.getModelIcon(model.icon));
                    data.originalSubItem.setText(Emoji.replaceEmoji(modelTitle, data.originalSubItem.textView.getPaint().getFontMetricsInt(), true));
                    fixIcon(data.originalSubItem);

                    String finalModelTitle = modelTitle;
                    data.originalSubItem.setOnClickListener(view -> handleOnClick(modelID, finalModelTitle, model, data));
                }
            } else if (data.canAskOnMedia()) {
                data.originalSubItem.setIcon(R.drawable.photo_paint_brush);
                data.originalSubItem.setText(getString(R.string.AiFeatures_Features_AskOnPhoto));
                data.originalSubItem.setOnClickListener(view -> handleAskOnMedia(data));
            } else if (data.canTranslate()) {
                data.originalSubItem.setIcon(R.drawable.msg_translate);
                data.originalSubItem.setText(getString(R.string.AiFeatures_Features_TranslateAI2));
                data.originalSubItem.setOnClickListener(view -> handleAiTranslation(data));
            }
            data.originalSubItem.invalidate();
            return;
        }

        data.originalSubItem.setRightIcon(R.drawable.msg_arrowright);
        data.originalSubItem.setIcon(R.drawable.aifeatures_solar);
        data.originalSubItem.setText(getString(R.string.AiFeatures_Brief));

        CustomModelSwipeActivity viewToSwipeBack = new CustomModelSwipeActivity(data, availableModels);
        if (data.useSwipeBack) {
            int swipeBackIndex = data.popupWindowLayout.addViewToSwipeBack(viewToSwipeBack.windowLayout);
            data.originalSubItem.openSwipeBackLayout = () -> {
                if (data.popupWindowLayout.getSwipeBack() != null) {
                    data.popupWindowLayout.getSwipeBack().openForeground(swipeBackIndex);
                }
            };
            data.originalSubItem.setOnClickListener(view -> data.originalSubItem.openSwipeBack());
        } else {
            data.originalSubItem.setOnClickListener(view -> {
                if (data.onSheetOpen != null) {
                    data.onSheetOpen.run();
                }
                data.modelID = null;

                MainAiBottomSheet alert = new MainAiBottomSheet(data);
                alert.setDimBehind(!data.supportsActivityRelatedDimBehind);
                alert.show();
            });
        }
    }

    public static void handleMessagesSelectionWithModel(Context context, String modelID, TLRPC.User currentUser, TLRPC.Chat currentChat, ArrayList<MessageObject> messages) {
        handleMessagesSelectionWithModel(context, modelID, currentUser, currentChat, messages, 0);
    }

    public static void handleMessagesSelectionWithModel(Context context, String modelID, TLRPC.User currentUser, TLRPC.Chat currentChat, ArrayList<MessageObject> messages, int forcedMessagesToPassCounter) {
        if (!MainAiHelper.canUseAiFeatures()) {
            return;
        }

        CustomModelsHelper.CustomModel model = CustomModelsHelper.getModelById(modelID);
        if (model == null || model.modelType != AiModelType.RELATED_TO_CHATS) {
            return;
        }

        Consumer<ArrayList<MessageObject>> consumer = messageObjects -> {
            if (messageObjects == null || messageObjects.isEmpty()) {
                BaseFragment lastFragment = LaunchActivity.getSafeLastFragment();
                if (lastFragment instanceof ChatActivity) {
                    AndroidUtilities.runOnUIThread(() -> BulletinFactory.of(lastFragment).createErrorBulletin(LocaleController.getString(R.string.MessageNotFound)).show());
                }
                return;
            }

            FillStateData data = new FillStateData();
            data.context = context;
            data.modelID = modelID;
            data.isChat = true;
            data.currentUser = currentUser;
            data.currentChat = currentChat;
            data.selectedMessages.addAll(messageObjects);

            AndroidUtilities.runOnUIThread(() -> {
                MainAiBottomSheet alert = new MainAiBottomSheet(data);
                alert.setDimBehind(true);
                alert.show();
            });
        };

        if (messages.isEmpty()) {
            MessagesController instance = MessagesController.getInstance(UserConfig.selectedAccount);
            ConnectionsManager manager = ConnectionsManager.getInstance(UserConfig.selectedAccount);

            int[] reqId = {0};

            AlertDialog progressDialog = new AlertDialog(context, AlertDialog.ALERT_TYPE_SPINNER);
            progressDialog.showDelayed(300);
            progressDialog.setOnCancelListener(dialog -> {
                if (reqId[0] != 0) {
                    manager.cancelRequest(reqId[0], true);
                }
            });

            TLRPC.TL_messages_getHistory req = new TLRPC.TL_messages_getHistory();
            req.peer = instance.getInputPeer(currentChat != null ? -currentChat.id : currentUser.id);
            req.limit = (forcedMessagesToPassCounter > 0 && forcedMessagesToPassCounter <= model.messagesToPass) ? forcedMessagesToPassCounter : model.messagesToPass;
            reqId[0] = manager.sendRequest(req, (response, error) -> {
                progressDialog.dismiss();

                if (response != null) {
                    TLRPC.messages_Messages res = (TLRPC.messages_Messages) response;
                    instance.putUsers(res.users, false);
                    instance.putChats(res.chats, false);
                    MessagesStorage.getInstance(UserConfig.selectedAccount).putUsersAndChats(res.users, res.chats, false, true);

                    Collections.reverse(res.messages);
                    ArrayList<MessageObject> tempMessages = new ArrayList<>();
                    for (TLRPC.Message message : res.messages) {
                        tempMessages.add(new MessageObject(UserConfig.selectedAccount, message, false, false));
                    }
                    consumer.accept(tempMessages);
                } else if (error != null) {
                    consumer.accept(null);
                }
            });
        } else {
            messages.sort(Comparator.comparingInt(MessageObject::getId));
            consumer.accept(messages);
        }
    }

    public static int getSuggestedAskOnMediaAction(FillStateData data) {
        int tokenIndex = new Random().nextInt(3) + 1;

        int key;
        if (tokenIndex == 2) {
            key = R.string.AiFeatures_CustomModels_Feature_SelectModel_Desc_Ask_Example2;
        } else if (tokenIndex == 3) {
            key = R.string.AiFeatures_CustomModels_Feature_SelectModel_Desc_Ask_Example3;
        } else {
            key = R.string.AiFeatures_CustomModels_Feature_SelectModel_Desc_Ask_Example1;
        }

        AiModelMessagesState state = CustomModelsMenuWrapper.getAvailableStates(data);

        if (state == AiModelMessagesState.VOICE_MESSAGES || state == AiModelMessagesState.MUSIC) {
            key = R.string.AiFeatures_CustomModels_Feature_SelectModel_Desc_Ask_Example5;
            if (tokenIndex == 2 || tokenIndex == 3) {
                key = R.string.AiFeatures_CustomModels_Feature_SelectModel_Desc_Ask_Example4;
            }
        }
        return key;
    }

    public static AiModelMessagesState getAvailableStates(FillStateData data) {
        return getAvailableStates(data.messageObject);
    }

    public static AiModelMessagesState getAvailableStates(MessageObject messageObject) {
        if (messageObject != null) {
            if (messageObject.messageOwner.media == null) {
                return AiModelMessagesState.TEXT_MESSAGES;
            } else if (messageObject.isPhoto()) {
                return AiModelMessagesState.PHOTOS;
            } else if (messageObject.isSticker() && !messageObject.isAnimatedSticker()) {
                return AiModelMessagesState.STICKERS;
            } else if (messageObject.isVoice()) {
                return AiModelMessagesState.VOICE_MESSAGES;
            } else if (messageObject.isMusic()) {
                return AiModelMessagesState.MUSIC;
            } else if (messageObject.isVideo()) {
                return AiModelMessagesState.VIDEOS;
            } else if (messageObject.isGif()) {
                return AiModelMessagesState.GIFS;
            }
        }

        return null;
    }

    public static ArrayList<String> getEligibleModels(FillStateData data, AiModelMessagesState state) {
        ArrayList<String> availableModels = new ArrayList<>();
        HashMap<String, CustomModelsHelper.CustomModel> models = CustomModelsHelper.getModelsList();
        for (String modelID : models.keySet()) {
            CustomModelsHelper.CustomModel model = Objects.requireNonNull(models.get(modelID));

            boolean isRelated;
            if (data.isInputBox || data.isChat) {
                isRelated = model.modelType == (data.isChat ? AiModelType.RELATED_TO_CHATS : AiModelType.RELATED_TO_INPUT);
            } else {
                isRelated = model.appearsInList.contains(state) && model.modelType == AiModelType.RELATED_TO_MESSAGES;
            }

            if (isRelated) {
                availableModels.add(modelID);
            }
        }
        return availableModels;
    }

    private static void handleOnClick(String modelID, String finalModelTitle, CustomModelsHelper.CustomModel model, FillStateData data) {
        if (data.onSheetOpen != null) {
            data.onSheetOpen.run();
        }

        if (data.isChat) {
            Bundle bundle = new Bundle();
            if (data.currentChat != null) {
                bundle.putLong("chat_id", data.currentChat.id);
            } else {
                bundle.putLong("user_id", data.currentUser.id);
            }
            bundle.putString("customAiModelID", modelID);
            bundle.putString("customAiModelTitle", finalModelTitle);
            bundle.putInt("customAiModelMaxMessages", model.messagesToPass);
            LaunchActivity.instance.presentFragment(new ChatActivity(bundle));
            return;
        }

        data.modelID = modelID;
        MainAiBottomSheet alert = new MainAiBottomSheet(data);
        alert.setDimBehind(!data.supportsActivityRelatedDimBehind);
        alert.show();
    }

    private static void handleAskOnMedia(FillStateData data) {
        data.modelID = CustomModelsHelper.VIRTUAL_ASK_ON_MEDIA_MODEL_ID;
        handleOnClick(data.modelID, null, null, data);
    }

    private static void handleAiTranslation(FillStateData data) {
        data.modelID = CustomModelsHelper.VIRTUAL_TRANSLATE_MODEL_ID;
        handleOnClick(data.modelID, null, null, data);
    }

    private static void fixIcon(ActionBarMenuSubItem item) {
        var imageView = item.getImageView();

        if (imageView != null) {
            ViewGroup.LayoutParams lp = imageView.getLayoutParams();
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                int sizeInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, imageView.getResources().getDisplayMetrics());

                lp.width = sizeInPx;
                lp.height = sizeInPx;

                imageView.setLayoutParams(lp);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }
    }

    private static class CustomModelSwipeActivity {
        public ActionBarPopupWindow.ActionBarPopupWindowLayout windowLayout;

        public CustomModelSwipeActivity(FillStateData data, ArrayList<String> eligibleModels) {
            windowLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(data.context, 0, null) {
                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    super.onMeasure(
                            MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(200), MeasureSpec.AT_MOST),
                            heightMeasureSpec
                    );
                }
            };
            windowLayout.setFitItems(true);

            if (data.popupWindowLayout.getSwipeBack() != null) {
                ActionBarMenuSubItem backItem = ActionBarMenuItem.addItem(windowLayout, R.drawable.msg_arrow_back, getString(R.string.Back), false, null);
                backItem.setOnClickListener(view -> data.popupWindowLayout.getSwipeBack().closeForeground());
                data.applyColors(backItem);
                ActionBarMenuItem.addGap(0, windowLayout);
            }

            if (data.canTranslate()) {
                ActionBarMenuSubItem item = ActionBarMenuItem.addItem(windowLayout, R.drawable.cup_star_solar, getString(R.string.AiFeatures_Features_TranslateAI2), false, null);
                item.setOnClickListener(view -> handleAiTranslation(data));
                data.applyColors(item);
            }

            if (data.canAskOnMedia()) {
                ActionBarMenuSubItem item = ActionBarMenuItem.addItem(windowLayout, R.drawable.photo_paint_brush, getString(R.string.AiFeatures_Features_AskOnPhoto), false, null);
                item.setOnClickListener(view -> handleAskOnMedia(data));
                data.applyColors(item);
            }

            if (data.canTranslate() || data.canAskOnMedia()) {
                ActionBarMenuItem.addGap(0, windowLayout);
            }

            for (String modelID : eligibleModels) {
                CustomModelsHelper.CustomModel model = CustomModelsHelper.getModelById(modelID);
                if (model != null) {
                    String modelTitle = formatString(R.string.AiFeatures_CustomModels_ModelID, modelID);
                    if (!model.title.trim().isEmpty()) {
                        modelTitle = model.title.trim();
                    }
                    ActionBarMenuSubItem item = ActionBarMenuItem.addItem(windowLayout, AiFeatureIcons.getModelIcon(model.icon), modelTitle, false, null);
                    item.textView.setText(Emoji.replaceEmoji(modelTitle, item.textView.getPaint().getFontMetricsInt(), true));
                    fixIcon(item);
                    data.applyColors(item);

                    String finalModelTitle = modelTitle;
                    item.setOnClickListener(view -> handleOnClick(modelID, finalModelTitle, model, data));
                }
            }
        }
    }

    public static class FillStateData {
        public MessageObject messageObject;
        public CharSequence messageText;
        public Utilities.CallbackReturn<URLSpan, Boolean> onLinkPress;

        public String modelID;

        public MessageObject replyMessageObject;

        public boolean isChat = false;
        public TLRPC.Chat currentChat;
        public TLRPC.User currentUser;
        public ArrayList<MessageObject> selectedMessages = new ArrayList<>();

        public boolean useSwipeBack = true;
        public boolean isInputBox = false;
        public Utilities.Callback<String> setInputBoxText;

        public Context context;
        public Runnable onSheetOpen;
        public Runnable onSheetClose;
        public Runnable onNewFragmentOpen;
        public boolean noforwards = false;
        public boolean supportsActivityRelatedDimBehind = false;
        public ActionBarPopupWindow.ActionBarPopupWindowLayout popupWindowLayout;
        public ActionBarMenuSubItem originalSubItem;

        private int textColor = -1;
        private int iconColor = -1;

        public void setColors(int textColor, int iconColor) {
            this.textColor = textColor;
            this.iconColor = iconColor;
        }

        public boolean hasColors() {
            return textColor != -1 && iconColor != -1;
        }

        public void applyColors(ActionBarMenuSubItem item) {
            if (item != null && hasColors()) {
                item.setColors(textColor, iconColor);
            }
        }

        public void applyColors() {
            applyColors(originalSubItem);
        }

        public void hideButton() {
            originalSubItem.setVisibility(View.GONE);
        }

        public long getChatID() {
            return messageObject != null ? messageObject.getChatId() : 0;
        }

        public boolean canAskOnMedia() {
            return useSwipeBack && !isInputBox && !isChat && OctoConfig.INSTANCE.aiFeaturesAskOnMedia.getValue() && getAvailableStates(this) != null && getAvailableStates(this) != AiModelMessagesState.TEXT_MESSAGES;
        }

        public boolean canTranslate() {
            return !isChat && OctoConfig.INSTANCE.aiFeaturesTranslateMessages.getValue() && messageText != null && !messageText.toString().trim().isEmpty();
        }
    }
}
