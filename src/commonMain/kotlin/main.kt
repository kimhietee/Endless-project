import korlibs.korge.Korge
import korlibs.korge.scene.sceneContainer
import korlibs.image.color.Colors
import utils.Constants
import scenes.MainMenuScene
import scenes.LoginScene
import managers.configureFirebase

suspend fun main() = Korge(
    windowWidth = Constants.SCREEN_WIDTH,
    windowHeight = Constants.SCREEN_HEIGHT,
    virtualWidth = Constants.SCREEN_WIDTH,
    virtualHeight = Constants.SCREEN_HEIGHT,
    bgcolor = Colors["#000000"],
    title = "Fighting Kimhie"
) {
    val initError = configureFirebase()
    if (initError != null) {
        println("Firebase initialization warning: $initError")
    }
    val scenes = sceneContainer()
    scenes.changeTo { LoginScene() }
}
