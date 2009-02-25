

--
-- Get the UUID associated with the type of the given name
--
-- @param s_name of the type we want data for
-- @return UUID if type exists, else NULL
--
CREATE OR REPLACE FUNCTION littleware.getTypeId ( VARCHAR )
    RETURNS VARCHAR AS $FUNC$
	DECLARE
		s_param_name     ALIAS FOR $1;
		s_var_pk_type    VARCHAR;
	BEGIN
	    SELECT INTO s_var_pk_type s_id
					FROM littleware.x_asset_type WHERE s_name=s_param_name;
		IF NOT FOUND THEN			
		   RAISE EXCEPTION 'littleware(notype): no such type %',
						   s_param_name;
	    END IF;
		RETURN s_var_pk_type;
	END; $FUNC$
LANGUAGE plpgsql;

GRANT EXECUTE ON FUNCTION littleware.getTypeId( VARCHAR ) TO littleware_user_group;


--
-- Get the UUID associated with the permission of the given name
--
-- @param s_name of the permission we want data for
-- @return UUID if type exists, else NULL
--
CREATE OR REPLACE FUNCTION littleware.getPermissionId ( VARCHAR )
    RETURNS VARCHAR AS $FUNC$
	DECLARE
		s_param_name     ALIAS FOR $1;
		s_var_pk_type    VARCHAR;
	BEGIN
	    SELECT INTO s_var_pk_type s_id
					FROM littleware.x_permission WHERE s_name=s_param_name;
		IF NOT FOUND THEN			
		   RAISE EXCEPTION 'littleware(noperm): no such permission %',
						   s_param_name;
	    END IF;
		RETURN s_var_pk_type;
	END; $FUNC$
LANGUAGE plpgsql;

GRANT EXECUTE ON FUNCTION littleware.getPermissionId( VARCHAR ) TO littleware_user_group;



--
-- littleware.copyTransactionHistory ( l_last_transaction )
--
-- Copy the asset-entry with the specified last-transaction
-- into the asset-history table.
--
-- RETURN NUMBER of rows inserted (1 on success, 0 if none found)
--
CREATE OR REPLACE FUNCTION littleware.copyTransactionHistory ( BIGINT )
    RETURNS INTEGER AS ' 
    DECLARE
        l_param_transaction   ALIAS FOR $1;
		
    BEGIN
	    -- Update max-transaction on entries already in history-table
	    UPDATE littleware.asset_history 
		           SET l_max_transaction=l_param_transaction
				   WHERE 
				      s_id IN (SELECT s_id FROM littleware.asset
					            WHERE l_last_transaction=l_param_transaction)
				      AND l_max_transaction=-1;
					  
		-- Copy history
        INSERT INTO littleware.asset_history 
		   (
		   	s_id  ,
			s_name        ,
			s_id_home     ,
			l_min_transaction ,
			s_pk_type         ,
			s_id_creator      ,
			s_id_updater      ,
			s_id_owner        ,
			f_value           ,
			s_id_acl          ,
			s_comment         ,
			s_last_change     ,
			s_data            ,
			s_id_from   ,
			s_id_to     ,
			t_created         ,
			t_updated         ,
			t_last_accessed   ,
			t_start           ,
			t_end             
		   )
           SELECT 
				s_id                ,
				s_name              ,
				s_id_home           ,
				l_last_transaction  , 
				s_pk_type           ,
				s_id_creator        ,
				s_id_updater        ,
				s_id_owner          ,
				f_value             ,
				s_id_acl            ,
				s_comment           ,
				s_last_change       ,
				s_data              ,
				s_id_from           ,
				s_id_to             ,
				t_created           ,
				t_updated           ,
				t_last_accessed     ,
				t_start             ,
				t_end   
			FROM littleware.asset WHERE l_last_transaction=l_param_transaction;

        IF FOUND THEN
            RETURN 1;
        END IF;

        RETURN 0;
     END;
    '
    LANGUAGE plpgsql;

GRANT EXECUTE ON FUNCTION 
        littleware.copyTransactionHistory ( BIGINT )
	TO littleware_user_group;

