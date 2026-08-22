ALTER TABLE category
    ADD COLUMN icon VARCHAR(50);

UPDATE category
SET icon = 'FOOD'
WHERE icon IS NULL;

ALTER TABLE category
    ALTER COLUMN icon SET NOT NULL;