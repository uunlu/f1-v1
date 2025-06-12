-- Drop existing foreign key constraints
ALTER TABLE race_winners DROP CONSTRAINT IF EXISTS race_winners_constructor_id_fkey;
ALTER TABLE season_champions DROP CONSTRAINT IF EXISTS season_champions_constructor_id_fkey;

-- Add new foreign key constraints using constructor_id
ALTER TABLE race_winners 
  ADD COLUMN constructor_ref VARCHAR(255),
  ADD CONSTRAINT fk_race_winners_constructor 
  FOREIGN KEY (constructor_ref) 
  REFERENCES constructors (constructor_id);

ALTER TABLE season_champions 
  ADD COLUMN constructor_ref VARCHAR(255),
  ADD CONSTRAINT fk_season_champions_constructor 
  FOREIGN KEY (constructor_ref) 
  REFERENCES constructors (constructor_id);

-- Copy data from old columns to new columns
UPDATE race_winners rw 
SET constructor_ref = c.constructor_id 
FROM constructors c 
WHERE rw.constructor_id = c.id;

UPDATE season_champions sc 
SET constructor_ref = c.constructor_id 
FROM constructors c 
WHERE sc.constructor_id = c.id;

-- Drop old columns
ALTER TABLE race_winners DROP COLUMN constructor_id;
ALTER TABLE season_champions DROP COLUMN constructor_id;

-- Rename new columns to old names
ALTER TABLE race_winners RENAME COLUMN constructor_ref TO constructor_id;
ALTER TABLE season_champions RENAME COLUMN constructor_ref TO constructor_id; 