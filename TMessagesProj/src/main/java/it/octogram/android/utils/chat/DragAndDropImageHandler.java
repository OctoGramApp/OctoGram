/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.chat;

import static org.telegram.messenger.LocaleController.formatString;

import android.content.ClipData;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.view.DragEvent;
import android.webkit.MimeTypeMap;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.VideoEditedInfo;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.PhotoViewer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import it.octogram.android.logs.OctoLogging;

/**
 * @noinspection SequencedCollectionMethodCanBeUsed
 */
public class DragAndDropImageHandler {
    private static final String TAG = "DragAndDropImageHandler";
    private final ChatActivityEnterView chatActivityEnterView;
    private final ChatActivityEnterView.ChatActivityEditTextCaption messageEditText;
    private final ChatActivity parentFragment;
    private final Theme.ResourcesProvider resourcesProvider;
    private final Context context;
    private final long payStars;
    private ArrayList<File> imageFiles;
    private final ChatActivityEnterView.ChatActivityEnterViewDelegate delegate;

    public DragAndDropImageHandler(
            ChatActivityEnterView chatActivityEnterView,
            ChatActivityEnterView.ChatActivityEditTextCaption messageEditText,
            ChatActivity parentFragment,
            Theme.ResourcesProvider resourcesProvider,
            Context context,
            ChatActivityEnterView.ChatActivityEnterViewDelegate delegate,
            long payStars
    ) {
        this.chatActivityEnterView = chatActivityEnterView;
        this.messageEditText = messageEditText;
        this.parentFragment = parentFragment;
        this.resourcesProvider = resourcesProvider;
        this.context = context;
        this.delegate = delegate;
        this.payStars = payStars;
    }

    public boolean handleDragEvent(DragEvent event) {
        if (event.getAction() == DragEvent.ACTION_DROP && !chatActivityEnterView.isEditingBusinessLink() && !chatActivityEnterView.isEditingCaption() && !chatActivityEnterView.isEditingMessage()) {
            ClipData d = event.getClipData();
            if (d != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (LaunchActivity.instance != null) {
                    LaunchActivity.instance.requestDragAndDropPermissions(event);
                }
                int MAX_DRAG_AND_DROP_FILE_COUNT = 4;

                if (d.getItemCount() > MAX_DRAG_AND_DROP_FILE_COUNT) {
                    var toast = BulletinFactory.of(parentFragment).createSimpleBulletin(
                            R.raw.info,
                            formatString(R.string.MaxDragAndDropFileCount, MAX_DRAG_AND_DROP_FILE_COUNT)
                    );
                    toast.show();
                    return true;
                }

                ArrayList<Uri> imageUris = new ArrayList<>();

                for (int i = 0; i < d.getItemCount() && i < MAX_DRAG_AND_DROP_FILE_COUNT; i++) {
                    ClipData.Item item = d.getItemAt(i);
                    if (d.getDescription().hasMimeType("image/*") || d.getDescription().hasMimeType("video/mp4")) {
                        imageUris.add(item.getUri());
                    }
                }

                if (!imageUris.isEmpty()) {
                    editPhoto(imageUris, d.getDescription().getMimeType(0));
                }
                return true;
            }
        }
        return false;
    }

    private void editPhoto(ArrayList<Uri> uris, String mime) {
        if (uris == null || uris.isEmpty()) {
            return;
        }

        imageFiles = new ArrayList<>();
        Utilities.globalQueue.postRunnable(() -> {
            try {
                for (Uri uri : uris) {
                    File file = AndroidUtilities.generatePicturePath(parentFragment != null && parentFragment.isSecretChat(), MimeTypeMap.getSingleton().getExtensionFromMimeType(mime));
                    InputStream in = context.getContentResolver().openInputStream(uri);
                    if (in != null) {
                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        int lengthRead;
                        while ((lengthRead = in.read(buffer)) > 0) {
                            fos.write(buffer, 0, lengthRead);
                            fos.flush();
                        }
                        in.close();
                        fos.close();
                        imageFiles.add(file);
                    }
                }

                ArrayList<MediaController.PhotoEntry> photoEntries = new ArrayList<>();
                for (File file : imageFiles) {
                    MediaController.PhotoEntry photoEntry = new MediaController.PhotoEntry(0, -1, 0, file.getAbsolutePath(), 0, false, 0, 0, 0);
                    photoEntries.add(photoEntry);
                }

                AndroidUtilities.runOnUIThread(() -> openPhotoViewerForEdit(photoEntries, imageFiles.get(0)));
            } catch (Throwable e) {
                OctoLogging.d(TAG, "Error editing photo: " + e.getMessage());
            }
        });
    }

