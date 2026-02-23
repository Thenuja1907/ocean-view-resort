USE ocean_view_resort;
UPDATE users SET password_hash = '$2a$12$KIx5TwMbFcAEfxQGMFhWRu3G6z7m4j8Qlj9mL2sKP1pXbRNz1cA3S' WHERE username = 'admin';
UPDATE users SET password_hash = '$2a$12$KIx5TwMbFcAEfxQGMFhWRu3G6z7m4j8Qlj9mL2sKP1pXbRNz1cA3S' WHERE username = 'receptionist';
SELECT username, password_hash FROM users;
