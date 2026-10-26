# Builds the Raazdar Arrow Puzzle rebuild APK. Cross-platform (pwsh): Windows, macOS, Linux CI.
# Prereqs: JDK 17 on PATH (or JAVA_HOME), Android SDK with platforms;android-36 + build-tools;35.0.0.
param(
    [string]$Out = ""
)
$ErrorActionPreference = "Stop"

$Root  = Join-Path $PSScriptRoot "app"
$Build = Join-Path $PSScriptRoot ".build"
if ([string]::IsNullOrEmpty($Out)) { $Out = Join-Path (Join-Path $PSScriptRoot "dist") "raazdar-arrowpuzzle-1.0.0.apk" }

function Resolve-Sdk {
    if ($env:ANDROID_HOME)   { return $env:ANDROID_HOME }
    if ($env:ANDROID_SDK_ROOT) { return $env:ANDROID_SDK_ROOT }
    $l = "$env:LOCALAPPDATA\Android\Sdk"
    if (Test-Path $l) { return $l }
    throw "Android SDK not found. Set ANDROID_HOME."
}
$sdk = Resolve-Sdk
$BtDirCandidates = @(
    (Join-Path $sdk "build-tools\35.0.0"),
    (Join-Path $sdk "build-tools\35.0.0"),  # windows style first
    (Join-Path $sdk "build-tools\35.0.0")
)
$Bt = Get-ChildItem (Join-Path $sdk "build-tools") -Directory | Sort-Object Name -Descending | Select-Object -First 1 -ExpandProperty FullName
$Platform = Join-Path $sdk "platforms\android-36\android.jar"
if (-not (Test-Path $Platform)) { throw "android.jar not found at $Platform - install platforms;android-36" }

function Find-Tool($name) {
    $cands = @((Join-Path $Bt "$name.exe"), (Join-Path $Bt $name), (Join-Path $Bt "$name.bat"))
    foreach ($c in $cands) { if (Test-Path $c) { return $c } }
    throw "tool not found: $name in $Bt"
}
$Aapt2    = Find-Tool "aapt2"
$D8       = Find-Tool "d8"
$Zipalign = Find-Tool "zipalign"
$Apksigner= Find-Tool "apksigner"

$Jdk = $env:JAVA_HOME
$Javac = if ($Jdk -and (Test-Path (Join-Path $Jdk "bin\javac.exe"))) { Join-Path $Jdk "bin\javac.exe" }
         elseif ($Jdk -and (Test-Path (Join-Path $Jdk "bin/javac"))) { Join-Path $Jdk "bin/javac" }
         else { "javac" }
$Jar  = if ($Jdk) { Join-Path $Jdk "bin\jar.exe" } else { "jar" }
$Keytool = if ($Jdk) { Join-Path $Jdk "bin\keytool.exe" } else { "keytool" }
if (-not (Get-Command $Javac -ErrorAction SilentlyContinue) -and -not (Test-Path $Javac)) { throw "javac not found" }

Remove-Item -Recurse -Force $Build -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path "$Build\gen", "$Build\classes", (Split-Path $Out -Parent) | Out-Null

Write-Host "SDK: $sdk"
Write-Host "Build-tools: $Bt"

# 1) compile resources
& $Aapt2 compile --dir "$Root\res" -o "$Build\res.zip"
if ($LASTEXITCODE -ne 0) { throw "aapt2 compile failed" }

# 2) link resources + manifest
& $Aapt2 link -o "$Build\unsigned.apk" -I $Platform --manifest "$Root\AndroidManifest.xml" `
    --java "$Build\gen" --min-sdk-version 26 --target-sdk-version 34 --auto-add-overlay `
    "$Build\res.zip"
if ($LASTEXITCODE -ne 0) { throw "aapt2 link failed" }

# 3) javac
$sources = @(Get-ChildItem -Recurse "$Root\src\main\java" -Filter *.java | ForEach-Object { $_.FullName })
$sources += Get-ChildItem -Recurse "$Build\gen" -Filter *.java | ForEach-Object { $_.FullName }
& $Javac -source 8 -target 8 -encoding UTF-8 -classpath $Platform -d "$Build\classes" $sources
if ($LASTEXITCODE -ne 0) { throw "javac failed" }

# 4) dex
$classFiles = Get-ChildItem -Recurse "$Build\classes" -Filter *.class | ForEach-Object { $_.FullName }
if ($Apksigner -like "*.bat") { & $D8 --release --lib $Platform --output "$Build" $classFiles }
else { & $D8 --release --lib $Platform --output "$Build" @classFiles }
if ($LASTEXITCODE -ne 0) { throw "d8 failed" }

# 5) add classes.dex into the apk (use zip/jar)
Copy-Item "$Build\unsigned.apk" "$Build\withdex.apk"
if (Get-Command zip -ErrorAction SilentlyContinue) {
    Push-Location $Build; try { zip -j -q "$Build\withdex.apk" classes.dex } finally { Pop-Location }
} else {
    & $Jar uf0 "$Build\withdex.apk" -C "$Build" classes.dex
}
if ($LASTEXITCODE -ne 0) { throw "dex merge failed" }

# 6) align + sign
$KS = $env:RAAZDAR_KEYSTORE
if (-not $KS) {
    $home_ = if ($env:USERPROFILE) { $env:USERPROFILE } else { $env:HOME }
    $KS = Join-Path $home_ ".android\raazdar-debug.keystore"
}
New-Item -ItemType Directory -Force -Path (Split-Path $KS -Parent) | Out-Null
if (-not (Test-Path $KS)) {
    & $Keytool -genkeypair -v -keystore $KS -storepass raazdar123 -keypass raazdar123 `
        -alias raazdar -keyalg RSA -keysize 2048 -validity 10000 `
        -dname "CN=Raazdar, OU=Raazdar, O=Raazdar, L=Raazdar, C=NA"
    if ($LASTEXITCODE -ne 0) { throw "keytool failed" }
}
& $Zipalign -f 4 "$Build\withdex.apk" "$Build\aligned.apk"
if ($LASTEXITCODE -ne 0) { throw "zipalign failed" }
& $Apksigner sign --ks $KS --ks-pass pass:raazdar123 --ks-key-alias raazdar --out $Out "$Build\aligned.apk"
if ($LASTEXITCODE -ne 0) { throw "apksigner failed" }
& $Apksigner verify $Out
Write-Host "BUILT: $Out"
