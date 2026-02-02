@echo off
echo ========================================
echo Systeme de Gestion TPE - Backend
echo ========================================
echo.

echo Verification de Java...
java -version
if %errorlevel% neq 0 (
    echo ERREUR: Java n'est pas installe ou n'est pas dans le PATH
    echo Veuillez installer Java 17 ou superieur
    pause
    exit /b 1
)
echo.

echo Verification de Maven...
mvn -version
if %errorlevel% neq 0 (
    echo ERREUR: Maven n'est pas installe ou n'est pas dans le PATH
    echo Veuillez installer Maven 3.8 ou superieur
    pause
    exit /b 1
)
echo.

echo ========================================
echo Compilation du projet...
echo ========================================
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo ERREUR: La compilation a echoue
    pause
    exit /b 1
)
echo.

echo ========================================
echo Demarrage de l'application...
echo ========================================
echo L'application sera accessible sur http://localhost:8080
echo Swagger UI: http://localhost:8080/swagger-ui.html
echo.
echo Utilisateurs par defaut:
echo - admin / Admin@123
echo - monetique / Monetique@123
echo - agence / Agence@123
echo - inputer / Inputer@123
echo - authorizer / Authorizer@123
echo.
echo Appuyez sur Ctrl+C pour arreter l'application
echo ========================================
echo.

call mvn spring-boot:run
