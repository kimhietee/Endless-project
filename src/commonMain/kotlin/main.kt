import korlibs.korge.Korge
import korlibs.korge.scene.sceneContainer
import korlibs.image.color.Colors
import utils.Constants
import scenes.MainMenuScene

suspend fun main() = Korge(
    windowWidth = Constants.SCREEN_WIDTH,
    windowHeight = Constants.SCREEN_HEIGHT,
    virtualWidth = Constants.SCREEN_WIDTH,
    virtualHeight = Constants.SCREEN_HEIGHT,
    bgcolor = Colors["#000000"],
    title = "Fighting Kimhie"
) {
    val scenes = sceneContainer()
    scenes.changeTo { MainMenuScene() }
}
