--
-- Schema definition for littleware DERBY cache
--
-- ij> driver 'org.apache.derby.jdbc.EmbeddedDriver';
-- ij> connect 'jdbc:derby:/Users/pasquini/Code/littleware/ddl/derbycache/template';
-- 

CREATE SCHEMA littleware;

--
-- AssetCore subset of postgres asset table
--
CREATE TABLE littleware.asset_cache (	
	s_id                VARCHAR(32) PRIMARY KEY,
	s_name              VARCHAR(80),
	s_id_home           VARCHAR(32),
	l_last_transaction  BIGINT,
	s_pk_type           VARCHAR(32),
	s_id_creator        VARCHAR(32),
	s_id_updater        VARCHAR(32),
	s_id_owner          VARCHAR(32),
	f_value             NUMERIC(16,4), 
	s_id_acl            VARCHAR(32),
	s_id_from           VARCHAR(32),
	s_id_to             VARCHAR(32),
	t_created           TIMESTAMP,
	t_updated           TIMESTAMP,
	t_start             TIMESTAMP, 
	t_end               TIMESTAMP
);

CREATE INDEX asset_from_idx ON littleware.asset_cache ( s_id_from, s_id_to, s_id_home );
CREATE INDEX asset_to_idx ON littleware.asset_cache ( s_id_to, s_id_from, s_id_home );
CREATE INDEX asset_typename_idx ON littleware.asset_cache ( s_pk_type, s_name );
CREATE INDEX asset_transaction_idx ON littleware.asset_cache ( l_last_transaction );

--
-- Setup a table to maintiain type-hierarchy data.
--
CREATE TABLE littleware.x_asset_type_tree (
         s_ancestor_id       VARCHAR(32) NOT NULL,
         s_descendent_id     VARCHAR(32) NOT NULL,
         PRIMARY KEY (s_ancestor_id, s_descendent_id)
         );
