# Organisation du projet

Le but est simple: chaque type de fichier a sa place. Si vous ne savez pas ou mettre quelque chose, revenez ici avant de coder.

## Dossiers principaux

```text
.
├── docs/
├── src/
│   └── main/
│       ├── java/fr/maxence/maxencemod/
│       ├── resources/assets/maxencemod/
│       └── templates/META-INF/
├── build.gradle
├── gradle.properties
└── README.md
```

## Code Java

```text
src/main/java/fr/maxence/maxencemod/
├── MaxenceMod.java
├── client/
├── config/
└── registry/
```

- `MaxenceMod.java`: point d'entree du mod. On evite d'y ajouter du contenu directement.
- `client/`: code qui concerne uniquement le client Minecraft, comme les ecrans, rendus et effets visuels.
- `config/`: options de configuration du mod.
- `registry/`: declaration des blocs, items, onglets creatifs, entites, menus, etc.

Regle pratique: si vous ajoutez un nouvel item, allez dans `registry/ModItems.java`. Si vous ajoutez un bloc, allez dans `registry/ModBlocks.java`.

## Resources Minecraft

```text
src/main/resources/assets/maxencemod/
├── lang/
├── blockstates/
├── models/
│   ├── block/
│   └── item/
└── textures/
    ├── block/
    └── item/
```

- `lang/`: noms affiches en jeu.
- `blockstates/`: etats des blocs.
- `models/block/`: modeles JSON des blocs.
- `models/item/`: modeles JSON des items.
- `textures/block/`: textures de blocs.
- `textures/item/`: textures d'items.

Les fichiers Blockbench `.bbmodel` peuvent rester dans `src/main/resources/assets/maxencemod/models/`, mais ils ne seront pas mis dans le `.jar` final.

## A ne pas modifier sans raison

- `gradle/`
- `gradlew`
- `gradlew.bat`
- `.github/workflows/build.yml`
- `build/`
- `.gradle/`

`build/` et `.gradle/` sont generes automatiquement. Ne mettez pas votre travail dedans.
