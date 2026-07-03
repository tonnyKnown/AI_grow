CREATE TABLE sys_role_permission
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    role_id       BIGINT                NOT NULL,
    permission_id BIGINT                NOT NULL,
    create_by     BIGINT                NOT NULL,
    create_time   datetime              NOT NULL,
    CONSTRAINT pk_sys_role_permission PRIMARY KEY (id)
);