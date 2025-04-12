# Get all Java files in controllers and services directories
$files = Get-ChildItem -Path "src\main\java\org\cpi2\controllers", "src\main\java\org\cpi2\services" -Filter "*.java" -Recurse

foreach ($file in $files) {
    # Read the content of the file
    $content = Get-Content $file.FullName -Raw
    
    # Remove single line comments
    $content = $content -replace '(?m)^\s*//.*$',''
    
    # Remove multi-line comments
    $content = $content -replace '/\*[^*]*\*+([^/*][^*]*\*+)*\/',''
    
    # Write the modified content back to the file
    Set-Content -Path $file.FullName -Value $content
}

Write-Host "Comments removed from all controller and service files."
