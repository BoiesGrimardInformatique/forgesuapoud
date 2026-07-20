# forgesuapoud

A personal fork of [**Forge**](https://github.com/Card-Forge/forge), the open-source *Magic: The Gathering* rules engine.

Everything the upstream project does, this fork does too — this document only covers what is **different** here. For the game itself, its features and its community, see the [upstream repository](https://github.com/Card-Forge/forge) and the [Forge Discord](https://discord.gg/HcPJNyD66a).

> Forge is not affiliated with Wizards of the Coast.

---

## What this fork changes

### Faster deck editor

Opening the deck editor used to take 4-5 seconds the first time and 2-3 seconds on every reopen. Four fixes, all removing redundant work from the Swing event thread:

| Fix | Effect |
| --- | --- |
| A section `ActionListener` was re-added on every open of the cached controller, so listeners accumulated and each one re-triggered a full card-pool reload | Reopens are near-instant |
| The catalog was loaded twice on first open, because the existing guard compared a pool against a *copy* of itself | ~700 ms saved; switching Main ↔ Sideboard is now free |
| `ItemManager.setPoolImpl` filled the model immediately before `updateView` cleared and refilled it | One less full copy of ~90k cards per load |
| `ItemColumn.compare` rebuilt an entire `Comparator` chain on *every comparison* | Speeds up every sorted list in the app, not just the deck editor |

### Alchemy cards can be left out

Alchemy is *Magic: The Gathering Arena*'s digital-only content. This fork can keep it out of the card database entirely, rather than filtering it screen by screen — so it also stays out of generated decks, AI decks and draft.

Two things count as Alchemy:

- **Rebalanced cards**, whose names start with `A-`.
- **Cards exclusive to the Alchemy sets** — `Online` editions whose code starts with `Y` (`YMID`, `YNEO`, …) or is `HBG`.

Paper remasters and anthologies that merely happen to be digital releases (`AKR`, `HA*`, `KLR`, `PRM`, `SIS`, …) are deliberately **kept**. A card reprinted into an Alchemy set but also printed on paper is kept too.

**On every launch the game asks whether to include Alchemy cards in that session**, in the configured language. The answer applies to that run only. The `UI_LOAD_ALCHEMY_CARDS` preference supplies the pre-selected button and remains the fallback — command-line modes never show the dialog and simply follow it.

### `mydecks/`

Personal decklists under version control. The game does **not** read them from here — Forge loads decks from the user profile, so copy one across to play it:

```bash
cp "mydecks/BG Mono Red Burn.dck" ~/.forge/decks/constructed/
```

(`forge-gui/res/decks` is gitignored upstream, as it is user-profile territory — hence a folder outside the game's resources.)

---

## Building and running

Requires a **JDK 17 or newer** (JDK 21 works) and **Maven**. On Debian/Ubuntu:

```bash
sudo apt install openjdk-21-jdk maven
```

Build the desktop client:

```bash
mvn -B clean package -DskipTests -Dcheckstyle.skip=true -pl forge-gui-desktop -am
```

Install the jar next to the game resources and run it:

```bash
mkdir -p ~/.local/share/forge-app
cp forge-gui-desktop/target/forge-gui-desktop-*-jar-with-dependencies.jar ~/.local/share/forge-app/forge.jar
ln -sfn "$(pwd)/forge-gui/res" ~/.local/share/forge-app/res
(cd ~/.local/share/forge-app && java -Xmx4g -jar forge.jar)
```

`forge.jar` is a frozen copy, so rebuild and re-copy it after any code change. The `res` symlink points back into this repository — moving or deleting the clone will break the game's resources.

### Headless simulation

Forge can play AI-vs-AI matches with no GUI, which is handy for checking that decks load and function:

```bash
java -jar forge.jar sim -d "Deck A.dck" "Deck B.dck" -n 20 -c 60 -q
```

Two things to know: `.dck` files are always loaded from `~/.forge/decks/constructed/` (the `-D` flag only applies to tournaments), and `-f` selects the *game type*, not format legality — there is no Modern/Standard filter, and the bundled AI decks draw on all of Magic's history.

Both seats are played by Forge's AI, so results measure how well the AI pilots a deck rather than the deck's real strength. Decks that need fine decisions — burn, in particular — score far below what a human pilot would achieve.

---

## License and credits

Forge is developed by the [Card-Forge community](https://github.com/Card-Forge/forge) and released under the **GNU General Public License v3**. This fork inherits that license; see [`LICENSE`](LICENSE).
