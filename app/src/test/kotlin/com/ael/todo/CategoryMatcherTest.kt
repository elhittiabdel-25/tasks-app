package com.ael.todo

import com.ael.todo.data.db.entity.Category
import com.ael.todo.data.db.entity.CategoryKeyword
import com.ael.todo.domain.CategoryMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CategoryMatcherTest {

    private lateinit var matcher: CategoryMatcher
    private lateinit var categories: List<Category>
    private lateinit var keywords: List<CategoryKeyword>

    @Before
    fun setup() {
        matcher = CategoryMatcher()
        categories = listOf(
            Category(id = 1, name = "Personal", color = 0, isPredefined = true),
            Category(id = 2, name = "Shopping", color = 0, isPredefined = true),
            Category(id = 3, name = "Work",     color = 0, isPredefined = true),
            Category(id = 4, name = "Health",   color = 0, isPredefined = true),
            Category(id = 5, name = "Home",     color = 0, isPredefined = true),
            Category(id = 6, name = "Finance",  color = 0, isPredefined = true),
        )
        keywords = listOf(
            CategoryKeyword(categoryId = 2, keyword = "buy"),
            CategoryKeyword(categoryId = 2, keyword = "shop"),
            CategoryKeyword(categoryId = 2, keyword = "groceries"),
            CategoryKeyword(categoryId = 3, keyword = "email"),
            CategoryKeyword(categoryId = 3, keyword = "call"),
            CategoryKeyword(categoryId = 3, keyword = "meeting"),
            CategoryKeyword(categoryId = 4, keyword = "doctor"),
            CategoryKeyword(categoryId = 4, keyword = "gym"),
            CategoryKeyword(categoryId = 4, keyword = "workout"),
            CategoryKeyword(categoryId = 5, keyword = "clean"),
            CategoryKeyword(categoryId = 5, keyword = "laundry"),
            CategoryKeyword(categoryId = 6, keyword = "pay"),
            CategoryKeyword(categoryId = 6, keyword = "bill"),
        )
    }

    @Test
    fun `exact keyword match returns correct category`() {
        val result = matcher.match("Buy groceries at the store", keywords, categories)
        assertEquals("Shopping", result?.name)
    }

    @Test
    fun `match is case insensitive`() {
        val result = matcher.match("Schedule a MEETING with team", keywords, categories)
        assertEquals("Work", result?.name)
    }

    @Test
    fun `no match returns Personal category`() {
        val result = matcher.match("Random unrelated task", keywords, categories)
        assertEquals("Personal", result?.name)
    }

    @Test
    fun `blank title returns Personal`() {
        val result = matcher.match("", keywords, categories)
        assertEquals("Personal", result?.name)
    }

    @Test
    fun `tiebreak by earliest position in title`() {
        // "email" (Work, catId=3) appears before "pay" (Finance, catId=6)
        // So Work should win because it appears earlier
        val result = matcher.match("email and pay the bill", keywords, categories)
        assertEquals("Work", result?.name)
    }

    @Test
    fun `tiebreak by category creation order when same position`() {
        // "buy" appears at index 0, "pay" also would be at different index
        // When two keywords appear at same position (impossible for same string),
        // category with lower id wins. Test: two categories, same start position
        // Actually this tests that when positions tie, lower categoryId wins.
        val extraKeyword = CategoryKeyword(categoryId = 6, keyword = "buy") // Finance also has "buy"
        val result = matcher.match("buy something", keywords + extraKeyword, categories)
        // "buy" first match: Shopping (catId=2) vs Finance (catId=6), Shopping wins (lower id)
        assertEquals("Shopping", result?.name)
    }

    @Test
    fun `word boundary match only - does not match substrings`() {
        // "call" should not match "callback" as a whole word
        val result = matcher.match("callback function refactor", keywords, categories)
        assertEquals("Personal", result?.name)
    }

    @Test
    fun `keyword at end of title matches`() {
        val result = matcher.match("Time to pay", keywords, categories)
        assertEquals("Finance", result?.name)
    }
}
