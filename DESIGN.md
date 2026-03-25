# Design System Document

## 1. Overview & Creative North Star
### The Digital Archivist
This design system moves beyond the utility of a standard bookmarking tool to create an editorial, high-end experience for personal curation. The "Creative North Star" is **The Digital Archivist**: a philosophy that treats digital links like physical artifacts in a gallery. 

Instead of a rigid, boxed-in grid, we utilize intentional asymmetry, generous white space (using our 16 and 20 spacing tokens), and sophisticated tonal layering. This system rejects "default" UI patterns in favor of a tactile, layered environment that feels both breathable and premium.

---

## 2. Colors & Visual Soul
The color palette is rooted in soft, clinical neutrals punctuated by deep teals and vibrant category accents.

*   **The "No-Line" Rule:** To maintain an editorial feel, **1px solid borders are strictly prohibited** for sectioning or containment. Structural boundaries must be defined solely by background shifts (e.g., a `surface-container-low` card resting on a `surface` background).
*   **Surface Hierarchy & Nesting:** Treat the UI as a series of nested layers.
    *   **Level 0 (Base):** `surface` (#f9f9fa) – The canvas.
    *   **Level 1 (Sections):** `surface-container-low` (#f3f3f4).
    *   **Level 2 (Interactive Elements):** `surface-container-lowest` (#ffffff).
*   **The "Glass & Gradient" Rule:** To provide "soul" to the interface, main Action Buttons and Floating Action Buttons (FABs) should utilize a subtle linear gradient from `primary` (#00677e) to `primary_fixed_dim` (#5bd5fa). For floating overlays, use a backdrop-blur (12px–20px) combined with 80% opacity on `surface_container_lowest`.
*   **Signature Textures:** Category icons should use vibrant, high-saturation colors (as seen in reference) but must be housed within container shapes using `md` (0.75rem) or `lg` (1rem) roundedness to soften the impact.

---

## 3. Typography
Our typography creates a clear distinction between "The Brand Voice" and "The Content."

*   **Display & Headlines (Plus Jakarta Sans):** Used for primary navigation and page titles (e.g., "Groups" or "Recently Saved"). The wide aperture and modern geometry of Plus Jakarta Sans provide an authoritative, editorial tone. Use `headline-lg` for top-level headers with a negative letter-spacing of -0.02em to increase premium feel.
*   **Body & Labels (Manrope):** Used for information density and metadata. Manrope is highly legible and "friendly." Use `body-md` for bookmark descriptions and `label-sm` for category tags or "12 bookmarks" metadata.
*   **Hierarchy Note:** Always pair a `headline-sm` title with a `label-md` subtitle in `on_surface_variant` (#414659) to create a sophisticated tonal contrast.

---

## 4. Elevation & Depth
Hierarchy is achieved through **Tonal Layering** rather than structural lines.

*   **The Layering Principle:** Depth is created by "stacking." A card using `surface-container-lowest` placed on a background of `surface-container-low` creates a soft, natural lift.
*   **Ambient Shadows:** For elements that require a "floating" effect (like the FAB), use an extra-diffused shadow:
    *   *Blur:* 24px | *Spread:* -4px | *Opacity:* 6% | *Color:* `on_surface` (#1a1c1d). This mimics natural ambient light.
*   **The "Ghost Border" Fallback:** If accessibility requires a border, use a "Ghost Border": the `outline-variant` token (#c1c5dd) at 15% opacity. Never use 100% opaque borders.
*   **Glassmorphism:** Bottom navigation bars should use `surface_container_low` with a 70% opacity and a `backdrop-filter: blur(20px)`. This integrates the navigation into the content rather than severing the layout.

---

## 5. Components

### Floating Action Button (FAB)
*   **Style:** `xl` (1.5rem) roundedness or `full`. 
*   **Color:** Gradient from `primary` to `primary_fixed_dim`.
*   **Interaction:** On hover/press, shift the gradient density rather than a simple color change. Use an ambient shadow for elevation.

### List Items & Cards
*   **Rule:** **Strictly no dividers.** 
*   **Layout:** Use a `surface-container-low` background for the card body. Use `spacing.4` (1rem) for internal padding and `spacing.3` (0.75rem) for vertical margins between items.
*   **Icons:** Category icons (like the colored letter-blocks in the reference) should use the `md` roundedness scale and high-vibrancy background colors.

### Bottom Navigation
*   **Style:** A "pill" or "docked" style.
*   **Active State:** Use a `primary_container` (#b5ebff) shape behind the active icon with `full` roundedness. 
*   **Typography:** Icons should be paired with `label-sm` text in `on_surface_variant` when inactive, and `primary` when active.

### Chips (Filters/Categories)
*   **Style:** `surface-container-high` background with `full` roundedness.
*   **Text:** `label-md` in `on_surface`.
*   **Action:** When selected, transition to `primary` with `on_primary` text. No borders.

---

## 6. Do’s and Don’ts

### Do:
*   **Do** use asymmetrical spacing (e.g., more top padding than bottom) to create an editorial flow.
*   **Do** use `on_surface_variant` for secondary information to create a visual hierarchy that guides the eye.
*   **Do** embrace the `xl` (1.5rem) roundedness for large containers to maintain a soft, friendly aesthetic.

### Don’t:
*   **Don’t** use black (#000000) for shadows. Always use a tinted version of `on_surface` at very low opacity.
*   **Don’t** use 1px dividers to separate list items; let the white space from the `spacing` scale do the work.
*   **Don’t** use standard "system" blues. Stick strictly to the `primary` (#00677e) and `tertiary` (#4b5c92) ranges for a custom, branded feel.
*   **Don’t** crowd the edges. Every element needs "room to breathe" to maintain the premium Archivist feel.