--
-- Little conversion function takes a string to a timestamp.
-- The freakin' postgres jdbc driver is not handling Timestamp
-- correctly, so work around it this way.
--
CREATE OR REPLACE FUNCTION littleware.stringToTimestamp ( VARCHAR ) 
    RETURNS TIMESTAMP AS $FUNC$
	DECLARE
	    s_param_timestring  ALIAS FOR $1;
	BEGIN
	    RETURN s_param_timestring;
	END;
    $FUNC$
    LANGUAGE plpgsql;
	
GRANT EXECUTE ON FUNCTION littleware.stringToTimestamp ( VARCHAR	 )
 TO littleware_user_group;


--
-- littleware.saveAsset ( 
--                         str_name, s_type, str_from_asset, str_to_asset,
--                         str_caller_id, str_acl_id, str_data, f_value, 
--                         str_asset_comment, str_update_comment,
--                         str_home_id, s_dt_start, s_dt_end, s_uuid_pk, s_owner,
--                         l_last_transaction, i_src
--                  );
--
-- Create an asset at the given position in the tree.  
--
-- @param l_last_transaction must equal the transaction number in the database
--                 if not null - otherwise throws a synchronization exception
-- @param i_src id of client issueing the command
-- @return the new transaction number assigned to the asset
-- @exception synchException
-- 
CREATE OR REPLACE FUNCTION littleware.saveAsset ( VARCHAR, VARCHAR, VARCHAR, VARCHAR, 
                                                    VARCHAR, VARCHAR, VARCHAR, NUMERIC(16,4), 
													VARCHAR, VARCHAR,
													VARCHAR, VARCHAR, VARCHAR, 
													VARCHAR, VARCHAR, BIGINT, INTEGER
													 )
    RETURNS BIGINT AS $FUNC$
    DECLARE
           s_param_name        ALIAS FOR $1;
           s_param_pk_type     ALIAS FOR $2;
		   s_param_from_asset  ALIAS FOR $3;
		   s_param_to_asset    ALIAS FOR $4;
           s_param_caller      ALIAS FOR $5;
           s_param_acl         ALIAS FOR $6;
           s_param_data        ALIAS FOR $7;
		   f_param_value       ALIAS FOR $8;
		   s_param_comment     ALIAS FOR $9;
		   s_param_update      ALIAS FOR $10;
		   s_param_home        ALIAS FOR $11;
		   s_param_start       ALIAS FOR $12;
		   s_param_stop        ALIAS FOR $13;
		   s_param_uuid        ALIAS FOR $14;
		   s_param_owner       ALIAS FOR $15;
           l_param_transaction ALIAS FOR $16;
           i_param_src         ALIAS FOR $17;
           l_var_transaction   BIGINT;
		   s_var_pk_home_type  VARCHAR;
		   i_tmp               INTEGER;
		   t_var_stop          TIMESTAMP;
		   t_var_start         TIMESTAMP;
           rec_tmp             RECORD;
    BEGIN
	       s_var_pk_home_type = littleware.getTypeId ( 'littleware.home' );
		   t_var_stop = littleware.stringToTimestamp ( s_param_stop );
		   t_var_start = littleware.stringToTimestamp ( s_param_start );

	       -- 
		   -- Enforce that home, user, group, and acl assets have a unique name
		   --
		   IF s_param_pk_type = s_var_pk_home_type THEN
		       SELECT INTO l_var_transaction l_last_transaction 
			    FROM littleware.asset WHERE
			       s_pk_type=s_param_pk_type AND s_name=s_param_name
				   AND s_id != s_param_uuid
				   ;
			    IF FOUND THEN
				    RAISE EXCEPTION 'littleware(homename): Home asset with name % already exists', s_param_name;
				END IF;
			ELSIF s_param_home IS NULL THEN
			    RAISE EXCEPTION 'littleware(nohome): must specify a non-null home asset';
			ELSE
			   --
			   -- If not a HOME asset, then make sure the HOME exists on this server
			   --
			   SELECT INTO l_var_transaction l_last_transaction 
			      FROM littleware.asset WHERE
			       s_id=s_param_home AND s_pk_type=s_var_pk_home_type;
			   IF NOT FOUND THEN
			       RAISE EXCEPTION 'littleware(nohome): Home asset % does not exist on this server', s_param_home;
			   END IF;
			END IF;
            
            IF l_param_transaction IS NOT NULL THEN
                -- 
                -- Check for cache coherency
                --
                SELECT INTO l_var_transaction l_last_transaction 
			      FROM littleware.asset WHERE s_id=s_param_uuid;
                IF l_var_transaction IS NOT NULL AND l_var_transaction != l_param_transaction THEN
                   RAISE EXCEPTION 'littleware(sync): caller transaction out of date';
                END IF;
            END IF;
               
			IF s_param_pk_type IN ( SELECT s_id FROM littleware.x_asset_type
			                          WHERE b_name_unique=true
							      ) THEN
			    -- enforce name uniqueness on these types
			   PERFORM l_last_transaction 
			    FROM littleware.asset WHERE
			       (s_pk_type=s_param_pk_type 
                     OR s_pk_type IN (SELECT s_ancestor_id FROM littleware.x_asset_type_tree
                                          WHERE s_descendent_id=s_param_pk_type)
                     OR s_pk_type IN (SELECT s_descendent_id FROM littleware.x_asset_type_tree
                                          WHERE s_ancestor_id=s_param_pk_type)
                                          )                     
                   AND s_name=s_param_name AND s_id != s_param_uuid;
			   IF FOUND THEN
					RAISE EXCEPTION 'littleware(uniquename): unique-name type asset with name % already exists', s_param_name;
			    END IF;
			END IF;
				
           --
		   -- Verify that we don't have colliding asset-child names
		   --
		   IF s_param_from_asset IS NOT NULL THEN
		       PERFORM l_last_transaction
			       FROM littleware.asset WHERE s_id=s_param_from_asset;
		       IF NOT FOUND THEN
			       RAISE EXCEPTION 'littleware(badfrom): From asset % does not exist on this server', s_param_from_asset;
			   END IF;
			   
			   SELECT INTO i_tmp COUNT(*) FROM littleware.asset
			       WHERE s_id_from=s_param_from_asset AND s_name=s_param_name AND s_id != s_param_uuid;
			   IF i_tmp > 0 THEN
			       RAISE EXCEPTION 'littleware(fromchild): from-asset % child name % already in use',
				                   s_param_from_asset, s_param_name;
			   END IF;
		   END IF;
		   
           l_var_transaction := nextval( 'littleware.seq_transaction_counter' );
		   
		   PERFORM l_last_transaction FROM littleware.asset WHERE s_id=s_param_uuid;
		   IF FOUND THEN
				UPDATE littleware.asset SET
					s_name              = s_param_name,
					s_id_home           = s_param_home,
					l_last_transaction  = l_var_transaction,
					s_id_updater        = s_param_caller,
					s_id_owner          = s_param_owner,
					f_value             = f_param_value,
					s_id_acl            = s_param_acl,
					s_comment           = s_param_comment,
					s_last_change       = s_param_update,
					s_data              = s_param_data,
					s_id_from           = s_param_from_asset,
					s_id_to             = s_param_to_asset,
					t_start             = t_var_start,
					t_end               = t_var_stop,
					t_updated           = now()
                  WHERE
					s_id = s_param_uuid;
                UPDATE littleware.client_cache SET i_status=1 
                     WHERE s_asset_id=s_param_uuid AND i_src != i_param_src;
		   ELSE
			   INSERT INTO littleware.asset (
					s_id                ,
					s_name              ,
					s_id_home           ,
					l_last_transaction  ,
					s_pk_type           ,
					s_id_creator        ,
					s_id_updater        ,
					s_id_owner          ,
					f_value             ,
					s_id_acl            ,
					s_comment           ,
					s_last_change       ,
					s_data              ,
					s_id_from           ,
					s_id_to             ,
					t_start             ,
					t_end   
				) VALUES (          
					s_param_uuid         ,
					s_param_name         ,
					s_param_home         ,
					l_var_transaction    ,
					s_param_pk_type      ,
					s_param_caller       ,
					s_param_caller       ,
					s_param_owner        ,
					f_param_value        ,
					s_param_acl          ,
					s_param_comment      ,
					s_param_update       ,
					s_param_data         ,
					s_param_from_asset   ,
					s_param_to_asset     ,
					t_var_start        ,
					t_var_stop
					);
               FOR rec_tmp IN SELECT DISTINCT i_src FROM littleware.client_cache WHERE i_src != i_param_src
               LOOP
                   INSERT INTO littleware.client_cache (i_src, s_asset_id, i_status)
                      VALUES (rec_tmp.i_src, s_param_uuid, 1);
               END LOOP;

			END IF;

           PERFORM littleware.copyTransactionHistory ( l_var_transaction );
           RETURN l_var_transaction;
    END;
    $FUNC$
    LANGUAGE plpgsql;
	
