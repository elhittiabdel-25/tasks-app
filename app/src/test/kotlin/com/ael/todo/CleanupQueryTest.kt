package com.ael.todo

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class CleanupQueryTest {

    private val days14InMillis = TimeUnit.DAYS.toMillis(14)

    private fun shouldDelete(completedAt: Long, clock: Long): Boolean {
        val cutoff = clock - days14InMillis
        return completedAt < cutoff
    }

    @Test
    fun `task completed exactly 14 days ago is NOT deleted`() {
        val clock = 1_000_000_000_000L
        val completedAt = clock - days14InMillis
        assertFalse(shouldDelete(completedAt, clock))
    }

    @Test
    fun `task completed 14 days and 1ms ago IS deleted`() {
        val clock = 1_000_000_000_000L
        val completedAt = clock - days14InMillis - 1L
        assertTrue(shouldDelete(completedAt, clock))
    }

    @Test
    fun `task completed 15 days ago is deleted`() {
        val clock = 1_000_000_000_000L
        val completedAt = clock - TimeUnit.DAYS.toMillis(15)
        assertTrue(shouldDelete(completedAt, clock))
    }

    @Test
    fun `task completed today is not deleted`() {
        val clock = 1_000_000_000_000L
        val completedAt = clock - TimeUnit.HOURS.toMillis(2)
        assertFalse(shouldDelete(completedAt, clock))
    }

    @Test
    fun `task completed 13 days ago is not deleted`() {
        val clock = 1_000_000_000_000L
        val completedAt = clock - TimeUnit.DAYS.toMillis(13)
        assertFalse(shouldDelete(completedAt, clock))
    }

    @Test
    fun `cutoff uses fixed clock for determinism`() {
        val fixedClock = 1_748_649_600_000L  // 2025-05-31 00:00:00 UTC
        val oldTask = fixedClock - days14InMillis - 1
        val newTask = fixedClock - TimeUnit.DAYS.toMillis(10)
        assertTrue(shouldDelete(oldTask, fixedClock))
        assertFalse(shouldDelete(newTask, fixedClock))
    }
}
