# Kanarek Design

## Overview

Kanarek is a reader plus radio/TV player. Its interface should feel like a calm newsroom with a clear broadcast signal: content first, playback state obvious, chrome quiet.

The canonical runtime tokens live in `app/src/main/java/com/kanarek/ui/theme/`. This file documents their durable intent and should change together with them.

**North Star:** readable feeds and articles, fast switching between news and playback, comfortable one-handed use.

**Avoid:** entertainment-dashboard clutter, decorative gradients, dense card grids, excessive animation, and using the signal color everywhere.

## Colors

Branded fallback palette:

- primary blue: `#3569B7`
- primary container: `#DCE7F7`
- signal yellow: `#C88B00`
- light background: `#F6F7F8`
- light surface: `#FFFFFF`
- light muted surface: `#EBEEF2`
- light text: `#171A1F`
- light muted text: `#616A75`
- light border: `#D6DCE4`

Dark fallback uses `#101419` background, `#171C22` surface and `#232A33` muted surface, with lighter semantic foreground and brand roles.

Dynamic color remains supported and enabled by default on Android 12+. The branded palette is the deliberate fallback and should remain coherent when users disable dynamic color.

Blue owns navigation and primary actions. Yellow is a signal for live/playback or exceptional attention, not a generic decoration color.

## Typography

Use the Android system font. Reader comfort matters more than visual novelty.

- headlines: 28–32sp, bold
- titles: 16–22sp, semibold
- primary reading/body: 17sp / 26sp line height
- secondary body: 14sp / 21sp
- labels/actions: 14sp, semibold

Do not bundle a custom typeface without a measured readability or product benefit.

## Layout

Keep bottom navigation as the primary top-level shell. Top-level screens may use generous or collapsing titles; reading/detail surfaces should compact quickly so content wins vertical space.

Frequent controls belong in the lower reach zone when practical. Playback controls must remain stable while metadata changes. Reader and player surfaces may feel related but should not be visually identical: reading favors quiet vertical rhythm, playback favors obvious transport hierarchy.

Adaptive layouts should use additional width for useful content or navigation, not decorative whitespace.

## Elevation & Depth

Use tonal surfaces and borders first. Reserve visible elevation for drawers, sheets, menus and floating playback surfaces. Avoid shadow stacks around every feed item.

## Shapes

Runtime shape scale:

- extra small: 6dp
- small: 8dp
- medium: 14dp
- large: 22dp
- extra large: 28dp

The reader should feel slightly crisper than a generic rounded-card app. Pills are for chips and compact state controls, not whole feed rows.

## Components

Material 3 components and the app theme are canonical.

- Feed rows: prioritize source, title, time and thumbnail without nested card layers.
- Player: transport actions are visually stronger than metadata actions.
- Navigation: bottom bar remains primary on phones; drawer stays secondary utility navigation.
- App bars: use large/collapsing treatment only on top-level browsing screens.
- Signal states: live/current playback may use tertiary yellow; unrelated actions should not.

## Do's and Don'ts

Do preserve dynamic color, dark mode, stable playback geometry and readable article spacing.

Do not add heavy animation, blur, custom rendering or a third-party component framework for visual novelty. Performance, content density and playback reliability remain higher priorities than style.
