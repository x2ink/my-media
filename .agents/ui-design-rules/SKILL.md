---
name: ui-design-rules
description: Establishes rules for layout design, enforcing Material 3 guidelines and mandatory use of the standardized dimens.xml file for all spacing, sizes, and dimensions.
---

# UI Design & Specification Guidelines

This skill enforces strict UI design constraints for this project to ensure layouts remain modern, consistent, compile cleanly, and adapt perfectly to system themes (such as Dark/Light modes and dynamic colors).

---

## 1. Material Design 3 (M3) Colors & Styling

To ensure compatibility with Material 3 and system-wide theme adjustments:
1.  **Do NOT hardcode colors**: Avoid strings like `#FFFFFF`, `#000000`, or custom color resources like `@color/purple_500` for text, backgrounds, and tints.
2.  **Use Theme Attributes**: Use semantic theme tokens instead.
    *   **Backgrounds**: `?android:attr/colorBackground` or `?attr/colorSurface`.
    *   **Component Containers**: `?attr/colorSurfaceVariant` or `?attr/colorPrimaryContainer`.
    *   **Primary text**: `?attr/colorOnSurface`.
    *   **Secondary/Tertiary text**: `?android:attr/textColorSecondary` or `?attr/colorOnSurfaceVariant`.
    *   **Accent Actions**: `?attr/colorPrimary` and matching `?attr/colorOnPrimary`.
3.  **UI Components**: Use Material 3 widgets exclusively:
    *   Buttons: `com.google.android.material.button.MaterialButton`.
    *   Switch: `com.google.android.material.materialswitch.MaterialSwitch`.
    *   Cards: `com.google.android.material.card.MaterialCardView` with `style="@style/Widget.Material3.CardView.Filled"`.
    *   Images: `com.google.android.material.imageview.ShapeableImageView` to easily apply rounded corners using `app:shapeAppearanceOverlay`.

---

## 2. Mandatory Spacing & Sizes: Standard `dimens.xml`

This project mandates that **NO hardcoded dimensions** (`dp` or `sp`) should ever be written directly inside layout XML files.

1.  **Standard dimensions file**: `app/src/main/res/values/dimens.xml`. It defines dimensions from `0dp` up to `1000dp`.
2.  **Usage**: For any size, margin, padding, corner radius, elevation, or width/height, reference `@dimen/dimens_X` (where `X` is the integer value).
    *   **Bad**:
        ```xml
        android:padding="16dp"
        android:layout_marginTop="8dp"
        app:cardCornerRadius="12dp"
        ```
    *   **Good**:
        ```xml
        android:padding="@dimen/dimens_16"
        android:layout_marginTop="@dimen/dimens_8"
        app:cardCornerRadius="@dimen/dimens_12"
        ```
3.  **Exceptions**: `0dp` can be used directly for layout weights or matching constraints (e.g. `android:layout_width="0dp"`), but utilizing `@dimen/dimens_0` is also fully acceptable.

---

## 3. Typography Rules

Avoid hardcoding text sizes via `sp` values in text views. Instead, use standard Material text appearances.

*   **Page Title**: `android:textAppearance="?attr/textAppearanceTitleLarge"`
*   **Section Headers**: `android:textAppearance="?attr/textAppearanceTitleMedium"`
*   **Body Content**: `android:textAppearance="?attr/textAppearanceBodyLarge"`
*   **Secondary Details**: `android:textAppearance="?attr/textAppearanceBodyMedium"`
*   **Labels/Captions**: `android:textAppearance="?attr/textAppearanceLabelMedium"`

If a specific custom size is strictly required, reference its equivalent value from `dimens.xml`.

---

## 4. Vector Asset Guidelines

1.  **Use Vectors**: Do not use bitmap assets (PNG/JPG) for UI icons.
2.  **Theme Adaptability**: Standardize icons in `res/drawable/` using the following properties to ensure they recolor dynamically with themes:
    *   `android:tint="?attr/colorControlNormal"` (for standard grey/black/white icons).
    *   `android:tint="?attr/colorPrimary"` (for primary accent icons).

---

## 5. Naming Conventions (XML -> Kotlin Binding)

To ensure seamless integration with Android View Binding:
*   **Layout IDs**: Always write IDs using `snake_case` in XML:
    *   `android:id="@+id/btn_play_pause"`
    *   `android:id="@+id/tv_song_title"`
*   **Kotlin Code**: View Binding automatically formats these properties to `camelCase` in your Activity/Fragment classes:
    *   `binding.btnPlayPause`
    *   `binding.tvSongTitle`
*   Keep IDs unique, clean, and self-describing.

---

## 6. Flat & Shadowless UI Constraints

To maintain a clean, modern, and lightweight flat aesthetic, the following constraints are strictly enforced:
1.  **No Drop Shadows or Elevation**:
    *   Do NOT use `android:elevation` or `app:cardElevation` (set them to `0dp` or `@dimen/dimens_0` if required by a component).
    *   Avoid shadows on any UI elements.
2.  **No Card Containers**:
    *   Avoid using `MaterialCardView` for list items, tasks, or sections. Use flat containers like `LinearLayout`, `ConstraintLayout`, or `FrameLayout` instead.
3.  **Separation & Visual Structure**:
    *   Use subtle background color variations (e.g., `?attr/colorSurfaceContainerLow` vs `?android:attr/colorBackground`) to group components.
    *   Use thin, flat divider lines (like `View` with `1dp` height/width tinted with `?attr/colorOutlineVariant`) for separating sections and list items.
4.  **Flat Items**:
    *   All list items (downloading tasks, video rows) must be completely flat with no borders or card borders, aligning cleanly with the screen background.

