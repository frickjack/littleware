CREATE USER 'littleware'@'localhost' IDENTIFIED BY 'littleware_password';

CREATE USER 'littleware_user' IDENTIFIED BY  'littleware_user_password';

CREATE USER 'fm_rw'@'%' IDENTIFIED BY  'fm_rw';

CREATE DATABASE littleware;

GRANT ALL PRIVILEGES ON littleware.* TO littleware WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON littleware.* TO 'littleware'@'localhost' WITH GRANT OPTION;
GRANT SUPER ON *.* TO 'littleware'@'localhost';