GRANT EXECUTE ON FUNCTION 
 littleware.saveAsset ( VARCHAR, VARCHAR, VARCHAR, VARCHAR, 
                                                    VARCHAR, VARCHAR, VARCHAR, NUMERIC(16,4), VARCHAR,
													VARCHAR, VARCHAR, VARCHAR, VARCHAR, 
													VARCHAR, VARCHAR, BIGINT, INTEGER
													 )
 TO littleware_user_group;

--- removeAsset ( str_asset_url, str_name );
--- updateAssetLink ( i_asset_id, i_link_id, l_last_transaction_id );
--- updateAssetName ( i_asset_id, str_name, l_last_transaction_id );
--- updateAssetData ( i_asset_id, str_data, l_last_transaction_id );
--- updateAssetcreator ( i_asset_id, i_creator_id, l_last_transaction_id );
--- updateAssetAcl ( i_asset_id, i_acl_id, l_last_transaction_id );
--- getAssetTransactions ( i_asset_id );

--
-- Get the asset with the specified ID
--
-- @param  s_param_asset_id    UUID of asset to retrieve
-- @param  i_src               client id
--
CREATE OR REPLACE FUNCTION littleware.getAsset ( VARCHAR, INTEGER )
    RETURNS SETOF littleware.asset AS $FUNC$
    DECLARE
       s_param_asset_id    ALIAS FOR $1;
       i_param_src         ALIAS FOR $2;
       rec_var_result      littleware.asset%ROWTYPE;
    BEGIN
	SELECT INTO rec_var_result 
				s_id                ,
				s_name              ,
				s_id_home           ,
				l_last_transaction  , 
				s_pk_type           ,
				s_id_creator        ,
				s_id_updater        ,
				s_id_owner          ,
				f_value             ,
				s_id_acl            ,
				s_comment           ,
				s_last_change       ,
				s_data              ,
				s_id_from           ,
				s_id_to             ,
				t_created           ,
				t_updated           ,
				t_last_accessed     ,
				t_start             ,
				t_end   
					FROM littleware.asset WHERE s_id=s_param_asset_id;
		
		IF FOUND THEN
            PERFORM littleware.addToCache( i_param_src, s_param_asset_id );
		    RETURN NEXT rec_var_result;
		END IF;
		
        RETURN;
    END; $FUNC$
