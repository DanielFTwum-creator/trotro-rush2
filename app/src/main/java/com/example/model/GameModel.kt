package com.example.model

import androidx.compose.ui.graphics.Color

enum class Colour(
    val idName: String,
    val displayName: String,
    val symbolChar: String,
    val symbolDescription: String,
    val lightColor: Color,
    val darkColor: Color,
    val highContrastColor: Color,
    val onColor: Color = Color.White
) {
    RED(
        idName = "red",
        displayName = "Red",
        symbolChar = "●",
        symbolDescription = "Circle",
        lightColor = Color(0xFFDC2626),
        darkColor = Color(0xFFEF4444),
        highContrastColor = Color(0xFFFF0000),
        onColor = Color.White
    ),
    BLUE(
        idName = "blue",
        displayName = "Blue",
        symbolChar = "■",
        symbolDescription = "Square",
        lightColor = Color(0xFF2563EB),
        darkColor = Color(0xFF3B82F6),
        highContrastColor = Color(0xFF0033FF),
        onColor = Color.White
    ),
    YELLOW(
        idName = "yellow",
        displayName = "Yellow",
        symbolChar = "▲",
        symbolDescription = "Triangle",
        lightColor = Color(0xFFEAB308),
        darkColor = Color(0xFFFACC15),
        highContrastColor = Color(0xFFFFFF00),
        onColor = Color.Black
    ),
    GREEN(
        idName = "green",
        displayName = "Green",
        symbolChar = "◆",
        symbolDescription = "Diamond",
        lightColor = Color(0xFF16A34A),
        darkColor = Color(0xFF22C55E),
        highContrastColor = Color(0xFF00FF00),
        onColor = Color.Black
    ),
    ORANGE(
        idName = "orange",
        displayName = "Orange",
        symbolChar = "★",
        symbolDescription = "Star",
        lightColor = Color(0xFFEA580C),
        darkColor = Color(0xFFF97316),
        highContrastColor = Color(0xFFFF6600),
        onColor = Color.White
    ),
    PURPLE(
        idName = "purple",
        displayName = "Purple",
        symbolChar = "⬡",
        symbolDescription = "Hexagon",
        lightColor = Color(0xFF9333EA),
        darkColor = Color(0xFFA855F7),
        highContrastColor = Color(0xFFCC00FF),
        onColor = Color.White
    ),
    PINK(
        idName = "pink",
        displayName = "Pink",
        symbolChar = "♥",
        symbolDescription = "Heart",
        lightColor = Color(0xFFDB2777),
        darkColor = Color(0xFFEC4899),
        highContrastColor = Color(0xFFFF0099),
        onColor = Color.White
    ),
    TEAL(
        idName = "teal",
        displayName = "Teal",
        symbolChar = "✚",
        symbolDescription = "Cross",
        lightColor = Color(0xFF0D9488),
        darkColor = Color(0xFF14B8A6),
        highContrastColor = Color(0xFF00FFFF),
        onColor = Color.Black
    ),
    BROWN(
        idName = "brown",
        displayName = "Brown",
        symbolChar = "☾",
        symbolDescription = "Crescent",
        lightColor = Color(0xFF854D0E),
        darkColor = Color(0xFFA16207),
        highContrastColor = Color(0xFF8B4513),
        onColor = Color.White
    ),
    WHITE(
        idName = "white",
        displayName = "White",
        symbolChar = "○",
        symbolDescription = "Ring",
        lightColor = Color(0xFFF1F5F9),
        darkColor = Color(0xFFCBD5E1),
        highContrastColor = Color(0xFFFFFFFF),
        onColor = Color.Black
    );

    companion object {
        fun fromId(id: String): Colour = entries.firstOrNull { it.idName.equals(id, ignoreCase = true) } ?: RED
    }
}

enum class Direction(val arrowSymbol: String, val rowDelta: Int, val colDelta: Int) {
    UP("↑", -1, 0),
    DOWN("↓", 1, 0),
    LEFT("←", 0, -1),
    RIGHT("→", 0, 1)
}

enum class Axis {
    HORIZONTAL,
    VERTICAL
}

enum class VehicleType(val defaultSeats: Int, val displayName: String) {
    CAR(4, "Car"),
    MINIBUS(6, "Trotro"),
    BUS(10, "Bus")
}

