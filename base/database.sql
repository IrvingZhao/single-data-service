drop table if exists single_data_info;
drop table if exists single_data_config;

/*==============================================================*/
/* Table: single_data_config                                    */
/*==============================================================*/
create table single_data_config
(
    id                   bigint not null,
    data_id              bigint comment '数据id',
    keyword              varchar(50) comment '配置key',
    value                varchar(255) comment 'value',
    primary key (id)
);

alter table single_data_config comment '单数据信息配置';

/*==============================================================*/
/* Index: i_data_id                                             */
/*==============================================================*/
create index i_data_id on single_data_config
    (
     data_id
        );

/*==============================================================*/
/* Table: single_data_info                                      */
/*==============================================================*/
create table single_data_info
(
    id                   bigint not null auto_increment,
    keyword              varchar(20) comment '唯一kei',
    type                 char(20) comment '类型',
    data                 text comment '数据',
    d_flag               char(1) comment '删除标志',
    primary key (id)
);

alter table single_data_info comment '单数据信息';