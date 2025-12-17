package com.ramitsuri.podcasts.android.navigation.deeplink

import android.net.Uri

class DeepLinkRequest(
    val uri: Uri,
) {
    /**
     * A list of path segments
     */
    val pathSegments: List<String> = uri.pathSegments

    /**
     * A map of query name to query value
     */
    val queries =
        buildMap {
            uri.queryParameterNames.forEach { argName ->
                this[argName] = uri.getQueryParameter(argName)!!
            }
        }
}
