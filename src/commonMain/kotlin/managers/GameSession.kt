package managers

/** Per-run choices before loading; extend when multiple heroes are wired in [scenes.GameScene]. */
object GameSession {
    var selectedHeroId: String? = null
        private set

    fun setSelectedHero(id: String) {
        selectedHeroId = id
    }

    fun clearSelectedHero() {
        selectedHeroId = null
    }
}
