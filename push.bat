@echo off
echo ============================================
echo   JoinLeaveSound 一键上传到远程仓库
echo ============================================
echo.

:: 检查 git 是否可用
git --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Git，请先安装 Git 并添加到环境变量。
    pause
    exit /b 1
)

:: 如果还不是 git 仓库，则初始化
if not exist ".git" (
    echo [信息] 初始化 Git 仓库...
    git init
    echo [信息] Git 仓库已初始化。
) else (
    echo [信息] 检测到已有 Git 仓库，跳过初始化。
)

:: 添加所有文件
echo.
echo [信息] 添加文件到暂存区...
git add .

:: 提交
set /p commit_msg="请输入本次提交信息（留空则使用默认信息）: "
if "%commit_msg%"=="" set commit_msg=Update JoinLeaveSound
echo [信息] 提交内容: %commit_msg%
git commit -m "%commit_msg%"

:: 检查是否已配置远程仓库
git remote get-url origin >nul 2>&1
if errorlevel 1 (
    echo.
    echo [警告] 未检测到远程仓库 origin。
    set /p repo_url="请输入远程仓库地址（例如 https://github.com/Dalict/JoinLeaveSound.git）: "
    if not "%repo_url%"=="" (
        git remote add origin %repo_url%
        echo [信息] 已添加远程仓库 origin。
    ) else (
        echo [错误] 未输入远程仓库地址，推送取消。
        pause
        exit /b 1
    )
) else (
    echo [信息] 远程仓库 origin 已存在。
)

:: 推送
echo.
set current_branch=
for /f "tokens=2" %%i in ('git branch --show-current 2^>nul') do set current_branch=%%i
if "%current_branch%"=="" set current_branch=master
echo [信息] 推送到 origin/%current_branch% ...
git push -u origin %current_branch%

if %errorlevel% equ 0 (
    echo.
    echo [成功] 代码已推送到远程仓库！
) else (
    echo.
    echo [失败] 推送时出现错误，请检查远程仓库地址和网络。
)
pause