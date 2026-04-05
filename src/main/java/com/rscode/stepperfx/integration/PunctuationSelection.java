package com.rscode.stepperfx.integration;

/**
 * Possible user preferences for punctuation.<br><br>
 *
 * Possible values:<br>
 * - {@code REMOVE_ALL_PUNCTUATION}: Use no punctuation<br>
 * - {@code REMOVE_SPACES}: Use no spaces, keep other punctuation<br>
 * - {@code USE_PUNCTUATION}: Include all punctuation
 */
public enum PunctuationSelection {
    REMOVE_ALL_PUNCTUATION,
    REMOVE_SPACES,
    USE_PUNCTUATION;
}
