package it.octogram.android;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.TranslateController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.AbstractSerializedData;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AnimatedEmojiSpan;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.TextStyleSpan;
import org.telegram.ui.Components.URLSpanReplacement;
import org.telegram.ui.Components.URLSpanUserMention;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import it.octogram.android.entities.EntitiesHelper;
import it.octogram.android.translator.TranslatorHelper;

public class MessageHelper extends BaseController {

    private static final MessageHelper[] Instance = new MessageHelper[UserConfig.MAX_ACCOUNT_COUNT];
    private static SpannableStringBuilder arrowSpan;
    public static Drawable arrowDrawable;
    public static SpannableStringBuilder editedSpan;
    public static Drawable editedDrawable;

    public MessageHelper(int num) {
        super(num);
    }

    public void saveStickerToGallery(Activity activity, MessageObject messageObject, Runnable callback) {
        saveStickerToGallery(activity, getPathToMessage(messageObject), messageObject.isVideoSticker(), callback);
    }

    public static void saveStickerToGallery(Activity activity, TLRPC.Document document, Runnable callback) {
        String path = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(document, true).toString();
        File temp = new File(path);
        if (!temp.exists()) {
            return;
        }
        saveStickerToGallery(activity, path, MessageObject.isVideoSticker(document), callback);
    }

    public MessageObject getMessageForTranslate(MessageObject selectedObject, MessageObject.GroupedMessages selectedObjectGroup) {
        MessageObject messageObject = null;
        if (selectedObjectGroup != null && !selectedObjectGroup.isDocuments) {
            messageObject = getTargetMessageObjectFromGroup(selectedObjectGroup);
        } else if (selectedObject.isPoll()) {
            messageObject = selectedObject;
        } else if (!TextUtils.isEmpty(selectedObject.messageOwner.message)) {
            messageObject = selectedObject;
        }
        return messageObject;
    }


