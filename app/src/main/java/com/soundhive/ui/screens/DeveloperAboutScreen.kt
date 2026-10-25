package com.soundhive.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundhive.R
import com.soundhive.ui.components.LiquidGlassButton
import com.soundhive.ui.components.LiquidGlassCard
import com.soundhive.ui.components.LiquidGlassIconButton
import com.soundhive.ui.components.LiquidGlassSurface
import com.soundhive.ui.components.SoundhiveGlassShapes
import com.soundhive.ui.components.SoundhiveGlassTier
import com.soundhive.ui.components.soundhiveTactilePress
import com.soundhive.ui.theme.SoundhiveBrand

/**
 * Soundhive Developer & About Screen.
 *
 * Minimalist, respectful developer information:
 * - "Built by Adhil CLT"
 * - "Creator & developer of Soundhive"
 * - "Soundhive is completely free and ad-free."
 * - Optional contribution link: https://buymeatea.online/adhil
 */
@Composable
fun DeveloperAboutScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    BackHandler {
        onBack()
    }

    val context = LocalContext.current
    var contentVisible by remember { mutableStateOf(false) }
    val smoothEasing = remember { CubicBezierEasing(0.2f, 0.0f, 0.1f, 1.0f) }

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .drawBehind {
                val centerOffset = Offset(size.width * 0.5f, size.height * 0.25f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SoundhiveBrand.DeepOrange.copy(alpha = 0.20f),
                            SoundhiveBrand.PrimaryOrange.copy(alpha = 0.06f),
                            Color.Transparent,
                        ),
                        center = centerOffset,
                        radius = size.width * 0.85f,
                    ),
                    center = centerOffset,
                    radius = size.width * 0.85f,
                )
            }
            .testTag("soundhive_developer_about_screen"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LiquidGlassIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    onClick = onBack,
                    contentDescription = "Back",
                    size = 38.dp,
                    iconSize = 18.dp,
                    tier = SoundhiveGlassTier.Subtle,
                    modifier = Modifier.testTag("developer_about_back_button"),
                )

                LiquidGlassSurface(
                    tier = SoundhiveGlassTier.Subtle,
                    shape = SoundhiveGlassShapes.Pill,
                    modifier = Modifier
                        .soundhiveTactilePress(onClick = onContinue)
                        .testTag("developer_about_skip_button"),
                ) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = Color(0xFFCBD5E1),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(tween(500, easing = smoothEasing)) +
                        slideInVertically(tween(500, easing = smoothEasing)) { it / 5 },
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(R.drawable.bglogo),
                            contentDescription = "Soundhive",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(80.dp)
                                .testTag("developer_about_logo"),
                        )

                        Spacer(Modifier.height(20.dp))

                        Text(
                            text = "Built by Adhil CLT",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp,
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("developer_about_title"),
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "Creator & developer of Soundhive",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = SoundhiveBrand.HighlightOrange,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Soundhive is completely free and ad-free.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Optional Contribution Card
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = SoundhiveGlassShapes.Card,
                    tier = SoundhiveGlassTier.Elevated,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                    ) {
                        Text(
                            text = "Support development",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = Color.White,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "If you'd like to help keep Soundhive free and support future development, you can optionally contribute.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                            ),
                            color = Color(0xFFCBD5E1),
                        )
                        Spacer(Modifier.height(14.dp))

                        LiquidGlassButton(
                            text = "☕ Support development",
                            icon = Icons.Rounded.OpenInNew,
                            onClick = {
                                try {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://buymeatea.online/adhil"),
                                    ).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                }
                            },
                            isPrimary = false,
                            tier = SoundhiveGlassTier.Elevated,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // Bottom Action
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp + contentPadding.calculateBottomPadding()),
            ) {
                LiquidGlassButton(
                    text = "Continue",
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    onClick = onContinue,
                    isPrimary = true,
                    tier = SoundhiveGlassTier.Prominent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("developer_about_continue_button"),
                )
            }
        }
    }
}
