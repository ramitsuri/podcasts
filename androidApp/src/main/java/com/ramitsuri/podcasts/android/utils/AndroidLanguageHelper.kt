package com.ramitsuri.podcasts.android.utils

import com.ramitsuri.podcasts.utils.LanguageHelper
import java.util.Locale

class AndroidLanguageHelper : LanguageHelper {
    override fun getAvailableLanguages(): List<String> {
        return Locale
            .getAvailableLocales()
            .map { it.displayLanguage }
            .distinct()
            .sorted()
    }

    override fun getLanguageCodesForLanguage(language: String): List<String> {
        return Locale
            .getAvailableLocales()
            .filter { it.displayLanguage == language }
            .map { "${it.language}-${it.country}" }
    }
}