LANGUAGE plpgsql;

GRANT EXECUTE ON FUNCTION littleware.getAsset( VARCHAR, INTEGER ) TO littleware_user_group;

--
-- Load the assets with the specified name and type and (optionally) home
--
-- @param s_name of the assets
-- @param s_pk_type
-- @param s_home_id
-- @param i_src
-- @return SETOF littleware.asset matching query conditions
--
CREATE OR REPLACE FUNCTION littleware.getAssetsByName( VARCHAR, VARCHAR, VARCHAR, INTEGER )
    RETURNS SETOF littleware.asset AS $FUNC$
    DECLARE
       s_param_name        ALIAS FOR $1;
	   s_param_pk_type     ALIAS FOR $2;
	   s_param_id_home     ALIAS FOR $3;
       i_param_src         ALIAS FOR $4;
       rec_info            littleware.asset%ROWTYPE;
    BEGIN
        IF s_param_id_home IS NOT NULL THEN
		    FOR rec_info IN SELECT
				s_id                ,
				s_name              ,
				s_id_home           ,
				l_last_transaction  , 
				s_pk_type           ,
				s_id_creator        ,
				s_id_updater        ,
				s_id_owner          ,
				f_value             ,
				s_id_acl            ,
				s_comment           ,
				s_last_change       ,
				s_data              ,
				s_id_from           ,
				s_id_to             ,
				t_created           ,
				t_updated           ,
				t_last_accessed     ,
				t_start             ,
				t_end   
					FROM littleware.asset WHERE s_name=s_param_name 
					       AND ( s_pk_type=s_param_pk_type 
                                 OR s_pk_type IN (SELECT s_descendent_id 
                                                  FROM littleware.x_asset_type_tree
                                                  WHERE s_ancestor_id=s_param_pk_type)
                                )                                                    
                           AND s_id_home=s_param_id_home
			LOOP
			    RETURN NEXT rec_info;
                PERFORM littleware.addToCache ( i_param_src, rec_info.s_id );
		    END LOOP;
		ELSE
		    FOR rec_info IN SELECT 
				s_id                ,
				s_name              ,
				s_id_home           ,
				l_last_transaction  , 
				s_pk_type           ,
				s_id_creator        ,
				s_id_updater        ,
				s_id_owner          ,
				f_value             ,
				s_id_acl            ,
				s_comment           ,
				s_last_change       ,
				s_data              ,
				s_id_from           ,
				s_id_to             ,
				t_created           ,
				t_updated           ,
				t_last_accessed     ,
				t_start             ,
				t_end   
					FROM littleware.asset WHERE s_name=s_param_name 
                       AND ( s_pk_type=s_param_pk_type 
                                 OR s_pk_type IN (SELECT s_descendent_id 
                                                  FROM littleware.x_asset_type_tree
                                                  WHERE s_ancestor_id=s_param_pk_type)
                                )
			LOOP
			    RETURN NEXT rec_info;
                PERFORM littleware.addToCache ( i_param_src, rec_info.s_id );
		    END LOOP;
		END IF;
		
        RETURN;
    END; $FUNC$
