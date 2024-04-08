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

import android.content.Intent
import android.util.DisplayMetrics
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import de.p72b.mocklation.R
import de.p72b.mocklation.data.PreferencesRepository
import de.p72b.mocklation.ui.model.collection.CollectionPage
import de.p72b.mocklation.ui.model.dashboard.DashboardPage
import de.p72b.mocklation.ui.model.map.MapActivity
import de.p72b.mocklation.ui.model.requirements.RequirementsPage
import de.p72b.mocklation.ui.model.simulation.SimulationPage
import de.p72b.mocklation.util.Logger
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    navController: NavHostController,
    navigator: Navigator,
    preferencesRepository: PreferencesRepository = koinInject()
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = false,
        confirmValueChange = {
            it != SheetValue.Hidden
        }
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

    val context = LocalContext.current
    val buttonsVisible = remember { mutableStateOf(true) }
    LaunchedEffect("navigation") {
        navigator.sharedFlow.onEach {
            navController.navigate(it.label)
        }.launchIn(this)
    }

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    when (currentRoute) {
        Navigator.NavTarget.Dashboard.label,
        Navigator.NavTarget.Simulation.label,
        Navigator.NavTarget.Collection.label -> {
            buttonsVisible.value = true
        }

        Navigator.NavTarget.Requirements.label -> {
            buttonsVisible.value = false
        }

        else -> {
            buttonsVisible.value = true
        }
    }

    val items by preferencesRepository.featureSelectedState.collectAsStateWithLifecycle()
    when (items) {
        is PreferencesRepository.SelectedIdState.Status -> {
            val status = items as PreferencesRepository.SelectedIdState.Status
            status.id.let {
                scope.launch {
                    when (currentRoute) {
                        Navigator.NavTarget.Dashboard.label,
                        Navigator.NavTarget.Collection.label -> {
                            if (it.isNullOrEmpty()) {
                                if (sheetState.currentValue == SheetValue.Expanded
                                    || sheetState.currentValue == SheetValue.PartiallyExpanded
                                ) {
                                    sheetState.hide()
                                }
                            } else {
                                if (sheetState.currentValue != SheetValue.Expanded) {
                                    sheetState.expand()
                                }
                            }
                        }

                        else -> {
                            if (sheetState.currentValue == SheetValue.Expanded
                                || sheetState.currentValue == SheetValue.PartiallyExpanded
                            ) {
                                sheetState.hide()
                            }
                        }
                    }
                }
            }
        }
    }

    val bottomPadding = when (sheetState.currentValue) {
        SheetValue.Hidden, SheetValue.PartiallyExpanded -> {
            16.dp
        }

        SheetValue.Expanded -> {
            165.dp
        }
    }
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
            if (Navigator.NavTarget.Collection.label == currentRoute) {
                FloatingActionButton(
                    modifier = Modifier.padding(bottom = bottomPadding),
                    onClick = {
                        context.startActivity(Intent(context, MapActivity::class.java))
                    },
                    contentColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(id = R.string.button_add)
                    )
                }
            }
        }
    ) { paddingValues ->
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = {
                SimulationPage(
                    modifier = Modifier.padding(paddingValues)
                )
            },
            sheetPeekHeight = 0.dp,
        ) {
            ContentBox(paddingValues, navController, sheetState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentBox(
    paddingValues: PaddingValues,
    navController: NavHostController,
    sheetState: SheetState
) {
    Box(
        modifier = Modifier.padding(paddingValues)
    ) {
        NavigationGraph(navController = navController, sheetState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationGraph(
    navController: NavHostController,
    sheetState: SheetState
) {
    NavHost(
        navController = navController,
        startDestination = Navigator.NavTarget.Dashboard.label
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
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    if (state.value) {
        NavigationBar(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            screens.forEach { screen ->
                NavigationBarItem(
                    icon = {
                        if (currentRoute == screen.label) {
                            Icon(
                                imageVector = screen.selectedIcon!!,
                                contentDescription = screen.label
                            )
                        } else {
                            Icon(imageVector = screen.icon!!, contentDescription = screen.label)
                        }
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
                    }
                )
            }
        }
    }
}

