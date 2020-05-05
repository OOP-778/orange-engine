package com.oop.orangeengine.message;

import com.oop.orangeengine.message.impl.OActionBarMessage;
import com.oop.orangeengine.message.impl.OChatMessage;
import com.oop.orangeengine.message.impl.OTitleMessage;

public interface OMessage<T extends OMessage> extends Replaceable<T>, Cloneable, Sendable {
    static OChatMessage newChatMessage() {
        return new OChatMessage();
    }

    static OTitleMessage newTitleMessage() {
        return new OTitleMessage();
    }

    static OActionBarMessage newActionBarMessage() {
        return new OActionBarMessage();
    }

    String[] raw();

    T clone();

    MessageType type();

    default OChatMessage asChat() {
        return (OChatMessage) this;
    }

    default OTitleMessage asTitle() {
        return (OTitleMessage) this;
    }

    default OActionBarMessage asActionBar() {
        return (OActionBarMessage) this;
    }
}
