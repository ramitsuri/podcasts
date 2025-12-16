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
    fun `navigate to an existing TopLevelRoute deep in stack`() {
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
        // Call goBack() when there are multiple routes in the stack. 
        // Verify the last route is removed and the previous one becomes the current destination.
        // TODO implement test
    }

    @Test
    fun `goBack when backstack has one entry`() {
        // Call goBack() when only Route.Home is in the backstack. 
        // Verify the backstack becomes empty.
        // TODO implement test
    }

    @Test
    fun `goBack on an empty backstack`() {
        // Call goBack() on a navigator that has an empty backstack. 
        // Verify the app does not crash and the backstack remains empty.
        // TODO implement test
    }

    @Test
    fun `Chained navigations and goBack calls`() {
        // Perform a series of navigate and goBack calls to simulate user flow. 
        // Verify the backstack state is correct after each operation.
        // TODO implement test
    }

    @Test
    fun `Navigate to route at index 0`() {
        // Navigate to a non-TopLevelRoute that is at the beginning of the backstack. 
        // Verify all other routes are popped off the stack.
        // TODO implement test
    }

    @Test
    fun `Large backstack handling`() {
        // Add a large number of routes to the backstack and then perform navigation and goBack operations. 
        // Check for performance issues or unexpected behavior.
        // TODO implement test
    }
}
