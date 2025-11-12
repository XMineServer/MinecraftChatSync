-- Users
DELETE FROM users;
DELETE FROM user_links;
DELETE FROM group_links;

SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE user_links;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS=1;

ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE user_links AUTO_INCREMENT = 1;

INSERT INTO users (uuid, username) VALUES
                                       (UUID(), 'alice'),
                                       (UUID(), 'bob');


INSERT INTO user_links (user_id, platform, external_id)
VALUES (1, 'telegram', '12345');

INSERT INTO group_links (platform, context_path, linked_at)
VALUES
    ('telegram', 'group/dev', NOW() - INTERVAL 2 DAY),
    ('telegram', 'group/admins', NOW() - INTERVAL 1 DAY),
    ('discord', 'channel/general', NOW());
