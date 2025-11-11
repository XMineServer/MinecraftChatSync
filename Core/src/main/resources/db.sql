CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    uuid UUID UNIQUE NOT NULL,
    username TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE user_links (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    platform TEXT NOT NULL,
    external_id TEXT NOT NULL,
    linked_at TIMESTAMP DEFAULT now(),
    UNIQUE (platform, external_id)
);

CREATE TABLE group_links (
    id SERIAL PRIMARY KEY,
    platform TEXT NOT NULL,
    context_path TEXT[] NOT NULL,
    linked_at TIMESTAMP DEFAULT now(),
    UNIQUE (platform, context_path)
);