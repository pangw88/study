drop table link_recipe_collect;
create table link_recipe_collect(
	user_id varchar(128) not null, 
	recipe_id varchar(128) not null, 
	create_time date not null 
);

drop table link_recipe_comment;
create table link_recipe_comment(
	user_id varchar(128) not null, 
	recipe_id varchar(128) not null, 
	comment_content varchar(1024) not null, 
	create_time date not null, 
	update_time timestamp default current_timestamp 
);

drop table link_recipe_grade;
create table link_recipe_grade(
	user_id varchar(128) not null, 
	recipe_id varchar(128) not null, 
	recipe_grade float not null, 
	create_time date not null, 
	update_time timestamp default current_timestamp 
);

drop table link_note_comment;
create table link_note_comment(
	user_id varchar(128) not null, 
	note_id varchar(128) not null, 
	comment_content varchar(1024) not null, 
	create_time date not null, 
	update_time timestamp default current_timestamp 
);

drop table link_user_focus;
create table link_user_focus(
	user_id varchar(128) not null, 
	focus_user_id varchar(128) not null, 
	create_time date not null 
);

drop table link_user_device;
create table link_user_device (
    user_id varchar(128) not null, 
    device_id varchar(128) not null, 
    active_time date not null, 
    online integer not null default 0, 
    last_on_line_time date not null, 
    last_off_line_time timestamp default current_timestamp, 
    state integer not null default 0 
);

drop table link_user_mc;
create table link_user_mc (
    user_id varchar(128) not null, 
    device_id varchar(128) not null, 
    mc_id varchar(128) not null, 
	mc_alias varchar(256), 
    active_time timestamp not null, 
    state integer not null default 0, 
	reserve1 varchar(2048), 
	reserve2 varchar(2048), 
	reserve3 varchar(2048), 
	reserve4 varchar(2048) 
);

drop table mst_config;
create table mst_config(
	conf_key varchar(128) not null,
	conf_value varchar(256), 
	description varchar(256),
	create_time DATE NOT NULL,
	update_time TIMESTAMP NOT NULL DEFAULT current_timestamp
);

drop table mst_entity;
create table mst_entity(
	id int not null generated always as identity,
	site varchar(256) not null,
	name varchar(256), 
	code varchar(256),
	app_code varchar(256), 
	pay_code varchar(256), 
	sec_code varchar(256), 
	mes_code varchar(256), 
	comp_code varchar(256), 
	priority varchar(256), 
	email varchar(256), 
	contact varchar(256), 
	description varchar(256),
	create_time DATE NOT NULL,
	update_time TIMESTAMP NOT NULL DEFAULT current_timestamp
);

drop table mst_contact;
create table mst_contact(
	name varchar(256) not null,
	phone_num varchar(256), 
	extra_num1 varchar(256),
	extra_num2 varchar(256),
	extra_num3 varchar(256),
	birthday varchar(256), 
	address varchar(256), 
	zip_code varchar(256), 
	company varchar(256), 
	email varchar(256), 
	description varchar(256),
	create_time DATE NOT NULL,
	update_time TIMESTAMP NOT NULL DEFAULT current_timestamp
);

drop table mst_custom_user;
create table mst_custom_user (
    user_id varchar(128) not null, 
    user_name varchar(256) not null, 
    user_password varchar(256), 
    nick_name varchar(256), 
    privilege varchar(256), 
    gender varchar(64), 
    age varchar(32), 
    city varchar(256), 
    job varchar(256), 
    active_time date not null, 
    last_online_time date not null, 
    last_modified_time timestamp default current_timestamp, 
    state integer not null default 0 
);

drop table mst_device;
create table mst_device (
    device_id varchar(128) not null, 
    device_name varchar(256), 
    device_type integer default 0, 
    brand varchar(256), 
    connect_type varchar(32), 
    active_time date not null, 
    last_online_time date not null, 
    last_modified_time timestamp default current_timestamp, 
    state integer not null default 0 
);

drop table mst_mc_brand;
create table mst_mc_brand (
    brand_id varchar(128) not null, 
    brand_name varchar(256), 
    create_time date not null, 
    last_modified_time timestamp default current_timestamp, 
    state integer not null default 0 
);

drop table mst_mc_product;
create table mst_mc_product (
    product_id varchar(128) not null, 
    brand_id varchar(128) not null, 
    category_id varchar(128) not null, 
    product_name varchar(256), 
    tx_type varchar(32), 
    code varchar(128), 
    card varchar(128), 
    model varchar(128), 
    description varchar(512), 
    create_time date not null, 
    last_modified_time timestamp default current_timestamp, 
    state integer not null default 0 
);

drop table mst_mc_product_model;
create table mst_mc_product_model (
    model_id varchar(128) not null, 
    product_id varchar(128) not null, 
    model_name varchar(256) not null, 
	model_adapter varchar(256) , 
    create_time date not null, 
    online_time date not null, 
    last_modified_time timestamp default current_timestamp, 
    state integer not null default 0 
);

drop table mst_mc;
create table mst_mc (
    mc_id varchar(128) not null, 
    mc_name varchar(256), 
    model_id varchar(128) not null, 
    mc_mac varchar(256), 
    mc_icon varchar(256), 
    register_ip varchar(256), 
    register_address varchar(256), 
    last_login_ip varchar(256), 
    last_login_address varchar(256), 
    brand varchar(256), 
    active_time timestamp, 
    state integer not null default 0 
);

drop table mst_mc_status_record;
create table mst_mc_status_record (
    mc_id varchar(128) not null, 
    last_status varchar(2048), 
    last_report_time date, 
	last_heart_time date, 
	push_flag int default 0, 
	push_change_time date, 
	serialport_flag int default 0, 
	serialport_change_time date 
);
