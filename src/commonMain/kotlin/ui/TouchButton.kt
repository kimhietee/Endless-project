package ui

import korlibs.image.bitmap.BmpSlice
import korlibs.korge.view.*

class TouchButton(
    width: Double,
    height: Double,
    slice: BmpSlice
) : Container() {

    private val btn = image(slice).apply {
        smoothing = false
        this.width = width
        this.height = height
    }

    var isPressed: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                btn.colorMul = if (value) korlibs.image.color.Colors["#888888"] else korlibs.image.color.Colors.WHITE
            }
        }

    init {
        addChild(btn)
        this.alpha = 0.85
    }
}
