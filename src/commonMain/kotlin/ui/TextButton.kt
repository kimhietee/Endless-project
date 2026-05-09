package ui

import korlibs.image.color.Colors
import korlibs.korge.input.onClick
import korlibs.korge.input.onOut
import korlibs.korge.input.onOver
import korlibs.korge.view.*
import korlibs.korge.view.align.centerOn
import managers.GameAssets

class TextButton(
    width: Double,
    height: Double,
    val text: String,
    onClickAction: () -> Unit
) : Container() {

    private val bg = image(GameAssets.buttonBgSlice).apply {
        smoothing = false
        this.width = width
        this.height = height
    }

    private val label = text(text, textSize = 24.0, color = Colors.WHITE, font = GameAssets.customFont) {
        centerOn(bg)
    }

    var isEnabled: Boolean = true
        set(value) {
            field = value
            alpha = if (value) 1.0 else 0.5
        }
        
    fun setText(newText: String) {
        label.text = newText
        label.centerOn(bg)
    }

    init {
        addChild(bg)
        addChild(label)  // Add label as a child so it renders on top
        onOver { if (isEnabled) alpha = 0.8 }
        onOut { if (isEnabled) alpha = 1.0 }
        onClick { 
            if (isEnabled) onClickAction()
        }
    }
}
