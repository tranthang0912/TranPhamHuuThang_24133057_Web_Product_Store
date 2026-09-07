$ErrorActionPreference='Stop'
$projectDir=Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectDir
if(-not(Test-Path 'database/SeedProducts.java')){throw 'SeedProducts.java not found.'}
& mvn -B -ntp dependency:copy-dependencies '-DincludeArtifactIds=mssql-jdbc' '-DoutputDirectory=.local/lib'
if($LASTEXITCODE-ne 0){throw 'Cannot prepare SQL JDBC driver.'}
$jdbcJar=Get-ChildItem '.local/lib' -Filter 'mssql-jdbc*.jar'|Select-Object -First 1
& java '-Dfile.encoding=UTF-8' -cp $jdbcJar.FullName database/SeedProducts.java
if($LASTEXITCODE-ne 0){throw 'Product import failed.'}