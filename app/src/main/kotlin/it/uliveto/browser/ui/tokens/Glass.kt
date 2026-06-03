package it.uliveto.browser.ui.tokens

/**
 * Glass material tiers used across the UI.
 * Real blur/frosted-glass implementation arrives in M4.
 */
enum class GlassMaterial {
    /** Purely ornamental glass — lower blur, higher transparency. */
    Decorative,

    /** Functional glass — higher blur, used for overlays that need legibility. */
    Functional,
}
