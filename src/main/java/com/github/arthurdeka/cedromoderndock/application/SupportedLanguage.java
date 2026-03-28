package com.github.arthurdeka.cedromoderndock.application;

import java.util.Locale;

public enum SupportedLanguage {
    EN_US("en", "US", "language.english", "English"),
    PT_BR("pt", "BR", "language.portugueseBrazil", "Portugu\u00EAs (Brasil)");

    private final Locale locale;
    private final String displayKey;
    private final String nativeDisplayName;

    SupportedLanguage(String language, String country, String displayKey, String nativeDisplayName) {
        this.locale = Locale.of(language, country);
        this.displayKey = displayKey;
        this.nativeDisplayName = nativeDisplayName;
    }

    public Locale locale() {
        return locale;
    }

    public String displayKey() {
        return displayKey;
    }

    public String nativeDisplayName() {
        return nativeDisplayName;
    }
}
