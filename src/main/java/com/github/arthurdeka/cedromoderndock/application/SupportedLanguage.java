package com.github.arthurdeka.cedromoderndock.application;

import java.util.Locale;

public enum SupportedLanguage {
    EN_US("en", "US", "English"),
    ZH_CN("zh", "CN", "\u7B80\u4F53\u4E2D\u6587"),
    ZH_TW("zh", "TW", "\u7E41\u9AD4\u4E2D\u6587"),
    HI_IN("hi", "IN", "\u0939\u093F\u0928\u094D\u0926\u0940"),
    ES_ES("es", "ES", "Espa\u00F1ol"),
    FR_FR("fr", "FR", "Fran\u00E7ais"),
    AR_SA("ar", "SA", "\u0627\u0644\u0639\u0631\u0628\u064A\u0629"),
    BN_BD("bn", "BD", "\u09AC\u09BE\u0982\u09B2\u09BE"),
    PT_BR("pt", "BR", "Portugu\u00EAs (Brasil)"),
    RU_RU("ru", "RU", "\u0420\u0443\u0441\u0441\u043A\u0438\u0439"),
    UR_PK("ur", "PK", "\u0627\u0631\u062F\u0648"),
    ID_ID("id", "ID", "Bahasa Indonesia"),
    DE_DE("de", "DE", "Deutsch"),
    JA_JP("ja", "JP", "\u65E5\u672C\u8A9E"),
    PCM_NG("pcm", "NG", "Naij\u00E1"),
    MR_IN("mr", "IN", "\u092E\u0930\u093E\u0920\u0940"),
    TE_IN("te", "IN", "\u0C24\u0C46\u0C32\u0C41\u0C17\u0C41"),
    TR_TR("tr", "TR", "T\u00FCrk\u00E7e"),
    TA_IN("ta", "IN", "\u0BA4\u0BAE\u0BBF\u0BB4\u0BCD"),
    YUE_HK("yue", "HK", "\u7CB5\u8A9E"),
    VI_VN("vi", "VN", "Ti\u1EBFng Vi\u1EC7t");

    private final Locale locale;
    private final String nativeDisplayName;

    SupportedLanguage(String language, String country, String nativeDisplayName) {
        this.locale = Locale.of(language, country);
        this.nativeDisplayName = nativeDisplayName;
    }

    public Locale locale() {
        return locale;
    }

    public String nativeDisplayName() {
        return nativeDisplayName;
    }
}
