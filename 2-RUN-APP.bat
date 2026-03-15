@echo off
title 2. Khoi chay Grocery POS
setlocal enabledelayedexpansion

echo ======================================================
echo          MO UNG DUNG GROCERY POS
echo ======================================================
echo.

:: 1. Kiem tra va Tu dong tai Java 21
echo [1/2] Dang kiem tra moi truong Java...
set "LOCAL_JDK=%~dp0local-jdk"

java -version 2>&1 | find "21" >nul
if %errorlevel% equ 0 (
    echo [+] Java 21 da co san tren he thong.
) else (
    if exist "%LOCAL_JDK%\bin\java.exe" (
        echo [+] Su dung Java 21 Portable (Da tai ve truoc do).
    ) else (
        echo [!] May ban chua co Java 21. Dang TU DONG TAI VE ban Portable...
        echo     Qua trinh nay mat khoang 1-3 phut tuy toc do mang. Vui long cho...
        
        curl -L -o jdk.zip "https://aka.ms/download-jdk/microsoft-jdk-21-windows-x64.zip"
        if not exist "jdk.zip" (
            echo [LOI] Khong the tai xuong Java. Vui long kiem tra mang!
            pause
            exit /b
        )
        
        echo     Dang giai nen (Vui long khong tat cua so nay)...
        powershell -Command "Expand-Archive -Path 'jdk.zip' -DestinationPath 'jdk-temp' -Force"
        
        for /d %%i in ("jdk-temp\*") do move "%%i" "%LOCAL_JDK%" >nul
        
        rmdir /s /q "jdk-temp"
        del "jdk.zip"
        echo [+] Tai va thiet lap Java 21 thanh cong!
    )
    
    :: Cau hinh de su dung Java portable vua tai
    set "JAVA_HOME=%LOCAL_JDK%"
    set "PATH=%LOCAL_JDK%\bin;!PATH!"
)
echo.

:: 2. Mo ung dung truc tiep tu code
echo [2/2] Dang khoi chay he thong ban hang...
echo (Luu y: Lan dau chay se mat vai phut)
echo.

call gradlew.bat :module-desktop-ui:run --no-daemon

if %errorlevel% neq 0 (
    echo.
    echo [!!!] LOI: Khong the khoi chay ung dung!
    echo Hay chup anh man hinh loi nay va gui cho toi.
    pause
    exit /b
)

echo.
echo ======================================================
echo       UNG DUNG DA DUOC MO THANH CONG!
echo ======================================================
exit
