

-- ..................................................
-- littleware.home
INSERT INTO asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES ( 
       'BD46E5588F9D4F41A6310100FE68DCB4', 'littleware.home', 
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       getTypeId( 'littleware.home' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'littleware home', 'create home',
       '', NULL, NULL,
       now(), now(), now(),
       NULL, NULL
      );

-- .....
-- littleware.administrator
INSERT INTO asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       '00000000000000000000000000000000', 'littleware.administrator',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       getTypeId( 'littleware.user' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'root admin user', 'create admin',
       '', 'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       now(), now(), now(),
       NULL, NULL
      );


-- littleware.test_user
INSERT INTO asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       '7AC5D21049254265B224B7512EFCF0D1', 'littleware.test_user',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       getTypeId( 'littleware.user' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '7AC5D21049254265B224B7512EFCF0D1',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'test user', 'create test user',
       '', 'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       now(), now(), now(),
       NULL, NULL
      );

-- ......

INSERT INTO asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       'D589EABED8EA43C1890DBF3CF1F9689A', 'littleware.test_home',
       'D589EABED8EA43C1890DBF3CF1F9689A', 1,
       getTypeId( 'littleware.home' ),
       '7AC5D21049254265B224B7512EFCF0D1',
       '7AC5D21049254265B224B7512EFCF0D1',
       '7AC5D21049254265B224B7512EFCF0D1',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'test home', 'create test home',
       '', NULL, NULL,
       now(), now(), now(),
       NULL, NULL
      );

-- ......



-- ......

INSERT INTO asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       '89A1CB79B5944447BED9F38D398A7D12',
       'group.littleware.administrator',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       getTypeId( 'littleware.group' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'admin group', 'create admin group',
       '', 'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       now(), now(), now(),
       NULL, NULL
      );

-- ......

INSERT INTO asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       'D701C9B3C9B7453299E89A0161DDC242',
       'group.littleware.everybody',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       getTypeId( 'littleware.group' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'everybody group', 'create everybody group',
       '', 'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       now(), now(), now(),
       NULL, NULL
      );

-- ......

-- admin in admin group
INSERT INTO asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       'B6ECCDEF7A8D4452AAC52716FA380795',
       'administrator',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       getTypeId( 'littleware.group_member' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'admin in admin group', 'admin in admin group',
       '',
       '89A1CB79B5944447BED9F38D398A7D12', '00000000000000000000000000000000',
       now(), now(), now(),
       NULL, NULL
      );

-- admin in everybody group
INSERT INTO asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       'CA9FFC8762674DB8AF28DD8955B07D98',
       'administrator',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       getTypeId( 'littleware.group_member' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'admin in everybody group', 'admin in everybody group',
       '',
       'D701C9B3C9B7453299E89A0161DDC242', '00000000000000000000000000000000',
       now(), now(), now(),
       NULL, NULL
      );

	   

-- test_user in everybody group
INSERT INTO asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       '6EDBC32311B943ECAE7A572F0FC2BB91',
       'test_user',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       getTypeId( 'littleware.group_member' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'test_user in everybody group', 'test_user in everybody group',
       '',
       'D701C9B3C9B7453299E89A0161DDC242', '7AC5D21049254265B224B7512EFCF0D1',
       now(), now(), now(),
       NULL, NULL
      );

-- acl
INSERT INTO asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'acl.littleware.everybody.read',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       getTypeId( 'littleware.acl' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'everybody read acl', 'everybody read acl',
       '',
       'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       now(), now(), now(),
       NULL, NULL
      );


INSERT INTO asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       'A20FF0A0829A433FBA7CF90B128F4FDF',
       'group.littleware.everybody.positive',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       getTypeId( 'littleware.acl_entry' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       1.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'everybody read acl', 'everybody read acl',
		'<acl:permlist xmlns:acl="http://www.littleware.com/xml/namespace/2006/acl"><acl:perm>EEB72C11DE934015BE42FA6FA9423EAC</acl:perm></acl:permlist>'
				,
       'F4CEDAA07B574FFFA27E0BA87078DC34', 'D701C9B3C9B7453299E89A0161DDC242',
       now(), now(), now(),
       NULL, NULL
      );

INSERT INTO asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       'C871EA418BE44F0EB9E68B5950740CE7',
       'acl.littleware.everybody.write',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       (SELECT s_id FROM x_asset_type WHERE s_name='littleware.acl' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'C871EA418BE44F0EB9E68B5950740CE7',
       'everybody write acl', 'everybody write acl',
       '',
       'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       now(),now(),now(),
       NULL, NULL
      );



INSERT INTO asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       'B12E686E011246E085E5E6279361B40C',
       'group.littleware.everybody.positive',
       'BD46E5588F9D4F41A6310100FE68DCB4', 1,
       (SELECT s_id FROM x_asset_type WHERE s_name='littleware.acl_entry' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       1.0, 'C871EA418BE44F0EB9E68B5950740CE7',
       'everybody read acl', 'everybody read acl',
		'<acl:permlist xmlns:acl="http://www.littleware.com/xml/namespace/2006/acl"><acl:perm>EEB72C11DE934015BE42FA6FA9423EAC</acl:perm><acl:perm>55D1BF9F49234D839B56354BC2F2BA90</acl:perm></acl:permlist>'
				,
       'C871EA418BE44F0EB9E68B5950740CE7', 'D701C9B3C9B7453299E89A0161DDC242',
       now(),now(),now(),
       NULL, NULL
      );

	   

-- service-type entries
INSERT INTO asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       'EDACB6E64AD54169AC72A2490D06E6B6',
       'littleware.service_home',
       'EDACB6E64AD54169AC72A2490D06E6B6', 1,
       getTypeId( 'littleware.home' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'service home', 'create service home',
       '', NULL, NULL,
       now(), now(), now(),
       NULL, NULL
      );

INSERT INTO asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       'FD4C5F5B4C904AC6BDC9ECA891C39543',
       'asset_manager_service',
       'EDACB6E64AD54169AC72A2490D06E6B6', 1,
       getTypeId( 'littleware.service' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'service', 'create service',
       '', 'EDACB6E64AD54169AC72A2490D06E6B6', NULL,
       now(), now(), now(),
       NULL, NULL
      );

INSERT INTO asset (	s_id, s_name, s_id_home, l_last_transaction,
	s_pk_type, s_id_creator, s_id_updater, s_id_owner, f_value,
	s_id_acl, s_comment, s_last_change, s_data, s_id_from, s_id_to,
	t_created, t_updated, t_last_accessed,
	t_start, t_end
) VALUES (
       '56A05693C0874780A716DEFA4E262F6F',
       'asset_search_service',
       'EDACB6E64AD54169AC72A2490D06E6B6', 1,
       getTypeId( 'littleware.service' ),
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       0.0, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       'service', 'create service',
       '', 'EDACB6E64AD54169AC72A2490D06E6B6', NULL,
       now(), now(), now(),
       NULL, NULL
      );
	   
	