LANGUAGE plpgsql;

GRANT EXECUTE ON FUNCTION littleware.getAssetsByName( VARCHAR, VARCHAR, VARCHAR, INTEGER ) TO littleware_user_group;


--
-- Get a dump of all the asset-type data in the database.
--
CREATE OR REPLACE FUNCTION littleware.getTypeDictionary ()
    RETURNS SETOF littleware.x_asset_type AS $FUNC$
	DECLARE
	    rec_info         littleware.x_asset_type%ROWTYPE;
	BEGIN
	    FOR rec_info IN SELECT *
					FROM littleware.x_asset_type LOOP
			RETURN NEXT rec_info;
		END LOOP;
	
		RETURN;
	END; $FUNC$
LANGUAGE plpgsql;

GRANT EXECUTE ON FUNCTION littleware.getTypeDictionary() TO littleware_user_group;


--
-- Get the assets of type 'home'
--
-- @param i_src database client id
-- @return setof name2id entries
--
CREATE OR REPLACE FUNCTION littleware.getHomeIdDictionary ( INTEGER )
    RETURNS SETOF littleware.name2id AS $FUNC$
    DECLARE
       i_param_src   ALIAS FOR $1;
       rec_info      littleware.name2id;
	BEGIN
	    FOR rec_info IN SELECT s_name, s_id 
					FROM littleware.asset WHERE s_pk_type=littleware.getTypeId( 'littleware.home' ) LOOP
            PERFORM littleware.addToCache ( i_param_src, rec_info.s_id );
			RETURN NEXT rec_info;
		END LOOP;
	
		RETURN;
    END; $FUNC$
LANGUAGE plpgsql;


GRANT EXECUTE ON FUNCTION littleware.getHomeIdDictionary( INTEGER ) TO littleware_user_group;


