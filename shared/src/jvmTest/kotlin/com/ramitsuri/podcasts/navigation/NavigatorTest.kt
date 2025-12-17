package com.ramitsuri.podcasts.navigation

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NavigatorTest {
    private lateinit var navigator: Navigator

    @Test
    fun `Initial state with default start route`() {
        navigator = Navigator()

        assertEquals(1, navigator.backstack.size)
        assertEquals(Route.Home, navigator.backstack[0])
    }

    @Test
    fun `Initial state with custom start route`() {
        navigator = Navigator(Route.Downloads)

        assertEquals(2, navigator.backstack.size)
        assertEquals(Route.Home, navigator.backstack[0])
        assertEquals(Route.Downloads, navigator.backstack[1])
    }

    @Test
    fun `Initial state with Home as start route`() {
        navigator = Navigator(Route.Home)

        assertEquals(1, navigator.backstack.size)
        assertEquals(Route.Home, navigator.backstack[0])
    }

    @Test
    fun `getBackstack returns correct list`() {
        navigator = Navigator(Route.Downloads)
        val backstack = navigator.backstack

        assertEquals(listOf(Route.Home, Route.Downloads), backstack)
    }

    @Test
    fun `getCurrentDestination initial state`() {
        navigator = Navigator()

        assertEquals(Route.Home, navigator.currentDestination)
    }

    @Test
    fun `getCurrentDestination initial state with non null start route`() {
        navigator = Navigator(Route.Downloads)

        assertEquals(Route.Downloads, navigator.currentDestination)
    }

    @Test
    fun `getCurrentDestination after navigation`() {
        navigator = Navigator()

        assertEquals(Route.Home, navigator.currentDestination)
        navigator.navigate(Route.Downloads)
        assertEquals(Route.Downloads, navigator.currentDestination)
        navigator.goBack()
        assertEquals(Route.Home, navigator.currentDestination)
    }

    @Test
    fun `navigate to a new TopLevelRoute removes everything but Home`() {
        navigator = Navigator()
        navigator.navigate(Route.Library)
        navigator.navigate(Route.Downloads)
        navigator.navigate(Route.Explore)

        assertEquals(listOf(Route.Home, Route.Explore), navigator.backstack)
    }

    @Test
    fun `navigate to a new TopLevelRoute multiple times`() {
        navigator = Navigator()
        navigator.navigate(Route.Library)
        navigator.navigate(Route.Explore)
        navigator.navigate(Route.Library)

        assertEquals(listOf(Route.Home, Route.Library), navigator.backstack)
    }

    @Test
    fun `navigate to an existing non TopLevelRoute`() {
        navigator = Navigator()

        navigator.navigate(Route.Library)
        navigator.navigate(Route.Downloads)
        navigator.navigate(Route.EpisodeHistory)
        navigator.navigate(Route.Downloads)

        assertEquals(listOf(Route.Home, Route.Library, Route.Downloads), navigator.backstack)
    }

    @Test
    fun `navigate to the current destination returns false`() {
        navigator = Navigator()

        navigator.navigate(Route.Library)
        val navigated = navigator.navigate(Route.Library)

        assertFalse(navigated)
    }

    @Test
    fun `navigate to Home as a TopLevelRoute, everything should be cleared`() {
        navigator = Navigator()

        navigator.navigate(Route.Library)
        navigator.navigate(Route.Downloads)
        navigator.navigate(Route.EpisodeHistory)
        navigator.navigate(Route.Home)

        assertEquals(listOf(Route.Home), navigator.backstack)
    }

    @Test
    fun `navigate to an existing route deep in stack`() {
        // Navigate to a TopLevelRoute that is already in the backstack but not at the top.
        navigator = Navigator()

        navigator.navigate(Route.Library)
        navigator.navigate(Route.Downloads)
        navigator.navigate(Route.EpisodeHistory)
        navigator.navigate(Route.PodcastDetails(2, false))
        navigator.navigate(Route.Library)

        assertEquals(listOf(Route.Home, Route.Library), navigator.backstack)
    }

    @Suppress("JoinDeclarationAndAssignment")
    @Test
    fun `navigate returns true on success`() {
        navigator = Navigator()

        var navigated: Boolean

        navigated = navigator.navigate(Route.Downloads)
        assertTrue(navigated)

        navigated = navigator.navigate(Route.EpisodeHistory)
        assertTrue(navigated)

        navigated = navigator.navigate(Route.Home)
        assertTrue(navigated)

        navigated = navigator.navigate(Route.Library)
        assertTrue(navigated)

        navigated = navigator.navigate(Route.Library)
        assertFalse(navigated)
    }

    @Test
    fun `goBack from a multi entry backstack`() {
        navigator = Navigator()

        navigator.navigate(Route.Home)
        navigator.navigate(Route.Library)
        navigator.navigate(Route.Downloads)
        navigator.navigate(Route.Settings)
        navigator.navigate(Route.EpisodeHistory)
        navigator.navigate(Route.BackupRestore)

        navigator.goBack()
        assertEquals(Route.EpisodeHistory, navigator.currentDestination)
        navigator.goBack()
        assertEquals(Route.Settings, navigator.currentDestination)
        navigator.goBack()
        assertEquals(Route.Downloads, navigator.currentDestination)
        navigator.goBack()
        assertEquals(Route.Library, navigator.currentDestination)
        navigator.goBack()
        assertEquals(Route.Home, navigator.currentDestination)
    }

    @Test
    fun `goBack when backstack has one entry`() {
        navigator = Navigator()

        navigator.goBack()
        assertTrue(navigator.backstack.isEmpty())
    }

    @Test
    fun `goBack on an empty backstack`() {
        navigator = Navigator()

        navigator.goBack()
        navigator.goBack()
        assertTrue(navigator.backstack.isEmpty())
    }

    @Test
    fun `Chained navigations and goBack calls`() {
        navigator = Navigator()

        navigator.navigate(Route.Library)
        navigator.navigate(Route.Downloads)
        navigator.navigate(Route.Explore)
        navigator.navigate(Route.Search)
        navigator.goBack()
        navigator.navigate(Route.Settings)
        navigator.goBack()
        navigator.navigate(Route.EpisodeDetails("episodeId", 1))
        navigator.navigate(Route.PodcastDetails(1, false))

        assertEquals(
            listOf(
                Route.Home,
                Route.Explore,
                Route.EpisodeDetails("episodeId", 1),
                Route.PodcastDetails(1, false),
            ),
            navigator.backstack,
        )
    }
}
