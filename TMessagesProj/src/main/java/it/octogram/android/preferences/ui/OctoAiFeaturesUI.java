/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui;

import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;
import static it.octogram.android.OctoConfig.TAG;

import android.content.Context;
import android.content.DialogInterface;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.ShareAlert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.ai.GenerateModelBottomSheet;
import it.octogram.android.ai.helper.CustomModelsHelper;
import it.octogram.android.deeplink.DeepLinkDef;
import it.octogram.android.logs.OctoLogging;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.CustomAIModelRow;
import it.octogram.android.preferences.rows.impl.FooterInformativeRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.preferences.ui.custom.AiConfigBottomSheet;
import it.octogram.android.utils.appearance.MessageStringHelper;

public class OctoAiFeaturesUI implements PreferencesEntry, NotificationCenter.NotificationCenterDelegate {
    private final HashMap<String, CustomAIModelRow> modelsAssoc = new HashMap<>();
    private FooterInformativeRow footerInformativeRow;

    private String sharingFullLocation;
    private String sharingFileName;
    private LongSparseArray<TLRPC.Dialog> sharingDid = new LongSparseArray<>();

    private final ConfigProperty<Boolean> showNoProviderAlert = new ConfigProperty<>(null, false);
    private PreferencesFragment fragment;
    private Context context;

    private final List<ConfigProperty<Boolean>> providers = List.of(
            OctoConfig.INSTANCE.aiFeaturesUseGoogleAPIs,
            OctoConfig.INSTANCE.aiFeaturesUseChatGPTAPIs,
            OctoConfig.INSTANCE.aiFeaturesUseOpenRouterAPIs
    );

    private final List<ConfigProperty<Boolean>> features = List.of(
            OctoConfig.INSTANCE.aiFeaturesTranslateMessages,
            OctoConfig.INSTANCE.aiFeaturesChatContext
    );

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        this.fragment = fragment;
        this.context = context;

        showNoProviderAlert.updateValue(!areThereEnabledProviders());
        modelsAssoc.clear();

