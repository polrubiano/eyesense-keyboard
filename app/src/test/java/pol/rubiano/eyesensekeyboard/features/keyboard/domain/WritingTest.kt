package pol.rubiano.eyesensekeyboard.features.keyboard.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.HexCoord
import pol.rubiano.eyesensekeyboard.features.keyboard.domain.hexgrid.PanelSlot

class WritingTest {

    @Test
    fun `a letter goes at the end`() {
        assertEquals("hol", "ho".typed(KeyAssignment.Text("l")))
    }

    @Test
    fun `typing on an empty text starts it`() {
        assertEquals("e", "".typed(KeyAssignment.Text("e")))
    }

    @Test
    fun `the space and whole phrases are typed the same way`() {
        assertEquals("hola ", "hola".typed(KeyAssignment.Text(" ")))
        assertEquals("hola ¿qué tal?", "hola ".typed(KeyAssignment.Text("¿qué tal?")))
    }

    @Test
    fun `delete character removes the last character`() {
        assertEquals("hol", "hola".typed(KeyAssignment.Function(KeyAction.DeleteCharacter)))
    }

    @Test
    fun `delete character on an empty text does nothing`() {
        assertEquals("", "".typed(KeyAssignment.Function(KeyAction.DeleteCharacter)))
    }

    @Test
    fun `delete character also removes a typed space`() {
        assertEquals("hola", "hola ".typed(KeyAssignment.Function(KeyAction.DeleteCharacter)))
    }

    @Test
    fun `clear text wipes everything written`() {
        assertEquals("", "hola ¿qué tal?".typed(KeyAssignment.Function(KeyAction.ClearText)))
    }

    @Test
    fun `clear text on an empty text does nothing`() {
        assertEquals("", "".typed(KeyAssignment.Function(KeyAction.ClearText)))
    }

    @Test
    fun `speak leaves the field empty`() {
        assertEquals("", "hola".typed(KeyAssignment.Function(KeyAction.Speak)))
    }

    @Test
    fun `functions without behaviour don't change the text yet`() {
        (KeyAction.all - KeyAction.DeleteCharacter - KeyAction.ClearText - KeyAction.Speak).forEach { action ->
            assertEquals("hola", "hola".typed(KeyAssignment.Function(action)))
        }
    }

    @Test
    fun `a free slot doesn't change the text`() {
        assertEquals("hola", "hola".typed(null))
    }

    @Test
    fun `a sequence of keys builds the word`() {
        val keys = listOf("h", "o", "l", "a").map { KeyAssignment.Text(it) }
        assertEquals("hola", keys.fold("") { text, key -> text.typed(key) })
    }

    // ---- pressed: the whole state ----

    private val start = Writing(Keyboard(Presets.PANEL, Presets.SPANISH, Presets.DEFAULT_FUNCTIONS))

    private val emotionKey = KeyAssignment.Function(KeyAction.SelectEmotion)

    private fun Writing.pressing(vararg keys: KeyAssignment?): Writing = keys.fold(this) { state, key -> state.pressed(key) }

    /** What the centre hexagon holds right now: "e" on the normal keyboard, "none" in a menu. */
    private val Writing.centre get() = keyboard.keyAt(PanelSlot.Cell(HexCoord.ORIGIN))

    @Test
    fun `it starts empty, with no emotion and the normal keyboard`() {
        assertEquals("", start.text)
        assertNull(start.emotion)
        assertNull(start.openMenu)
        assertEquals(emptyList<SpokenMessage>(), start.spoken)
        assertEquals(start.base, start.keyboard)
    }

    @Test
    fun `pressing keys writes through typed`() {
        val state = start.pressing(
            KeyAssignment.Text("h"), KeyAssignment.Text("i"), KeyAssignment.Function(
                KeyAction.DeleteCharacter))
        assertEquals("h", state.text)
    }

    @Test
    fun `the keyboard follows the open menu`() {
        assertEquals(start.base.showing(Menu.Emotions), start.copy(openMenu = Menu.Emotions).keyboard)
    }

    @Test
    fun `the emotion key opens its menu`() {
        val state = start.pressing(emotionKey)
        assertEquals(Menu.Emotions, state.openMenu)
        assertEquals(KeyAssignment.Choice(null), state.centre)
    }

    @Test
    fun `picking an emotion stores it and closes the menu`() {
        val state = start.pressing(emotionKey, KeyAssignment.Choice(Emotion.SAD))
        assertEquals(Emotion.SAD, state.emotion)
        assertNull(state.openMenu)
        assertEquals(KeyAssignment.Text("e"), state.centre)
    }

    @Test
    fun `the centre removes the emotion and closes the menu`() {
        val state = start.pressing(emotionKey, KeyAssignment.Choice(Emotion.HAPPY), emotionKey, KeyAssignment.Choice(null))
        assertNull(state.emotion)
        assertNull(state.openMenu)
    }

    @Test
    fun `the emotion key closes the menu without changes`() {
        val state = start.pressing(emotionKey, KeyAssignment.Choice(Emotion.ANGRY), emotionKey, emotionKey)
        assertEquals(Emotion.ANGRY, state.emotion)
        assertNull(state.openMenu)
    }

    @Test
    fun `the menu doesn't touch the text`() {
        val hola = start.copy(text = "hola")
        assertEquals("hola", hola.pressing(emotionKey, KeyAssignment.Choice(Emotion.HAPPY)).text)
    }

    @Test
    fun `a free slot in a menu changes nothing`() {
        val open = start.pressing(emotionKey)
        assertEquals(open, open.pressed(null))
    }

    @Test(expected = IllegalStateException::class)
    fun `picking none with no menu open is a bug`() {
        start.pressed(KeyAssignment.Choice(null))
    }

    // ---- speak ----

    private val speakKey = KeyAssignment.Function(KeyAction.Speak)

    @Test
    fun `speaking keeps the message and empties the field`() {
        val state = start.copy(text = "hola").pressed(speakKey)
        assertEquals(listOf(SpokenMessage(emotion = null, text = "hola")), state.spoken)
        assertEquals("", state.text)
    }

    @Test
    fun `speaking keeps the emotion for the next message`() {
        val state = start.copy(text = "hola", emotion = Emotion.HAPPY).pressed(speakKey)
        assertEquals(Emotion.HAPPY, state.spoken.last().emotion)
        assertEquals(Emotion.HAPPY, state.emotion)
    }

    @Test
    fun `spoken messages pile up, oldest first`() {
        val state = start.copy(text = "hola").pressing(speakKey, KeyAssignment.Text("y"), speakKey)
        assertEquals(listOf(SpokenMessage(null, "hola"), SpokenMessage(null, "y")), state.spoken)
    }

    @Test
    fun `each message keeps the emotion it was said with`() {
        val state = start.copy(text = "hola", emotion = Emotion.HAPPY)
            .pressing(speakKey, emotionKey, KeyAssignment.Choice(Emotion.SAD), KeyAssignment.Text("y"), speakKey)
        assertEquals(listOf(Emotion.HAPPY, Emotion.SAD), state.spoken.map { it.emotion })
    }

    @Test
    fun `speaking an empty field changes nothing`() {
        val said = start.copy(spoken = listOf(SpokenMessage(emotion = null, text = "hola")))
        assertEquals(said, said.pressed(speakKey))
    }

    @Test
    fun `the payload carries the emotion id and the text`() {
        assertEquals("[happy][hola]", SpokenMessage(Emotion.HAPPY, "hola").payload)
    }

    @Test
    fun `the payload leaves the emotion empty when there is none`() {
        assertEquals("[][hola]", SpokenMessage(null, "hola").payload)
    }
}
