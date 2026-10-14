package com.daeumgil.app

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CurrentTransitUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Before fun resetSelection() {
        clearSelection()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("아직 선택하지 않았어요").fetchSemanticsNodes().isNotEmpty() }
    }
    @After fun cleanUp() { clearSelection() }
    private fun clearSelection() = runBlocking {
        (compose.activity.application as DaeumgilApplication).container.currentTransitRepository.clear()
    }
    private fun click(tag: String) { compose.onNodeWithTag(tag).performScrollTo().performClick() }
    private fun save() { compose.onNodeWithTag("save-current").performClick() }
    private fun awaitHome() {
        compose.waitUntil(10_000) { compose.onAllNodesWithText("어디로 가고 있나요?").fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun missingFieldsAreReportedAndWalkingSelectionSurvivesActivityRecreation() {
        click("select-current")
        save()
        compose.onNodeWithTag("validation-error").assertTextContains("교통수단", substring = true)
        click("transport-WALKING")
        save()
        compose.onNodeWithTag("validation-error").assertTextContains("위치", substring = true)
        click("pick-stop")
        click("stop-sample-start-0")
        save()
        awaitHome()
        compose.onNodeWithTag("select-current").assertTextContains("걷는 중", substring = true)
        compose.activityRule.scenario.recreate()
        awaitHome()
        compose.onNodeWithTag("select-current").assertTextContains("연습 출발역", substring = true)
        compose.onNodeWithTag("find-route").performScrollTo().assertIsNotEnabled()
    }

    @Test fun subwayDraftRestoresAndExpressStopListExcludesSkippedStop() {
        click("select-current")
        click("transport-SUBWAY")
        click("pick-route")
        compose.onNodeWithTag("picker-query").performTextReplacement("A선")
        click("route-sample-subway-a")
        click("service-EXPRESS")
        click("pick-direction")
        click("direction-subway-a-out")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("service-EXPRESS").assertIsSelected()
        click("pick-stop")
        compose.onNodeWithText("연습 중간역").assertDoesNotExist()
        click("stop-sample-transfer-2")
        click("confirm-onboard")
        save()
        awaitHome()
        compose.onNodeWithTag("select-current").assertTextContains("연습 A선", substring = true)
        compose.onNodeWithTag("select-current").assertTextContains("급행", substring = true)
        compose.onNodeWithTag("select-current").assertTextContains("연습 환승역", substring = true)
    }

    @Test fun regionChangeClearsBusRouteAndUnknownDirectionIsExplicitlySaved() {
        click("select-current")
        click("transport-BUS")
        click("pick-region")
        click("region-sample-east")
        click("pick-route")
        compose.onNodeWithTag("picker-query").performTextReplacement("101")
        click("route-sample-bus-east-101-b")
        click("pick-direction")
        click("direction-bus-east-b-out")
        click("pick-region")
        click("region-sample-west")
        compose.onNodeWithTag("pick-route").assertTextContains("목록에서 선택")
        compose.onNodeWithTag("pick-direction").assertDoesNotExist()
        click("pick-route")
        click("route-sample-bus-west-101")
        click("pick-direction")
        click("direction-unknown")
        click("pick-stop")
        click("stop-sample-park-0")
        click("confirm-onboard")
        save()
        awaitHome()
        compose.onNodeWithTag("select-current").assertTextContains("서쪽", substring = true)
        compose.onNodeWithTag("select-current").assertTextContains("방향을 모르겠어요", substring = true)
        compose.onNodeWithText("샘플 데이터").assertIsDisplayed()
    }
}
