@echo off
setlocal enabledelayedexpansion
title [GROCERY POS] TIEN TRINH KHOI CHAY UNG DUNG

cd /d "%~dp0"
cls

echo ======================================================
echo          MO UNG DUNG GROCERY POS
echo ======================================================
echo.

:: --- BUOC 1: KIEM TRA JAVA ---
echo [1/2] DANG KIEM TRA MOI TRUONG JAVA...
echo ------------------------------------------------------
set "USE_LOCAL=0"

java -version 2>&1 | find "21" >nul
if %errorlevel% neq 0 (
    if exist "local-jdk\bin\java.exe" (
        echo [OK] Su dung Java 21 Portable co san.
        set "USE_LOCAL=1"
    ) else (
        echo [!] KHONG TIM THAY JAVA 21!
        echo [LOG] Vui long chay file '1-SETUP-DATABASE.bat' truoc de tai Java tu dong.
        echo.
        pause
        exit /b
    )
) else (
    echo [OK] Java 21 da co san tren he thong.
)

if "!USE_LOCAL!"=="1" (
    set "JAVA_HOME=%~dp0local-jdk"
    set "PATH=%~dp0local-jdk\bin;!PATH!"
)
echo.

:: --- BUOC 2: MO UNG DUNG ---
echo [2/2] DANG KHOI CHAY HE THONG BAN HANG...
echo ------------------------------------------------------
echo [LOG] Dang nap giao dien FXML va khoi tao Controller...
echo [LOG] Dang ket noi voi Co so du lieu SQLite...
echo [LOG] (Luu y: Lan dau chay ung dung se mat vai phut)
echo.

call gradlew.bat :module-desktop-ui:run --no-daemon

if %errorlevel% neq 0 (
    echo.
    echo [!!!] LOI: KHONG THE MO UNG DUNG!
    echo [LOG] Co loi phat sinh trong qua trinh nap giao dien.
    echo [HUONG DAN] Hay chup anh man hinh loi nay va gui cho toi.
    pause
    exit /b
)

exit
