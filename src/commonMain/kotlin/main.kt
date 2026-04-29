import korlibs.korge.Korge
import korlibs.korge.scene.sceneContainer
import korlibs.image.color.Colors
//import korlibs.math.geom.Size

//suspend fun main() = Korge(
//    windowSize = Size(1024, 720), // Width and Height of the game window
//    backgroundColor = Colors["#1A237E"], // Dark blue background
//    title = "Fighting Kimhie"
//) {
//    val scenes = sceneContainer()
//    scenes.changeTo { GameScene() }
//}

suspend fun main() = Korge(
    windowWidth = Constants.SCREEN_WIDTH,
    windowHeight = Constants.SCREEN_HEIGHT,
    virtualWidth = Constants.SCREEN_WIDTH,
    virtualHeight = Constants.SCREEN_HEIGHT,
    bgcolor = Colors["#1A237E"],
    title = "Fighting Kimhie"
) {
    val scenes = sceneContainer()
    scenes.changeTo { MainMenuScene() }
//    scenes.changeTo { GameScene() }
}


