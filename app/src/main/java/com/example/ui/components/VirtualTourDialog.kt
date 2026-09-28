package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.SoundPlayer
import com.example.model.Colour

data class TourSlide(
    val stepNumber: Int,
    val icon: ImageVector,
    val badge: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val tip: String
)

@Composable
fun VirtualTourDialog(
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }

    val slides = remember {
        listOf(
            TourSlide(
                stepNumber = 1,
                icon = Icons.Default.Traffic,
                badge = "WELCOME TO ACCRA CENTRAL",
                title = "The Lorry Park Traffic Jam",
                subtitle = "Vehicles are gridlocked trying to escape the station yard!",
                description = "Welcome to Trotro Rush! The lorry park is completely packed with colourful trotros, 207 Sprinters, and buses. Every driver wants to escape the yard to pick up waiting passengers, but vehicles are tightly jammed and blocking each other's path.",
                tip = "Ghanaian Wisdom: 'Nyame Bekyere' — Patience and sharp eyes will clear the road!"
            ),
            TourSlide(
                stepNumber = 2,
                icon = Icons.Default.Navigation,
                badge = "RULE 1: ESCAPE LANE",
                title = "Follow the Direction Arrow",
                subtitle = "Trotros can only drive forward in their arrow direction!",
                description = "Each trotro has a white navigation arrow on its roof (↑, ↓, ←, →). Trotros cannot reverse or steer sideways. A trotro can ONLY escape the yard if all cells between its front bumper and the park boundary are completely empty!",
                tip = "Tap a blocked trotro to see a bump warning. Find the unblocked vehicles first to unlock the jam."
            ),
            TourSlide(
                stepNumber = 3,
                icon = Icons.Default.DirectionsBus,
                badge = "RULE 2: PARKING BAYS",
                title = "Loading Bays Are Limited",
                subtitle = "Trotros park in the lowest numbered open bay.",
                description = "When a trotro breaks free from the yard, it drives straight into the leftmost available parking slot (4 to 7 bays per level). Parked trotros wait there until their seats are 100% filled with matching passengers.",
                tip = "Caution: Don't fill all bays with the wrong colours, or you will create a gridlock deadlock!"
            ),
            TourSlide(
                stepNumber = 4,
                icon = Icons.Default.People,
                badge = "RULE 3: COLOUR MATCHING",
                title = "Boarding & Departure Rush",
                subtitle = "Gate passengers board matching trotros automatically!",
                description = "Passengers wait in a queue at the station gate. The passenger at the head of the queue immediately boards a parked trotro of the same colour and symbol (●, ■, ▲, etc.). As soon as a trotro is fully boarded, it honks and zooms off, freeing the bay for another vehicle!",
                tip = "Watch the queue order carefully. Send out trotros that match the front passengers."
            ),
            TourSlide(
                stepNumber = 5,
                icon = Icons.Default.Star,
                badge = "MASTER THE YARD",
                title = "Unlimited Undo & 3 Stars",
                subtitle = "No timers, no lives, zero stress!",
                description = "Made a wrong move? Tap the Undo button anytime to rewind your moves back to safety. Clear all queue passengers in minimum moves to match the Par target and earn 3 Golden Stars across 40 stations in Ghana!",
                tip = "You are ready to command the yard, Station Master! Tap 'Start Playing!' below."
            )
        )
    }

    val slide = slides[currentStep]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar: Ghanaian ribbon + Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .width(60.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    ) {
                        Box(modifier = Modifier.weight(1f).height(4.dp).background(Color(0xFFEF4444)))
                        Box(modifier = Modifier.weight(1f).height(4.dp).background(Color(0xFFFACC15)))
                        Box(modifier = Modifier.weight(1f).height(4.dp).background(Color(0xFF10B981)))
                    }

                    Text(
                        text = "VIRTUAL TOUR (${currentStep + 1}/${slides.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    IconButton(
                        onClick = {
                            SoundPlayer.playClick()
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("tour_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Tour",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                AnimatedContent(
                    targetState = slide,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "TourSlideAnimation"
                ) { current ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Visual Hero Badge
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = current.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stage Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = current.badge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = current.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = current.subtitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive illustration preview for step with unified accessibility semantics
                        TourStepIllustration(stepIndex = current.stepNumber)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = current.description,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tip Card with grouped TalkBack announcement
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics(mergeDescendants = true) {
                                    contentDescription = "Station Master Tip: ${current.tip}"
                                }
                        ) {
                            Text(
                                text = "💡 ${current.tip}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Step Dots with semantic indicator
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {
                            contentDescription = "Step ${currentStep + 1} of ${slides.size}"
                        }
                ) {
                    slides.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (index == currentStep) 10.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentStep) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation Buttons (Back, Next/Start)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = {
                                SoundPlayer.playClick()
                                currentStep--
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back")
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                SoundPlayer.playClick()
                                onDismiss()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Skip Tour")
                        }
                    }

                    Button(
                        onClick = {
                            SoundPlayer.playClick()
                            if (currentStep < slides.size - 1) {
                                currentStep++
                            } else {
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (currentStep < slides.size - 1) {
                            Text("Next Step")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        } else {
                            Text("Start Playing!", fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TourStepIllustration(stepIndex: Int) {
    val illustrationSemantic = when (stepIndex) {
        1 -> "Illustration: Trotros jammed at the lorry park with collision and no-entry blocked lanes"
        2 -> "Illustration: Trotro following clear green arrow path into station exit gate"
        3 -> "Illustration: Loading bays with red and blue parked trotros and free slots"
        4 -> "Illustration: Passenger queue at the station gate boarding a matching red trotro"
        else -> "Illustration: Three stars award for par performance and unlimited undo replay control"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = illustrationSemantic
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            when (stepIndex) {
                1 -> {
                    // Gridlocked Yard Illustration with explicit explanation and caption
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MiniTrotroIllustration(color = Colour.RED, slogan = "Nyame", dir = "↑")
                            Text("💥", fontSize = 16.sp)
                            MiniTrotroIllustration(color = Colour.BLUE, slogan = "Sea Never", dir = "→")
                            Text("⛔", fontSize = 16.sp)
                            MiniTrotroIllustration(color = Colour.YELLOW, slogan = "Slow", dir = "↓")
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "💥 Traffic collision ahead  ·  ⛔ Lane blocked by another trotro",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                2 -> {
                    // Arrow Escape Lane Illustration
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MiniTrotroIllustration(color = Colour.GREEN, slogan = "Clear!", dir = "➔")
                        Text("════ ➔", fontWeight = FontWeight.Black, color = Color(0xFF22C55E), fontSize = 14.sp)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF22C55E))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("EXIT GATE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 9.sp)
                        }
                    }
                }
                3 -> {
                    // Parking Bays
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SlotBadge(label = "Bay 1", text = "● RED 2/4", active = true, color = Colour.RED)
                        SlotBadge(label = "Bay 2", text = "■ BLUE 3/6", active = true, color = Colour.BLUE)
                        SlotBadge(label = "Bay 3", text = "FREE", active = false, color = Colour.WHITE)
                        SlotBadge(label = "Bay 4", text = "FREE", active = false, color = Colour.WHITE)
                    }
                }
                4 -> {
                    // Queue and Boarding
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PassengerBadge(Colour.RED)
                        PassengerBadge(Colour.RED)
                        PassengerBadge(Colour.BLUE)
                        Text("➔ GATE ➔", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        MiniTrotroIllustration(color = Colour.RED, slogan = "Loading", dir = "P1")
                    }
                }
                else -> {
                    // Stars and Victory
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(28.dp))
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(36.dp))
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Undo, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Text("REPLAY", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniTrotroIllustration(color: Colour, slogan: String, dir: String) {
    Box(
        modifier = Modifier
            .size(width = 64.dp, height = 34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color.lightColor)
            .border(1.dp, Color.White, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = color.symbolChar, color = color.onColor, fontSize = 11.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = dir, color = color.onColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SlotBadge(label: String, text: String, active: Boolean, color: Colour) {
    Box(
        modifier = Modifier
            .size(width = 62.dp, height = 36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) color.lightColor else Color(0x3364748B))
            .border(1.dp, if (active) Color.White else Color(0x5564748B), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 8.sp, color = if (active) color.onColor else Color.LightGray)
            Text(text, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (active) color.onColor else Color.White)
        }
    }
}

@Composable
private fun PassengerBadge(color: Colour) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(color.lightColor)
            .border(1.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = color.symbolChar, color = color.onColor, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}
