param([string]$TomcatHome='E:\apache-tomcat-11.0.25',[int]$Port=8080)
$ErrorActionPreference='Stop'
$projectDir=Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectDir
if(-not(Test-Path (Join-Path $TomcatHome 'bin/bootstrap.jar'))){throw 'Specify the Tomcat 11 installation folder.'}
if(-not(Test-Path '.local/application.properties')){throw 'Run scripts/setup-local.ps1 first.'}
$runtimeDir=Join-Path $projectDir '.runtime/tomcat'
foreach($part in @('conf','logs','temp','webapps','work')){New-Item -ItemType Directory -Force -Path (Join-Path $runtimeDir $part)|Out-Null}
foreach($config in @('web.xml','catalina.properties','logging.properties','context.xml')){Copy-Item -LiteralPath (Join-Path $TomcatHome "conf/$config") -Destination (Join-Path $runtimeDir "conf/$config") -Force}
$serverXml="""<?xml version=`"1.0`" encoding=`"UTF-8`"?>
<Server port=`"-1`"><Service name=`"Catalina`"><Connector address=`"127.0.0.1`" port=`"$Port`" protocol=`"HTTP/1.1`" connectionTimeout=`"20000`" URIEncoding=`"UTF-8`"/><Engine name=`"Catalina`" defaultHost=`"localhost`"><Host name=`"localhost`" appBase=`"webapps`" unpackWARs=`"true`" autoDeploy=`"false`"/></Engine></Service></Server>
"""
[IO.File]::WriteAllText((Join-Path $runtimeDir 'conf/server.xml'),$serverXml,[Text.UTF8Encoding]::new($false))
& mvn -B -ntp package
if($LASTEXITCODE-ne 0){throw 'Build failed.'}
Copy-Item target/product-store.war (Join-Path $runtimeDir 'webapps/ROOT.war') -Force
$localConfig=(Join-Path $projectDir '.local/application.properties').Replace('\','/')
$classPath=(Join-Path $TomcatHome 'bin/bootstrap.jar')+';'+(Join-Path $TomcatHome 'bin/tomcat-juli.jar')
& java "-Dcatalina.home=$TomcatHome" "-Dcatalina.base=$runtimeDir" "-Djava.io.tmpdir=$(Join-Path $runtimeDir 'temp')" "-Dspring.config.additional-location=file:$localConfig" '-Dfile.encoding=UTF-8' '-classpath' $classPath org.apache.catalina.startup.Bootstrap start