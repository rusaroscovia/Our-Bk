package com.bk.service;

import java.util.Locale;

final class NameNormalizer {

    private NameNormalizer() {
    }

    static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
