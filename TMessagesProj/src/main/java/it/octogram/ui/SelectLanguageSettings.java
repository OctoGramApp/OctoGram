package it.octogram.ui;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextRadioCell;

import java.util.ArrayList;

import it.octogram.android.OctoConfig;
import it.octogram.android.translator.BaseTranslator;
import it.octogram.android.translator.Translator;

public class SelectLanguageSettings extends BaseSettingsActivity {

    private int languageHeaderRow;
    private int languagesStartRow;
    private ArrayList<CharSequence> names;
    private ArrayList<String> targetLanguages;
    private boolean searchWas;

    @Override
    public boolean onFragmentCreate() {
        fixMostImportantLanguages(loadLanguages().getCurrentAppLanguage());
        return super.onFragmentCreate();
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("TranslationLanguage", R.string.TranslationLanguage);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position != languageHeaderRow) {
            String selectedLanguage = targetLanguages.get(position - languagesStartRow);
            OctoConfig.setTranslationTarget(selectedLanguage);
            finishFragment();
        }
    }

    @Override
    protected ActionBarMenuItem createMenuItem() {
        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem item = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {

            @Override
            public void onSearchCollapse() {
                searchWas = false;
                fixMostImportantLanguages(loadLanguages().getCurrentAppLanguage());
                emptyView.setVisibility(View.GONE);
                updateRowsId();
            }

            @Override
            public void onTextChanged(EditText editText) {
                String text = editText.getText().toString();
                if (text.length() != 0) {
                    searchWas = true;
                    getLanguagesFiltered(text);
                    emptyView.setVisibility(targetLanguages.size() == 0 ? View.VISIBLE : View.GONE);
                } else {
                    searchWas = false;
                    fixMostImportantLanguages(loadLanguages().getCurrentAppLanguage());
                    emptyView.setVisibility(View.GONE);
                }
                updateRowsId();
            }
        });
        item.setSearchFieldHint(LocaleController.getString("Search", R.string.Search));
        return item;
    }

    @Override
    protected boolean haveEmptyView() {
        return true;
    }

    private BaseTranslator loadLanguages() {
        BaseTranslator translator = Translator.getCurrentTranslator();
        targetLanguages = new ArrayList<>(translator.getTargetLanguages());
        names = new ArrayList<>();
        for (String language : targetLanguages) {
            names.add(getLanguage(language));
        }
        AndroidUtilities.selectionSort(names, targetLanguages);
        return translator;
    }

    private void fixMostImportantLanguages(String appLanguage) {
        int indexLangEn = targetLanguages.indexOf("en");
        names.add(0, names.get(indexLangEn));
        targetLanguages.add(0, targetLanguages.get(indexLangEn));
        names.remove(indexLangEn + 1);
        targetLanguages.remove(indexLangEn + 1);
        int indexLangClient = targetLanguages.indexOf(appLanguage);
        if (indexLangClient != -1) {
            names.add(0, names.get(indexLangClient));
            targetLanguages.add(0, targetLanguages.get(indexLangClient));
            names.remove(indexLangClient + 1);
            targetLanguages.remove(indexLangClient + 1);
        }
        targetLanguages.add(0, "app");
        names.add(0, getLanguage(appLanguage) + " - " + LocaleController.getString("Default", R.string.Default));

        if (!OctoConfig.translationTarget.equals("app")) {
            int indexLangSelected = targetLanguages.indexOf(OctoConfig.translationTarget);
            names.add(0, names.get(indexLangSelected));
            targetLanguages.add(0, targetLanguages.get(indexLangSelected));
            names.remove(indexLangSelected + 1);
            targetLanguages.remove(indexLangSelected + 1);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    protected void updateRowsId() {
        super.updateRowsId();
        languageHeaderRow = -1;
        if (!searchWas) {
            languageHeaderRow = rowCount++;
        }
        languagesStartRow = rowCount;
        rowCount += targetLanguages.size();

        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected BaseListAdapter createAdapter() {
        return new ListAdapter();
    }

    private void getLanguagesFiltered(String filter) {
        filter = filter.toLowerCase().trim();
        fixMostImportantLanguages(loadLanguages().getCurrentAppLanguage());
        ArrayList<String> targetLanguagesTemp = new ArrayList<>();
        ArrayList<CharSequence> namesTemp = new ArrayList<>();
        for (int i = 0; i < targetLanguages.size(); i++) {
            String[] languages = names.get(i).toString().split(" - ");
            if ((languages.length > 2 ? languages[2] : languages[1]).toLowerCase().contains(filter) || languages[0].toLowerCase().contains(filter)) {
                targetLanguagesTemp.add(targetLanguages.get(i));
                namesTemp.add(names.get(i));
            }
        }
        targetLanguages = targetLanguagesTemp;
        names = namesTemp;
    }

    private class ListAdapter extends BaseListAdapter {

        @Override
        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            switch (ViewType.fromInt(holder.getItemViewType())) {
                case TEXT_RADIO:
                    TextRadioCell textRadioCell = (TextRadioCell) holder.itemView;
                    String[] languages = names.get(position - languagesStartRow).toString().split(" - ");
                    boolean isSelectedLanguage = OctoConfig.translationTarget.equals(targetLanguages.get(position - languagesStartRow));
                    textRadioCell.setTextAndValueAndCheck(
                            languages.length > 2 ? languages[2] : languages[1],
                            languages[0],
                            isSelectedLanguage,
                            false,
                            true
                    );
                    break;
                case HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == languageHeaderRow) {
                        headerCell.setText(LocaleController.getString("Language", R.string.Language));
                    }
                    break;
            }
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.TEXT_RADIO;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position == languageHeaderRow) {
                return ViewType.HEADER;
            }
            return ViewType.TEXT_RADIO;
        }
    }
}