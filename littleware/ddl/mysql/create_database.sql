CREATE USER 'littleware' IDENTIFIED BY 'littleware_password';

CREATE USER 'littleware_user' IDENTIFIED BY  'littleware_user_password';

CREATE USER 'fm_rw'@'%' IDENTIFIED BY  'fm_rw';

CREATE DATABASE littleware;

GRANT ALL PRIVILEGES ON littleware.* TO 'littleware' WITH GRANT OPTION;

