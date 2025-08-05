INSERT INTO users (email, password_hash, first_name, last_name, phone_number, role_id)
VALUES ('admin@example.com',
           --Admin@123
        '$2a$10$aWV/We1hnHQcOgayQQ5pmeTiseepBA1GGIE6WboNUVV4TzgDxahSi',
        'Admin',
        'User',
        '+10000000000',
        (SELECT id FROM role WHERE name = 'ADMIN'));
