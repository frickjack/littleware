
-- ..................................................


SELECT  littleware.saveAsset ( 'littleware.home', littleware.getTypeId( 'littleware.home' ), NULL, NULL,
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'root home asset', 'no comment',
       'BD46E5588F9D4F41A6310100FE68DCB4',
        NULL, NULL, 'BD46E5588F9D4F41A6310100FE68DCB4',
       '00000000000000000000000000000000', NULL, 0
       );
	   
SELECT  littleware.saveAsset ( 'littleware.web_home', littleware.getTypeId( 'littleware.home' ), NULL, NULL,
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'root home asset', 'no comment',
       'F3C04056E9F24F7BB236947F246DE8D3',
        NULL, NULL, 'F3C04056E9F24F7BB236947F246DE8D3',
       '00000000000000000000000000000000', NULL, 0
       );	   


SELECT  littleware.saveAsset ( 'littleware.test_home', littleware.getTypeId( 'littleware.home' ), 
        'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'home for test assets', 'set owner to littleware.test_user',
        'D589EABED8EA43C1890DBF3CF1F9689A',
        NULL, NULL, 'D589EABED8EA43C1890DBF3CF1F9689A',
       '7AC5D21049254265B224B7512EFCF0D1', NULL, 0      
     );
	   
SELECT  littleware.saveAsset ( 'littleware.test_asset', littleware.getTypeId( 'littleware.generic' ), 
        'D589EABED8EA43C1890DBF3CF1F9689A', NULL,
       '00000000000000000000000000000000',
        'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'asset to test deleteAsset against', 'no comment',
       'D589EABED8EA43C1890DBF3CF1F9689A',
        NULL, NULL, '09D46DAC73C24F00A1A1644523FF1382',
       '00000000000000000000000000000000', NULL, 0
       );

SELECT littleware.deleteAsset ( '09D46DAC73C24F00A1A1644523FF1382', 'cleanup test', 0 );


SELECT  littleware.saveAsset ( 'littleware.administrator', littleware.getTypeId( 'littleware.user' ), 
        'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'master administrator user', 'no comment',
       'BD46E5588F9D4F41A6310100FE68DCB4',
        NULL, NULL, '00000000000000000000000000000000',
       '00000000000000000000000000000000', NULL, 0
       );
	   
SELECT  littleware.saveAsset ( 'littleware.test_user', littleware.getTypeId( 'littleware.user' ), 
        'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'test-case runner user', 'no comment',
       'BD46E5588F9D4F41A6310100FE68DCB4',
        NULL, NULL, '7AC5D21049254265B224B7512EFCF0D1',
       '7AC5D21049254265B224B7512EFCF0D1', NULL, 0
       );
	   
SELECT  littleware.saveAsset ( 'guest', littleware.getTypeId( 'littleware.user' ), 
        'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'guest user', 'no comment',
       'BD46E5588F9D4F41A6310100FE68DCB4',
        NULL, NULL, 'F5F4AFD9F84D4CFFBFDEA81F39F06701',
       'F5F4AFD9F84D4CFFBFDEA81F39F06701', NULL, 0
       );

SELECT  littleware.saveAsset ( 'web.admin', littleware.getTypeId( 'littleware.user' ), 
        'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'web.admin user', 'no comment',
       'F3C04056E9F24F7BB236947F246DE8D3',
        NULL, NULL, 'C58716683A9E4AC0A1CE13E4FB1060C2',
       'F5F4AFD9F84D4CFFBFDEA81F39F06701', NULL, 0
       );

	   
SELECT  littleware.saveAsset ( 'group.littleware.administrator', littleware.getTypeId( 'littleware.group' ), 
        'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'master admin group', 'no comment',
       'BD46E5588F9D4F41A6310100FE68DCB4',
        NULL, NULL, '89A1CB79B5944447BED9F38D398A7D12',
       '00000000000000000000000000000000', NULL, 0
       );

SELECT  littleware.saveAsset ( 'group.littleware.everybody', littleware.getTypeId( 'littleware.group' ), 
        'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'global everybody group', 'no comment',
       'BD46E5588F9D4F41A6310100FE68DCB4',
        NULL, NULL, 'D701C9B3C9B7453299E89A0161DDC242',
       '00000000000000000000000000000000', NULL, 0
       );
	   
