@echo off
title GroceryPOS - Khoi tao co so du lieu & Du lieu mau
setlocal enabledelayedexpansion

echo ======================================================
echo       KHOI TAO BANG & NAP DU LIEU MAU GROCERY POS
echo ======================================================
echo.

:: 1. Kiem tra Java
echo [1/3] Dang kiem tra moi truong Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [LOI] Khong tim thay Java! Vui long cai dat JDK 21.
    pause
    exit /b
)
echo Tim thay Java. San sang tiep tuc.
echo.

:: 2. Chay DataSeeder thong qua Gradle
echo [2/3] Dang khoi tao bang va nap du lieu gia...
echo (Luu y: Gradle se tu dong tai thu vien trong lan dau chay)
echo.

:: Goi task run trong module-core de chay lop DataSeeder
call gradlew.bat :module-core:run --no-daemon

if %errorlevel% neq 0 (
    echo.
    echo [LOI] Qua trinh khoi tao du lieu that bai!
    echo Vui long kiem tra file schema.sql va seed_data.sql.
    pause
    exit /b
)

echo.
echo [3/3] HOAN TAT!
echo Co so du lieu cua ban da co san cac bang va du lieu mau de test.
echo.
echo ======================================================
echo     BAY GIO BAN CO THE CHAY FILE 'run_pos.bat'
echo ======================================================
pause
exit
