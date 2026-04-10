-- ============================================================
-- SEED: 20 fake users + results + reviews
-- password for all: "password123" (bcrypt hash)
-- ============================================================

-- Make sure levels exist
INSERT INTO levels (level_id, level_name) VALUES
  (1,'Level 1'),(2,'Level 2'),(3,'Level 3'),
  (4,'Level 4'),(5,'Level 5'),(6,'Level 6'),
  (7,'Level 7'),(8,'Level 8'),(9,'Level 9')
ON CONFLICT (level_id) DO NOTHING;

-- 20 users (seed_ prefix for easy cleanup)
INSERT INTO users (user_name, password_hash, email) VALUES
  ('seed_alex',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_alex@test.com'),
  ('seed_maria',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_maria@test.com'),
  ('seed_ivan',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_ivan@test.com'),
  ('seed_olena',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_olena@test.com'),
  ('seed_dmytro',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_dmytro@test.com'),
  ('seed_katya',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_katya@test.com'),
  ('seed_bogdan',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_bogdan@test.com'),
  ('seed_nastya',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_nastya@test.com'),
  ('seed_artem',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_artem@test.com'),
  ('seed_sofia',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_sofia@test.com'),
  ('seed_maxim',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_maxim@test.com'),
  ('seed_anya',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_anya@test.com'),
  ('seed_nikita',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_nikita@test.com'),
  ('seed_daria',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_daria@test.com'),
  ('seed_roman',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_roman@test.com'),
  ('seed_lina',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_lina@test.com'),
  ('seed_oleg',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_oleg@test.com'),
  ('seed_vika',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_vika@test.com'),
  ('seed_taras',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_taras@test.com'),
  ('seed_yulia',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seed_yulia@test.com');

-- Best results — each user completed several levels with varying time/score
INSERT INTO best_level_results (user_id, level_id, best_time_ms, best_score, achieved_at)
SELECT u.user_id, g.level_id, g.best_time_ms, g.best_score, g.achieved_at
FROM users u
JOIN (VALUES
  -- seed_alex: total = 8200
  ('seed_alex',   1,  12400, 3500, '2026-04-01 10:15:00'::timestamp),
  ('seed_alex',   2,  18700, 2800, '2026-04-01 10:32:00'::timestamp),
  ('seed_alex',   3,  25100, 1900, '2026-04-02 14:00:00'::timestamp),
  -- seed_maria: total = 11200
  ('seed_maria',  1,   9800, 3800, '2026-04-01 11:00:00'::timestamp),
  ('seed_maria',  2,  15300, 3100, '2026-04-01 11:20:00'::timestamp),
  ('seed_maria',  3,  22000, 2500, '2026-04-02 09:10:00'::timestamp),
  ('seed_maria',  4,  31500, 1800, '2026-04-03 16:00:00'::timestamp),
  -- seed_ivan: total = 3400
  ('seed_ivan',   1,  14200, 2000, '2026-04-02 08:30:00'::timestamp),
  ('seed_ivan',   2,  20100, 1400, '2026-04-02 09:00:00'::timestamp),
  -- seed_olena: total = 12800
  ('seed_olena',  1,  10500, 3600, '2026-04-01 12:00:00'::timestamp),
  ('seed_olena',  2,  16800, 3200, '2026-04-01 12:30:00'::timestamp),
  ('seed_olena',  3,  23400, 2700, '2026-04-02 15:00:00'::timestamp),
  ('seed_olena',  4,  29000, 2000, '2026-04-03 10:00:00'::timestamp),
  ('seed_olena',  5,  37200, 1300, '2026-04-04 11:00:00'::timestamp),
  -- seed_dmytro: total = 7600
  ('seed_dmytro', 1,  11000, 3200, '2026-04-01 14:00:00'::timestamp),
  ('seed_dmytro', 2,  17500, 2600, '2026-04-02 10:00:00'::timestamp),
  ('seed_dmytro', 3,  24800, 1800, '2026-04-03 09:00:00'::timestamp),
  -- seed_katya: total = 9200
  ('seed_katya',  1,  13100, 3100, '2026-04-02 16:00:00'::timestamp),
  ('seed_katya',  2,  19500, 2700, '2026-04-03 11:00:00'::timestamp),
  ('seed_katya',  3,  26700, 2100, '2026-04-04 14:00:00'::timestamp),
  ('seed_katya',  4,  33000, 1300, '2026-04-05 09:00:00'::timestamp),
  -- seed_bogdan: total = 2600
  ('seed_bogdan', 1,  15600, 1500, '2026-04-03 08:00:00'::timestamp),
  ('seed_bogdan', 2,  21400, 1100, '2026-04-03 08:30:00'::timestamp),
  -- seed_nastya: total = 14200
  ('seed_nastya', 1,   8900, 3700, '2026-04-01 09:00:00'::timestamp),
  ('seed_nastya', 2,  14000, 3100, '2026-04-01 09:30:00'::timestamp),
  ('seed_nastya', 3,  19800, 2800, '2026-04-02 10:00:00'::timestamp),
  ('seed_nastya', 4,  27500, 2200, '2026-04-03 14:00:00'::timestamp),
  ('seed_nastya', 5,  34100, 1500, '2026-04-04 16:00:00'::timestamp),
  ('seed_nastya', 6,  42000,  900, '2026-04-05 11:00:00'::timestamp),
  -- seed_artem: total = 10200
  ('seed_artem',  1,  11800, 3400, '2026-04-02 13:00:00'::timestamp),
  ('seed_artem',  2,  18200, 2900, '2026-04-03 15:00:00'::timestamp),
  ('seed_artem',  3,  25600, 2300, '2026-04-04 10:00:00'::timestamp),
  ('seed_artem',  4,  32400, 1600, '2026-04-05 12:00:00'::timestamp),
  -- seed_sofia: total = 14900
  ('seed_sofia',  1,  10100, 3500, '2026-04-01 15:00:00'::timestamp),
  ('seed_sofia',  2,  16200, 3000, '2026-04-02 11:00:00'::timestamp),
  ('seed_sofia',  3,  23000, 2600, '2026-04-03 09:30:00'::timestamp),
  ('seed_sofia',  4,  30200, 2200, '2026-04-04 14:00:00'::timestamp),
  ('seed_sofia',  5,  38500, 1700, '2026-04-05 16:00:00'::timestamp),
  ('seed_sofia',  6,  45000, 1200, '2026-04-06 10:00:00'::timestamp),
  ('seed_sofia',  7,  52000,  700, '2026-04-07 09:00:00'::timestamp),
  -- seed_maxim: total = 4000
  ('seed_maxim',  1,  13500, 2400, '2026-04-03 10:00:00'::timestamp),
  ('seed_maxim',  2,  20800, 1600, '2026-04-04 11:00:00'::timestamp),
  -- seed_anya: total = 6800
  ('seed_anya',   1,  12000, 2900, '2026-04-02 14:30:00'::timestamp),
  ('seed_anya',   2,  17900, 2300, '2026-04-03 16:00:00'::timestamp),
  ('seed_anya',   3,  24200, 1600, '2026-04-04 09:00:00'::timestamp),
  -- seed_nikita: total = 8800
  ('seed_nikita', 1,  14800, 3000, '2026-04-03 12:00:00'::timestamp),
  ('seed_nikita', 2,  21000, 2500, '2026-04-04 13:00:00'::timestamp),
  ('seed_nikita', 3,  28300, 1900, '2026-04-05 10:00:00'::timestamp),
  ('seed_nikita', 4,  35600, 1400, '2026-04-06 14:00:00'::timestamp),
  -- seed_daria: total = 12200
  ('seed_daria',  1,   9500, 3500, '2026-04-01 10:00:00'::timestamp),
  ('seed_daria',  2,  15000, 2900, '2026-04-01 10:30:00'::timestamp),
  ('seed_daria',  3,  21500, 2500, '2026-04-02 11:00:00'::timestamp),
  ('seed_daria',  4,  28800, 1900, '2026-04-03 15:00:00'::timestamp),
  ('seed_daria',  5,  36000, 1400, '2026-04-04 09:30:00'::timestamp),
  -- seed_roman: total = 600
  ('seed_roman',  1,  16000,  600, '2026-04-04 08:00:00'::timestamp),
  -- seed_lina: total = 5800
  ('seed_lina',   1,  11500, 2500, '2026-04-02 15:00:00'::timestamp),
  ('seed_lina',   2,  18000, 2000, '2026-04-03 10:00:00'::timestamp),
  ('seed_lina',   3,  25000, 1300, '2026-04-04 12:00:00'::timestamp),
  -- seed_oleg: total = 3200
  ('seed_oleg',   1,  13800, 1900, '2026-04-03 14:00:00'::timestamp),
  ('seed_oleg',   2,  20500, 1300, '2026-04-04 16:00:00'::timestamp),
  -- seed_vika: total = 11800
  ('seed_vika',   1,  10800, 3300, '2026-04-01 13:00:00'::timestamp),
  ('seed_vika',   2,  17200, 2800, '2026-04-02 14:00:00'::timestamp),
  ('seed_vika',   3,  24000, 2400, '2026-04-03 11:00:00'::timestamp),
  ('seed_vika',   4,  31000, 1900, '2026-04-04 15:00:00'::timestamp),
  ('seed_vika',   5,  39000, 1400, '2026-04-05 10:00:00'::timestamp),
  -- seed_taras: total = 2200
  ('seed_taras',  1,  15200, 1300, '2026-04-04 09:00:00'::timestamp),
  ('seed_taras',  2,  22000,  900, '2026-04-05 11:00:00'::timestamp),
  -- seed_yulia: total = 14850
  ('seed_yulia',  1,   9200, 3200, '2026-04-01 08:00:00'::timestamp),
  ('seed_yulia',  2,  14500, 2700, '2026-04-01 08:30:00'::timestamp),
  ('seed_yulia',  3,  20500, 2300, '2026-04-02 09:00:00'::timestamp),
  ('seed_yulia',  4,  27000, 1900, '2026-04-03 12:00:00'::timestamp),
  ('seed_yulia',  5,  34800, 1600, '2026-04-04 14:00:00'::timestamp),
  ('seed_yulia',  6,  43000, 1300, '2026-04-05 16:00:00'::timestamp),
  ('seed_yulia',  7,  50500,  900, '2026-04-06 11:00:00'::timestamp),
  ('seed_yulia',  8,  58000,  550, '2026-04-07 13:00:00'::timestamp),
  ('seed_yulia',  9,  67000,  400, '2026-04-08 10:00:00'::timestamp)
) AS g(user_name, level_id, best_time_ms, best_score, achieved_at)
ON u.user_name = g.user_name;

-- Reviews — one per user (user_id is PK)
INSERT INTO reviews (user_id, rating, comment, created_at)
SELECT u.user_id, r.rating, r.comment, r.created_at
FROM users u
JOIN (VALUES
  ('seed_alex',   5, 'Amazing game, the mazes are so addictive!',                            '2026-04-02 18:00:00'::timestamp),
  ('seed_maria',  5, 'Love it! Already cleared 4 levels, can''t stop playing',               '2026-04-03 20:00:00'::timestamp),
  ('seed_ivan',   4, 'Cool mechanics, but I wish there were more levels',                    '2026-04-02 12:00:00'::timestamp),
  ('seed_olena',  5, 'Best puzzle game I''ve ever played, hands down',                       '2026-04-04 15:00:00'::timestamp),
  ('seed_dmytro', 4, 'Great game overall, level 6 is brutal though',                         '2026-04-03 14:00:00'::timestamp),
  ('seed_katya',  3, 'Not bad, controls feel a bit laggy sometimes',                         '2026-04-05 11:00:00'::timestamp),
  ('seed_bogdan', 4, 'Solid brain teaser, good for warming up in the morning',               '2026-04-03 09:30:00'::timestamp),
  ('seed_nastya', 5, 'Finished all 6 levels in a day, need more content ASAP!',              '2026-04-05 19:00:00'::timestamp),
  ('seed_artem',  4, 'Nice difficulty curve and pleasant design',                             '2026-04-05 14:00:00'::timestamp),
  ('seed_sofia',  5, 'Completed all 7 levels — what an experience!',                         '2026-04-07 12:00:00'::timestamp),
  ('seed_maxim',  3, 'It''s okay, would really appreciate hints on harder levels',           '2026-04-04 13:00:00'::timestamp),
  ('seed_anya',   4, 'Beautiful graphics and smooth gameplay',                                '2026-04-04 11:00:00'::timestamp),
  ('seed_nikita', 4, 'Got hooked for hours, would definitely recommend',                     '2026-04-06 16:00:00'::timestamp),
  ('seed_daria',  5, 'My favorite! Every single level feels unique',                         '2026-04-04 12:00:00'::timestamp),
  ('seed_roman',  3, 'Only beat level 1 so far but I''m already enjoying it',                '2026-04-04 10:00:00'::timestamp),
  ('seed_lina',   4, 'Love the logic puzzles, multiplayer mode would be awesome',            '2026-04-04 14:00:00'::timestamp),
  ('seed_oleg',   4, 'Simple rules but the levels really make you think',                    '2026-04-04 18:00:00'::timestamp),
  ('seed_vika',   5, 'Totally addicted, 5 levels down and counting',                         '2026-04-05 12:00:00'::timestamp),
  ('seed_taras',  3, 'Decent game, could use some background music though',                  '2026-04-05 13:00:00'::timestamp),
  ('seed_yulia',  5, 'All 9 levels completed! Absolute masterpiece!',                        '2026-04-08 14:00:00'::timestamp)
) AS r(user_name, rating, comment, created_at)
ON u.user_name = r.user_name;


-- ============================================================
-- CLEANUP: delete all seed users (cascades to
-- best_level_results, reviews, user_sessions)
-- ============================================================
-- DELETE FROM users WHERE user_name LIKE 'seed_%';