# Ajouter du contenu sans tout casser

Cette page sert de checklist pour ajouter un bloc ou un item.

## Ajouter un item simple

1. Declarer l'item dans `src/main/java/fr/maxence/maxencemod/registry/ModItems.java`.
2. L'ajouter dans l'onglet creatif dans `ModCreativeTabs.java`.
3. Ajouter son nom dans `src/main/resources/assets/maxencemod/lang/en_us.json`.
4. Ajouter son modele JSON dans `src/main/resources/assets/maxencemod/models/item/`.
5. Ajouter sa texture dans `src/main/resources/assets/maxencemod/textures/item/`.
6. Lancer `.\gradlew.bat build`.

## Ajouter un bloc simple

1. Declarer le bloc dans `ModBlocks.java`.
2. Declarer son item de bloc dans `ModItems.java`.
3. L'ajouter dans l'onglet creatif dans `ModCreativeTabs.java`.
4. Ajouter son nom dans `lang/en_us.json`.
5. Ajouter son blockstate dans `blockstates/`.
6. Ajouter son modele dans `models/block/`.
7. Ajouter son modele item dans `models/item/`.
8. Ajouter sa texture dans `textures/block/`.
9. Lancer `.\gradlew.bat build`.

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
