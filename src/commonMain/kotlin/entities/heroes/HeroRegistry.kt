package entities.heroes

import managers.GameSession

object HeroRegistry {

    fun configForSessionSelection(selectedId: String?): HeroConfig = when (selectedId) {
        FireWizardHero.ID -> FireWizardHero.createConfig()
        WandererMagicianHero.ID -> WandererMagicianHero.createConfig()
        else -> FireWizardHero.createConfig()
    }

    fun configForCurrentSession(): HeroConfig =
        configForSessionSelection(GameSession.selectedHeroId)
}
