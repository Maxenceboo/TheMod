# Maxence Mod

Base de mod Minecraft avec NeoForge et ModDevGradle.

## Lancer le client de test

```powershell
.\gradlew.bat runClient
```

## Build le mod

```powershell
.\gradlew.bat build
```

Le fichier `.jar` sera genere dans `build/libs/`.

## Ouvrir dans IntelliJ IDEA

Ouvre ce dossier comme projet Gradle. Au premier import, Gradle telecharge Minecraft, NeoForge et les dependances; ca peut prendre quelques minutes.

## Fichiers importants

- `gradle.properties`: nom, version et identifiant du mod.
- `src/main/java/fr/maxence/maxencemod`: code Java du mod.
- `src/main/resources/assets/maxencemod`: traductions, textures et modeles.
- `src/main/templates/META-INF/neoforge.mods.toml`: metadata generee du mod.

## Organisation d'equipe

Avant d'ajouter du contenu, lis ces fichiers:

- [Organisation du projet](docs/ORGANISATION.md)
- [Travailler a deux avec Git](docs/GIT-A-DEUX.md)
- [Ajouter du contenu sans tout casser](docs/AJOUTER-DU-CONTENU.md)
