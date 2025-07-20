/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui;

import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.content.DialogInterface;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ItemOptions;

import java.util.HashMap;
import java.util.List;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.app.OctoPreferences;
import it.octogram.android.app.PreferenceType;
import it.octogram.android.app.PreferencesEntry;
import it.octogram.android.app.fragment.PreferencesFragment;
import it.octogram.android.app.rows.impl.CustomAIModelRow;
import it.octogram.android.app.rows.impl.FooterInformativeRow;
import it.octogram.android.app.rows.impl.ShadowRow;
import it.octogram.android.app.rows.impl.SwitchRow;
import it.octogram.android.app.rows.impl.TextDetailRow;
import it.octogram.android.app.rows.impl.TextIconRow;
import it.octogram.android.utils.ai.CustomModelsHelper;
import it.octogram.android.utils.ai.ui.GenerateModelBottomSheet;
import it.octogram.android.utils.appearance.MessageStringHelper;
import it.octogram.android.utils.chat.FileShareHelper;
import it.octogram.android.utils.deeplink.DeepLinkDef;

public class OctoChatsAiFeaturesUI implements PreferencesEntry {
    private final HashMap<String, CustomAIModelRow> modelsAssoc = new HashMap<>();
    private FooterInformativeRow footerInformativeRow;

    private PreferencesFragment fragment;
    private Context context;

    private final List<ConfigProperty<Boolean>> features = List.of(
            OctoConfig.INSTANCE.aiFeaturesTranslateMessages,
            OctoConfig.INSTANCE.aiFeaturesChatContext,
            OctoConfig.INSTANCE.aiFeaturesAskOnMedia
    );

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        this.fragment = fragment;
        this.context = context;

        if (OctoConfig.INSTANCE.aiFeatures.getValue() && !OctoConfig.INSTANCE.aiFeaturesAcceptedTerms.getValue()) {
            OctoConfig.INSTANCE.aiFeaturesAcceptedTerms.updateValue(true);
        }

        modelsAssoc.clear();

