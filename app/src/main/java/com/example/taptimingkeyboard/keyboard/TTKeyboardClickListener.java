package com.example.taptimingkeyboard.keyboard;

/**
 * Used by {@link TapTimingKeyboard} to send click events
 */
public interface TTKeyboardClickListener {
    /**
     * @param ttButton clicked button
     * @param clickId unique (for TapTimingKeyboard instance) click id
     */
    void onKeyboardClick(TTKeyboardButton ttButton, long clickId);
}