    private void openPhotoViewerForEdit(ArrayList<MediaController.PhotoEntry> photoEntries, File sourceFile) {
        if (parentFragment == null || parentFragment.getParentActivity() == null) {
            return;
        }
        if (chatActivityEnterView.isKeyboardVisible()) {
            AndroidUtilities.hideKeyboard(messageEditText);
            AndroidUtilities.runOnUIThread(() -> openPhotoViewerForEdit(photoEntries, sourceFile), 100);
            return;
        }

        PhotoViewer.getInstance().setParentActivity(parentFragment, resourcesProvider);
        PhotoViewer.getInstance().openPhotoForSelect(convertToArrayListOfObjects(photoEntries), 0, 2, false, new PhotoViewer.EmptyPhotoViewerProvider() {
            boolean sending;

            @Override
            public void sendButtonPressed(int index, VideoEditedInfo videoEditedInfo, boolean notify, int scheduleDate, boolean forceDocument) {
                if (chatActivityEnterView.replyingQuote != null && chatActivityEnterView.replyingQuote.outdated) {
                    parentFragment.showQuoteMessageUpdate();
                    return;
                }
                ArrayList<SendMessagesHelper.SendingMediaInfo> photos = new ArrayList<>();
                for (MediaController.PhotoEntry photoEntry : photoEntries) {
                    SendMessagesHelper.SendingMediaInfo info = new SendMessagesHelper.SendingMediaInfo();
                    if (!photoEntry.isVideo && photoEntry.imagePath != null) {
                        info.path = photoEntry.imagePath;
                    } else if (photoEntry.path != null) {
                        info.path = photoEntry.path;
                    }
                    info.thumbPath = photoEntry.thumbPath;
                    info.isVideo = photoEntry.isVideo;
                    info.caption = photoEntry.caption != null ? photoEntry.caption.toString() : null;
                    info.entities = photoEntry.entities;
                    info.masks = photoEntry.stickers;
                    info.ttl = photoEntry.ttl;
                    info.videoEditedInfo = videoEditedInfo;
                    info.canDeleteAfter = true;
                    photos.add(info);
                    photoEntry.reset();
                }
                sending = true;
                boolean updateStickersOrder = SendMessagesHelper.checkUpdateStickersOrder(photos.get(0).caption);
                SendMessagesHelper.prepareSendingMedia(
                        chatActivityEnterView.accountInstance,
                        photos,
                        chatActivityEnterView.dialog_id,
                        chatActivityEnterView.getReplyingMessageObject(),
                        chatActivityEnterView.getThreadMessage(),
                        null,
                        chatActivityEnterView.replyingQuote,
                        false,
                        true,
                        chatActivityEnterView.getEditingMessageObject(),
                        notify,
                        scheduleDate,
                        parentFragment.getChatMode(),
                        updateStickersOrder,
                        null,
                        messageEditText.getQuickReplyShortcut(),
                        parentFragment.getQuickReplyId(),
                        0,
                        false,
                        0,
                        0,
                        null
                );
                if (delegate != null) delegate.onMessageSend(null, true, scheduleDate, 0);
            }

            @Override
            public void willHidePhotoViewer() {
                if (!sending) {
                    for (File file : imageFiles) {
                        try {
                            if (file.delete()) {
                                OctoLogging.d(TAG, "File deleted: " + file.getAbsolutePath());
                            }
                        } catch (Throwable e) {
                            OctoLogging.d(TAG, "Error deleting file: " + e.getMessage());
                        }
                    }
                }
            }

            @Override
            public boolean canCaptureMorePhotos() {
                return false;
            }
        }, parentFragment);
    }

    private ArrayList<Object> convertToArrayListOfObjects(ArrayList<MediaController.PhotoEntry> photoEntries) {
        return new ArrayList<>(photoEntries);
    }

}