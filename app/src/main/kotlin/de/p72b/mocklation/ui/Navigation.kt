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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import de.p72b.mocklation.R
import de.p72b.mocklation.ui.model.collection.CollectionPage
import de.p72b.mocklation.ui.model.dashboard.DashboardPage
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

    val buttonsVisible = remember { mutableStateOf(true) }
    Scaffold(
        bottomBar = {
            BottomNavigation(
                navController = navController,
                state = buttonsVisible,
                modifier = Modifier
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // TODO
                },
                contentColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(id = R.string.button_add))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            NavigationGraph(navController = navController)
        }
    }
}

@Composable
fun NavigationGraph(navController: NavHostController) {
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
        composable(Navigator.NavTarget.Dashboard.label) {
            DashboardPage(
                modifier = Modifier.padding(
                    16.dp
                )
            )
        }
        composable(Navigator.NavTarget.Collection.label) {
            CollectionPage(
                modifier = Modifier.padding(
                    16.dp
                )
            )
        }
    }
}

@Composable
fun BottomNavigation(
    navController: NavHostController, state: MutableState<Boolean>, modifier: Modifier = Modifier
) {
    val screens = listOf(
        Navigator.NavTarget.Dashboard,
        Navigator.NavTarget.Collection
    )
    navController.currentBackStackEntryAsState()

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        screens.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(imageVector = screen.icon!!, contentDescription = "")
                },
                selected = currentRoute == screen.label,
                onClick = {
                    navController.navigate(screen.label) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    unselectedTextColor = LocalContentColor.current.copy(alpha = 0f),
                    selectedTextColor = MaterialTheme.colorScheme.onSurface
                ),
            )
        }
    }
}