    private static void saveStickerToGallery(Activity activity, String path, boolean video, Runnable callback) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                if (video) {
                    MediaController.saveFile(path, activity, 1, null, null, callback);
                } else {
                    Bitmap image = BitmapFactory.decodeFile(path);
                    if (image != null) {
                        File file = new File(path.replace(".webp", ".png"));
                        FileOutputStream stream = new FileOutputStream(file);
                        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        stream.close();
                        MediaController.saveFile(file.toString(), activity, 0, null, null, callback);
                    }
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        });
    }

    public String getPathToMessage(MessageObject messageObject) {
        String path = messageObject.messageOwner.attachPath;
        if (!TextUtils.isEmpty(path)) {
            File temp = new File(path);
            if (!temp.exists()) {
                path = null;
            }
        }
        if (TextUtils.isEmpty(path)) {
            path = FileLoader.getInstance(UserConfig.selectedAccount).getPathToMessage(messageObject.messageOwner).toString();
            File temp = new File(path);
            if (!temp.exists()) {
                path = null;
            }
        }
        if (TextUtils.isEmpty(path)) {
            path = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(messageObject.getDocument(), true).toString();
            File temp = new File(path);
            if (!temp.exists()) {
                return null;
            }
        }
        return path;
    }

    public static MessageHelper getInstance(int num) {
        MessageHelper localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (MessageHelper.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new MessageHelper(num);
                }
            }
        }
        return localInstance;
    }

    public boolean isMessageTranslatable(MessageObject messageObject) {
        if (messageObject.isPoll()) {
            return true;
        }
        return !TextUtils.isEmpty(messageObject.messageOwner.message) && !isLinkOrEmojiOnlyMessage(messageObject);
    }

    public boolean isLinkOrEmojiOnlyMessage(MessageObject messageObject) {
        var entities = messageObject.messageOwner.entities;
        if (entities != null) {
            for (TLRPC.MessageEntity entity : entities) {
                if (entity instanceof TLRPC.TL_messageEntityBotCommand ||
                        entity instanceof TLRPC.TL_messageEntityEmail ||
                        entity instanceof TLRPC.TL_messageEntityUrl ||
                        entity instanceof TLRPC.TL_messageEntityMention ||
                        entity instanceof TLRPC.TL_messageEntityCashtag ||
                        entity instanceof TLRPC.TL_messageEntityHashtag ||
                        entity instanceof TLRPC.TL_messageEntityBankCard ||
                        entity instanceof TLRPC.TL_messageEntityPhone) {
                    if (entity.offset == 0 && entity.length == messageObject.messageOwner.message.length()) {
                        return true;
                    }
                }
            }
        }
        return EntitiesHelper.isEmoji(messageObject.messageOwner.message);
    }

    public MessageObject getMessageForRepeat(MessageObject selectedObject, MessageObject.GroupedMessages selectedObjectGroup) {
        MessageObject messageObject = null;
        if (selectedObjectGroup != null && !selectedObjectGroup.isDocuments) {
            messageObject = getTargetMessageObjectFromGroup(selectedObjectGroup);
        } else if (!TextUtils.isEmpty(selectedObject.messageOwner.message) || selectedObject.isAnyKindOfSticker()) {
            messageObject = selectedObject;
        }
        return messageObject;
    }

    public static MessageObject getTargetMessageObjectFromGroup(MessageObject.GroupedMessages selectedObjectGroup) {
        MessageObject messageObject = null;
        for (MessageObject object : selectedObjectGroup.messages) {
            if (!TextUtils.isEmpty(object.messageOwner.message)) {
                if (messageObject != null) {
                    messageObject = null;
                    break;
                } else {
                    messageObject = object;
                }
            }
        }
        return messageObject;
    }

    public static CharSequence createEditedString(MessageObject messageObject) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        if (editedDrawable == null) {
            editedDrawable = Objects.requireNonNull(ContextCompat.getDrawable(ApplicationLoader.applicationContext, R.drawable.msg_edited)).mutate();
        }
        if (editedSpan == null) {
            editedSpan = new SpannableStringBuilder("\u200B");
            editedSpan.setSpan(new ColoredImageSpan(editedDrawable), 0, 1, 0);
        }
        spannableStringBuilder
                .append(' ')
                .append(OctoConfig.showPencilIcon ? editedSpan : LocaleController.getString("EditedMessage", R.string.EditedMessage))
                .append(' ')
                .append(LocaleController.getInstance().formatterDay.format((long) (messageObject.messageOwner.date) * 1000));
        return spannableStringBuilder;
    }

    public static CharSequence createTranslateString(MessageObject messageObject) {
        String time = LocaleController.getInstance().formatterDay.format((long) (messageObject.messageOwner.date) * 1000);
        String fromLanguage = messageObject.messageOwner.originalLanguage;
        String toLanguage = messageObject.messageOwner.translatedToLanguage;
        if (messageObject.messageOwner.translatedText != null &&
                TextUtils.equals(messageObject.messageOwner.message, messageObject.messageOwner.translatedText.text) &&
                !MessagesController.getInstance(UserConfig.selectedAccount).getTranslateController().isManualTranslation(messageObject)
        ) {
            return null;
        }
        if (TextUtils.isEmpty(fromLanguage) || TextUtils.isEmpty(toLanguage) || TextUtils.equals(fromLanguage, TranslateController.UNKNOWN_LANGUAGE)) {
            return LocaleController.getString("MessageTranslated", R.string.MessageTranslated) + " " + time;
        }
        fromLanguage = fromLanguage.split("-")[0];
        if (arrowDrawable == null) {
            arrowDrawable = Objects.requireNonNull(ContextCompat.getDrawable(ApplicationLoader.applicationContext, R.drawable.search_arrow)).mutate();
        }
        if (arrowSpan == null) {
            arrowSpan = new SpannableStringBuilder("\u200B");
            arrowSpan.setSpan(new ColoredImageSpan(arrowDrawable), 0, 1, 0);
        }
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder
                .append(TranslatorHelper.languageName(fromLanguage))
                .append(' ')
                .append(arrowSpan)
                .append(' ')
                .append(TranslatorHelper.languageName(toLanguage))
                .append(' ')
                .append(time);
        return spannableStringBuilder;
    }

    public String getPlainText(MessageObject messageObject) {
        StringBuilder plainText;
        if (messageObject.type == MessageObject.TYPE_POLL) {
            TLRPC.Poll poll = ((TLRPC.TL_messageMediaPoll) messageObject.messageOwner.media).poll;
            plainText = new StringBuilder(poll.question);
            plainText.append("\n");
            for (int a = 0; a < poll.answers.size(); a++) {
                TLRPC.TL_pollAnswer answer = poll.answers.get(a);
                plainText.append("\n").append("\uD83D\uDD18").append(" ").append(answer.text);
            }
        } else if (messageObject.messageOwner.message != null) {
            plainText = new StringBuilder(messageObject.messageOwner.message);
            if (messageObject.messageOwner.reply_markup != null) {
                plainText.append("\n");
                for (int a = 0; a < messageObject.messageOwner.reply_markup.rows.size(); a++) {
                    TLRPC.TL_keyboardButtonRow row = messageObject.messageOwner.reply_markup.rows.get(a);
                    for (int b = 0; b < row.buttons.size(); b++) {
                        TLRPC.KeyboardButton button = row.buttons.get(b);
                        plainText.append("\n").append("\uD83D\uDD18").append(" ").append(button.text);
                    }
                }
            }
        } else {
            return null;
        }
        return plainText.toString();
    }

    public static class PollTexts {
        private final ArrayList<String> texts = new ArrayList<>();
        public static int constructor = 0xca;

        public PollTexts(TLRPC.Poll source) {
            texts.add(source.question);
            for (int a = 0; a < source.answers.size(); a++) {
                texts.add(source.answers.get(a).text);
            }
        }

        public PollTexts() {}

        public ArrayList<String> getTexts() {
            return texts;
        }

        public PollTexts copy() {
            PollTexts pollTexts = new PollTexts();
            pollTexts.texts.addAll(texts);
            return pollTexts;
        }

        public void serializeToStream(AbstractSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(0x1cb5c415);
            stream.writeInt32(texts.size());
            for (int a = 0; a < texts.size(); a++) {
                stream.writeString(texts.get(a));
            }
        }

        public static PollTexts TLdeserialize(AbstractSerializedData stream, int constructor, boolean exception) {
            if (constructor != PollTexts.constructor) {
                if (exception) {
                    throw new RuntimeException(String.format("can't parse magic %x in PollTexts", constructor));
                } else {
                    return null;
                }
            }
            PollTexts result = new PollTexts();
            result.readParams(stream, exception);
            return result;
        }

        public void readParams(AbstractSerializedData stream, boolean exception) {
            int magic = stream.readInt32(exception);
            if (magic != 0x1cb5c415) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int a = 0; a < count; a++) {
                texts.add(stream.readString(exception));
            }
        }

        public int size() {
            return texts.size();
        }

        public void applyTextToPoll(TLRPC.Poll poll) {
            poll.question = texts.get(0);
            for (int a = 0; a < poll.answers.size(); a++) {
                poll.answers.get(a).text = texts.get(a + 1);
            }
        }
    }

    public static class ReplyMarkupButtonsTexts {
        private final ArrayList<ArrayList<String>> texts = new ArrayList<>();
        public static int constructor = 0xc9;

        public ReplyMarkupButtonsTexts(ArrayList<TLRPC.TL_keyboardButtonRow> source) {
            for (int a = 0; a < source.size(); a++) {
                ArrayList<TLRPC.KeyboardButton> buttonRow = source.get(a).buttons;
                ArrayList<String> row = new ArrayList<>();
                for (int b = 0; b < buttonRow.size(); b++) {
                    TLRPC.KeyboardButton button2 = buttonRow.get(b);
                    row.add(button2.text);
                }
                texts.add(row);
            }
        }

        public ReplyMarkupButtonsTexts() {}

        public int size() {
            int total = 0;
            for (int a = 0; a < texts.size(); a++) {
                total += texts.get(a).size();
            }
            return total;
        }

        public void serializeToStream(AbstractSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(0x1cb5c415);
            stream.writeInt32(texts.size());
            for (int a = 0; a < texts.size(); a++) {
                stream.writeInt32(0x1cb5c415);
                stream.writeInt32(texts.get(a).size());
                for (int b = 0; b < texts.get(a).size(); b++) {
                    stream.writeString(texts.get(a).get(b));
                }
            }
        }

        public static ReplyMarkupButtonsTexts TLdeserialize(AbstractSerializedData stream, int constructor, boolean exception) {
            if (constructor != ReplyMarkupButtonsTexts.constructor) {
                if (exception) {
                    throw new RuntimeException(String.format("can't parse magic %x in ReplyMarkupButtonsTexts", constructor));
                } else {
                    return null;
                }
            }
            ReplyMarkupButtonsTexts result = new ReplyMarkupButtonsTexts();
            result.readParams(stream, exception);
            return result;
        }

        public void readParams(AbstractSerializedData stream, boolean exception) {
            int magic = stream.readInt32(exception);
            if (magic != 0x1cb5c415) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int a = 0; a < count; a++) {
                int subMagic = stream.readInt32(exception);
                if (subMagic != 0x1cb5c415) {
                    if (exception) {
                        throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                    }
                    return;
                }
                int subCount = stream.readInt32(exception);
                texts.add(new ArrayList<>());
                for (int b = 0; b < subCount; b++) {
                    String text = stream.readString(exception);
                    texts.get(a).add(text);
                }
            }
        }

        public ArrayList<ArrayList<String>> getTexts() {
            return texts;
        }

        public ReplyMarkupButtonsTexts copy() {
            ReplyMarkupButtonsTexts replyMarkupButtonsTexts = new ReplyMarkupButtonsTexts();
            ArrayList<ArrayList<String>> textsNew = new ArrayList<>();
            for (int a = 0; a < texts.size(); a++) {
                textsNew.add(new ArrayList<>(texts.get(a)));
            }
            replyMarkupButtonsTexts.texts.addAll(textsNew);
            return replyMarkupButtonsTexts;
        }

        public void applyTextToKeyboard(ArrayList<TLRPC.TL_keyboardButtonRow> rows) {
            for (int a = 0; a < rows.size(); a++) {
                ArrayList<TLRPC.KeyboardButton> buttonRow = rows.get(a).buttons;
                ArrayList<String> row = texts.get(a);
                for (int b = 0; b < buttonRow.size(); b++) {
                    TLRPC.KeyboardButton button2 = buttonRow.get(b);
                    button2.text = row.get(b);
                }
            }
        }
    }

    public void addMessageToClipboard(MessageObject selectedObject, Runnable callback) {
        String path = getPathToMessage(selectedObject);
        if (!TextUtils.isEmpty(path)) {
            addFileToClipboard(new File(path), callback);
        }
    }

    public static void addFileToClipboard(File file, Runnable callback) {
        try {
            Context context = ApplicationLoader.applicationContext;
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            Uri uri = FileProvider.getUriForFile(context, ApplicationLoader.getApplicationId() + ".provider", file);
            ClipData clip = ClipData.newUri(context.getContentResolver(), "label", uri);
            clipboard.setPrimaryClip(clip);
            callback.run();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static boolean canSendAsDice(CharSequence text, ChatActivity parentFragment, long dialog_id) {
        boolean canSendGames = true;
        if (DialogObject.isChatDialog(dialog_id)) {
            TLRPC.Chat chat = parentFragment.getMessagesController().getChat(-dialog_id);
            canSendGames = ChatObject.canSendStickers(chat);
        }
        boolean containsGame = parentFragment.getMessagesController().diceEmojies.contains(text.toString().replace("\ufe0f", ""));
        boolean containsSpans = false;
        if (text instanceof Editable) {
            containsSpans = Arrays.stream(((Editable) text).getSpans(0, text.length(), Object.class))
                    .anyMatch(span -> span instanceof TextStyleSpan || span instanceof AnimatedEmojiSpan ||
                            span instanceof URLSpanReplacement || span instanceof URLSpanUserMention);
        }
        return canSendGames && containsGame && !containsSpans;
    }

    public static File getFileFromMessage(MessageObject selectedObject) {
        File locFile = null;
        if (!TextUtils.isEmpty(selectedObject.messageOwner.attachPath)) {
            File f = new File(selectedObject.messageOwner.attachPath);
            if (f.exists()) {
                locFile = f;
            }
        }
        if (locFile == null) {
            File f = FileLoader.getInstance(UserConfig.selectedAccount).getPathToMessage(selectedObject.messageOwner);
            if (f.exists()) {
                locFile = f;
            }
        }
        return locFile;
    }
}
