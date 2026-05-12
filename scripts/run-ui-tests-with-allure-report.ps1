param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$MavenArguments = @()
)

$testStatus = 0
try {
    & .\mvnw.cmd test @MavenArguments
    if ($LASTEXITCODE -ne 0) {
        $testStatus = $LASTEXITCODE
    }
} catch {
    $testStatus = 1
    Write-Error $_
}

& .\mvnw.cmd allure:report -DskipTests
if ($LASTEXITCODE -ne 0 -and $testStatus -eq 0) {
    $testStatus = $LASTEXITCODE
}

exit $testStatus

