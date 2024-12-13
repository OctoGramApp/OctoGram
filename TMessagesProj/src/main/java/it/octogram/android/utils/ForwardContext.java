/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.utils;

import org.telegram.messenger.MessageObject;

import java.util.ArrayList;

public interface ForwardContext {
    ForwardParams forwardParams = new ForwardParams();

    ArrayList<MessageObject> getForwardingMessages();

    default boolean forceShowScheduleAndSound() {
        return false;
    }

    default ForwardParams getForwardParams() {
        return forwardParams;
    }

    default void setForwardParams(boolean noQuote, boolean noCaption) {
        forwardParams.noQuote = noQuote;
        forwardParams.noCaption = noCaption;
        forwardParams.notify = true;
        forwardParams.scheduleDate = 0;
    }

    default void setForwardParams(boolean noQuote) {
        forwardParams.noQuote = noQuote;
        forwardParams.noCaption = false;
        forwardParams.notify = true;
        forwardParams.scheduleDate = 0;
    }

    class ForwardParams {
        public boolean noQuote = false;
        public boolean noCaption = false;
        public boolean notify = true;
        public int scheduleDate = 0;
    }
}