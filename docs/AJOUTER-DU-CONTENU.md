# Ajouter du contenu sans tout casser

Cette page sert de checklist pour ajouter du contenu petit a petit.

## Ajouter un item simple

1. Declarer l'item dans `src/main/java/fr/maxence/maxencemod/registry/ModItems.java`.
2. L'ajouter dans l'onglet creatif dans `ModCreativeTabs.java`.
3. Ajouter son nom dans `src/main/resources/assets/maxencemod/lang/en_us.json`.
4. Ajouter son modele JSON dans `src/main/resources/assets/maxencemod/models/item/`.
5. Ajouter sa texture dans `src/main/resources/assets/maxencemod/textures/item/`.
6. Optionnel: ajouter une recette dans `src/main/resources/data/maxencemod/recipe/`.
7. Lancer `.\gradlew.bat build`.

## Ajouter un bloc simple

1. Declarer le bloc dans `ModBlocks.java`.
2. Declarer son item de bloc dans `ModItems.java`.
3. L'ajouter dans l'onglet creatif dans `ModCreativeTabs.java`.
4. Ajouter son nom dans `assets/maxencemod/lang/en_us.json`.
5. Ajouter son blockstate dans `assets/maxencemod/blockstates/`.
6. Ajouter son modele bloc dans `assets/maxencemod/models/block/`.
7. Ajouter son modele item dans `assets/maxencemod/models/item/`.
8. Ajouter sa texture dans `assets/maxencemod/textures/block/`.
9. Ajouter son loot dans `data/maxencemod/loot_table/blocks/`.
10. Optionnel: ajouter ses tags dans `data/maxencemod/tags/block/`.
11. Lancer `.\gradlew.bat build`.

## Ajouter un mob

1. Declarer le type d'entite dans `registry/ModEntities.java`.
2. Creer la classe du mob dans `entity/`.
3. Creer le renderer dans `client/renderer/`.
4. Mettre le modele Java exporte par Blockbench dans `client/model/`.
5. Mettre la texture dans `assets/maxencemod/textures/entity/`.
6. Ajouter son nom dans `assets/maxencemod/lang/en_us.json`.
7. Ajouter son loot dans `data/maxencemod/loot_table/entities/`.
8. Lancer `.\gradlew.bat build`.

## Ajouter un minerai

1. Ajouter le bloc du minerai dans `ModBlocks.java`.
2. Ajouter son item de bloc dans `ModItems.java`.
3. Ajouter textures/modeles/blockstate comme un bloc normal.
4. Ajouter le loot dans `data/maxencemod/loot_table/blocks/`.
5. Ajouter les tags de minage dans `data/maxencemod/tags/block/`.
6. Ajouter la generation dans `world/` et `data/maxencemod/worldgen/`.
7. Lancer `.\gradlew.bat build`.

## Ajouter un son

1. Mettre le fichier `.ogg` dans `assets/maxencemod/sounds/`.
2. Declarer le son dans `assets/maxencemod/sounds.json`.
3. Si le son est utilise en Java, ajouter `registry/ModSounds.java`.
4. Lancer `.\gradlew.bat build`.

## Noms a respecter

- Mod ID: `maxencemod`
- Package Java: `fr.maxence.maxencemod`
- Identifiants: uniquement minuscules, chiffres et underscores.

Exemples corrects:

```text
ruby
ruby_ore
raw_ruby
compressed_ruby_block
```

Exemples a eviter:

```text
Ruby
ruby ore
ruby-ore
MonItem
```

## Si vous hesitez

Ajoutez d'abord un seul item ou un seul bloc, faites un build, puis seulement apres ajoutez le reste.
