@echo off
setlocal enabledelayedexpansion
title [GROCERY POS] TIEN TRINH KHOI TAO HE THONG

cd /d "%~dp0"
cls

echo ======================================================
echo       TIEN TRINH TU DONG THIET LAP HE THONG
echo ======================================================
echo.

:: --- BUOC 1: KIEM TRA & TAI JAVA ---
echo [BUOC 1/3] KIEM TRA MOI TRUONG JAVA 21...
echo ------------------------------------------------------
set "USE_LOCAL=0"

java -version 2>&1 | find "21" >nul
if %errorlevel% equ 0 (
    echo [OK] He thong da co san Java 21. Bo qua buoc tai.
) else (
    if exist "local-jdk\bin\java.exe" (
        echo [OK] Su dung Java 21 Portable da co san trong thu muc.
        set "USE_LOCAL=1"
    ) else (
        echo [!] KHONG TIM THAY JAVA 21. BAT DAU TAI TU DONG...
        echo [LOG] Dang ket noi den may chu Microsoft de tai JDK 21...
        
        :: Su dung PowerShell de hien thi thanh tien trinh tai thuc te
        powershell -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $url = 'https://aka.ms/download-jdk/microsoft-jdk-21-windows-x64.zip'; $output = 'jdk_temp.zip'; Write-Host '[LOG] Dang tai file (180MB)...'; Invoke-WebRequest -Uri $url -OutFile $output -ErrorAction Stop; }"
        
        if not exist "jdk_temp.zip" (
            echo [LOI] Khong the tai Java! Vui long kiem tra Internet va thu lai.
            pause
            exit /b
        )
        
        echo [LOG] Tai xong! Dang bat dau giai nen (Vui long cho)...
        if exist "jdk-extract" rmdir /s /q "jdk-extract"
        powershell -Command "Expand-Archive -Path 'jdk_temp.zip' -DestinationPath 'jdk-extract' -Force"
        
        echo [LOG] Dang cau hinh thu muc Java...
        if not exist "local-jdk" mkdir "local-jdk"
        for /d %%i in ("jdk-extract\*") do (
            xcopy "%%i" "local-jdk" /E /H /Y >nul
        )
        
        rmdir /s /q "jdk-extract"
        del /f /q "jdk_temp.zip"
        
        echo [OK] Thiet lap Java 21 Portable thanh cong.
        set "USE_LOCAL=1"
    )
)

if "!USE_LOCAL!"=="1" (
    set "JAVA_HOME=%~dp0local-jdk"
    set "PATH=%~dp0local-jdk\bin;!PATH!"
)
echo.

:: --- BUOC 2: KHOI TAO DATABASE ---
echo [BUOC 2/3] KHOI TAO CO SO DU LIEU & DU LIEU MAU...
echo ------------------------------------------------------
echo [LOG] Dang chay script tao bang (Schema)...
echo [LOG] Dang nap du lieu san pham, khach hang mau...
echo [LOG] (Qua trinh nay co the mat 1-2 phut cho lan dau chay)
echo.

call gradlew.bat :module-core:run --no-daemon
if %errorlevel% neq 0 (
    echo.
    echo [LOI] LOI KHI KHOI TAO DATABASE!
    echo [HUONG DAN] Hay kiem tra xem thu muc du an co quyen Ghi (Write) khong.
    pause
    exit /b
)

echo.
echo [BUOC 3/3] HOAN TAT KIEM TRA CUOI CUNG...
echo ------------------------------------------------------
echo [OK] Tat ca cac bang da duoc tao thanh cong.
echo [OK] Du lieu mau da duoc nap vao SQLite.
echo.
echo ======================================================
echo       XU LY THANH CONG! HE THONG DA SAN SANG.
echo    HAY CHAY FILE '2-RUN-APP.bat' DE MO UNG DUNG.
echo ======================================================
pause
exit
