package it.octogram.android.utils.chat;

import static org.telegram.messenger.LocaleController.getString;

import org.telegram.messenger.R;
import org.telegram.ui.ChatActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class ContextMenuScanHelper {
    public static final ArrayList<CharSequence> items = new ArrayList<>();
    public static final ArrayList<Integer> options = new ArrayList<>();
    public static final ArrayList<Integer> icons = new ArrayList<>();

    public static final ArrayList<Integer> pinnedOptions = new ArrayList<>();
    public static final HashMap<Integer, String> pinnedOptionsNameReplacements = new HashMap<>();
    public static final ArrayList<SubCategories> subcategories = new ArrayList<>();

    public static class SubCategories {
        private int categoryIcon;
        private int categoryName;
        private final ArrayList<Integer> categoryOptions = new ArrayList<>();

        public static SubCategories instance() {
            return new SubCategories();
        }

        public SubCategories setCategoryIcon(int categoryIcon) {
            this.categoryIcon = categoryIcon;
            return this;
        }

        public SubCategories setCategoryName(int categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public SubCategories addCategoryOption(int option) {
            if (!categoryOptions.contains(option)) {
                categoryOptions.add(option);
            }
            return this;
        }

        public int getCategoryIcon() {
            return categoryIcon;
        }

        public String getCategoryName() {
            return getString(categoryName);
        }

        public ArrayList<Integer> getCategoryOptions() {
            return categoryOptions;
        }
    }
}
