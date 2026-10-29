$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path $PSScriptRoot -Parent
$projectRoot = (Resolve-Path (Join-Path $taskRoot '../../../../../../')).Path
$testDirectory = Join-Path $taskRoot '.verification/hmi-tests'
New-Item -ItemType Directory -Force -Path $testDirectory | Out-Null
$testFile = Join-Path $testDirectory 'HmiCommunicationServiceTest.java'
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'HmiCommunicationServiceTest.java.txt') -Destination $testFile
try {
    $pom = [xml](Get-Content (Join-Path $projectRoot 'pom.xml') -Raw)
    $namespace = $pom.DocumentElement.NamespaceURI
    foreach ($item in @(@('sourceDirectory','${project.basedir}/..'), @('testSourceDirectory','${project.basedir}/hmi-tests'), @('directory','${project.basedir}/hmi-target'))) {
        $element = $pom.CreateElement($item[0], $namespace)
        $element.InnerText = $item[1]
        [void]$pom.project.build.AppendChild($element)
    }
    $compiler = $pom.project.build.plugins.plugin | Where-Object artifactId -eq 'maven-compiler-plugin'
    $configuration = $pom.CreateElement('configuration', $namespace)
    $excludes = $pom.CreateElement('excludes', $namespace)
    $exclude = $pom.CreateElement('exclude', $namespace)
    $exclude.InnerText = '.verification/**'
    [void]$excludes.AppendChild($exclude)
    [void]$configuration.AppendChild($excludes)
    [void]$compiler.AppendChild($configuration)
    $pomPath = Join-Path $taskRoot '.verification/hmi-pom.xml'
    $pom.Save($pomPath)
    & (Join-Path $projectRoot 'mvnw.cmd') -o -f $pomPath test '-Dtest=HmiCommunicationServiceTest'
    if ($LASTEXITCODE -ne 0) { throw "Tests failed: $LASTEXITCODE" }
} finally {
    Remove-Item -LiteralPath $testFile -ErrorAction SilentlyContinue
}
