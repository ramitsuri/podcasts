package com.ramitsuri.podcasts.model

enum class EpisodeSortOrder(val key: Long) {
    DATE_PUBLISHED_DESC(0),
    DATE_PUBLISHED_ASC(1),
    ;

    companion object {
        val default = DATE_PUBLISHED_DESC

        fun fromKey(key: Long): EpisodeSortOrder {
            return entries.firstOrNull { it.key == key } ?: DATE_PUBLISHED_DESC
        }
    }
}
