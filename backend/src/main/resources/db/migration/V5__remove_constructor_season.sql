-- Drop the existing unique index
DROP INDEX IF EXISTS idx_constructors_unique_per_season;

-- Add a new unique constraint on constructor_id only
ALTER TABLE constructors ADD CONSTRAINT uk_constructor_id UNIQUE (constructor_id);

-- Remove the season column
ALTER TABLE constructors DROP COLUMN IF EXISTS season; 