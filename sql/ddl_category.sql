create table sys_category
(
    id            bigint auto_increment
        primary key,
    category_name varchar(100)      not null comment '分类名称',
    category_code varchar(100)      not null comment '分类编码',
    sort          int     default 0 null comment '排序',
    status        tinyint default 1 null comment '状态 0禁用 1启用',
    parent_id     bigint            null comment '父级分类ID',
    remark        varchar(500)      null comment '备注',
    create_by     bigint            null comment '创建人',
    create_time   datetime          null comment '创建时间',
    update_by     bigint            null comment '更新人',
    update_time   datetime          null comment '更新时间',
    constraint category_code
        unique (category_code)
)
    comment '商品分类表';