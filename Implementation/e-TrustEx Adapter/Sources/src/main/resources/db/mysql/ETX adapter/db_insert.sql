insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('as2adapter_etrustex_endpoint', 'http://localhost:8080/eprior/services');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('as2adapter_etrustex_login', 'AS2ADAPTER');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('as2adapter_etrustex_password', 'AS2ADAPTER');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('STORE_BINARY_WEBSERVICE_USR', 'AS2ADAPTER');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('STORE_BINARY_WEBSERVICE_PWD', 'AS2ADAPTER');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('URL_STORE_BINARY_WEBSERIVCE', 'http://localhost:8080/etrustex/wrapperservices');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('CHUNK_SIZE', '10000');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('ALREADY_EXIST_ERROR', 'error.duplicate');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('EANALLSUPPLIERS', 'EANALLSUPPLIERS');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('URL_VIEW_WEBSERIVCE', 'http://localhost:8080/etrustex/services');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('VIEW_WEBSERVICE_USR', 'AS2ADAPTER');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('VIEW_WEBSERVICE_PWD', 'AS2ADAPTER');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('URL_BUNDLE_WEBSERIVCE', 'http://localhost:8080/etrustex/services');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('BUNDLE_WEBSERVICE_USR', 'AS2ADAPTER');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('BUNDLE_WEBSERVICE_PWD', 'AS2ADAPTER');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('FILE_STORE_PATH', 'c:\\Pgm\\programs\\wildfly-10.1.0.Final-PEPPOL\\bin\\logs');

update etrustex.ETR_TB_METADATA set MD_VALUE="c:\\Pgm\\programs\\wildfly-10.1.0.Final-PEPPOL\\bin\\logs" where  MD_TYPE = 'FILE_STORE_PATH';



insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('as2adapter_etrustex_endpoint', 'http://158.167.241.132:1043/eprior/services');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('as2adapter_etrustex_login', 'PEPPOL_APP');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('as2adapter_etrustex_password', '!M+6($u*3z4&0rJ');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('STORE_BINARY_WEBSERVICE_USR', 'PEPPOL_APP');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('STORE_BINARY_WEBSERVICE_PWD', '!M+6($u*3z4&0rJ');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('URL_STORE_BINARY_WEBSERIVCE', 'http://158.167.241.132:1043/etrustex/wrapperservices');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('CHUNK_SIZE', '10000');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('ALREADY_EXIST_ERROR', 'error.duplicate');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('EANALLSUPPLIERS', 'EANALLSUPPLIERS');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('URL_VIEW_WEBSERIVCE', 'http://158.167.241.132:1043/etrustex/services');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('VIEW_WEBSERVICE_USR', 'PEPPOL_APP');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('VIEW_WEBSERVICE_PWD', '!M+6($u*3z4&0rJ');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('URL_BUNDLE_WEBSERIVCE', 'http://158.167.241.132:1043/etrustex/services');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('BUNDLE_WEBSERVICE_USR', 'PEPPOL_APP');
insert into peppol_adapter.OXA_ADAPTER_METADATA (MD_TYPE, MD_VALUE) VALUES ('BUNDLE_WEBSERVICE_PWD', '!M+6($u*3z4&0rJ');

as2adapter_etrustex_endpoint	http://localhost:8080/eprior/services
as2adapter_etrustex_login	AS2ADAPTER
as2adapter_etrustex_password	AS2ADAPTER
STORE_BINARY_WEBSERVICE_USR	AS2ADAPTER
STORE_BINARY_WEBSERVICE_PWD	AS2ADAPTER
URL_STORE_BINARY_WEBSERIVCE	http://localhost:8080/etrustex/wrapperservices
CHUNK_SIZE	10000
ALREADY_EXIST_ERROR	error.duplicate
EANALLSUPPLIERS	EANALLSUPPLIERS
URL_VIEW_WEBSERIVCE	http://localhost:8080/etrustex/services
VIEW_WEBSERVICE_USR	AS2ADAPTER
VIEW_WEBSERVICE_PWD	AS2ADAPTER
URL_BUNDLE_WEBSERIVCE	http://localhost:8080/etrustex/services
BUNDLE_WEBSERVICE_USR	AS2ADAPTER
BUNDLE_WEBSERVICE_PWD	AS2ADAPTER