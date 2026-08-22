-- Add Google OAuth2 support columns
ALTER TABLE users ADD COLUMN google_id VARCHAR(255) UNIQUE;
ALTER TABLE users ADD COLUMN oauth_provider VARCHAR(50) DEFAULT 'LOCAL';
ALTER TABLE users ADD COLUMN google_picture_url VARCHAR(1024);
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;
