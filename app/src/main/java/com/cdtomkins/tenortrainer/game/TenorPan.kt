package com.cdtomkins.tenortrainer.game

/** Octave band a note sits in on the pan; shown in the prompt so repeated note names are unambiguous. */
enum class Register(val label: String) { LOW("low"), MIDDLE("middle"), HIGH("high") }

/**
 * A note's tap target in pixels of the pan image (see `tenor_pan.jpg`, 1848 x 1848).
 * Positions were estimated from the note labels in the photo, so adjust them here if a target feels off.
 */
data class PanNote(val name: String, val register: Register, val x: Float, val y: Float, val radius: Float) {
    /** The text shown to the player, for example "high C#". */
    val promptText: String get() = "${register.label} $name"

    /** Distance from a point to this note's edge; negative inside the note. */
    fun edgeDistance(px: Float, py: Float): Double {
        val dx = (px - x).toDouble()
        val dy = (py - y).toDouble()
        return Math.hypot(dx, dy) - radius
    }
}

object TenorPan {
    const val IMAGE_SIZE = 1848

    private const val OUTER = 150f
    private const val INNER = 95f

    val notes: List<PanNote> = listOf(
        // Outer ring, clockwise from the top: low register
        PanNote("C#", Register.LOW, 520f, 225f, OUTER),
        PanNote("F#", Register.LOW, 900f, 115f, OUTER),
        PanNote("B", Register.LOW, 1222f, 178f, OUTER),
        PanNote("E", Register.LOW, 1538f, 402f, OUTER),
        PanNote("A", Register.LOW, 1710f, 752f, OUTER),
        PanNote("D", Register.LOW, 1690f, 1200f, OUTER),
        PanNote("G", Register.LOW, 1420f, 1578f, OUTER),
        PanNote("C", Register.LOW, 980f, 1745f, OUTER),
        PanNote("F", Register.LOW, 470f, 1605f, OUTER),
        PanNote("Bb", Register.LOW, 185f, 1300f, OUTER),
        PanNote("Eb", Register.LOW, 110f, 890f, OUTER),
        PanNote("Ab", Register.LOW, 225f, 490f, OUTER),

        // Inner area. Repeated names are split so the copy nearest the pan centre is high.
        PanNote("F#", Register.MIDDLE, 890f, 600f, INNER),
        PanNote("B", Register.MIDDLE, 1050f, 580f, INNER),
        PanNote("C#", Register.MIDDLE, 760f, 700f, INNER),
        PanNote("E", Register.MIDDLE, 1150f, 720f, INNER),
        PanNote("Ab", Register.MIDDLE, 590f, 790f, INNER),
        PanNote("E", Register.HIGH, 995f, 800f, INNER),
        PanNote("C#", Register.HIGH, 885f, 840f, INNER),
        PanNote("A", Register.MIDDLE, 1268f, 860f, INNER),
        PanNote("Eb", Register.MIDDLE, 625f, 960f, INNER),
        PanNote("Eb", Register.HIGH, 815f, 960f, INNER),
        PanNote("D", Register.HIGH, 1015f, 910f, INNER),
        PanNote("C", Register.HIGH, 915f, 985f, INNER),
        PanNote("D", Register.MIDDLE, 1185f, 1020f, INNER),
        PanNote("Bb", Register.MIDDLE, 600f, 1135f, INNER),
        PanNote("F", Register.MIDDLE, 760f, 1195f, INNER),
        PanNote("C", Register.MIDDLE, 945f, 1180f, INNER),
        PanNote("G", Register.MIDDLE, 1130f, 1190f, INNER),
    )
}
