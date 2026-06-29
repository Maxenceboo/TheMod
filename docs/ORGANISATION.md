# Organisation du projet

Le but est simple: chaque type de fichier a sa place. Si vous ne savez pas ou mettre quelque chose, revenez ici avant de coder.

## Dossiers principaux

```text
.
|-- docs/
|-- src/
|   `-- main/
|       |-- java/fr/themod/
|       |-- resources/assets/themod/
|       |-- resources/data/themod/
|       `-- templates/META-INF/
|-- build.gradle
|-- gradle.properties
`-- README.md
```

## Code Java

```text
src/main/java/fr/themod/
|-- TheMod.java
|-- block/
|-- client/
|   |-- model/
|   `-- renderer/
|-- config/
|-- entity/
|-- item/
|-- registry/
|-- screen/
`-- world/
```

- `TheMod.java`: point d'entree du mod. On evite d'y ajouter du contenu directement.
- `block/`: classes Java speciales pour les blocs avec comportement custom.
- `client/`: code qui concerne uniquement l'affichage cote client.
- `client/model/`: modeles Java exportes par Blockbench pour les mobs ou entites animees.
- `client/renderer/`: renderers des mobs, entites ou blocs speciaux.
- `config/`: options de configuration du mod.
- `entity/`: classes Java des mobs et entites.
- `item/`: classes Java speciales pour les items avec comportement custom.
- `registry/`: declaration des blocs, items, mobs, onglets creatifs, sons, menus, etc.
- `screen/`: ecrans et menus custom.
- `world/`: generation de monde, minerais, biomes, structures.

Regle pratique: si vous ajoutez un contenu simple, commencez presque toujours dans `registry/`.

## Assets: visuel et son

Les assets sont ce que le client Minecraft affiche ou joue: textures, modeles, traductions, sons.

```text
src/main/resources/assets/themod/
|-- lang/
|-- blockstates/
|-- models/
|   |-- block/
|   |-- item/
|   `-- entity/
|-- textures/
|   |-- block/
|   |-- item/
|   |-- entity/
|   |-- gui/
|   `-- particle/
`-- sounds/
```

- `lang/`: noms affiches en jeu, par exemple `en_us.json`.
- `blockstates/`: etats des blocs, par exemple orientation ou variante.
- `models/block/`: modeles JSON des blocs.
- `models/item/`: modeles JSON des items.
- `models/entity/`: fichiers de travail ou modeles exportes lies aux entites.
- `textures/block/`: textures de blocs.
- `textures/item/`: textures d'items.
- `textures/entity/`: textures des mobs et entites.
- `textures/gui/`: textures d'interfaces.
- `textures/particle/`: textures de particules.
- `sounds/`: fichiers sons, souvent en `.ogg`.

Les fichiers Blockbench `.bbmodel` peuvent rester dans `src/main/resources/assets/themod/models/`, mais ils ne seront pas mis dans le `.jar` final.

## Data: gameplay

Les data files changent le comportement du jeu: recettes, loot, tags, generation de monde.

```text
src/main/resources/data/themod/
|-- recipe/
|-- loot_table/
|   |-- blocks/
|   |-- entities/
|   `-- chests/
|-- tags/
|   |-- block/
|   |-- item/
|   `-- entity_type/
|-- advancement/
`-- worldgen/
```

- `recipe/`: recettes de craft, four, smithing, etc.
- `loot_table/blocks/`: drops des blocs quand ils sont casses.
- `loot_table/entities/`: drops des mobs.
- `loot_table/chests/`: loot de coffres de structures.
- `tags/block/`: groupes de blocs, par exemple les blocs minables avec pioche.
- `tags/item/`: groupes d'items.
- `tags/entity_type/`: groupes de types d'entites.
- `advancement/`: succes/advancements.
- `worldgen/`: generation de minerais, biomes, features, structures.

Attention: en Minecraft recent, plusieurs dossiers data sont au singulier: `recipe`, `loot_table`, `advancement`.

## Ou mettre quoi

| Ce que vous ajoutez | Code Java | Assets | Data |
| --- | --- | --- | --- |
| Item simple | `registry/ModItems.java` | `models/item/`, `textures/item/`, `lang/` | parfois `recipe/`, `tags/item/` |
| Bloc simple | `registry/ModBlocks.java`, `registry/ModItems.java` | `blockstates/`, `models/block/`, `models/item/`, `textures/block/`, `lang/` | `loot_table/blocks/`, `tags/block/`, `recipe/` |
| Mob | `entity/`, `registry/ModEntities.java`, `client/model/`, `client/renderer/` | `textures/entity/`, `lang/` | `loot_table/entities/`, `tags/entity_type/` |
| Minerai | `registry/ModBlocks.java`, `world/` | fichiers de bloc | `worldgen/`, `loot_table/blocks/`, `tags/block/` |
| Son | `registry/ModSounds.java` | `sounds/` et `sounds.json` | rarement |
| Interface | `screen/`, `client/` | `textures/gui/` | rarement |

## A ne pas modifier sans raison

- `gradle/`
- `gradlew`
- `gradlew.bat`
- `.github/workflows/build.yml`
- `build/`
- `.gradle/`

`build/` et `.gradle/` sont generes automatiquement. Ne mettez pas votre travail dedans.
