param([int]$Port = 8080)
$ErrorActionPreference='Stop'
$projectDir=Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectDir
if(-not(Test-Path '.local/application.properties')){throw 'Run scripts/setup-local.ps1 first.'}
& mvn -B -ntp package
if($LASTEXITCODE-ne 0){throw 'Build failed.'}
& java "-Dspring.config.additional-location=file:$((Join-Path $projectDir '.local/application.properties').Replace('\','/'))" -jar target/product-store.war "--server.port=$Port"