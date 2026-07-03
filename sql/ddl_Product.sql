CREATE TABLE sys_product
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    product_name  VARCHAR(100)          NOT NULL,
    product_code  VARCHAR(50)           NULL,
    category      VARCHAR(50)           NULL,
    price         DECIMAL(10, 2)        NULL,
    stock         INT                   NULL,
    `description` VARCHAR(500)          NULL,
    image_url     VARCHAR(255)          NULL,
    status        INT                   NOT NULL,
    create_by     BIGINT                NOT NULL,
    create_time   datetime              NOT NULL,
    update_by     BIGINT                NULL,
    update_time   datetime              NULL,
    remark        VARCHAR(500)          NULL,
    CONSTRAINT pk_sys_product PRIMARY KEY (id)
);