# Builds the Raazdar Arrow Puzzle rebuild APK. Cross-platform pwsh (Windows + Linux CI).
# Prereqs: JDK 17 on PATH (or JAVA_HOME), Android SDK with platforms;android-36 + build-tools 35.0.0.
param(
    [string]$Out = ""
)
$ErrorActionPreference = "Stop"

$Root  = Join-Path $PSScriptRoot "app"
$Build = Join-Path $PSScriptRoot ".build"
if ([string]::IsNullOrEmpty($Out)) { $Out = Join-Path (Join-Path $PSScriptRoot "dist") "raazdar-arrowpuzzle-1.0.0.apk" }

function Resolve-Sdk {
    if ($env:ANDROID_HOME) { return $env:ANDROID_HOME }
    if ($env:ANDROID_SDK_ROOT) { return $env:ANDROID_SDK_ROOT }
    $l = "$env:LOCALAPPDATA/Android/Sdk"
    if (Test-Path $l) { return $l }
    throw "Android SDK not found. Set ANDROID_HOME."
}
$sdk = Resolve-Sdk
$Bt = (Get-ChildItem (Join-Path $sdk "build-tools") -Directory | Sort-Object Name -Descending | Select-Object -First 1).FullName
$Platform = Join-Path $sdk "platforms/android-36/android.jar"
if (-not (Test-Path $Platform)) { throw "android.jar not found at $Platform - install platforms;android-36" }

function Find-Tool($name) {
    $cands = @((Join-Path $Bt "$name.exe"), (Join-Path $Bt $name))
    foreach ($c in $cands) { if (Test-Path $c) { return $c } }
    throw "tool not found: $name in $Bt"
}
$Aapt2    = Find-Tool "aapt2"
$D8       = Find-Tool "d8"
$Zipalign = Find-Tool "zipalign"
$Apksigner= Find-Tool "apksigner"

$Javac = "javac"; $Jar = "jar"; $Keytool = "keytool"
if ($env:JAVA_HOME) {
    foreach ($t in @("javac","jar","keytool")) {
        $p = Join-Path $env:JAVA_HOME "bin/$t"
        if (Test-Path "$p.exe") { Set-Variable -Name $t -Value "$p.exe" }
        elseif (Test-Path $p) { Set-Variable -Name $t -Value $p }
    }
}

Remove-Item -Recurse -Force $Build -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path (Join-Path $Build "gen"), (Join-Path $Build "classes"), (Split-Path $Out -Parent) | Out-Null

Write-Host "SDK: $sdk"
Write-Host "Build-tools: $Bt"

# 1) compile resources
& $Aapt2 compile --dir (Join-Path $Root "res") -o (Join-Path $Build "res.zip")
if ($LASTEXITCODE -ne 0) { throw "aapt2 compile failed" }

# 2) link resources + manifest
& $Aapt2 link -o (Join-Path $Build "unsigned.apk") -I $Platform --manifest (Join-Path $Root "AndroidManifest.xml") `
    --java (Join-Path $Build "gen") --min-sdk-version 26 --target-sdk-version 34 --auto-add-overlay `
    (Join-Path $Build "res.zip")
if ($LASTEXITCODE -ne 0) { throw "aapt2 link failed" }

# 3) javac
$sources = @(Get-ChildItem -Recurse (Join-Path $Root "src/main/java") -Filter *.java | ForEach-Object { $_.FullName })
$sources += Get-ChildItem -Recurse (Join-Path $Build "gen") -Filter *.java | ForEach-Object { $_.FullName }
& $Javac -source 8 -target 8 -encoding UTF-8 -classpath $Platform -d (Join-Path $Build "classes") $sources
if ($LASTEXITCODE -ne 0) { throw "javac failed" }

# 4) dex
$classFiles = Get-ChildItem -Recurse (Join-Path $Build "classes") -Filter *.class | ForEach-Object { $_.FullName }
& $D8 --release --lib $Platform --output $Build $classFiles
if ($LASTEXITCODE -ne 0) { throw "d8 failed" }

# 5) add classes.dex into the apk
Copy-Item (Join-Path $Build "unsigned.apk") (Join-Path $Build "withdex.apk")
if (Get-Command zip -ErrorAction SilentlyContinue) {
    Push-Location $Build; try { zip -j -q "$Build/withdex.apk" classes.dex } finally { Pop-Location }
} else {
    & $Jar uf0 (Join-Path $Build "withdex.apk") -C $Build classes.dex
}
if ($LASTEXITCODE -ne 0) { throw "dex merge failed" }

# 6) align + sign (debug keystore auto-generated; CI keystores differ per run until release secrets are set)
$KS = $env:RAAZDAR_KEYSTORE
if (-not $KS) {
    $homeDir = if ($env:USERPROFILE) { $env:USERPROFILE } else { $env:HOME }
    $KS = Join-Path $homeDir ".android/raazdar-debug.keystore"
}
New-Item -ItemType Directory -Force -Path (Split-Path $KS -Parent) | Out-Null
if (-not (Test-Path $KS)) {
    & $Keytool -genkeypair -v -keystore $KS -storepass raazdar123 -keypass raazdar123 `
        -alias raazdar -keyalg RSA -keysize 2048 -validity 10000 `
        -dname "CN=Raazdar, OU=Raazdar, O=Raazdar, L=Raazdar, C=NA"
    if ($LASTEXITCODE -ne 0) { throw "keytool failed" }
}
& $Zipalign -f 4 (Join-Path $Build "withdex.apk") (Join-Path $Build "aligned.apk")
if ($LASTEXITCODE -ne 0) { throw "zipalign failed" }
& $Apksigner sign --ks $KS --ks-pass pass:raazdar123 --ks-key-alias raazdar --out $Out (Join-Path $Build "aligned.apk")
if ($LASTEXITCODE -ne 0) { throw "apksigner failed" }
& $Apksigner verify $Out
Write-Host "BUILT: $Out"
