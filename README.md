# EyeSense Keyboard

![CI](https://github.com/polrubiano/eyesense-keyboard/actions/workflows/ci.yml/badge.svg)

A hexagonal on-screen keyboard designed for **eye tracking**, built entirely with **Jetpack Compose**.

## The idea

When you type with your eyes, every key is a journey. This keyboard puts the most frequent key at an **optical centre** and arranges the next most frequent ones in concentric rings around it, so the gaze travels as little as possible.

```
 ⬢ ⬡ ⬡ ⬡ ⬡ ⬡ ⬢       ⬢ ◗  corner keys: the cells beside the honeycomb,
◗ ⬡ ⬡ ⬡ ⬡ ⬡ ⬡ ◖          the lower half cut by the screen edge
 ⬡ ⬡ ⬡ E ⬡ ⬡ ⬡
◗ ⬡ ⬡ ⬡ ⬡ ⬡ ⬡ ◖     ␣ ␣  the space bar: two hexagons merged into one key
 ⬢ ⬡ ⬡ ⬡ ␣ ␣ ⬢
```

A honeycomb of 29 cells. The four function keys in the corners are built from the same honeycomb, from the cells left over beside it, so the layout keeps its hexagonal rhythm up to the screen edge. The space bar is two hexagons merged into one key. The whole layout derives from a single value, the hexagon size, so it adapts to any screen.

On the full text of *Don Quijote*, the original EyeSense layout needs **53 % less eye travel than QWERTY** (a theoretical estimate, measured with [letter-metter](https://github.com/polrubiano/letter-metter)). The new hexagonal layout will go through the same measurement once the key assignment is final.

## Why a rewrite

The keyboard comes from [EyeSense](https://github.com/polrubiano/eyesense). Its keyboard was built with XML views. This repository is an **open, standalone rewrite of that keyboard** in Jetpack Compose: the same idea, made simpler, more versatile and easier to scale.

| | EyeSense (XML) | EyeSense Keyboard (Compose) |
|---|---|---|
| Layout | `ConstraintLayout` with 30 percentage guidelines | Computed from a few parameters (rings, rows) |
| Honeycomb | Approximated: rows don't nest | True hexagonal grid |
| Touch areas | Rectangles that overlap between rows | Exact honeycomb shapes, including keys that span several cells |
| Changing the layout | Edit guidelines and constraints by hand | Change a parameter |
| Reuse | Tied to this keyboard | Generic panel for any content |
| Tests | — | Geometry and hit testing unit-tested |

## Highlights

- **Generic hex panel component**: content-agnostic, with a slot API and scoped modifiers in the style of `BoxScope`.
- **Exact touch zones**: taps are resolved mathematically (pixel → hexagon) instead of by overlapping rectangles. The same function will serve gaze points.
- **Pure-Kotlin geometry**: coordinates, layout and hit testing are independent from Compose and fully unit-tested.
- **Keys made of cells**: any group of cells becomes one shape (joined, clipped to the panel, inset for the gap, rounded) and still behaves like any other Compose `Shape`.
- **Compiler-guided functions**: every `when` over key actions is exhaustive, so adding a function breaks the build in each place that must handle it.

## Roadmap

- [x] Hexagonal panel: model, geometry, rendering and touch
- [ ] Functional keyboard: key model and real text input *(in progress: typing and delete work)*
- [ ] Measure the new layout's eye travel with letter-metter and compare it with the original
- [ ] Live camera background
- [ ] Customisable key layout, persisted between sessions
- [ ] Accessibility and polish

https://github.com/user-attachments/assets/311c2740-6cea-4620-addc-753961e549f3

## Tech

Kotlin · Jetpack Compose · Material 3 · JUnit · GitHub Actions

## Build & test

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

## References
- [Hexagonal Grids, Red Blob Games](https://www.redblobgames.com/grids/hexagons/)
