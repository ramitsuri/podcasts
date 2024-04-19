package com.ramitsuri.podcasts.model

enum class EpisodeSortOrder(val key: Int) {
    DATE_PUBLISHED_DESC(0),
    DATE_PUBLISHED_ASC(1),
    ;

    companion object {
        fun fromKey(key: Int): EpisodeSortOrder {
            return entries.firstOrNull { it.key == key } ?: DATE_PUBLISHED_DESC
        }
    }
}
