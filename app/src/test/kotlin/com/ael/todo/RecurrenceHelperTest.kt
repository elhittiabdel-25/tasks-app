package com.ael.todo

import com.ael.todo.data.db.entity.Priority
import com.ael.todo.data.db.entity.Recurrence
import com.ael.todo.data.db.entity.Task
import com.ael.todo.data.db.entity.TaskStatus
import com.ael.todo.domain.RecurrenceHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.Calendar

class RecurrenceHelperTest {

    private fun makeTask(
        id: Long = 1L,
        dueAt: Long? = null,
        recurrence: Recurrence = Recurrence.DAILY
    ) = Task(
        id = id,
        title = "Test task",
        dueAt = dueAt,
        priority = Priority.NONE,
        categoryId = 1L,
        status = TaskStatus.COMPLETED,
        recurrence = recurrence,
        createdAt = 0L
    )

    private fun epochOf(year: Int, month: Int, day: Int, hour: Int = 10, min: Int = 0): Long {
        return Calendar.getInstance().apply {
            set(year, month - 1, day, hour, min, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @Test
    fun `daily recurrence adds one day`() {
        val dueAt = epochOf(2025, 6, 15, 9, 0)
        val task = makeTask(dueAt = dueAt, recurrence = Recurrence.DAILY)
        val next = RecurrenceHelper.nextInstance(task, System.currentTimeMillis())
        val expected = RecurrenceHelper.addDays(dueAt, 1)
        assertEquals(expected, next.dueAt)
    }

    @Test
    fun `weekly recurrence adds seven days`() {
        val dueAt = epochOf(2025, 6, 15, 14, 30)
        val task = makeTask(dueAt = dueAt, recurrence = Recurrence.WEEKLY)
        val next = RecurrenceHelper.nextInstance(task, System.currentTimeMillis())
        val expected = RecurrenceHelper.addDays(dueAt, 7)
        assertEquals(expected, next.dueAt)
    }

    @Test
    fun `daily across month boundary Jan 31 to Feb 1`() {
        val dueAt = epochOf(2025, 1, 31, 8, 0)
        val task = makeTask(dueAt = dueAt, recurrence = Recurrence.DAILY)
        val next = RecurrenceHelper.nextInstance(task, System.currentTimeMillis())
        val cal = Calendar.getInstance().apply { timeInMillis = next.dueAt!! }
        assertEquals(2025, cal.get(Calendar.YEAR))
        assertEquals(2 - 1, cal.get(Calendar.MONTH)) // February (0-based)
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(8, cal.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `weekly across month boundary preserves time`() {
        val dueAt = epochOf(2025, 3, 28, 11, 45)
        val task = makeTask(dueAt = dueAt, recurrence = Recurrence.WEEKLY)
        val next = RecurrenceHelper.nextInstance(task, System.currentTimeMillis())
        val cal = Calendar.getInstance().apply { timeInMillis = next.dueAt!! }
        assertEquals(4 - 1, cal.get(Calendar.MONTH)) // April
        assertEquals(4, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(11, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(45, cal.get(Calendar.MINUTE))
    }

    @Test
    fun `next instance has id=0 (new insert)`() {
        val task = makeTask(id = 42L, dueAt = epochOf(2025, 6, 10), recurrence = Recurrence.DAILY)
        val next = RecurrenceHelper.nextInstance(task, System.currentTimeMillis())
        assertEquals(0L, next.id)
    }

    @Test
    fun `next instance has PENDING status`() {
        val task = makeTask(dueAt = epochOf(2025, 6, 10), recurrence = Recurrence.DAILY)
        val next = RecurrenceHelper.nextInstance(task, System.currentTimeMillis())
        assertEquals(TaskStatus.PENDING, next.status)
    }

    @Test
    fun `next instance completedAt is null`() {
        val task = makeTask(dueAt = epochOf(2025, 6, 10), recurrence = Recurrence.DAILY)
        val next = RecurrenceHelper.nextInstance(task, System.currentTimeMillis())
        assertEquals(null, next.completedAt)
    }
}
