--
-- accounts.sql
--
-- Little database for tracking information on user's
-- that have accounts with a littleware server,
-- specifying what access permissions the user has,
-- and grouping users. Bla bla.
--
--


--
-- Track the password associated with 'principal' type assets
--
CREATE TABLE littleware.password (
	s_asset_id          VARCHAR(32) PRIMARY KEY,
	s_password          VARCHAR(40),
	t_created           TIMESTAMP NOT NULL DEFAULT now(),
	t_updated           TIMESTAMP NOT NULL DEFAULT now()
	);
	
GRANT SELECT, UPDATE, INSERT, DELETE ON littleware.password TO GROUP littleware_user_group;



--
-- Check password for the given user
--
-- @param s_param_user_id
-- @param s_param_password
-- @return true if user authenticates, false otherwise
--
CREATE OR REPLACE FUNCTION littleware.checkPassword ( VARCHAR, VARCHAR )
    RETURNS BOOLEAN AS $FUNC$
	DECLARE
	    s_param_user_id     ALIAS FOR $1;
		s_param_password    ALIAS FOR $2;
	BEGIN
	    PERFORM s_password FROM littleware.password WHERE
		         s_asset_id=s_param_user_id AND s_password=s_param_password;
        IF FOUND THEN
		    RETURN true;
		END IF;
	
		RETURN false;
	END; $FUNC$
LANGUAGE plpgsql;

GRANT EXECUTE ON FUNCTION littleware.checkPassword( VARCHAR, VARCHAR ) TO littleware_user_group;

--
-- Save new password for the given user
--
-- @param s_param_user_id
-- @param s_param_password
-- @return true if new entry added to db, false if just an update
--
CREATE OR REPLACE FUNCTION littleware.savePassword ( VARCHAR, VARCHAR )
    RETURNS BOOLEAN AS $FUNC$
	DECLARE
	    s_param_user_id     ALIAS FOR $1;
		s_param_password    ALIAS FOR $2;
		b_var_result        BOOLEAN;
	BEGIN
	    PERFORM s_password FROM littleware.password WHERE
		         s_asset_id=s_param_user_id;
        IF FOUND THEN
		    UPDATE littleware.password 
			    SET s_password=s_param_password, t_updated=now()
			WHERE s_asset_id=s_param_user_id;
		    RETURN false;
		END IF;
	    INSERT INTO littleware.password (s_asset_id, s_password) 
		     VALUES ( s_param_user_id, s_param_password );
		RETURN true;
	END; $FUNC$
LANGUAGE plpgsql;

GRANT EXECUTE ON FUNCTION littleware.savePassword( VARCHAR, VARCHAR ) TO littleware_user_group;