--
-- Get the child assets of the given parent.
--
-- @param s_parent_id of from-asset - must exist on this server
-- @param s_pk_type to limit result-set to, may be NULL
-- @param i_src_id of client
-- @return setof name2id entries
--
CREATE OR REPLACE FUNCTION littleware.getChildIdDictionary ( VARCHAR, VARCHAR, INTEGER )
    RETURNS SETOF littleware.name2id AS $FUNC$
    DECLARE
	   s_param_id_parent   ALIAS FOR $1;
	   s_param_pk_type     ALIAS FOR $2;
       i_param_src         ALIAS FOR $3;
       rec_info            littleware.name2id;
	BEGIN
        -- Don't freak out if the parent is deleted ...
	    -- PERFORM s_id FROM littleware.asset WHERE s_id=s_param_id_parent;
		-- IF NOT FOUND THEN
		--	RAISE EXCEPTION 'littleware(noparent): parent does not exist: %', s_param_id_parent;
		-- END IF;

        IF s_param_pk_type IS NOT NULL THEN
		    FOR rec_info IN SELECT s_name, s_id 
			                   FROM littleware.asset 
				               WHERE s_id_from = s_param_id_parent
					            AND (s_pk_type=s_param_pk_type 
                                     OR s_pk_type IN (SELECT s_descendent_id 
                                                      FROM littleware.x_asset_type_tree
                                                      WHERE s_ancestor_id=s_param_pk_type)
                                )
            LOOP
			    RETURN NEXT rec_info;
                PERFORM littleware.addToCache ( i_param_src, rec_info.s_id );
		    END LOOP;
		ELSE
		    FOR rec_info IN SELECT s_name, s_id 
			                   FROM littleware.asset 
				               WHERE s_id_from = s_param_id_parent LOOP
			    RETURN NEXT rec_info;
                PERFORM littleware.addToCache ( i_param_src, rec_info.s_id );
		    END LOOP;
		END IF;
	
		RETURN;
    END; $FUNC$ 
LANGUAGE plpgsql;


GRANT EXECUTE ON FUNCTION littleware.getChildIdDictionary( VARCHAR, VARCHAR, INTEGER ) TO littleware_user_group;


--
-- Get the child assets of the given parent.
--
-- @param s_to_id of to-asset 
-- @param s_pk_type to limit result-set to, may be NULL
-- @param i_src_id of client
-- @return setof name2id entries
--
CREATE OR REPLACE FUNCTION littleware.getAssetIdsTo ( VARCHAR, VARCHAR, INTEGER )
    RETURNS SETOF littleware.id AS $FUNC$
    DECLARE
	   s_param_id_to       ALIAS FOR $1;
	   s_param_pk_type     ALIAS FOR $2;
       i_param_src         ALIAS FOR $3;
       rec_info            littleware.id;
	BEGIN

        FOR rec_info IN SELECT s_id 
                           FROM littleware.asset 
                           WHERE s_id_to = s_param_id_to
                            AND (s_pk_type=s_param_pk_type 
                                OR s_pk_type IN (SELECT s_descendent_id 
                                                      FROM littleware.x_asset_type_tree
                                                      WHERE s_ancestor_id=s_param_pk_type)
                            ) LOOP
            RETURN NEXT rec_info;
            PERFORM littleware.addToCache ( i_param_src, rec_info.s_id );
        END LOOP;
	
		RETURN;
    END; $FUNC$ 
LANGUAGE plpgsql;


GRANT EXECUTE ON FUNCTION littleware.getAssetIdsTo( VARCHAR, VARCHAR, INTEGER ) TO littleware_user_group;


--
-- Delete the asset with the given id after applying the given UPDATE comment
--
-- @param s_id of asset to delete
-- @param s_update_comment
-- @param i_src client id
-- @return transaction number of update
--
CREATE OR REPLACE FUNCTION littleware.deleteAsset ( VARCHAR, VARCHAR, INTEGER )
    RETURNS BIGINT AS $FUNC$
    DECLARE
	   s_param_id             ALIAS FOR $1;
	   s_param_update_comment ALIAS FOR $2;
       i_param_src            ALIAS FOR $3;
	   l_var_transaction      BIGINT;
	BEGIN
	   l_var_transaction := nextval( 'littleware.seq_transaction_counter' );
	   UPDATE littleware.asset SET s_last_change=s_param_update_comment
	             WHERE s_id=s_param_id;
	   IF NOT FOUND THEN
			RAISE EXCEPTION 'littleware(noasset): asset does not exist: %', s_param_id;
	   END IF;	
       PERFORM littleware.copyTransactionHistory ( l_var_transaction );
	   DELETE FROM littleware.asset WHERE s_id=s_param_id;
       UPDATE littleware.client_cache SET i_status=2 WHERE s_asset_id=s_param_id;
	   
	   RETURN l_var_transaction;
    END; $FUNC$
LANGUAGE plpgsql;

GRANT EXECUTE ON FUNCTION littleware.deleteAsset( VARCHAR, VARCHAR, INTEGER ) TO littleware_user_group;

