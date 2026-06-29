# Travailler a deux avec Git

Objectif: eviter que deux personnes modifient le meme fichier au meme moment sans se parler.

## Avant de commencer

```powershell
git status
git pull
```

Si `git status` affiche des fichiers modifies, terminez ou sauvegardez votre travail avant de recuperer celui de l'autre.

## Faire une modification

Travaillez sur une branche par sujet:

```powershell
git checkout -b feature/nom-court
```

Exemples:

```powershell
git checkout -b feature/premier-minerai
git checkout -b feature/nouvelle-epee
git checkout -b fix/texture-bloc
```

## Avant de commit

```powershell
.\gradlew.bat build
git status
```

Si le build casse, corrigez avant de partager.

## Commit propre

```powershell
git add .
git commit -m "Ajoute le premier minerai"
```

Message simple: verbe + ce qui change.

## Regle d'equipe

- Une branche = une idee.
- Ne modifiez pas le meme fichier a deux en meme temps.
- Prevenez l'autre avant de toucher `build.gradle`, `gradle.properties` ou `MaxenceMod.java`.
- Faites un build avant de push.
- Si Git parle de conflit, ne forcez pas: arretez-vous et reglez le fichier tranquillement.
