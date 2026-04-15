import korlibs.korge.scene.Scene
import korlibs.korge.view.*
import korlibs.image.color.Colors
import korlibs.event.*
import korlibs.image.bitmap.BmpSlice
import korlibs.image.format.readBitmapSlice
import korlibs.io.file.std.resourcesVfs
import korlibs.time.seconds


class GameScene : Scene() {

    override suspend fun SContainer.sceneMain() {
        // assets
        val idleFrames = loadFrames("fireWizard", "idle_pngs", "image_0-", 7)
        val runFrames = loadFrames("fireWizard", "run_pngs", "Run_", 8)
        val jumpFrames = loadFrames("fireWizard", "jump_pngs", "Jump_", 6)
        val attackFrames = loadFrames("fireWizard", "fireball_pngs", "image_0-", 8)

        val ground = solidRect(Constants.SCREEN_WIDTH.toDouble(), Constants.GROUND, Colors.DARKGREEN)
        ground.xy(0.0, Constants.GROUND)



        val player = Character(true, idleFrames, runFrames, jumpFrames, attackFrames)


        // LEFT button
        val leftBtn = TouchButton(120.0, 120.0) {
            TouchInput.left = it
        }.xy(50.0, Constants.SCREEN_HEIGHT - 150.0)

        val rightBtn = TouchButton(120.0, 120.0) {
            TouchInput.right = it
        }.xy(200.0, Constants.SCREEN_HEIGHT - 150.0)

        val jumpBtn = TouchButton(120.0, 120.0) {
            TouchInput.jump = it
        }.xy(Constants.SCREEN_WIDTH - 150.0, Constants.SCREEN_HEIGHT - 150.0)

        val attackBtn = TouchButton(120.0, 120.0) {
            TouchInput.attack = it
        }.xy(Constants.SCREEN_WIDTH - 300.0, Constants.SCREEN_HEIGHT - 150.0)

        addChild(leftBtn)
        addChild(rightBtn)
        addChild(jumpBtn)
        addChild(attackBtn)
        addChild(player)
        player.xy(100.0, Constants.GROUND - player.characterHeight)

        addUpdater { dt ->
            val dtSec = dt.seconds

//            if (views.input.keys[Key.UP] || views.input.keys[Key.SPACE] || views.input.keys[Key.W]) {
//                player.jump()
////                println("Player is Jumping: ${player.isJumping()}")
//            }
//
////            else if (views.input.keys[Key.E]) {
////                player.attack(-1.0, dtSec)
////            }
//
//            if (views.input.keys[Key.LEFT] || views.input.keys[Key.A]) {
//                player.run(-1.0, dtSec)
//            }
//
//
//
//
//            if (views.input.keys[Key.RIGHT] || views.input.keys[Key.D]) {
//                player.run(1.0, dtSec)
//            }
//
//
//
//
//            else {
//                player.run(0.0, dtSec)
//            }

            player.update(dtSec, views)

//            player.updatePhysics(dtSec)
////            player.updateAnimation(dtSec)
//            player.handleAnimation(dtSec)







        }
    }



    // HELEPER
    suspend fun loadFrames(
        hero: String,
        folder: String,
        prefix: String,
        count: Int
    ) : List<BmpSlice> {
        return (0 until count).map { i ->
            resourcesVfs["$hero/$folder/${prefix}$i.png"].readBitmapSlice()
        }
    }


}
