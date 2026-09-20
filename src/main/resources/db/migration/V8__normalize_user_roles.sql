UPDATE users
SET role = 'ADMIN'
WHERE role = 'ROLE_ADMIN';

UPDATE users
SET role = 'USER'
WHERE role = 'ROLE_USER';