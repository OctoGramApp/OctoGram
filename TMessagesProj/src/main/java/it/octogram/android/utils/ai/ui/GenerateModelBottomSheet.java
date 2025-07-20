/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.ai.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.runOnUIThread;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.Color;
import android.os.Vibrator;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.OutlineEditText;
import org.telegram.ui.Components.StickerImageView;
import org.telegram.ui.LaunchActivity;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import it.octogram.android.OctoColors;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.utils.ai.AiPrompt;
import it.octogram.android.utils.ai.CustomModelsHelper;
import it.octogram.android.utils.ai.MainAiHelper;

public class GenerateModelBottomSheet extends BottomSheet {
    private final Context context;

    public GenerateModelBottomSheet(Context context, Consumer<CustomModelsHelper.CustomModel> consumer) {
        super(context, true);
        this.context = context;

        setApplyBottomPadding(false);
        setApplyTopPadding(false);
        fixNavigationBar(getThemedColor(Theme.key_windowBackgroundWhite));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        StickerImageView imageView = new StickerImageView(context, UserConfig.selectedAccount);
        imageView.setStickerPackName(OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME);
        imageView.setStickerNum(StickerUi.NEW_MODEL_GENERATION.getValue());
        imageView.getImageReceiver().setAutoRepeat(1);
        linearLayout.addView(imageView, LayoutHelper.createLinear(144, 144, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 16));

        TextView textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        textView.setTypeface(AndroidUtilities.bold());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(getString(R.string.AiFeatures_CustomModels_Generate));
        textView.setPadding(dp(30), 0, dp(30), 0);
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        textView = new TextView(context);
        textView.setTextColor(ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_dialogTextBlack), 150));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(getString(R.string.AiFeatures_CustomModels_Generate_Desc));
        textView.setPadding(dp(30), dp(10), dp(30), dp(21));
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        OutlineEditText currentElement = new OutlineEditText(context);
        currentElement.getEditText().setMinHeight(AndroidUtilities.dp(58));
        currentElement.getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        currentElement.getEditText().setMaxLines(10);
        currentElement.getEditText().setPadding(AndroidUtilities.dp(15), AndroidUtilities.dp(15), AndroidUtilities.dp(15), AndroidUtilities.dp(15));
        currentElement.setHint(getString(R.string.AiFeatures_CustomModels_Generate_Input));
        currentElement.updateColorAsDefined(Color.parseColor(OctoColors.AiColor.getValue()));
        currentElement.getEditText().setCursorColor(Color.parseColor(OctoColors.AiColor.getValue()));
        linearLayout.addView(currentElement, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 17, 10, 17, 10));

        TextView buttonTextView = new TextView(context);
        buttonTextView.setPadding(dp(34), 0, dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(AndroidUtilities.bold());
        buttonTextView.setText(getString(R.string.AiFeatures_CustomModels_Generate_Button));
        buttonTextView.setTextColor(Color.WHITE);
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(dp(6), Color.parseColor(OctoColors.AiColor.getValue()), ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundWhite), 120)));
        buttonTextView.setOnClickListener(view -> {
            AlertDialog progressDialog = new AlertDialog(LaunchActivity.instance, AlertDialog.ALERT_TYPE_SPINNER);
            progressDialog.setCanCancel(false);
            progressDialog.show();
            generate(currentElement, progressDialog, consumer);
        });
        linearLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 15, 16, 8));

        setCustomView(linearLayout);
    }

    private AiPrompt getPrompt(OutlineEditText currentElement) {
        SpannableStringBuilder sb = new SpannableStringBuilder()
                .append("You are an expert in creating high-quality AI prompt instructions. ")
                .append("Your task is to create optimal prompt instructions for a Telegram client with AI functions.\n\n")
                .append("The prompts must be in ENGLISH regardless of the user's input language.\n\n")
                .append("The prompts should give clear instructions to the AI on how to process the content.\n\n")
                .append("Every prompt must:\n")
                .append("1. Use the available tags (#chat_title, #chat_username, #chat_description, #chat_members_count, #message_text) appropriately\n")
                .append("2. Specify that responses must be in the user's #language\n")
                .append("3. Request markdown formatting for better readability\n")
                .append("4. Specify that content must have a width suitable for phone screens\n")
                .append("5. Be clear, concise and specific in instructions\n\n")
                .append("Titles should be concise and WITHOUT emojis. The prompt must be optimized for immediate use without further improvements.");

        SpannableStringBuilder sb2 = new SpannableStringBuilder()
                .append("Create a high-quality prompt based on this idea: \"").append(currentElement.getEditText().getText().toString().trim()).append("\"\n\n")
                .append("The prompt MUST BE IN ENGLISH and should:\n")
                .append("- Provide clear instructions to the AI on how to process the input\n")
                .append("- Specify that the response must be in #language\n")
                .append("- Request markdown formatting optimized for phones\n")
                .append("- Use appropriate tags (#message_text, etc.)\n\n")
                .append("Important formatting requirements to include:\n")
                .append("1. Use markdown formatting (headers, bold, italic)\n")
                .append("2. Keep width narrow for phone screens\n\n")
                .append("Respond in JSON format with these fields:\n")
                .append("- title: concise title WITHOUT emojis (max 30 chars)\n")
                .append("- prompt: the complete, well-structured prompt in ENGLISH\n")
                .append("- appearsInList: empty array (I'll determine this automatically)\n")
                .append("- uploadMedia: boolean (I'll determine this automatically)");

        return new AiPrompt(sb.toString(), sb2.toString());
    }

    private void generate(OutlineEditText currentElement, AlertDialog progressDialog, Consumer<CustomModelsHelper.CustomModel> consumer) {
        if (currentElement.getEditText().getText().toString().trim().length() < 4) {
            progressDialog.dismiss();
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(200);
            }
            AndroidUtilities.shakeView(currentElement);
            return;
        }

        MainAiHelper.request(getPrompt(currentElement), MainAiHelper.getPreferredProvider(), new MainAiHelper.OnResultState() {
            @Override
            public void onSuccess(String result) {
                AndroidUtilities.hideKeyboard(currentElement.getEditText());
                handleResponse(result, (v) -> {
                    progressDialog.dismiss();
                    dismiss();
                    consumer.accept(v);
                }, this::onFailed);
            }

            @Override
            public void onFailed() {
                runOnUIThread(() -> {
                    progressDialog.dismiss();
                    BulletinFactory.of((FrameLayout) containerView, resourcesProvider).createErrorBulletin(getString(R.string.AiFeatures_CustomModels_Generate_Failed)).show();
                });
            }

            @Override
            public void onTooManyRequests() {
                runOnUIThread(() -> {
                    progressDialog.dismiss();
                    BulletinFactory.of((FrameLayout) containerView, resourcesProvider).createErrorBulletin(getString((R.string.FloodWait))).show();
                });
            }

            @Override
            public void onModelOverloaded() {
                runOnUIThread(() -> {
                    progressDialog.dismiss();
                    BulletinFactory.of((FrameLayout) containerView, resourcesProvider).createErrorBulletin(getString((R.string.AiFeatures_CustomModels_Feature_Failed_Overloaded))).show();
                });
            }

        });
    }

    private void handleResponse(String result, Consumer<CustomModelsHelper.CustomModel> consumer, Runnable onFailed) {
        try {
            int start = result.indexOf('{');
            int end = result.lastIndexOf('}') + 1;
            if (start != -1 && end != 0 && end > start) {
                String jsonString = result.substring(start, end);
                JSONObject object = new JSONObject(new JSONTokener(jsonString));

                String lowerPrompt = object.getString("prompt").toLowerCase();
                JSONArray messageTypes = determineMessageTypes(lowerPrompt);

                object.remove("appearsInList");
                object.put("appearsInList", messageTypes);
                object.put("uploadMedia", determineShouldUploadMedia(lowerPrompt, messageTypes));

                if (CustomModelsHelper.isValidModel(object)) {
                    CustomModelsHelper.CustomModel model = new CustomModelsHelper.CustomModel();
                    model.saveFromJsonObject(object);
                    consumer.accept(model);
                } else {
                    onFailed.run();
                }
            }
        } catch (JSONException ignored) {
            onFailed.run();
        }
    }

    @NonNull
    private JSONArray determineMessageTypes(String lowerPrompt) {
        JSONArray messageTypes = new JSONArray();

        if (containsAny(lowerPrompt, "#message_text", "text", "message", "analyze", "content")) {
            messageTypes.put("msg");
        }

        if (containsAny(lowerPrompt, "photo", "image", "picture", "visual", "analyze image")) {
            messageTypes.put("pts");
        }

        if (lowerPrompt.contains("sticker")) {
            messageTypes.put("sts");
        }

        if (containsAny(lowerPrompt, "music", "audio", "song", "sound")) {
            messageTypes.put("msc");
        }

        if (containsAny(lowerPrompt, "voice", "speech", "spoken", "vocal")) {
            messageTypes.put("vms");
        }

        if (containsAny(lowerPrompt, "video", "movie", "clip", "film", "footage")) {
            messageTypes.put("vid");
        }

        if (containsAny(lowerPrompt, "gif", "animation", "animated")) {
            messageTypes.put("gif");
        }

        if (messageTypes.length() == 0) {
            messageTypes.put("msg");
        }
        return messageTypes;
    }

    private boolean determineShouldUploadMedia(String prompt, JSONArray messageTypes) {
        List<String> mediaTypes = Arrays.asList("pts", "msc", "vms", "vid", "gif");
        boolean hasMediaTypes = false;
        for (int i = 0; i < messageTypes.length(); i++) {
            try {
                if (mediaTypes.contains(messageTypes.getString(i))) {
                    hasMediaTypes = true;
                    break;
                }
            } catch (JSONException ignored) {
            }
        }
        String lowerPrompt = prompt.toLowerCase();
        boolean mentionsMedia = lowerPrompt.contains("photo") ||
                lowerPrompt.contains("image") ||
                lowerPrompt.contains("audio") ||
                lowerPrompt.contains("music") ||
                lowerPrompt.contains("voice") ||
                lowerPrompt.contains("video") ||
                lowerPrompt.contains("gif");

        return hasMediaTypes || mentionsMedia;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
