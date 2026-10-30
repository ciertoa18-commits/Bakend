-- =====================================================================
-- Reiniciar el esquema de VetAndes desde cero.
--
-- Usar cuando Flyway falla al arrancar con alguno de estos mensajes:
--   "Migration checksum mismatch for migration version 1"
--   "Validate failed: Migrations have failed validation"
--
-- Que significan: el esquema ya tiene una tabla
-- flyway_schema_history con una version 1 registrada (de otro
-- proyecto, o de una ejecucion anterior con el script modificado).
-- Flyway no vuelve a aplicar una version ya registrada, asi que las
-- tablas de VetAndes nunca se crean.
--
-- Tambien sirve para dejar la base como recien instalada antes de la
-- demostracion, de modo que los 409 de duplicado y los 204 de borrado
-- de la coleccion de Postman salgan como se espera.
--
-- Ejecutar como SYS / SYSTEM (AS SYSDBA).
-- =====================================================================

ALTER SESSION SET CONTAINER = freepdb1;


-- ---------------------------------------------------------------------
-- PASO 1. Diagnostico: mirar ANTES de borrar nada.
-- ---------------------------------------------------------------------

-- Que migraciones hay registradas
SELECT installed_rank, version, description, script, installed_on, success
FROM FERRE.flyway_schema_history
ORDER BY installed_rank;

-- Que tablas viven en el esquema
SELECT table_name
FROM all_tables
WHERE owner = 'FERRE'
ORDER BY table_name;


-- ---------------------------------------------------------------------
-- PASO 2. Borrar y recrear el usuario.
--
-- ATENCION: DROP USER ... CASCADE borra TODAS las tablas y datos del
-- esquema. En FERRE solo vive VetAndes, asi que se pierde unicamente
-- lo que Flyway vuelve a crear acto seguido.
-- ---------------------------------------------------------------------

DROP USER FERRE CASCADE;

-- La clave entre comillas dobles: en Oracle una contrasena sin
-- comillas no puede empezar por un digito (ORA-00988).
CREATE USER Ferre IDENTIFIED BY "&DB_PASSWORD";

GRANT ALL PRIVILEGES TO Ferre;

ALTER USER Ferre QUOTA UNLIMITED ON USERS;

GRANT CONNECT, RESOURCE TO Ferre;
GRANT DBA TO Ferre;


-- ---------------------------------------------------------------------
-- PASO 3. Comprobar que quedo vacio: debe devolver cero filas.
-- ---------------------------------------------------------------------

SELECT table_name
FROM all_tables
WHERE owner = 'FERRE';

-- Ahora arrancar el backend. Flyway aplica V1__esquema_y_semilla.sql
-- desde cero y crea las tablas con los datos semilla.
