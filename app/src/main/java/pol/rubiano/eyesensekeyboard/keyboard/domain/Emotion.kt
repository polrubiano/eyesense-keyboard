package pol.rubiano.eyesensekeyboard.keyboard.domain

/**
 * Emotion attached to the message. It is **context, not text**: it isn't written into the field,
 * it travels alongside the message (in the future, together with the prompt sent to the AI).
 *
 * Today the user picks it with [KeyAction.SelectEmotion]; later EyeSense's real-time
 * detection will be able to fill in this same value.
 *
 * [id] is stable because it's a value that will be sent to other systems.
 */
enum class Emotion(val id: String) {
    HAPPY("happy"),
    ANGRY("angry"),
    SAD("sad"),
    ;

    companion object {
        fun fromId(id: String): Emotion? = entries.firstOrNull { it.id == id }
    }
}
