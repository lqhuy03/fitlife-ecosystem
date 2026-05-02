-- Add bmi and fitness_goal columns to members table
ALTER TABLE members ADD COLUMN bmi DOUBLE NULL DEFAULT NULL;
ALTER TABLE members ADD COLUMN fitness_goal VARCHAR(255) NULL DEFAULT NULL;


