---
name: android-material-symbols-icons
description: Use when adding, replacing, or reviewing Material Symbols icons in this Android XML/ViewBinding project. Ensures icons are imported as local VectorDrawable resources and referenced consistently from XML or Kotlin.
metadata:
  short-description: Add Material Symbols as Android vector drawables
---

# Android Material Symbols Icons

Use this skill when working on Android icons in this project and the user wants Material Symbols style assets.

## Project Convention

This project imports icons as local Android VectorDrawable XML files under:

```text
app/src/main/res/drawable/ic_*.xml
```

Do not introduce icon fonts, remote image loading, Compose icons, or new icon libraries unless the user explicitly asks for a different approach.

Preferred icon resource shape:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="..." />
</vector>
```

## Import Rules

- Use Material Symbols source artwork exported as SVG or Android vector path data.
- Save icons as `ic_<meaning>.xml`, using lowercase snake case.
- Keep default icons monochrome unless a specific product requirement needs color.
- Prefer `?attr/colorControlNormal` for general icons.
- Prefer caller-side tint overrides for special contexts, for example `app:iconTint="?attr/colorOnPrimary"`.
- Keep viewport at `24 x 24` for standard Material Symbols icons.
- Do not commit preview/test text or unrelated drawable changes.

## Usage Rules

In XML, reference icons directly:

```xml
android:src="@drawable/ic_search"
app:icon="@drawable/ic_play"
app:navigationIcon="@drawable/ic_back"
```

In Kotlin, switch icons through resource IDs:

```kotlin
val iconRes = if (isPlaying) {
    R.drawable.ic_pause
} else {
    R.drawable.ic_play
}
button.setIconResource(iconRes)
```

Do not hardcode file paths or parse drawable XML at runtime.

## Replacement Workflow

When adding or replacing an icon:

1. Check `app/src/main/res/drawable/` for an existing suitable `ic_*.xml`.
2. Reuse existing icons when the semantic meaning already matches.
3. If a new icon is needed, add a new `ic_<meaning>.xml` VectorDrawable.
4. Update XML or Kotlin references to use the new drawable.
5. Run a compile check, usually:

```bash
./gradlew :app:compileDebugKotlin
```

For layout-only icon changes, also inspect affected XML usages for size, tint, and alignment.

## Review Checklist

- Icon name describes meaning, not visual shape only.
- Drawable is local under `res/drawable`.
- No new icon library or font dependency was added.
- Vector uses standard Material 24dp viewport unless there is a clear reason.
- Tinting is theme-aware.
- Existing project XML/ViewBinding style is preserved.
