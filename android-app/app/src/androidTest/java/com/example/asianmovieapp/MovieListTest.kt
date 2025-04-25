package com.example.asianmovieapp

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.*
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import org.junit.Rule
import org.junit.Test


class MovieListTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()


    @Test
    fun randomMovie_NavigatesToDetails() {
        // Click the Random Movie button
        composeTestRule.onNodeWithTag("RandomMovieButton", useUnmergedTree = true)
            .assertExists()
            .performClick()

        // Wait for the MovieDetailsScreen to appear
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithTag("DetailsScreenColumn").fetchSemanticsNodes().isNotEmpty()
        }

        // Confirm we are on the MovieDetails screen by checking the movie title is displayed
        composeTestRule.onNodeWithTag("DetailsTitle").assertExists()
    }


    @Test
    fun addMovie_OpensFormAndSavesMovie() {
        composeTestRule.onNodeWithContentDescription("Add Movie").performClick()

        composeTestRule.onNodeWithTag("TitleInput").performTextInput("1 Test Movie")
        composeTestRule.onNodeWithTag("GenreInput").performTextInput("Action")
        composeTestRule.onNodeWithTag("DescriptionInput").performTextInput("A test movie.")
        composeTestRule.onNodeWithTag("ImageUrlInput")
            .performTextInput("https://example.com/poster.jpg")
        composeTestRule.onNodeWithTag("ReleaseDateInput").performTextInput("2025-01-01")

        composeTestRule.onNodeWithTag("RatingInput").performTextClearance()
        composeTestRule.onNodeWithTag("RatingInput").performTextInput("8.5")

        composeTestRule.onNodeWithTag("SubmitButton", useUnmergedTree = true).performClick()

        // Wait until the new movie appears and try to click on it
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithText("1 Test Movie").fetchSemanticsNodes().isNotEmpty()
        }

        // Click it to confirm it's interactive
        composeTestRule.onNodeWithText("1 Test Movie").performClick()
    }

    @Test
    fun editMovie_OpensFormAndUpdatesMovie() {
        // Click on the movie added previously
        composeTestRule.onNodeWithText("1 Test Movie").performClick()

        // Wait for the movie details screen to load
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithTag("MovieDetailsInfo", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Click the Edit button using the unmerged tree
        composeTestRule.onNodeWithTag("EditMovieButton", useUnmergedTree = true)
            .assertExists("Edit button not found")
            .assertIsDisplayed()
            .performClick()

        // Wait until the form shows up again
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithTag("SubmitButton", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Update the title
        composeTestRule.onNodeWithTag("TitleInput")
            .performTextInput("1 Updated ")

        // Submit the edited movie
        composeTestRule.onNodeWithTag("SubmitButton", useUnmergedTree = true).performClick()

        // Wait for the updated title to appear
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithText("1 Updated 1 Test Movie")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Confirm it’s interactive
        composeTestRule.onNodeWithText("1 Updated 1 Test Movie").performClick()
    }

    @Test
    fun addReview_AddsReviewSuccessfully() {
        // Open the movie details for the existing test movie
        composeTestRule.onNodeWithText("1 Updated 1 Test Movie").performClick()

        // Wait for MovieDetails screen
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithTag("MovieDetailsInfo", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Click the "Add Review" button
        composeTestRule.onNodeWithTag("OpenReviewFormButton", useUnmergedTree = true)
            .assertExists("Review button not found")
            .performClick()

        // Input reviewer's name
        composeTestRule.onNodeWithTag("ReviewerInput").performTextInput("Alice")

        // Input rating (clear first if needed)
        composeTestRule.onNodeWithTag("RatingInput")
            .performTextInput("9")

        // Input review text
        composeTestRule.onNodeWithTag("ReviewTextInput")
            .performTextInput("This movie was fantastic!")

        // Submit the review
        composeTestRule.onNodeWithTag("SubmitReviewButton", useUnmergedTree = true).performClick()

        // Navigate back to the list screen
        composeTestRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        // Wait for the list to be visible again
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithText("1 Updated 1 Test Movie").fetchSemanticsNodes().isNotEmpty()
        }

        // Click the movie again to re-enter details
        composeTestRule.onNodeWithText("1 Updated 1 Test Movie").performClick()

        // Now check if the review is displayed
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithTag("ReviewerText", useUnmergedTree = true)
                .fetchSemanticsNodes().any {
                    val text = it.config.getOrElse(SemanticsProperties.Text) { emptyList() }
                    text.joinToString().contains("Alice")
                }
        }

        // Optional: assert comment appears too
        composeTestRule.onAllNodesWithTag("ReviewText", useUnmergedTree = true)
            .assertAny(hasText("This movie was fantastic!"))
    }

    @Test
    fun editReview_UpdatesReviewSuccessfully() {
        // Open the test movie
        composeTestRule.onNodeWithText("1 Updated 1 Test Movie").performClick()

        // Wait for the details screen to load
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithTag("MovieDetailsInfo", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Find the first EditReviewButton and click it
        composeTestRule.onAllNodesWithTag("EditReviewButton", useUnmergedTree = true)
            .onFirst()
            .assertExists("EditReviewButton not found")
            .performClick()

        // Update the review comment
        composeTestRule.onNodeWithTag("ReviewTextInput")
            .performTextInput("Loved it even more! ")

        // Submit the update
        composeTestRule.onNodeWithTag("SubmitReviewButton", useUnmergedTree = true)
            .performScrollTo()
            .performClick()

        // Navigate back to list
        composeTestRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        // Wait for movie to show up again
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithText("1 Updated 1 Test Movie").fetchSemanticsNodes().isNotEmpty()
        }

        // Re-open the movie details
        composeTestRule.onNodeWithText("1 Updated 1 Test Movie").performClick()

        // Wait for the updated review to appear
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithTag("ReviewText", useUnmergedTree = true)
                .fetchSemanticsNodes().any {
                    val text = it.config.getOrElse(SemanticsProperties.Text) { emptyList() }
                    text.joinToString().contains("Loved it even more! This movie was fantastic!")
                }
        }

        // Confirm the new text is interactive
        composeTestRule.onAllNodesWithTag("ReviewText", useUnmergedTree = true)
            .assertAny(hasText("Loved it even more! This movie was fantastic!"))
    }

    @Test
    fun deleteReview_DeletesReviewSuccessfully() {
        // Open the test movie
        composeTestRule.onNodeWithText("1 Updated 1 Test Movie").performClick()

        // Wait for the details screen to load
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithTag("MovieDetailsInfo", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Confirm the review exists first
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithTag("ReviewText", useUnmergedTree = true)
                .fetchSemanticsNodes().any {
                    val text = it.config.getOrElse(SemanticsProperties.Text) { emptyList() }
                    text.joinToString().contains("Loved it even more! This movie was fantastic!")
                }
        }

        // Delete the review
        composeTestRule.onAllNodesWithTag("DeleteReviewButton", useUnmergedTree = true)
            .onFirst()
            .assertExists("DeleteReviewButton not found")
            .performClick()

        // Navigate back to list screen
        composeTestRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        // Wait for movie list
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithText("1 Updated 1 Test Movie").fetchSemanticsNodes().isNotEmpty()
        }

        // Re-enter movie details
        composeTestRule.onNodeWithText("1 Updated 1 Test Movie").performClick()

        // Wait to confirm the review is gone
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithTag("ReviewText", useUnmergedTree = true)
                .fetchSemanticsNodes().none {
                    val text = it.config.getOrElse(SemanticsProperties.Text) { emptyList() }
                    text.joinToString().contains("Loved it even more! This movie was fantastic!")
                }
        }

    }

    @Test
    fun deleteMovie_RemovesMovieFromList() {
        // Click on the movie to go to details
        composeTestRule.onNodeWithText("1 Updated 1 Test Movie").performClick()

        // Click delete button
        composeTestRule.onNodeWithTag("DeleteMovieButton").performClick()

        // Confirm it's no longer in the list (wait for navigation to finish first)
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithText("1 Updated 1 Test Movie").fetchSemanticsNodes().isEmpty()
        }

        composeTestRule.onAllNodesWithText("1 Updated 1 Test Movie").assertCountEquals(0)
    }

}