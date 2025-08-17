/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.ButtonSpan;
import org.telegram.ui.Components.CodepointsLengthInputFilter;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.Components.OutlineEditText;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.TableView;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import it.octogram.android.AiModelMessagesState;
import it.octogram.android.AiModelType;
import it.octogram.android.ConfigProperty;
import it.octogram.android.ExpandableRowsIds;
import it.octogram.android.app.OctoPreferences;
import it.octogram.android.app.PreferenceType;
import it.octogram.android.app.PreferencesEntry;
import it.octogram.android.app.fragment.PreferencesFragment;
import it.octogram.android.app.rows.impl.CustomCellRow;
import it.octogram.android.app.rows.impl.ExpandableRows;
import it.octogram.android.app.rows.impl.FooterInformativeRow;
import it.octogram.android.app.rows.impl.ListRow;
import it.octogram.android.app.rows.impl.ShadowRow;
import it.octogram.android.app.rows.impl.SliderRow;
import it.octogram.android.app.rows.impl.SwitchRow;
import it.octogram.android.app.rows.impl.TextIconRow;
import it.octogram.android.app.ui.cells.IconStyleSelectorCell;
import it.octogram.android.utils.ai.CustomModelsHelper;
import it.octogram.android.utils.ai.icons.AiFeatureIcons;
import it.octogram.android.utils.appearance.PopupChoiceDialogOption;
import it.octogram.android.utils.config.ExpandableRowsOption;

public class OctoChatsAiNewModelUI implements PreferencesEntry {
    private final ConfigProperty<Integer> modelType = new ConfigProperty<>(null, AiModelType.RELATED_TO_MESSAGES.getId());
    private final ConfigProperty<Boolean> supportsMediaUpload = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> usesMediaUpload = new ConfigProperty<>(null, false);
    private final ConfigProperty<Integer> numberOfMessagesToPass = new ConfigProperty<>(null, CustomModelsHelper.DEFAULT_MESSAGES_TO_PASS);

    private final ConfigProperty<Boolean> isRelatedToMessages = new ConfigProperty<>(null, true);
    private final ConfigProperty<Boolean> isRelatedToInputBox = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> isRelatedToChats = new ConfigProperty<>(null, false);
    private final ConfigProperty<String> currentIcon = new ConfigProperty<>(null, "");

    private Context context;
    private PreferencesFragment fragment;
    private ModelCallback callback;

    private String currentModelId = null;
    private CustomModelsHelper.CustomModel currentModelData = null;
    private boolean isImportingModel = true;
    private OutlineEditText editTextTitle;
    private OutlineEditText editText;

    private final ArrayList<AiModelMessagesState> availableStates = new ArrayList<>();
    private final HashMap<AiModelMessagesState, ConfigProperty<Boolean>> dataMessagesAssocProperty = new HashMap<>();

    {
        availableStates.add(AiModelMessagesState.TEXT_MESSAGES);
        availableStates.add(AiModelMessagesState.PHOTOS);
        availableStates.add(AiModelMessagesState.STICKERS);
        availableStates.add(AiModelMessagesState.MUSIC);
        availableStates.add(AiModelMessagesState.VOICE_MESSAGES);
        availableStates.add(AiModelMessagesState.VIDEOS);
        availableStates.add(AiModelMessagesState.GIFS);
    }

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        this.context = context;
        this.fragment = fragment;

        CustomModelsHelper.CustomModel model = currentModelData != null ? currentModelData : currentModelId == null ? null : CustomModelsHelper.getModelById(currentModelId);
        if (model != null) {
            modelType.updateValue(model.modelType.getId());
            usesMediaUpload.updateValue(supportsMediaUpload(model.appearsInList) && model.uploadMedia);
            supportsMediaUpload.updateValue(supportsMediaUpload(model.appearsInList));
            numberOfMessagesToPass.updateValue(model.messagesToPass);
            isRelatedToMessages.updateValue(modelType.getValue().equals(AiModelType.RELATED_TO_MESSAGES.getId()));
            isRelatedToInputBox.updateValue(modelType.getValue().equals(AiModelType.RELATED_TO_INPUT.getId()));
            isRelatedToChats.updateValue(modelType.getValue().equals(AiModelType.RELATED_TO_CHATS.getId()));
            currentIcon.updateValue(model.icon);
        }

