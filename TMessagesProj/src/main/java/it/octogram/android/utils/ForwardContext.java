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

    static void setForwardParams(boolean noQuote, boolean noCaption) {
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