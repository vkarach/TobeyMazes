CREATE TABLE IF NOT EXISTS users (
    user_id       INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_name     VARCHAR(60)  NOT NULL UNIQUE,
    password_hash VARCHAR(60)  NOT NULL,
    email         VARCHAR(60) NOT NULL UNIQUE,
    added_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CHECK (email ~* '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$')
);

CREATE TABLE IF NOT EXISTS user_sessions (
    user_id       INTEGER      NOT NULL PRIMARY KEY,
    session_token VARCHAR(255) NOT NULL UNIQUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expire_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id)
    REFERENCES users(user_id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS levels (
    level_id   INTEGER PRIMARY KEY,
    level_name VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS best_level_results (
    user_id       INTEGER   NOT NULL,
    level_id      INTEGER   NOT NULL,
    best_time_ms  BIGINT,
    best_score    INTEGER,
    achieved_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, level_id),

    FOREIGN KEY (level_id)
        REFERENCES levels(level_id)
        ON DELETE CASCADE,

    FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reviews (
    user_id    INTEGER PRIMARY KEY,
    rating     SMALLINT NOT NULL,
    comment    TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS verification_emails (
    email_id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    email_code INTEGER   NOT NULL,
    send_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expire_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP + INTERVAL '10 minutes'

);

-- DROP TABLE user_sessions;
-- DROP TABLE best_level_results;
-- DROP TABLE reviews;
-- DROP TABLE verification_emails;
-- DROP TABLE users;