-- V3__add_season_column_only.sql

ALTER TABLE constructors ADD COLUMN season VARCHAR(255);

UPDATE constructors SET season = 'unknown' WHERE season IS NULL;