        return OctoPreferences.builder(getString(R.string.AiFeatures_Brief))
                .deepLink(DeepLinkDef.AI_FEATURES)
                .row(new SwitchRow.SwitchRowBuilder()
                        .isMainPageAction(true)
                        .preferenceValue(OctoConfig.INSTANCE.aiFeatures)
                        .title(getString(R.string.AiFeatures))
                        .build()
                )
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.TRANSLATOR_GEMINI, true, getString(R.string.AiFeatures_Desc_Brief))
                .category(getString(R.string.AiFeatures_AccessVia), OctoConfig.INSTANCE.aiFeatures, category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> handleSwitch(OctoConfig.INSTANCE.aiFeaturesUseGoogleAPIs))
                            .preferenceValue(OctoConfig.INSTANCE.aiFeaturesUseGoogleAPIs)
                            .title(getString(R.string.AiFeatures_AccessVia_GoogleAPI))
                            .description(getString(R.string.AiFeatures_AccessVia_ReqAuth))
                            .showIf(OctoConfig.INSTANCE.aiFeatures)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> handleSwitch(OctoConfig.INSTANCE.aiFeaturesUseChatGPTAPIs))
                            .preferenceValue(OctoConfig.INSTANCE.aiFeaturesUseChatGPTAPIs)
                            .title(getString(R.string.AiFeatures_AccessVia_ChatGPTAPI))
                            .description(getString(R.string.AiFeatures_AccessVia_ReqAuth))
                            .showIf(OctoConfig.INSTANCE.aiFeatures)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> handleSwitch(OctoConfig.INSTANCE.aiFeaturesUseOpenRouterAPIs))
                            .preferenceValue(OctoConfig.INSTANCE.aiFeaturesUseOpenRouterAPIs)
                            .title(getString(R.string.AiFeatures_AccessVia_OpenRouterAPI))
                            .description(getString(R.string.AiFeatures_AccessVia_ReqAuth))
                            .showIf(OctoConfig.INSTANCE.aiFeatures)
                            .build());
                })
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(getString(R.string.AiFeatures_AccessVia_Empty))
                        .showIf(showNoProviderAlert)
                        .build()
                )
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
                })
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
                            .title(getString(R.string.AiFeatures_CustomModels_Generate))
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

    @Override
    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (!isOpen) {
            stopUpdateReceiver();
        }
    }

    private boolean handleSwitch(ConfigProperty<Boolean> currentProperty) {
        if (providers.contains(currentProperty)) {
            openPropertyConfig(currentProperty);
        } else {
            if (currentProperty.getValue() && canDisableProperty(currentProperty)) {
                return true;
            } else {
                return !currentProperty.getValue();
            }
        }

        return false;
    }

    private boolean canDisableProperty(ConfigProperty<Boolean> currentProperty) {
        if (!currentProperty.getValue()) {
            return false;
        }

        if (features.contains(currentProperty) && !areThereEnabledFeatures(currentProperty)) {
            BulletinFactory.of(fragment).createSimpleBulletin(R.raw.chats_infotip, getString(R.string.AiFeatures_Features_Empty), getString(R.string.AiFeatures_AccessVia_Empty_Disable), () -> {
                OctoConfig.INSTANCE.aiFeatures.updateValue(false);
                showNoProviderAlert.updateValue(!areThereEnabledProviders());
                fragment.reloadUIAfterValueUpdate();
            }).show();
            return false;
        }

        return true;
    }

    private void openPropertyConfig(ConfigProperty<Boolean> property) {
        new AiConfigBottomSheet(context, fragment, new AiConfigBottomSheet.AiConfigInterface() {
            @Override
            public ConfigProperty<Boolean> getProperty() {
                return property;
            }

            @Override
            public void onStateUpdated() {
                showNoProviderAlert.updateValue(!areThereEnabledProviders());
                fragment.reloadUIAfterValueUpdate();
            }

            @Override
            public boolean canShowSuccessBulletin() {
                return areThereEnabledProviders();
            }
        }).show();
    }

    private boolean areThereEnabledProviders() {
        return areThereEnabledStates(null, true);
    }

    private boolean areThereEnabledFeatures(ConfigProperty<Boolean> currentProperty) {
        return areThereEnabledStates(currentProperty, false);
    }

    private boolean areThereEnabledStates(ConfigProperty<Boolean> currentProperty, boolean isProvider) {
        for (ConfigProperty<Boolean> property : (isProvider ? providers : features)) {
            if (currentProperty != property && property.getValue()) {
                return true;
            }
        }

        return false;
    }

    private void openGenerateModel() {
        new GenerateModelBottomSheet(context, (model) -> {
            OctoAiNewModelUI newModelUI = new OctoAiNewModelUI();
            newModelUI.setCallback(new OctoAiNewModelUI.ModelCallback() {
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
        OctoAiNewModelUI newModelUI = new OctoAiNewModelUI();
        newModelUI.setCallback(new OctoAiNewModelUI.ModelCallback() {
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
        try {
            CustomModelsHelper.CustomModel model = CustomModelsHelper.getModelById(modelID);
            if (model == null) {
                return;
            }

            String fileName = modelID;
            if (!model.title.contains("/") && !model.title.contains(".") && !model.title.contains("\\")) {
                fileName = model.title;
            }

            JSONObject mainObject = model.convertAsObject();

            File cacheDir = AndroidUtilities.getCacheDir();
            File cacheFile = new File(cacheDir.getPath(), fileName+OctoConfig.OCTOMODEL_EXTENSION);
            if (cacheFile.exists()) {
                if (cacheFile.delete()) {
                    OctoLogging.d(TAG, "Deleted existing file");
                }
            }

            FileOutputStream fos = new FileOutputStream(cacheFile);
            var charset = StandardCharsets.UTF_8.name();
            if (BuildConfig.DEBUG) {
                fos.write(mainObject.toString(4).getBytes(charset));
            } else {
                fos.write(mainObject.toString().getBytes());
            }
            fos.close();

            sharingFileName = cacheFile.getName();
            sharingFullLocation = cacheFile.getAbsolutePath();
            FileLoader instance = FileLoader.getInstance(UserConfig.selectedAccount);

            ShareAlert shAlert = new ShareAlert(fragment.getParentActivity(), null, null, false, null, false, true) {

                @Override
                protected void onSend(LongSparseArray<TLRPC.Dialog> did, int count, TLRPC.TL_forumTopic topic, boolean showToast) {
                    sharingDid = did;
                    if (!showToast) return;
                    super.onSend(sharingDid, count, topic, true);

                    initUpdateReceiver();
                    instance.uploadFile(cacheFile.getPath(), false, true, ConnectionsManager.FileTypeFile);
                }
            };
            fragment.showDialog(shAlert);
        } catch (JSONException e) {
            OctoLogging.e(getClass().getName(), "Error sharing model export", e);
        } catch (IOException e) {
            OctoLogging.e(getClass().getName(), "Error generating model export", e);
        }
    }

    private void initUpdateReceiver() {
        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(this, NotificationCenter.fileUploaded);
        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(this, NotificationCenter.fileUploadFailed);
    }

    private void stopUpdateReceiver() {
        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(this, NotificationCenter.fileUploaded);
        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(this, NotificationCenter.fileUploadFailed);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.fileUploaded) {
            String location = (String) args[0];
            TLRPC.InputFile inputFile = (TLRPC.InputFile) args[1];

            if (inputFile == null) {
                return;
            }

            if (!Objects.equals(location, sharingFullLocation)) {
                return;
            }

            stopUpdateReceiver();

            if (fragment != null) {
                AndroidUtilities.runOnUIThread(() -> BulletinFactory.of(fragment).createSimpleBulletin(R.raw.forward, getString(R.string.ExportDataShareDone)).show());
            }

            TLRPC.TL_documentAttributeFilename attr = new TLRPC.TL_documentAttributeFilename();
            attr.file_name = sharingFileName;

            TLRPC.TL_inputMediaUploadedDocument inputMediaDocument = new TLRPC.TL_inputMediaUploadedDocument();
            inputMediaDocument.file = inputFile;
            inputMediaDocument.attributes.add(attr);
            inputMediaDocument.mime_type = OctoConfig.EXPORT_BACKUP_MIME_TYPE;

            for (int i = 0; i < sharingDid.size(); i++) {
                TLRPC.TL_messages_sendMedia req = new TLRPC.TL_messages_sendMedia();
                req.peer = MessagesController.getInstance(fragment.getCurrentAccount()).getInputPeer(sharingDid.keyAt(i));
                req.random_id = SendMessagesHelper.getInstance(fragment.getCurrentAccount()).getNextRandomId();
                req.silent = false;
                req.invert_media = true;
                req.media = inputMediaDocument;
                ConnectionsManager.getInstance(fragment.getCurrentAccount()).sendRequest(req, null);
            }
        } else if (id == NotificationCenter.fileUploadFailed) {
            String location = (String) args[0];

            if (!Objects.equals(location, sharingFullLocation)) {
                return;
            }

            stopUpdateReceiver();
        }
    }
}