--
-- Add the given asset id to the client with given src id cache
--
-- @param i_param_src id of the client to clear the cache image for
-- @param s_param_asset_id to add to cache
--
CREATE OR REPLACE FUNCTION littleware.addToCache( INTEGER, VARCHAR )
    RETURNS INTEGER AS $FUNC$
    DECLARE
        i_param_src        ALIAS FOR $1;
        s_param_asset_id   ALIAS FOR $2;
        i_var_old_status   INTEGER;
    BEGIN
        SELECT INTO i_var_old_status i_status
            FROM littleware.client_cache
            WHERE i_src=i_param_src AND s_asset_id=s_param_asset_id;
            
        IF NOT FOUND THEN
            INSERT INTO littleware.client_cache (i_src, s_asset_id, i_status)
                      VALUES (i_param_src, s_param_asset_id, 0 );
        ELSIF i_var_old_status != 0 THEN
            UPDATE littleware.client_cache SET i_status=0 
                 WHERE i_src=i_param_src AND s_asset_id=s_param_asset_id;
            
        END IF;
        RETURN 0;
    END; $FUNC$
LANGUAGE plpgsql;

GRANT EXECUTE ON FUNCTION littleware.addToCache( INTEGER, VARCHAR ) TO littleware_user_group;


--
-- Clear the cache for the given source
--
-- @param i_param_src id of the client to clear the cache image for
--
CREATE OR REPLACE FUNCTION littleware.clearCache( INTEGER )
    RETURNS INTEGER AS $FUNC$
    DECLARE
        i_param_src        ALIAS FOR $1;
    BEGIN
        DELETE FROM littleware.client_cache WHERE i_src=i_param_src;
        RETURN 0;
    END;
 $FUNC$
LANGUAGE plpgsql;

GRANT EXECUTE ON FUNCTION littleware.clearCache( INTEGER ) TO littleware_user_group;

--
-- Synchronize the cache for the given source.
-- Return to caller the set of asssets the client with the
-- given src-id needs to sync up with, and cleanup the data tables.
--
-- @param i_param_src id of the client that is the source of this request
--
CREATE OR REPLACE FUNCTION littleware.synchCache( INTEGER )
    RETURNS SETOF littleware.asset AS $FUNC$
    DECLARE
       i_param_src        ALIAS FOR $1;
       rec_info            littleware.asset%ROWTYPE;
    BEGIN
       FOR rec_info IN SELECT
				a.s_id                ,
				a.s_name              ,
				a.s_id_home           ,
				a.l_last_transaction  , 
				a.s_pk_type           ,
				a.s_id_creator        ,
				a.s_id_updater        ,
				a.s_id_owner          ,
				a.f_value             ,
				a.s_id_acl            ,
				a.s_comment           ,
				a.s_last_change       ,
				a.s_data              ,
				a.s_id_from           ,
				a.s_id_to             ,
				a.t_created           ,
				a.t_updated           ,
				a.t_last_accessed     ,
				a.t_start             ,
				a.t_end   
            FROM littleware.asset a, littleware.client_cache c
            WHERE c.i_src=i_param_src 
                  AND c.i_status=1 
                  AND c.s_asset_id=a.s_id
        LOOP
            RETURN NEXT rec_info;
        END LOOP;
        FOR rec_info IN SELECT 
				s_asset_id                ,
				NULL              ,
				NULL           ,
				NULL  , 
				NULL           ,
				NULL        ,
				NULL        ,
				NULL          ,
				NULL             ,
				NULL            ,
				NULL           ,
				NULL       ,
				NULL              ,
				NULL           ,
				NULL             ,
				NULL           ,
				NULL           ,
				NULL     ,
				NULL             ,
				NULL   
            FROM littleware.client_cache 
            WHERE i_src=i_param_src 
                  AND i_status=2
        LOOP
            RETURN NEXT rec_info;
        END LOOP;
        DELETE FROM littleware.client_cache WHERE i_src=i_param_src AND i_status=2;
        UPDATE littleware.client_cache SET i_status=0 WHERE i_src=i_param_src AND i_status > 0;
		
        RETURN;
    END; $FUNC$
LANGUAGE plpgsql;

GRANT EXECUTE ON FUNCTION littleware.synchCache( INTEGER ) TO littleware_user_group;



-- ............................................

                  
         
         
