import korlibs.image.color.Colors
import korlibs.korge.view.*
import korlibs.math.geom.Point

class HUD(private val player: Character) : Container() {

    // -------------------------------------------------------
    // LAYOUT CONSTANTS
    // -------------------------------------------------------
    private val startX     = 20.0
    private val startY     = 20.0
    private val barWidth   = 200.0
    private val barHeight  = 50.0
    private val barGap     = 10.0   // vertical gap between HP and mana bar
    private val labelWidth = 40.0   // space reserved left of bar for label text

    // bar backgrounds (dark track)
    private val hpBg   = solidRect(barWidth, barHeight, Colors["#330000"])
    private val manaBg = solidRect(barWidth, barHeight, Colors["#001133"])

    // filled bars
    private val hpBar   = solidRect(barWidth, barHeight, Colors["#22cc44"])
    private val manaBar = solidRect(barWidth, barHeight, Colors["#2299ff"])

    // labels
    private val hpLabel   = text("HP",   textSize = 14.0, color = Colors.WHITE)
    private val manaLabel = text("MP",   textSize = 14.0, color = Colors.WHITE)
    private val hpValue   = text("",     textSize = 13.0, color = Colors.WHITE)
    private val manaValue = text("",     textSize = 13.0, color = Colors.WHITE)

    init {
        val barX = startX + labelWidth

        // HP row
        hpLabel.xy(startX, startY + 2.0)
        hpBg.xy(barX, startY)
        hpBar.xy(barX, startY)
        hpValue.xy(barX + barWidth + 6.0, startY + 2.0)

        // Mana row
        val manaY = startY + barHeight + barGap
        manaLabel.xy(startX, manaY + 2.0)
        manaBg.xy(barX, manaY)
        manaBar.xy(barX, manaY)
        manaValue.xy(barX + barWidth + 6.0, manaY + 2.0)
    }

    // -------------------------------------------------------
    // CALLED EVERY FRAME FROM GameScene addUpdater
    // -------------------------------------------------------
    fun update() {
        val hpRatio   = (player.health / player.maxHealth).coerceIn(0.0, 1.0)
        val manaRatio = (player.mana   / player.maxMana  ).coerceIn(0.0, 1.0)

        hpBar.width   = barWidth * hpRatio
        manaBar.width = barWidth * manaRatio

        // color shifts: green → yellow → red as HP drops
        hpBar.colorMul = when {
            hpRatio > 0.4 -> Colors["#22cc44"]   // green
            hpRatio > 0.2 -> Colors["#ffcc00"]   // yellow
            else          -> Colors["#cc2222"]   // red
        }

        hpValue.text   = "${player.health.toInt()} / ${player.maxHealth.toInt()}"
        manaValue.text = "${player.mana.toInt()} / ${player.maxMana.toInt()}"
    }
}