        return OctoPreferences.builder(getString(R.string.AiFeatures_Brief))
                .deepLink(DeepLinkDef.AI_FEATURES)
                .row(new SwitchRow.SwitchRowBuilder()
                        .isMainPageAction(true)
                        .onClick(() -> {
                            if (OctoConfig.INSTANCE.aiFeaturesAcceptedTerms.getValue() || OctoConfig.INSTANCE.aiFeatures.getValue()) {
                                return true;
                            }

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(fragment.getParentActivity());
                            alertDialogBuilder.setTitle(getString(R.string.AiFeatures_EnableOptionsTerms));
                            alertDialogBuilder.setMessage(getString(R.string.AiFeatures_EnableOptionsTerms_Desc));
                            alertDialogBuilder.setPositiveButton(getString(R.string.AiFeatures_EnableOptionsTerms_Accept), (dialog, which) -> {
                                OctoConfig.INSTANCE.aiFeaturesAcceptedTerms.updateValue(true);
                                OctoConfig.INSTANCE.aiFeatures.updateValue(true);
                                fragment.reloadUIAfterValueUpdate();
                            });
                            alertDialogBuilder.setNegativeButton(getString(R.string.Cancel), null);
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                            return false;
                        })
                        .preferenceValue(OctoConfig.INSTANCE.aiFeatures)
                        .title(getString(R.string.AiFeatures))
                        .build()
                )
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.TRANSLATOR_GEMINI, true, getString(R.string.AiFeatures_Desc_Brief))
                .category(getString(R.string.AiFeatures_Features), OctoConfig.INSTANCE.aiFeatures, category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> handleSwitch(OctoConfig.INSTANCE.aiFeaturesTranslateMessages))
                            .preferenceValue(OctoConfig.INSTANCE.aiFeaturesTranslateMessages)
                            .title(getString(R.string.TranslateMessages))
                            .showIf(OctoConfig.INSTANCE.aiFeatures)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> handleSwitch(OctoConfig.INSTANCE.aiFeaturesChatContext))
                            .preferenceValue(OctoConfig.INSTANCE.aiFeaturesChatContext)
                            .title(getString(R.string.AiFeatures_Features_ChatContext))
                            .showIf(OctoConfig.INSTANCE.aiFeatures)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> handleSwitch(OctoConfig.INSTANCE.aiFeaturesAskOnMedia))
                            .preferenceValue(OctoConfig.INSTANCE.aiFeaturesAskOnMedia)
                            .title(getString(R.string.AiFeatures_Features_AskOnPhoto))
                            .showIf(OctoConfig.INSTANCE.aiFeatures)
                            .build());
                })
                .row(new TextDetailRow.TextDetailRowBuilder()
                        .propertySelectionTag("providers")
                        .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoChatsAiProvidersUI())))
                        .icon(R.drawable.msg_payment_provider)
                        .title(getString(R.string.TranslatorProvider))
                        .description(getString(R.string.AiFeatures_AccessVia_Desc))
                        .showIf(OctoConfig.INSTANCE.aiFeatures)
                        .build()
                )
                .row(new ShadowRow())
                .categoryWithoutShadow(getString(R.string.AiFeatures_CustomModels), OctoConfig.INSTANCE.aiFeatures, category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .isBlue(true)
                            .onClick(() -> openModel(null))
                            .propertySelectionTag("createModel")
                            .icon(R.drawable.msg_add)
                            .showIf(OctoConfig.INSTANCE.aiFeatures)
                            .title(getString(R.string.AiFeatures_CustomModels_Create))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .isBlue(true)
                            .onClick(this::openGenerateModel)
                            .propertySelectionTag("generateModel")
                            .icon(R.drawable.aifeatures_solar)
                            .showIf(OctoConfig.INSTANCE.aiFeatures)
                            .title(getString(R.string.AiFeatures_CustomModels_Generate) + " (beta)")
                            .build());

                    HashMap<String, CustomModelsHelper.CustomModel> modelsList = CustomModelsHelper.getModelsList();
                    for (String modelID : modelsList.keySet()) {
                        CustomAIModelRow customCell = createModelCell(modelID);
                        category.row(customCell);
                        modelsAssoc.put(modelID, customCell);
                    }
                })
                .row(footerInformativeRow = new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(MessageStringHelper.getUrlNoUnderlineText(
                                new SpannableString(
                                        MessageStringHelper.fromHtml(
                                                formatString(
                                                        R.string.AiFeatures_CustomModels_Desc,
                                                        "<a href='https://t.me/" + OctoConfig.AI_EXAMPLE_CHANNEL_TAG + "'>@" + OctoConfig.AI_EXAMPLE_CHANNEL_TAG + "</a>"
                                                )
                                        )
                                )
                        ))
                        .showIf(OctoConfig.INSTANCE.aiFeatures)
                        .build())
                .build();
    }

    private boolean handleSwitch(ConfigProperty<Boolean> currentProperty) {
        if (currentProperty.getValue() && canDisableProperty(currentProperty)) {
            return true;
        } else {
            return !currentProperty.getValue();
        }
    }

    private boolean canDisableProperty(ConfigProperty<Boolean> currentProperty) {
        if (!currentProperty.getValue()) {
            return false;
        }

        if (features.contains(currentProperty) && !areThereEnabledFeatures(currentProperty)) {
            BulletinFactory.of(fragment).createSimpleBulletin(R.raw.chats_infotip, getString(R.string.AiFeatures_Features_Empty), getString(R.string.AiFeatures_AccessVia_Empty_Disable), () -> {
                OctoConfig.INSTANCE.aiFeatures.updateValue(false);
                fragment.reloadUIAfterValueUpdate();
            }).show();
            return false;
        }

        return true;
    }

    private boolean areThereEnabledFeatures(ConfigProperty<Boolean> currentProperty) {
        for (ConfigProperty<Boolean> property : features) {
            if (currentProperty != property && property.getValue()) {
                return true;
            }
        }

        return false;
    }

    private void openGenerateModel() {
        new GenerateModelBottomSheet(context, (model) -> {
            OctoChatsAiNewModelUI newModelUI = new OctoChatsAiNewModelUI();
            newModelUI.setCallback(new OctoChatsAiNewModelUI.ModelCallback() {
                @Override
                public void onCreated(String modelID) {
                    updateUiAddingModel(modelID);
                }

                @Override
                public void onEdited(String modelID) {
                    updateUiModifyingModel();
                }
            });
            newModelUI.setCurrentModel(model, false);
            fragment.presentFragment(new PreferencesFragment(newModelUI));
        }).show();
    }

    private void openModel(String modelID) {
        openModel(modelID, false);
    }

    private void openModel(String modelID, boolean asClone) {
        OctoChatsAiNewModelUI newModelUI = new OctoChatsAiNewModelUI();
        newModelUI.setCallback(new OctoChatsAiNewModelUI.ModelCallback() {
            @Override
            public void onCreated(String modelID) {
                updateUiAddingModel(modelID);
            }

            @Override
            public void onEdited(String modelID) {
                updateUiModifyingModel();
            }
        });
        if (modelID != null) {
            if (asClone) {
                CustomModelsHelper.CustomModel model = CustomModelsHelper.getModelById(modelID);
                if (model != null) {
                    newModelUI.setCurrentModel(model, false);
                }
            } else {
                newModelUI.setCurrentModelId(modelID);
            }
        }
        fragment.presentFragment(new PreferencesFragment(newModelUI));
    }

    private void showModelOptions(View v, String modelID) {
        ItemOptions options = ItemOptions.makeOptions(fragment, v);
        options.add(R.drawable.msg_edit, getString(R.string.AiFeatures_CustomModels_Model_Edit), () -> openModel(modelID));
        options.add(R.drawable.msg_copy, getString(R.string.AiFeatures_CustomModels_Model_Clone), () -> openModel(modelID, true));
        options.add(R.drawable.msg_share, getString(R.string.AiFeatures_CustomModels_Model_Share), () -> shareModel(modelID));
        options.add(R.drawable.msg_delete, getString(R.string.AiFeatures_CustomModels_Model_Delete), true, () -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(fragment.getParentActivity());
            alertDialogBuilder.setTitle(getString(R.string.AiFeatures_CustomModels_Model_Delete));
            alertDialogBuilder.setMessage(getString(R.string.AiFeatures_CustomModels_Model_Delete_Alert));
            alertDialogBuilder.setPositiveButton(getString(R.string.AiFeatures_CustomModels_Model_Delete_Alert_Sure), (dialog, which) -> {
                dialog.dismiss();
                CustomModelsHelper.deleteModel(modelID);
                updateUiRemovingModel(modelID);
            });
            alertDialogBuilder.setNegativeButton(getString(R.string.Cancel), null);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            ((TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)).setTextColor(Theme.getColor(Theme.key_text_RedBold));
        });
        if (LocaleController.isRTL) {
            options.setGravity(Gravity.LEFT);
        }
        options.show();
    }

    private CustomAIModelRow createModelCell(String modelID) {
        return new CustomAIModelRow.CustomAIModelRowBuilder()
                .modelID(modelID)
                .onClick(() -> openModel(modelID))
                .onShowOptions(v -> showModelOptions(v, modelID))
                .showIf(OctoConfig.INSTANCE.aiFeatures)
                .build();
    }

    private void updateUiAddingModel(String modelID) {
        if (modelsAssoc.containsKey(modelID)) {
            return;
        }

        CustomAIModelRow customCell = createModelCell(modelID);
        modelsAssoc.put(modelID, customCell);
        fragment.insertRow(customCell, footerInformativeRow, true);
    }

    private void updateUiModifyingModel() {
        AndroidUtilities.runOnUIThread(() -> fragment.notifyItemChanged(PreferenceType.CUSTOM_AI_MODEL.getAdapterType()), 100);
    }

    private void updateUiRemovingModel(String modelID) {
        if (modelsAssoc.containsKey(modelID)) {
            fragment.deleteRow(modelsAssoc.get(modelID));
            modelsAssoc.remove(modelID);
        }
    }

    private void shareModel(String modelID) {
        CustomModelsHelper.CustomModel model = CustomModelsHelper.getModelById(modelID);
        if (model == null) {
            return;
        }

        String fileName = modelID;
        if (!model.title.contains("/") && !model.title.contains(".") && !model.title.contains("\\")) {
            fileName = model.title;
        }

        JSONObject mainObject = model.convertAsObject();

        FileShareHelper.FileShareData data = new FileShareHelper.FileShareData();
        data.fileName = fileName;
        data.fileExtension = OctoConfig.OCTOMODEL_EXTENSION;
        data.fileContent = mainObject;
        data.fragment = fragment;
        data.caption = getString(R.string.AiFeatures_CustomModels_Model_Share_Text);
        data.delegate = new FileShareHelper.FileShareData.FileShareDelegate() {
            @Override
            public void onSuccess() {
                AndroidUtilities.runOnUIThread(() -> BulletinFactory.of(fragment).createSimpleBulletin(R.raw.forward, getString(R.string.ExportDataShareDone)).show());
            }
        };
        FileShareHelper.init(data);
    }
}
