import korlibs.korge.view.*
import korlibs.image.color.Colors
import korlibs.korge.input.onDown
import korlibs.korge.input.onUp
import korlibs.korge.input.onUpOutside


class TouchButton(
    width: Double,
    height: Double,
    private val onChange: (Boolean) -> Unit
) : Container() {

    private val bg = solidRect(width, height, Colors["#444444"])
    private val highlight = solidRect(width, height, Colors["#888888"]).apply {
        alpha = 0.0
    }

    private var pressed = false

    init {
        addChild(bg)
        addChild(highlight)

        this.alpha = 0.85

        // ✅ ONLY SAFE MULTI-TOUCH METHOD IN KORGE VIEW SYSTEM
        onDown {
            pressed = true
            highlight.alpha = 0.5
            onChange(true)
        }

        onUp {
            pressed = false
            highlight.alpha = 0.0
            onChange(false)
        }

        onUpOutside {
            pressed = false
            highlight.alpha = 0.0
            onChange(false)
        }
    }
}
