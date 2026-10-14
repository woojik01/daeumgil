package com.daeumgil.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoundationUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun homeNavigatesToCurrentDestinationAndFavoritesAndBack() {
        awaitSettings()
        compose.onNodeWithTag("select-current").performClick()
        compose.onNodeWithText("이동 상태 선택을 준비하고 있어요.").assertIsDisplayed()
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("select-destination").performClick()
        compose.onNodeWithText("목적지 검색을 준비하고 있어요.").assertIsDisplayed()
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("open-favorites").performScrollTo().performClick()
        compose.onNodeWithText("즐겨찾는 목적지가 아직 없어요.").assertIsDisplayed()
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("find-route").performScrollTo().assertIsNotEnabled()
    }

    @Test fun selectedThemeAndSettingsDestinationSurviveActivityRecreation() {
        awaitSettings()
        compose.onNodeWithTag("open-settings").performClick()
        compose.onNodeWithTag("theme-dark").performClick()
        awaitSelected("theme-dark")
        compose.onNodeWithTag("theme-dark").assertIsSelected()
        compose.activityRule.scenario.recreate()
        awaitSettings()
        awaitSelected("theme-dark")
        compose.onNodeWithTag("theme-dark").assertIsSelected()
        compose.onNodeWithTag("theme-light").performClick()
        awaitSelected("theme-light")
        compose.onNodeWithTag("theme-light").assertIsSelected()
        compose.onNodeWithTag("theme-system").performClick()
        awaitSelected("theme-system")
        compose.onNodeWithTag("theme-system").assertIsSelected()
    }

    private fun awaitSettings() {
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithText("설정을 불러오는 중이에요.").fetchSemanticsNodes().isEmpty()
        }
    }

    private fun awaitSelected(tag: String) {
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodes(hasTestTag(tag) and isSelected()).fetchSemanticsNodes().isNotEmpty()
        }
    }
}