SELECT  littleware.saveAsset ( 'group.littleware.test_user', littleware.getTypeId( 'littleware.group' ), 
        'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'test group', 'no comment',
       'BD46E5588F9D4F41A6310100FE68DCB4',
        NULL, NULL, 'C4574D74BF2647CC91F60D1396CA6315',
       '7AC5D21049254265B224B7512EFCF0D1', NULL, 0
       );

SELECT  littleware.saveAsset ( 'group.littleware.web', littleware.getTypeId( 'littleware.group' ), 
        'F3C04056E9F24F7BB236947F246DE8D3', NULL,
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'web-registered everybody group', 'no comment',
       'F3C04056E9F24F7BB236947F246DE8D3',
        NULL, NULL, '688D1B51F06043E5B93CDA61593B778B',
       '00000000000000000000000000000000', NULL, 0
       );
       

-- admin in admin group	  
SELECT  littleware.saveAsset ( 'administrator', littleware.getTypeId( 'littleware.group_member' ), 
        '89A1CB79B5944447BED9F38D398A7D12', '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'admin is admin group member', 'no comment',
       'BD46E5588F9D4F41A6310100FE68DCB4',
        NULL, NULL, 'B6ECCDEF7A8D4452AAC52716FA380795',
       '00000000000000000000000000000000', NULL, 0
       );
	   
SELECT  littleware.saveAsset ( 'web.admin', littleware.getTypeId( 'littleware.group_member' ), 
        '89A1CB79B5944447BED9F38D398A7D12', 'C58716683A9E4AC0A1CE13E4FB1060C2',
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'web.admin is admin group member', 'no comment',
       'BD46E5588F9D4F41A6310100FE68DCB4',
        NULL, NULL, '2FD845D6FEAE440CB8229331598C334D',
       '00000000000000000000000000000000', NULL, 0
       );
	   
-- admin in everybody group	  
SELECT  littleware.saveAsset ( 'administrator', littleware.getTypeId( 'littleware.group_member' ), 
        'D701C9B3C9B7453299E89A0161DDC242', '00000000000000000000000000000000',
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'test_user is group member', 'no comment',
       'BD46E5588F9D4F41A6310100FE68DCB4',
        NULL, NULL, 'CA9FFC8762674DB8AF28DD8955B07D98',
       '00000000000000000000000000000000', NULL, 0
       );

-- web.admin in everybody group	   
SELECT  littleware.saveAsset ( 'web.admin', littleware.getTypeId( 'littleware.group_member' ), 
        'D701C9B3C9B7453299E89A0161DDC242', 'C58716683A9E4AC0A1CE13E4FB1060C2',
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'web.admin is group member', 'no comment',
       'BD46E5588F9D4F41A6310100FE68DCB4',
        NULL, NULL, '70160C0AF24F402091A2CD19EDED8E8C',
       '00000000000000000000000000000000', NULL, 0
       );
	   

-- test_user in everybody group	   
SELECT  littleware.saveAsset ( 'test_user', littleware.getTypeId( 'littleware.group_member' ), 
        'D701C9B3C9B7453299E89A0161DDC242', '7AC5D21049254265B224B7512EFCF0D1',
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'admin is group member', 'no comment',
       'BD46E5588F9D4F41A6310100FE68DCB4',
        NULL, NULL, '6EDBC32311B943ECAE7A572F0FC2BB91',
       '00000000000000000000000000000000', NULL, 0
       );	   

-- guest in everybody group	   
SELECT  littleware.saveAsset ( 'guest', littleware.getTypeId( 'littleware.group_member' ), 
        'D701C9B3C9B7453299E89A0161DDC242', 'F5F4AFD9F84D4CFFBFDEA81F39F06701',
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'guest is group member', 'no comment',
       'BD46E5588F9D4F41A6310100FE68DCB4',
        NULL, NULL, '98E29E67BE4E4B63BA77486CA031FF0B',
       '00000000000000000000000000000000', NULL, 0
       );	   

