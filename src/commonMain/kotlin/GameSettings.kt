/**
 * GameSettings — persistent global state shared across all scenes.
 *
 * These values survive scene transitions (they live on the object, not in a scene).
 * Both flags can be toggled at runtime and take effect immediately.
 *
 * showHitbox    — when true, red/blue/green debug outlines are rendered on all
 *                 hitboxes (player, enemies, attack displays).
 *
 * developerMode — when true, enables:
 *                 • Infinite upgrades (no upgrade-point cost)
 *                 • Level-Up button in GameScene
 *                 • Next Wave button in GameScene (advances game time)
 *
 * When developerMode is set to FALSE, GameScene must reset all dev-only effects
 * (e.g., free upgrades no longer available — already-applied upgrades remain as
 * they are; only the infinite-upgrade bypass is removed).
 *
 * NOTE: Constants.SHOW_HITBOX is kept for legacy compile compatibility but is
 * no longer read at runtime — GameSettings.showHitbox is the live source.
 */
object GameSettings {

    /** Show colored hitbox outlines for player, enemies, and attack displays. */
    var showHitbox: Boolean = false

    /** Master switch for all developer / testing features. */
    var developerMode: Boolean = false
}