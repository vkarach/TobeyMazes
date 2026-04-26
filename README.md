# Tobey Mazes

Play it: **https://tobeymazes.xyz** — it's on my own server, with a domain I bought, and Google already indexes it (just search *Tobey Mazes*).

### The game

You control Konek Tobey, a little pixel horse. He doesn't stop when you release the key — he slides in the direction you pressed until he bumps into a wall. Each level has a few flowers scattered around, and you need to pick the right sequence of slides to sweep them all up.

Move with arrows or WASD, or swipes on a phone. Fewer moves and faster time give you a better score.

### What's inside

Three clients share the same game core:

- **Web** — Spring Boot + Thymeleaf, mobile-friendly (touch, swipes, landscape hint, iPhone fullscreen guide). This is what's live at tobeymazes.xyz.
- **Desktop** — FXGL app with parallax backgrounds, sound and animations.
- **Console** — ANSI terminal version.

Plus accounts, a global leaderboard, player reviews, and a hidden room somewhere.

Levels are plain `.txt` files under `resources/levels/`. `S` is the horse's start tile, `!` is a flower, and two bit-matrices describe walls between cells. That's just how I write the maps — the player sees the horse and flowers, not the characters.

### Stack

Java · Spring Boot · Thymeleaf · FXGL · Maven

### Running it

```bash
mvn spring-boot:run
# console
```
```bash
mvn exec:java -Dexec.args=--ui=fxgl -Dspring.profiles.active=fxgl
# FXGL desktop
```

The web version is the same codebase running as a normal Spring Boot web app.

### Demos

- Console: https://drive.google.com/file/d/1SJuuN40cgBDqtTGFDCi1R8A1gdFIeCln/view?usp=sharing
- Web: [play it live](https://tobeymazes.xyz)
