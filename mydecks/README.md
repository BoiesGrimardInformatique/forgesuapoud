# mydecks

Personal decklists kept under version control.

These are **not** loaded by the game from here. Forge reads decks from the user
profile, so to actually play one, copy it across:

```bash
cp "mydecks/BG Mono Red Burn.dck" ~/.forge/decks/constructed/
```

`forge-gui/res/decks` is gitignored on purpose (it is user profile territory),
which is why this folder sits outside the game's resources.
