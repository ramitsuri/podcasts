package com.ramitsuri.podcasts.utils

interface LanguageHelper {
    fun getAvailableLanguages(): List<String>

    fun getLanguageCodesForLanguages(languages: List<String>): List<String>

    fun getDefaultLanguages(): List<String>
}
