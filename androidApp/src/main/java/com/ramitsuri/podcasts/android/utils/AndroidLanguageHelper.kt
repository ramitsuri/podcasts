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

    override fun getLanguageCodesForLanguages(languages: List<String>): List<String> {
        val default = Locale.getDefault()
        return Locale
            .getAvailableLocales()
            .filter { it.displayLanguage in languages }
            .flatMap {
                // This is quite naive but will have to do for now, since the Podcast Index service
                // seems to have a hidden limit of how many languages it'll take in and also it won't
                // return for all `en-` language variations if you only send `en`. So need to send the
                // most important ones
                listOf(it.language, "${it.language}-${default.country}")
            }
    }

    override fun getDefaultLanguages(): List<String> {
        return listOf("English")
    }
}
