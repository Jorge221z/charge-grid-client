package com.jorge.chargegridapp.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorge.chargegridapp.R
import com.jorge.chargegridapp.core.ui.screens.theme.BackgroundDark
import com.jorge.chargegridapp.core.ui.screens.theme.ElectricGreen
import kotlinx.coroutines.delay

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import com.jorge.chargegridapp.core.ui.screens.theme.ElectricGreenDark

@Composable
fun ChargeGridSplashScreen(onSplashFinished: () -> Unit) {
    val alphaLogo = remember { Animatable(0f) }
    val alphaText = remember { Animatable(0f) }
    val alphaTagline = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Sequential animation for a premium feel
        alphaLogo.animateTo(1f, tween(800))
        alphaText.animateTo(1f, tween(600))
        alphaTagline.animateTo(1f, tween(600))
        delay(1200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1C1C1E), BackgroundDark),
                    center = Offset.Unspecified,
                    radius = Float.POSITIVE_INFINITY
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle background pattern or element could go here
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.charge_grid_logo),
                contentDescription = "ChargeGrid Logo",
                modifier = Modifier
                    .size(120.dp) // Smaller, more elegant size
                    .alpha(alphaLogo.value),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "CHARGEGRID",
                style = MaterialTheme.typography.headlineSmall.copy(
                    letterSpacing = 10.sp, // Very wide spacing for "luxury" tech look
                    fontWeight = FontWeight.Light,
                    brush = Brush.linearGradient(
                        colors = listOf(ElectricGreen, Color.White)
                    )
                ),
                modifier = Modifier.alpha(alphaText.value)
            )
        }

        // Minimalist Tagline at the bottom
        Text(
            text = "POWERING THE FUTURE",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 4.sp,
                fontWeight = FontWeight.ExtraLight
            ),
            color = Color.Gray.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(alphaTagline.value)
        )
    }
}
