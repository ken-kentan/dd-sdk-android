/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.core.net.toUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.datadog.android.compose.NavigationViewTrackingEffect
import com.datadog.android.rum.GlobalRum
import com.google.accompanist.appcompattheme.AppCompatTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppCompatTheme {
                val navController = rememberNavController().apply {
                    NavigationViewTrackingEffect(navController = this)
                }

                NavHost(
                    navController = navController,
                    startDestination = "home",
                    route = "route",
                ) {
                    composable("home") {
                        Surface(
                            modifier = Modifier.onInitialRender {
                                GlobalRum.get().addTiming("timing_test")
                            }
                        ) {
                            Column {
                                Text("Home screen")
                                Button(onClick = { navController.navigate("item") }) {
                                    Text("Goto item screen")
                                }
                            }
                        }
                    }

                    composable("item") {
                        Surface(
                            modifier = Modifier.onInitialRender {
                                GlobalRum.get().addTiming("timing_test")
                            }
                        ) {
                            Text("Item screen")
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.onInitialRender(
    onRendered: () -> Unit,
) = composed {
    var called by remember { mutableStateOf(false) }
    drawWithContent {
        drawContent()
        if (!called) {
            called = true
            onRendered()
        }
    }
}
