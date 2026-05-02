object Constants {
    const val SCREEN_WIDTH  = 1280
    const val SCREEN_HEIGHT = 720
    const val GROUND        = 580.0
    const val GRAVITY       = 2000.0

    // -------------------------------------------------------
    // DEBUG — LEGACY constant kept so existing code compiles
    // unchanged.  At runtime, always read GameSettings.showHitbox
    // instead — it is the live, toggle-able source of truth.
    //
    //   OLD:  if (Constants.SHOW_HITBOX)  ← compile-time constant
    //   NEW:  if (GameSettings.showHitbox) ← runtime toggle
    //
    // DO NOT change debug rendering by editing this value.
    // Use the Settings screen toggle instead.
    // -------------------------------------------------------
    @Deprecated("Use GameSettings.showHitbox for runtime toggle")
    const val SHOW_HITBOX = false   // static default; overridden at runtime
}