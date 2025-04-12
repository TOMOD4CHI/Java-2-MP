# Script pour supprimer les commentaires des fichiers FXML
$fxmlFiles = Get-ChildItem -Path "c:\Users\user\Documents\GitHub\Java-2-MP" -Filter "*.fxml" -Recurse

foreach ($file in $fxmlFiles) {
    Write-Host "Nettoyage des commentaires dans: $($file.FullName)"
    
    # Lire le contenu du fichier
    $content = Get-Content -Path $file.FullName -Raw
    
    # Supprimer les commentaires XML (<!-- ... -->)
    $newContent = $content -replace '<!--.*?-->', ''
    
    # Écrire le contenu modifié dans le fichier
    Set-Content -Path $file.FullName -Value $newContent
}

Write-Host "Tous les fichiers FXML ont été nettoyés."
