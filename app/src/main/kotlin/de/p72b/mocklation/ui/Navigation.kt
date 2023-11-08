/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.p72b.mocklation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.p72b.mocklation.ui.model.requirements.RequirementsPage
import de.p72b.mocklation.ui.model.simulation.SimulationPage
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun MainNavigation(
    navController: NavHostController,
    navigator: Navigator
) {
    LaunchedEffect("navigation") {
        navigator.sharedFlow.onEach {
            navController.navigate(it.label)
        }.launchIn(this)
    }

    NavHost(
        navController = navController,
        startDestination = Navigator.NavTarget.Requirements.label
    ) {
        composable(Navigator.NavTarget.Simulation.label) {
            SimulationPage(
                modifier = Modifier.padding(
                    16.dp
                )
            )
        }
        composable(Navigator.NavTarget.Requirements.label) {
            RequirementsPage(
                modifier = Modifier.padding(
                    16.dp
                )
            )
        }
    }
}
