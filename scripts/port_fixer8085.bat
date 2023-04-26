@echo off

setlocal ENABLEEXTENSIONS DISABLEDELAYEDEXPANSION
for /f "delims=" %%I in ('netstat -ano ^| findstr "[0123456789]:8085"') do (
    set "_=%%I"
    call :KillTask %%_:~71%%
)
endlocal

pause
goto :end

:KillTask
taskkill /f /pid %1
goto :end

:end