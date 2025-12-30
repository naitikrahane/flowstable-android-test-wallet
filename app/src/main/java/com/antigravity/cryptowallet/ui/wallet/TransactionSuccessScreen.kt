package com.antigravity.cryptowallet.ui.wallet

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.cryptowallet.ui.components.BrutalistButton
import kotlinx.coroutines.delay

@Composable
fun TransactionSuccessScreen(
    amount: String,
    symbol: String,
    recipient: String,
    txHash: String,
    onDone: () -> Unit
) {
    // Animation States
    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = true
    
    val transition = updateTransition(transitionState, label = "SuccessTransition")
    
    val circleProgress by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 600, easing = FastOutSlowInEasing) },
        label = "Circle"
    ) { if (it) 1f else 0f }
    
    val checkProgress by transition.animateFloat(
        transitionSpec = { 
            tween(durationMillis = 400, delayMillis = 600, easing = LinearOutSlowInEasing) 
        },
        label = "Check"
    ) { if (it) 1f else 0f }

    val contentAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 500, delayMillis = 1000) },
        label = "Content"
    ) { if (it) 1f else 0f }

    val backgroundColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onPrimary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Success Icon (PhonePe style)
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 8.dp.toPx()
                val radius = size.minDimension / 2 - strokeWidth / 2
                
                // Draw Circle
                drawArc(
                    color = contentColor,
                    startAngle = -90f,
                    sweepAngle = 360f * circleProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )

                // Draw Checkmark
                if (checkProgress > 0) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(center.x - radius * 0.4f, center.y)
                        lineTo(center.x - radius * 0.1f, center.y + radius * 0.3f)
                        lineTo(center.x + radius * 0.5f, center.y - radius * 0.4f)
                    }
                    
                    // We need to trim the path based on progress, but simple line drawing is easier for manual canvas
                    // Let's do simple line segments
                    
                    val p1 = Offset(center.x - radius * 0.4f, center.y)
                    val p2 = Offset(center.x - radius * 0.1f, center.y + radius * 0.3f)
                    val p3 = Offset(center.x + radius * 0.5f, center.y - radius * 0.4f)
                    
                    val totalLen1 = (p2 - p1).getDistance()
                    val totalLen2 = (p3 - p2).getDistance()
                    val totalLen = totalLen1 + totalLen2
                    
                    val currentLen = totalLen * checkProgress
                    
                    if (currentLen > 0) {
                        val end1 = if (currentLen > totalLen1) p2 else p1 + (p2 - p1) * (currentLen / totalLen1)
                        drawLine(
                            color = contentColor,
                            start = p1,
                            end = end1,
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                    
                    if (currentLen > totalLen1) {
                         val len2 = currentLen - totalLen1
                         val end2 = p2 + (p3 - p2) * (len2 / totalLen2)
                         drawLine(
                            color = contentColor,
                            start = p2,
                            end = end2,
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Text Content
        androidx.compose.animation.AnimatedVisibility(
            visibleState = transitionState,
            enter = androidx.compose.animation.fadeIn(tween(1000, 1000)) + androidx.compose.animation.slideInVertically(tween(1000, 1000)) { 50 }
        ) {
             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Payment Successful",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "$amount $symbol",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = contentColor,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "to ${recipient.take(6)}...${recipient.takeLast(4)}",
                    fontSize = 14.sp,
                    color = contentColor.copy(alpha = 0.8f),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Ref: ${txHash.take(10)}...",
                    fontSize = 12.sp,
                    color = contentColor.copy(alpha = 0.6f),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
             }
        }

        Spacer(modifier = Modifier.weight(1f))

        BrutalistButton(
            text = "DONE",
            onClick = onDone,
            inverted = true, // Inverted on colored background often looks good (White button on Color)
            modifier = Modifier.fillMaxWidth()
        )
    }
}
