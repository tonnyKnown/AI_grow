CREATE TABLE sys_user_role
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    user_id     BIGINT                NOT NULL,
    role_id     BIGINT                NOT NULL,
    create_by   BIGINT                NOT NULL,
    create_time datetime              NOT NULL,
    CONSTRAINT pk_sys_user_role PRIMARY KEY (id)
);