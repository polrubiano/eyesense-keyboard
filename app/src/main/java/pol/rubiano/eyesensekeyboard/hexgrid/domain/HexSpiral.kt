package pol.rubiano.eyesensekeyboard.hexgrid.domain

/**
 * Cells of ring [radius] around the origin, clockwise starting from east.
 *
 * How it walks: ring k has 6 "corners" (each direction × k). From each corner we walk
 * k steps towards the next corner. 6 sides × k steps = 6k cells.
 */
fun hexRing(radius: Int): List<HexCoord> {
    require(radius >= 0) { "The radius can't be negative: $radius" }
    if (radius == 0) return listOf(HexCoord.ORIGIN)

    val directions = HexCoord.DIRECTIONS
    return buildList {
        for (side in directions.indices) {
            var cell = directions[side] * radius
            // To reach the next corner (clockwise), we walk two directions further along the list.
            val step = directions[(side + 2) % directions.size]
            repeat(radius) {
                add(cell)
                cell += step
            }
        }
    }
}

/**
 * Infinite spiral from the centre outwards: ring 0, ring 1, ring 2...
 * and clockwise within each ring.
 *
 * It's a lazy [Sequence]: cells are only computed as they are requested, so whoever
 * uses it decides where to stop (for example with `takeWhile { it.ring <= 3 }`).
 */
fun hexSpiral(): Sequence<HexCoord> =
    generateSequence(0) { it + 1 }.flatMap { hexRing(it) }
