# Script pour supprimer les fichiers CSS inutilisés
$unusedCSS = @(
    "c:\Users\user\Documents\GitHub\Java-2-MP\src\main\resources\css\style1.css",
    "c:\Users\user\Documents\GitHub\Java-2-MP\src\main\resources\css\style3.css",
    "c:\Users\user\Documents\GitHub\Java-2-MP\src\main\resources\css\style4.css",
    "c:\Users\user\Documents\GitHub\Java-2-MP\src\main\resources\css\style5.css",
    "c:\Users\user\Documents\GitHub\Java-2-MP\src\main\resources\css\style6.css",
    "c:\Users\user\Documents\GitHub\Java-2-MP\src\main\resources\css\modern-alerts.css"
)

foreach ($file in $unusedCSS) {
    if (Test-Path $file) {
        Write-Host "Suppression du fichier CSS inutilisé: $file"
        Remove-Item $file -Force
    } else {
        Write-Host "Fichier non trouvé: $file"
    }
}

Write-Host "\nLes fichiers CSS inutilisés ont été supprimés."
