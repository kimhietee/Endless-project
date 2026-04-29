import korlibs.korge.scene.Scene
import korlibs.korge.view.*
import korlibs.image.color.Colors
import korlibs.image.bitmap.BmpSlice
import korlibs.image.format.readBitmapSlice
import korlibs.io.file.std.resourcesVfs
import korlibs.time.seconds
import korlibs.math.geom.Point


class GameScene : Scene() {

    override suspend fun SContainer.sceneMain() {

        // -------------------------------------------------------
        // BACKGROUND
        // resources/bg/background.png
        // -------------------------------------------------------
        val bgSlice = resourcesVfs["bg/background.png"].readBitmapSlice()
        val bg = image(bgSlice).apply {
            width = Constants.SCREEN_WIDTH.toDouble()
            height = Constants.SCREEN_HEIGHT.toDouble()
            smoothing = true
            y = Constants.GROUND - Constants.SCREEN_HEIGHT
        }
        addChild(bg)

        // -------------------------------------------------------
        // CHARACTER ASSETS
        // -------------------------------------------------------
        val idleFrames   = loadFrames("fireWizard", "idle_pngs",    "image_0-",  7)
        val runFrames    = loadFrames("fireWizard", "run_pngs",      "Run_",      8)
        val jumpFrames   = loadFrames("fireWizard", "jump_pngs",     "Jump_",     6)
        val attackFrames = loadFrames("fireWizard", "slash_pngs",    "Attack_1_", 10)
        val skillFrames  = loadFrames("fireWizard", "fireball_pngs", "image_0-",  8)  // shared by all 4 skills

        // -------------------------------------------------------
        // GROUND
        // -------------------------------------------------------
        val ground = solidRect(Constants.SCREEN_WIDTH.toDouble(), Constants.SCREEN_HEIGHT - Constants.GROUND, Colors.DARKGREEN)
        ground.xy(0.0, Constants.GROUND)
        addChild(ground)

        // -------------------------------------------------------
        // PLAYER
        // -------------------------------------------------------
        val player = Character(true, idleFrames, runFrames, jumpFrames, attackFrames, skillFrames)
        addChild(player)
        player.xy(100.0, Constants.GROUND - player.characterHeight)


        // -------------------------------------------------------
        // BUTTON IMAGE ASSETS
        // -------------------------------------------------------
        val leftSlice   = resourcesVfs["ui/buttons/btn_left.png"].readBitmapSlice()
        val rightSlice  = resourcesVfs["ui/buttons/btn_right.png"].readBitmapSlice()
        val jumpSlice   = resourcesVfs["ui/buttons/btn_jump.png"].readBitmapSlice()
        val attackSlice = resourcesVfs["ui/buttons/btn_attack.png"].readBitmapSlice()
        val skill1Slice = resourcesVfs["skill_icons/fire_wizard/1.png"].readBitmapSlice()
        val skill2Slice = resourcesVfs["skill_icons/fire_wizard/2.png"].readBitmapSlice()
        val skill3Slice = resourcesVfs["skill_icons/fire_wizard/3.png"].readBitmapSlice()
        val skill4Slice = resourcesVfs["skill_icons/fire_wizard/4.png"].readBitmapSlice()

        // -------------------------------------------------------
        // BUTTON LAYOUT
        // Single row sitting in the ground strip
        // [LEFT][RIGHT][SK1][SK2][SK3][SK4]        [ATK][JUMP]
        // -------------------------------------------------------
        val btnSize = 100.0
        val gap     =   8.0
        val rowY    = Constants.GROUND + (Constants.SCREEN_HEIGHT - Constants.GROUND - btnSize) / 2.0

        val leftBtn  = TouchButton(btnSize, btnSize, leftSlice ).xy(20.0,                   rowY)
        val rightBtn = TouchButton(btnSize, btnSize, rightSlice).xy(20.0 + btnSize + gap,   rowY)

        val skillsStartX = 20.0 + (btnSize + gap) * 2
        val skillBtn1 = TouchButton(btnSize, btnSize, skill1Slice).xy(skillsStartX + (btnSize + gap) * 0, rowY)
        val skillBtn2 = TouchButton(btnSize, btnSize, skill2Slice).xy(skillsStartX + (btnSize + gap) * 1, rowY)
        val skillBtn3 = TouchButton(btnSize, btnSize, skill3Slice).xy(skillsStartX + (btnSize + gap) * 2, rowY)
        val skillBtn4 = TouchButton(btnSize, btnSize, skill4Slice).xy(skillsStartX + (btnSize + gap) * 3, rowY)

        val jumpX   = Constants.SCREEN_WIDTH - 20.0 - btnSize
        val attackX = jumpX - gap - btnSize
        val jumpBtn   = TouchButton(btnSize, btnSize, jumpSlice  ).xy(jumpX,   rowY)
        val attackBtn = TouchButton(btnSize, btnSize, attackSlice).xy(attackX, rowY)

        addChild(leftBtn)
        addChild(rightBtn)
        addChild(skillBtn1)
        addChild(skillBtn2)
        addChild(skillBtn3)
        addChild(skillBtn4)
        addChild(attackBtn)
        addChild(jumpBtn)

        // -------------------------------------------------------
        // MAIN UPDATE LOOP
        // -------------------------------------------------------
        addUpdater { dt ->
            val dtSec = dt.seconds

            TouchInput.left   = false
            TouchInput.right  = false
            TouchInput.jump   = false
            TouchInput.attack = false
            TouchInput.skill1 = false
            TouchInput.skill2 = false
            TouchInput.skill3 = false
            TouchInput.skill4 = false

            for (touch in views.input.activeTouches) {
                val point = Point(touch.x, touch.y)
                if (leftBtn.hitTest(point)   != null) TouchInput.left   = true
                if (rightBtn.hitTest(point)  != null) TouchInput.right  = true
                if (jumpBtn.hitTest(point)   != null) TouchInput.jump   = true
                if (attackBtn.hitTest(point) != null) TouchInput.attack = true
                if (skillBtn1.hitTest(point) != null) TouchInput.skill1 = true
                if (skillBtn2.hitTest(point) != null) TouchInput.skill2 = true
                if (skillBtn3.hitTest(point) != null) TouchInput.skill3 = true
                if (skillBtn4.hitTest(point) != null) TouchInput.skill4 = true
            }

            leftBtn.isPressed   = TouchInput.left
            rightBtn.isPressed  = TouchInput.right
            jumpBtn.isPressed   = TouchInput.jump
            attackBtn.isPressed = TouchInput.attack
            skillBtn1.isPressed = TouchInput.skill1
            skillBtn2.isPressed = TouchInput.skill2
            skillBtn3.isPressed = TouchInput.skill3
            skillBtn4.isPressed = TouchInput.skill4

            player.update(dtSec, views)
        }
    }

    suspend fun loadFrames(
        hero: String,
        folder: String,
        prefix: String,
        count: Int
    ): List<BmpSlice> {
        return (0 until count).map { i ->
            resourcesVfs["$hero/$folder/${prefix}$i.png"].readBitmapSlice()
        }
    }
}
