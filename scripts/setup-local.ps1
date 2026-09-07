param([string]$SqlServer='localhost')
$ErrorActionPreference='Stop'
$projectDir=Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectDir
$localDir=Join-Path $projectDir '.local';New-Item -ItemType Directory -Force $localDir|Out-Null
$configPath=Join-Path $localDir 'application.properties'
if(Test-Path $configPath){Write-Host 'Cấu hình SQL đã tồn tại, không thay đổi mật khẩu.';exit 0}
& sqlcmd -S $SqlServer -E -C -b -i database/schema.sql
if($LASTEXITCODE-ne 0){throw 'Không thể tạo database ProductStore.'}
$dbSecret=[Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(24))+'a!'
$loginSql="IF SUSER_ID(N'productstore_app') IS NULL CREATE LOGIN productstore_app WITH PASSWORD='$dbSecret',CHECK_POLICY=ON; USE ProductStore; IF USER_ID(N'productstore_app') IS NULL CREATE USER productstore_app FOR LOGIN productstore_app; ALTER ROLE db_datareader ADD MEMBER productstore_app; ALTER ROLE db_datawriter ADD MEMBER productstore_app;"
& sqlcmd -S $SqlServer -E -C -b -Q $loginSql|Out-Null
if($LASTEXITCODE-ne 0){throw 'Không thể tạo tài khoản SQL.'}
$settings="spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ProductStore;encrypt=true;trustServerCertificate=true`nspring.datasource.username=productstore_app`nspring.datasource.password=$dbSecret`nstore.admin.password=admin`nstore.admin.email=admin`n"
[IO.File]::WriteAllText($configPath,$settings,[Text.UTF8Encoding]::new($false))
[IO.File]::WriteAllText((Join-Path $localDir 'TAI_KHOAN.txt'),"Tai khoan quan tri: admin`nMat khau: admin`n",[Text.UTF8Encoding]::new($false))
& scripts/seed-products.ps1