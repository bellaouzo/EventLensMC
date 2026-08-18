package dev.bellaouzo.eventlens.setup.ui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@FunctionalInterface
interface SimpleDocumentListener extends DocumentListener {

    void changed();

    static SimpleDocumentListener on(Runnable action) {
        return action::run;
    }

    @Override
    default void insertUpdate(DocumentEvent event) {
        changed();
    }

    @Override
    default void removeUpdate(DocumentEvent event) {
        changed();
    }

    @Override
    default void changedUpdate(DocumentEvent event) {
        changed();
    }
}
