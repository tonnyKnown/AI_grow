CREATE TABLE sys_user
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    username    VARCHAR(50)           NOT NULL,
    password    VARCHAR(255)          NOT NULL,
    email       VARCHAR(100)          NULL,
    phone       VARCHAR(20)           NULL,
    real_name   VARCHAR(50)           NULL,
    avatar      VARCHAR(255)          NULL,
    status      INT                   NOT NULL,
    create_by   BIGINT                NOT NULL,
    create_time datetime              NOT NULL,
    update_by   BIGINT                NULL,
    update_time datetime              NULL,
    remark      VARCHAR(500)          NULL,
    CONSTRAINT pk_sys_user PRIMARY KEY (id)
);

ALTER TABLE sys_user
    ADD CONSTRAINT uc_sys_user_username UNIQUE (username);