data class Vehicle(
    val id: String,
    val type: VehicleType,
    val colour: Colour,
    val row: Int,
    val col: Int,
    val length: Int,
    val axis: Axis,
    val direction: Direction,
    val seats: Int,
    val boarded: Int = 0,
    val slogan: String = "Nyame Bekyere"
) {
    val isFull: Boolean get() = boarded >= seats
    val freeSeats: Int get() = (seats - boarded).coerceAtLeast(0)

    fun occupiedCells(): List<Pair<Int, Int>> {
        return (0 until length).map { offset ->
            if (axis == Axis.HORIZONTAL) {
                Pair(row, col + offset)
            } else {
                Pair(row + offset, col)
            }
        }
    }
}

enum class GameStatus {
    PLAYING,
    WON,
    LOST
}

sealed class EngineEvent {
    data class Moved(val vehicleId: String, val slotIndex: Int) : EngineEvent()
    data class Blocked(val vehicleId: String) : EngineEvent()
    object NoFreeSlot : EngineEvent()
    data class Boarded(val colour: Colour, val slotIndex: Int, val remainingSeats: Int) : EngineEvent()
    data class Departed(val vehicleId: String, val slotIndex: Int) : EngineEvent()
    object Won : EngineEvent()
    object Lost : EngineEvent()
}

data class EngineState(
    val rows: Int,
    val cols: Int,
    val carPark: List<Vehicle>,
    val slots: List<Vehicle?>,
    val queue: List<Colour>,
    val moves: Int = 0,
    val status: GameStatus = GameStatus.PLAYING
) {
    val totalPassengersRemaining: Int get() = queue.size
    val freeSlotsCount: Int get() = slots.count { it == null }
}

enum class TutorialStep(val title: String, val message: String) {
    TAP_TO_MOVE(
        "Tap to Move",
        "Tap the green trotro with a clear exit path. It will drive directly into the parking bay."
    ),
    BLOCKED_MOVE(
        "Blocked Path",
        "Vehicles cannot move if another car is blocking their arrow direction."
    ),
    BOARDING(
        "Matching Boarding",
        "Passengers at the front of the queue automatically board vehicles of the same colour!"
    ),
    FULL_SLOTS(
        "Parking Slots",
        "Parking slots are limited. Fill a trotro to make it depart and free up a slot."
    )
}

enum class LevelTier(
    val title: String,
    val subtitle: String,
    val description: String,
    val minLevel: Int,
    val maxLevel: Int,
    val badgeColor: Color
) {
    BEGINNER(
        title = "Tier 1: Morning Light",
        subtitle = "Beginner Lorry Park",
        description = "Introductory traffic jams (3-4 Trotros, 2-3 Colours)",
        minLevel = 1,
        maxLevel = 10,
        badgeColor = Color(0xFF10B981)
    ),
    INTERMEDIATE(
        title = "Tier 2: Midday Rush",
        subtitle = "Intermediate Gridlock",
        description = "Interlocking cross-cutting routes (4-5 Trotros, 3-4 Colours)",
        minLevel = 11,
        maxLevel = 20,
        badgeColor = Color(0xFFF59E0B)
    ),
    ADVANCED(
        title = "Tier 3: Evening Commute",
        subtitle = "Advanced Congestion",
        description = "Multi-vehicle blocking cascades (5-6 Trotros, 4-5 Colours)",
        minLevel = 21,
        maxLevel = 30,
        badgeColor = Color(0xFFF97316)
    ),
    EXPERT(
        title = "Tier 4: Circle Jam Master",
        subtitle = "Master Traffic Jam",
        description = "Deep spatial dependencies & tight bay management (6-8 Trotros, 4-6 Colours)",
        minLevel = 31,
        maxLevel = 40,
        badgeColor = Color(0xFFEF4444)
    );

    companion object {
        fun fromLevelNumber(num: Int): LevelTier = when {
            num <= 10 -> BEGINNER
            num <= 20 -> INTERMEDIATE
            num <= 30 -> ADVANCED
            else -> EXPERT
        }

        fun fromLevelId(id: String): LevelTier {
            val num = id.removePrefix("L").toIntOrNull() ?: 1
            return fromLevelNumber(num)
        }
    }
}

data class LevelData(
    val formatVersion: Int = 1,
    val id: String,
    val name: String,
    val tutorial: List<String> = emptyList(),
    val gridCols: Int,
    val gridRows: Int,
    val slots: Int,
    val colours: List<Colour>,
    val vehicles: List<Vehicle>,
    val queue: List<Colour>,
    val par: Int,
    val checkedBy: String = "D. F. Twum",
    val checkedDate: String = "2026-09-26"
) {
    val difficultyTier: LevelTier get() = LevelTier.fromLevelId(id)
}
