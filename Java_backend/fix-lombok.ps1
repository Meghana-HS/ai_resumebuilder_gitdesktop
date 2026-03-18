# PowerShell script to remove Lombok imports from all Java files
Get-ChildItem -Path "src\main\java" -Recurse -Filter "*.java" | ForEach-Object {
    $content = Get-Content $_.FullName
    # Remove Lombok imports
    $content = $content -replace "import lombok\.Data;", "" -replace "import lombok\.NoArgsConstructor;", "" -replace "import lombok\.AllArgsConstructor;", ""
    # Remove Lombok annotations
    $content = $content -replace "@Data", "" -replace "@NoArgsConstructor", "" -replace "@AllArgsConstructor", ""
    Set-Content $_.FullName $content
}

Write-Host "Lombok imports and annotations removed from all Java files"
