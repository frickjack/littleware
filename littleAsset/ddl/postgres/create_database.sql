
CREATE GROUP littleware_user_group;

Create USER littleware 
    WITH UNENCRYPTED PASSWORD 'littleware_password' 
    IN GROUP littleware_user_group;

CREATE USER littleware_admin
    WITH UNENCRYPTED PASSWORD 'littleware_admin_password' 
    IN GROUP littleware_user_group;

CREATE USER littleware_user 
    WITH UNENCRYPTED PASSWORD 'littleware_user_password' 
    IN GROUP littleware_user_group;

CREATE DATABASE littleware WITH 
     OWNER = littleware;


CREATE LANGUAGE plpgsql;