-- web.admin in group.littleware.web
SELECT  littleware.saveAsset ( 'web.admin', littleware.getTypeId( 'littleware.group_member' ), 
        '688D1B51F06043E5B93CDA61593B778B', 'C58716683A9E4AC0A1CE13E4FB1060C2',
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'web.admin is group member', 'no comment',
       'F3C04056E9F24F7BB236947F246DE8D3',
        NULL, NULL, 'D734938B25B64E12BE1523126336D6BD',
       '00000000000000000000000000000000', NULL, 0
       );
	   
-- acl
SELECT  littleware.saveAsset ( 'acl.littleware.everybody.read', littleware.getTypeId( 'littleware.acl' ), 
        'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       '00000000000000000000000000000000',
        'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'everybody read ACL', 'no comment', 
       'BD46E5588F9D4F41A6310100FE68DCB4',
        NULL, NULL, 'F4CEDAA07B574FFFA27E0BA87078DC34',
       '00000000000000000000000000000000', NULL, 0
       );
	   
SELECT  littleware.saveAsset ( 'acl.littleware.web.read', littleware.getTypeId( 'littleware.acl' ), 
        'F3C04056E9F24F7BB236947F246DE8D3', NULL,
       '00000000000000000000000000000000',
        'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'web read ACL', 'no comment', 
       'F3C04056E9F24F7BB236947F246DE8D3',
        NULL, NULL, '8AA9C948545B4ECD990947D37F8C2AD7',
       '00000000000000000000000000000000', NULL, 0
       );
	   
	   
SELECT  littleware.saveAsset ( 'read.group.littleware.everybody', littleware.getTypeId( 'littleware.acl_entry' ), 
        'F4CEDAA07B574FFFA27E0BA87078DC34', 'D701C9B3C9B7453299E89A0161DDC242',
       '00000000000000000000000000000000',
        'F4CEDAA07B574FFFA27E0BA87078DC34',
		'<acl:permlist xmlns:acl="http://www.littleware.com/xml/namespace/2006/acl"><acl:perm>' 
                || littleware.getPermissionId( 'read' )
				|| '</acl:perm></acl:permlist>'
				, 
	    1, 'everybody group read permission', 'new ACL entry',
       'BD46E5588F9D4F41A6310100FE68DCB4',
        NULL, NULL, 'A20FF0A0829A433FBA7CF90B128F4FDF',
       '00000000000000000000000000000000', NULL, 0
       );
	   
SELECT  littleware.saveAsset ( 'read.group.littleware.web', littleware.getTypeId( 'littleware.acl_entry' ), 
        '8AA9C948545B4ECD990947D37F8C2AD7', '688D1B51F06043E5B93CDA61593B778B',
       '00000000000000000000000000000000',
        'F4CEDAA07B574FFFA27E0BA87078DC34',
		'<acl:permlist xmlns:acl="http://www.littleware.com/xml/namespace/2006/acl"><acl:perm>' 
                || littleware.getPermissionId( 'read' )
				|| '</acl:perm></acl:permlist>'
				, 
	    1, 'web group read permission', 'new ACL entry',
       'F3C04056E9F24F7BB236947F246DE8D3',
        NULL, NULL, '1E20529A510B4E9794B04C4B4E81AE50',
       '00000000000000000000000000000000', NULL, 0
       );	   

-- test acl
SELECT  littleware.saveAsset ( 'acl.littleware.test_acl', littleware.getTypeId( 'littleware.acl' ), 
        'D589EABED8EA43C1890DBF3CF1F9689A', NULL,
       '7AC5D21049254265B224B7512EFCF0D1',
        'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'everybody read ACL', 'no comment', 
       'D589EABED8EA43C1890DBF3CF1F9689A',
        NULL, NULL, 'F26CAA98EBCA44BD8AF38F81CB066277',
       '7AC5D21049254265B224B7512EFCF0D1', NULL, 0
       );

-- service-type entries
SELECT  littleware.saveAsset ( 'littleware.service_home', littleware.getTypeId( 'littleware.home' ), 
        'BD46E5588F9D4F41A6310100FE68DCB4', NULL,
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'home for service-type assets', 'no comment',
       'BD46E5588F9D4F41A6310100FE68DCB4',
        NULL, NULL, 'EDACB6E64AD54169AC72A2490D06E6B6',
       '00000000000000000000000000000000', NULL, 0
       );

