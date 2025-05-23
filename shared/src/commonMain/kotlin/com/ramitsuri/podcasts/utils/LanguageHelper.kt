package com.ramitsuri.podcasts.utils

interface LanguageHelper {
    fun getAvailableLanguages(): List<String>

    fun getLanguageCodesForLanguage(language: String): List<String>
}
