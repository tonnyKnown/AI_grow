CREATE TABLE sys_role
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    role_name     VARCHAR(50)           NOT NULL,
    role_key      VARCHAR(100)          NOT NULL,
    `description` VARCHAR(255)          NULL,
    status        INT                   NOT NULL,
    create_by     BIGINT                NOT NULL,
    create_time   datetime              NOT NULL,
    update_by     BIGINT                NULL,
    update_time   datetime              NULL,
    remark        VARCHAR(500)          NULL,
    CONSTRAINT pk_sys_role PRIMARY KEY (id)
);