SELECT  littleware.saveAsset ( 'littleware.asset_manager_service',
        littleware.getTypeId( 'littleware.service' ),
        'EDACB6E64AD54169AC72A2490D06E6B6', NULL,
       '00000000000000000000000000000000',
        'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'access controller for AssetManager service', 'everybody has access',
       'EDACB6E64AD54169AC72A2490D06E6B6',
        NULL, NULL, 'FD4C5F5B4C904AC6BDC9ECA891C39543',
       '00000000000000000000000000000000', NULL, 0
       );
	   
SELECT  littleware.saveAsset ( 'littleware.asset_search_service', littleware.getTypeId( 'littleware.service' ), 
        'EDACB6E64AD54169AC72A2490D06E6B6', NULL,
       '00000000000000000000000000000000',
        'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'access controller for AssetSearchManager service', 'everybody has access',
       'EDACB6E64AD54169AC72A2490D06E6B6',
        NULL, NULL, '56A05693C0874780A716DEFA4E262F6F',
       '00000000000000000000000000000000', NULL, 0
       );
	
SELECT  littleware.saveAsset ( 'littleware.acl_manager_service', littleware.getTypeId( 'littleware.service' ), 
        'EDACB6E64AD54169AC72A2490D06E6B6', NULL,
       '00000000000000000000000000000000',
        'F4CEDAA07B574FFFA27E0BA87078DC34',
       'no data', 0, 'access controller for AclManager service', 'everybody has access',
       'EDACB6E64AD54169AC72A2490D06E6B6',
        NULL, NULL, '25A9379640B94B26BBA6D0607981B070',
       '00000000000000000000000000000000', NULL, 0
       );

-- quota setup
-- <quota:quotaset xmlns:quota="http://www.littleware.com/xml/namespace/2006/quota"><quota:quotaspec type="update" limit="100" /></quota:quotaset>
		
SELECT  littleware.saveAsset ( 'littleware_quota', littleware.getTypeId( 'littleware.quota' ), 
        '7AC5D21049254265B224B7512EFCF0D1', NULL,
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
       '<quota:quotaset xmlns:quota="http://www.littleware.com/xml/namespace/2006/quota">'
	       || '<quota:quotaspec type="update" limit="100" /></quota:quotaset>', 
	    0, 'test_user quota', 'no comment',
       'BD46E5588F9D4F41A6310100FE68DCB4',
        null, null, 
		'81CA9F7D3A024138B131869C61B34012',
       '00000000000000000000000000000000', NULL, 0
       );
	   
SELECT  littleware.saveAsset ( 'littleware_quota', littleware.getTypeId( 'littleware.quota' ), 
        'F5F4AFD9F84D4CFFBFDEA81F39F06701', NULL,
       '00000000000000000000000000000000',
       'F4CEDAA07B574FFFA27E0BA87078DC34',
	   '<quota:quotaset xmlns:quota="http://www.littleware.com/xml/namespace/2006/quota">'
	      || '<quota:quotaspec type="update" limit="100" /></quota:quotaset>', 
	   0, 'guest quota', 'no comment',
       'BD46E5588F9D4F41A6310100FE68DCB4',
        null, null,
		'859E435E15374C4B93F14F8E69DD2B30',
       '00000000000000000000000000000000', NULL, 0
       );
	   
UPDATE littleware.asset SET t_start=now (), t_end=now() + interval '1 days'
   WHERE s_id 
      IN ('859E435E15374C4B93F14F8E69DD2B30', '81CA9F7D3A024138B131869C61B34012' );
	  
	   	   
-- password setup
-- admin
SELECT littleware.savePassword ( '00000000000000000000000000000000', 'XXXXXXXXXXXXXX' );
-- test_user
SELECT littleware.savePassword ( '7AC5D21049254265B224B7512EFCF0D1', 'test123' );
-- guest
SELECT littleware.savePassword ( 'F5F4AFD9F84D4CFFBFDEA81F39F06701', 'XXXXXXXXXXXXX' );
-- web.admin
SELECT littleware.savePassword ( 'C58716683A9E4AC0A1CE13E4FB1060C2', 'XXXXXXXXXXXXX' );

-- Clear sync-cache
SELECT littleware.clearCache ( 0 );


