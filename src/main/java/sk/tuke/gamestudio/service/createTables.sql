SELECT current_database();

DROP TABLE score;
DROP TABLE best_times;
DROP TABLE users;

CREATE TABLE users (
    user_id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_name VARCHAR(60)   NOT NULL UNIQUE,
    added_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
    -- maybe password
);

-- ALTER TABLE users RENAME COLUMN

CREATE TABLE score (
    player_id    INTEGER     NOT NULL,
    player       VARCHAR(64) NOT NULL,
    game         VARCHAR(64) NOT NULL,
    points       INTEGER     NOT NULL,
    playedon     TIMESTAMP   NOT NULL,

    FOREIGN KEY (player_id)
    REFERENCES users(user_id)
    ON DELETE CASCADE
);

CREATE TABLE best_times (
    player_id     INTEGER     NOT NULL,
    level_id      VARCHAR(60) NOT NULL,
    best_time_ms  BIGINT      NOT NULL,
    achieved_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (player_id, level_id),

    FOREIGN KEY (player_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);