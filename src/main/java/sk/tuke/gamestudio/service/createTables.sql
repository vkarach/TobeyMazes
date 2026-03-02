SELECT current_database();

DROP TABLE score;
DROP TABLE best_times;
DROP TABLE users;
DROP TABLE user_sessions;

CREATE TABLE users (
    user_id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_name VARCHAR(60)   NOT NULL UNIQUE,
    added_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
    -- maybe password
);

CREATE TABLE user_sessions (
    user_id         INTEGER PRIMARY KEY,
    session_token   VARCHAR(255)    NOT NULL UNIQUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expire_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP + INTERVAL '1 month',

    FOREIGN KEY (user_id)
    REFERENCES users(user_id)
    ON DELETE CASCADE
);

-- ALTER TABLE users RENAME COLUMN

CREATE TABLE score (
    user_id      INTEGER     NOT NULL,
    player       VARCHAR(64) NOT NULL,
    game         VARCHAR(64) NOT NULL,
    points       INTEGER     NOT NULL,
    playedon     TIMESTAMP   NOT NULL,

    FOREIGN KEY (user_id)
    REFERENCES users(user_id)
    ON DELETE CASCADE
);

CREATE TABLE best_times (
    user_id       INTEGER     NOT NULL,
    level_id      VARCHAR(60) NOT NULL,
    best_time_ms  BIGINT      NOT NULL,
    achieved_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, level_id),

    FOREIGN KEY (user_id)
    REFERENCES users(user_id)
    ON DELETE CASCADE
);