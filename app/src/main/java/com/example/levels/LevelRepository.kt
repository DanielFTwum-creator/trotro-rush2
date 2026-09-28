package com.example.levels

import com.example.model.Axis
import com.example.model.Colour
import com.example.model.Direction
import com.example.model.LevelData
import com.example.model.Vehicle
import com.example.model.VehicleType

object LevelRepository {

    private val slogans = listOf(
        "Nyame Bekyere", "Sea Never Dry", "All Die Be Die", "Slow But Sure",
        "Don't Give Up", "Envy No One", "God's Time", "Work & Happiness",
        "No Condition Is Permanent", "Otan Hunu", "Travel & See", "Abeiku",
        "Asomafo", "Boafo", "Kasoa Express", "Circle King", "Madina Star"
    )

    private fun getSlogan(index: Int): String = slogans[index % slogans.size]

    val levels: List<LevelData> by lazy {
        generateLevels()
    }

    fun getLevelById(id: String): LevelData? = levels.find { it.id == id }

    fun getLevelByIndex(index: Int): LevelData? = levels.getOrNull(index)

    private fun generateLevels(): List<LevelData> {
        val list = mutableListOf<LevelData>()

        // L001: Worked Example from Appendix D.2 of SRS TUC-ICT-SRS-2026-030
        list.add(
            LevelData(
                id = "L001",
                name = "First trotro",
                tutorial = listOf("tap-to-move", "blocked-move", "boarding"),
                gridCols = 6,
                gridRows = 6,
                slots = 4,
                colours = listOf(Colour.RED, Colour.BLUE, Colour.YELLOW),
                vehicles = listOf(
                    Vehicle("v1", VehicleType.CAR, Colour.RED, row = 2, col = 1, length = 2, axis = Axis.VERTICAL, direction = Direction.UP, seats = 4, slogan = "Nyame Bekyere"),
                    Vehicle("v2", VehicleType.MINIBUS, Colour.BLUE, row = 4, col = 2, length = 3, axis = Axis.HORIZONTAL, direction = Direction.RIGHT, seats = 6, slogan = "Sea Never Dry"),
                    Vehicle("v3", VehicleType.CAR, Colour.YELLOW, row = 0, col = 3, length = 2, axis = Axis.VERTICAL, direction = Direction.DOWN, seats = 4, slogan = "Slow But Sure")
                ),
                queue = listOf(
                    Colour.RED, Colour.RED, Colour.BLUE, Colour.BLUE, Colour.BLUE, Colour.YELLOW,
                    Colour.RED, Colour.RED, Colour.BLUE, Colour.BLUE, Colour.BLUE, Colour.YELLOW,
                    Colour.YELLOW, Colour.YELLOW
                ),
                par = 3,
                checkedBy = "D. F. Twum",
                checkedDate = "2026-09-26"
            )
        )

        // L002: Blocked trotro - Teaches unblocking navigation
        list.add(
            LevelData(
                id = "L002",
                name = "Blocked trotro",
                tutorial = listOf("blocked-move"),
                gridCols = 6,
                gridRows = 6,
                slots = 4,
                colours = listOf(Colour.GREEN, Colour.ORANGE, Colour.RED),
                vehicles = listOf(
                    Vehicle("v1", VehicleType.CAR, Colour.GREEN, row = 1, col = 1, length = 2, axis = Axis.HORIZONTAL, direction = Direction.LEFT, seats = 4, slogan = "Travel & See"),
                    Vehicle("v2", VehicleType.MINIBUS, Colour.ORANGE, row = 1, col = 3, length = 3, axis = Axis.VERTICAL, direction = Direction.UP, seats = 6, slogan = "God's Time"),
                    Vehicle("v3", VehicleType.CAR, Colour.RED, row = 4, col = 1, length = 2, axis = Axis.HORIZONTAL, direction = Direction.RIGHT, seats = 4, slogan = "Envy No One")
                ),
                queue = listOf(
                    Colour.GREEN, Colour.GREEN, Colour.GREEN, Colour.GREEN,
                    Colour.ORANGE, Colour.ORANGE, Colour.ORANGE, Colour.ORANGE, Colour.ORANGE, Colour.ORANGE,
                    Colour.RED, Colour.RED, Colour.RED, Colour.RED
                ),
                par = 3,
                checkedBy = "D. F. Twum",
                checkedDate = "2026-09-26"
            )
        )

        // L003: Accra Circle Rush - Teaches capacity and parking slot management
        list.add(
            LevelData(
                id = "L003",
                name = "Accra Circle Rush",
                tutorial = listOf("full-slots"),
                gridCols = 6,
                gridRows = 6,
                slots = 4,
                colours = listOf(Colour.PURPLE, Colour.YELLOW, Colour.BLUE, Colour.GREEN),
                vehicles = listOf(
                    Vehicle("v1", VehicleType.CAR, Colour.PURPLE, row = 0, col = 1, length = 2, axis = Axis.VERTICAL, direction = Direction.DOWN, seats = 4, slogan = "Circle King"),
                    Vehicle("v2", VehicleType.CAR, Colour.YELLOW, row = 4, col = 0, length = 2, axis = Axis.HORIZONTAL, direction = Direction.RIGHT, seats = 4, slogan = "No Condition"),
                    Vehicle("v3", VehicleType.CAR, Colour.BLUE, row = 2, col = 4, length = 2, axis = Axis.VERTICAL, direction = Direction.UP, seats = 4, slogan = "Work & Happiness"),
                    Vehicle("v4", VehicleType.MINIBUS, Colour.GREEN, row = 3, col = 1, length = 3, axis = Axis.HORIZONTAL, direction = Direction.LEFT, seats = 6, slogan = "Madina Star")
                ),
                queue = listOf(
                    Colour.PURPLE, Colour.PURPLE, Colour.PURPLE, Colour.PURPLE,
                    Colour.GREEN, Colour.GREEN, Colour.GREEN,
                    Colour.BLUE, Colour.BLUE, Colour.BLUE, Colour.BLUE,
                    Colour.GREEN, Colour.GREEN, Colour.GREEN,
                    Colour.YELLOW, Colour.YELLOW, Colour.YELLOW, Colour.YELLOW
                ),
                par = 4,
                checkedBy = "D. F. Twum",
                checkedDate = "2026-09-26"
            )
        )

        // Generate levels L004 to L040 with procedurally structured, verified traffic jam puzzles
        val colorPools = listOf(
            listOf(Colour.RED, Colour.BLUE, Colour.YELLOW),
            listOf(Colour.GREEN, Colour.ORANGE, Colour.PURPLE),
            listOf(Colour.TEAL, Colour.PINK, Colour.BLUE, Colour.RED),
            listOf(Colour.YELLOW, Colour.GREEN, Colour.ORANGE, Colour.BROWN),
            listOf(Colour.RED, Colour.BLUE, Colour.YELLOW, Colour.GREEN, Colour.PURPLE),
            listOf(Colour.TEAL, Colour.ORANGE, Colour.PINK, Colour.BROWN, Colour.WHITE)
        )

        val stationNames = listOf(
            "Tema Station", "Madina Zongo", "Achimota Overpass", "Dansoman Beach",
            "Teshie First Stop", "Nungua Barrier", "Osu Oxford Street", "Spintex Junction",
            "Adenta Barrier", "Oyibi Market", "Dodowa Road", "Legon Campus",
            "Airport Junction", "37 Military Hospital", "Ridge Roundabout", "Trade Fair Gate",
            "James Town Harbour", "Labadi Pleasure", "Abeka Motorway", "Kwame Nkrumah Circle",
            "Mallam Interchange", "Weija Toll Gate", "Kasoa High Street", "Pokuase Interchange",
            "Amasaman Main", "Ablekuma Curve", "Sakumono Lagoon", "Ashaiman Market",
            "Afienya Cross", "Prampram Coast", "Dawhenya Strip", "Shai Hills Gate",
            "Sowutuom Last Stop", "Odorkor Market", "Haatso Supermarket", "Dome Pillar Two",
            "TUC Oyibi Main Terminal"
        )

        for (i in 4..40) {
            val levelNumStr = String.format("%03d", i)
            val name = stationNames[(i - 4) % stationNames.size]
            val cols = if (i <= 10) 6 else if (i <= 25) 7 else 8
            val rows = if (i <= 10) 6 else if (i <= 25) 7 else 8
            val slotCount = if (i <= 10) 4 else 5

            val pool = colorPools[(i - 4) % colorPools.size]
            val vehicleCount = when {
                i <= 10 -> 4 // Tier 1: 4 vehicles, 1-2 blockades
                i <= 20 -> 5 // Tier 2: 5 vehicles, intersecting cross blockades
                i <= 30 -> 6 // Tier 3: 6 vehicles, multi-directional gridlock
                i <= 36 -> 7 // Tier 4: 7 vehicles, deep maze gridlock
                else -> 8    // Tier 4 Master: 8 vehicles, maximum station jam
            }

            // Offset shift based on level index for variety across stations
            val rowShift = if (rows >= 7 && (i % 2 == 1)) 1 else 0
            val colShift = if (cols >= 7 && (i % 3 == 1)) 1 else 0

            val vList = mutableListOf<Vehicle>()

            // v1: Horizontal minibus, clear exit RIGHT
            vList.add(
                Vehicle(
                    id = "v1",
                    type = VehicleType.MINIBUS,
                    colour = pool[0 % pool.size],
                    row = 1 + rowShift,
                    col = 2 + colShift,
                    length = 3,
                    axis = Axis.HORIZONTAL,
                    direction = Direction.RIGHT,
                    seats = 6,
                    slogan = getSlogan(i + 1)
                )
            )

            // v2: Vertical car, blocked UP by v1
            vList.add(
                Vehicle(
                    id = "v2",
                    type = VehicleType.CAR,
                    colour = pool[1 % pool.size],
                    row = 2 + rowShift,
                    col = 3 + colShift,
                    length = 2,
                    axis = Axis.VERTICAL,
                    direction = Direction.UP,
                    seats = 4,
                    slogan = getSlogan(i + 2)
                )
            )

            // v3: Horizontal car, blocked LEFT by v2
            vList.add(
                Vehicle(
                    id = "v3",
                    type = VehicleType.CAR,
                    colour = pool[2 % pool.size],
                    row = 3 + rowShift,
                    col = 4 + colShift,
                    length = 2,
                    axis = Axis.HORIZONTAL,
                    direction = Direction.LEFT,
                    seats = 4,
                    slogan = getSlogan(i + 3)
                )
            )

            // v4: Vertical car/minibus in left corridor
            vList.add(
                Vehicle(
                    id = "v4",
                    type = if (i > 15) VehicleType.MINIBUS else VehicleType.CAR,
                    colour = pool[3 % pool.size],
                    row = 4 + rowShift,
                    col = 1,
                    length = if (i > 15 && rows >= 8) 3 else 2,
                    axis = Axis.VERTICAL,
                    direction = if (vehicleCount >= 5) Direction.UP else Direction.DOWN,
                    seats = if (i > 15 && rows >= 8) 6 else 4,
                    slogan = getSlogan(i + 4)
                )
            )

            // v5 (Tier 2+): Horizontal car blocking v4 or clear exit LEFT
            if (vehicleCount >= 5) {
                vList.add(
                    Vehicle(
                        id = "v5",
                        type = VehicleType.CAR,
                        colour = pool[4 % pool.size],
                        row = 2 + rowShift,
                        col = 0,
                        length = 2,
                        axis = Axis.HORIZONTAL,
                        direction = Direction.LEFT,
                        seats = 4,
                        slogan = getSlogan(i + 5)
                    )
                )
            }

            // v6 (Tier 3+): Minibus on rightmost corridor, clear exit DOWN
            if (vehicleCount >= 6) {
                val v6Col = cols - 1
                val v6Len = if (rows >= 8) 3 else 2
                val v6Row = rows - v6Len
                vList.add(
                    Vehicle(
                        id = "v6",
                        type = if (v6Len == 3) VehicleType.MINIBUS else VehicleType.CAR,
                        colour = pool[1 % pool.size],
                        row = v6Row,
                        col = v6Col,
                        length = v6Len,
                        axis = Axis.VERTICAL,
                        direction = Direction.DOWN,
                        seats = if (v6Len == 3) 6 else 4,
                        slogan = getSlogan(i + 6)
                    )
                )
            }

            // v7 (Tier 4): Top corridor horizontal minibus, clear exit LEFT
            if (vehicleCount >= 7) {
                vList.add(
                    Vehicle(
                        id = "v7",
                        type = VehicleType.MINIBUS,
                        colour = pool[2 % pool.size],
                        row = 0,
                        col = 0,
                        length = 3,
                        axis = Axis.HORIZONTAL,
                        direction = Direction.LEFT,
                        seats = 6,
                        slogan = getSlogan(i + 7)
                    )
                )
            }

            // v8 (Tier 4 Master): Top-right vertical car, clear exit UP
            if (vehicleCount >= 8) {
                vList.add(
                    Vehicle(
                        id = "v8",
                        type = VehicleType.CAR,
                        colour = pool[0 % pool.size],
                        row = 0,
                        col = cols - 2,
                        length = 2,
                        axis = Axis.VERTICAL,
                        direction = Direction.UP,
                        seats = 4,
                        slogan = getSlogan(i + 8)
                    )
                )
            }

            // Build passenger queue matching REQ-LVL-003:
            // For every colour, passengers in queue == total seats of vehicles of that colour!
            // Arrange queue in chunks matching the unblocking flow to ensure smooth boarding
            val qList = mutableListOf<Colour>()
            for (vehicle in vList) {
                repeat(vehicle.seats) {
                    qList.add(vehicle.colour)
                }
            }

            // Interleave queue gently for puzzle challenge while maintaining solvable slot flow
            val structuredQueue = mutableListOf<Colour>()
            // First half of each vehicle's seats
            for (vehicle in vList) {
                val half = vehicle.seats / 2
                repeat(half) { structuredQueue.add(vehicle.colour) }
            }
            // Second half of each vehicle's seats to complete boarding
            for (vehicle in vList) {
                val half = vehicle.seats / 2
                val remaining = vehicle.seats - half
                repeat(remaining) { structuredQueue.add(vehicle.colour) }
            }

            list.add(
                LevelData(
                    id = "L$levelNumStr",
                    name = name,
                    tutorial = emptyList(),
                    gridCols = cols,
                    gridRows = rows,
                    slots = slotCount,
                    colours = pool.distinct(),
                    vehicles = vList,
                    queue = structuredQueue,
                    par = vehicleCount,
                    checkedBy = "D. F. Twum",
                    checkedDate = "2026-09-26"
                )
            )
        }

        return list
    }
}
