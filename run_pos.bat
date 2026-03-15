@echo off
title GroceryPOS - He thong quan ly ban hang
setlocal enabledelayedexpansion

echo ======================================================
echo       TRINH TU DONG THIET LAP & CHAY GROCERY POS
echo ======================================================
echo.

:: 1. Kiem tra Java 21 (Bat buoc)
echo [1/3] Dang kiem tra moi truong Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] LOI: Khong tim thay Java tren may ban.
    echo Vui long tai va cai dat JDK 21 tai: https://www.azul.com/downloads/?package=jdk#zulu
    echo Sau do hay chay lai file nay.
    pause
    exit /b
)
echo Tim thay Java. San sang tiep tuc.
echo.

:: 2. Tu dong thiet lap Database SQLite
echo [2/3] Dang chuan bi Co so du lieu...
set DB_DIR=%APPDATA%\GroceryPOS
if not exist "%DB_DIR%" (
    mkdir "%DB_DIR%"
    echo [+] Da tao thu muc luu tru du lieu tai: %DB_DIR%
) else (
    echo [ok] Thu muc du lieu da san sang.
)
echo.

:: 3. Chay ung dung truc tiep tu ma nguon
echo [3/3] Dang tai thu vien va khoi chay ung dung...
echo (Luu y: Lan dau chay se mat 1-2 phut de tai Gradle va cac thu vien JavaFX)
echo.

:: Su dung lenh gradlew run de chay truc tiep tu code (khong can file jar)
call gradlew.bat :module-desktop-ui:run --no-daemon

if %errorlevel% neq 0 (
    echo.
    echo [!] LOI: Khong the chay ung dung. 
    echo Hay kiem tra ket noi mang hoac phien ban Java cua ban.
    pause
    exit /b
)

exit
