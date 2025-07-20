/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.chat;

import java.util.ArrayList;
import java.util.HashMap;

public class ContextMenuScanHelper {
    public static final ArrayList<CharSequence> items = new ArrayList<>();
    public static final ArrayList<Integer> options = new ArrayList<>();
    public static final ArrayList<Integer> icons = new ArrayList<>();

    static {
    }

    public static final ArrayList<Integer> pinnedOptions = new ArrayList<>();
    static {
    }

    public static final HashMap<Integer, String> pinnedOptionsNameReplacements = new HashMap<>();
    static {
    }

    public static final ArrayList<SubCategories> subcategories = new ArrayList<>();
    static {
    }

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
            return this;
        }

        public int getCategoryIcon() {
            return categoryIcon;
        }

        public String getCategoryName() {
            return "";
        }

        public ArrayList<Integer> getCategoryOptions() {
            return new ArrayList<>();
        }
    }
}
