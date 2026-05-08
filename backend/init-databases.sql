-- Creates all databases required by the Stonk microservices platform.
-- This script runs once on the first container startup via docker-entrypoint-initdb.d.
-- The default database (POSTGRES_DB) is created automatically by the Postgres image.

CREATE DATABASE auth_db;
CREATE DATABASE wallet_db;
CREATE DATABASE trading_db;
CREATE DATABASE market_db;
CREATE DATABASE audit_db;
CREATE DATABASE portfolio_db;