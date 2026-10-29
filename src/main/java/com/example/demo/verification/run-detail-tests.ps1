param([switch]$Mysql, [string]$DatabaseProperties)
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path $PSScriptRoot -Parent
$projectRoot = (Resolve-Path (Join-Path $taskRoot '../../../../../../')).Path
$testDirectory = Join-Path $taskRoot '.verification/tests'
New-Item -ItemType Directory -Force -Path $testDirectory | Out-Null
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'WorkOrderDetailTest.java.txt') -Destination (Join-Path $testDirectory 'WorkOrderDetailTest.java')
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'WorkOrderDetailMysqlTest.java.txt') -Destination (Join-Path $testDirectory 'WorkOrderDetailMysqlTest.java')
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'MaterialHistoryFormTest.java.txt') -Destination (Join-Path $testDirectory 'MaterialHistoryFormTest.java')
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'MachineHistoryLogStateTest.java.txt') -Destination (Join-Path $testDirectory 'MachineHistoryLogStateTest.java')
# Keep generated outputs inside the permitted source workspace; do not change the project POM.
$pom = [xml](Get-Content (Join-Path $projectRoot 'pom.xml') -Raw)
$namespace = $pom.DocumentElement.NamespaceURI
foreach ($item in @(@('sourceDirectory','${project.basedir}/..'), @('testSourceDirectory','${project.basedir}/tests'))) {
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
$pomPath = Join-Path $taskRoot '.verification/pom.xml'
$pom.Save($pomPath)
$testArguments = @('-o', '-f', $pomPath, 'test')
if ($Mysql) {
    if (-not $DatabaseProperties) { throw 'DatabaseProperties is required for MySQL tests' }
    $testArguments += '-Ddetail.mysql.tests=true'
    $testArguments += "-Ddetail.mysql.properties=$DatabaseProperties"
}
try {
    & (Join-Path $projectRoot 'mvnw.cmd') @testArguments
    if ($LASTEXITCODE -ne 0) { throw "Tests failed: $LASTEXITCODE" }
} finally {
    Remove-Item -LiteralPath (Join-Path $testDirectory 'MachineHistoryLogStateTest.java') -ErrorAction SilentlyContinue
    # Do not leave test Java files below the real project's main/java source root.
    Remove-Item -LiteralPath (Join-Path $testDirectory 'WorkOrderDetailTest.java') -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath (Join-Path $testDirectory 'WorkOrderDetailMysqlTest.java') -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath (Join-Path $testDirectory 'MaterialHistoryFormTest.java') -ErrorAction SilentlyContinue
}
