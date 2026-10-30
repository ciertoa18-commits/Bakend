-- =====================================================================
-- Crear el esquema de VetAndes en Oracle.
--
-- Ejecutar UNA VEZ como SYS / SYSTEM (AS SYSDBA).
--
-- El esquema debe estar VACIO: Flyway lleva su control en
-- flyway_schema_history, y si ahi ya hay una version 1 de otro
-- proyecto, V1__esquema_y_semilla.sql no se aplica y el arranque
-- falla. Por eso se usa un usuario nuevo y no uno reciclado.
-- =====================================================================

-- 1. Comprobar que el contenedor conectable esta abierto
SELECT name, open_mode
FROM v$pdbs;

-- 2. Trabajar dentro del PDB, no en el contenedor raiz CDB$ROOT
ALTER SESSION SET CONTAINER = freepdb1;

-- 3. Crear el usuario.
--
--    La clave va ENTRE COMILLAS DOBLES. En Oracle una contrasena sin
--    comillas no puede empezar por un digito. SQL*Plus pedira el
--    valor de DB_PASSWORD al ejecutar el script.
--
--    El nombre Ferre va sin comillas, asi que Oracle lo guarda en
--    mayusculas como FERRE: ese es el usuario que pide
--    application-dev.yaml.
CREATE USER Ferre IDENTIFIED BY "&DB_PASSWORD";

GRANT ALL PRIVILEGES TO Ferre;

ALTER USER Ferre QUOTA UNLIMITED ON USERS;

GRANT CONNECT, RESOURCE TO Ferre;
GRANT DBA TO Ferre;

-- 4. Comprobacion: debe devolver una fila con FERRE y OPEN
SELECT username, account_status, default_tablespace
FROM dba_users
WHERE username = 'FERRE';

-- 5. Debe devolver cero filas: el esquema esta vacio y listo para
--    que Flyway aplique la migracion al arrancar el backend.
SELECT table_name
FROM all_tables
WHERE owner = 'FERRE';
