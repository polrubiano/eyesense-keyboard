package pol.rubiano.eyesensekeyboard.keyboard.domain

/**
 * Emotion attached to the message. It is **context, not text**: it isn't written into the field,
 * it travels alongside the message (in the future, together with the prompt sent to the AI).
 *
 * Today the user picks it with [KeyAction.SelectEmotion]; later EyeSense's real-time
 * detection will be able to fill in this same value.
 *
 * [id] is stable because it's a value that will be sent to other systems.
 *
 * It's an [Option]: the user picks it from [Menu.Emotions]. "No emotion" is `null`,
 * not a value of the enum, because "none" isn't an emotion to send anywhere.
 */
enum class Emotion(val id: String, override val pictogram: String) : Option {
    HAPPY("happy", "😊"),
    ANGRY("angry", "😠"),
    SAD("sad", "😢"),
    ;

    companion object {
        fun fromId(id: String): Emotion? = entries.firstOrNull { it.id == id }
    }
}
