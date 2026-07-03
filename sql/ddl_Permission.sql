CREATE TABLE sys_permission
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    permission_name VARCHAR(50)           NOT NULL,
    permission_key  VARCHAR(100)          NOT NULL,
    resource_type   VARCHAR(50)           NULL,
    `path`          VARCHAR(100)          NULL,
    `component`     VARCHAR(100)          NULL,
    parent_id       BIGINT                NULL,
    order_num       INT                   NOT NULL,
    status          INT                   NOT NULL,
    create_by       BIGINT                NOT NULL,
    create_time     datetime              NOT NULL,
    update_by       BIGINT                NULL,
    update_time     datetime              NULL,
    remark          VARCHAR(500)          NULL,
    CONSTRAINT pk_sys_permission PRIMARY KEY (id)
);