        ConfigProperty<Boolean> isImportingMode = new ConfigProperty<>(null, currentModelData != null && currentModelId == null && isImportingModel);

        int pageTitle = R.string.AiFeatures_CustomModels_Create_Short;
        if (model != null) {
            if (currentModelData != null && isImportingModel) {
                pageTitle = R.string.AiFeatures_CustomModels_Create_Short_Import;
            } else if (currentModelId != null) {
                pageTitle = R.string.AiFeatures_CustomModels_Create_Short_Edit;
            }
        }

        return OctoPreferences.builder(getString(pageTitle))
                .saveButtonAvailable(true)
                .category(getString(R.string.AiFeatures_CustomModels_Create_Details), category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder().layout(getPromptInput(true)).avoidReDraw(true).build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> {
                                IconStyleSelectorCell iconStyleSelectorCell = new IconStyleSelectorCell(context, true);
                                iconStyleSelectorCell.setOnItemClick(onItemClick -> {
                                    currentIcon.updateValue(onItemClick);
                                    iconStyleSelectorCell.dismiss();
                                    fragment.notifyItemChanged(PreferenceType.TEXT_ICON.getAdapterType());
                                    updateConfig();
                                });
                                iconStyleSelectorCell.show();
                            })
                            .setDynamicDataUpdate(new TextIconRow.OnDynamicDataUpdate() {
                                @Override
                                public String getTitle() {
                                    return getString(R.string.AiFeatures_CustomModels_Create_ModelIcon);
                                }

                                @Override
                                public String getValue() {
                                    return "";
                                }

                                @Override
                                public int getIcon() {
                                    return AiFeatureIcons.getModelIcon(currentIcon.getValue());
                                }
                            })
                            .title(getString(R.string.AiFeatures_CustomModels_Create_ModelIcon))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(modelType)
                            .onSelected(this::updateConfig)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(AiModelType.RELATED_TO_MESSAGES.getId())
                                            .setItemTitle(getString(R.string.AiFeatures_CustomModels_Create_ModelType_Messages))
                                            .setItemDescription(getString(R.string.AiFeatures_CustomModels_Create_ModelType_Messages_Desc)),
                                    new PopupChoiceDialogOption()
                                            .setId(AiModelType.RELATED_TO_INPUT.getId())
                                            .setItemTitle(getString(R.string.AiFeatures_CustomModels_Create_ModelType_InputBox))
                                            .setItemDescription(getString(R.string.AiFeatures_CustomModels_Create_ModelType_InputBox_Desc)),
                                    new PopupChoiceDialogOption()
                                            .setId(AiModelType.RELATED_TO_CHATS.getId())
                                            .setItemTitle(getString(R.string.AiFeatures_CustomModels_Create_ModelType_Chats))
                                            .setItemDescription(getString(R.string.AiFeatures_CustomModels_Create_ModelType_Chats_Desc))
                            ))
                            .icon(R.drawable.msg_customize)
                            .title(getString(R.string.AiFeatures_CustomModels_Create_ModelType))
                            .build());
                })
                .categoryWithoutShadow(getString(R.string.AiFeatures_CustomModels_Create_Prompt), category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder().layout(getPromptInput(false)).avoidReDraw(true).build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .isBlue(true)
                            .onClick(() -> new SuggestedHashtagsBottomSheet(context, true).show())
                            .icon(R.drawable.menu_hashtag)
                            .title(getString(R.string.AiFeatures_CustomModels_Create_AvailableTags))
                            .build());
                })
                .row(new ShadowRow(isImportingMode, true))
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(getString(R.string.AiFeatures_CustomModels_Create_Importing))
                        .showIf(isImportingMode)
                        .build()
                )
                .category(getString(R.string.AiFeatures_CustomModels_Create_OptionsHeader), category -> {
                    dataMessagesAssocProperty.clear();

                    ExpandableRows.ExpandableRowsBuilder expandedBuilder = new ExpandableRows.ExpandableRowsBuilder()
                            .setId(ExpandableRowsIds.AI_MODEL_OPTIONS_MESSAGES.getId())
                            .setIcon(R.drawable.msg_message_s)
                            .setMainTitle(getString(R.string.AiFeatures_CustomModels_Create_OptionsHeader_Where))
                            .hideMainSwitch(true)
                            .setOnSingleStateChange(this::updateConfig);

                    for (AiModelMessagesState state : availableStates) {
                        ConfigProperty<Boolean> tempProp = new ConfigProperty<>(null, model != null && model.appearsInList.contains(state));
                        dataMessagesAssocProperty.put(state, tempProp);
                        expandedBuilder.addRow(new ExpandableRowsOption()
                                .optionTitle(state.getStateName())
                                .property(tempProp)
                        );
                    }

                    category.row(expandedBuilder.showIf(isRelatedToMessages).build());

                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(this::updateConfig)
                            .preferenceValue(usesMediaUpload)
                            .title(getString(R.string.AiFeatures_CustomModels_Create_OptionsHeader_SupportsUpload))
                            .showIf(supportsMediaUpload)
                            .build());
                })
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(getString(R.string.AiFeatures_CustomModels_Create_OptionsHeader_SupportsUpload_Desc))
                        .showIf(supportsMediaUpload)
                        .build()
                )
                .category(getString(R.string.AiFeatures_CustomModels_Create_OptionsHeader_MessagesToPass), isRelatedToChats, category -> {
                    category.row(new SliderRow.SliderRowBuilder()
                            .min(5)
                            .max(100)
                            .onSelected(() -> fragment.notifyItemChanged(PreferenceType.FOOTER_INFORMATIVE.getAdapterType()))
                            .onTouchEnd(this::updateConfig)
                            .preferenceValue(numberOfMessagesToPass)
                            .showIf(isRelatedToChats)
                            .build()
                    );
                })
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .setDynamicDataUpdate(() -> formatString(R.string.AiFeatures_CustomModels_Create_ModelType_Chats_Detailed, numberOfMessagesToPass.getValue()))
                        .title(formatString(R.string.AiFeatures_CustomModels_Create_ModelType_Chats_Detailed, numberOfMessagesToPass.getValue()))
                        .showIf(isRelatedToChats)
                        .build()
                )
                .build();
    }

    @Override
    public void onFragmentCreate() {
        if (currentModelData != null && currentModelId == null) {
            fragment.updateDoneButtonVisibility(true, true);
        }
    }
    private boolean supportsMediaUpload(ArrayList<AiModelMessagesState> list) {
        if (!isRelatedToMessages.getValue()) {
            return false;
        }
        for (AiModelMessagesState state : list) {
            if (state != AiModelMessagesState.TEXT_MESSAGES) {
                return true;
            }
        }
        return false;
    }

    private void updateConfig() {
        isRelatedToMessages.updateValue(modelType.getValue().equals(AiModelType.RELATED_TO_MESSAGES.getId()));
        isRelatedToInputBox.updateValue(modelType.getValue().equals(AiModelType.RELATED_TO_INPUT.getId()));
        isRelatedToChats.updateValue(modelType.getValue().equals(AiModelType.RELATED_TO_CHATS.getId()));
        if (!isRelatedToMessages.getValue()) {
            supportsMediaUpload.updateValue(false);
        } else {
            boolean supportsMediaUploadState = false;
            for (AiModelMessagesState state : dataMessagesAssocProperty.keySet()) {
                if (Objects.requireNonNull(dataMessagesAssocProperty.get(state)).getValue() && state != AiModelMessagesState.TEXT_MESSAGES) {
                    supportsMediaUploadState = true;
                    break;
                }
            }
            supportsMediaUpload.updateValue(supportsMediaUploadState);
        }
        if (!supportsMediaUpload.getValue()) {
            usesMediaUpload.updateValue(false);
        }
        fragment.updateDoneButtonVisibility(hasChanges(), true);
    }

    public void setCallback(ModelCallback callback) {
        this.callback = callback;
    }

    public void setCurrentModelId(String currentModelId) {
        this.currentModelId = currentModelId;
    }

    public void setCurrentModel(CustomModelsHelper.CustomModel model, boolean isImportingModel) {
        this.currentModelData = model;
        this.isImportingModel = isImportingModel;
    }

    public void setCurrentModel(CustomModelsHelper.CustomModel model) {
        setCurrentModel(model, true);
    }

    public LinearLayout getPromptInput(boolean isTitle) {
        int inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT;
        if (!isTitle) {
            inputType = inputType | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
        }

        OutlineEditText currentElement = new OutlineEditText(context);
        currentElement.getEditText().setMinHeight(AndroidUtilities.dp(58));
        currentElement.getEditText().setInputType(inputType);
        //currentElement.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
        //currentElement.getEditText().setMaxLines(isTitle ? 1 : 10);
        currentElement.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editText == null || editTextTitle == null) {
                    return;
                }

                fragment.updateDoneButtonVisibility(hasChanges(), true);
            }
        });
        currentElement.setHint(getString(isTitle ? R.string.AiFeatures_CustomModels_Create_Title : R.string.AiFeatures_CustomModels_Create_YourPrompt));

        if (currentModelId != null || currentModelData != null) {
            CustomModelsHelper.CustomModel model = currentModelData != null ? currentModelData : CustomModelsHelper.getModelById(currentModelId);
            if (model != null) {
                currentElement.getEditText().setText(Emoji.replaceEmoji(isTitle ? model.title : model.prompt, currentElement.getEditText().getPaint().getFontMetricsInt(), true));
            }
        }

        if (isTitle) {
            editTextTitle = currentElement;

            InputFilter[] inputFilters = new InputFilter[1];
            inputFilters[0] = new CodepointsLengthInputFilter(40) {
                @Override
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dStart, int dEnd) {
                    if (source != null && !TextUtils.isEmpty(source) && TextUtils.indexOf(source, '\n') == source.length() - 1) {
                        return "";
                    }
                    CharSequence result = super.filter(source, start, end, dest, dStart, dEnd);
                    if (result != null && source != null && result.length() != source.length()) {
                        Vibrator v = (Vibrator) fragment.getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        if (v != null) {
                            v.vibrate(200);
                        }
                        AndroidUtilities.shakeView(currentElement);
                    }
                    return result;
                }
            };
            currentElement.getEditText().setFilters(inputFilters);
        } else {
            currentElement.getEditText().setPadding(
                    AndroidUtilities.dp(15), AndroidUtilities.dp(15), AndroidUtilities.dp(15), AndroidUtilities.dp(15)
            );
            editText = currentElement;
        }

        LinearLayout layout = new LinearLayout(context);
        layout.addView(currentElement, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 17, 10, 17, 10));
        return layout;
    }

    private class SuggestedHashtagsBottomSheet extends BottomSheet {

        public SuggestedHashtagsBottomSheet(Context context, boolean needFocus) {
            super(context, needFocus);

            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            RLottieImageView imageView = new RLottieImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setAnimation(R.raw.tag_icon_3, 70, 70);
            imageView.playAnimation();
            imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_featuredStickers_buttonText), PorterDuff.Mode.SRC_IN));
            imageView.setBackground(Theme.createCircleDrawable(dp(80), Theme.getColor(Theme.key_featuredStickers_addButton, resourcesProvider)));
            linearLayout.addView(imageView, LayoutHelper.createLinear(80, 80, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 16));

            LinkSpanDrawable.LinksTextView sheetTitle = new LinkSpanDrawable.LinksTextView(context);
            sheetTitle.setTextColor(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
            sheetTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            sheetTitle.setTypeface(AndroidUtilities.bold());
            sheetTitle.setGravity(Gravity.CENTER);
            sheetTitle.setText(getString(R.string.AiFeatures_CustomModels_Create_AvailableTags));
            linearLayout.addView(sheetTitle, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 24, 5, 24, 2));

            LinkSpanDrawable.LinksTextView sheetSubtitle = new LinkSpanDrawable.LinksTextView(context);
            sheetSubtitle.setTextColor(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
            sheetSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            sheetSubtitle.setGravity(Gravity.CENTER);
            sheetSubtitle.setLinkTextColor(Theme.getColor(Theme.key_chat_messageLinkIn, resourcesProvider));
            sheetSubtitle.setLineSpacing(dp(2), 1f);
            sheetSubtitle.setDisablePaddingsOffsetY(true);
            sheetSubtitle.setText(getString(R.string.AiFeatures_CustomModels_Create_AvailableTags_Clickable));
            linearLayout.addView(sheetSubtitle, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 24, 0, 24, 17));

            TableView tableView = new TableView(context, resourcesProvider);
            linearLayout.addView(tableView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 16, 0, 16, 0));

            int i = 0;
            List<Pair<String, String>> availableTags = CustomModelsHelper.getAvailableHashtags(modelType.getValue());
            for (Pair<String, String> state : availableTags) {
                insertPairIntoTableView(context, tableView, state, i == 0, i == availableTags.size() - 1, this::dismiss);
                i++;
            }

            if (i > 0) {
                SimpleTextView or = new SimpleTextView(context) {
                    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

                    @Override
                    protected void dispatchDraw(Canvas canvas) {
                        paint.setColor(Theme.getColor(Theme.key_dialogGrayLine, resourcesProvider));
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(1);
                        final float cy = getHeight() / 2f;
                        canvas.drawLine(0, cy, getWidth() / 2f - getTextWidth() / 2f - dp(8), cy, paint);
                        canvas.drawLine(getWidth() / 2f + getTextWidth() / 2f + dp(8), cy, getWidth(), cy, paint);

                        super.dispatchDraw(canvas);
                    }
                };
                or.setGravity(Gravity.CENTER);
                or.setAlignment(Layout.Alignment.ALIGN_CENTER);
                or.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2, resourcesProvider));
                or.setText(" " + getString(R.string.AiFeatures_CustomModels_Create_AvailableTags_Special).toLowerCase() + " ");
                or.setTextSize(14);
                linearLayout.addView(or, LayoutHelper.createLinear(270, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 12, 17, 12, 17));
            }

            tableView = new TableView(context, resourcesProvider);
            linearLayout.addView(tableView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 16, 0, 16, 0));

            i = 0;
            for (Pair<String, String> state : CustomModelsHelper.getSpecialHashtags()) {
                insertPairIntoTableView(context, tableView, state, i == 0, false, this::dismiss);
                i++;
            }

            tableView.addFullRow(getString(R.string.AiFeatures_CustomModels_Create_AvailableTags_Clickable_Special));

            FrameLayout buttonView = new FrameLayout(context);
            buttonView.setBackgroundColor(getThemedColor(Theme.key_dialogBackground));

            View buttonShadowView = new View(context);
            buttonShadowView.setBackgroundColor(getThemedColor(Theme.key_dialogShadowLine));
            buttonShadowView.setAlpha(0);
            buttonView.addView(buttonShadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.getShadowHeight() / dpf2(1), Gravity.TOP | Gravity.FILL_HORIZONTAL));

            TextView buttonTextView = new TextView(context);
            buttonTextView.setLines(1);
            buttonTextView.setSingleLine(true);
            buttonTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            buttonTextView.setEllipsize(TextUtils.TruncateAt.END);
            buttonTextView.setGravity(Gravity.CENTER);
            buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
            buttonTextView.setTypeface(AndroidUtilities.bold());
            buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            buttonTextView.setText(getString(R.string.AiFeatures_CustomModels_Create_AvailableTags_Close));
            buttonTextView.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_featuredStickers_addButton), 6));
            buttonTextView.setOnClickListener(e -> dismiss());
            buttonView.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 16, 16, 16, 16));

            linearLayout.addView(buttonView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));

            setCustomView(linearLayout);
        }
    }

    private void insertPairIntoTableView(Context context, TableView tableView, Pair<String, String> state, boolean isFirst, boolean isLast, Runnable dismiss) {
        TableView.TableRowTitle hashtagView = new TableView.TableRowTitle(tableView, "#" + state.first);
        hashtagView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MONO));
        hashtagView.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_table_background), isFirst ? 6 : 0, 0, 0, isLast ? 6 : 0));
        hashtagView.setOnClickListener(v -> {
            dismiss.run();
            editText.getEditText().setText(MessageFormat.format("{0} #{1}", editText.getEditText().getText().toString(), state.first));
            editText.invalidate();
        });

        ButtonSpan.TextViewButtons textView = new ButtonSpan.TextViewButtons(context);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setText(state.second);

        TableRow row = new TableRow(context);
        TableRow.LayoutParams lp;
        lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        row.addView(hashtagView, lp);
        lp = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        row.addView(new TableView.TableRowContent(tableView, textView), lp);
        tableView.addView(row);
    }

    @Override
    public void onSaveButtonPressed() {
        onSaveButtonPressed(false);
    }

    public void onSaveButtonPressed(boolean forced) {
        if (currentModelId == null && currentModelData != null && !forced) {
            HashMap<String, CustomModelsHelper.CustomModel> models = CustomModelsHelper.getModelsList();
            boolean hasValidClone = false;
            for (CustomModelsHelper.CustomModel model : models.values()) {
                if (model.modelType != currentModelData.modelType) {
                    continue;
                }
                if (model.prompt.trim().equalsIgnoreCase(currentModelData.prompt.trim()) || model.title.trim().equalsIgnoreCase(currentModelData.title.trim())) {
                    hasValidClone = true;
                    break;
                }
            }
            if (hasValidClone) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, null);
                builder.setTitle(getString(R.string.AiFeatures_CustomModels_Create_Duplicate));
                String text = getString(R.string.AiFeatures_CustomModels_Create_Duplicate_Desc);
                builder.setMessage(text);
                builder.setPositiveButton(getString(R.string.AiFeatures_CustomModels_Create_Duplicate_Import), (dialogInterface, i) -> onSaveButtonPressed(true));
                builder.setNegativeButton(getString(R.string.Discard), (dialogInterface, i) -> fragment.finishFragment());

                AlertDialog dialog = builder.show();
                TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (button != null) {
                    button.setTextColor(Theme.getColor(Theme.key_text_RedBold));
                }
                return;
            }
        }

        ArrayList<AiModelMessagesState> appearsInList = new ArrayList<>();
        for (AiModelMessagesState state : dataMessagesAssocProperty.keySet()) {
            if (Objects.requireNonNull(dataMessagesAssocProperty.get(state)).getValue()) {
                appearsInList.add(state);
            }
        }

        CustomModelsHelper.CustomModel modelData = currentModelId == null ? null : CustomModelsHelper.getModelById(currentModelId);
        if (modelData == null) {
            currentModelId = CustomModelsHelper.createNewModel(
                    editTextTitle.getEditText().getText().toString().trim(),
                    currentIcon.getValue(),
                    editText.getEditText().getText().toString().trim(),
                    modelType.getValue(),
                    appearsInList,
                    numberOfMessagesToPass.getValue(),
                    usesMediaUpload.getValue()
            );
        } else {
            currentModelId = CustomModelsHelper.updateModel(
                    currentModelId,
                    editTextTitle.getEditText().getText().toString().trim(),
                    currentIcon.getValue(),
                    editText.getEditText().getText().toString().trim(),
                    modelType.getValue(),
                    appearsInList,
                    numberOfMessagesToPass.getValue(),
                    usesMediaUpload.getValue()
            );
        }

        fragment.finishFragment();

        if (callback != null) {
            if (modelData == null) {
                callback.onCreated(currentModelId);
            } else {
                callback.onEdited(currentModelId);
            }
        }
    }

    private boolean hasChanges() {
        if (editText.getEditText().getText().toString().trim().isEmpty()) {
            return false;
        }

        boolean hasChanges = false;

        CustomModelsHelper.CustomModel modelData = currentModelId == null ? null : CustomModelsHelper.getModelById(currentModelId);

        if (modelData == null) {
            if (!editTextTitle.getEditText().getText().toString().trim().isEmpty() || !editText.getEditText().getText().toString().trim().isEmpty() || !currentIcon.getValue().isEmpty()) {
                hasChanges = true;
            }

            if (modelType.getValue() == AiModelType.RELATED_TO_MESSAGES.getId()) {
                if (supportsMediaUpload.getValue() && usesMediaUpload.getValue()) {
                    hasChanges = true;
                }

                for (AiModelMessagesState state : dataMessagesAssocProperty.keySet()) {
                    if (Objects.requireNonNull(dataMessagesAssocProperty.get(state)).getValue()) {
                        hasChanges = true;
                    }
                }
            } else {
                hasChanges = true; // has changes model type from default type to different one
            }

            if (modelType.getValue() == AiModelType.RELATED_TO_CHATS.getId()) {
                if (numberOfMessagesToPass.getValue() != CustomModelsHelper.DEFAULT_MESSAGES_TO_PASS) {
                    hasChanges = true;
                }
            }
        } else {
            if (!modelData.title.trim().equals(editTextTitle.getEditText().getText().toString().trim())) {
                hasChanges = true;
            }

            if (!modelData.icon.trim().equals(currentIcon.getValue())) {
                hasChanges = true;
            }

            if (!modelData.prompt.trim().equals(editText.getEditText().getText().toString().trim())) {
                hasChanges = true;
            }

            if (!Objects.equals(modelData.modelType.getId(), modelType.getValue())) {
                hasChanges = true;
            }

            if (modelType.getValue() == AiModelType.RELATED_TO_MESSAGES.getId()) {
                if (supportsMediaUpload.getValue() && modelData.uploadMedia != usesMediaUpload.getValue()) {
                    hasChanges = true;
                }

                for (AiModelMessagesState state : dataMessagesAssocProperty.keySet()) {
                    if (Objects.requireNonNull(dataMessagesAssocProperty.get(state)).getValue() && !modelData.appearsInList.contains(state)) {
                        hasChanges = true;
                        break;
                    }
                }

                for (AiModelMessagesState state : modelData.appearsInList) {
                    if (dataMessagesAssocProperty.containsKey(state) && !Objects.requireNonNull(dataMessagesAssocProperty.get(state)).getValue()) {
                        hasChanges = true;
                        break;
                    }
                }
            }

            if (modelType.getValue() == AiModelType.RELATED_TO_CHATS.getId()) {
                if (modelData.messagesToPass != numberOfMessagesToPass.getValue()) {
                    hasChanges = true;
                }
            }
        }

        return hasChanges;
    }

    @Override
    public boolean canBeginSlide() {
        boolean hasChanges = hasChanges();

        if (hasChanges) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, null);
            builder.setTitle(getString(R.string.UnsavedChanges));
            String text = getString(R.string.AiFeatures_CustomModels_Create_EditedElements);
            builder.setMessage(text);
            builder.setPositiveButton(getString(R.string.ApplyTheme), (dialogInterface, i) -> onSaveButtonPressed());
            builder.setNegativeButton(getString(R.string.Discard), (dialogInterface, i) -> fragment.finishFragment());

            AlertDialog dialog = builder.show();
            TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(Theme.getColor(Theme.key_text_RedBold));
            }
        }

        return !hasChanges;
    }

    public interface ModelCallback {
        void onCreated(String modelID);

        void onEdited(String modelID);
    }
}
