CREATE DATABASE IF NOT EXISTS `xxl_job` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `xxl_job`;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `xxl_job_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `job_group` int(11) NOT NULL COMMENT 'Executor group id',
  `job_desc` varchar(255) NOT NULL,
  `add_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `author` varchar(64) DEFAULT NULL COMMENT 'Author',
  `alarm_email` varchar(255) DEFAULT NULL COMMENT 'Alarm email',
  `schedule_type` varchar(50) NOT NULL DEFAULT 'NONE' COMMENT 'Schedule type',
  `schedule_conf` varchar(128) DEFAULT NULL COMMENT 'Schedule config',
  `misfire_strategy` varchar(50) NOT NULL DEFAULT 'DO_NOTHING' COMMENT 'Misfire strategy',
  `executor_route_strategy` varchar(50) DEFAULT NULL COMMENT 'Route strategy',
  `executor_handler` varchar(255) DEFAULT NULL COMMENT 'Job handler',
  `executor_param` varchar(512) DEFAULT NULL COMMENT 'Job param',
  `executor_block_strategy` varchar(50) DEFAULT NULL COMMENT 'Block strategy',
  `executor_timeout` int(11) NOT NULL DEFAULT '0' COMMENT 'Timeout seconds',
  `executor_fail_retry_count` int(11) NOT NULL DEFAULT '0' COMMENT 'Fail retry count',
  `glue_type` varchar(50) NOT NULL COMMENT 'Glue type',
  `glue_source` mediumtext COMMENT 'Glue source',
  `glue_remark` varchar(128) DEFAULT NULL COMMENT 'Glue remark',
  `glue_updatetime` datetime DEFAULT NULL COMMENT 'Glue update time',
  `child_jobid` varchar(255) DEFAULT NULL COMMENT 'Child job ids',
  `trigger_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT 'Trigger status',
  `trigger_last_time` bigint(13) NOT NULL DEFAULT '0' COMMENT 'Last trigger time',
  `trigger_next_time` bigint(13) NOT NULL DEFAULT '0' COMMENT 'Next trigger time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `xxl_job_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `job_group` int(11) NOT NULL COMMENT 'Executor group id',
  `job_id` int(11) NOT NULL COMMENT 'Job id',
  `executor_address` varchar(255) DEFAULT NULL COMMENT 'Executor address',
  `executor_handler` varchar(255) DEFAULT NULL COMMENT 'Job handler',
  `executor_param` varchar(512) DEFAULT NULL COMMENT 'Job param',
  `executor_sharding_param` varchar(20) DEFAULT NULL COMMENT 'Sharding param',
  `executor_fail_retry_count` int(11) NOT NULL DEFAULT '0' COMMENT 'Fail retry count',
  `trigger_time` datetime DEFAULT NULL COMMENT 'Trigger time',
  `trigger_code` int(11) NOT NULL COMMENT 'Trigger result code',
  `trigger_msg` text COMMENT 'Trigger message',
  `handle_time` datetime DEFAULT NULL COMMENT 'Handle time',
  `handle_code` int(11) NOT NULL COMMENT 'Handle result code',
  `handle_msg` text COMMENT 'Handle message',
  `alarm_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT 'Alarm status',
  PRIMARY KEY (`id`),
  KEY `I_trigger_time` (`trigger_time`),
  KEY `I_handle_code` (`handle_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `xxl_job_log_report` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `trigger_day` datetime DEFAULT NULL COMMENT 'Trigger day',
  `running_count` int(11) NOT NULL DEFAULT '0' COMMENT 'Running count',
  `suc_count` int(11) NOT NULL DEFAULT '0' COMMENT 'Success count',
  `fail_count` int(11) NOT NULL DEFAULT '0' COMMENT 'Fail count',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `i_trigger_day` (`trigger_day`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `xxl_job_logglue` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `job_id` int(11) NOT NULL COMMENT 'Job id',
  `glue_type` varchar(50) DEFAULT NULL COMMENT 'Glue type',
  `glue_source` mediumtext COMMENT 'Glue source',
  `glue_remark` varchar(128) NOT NULL COMMENT 'Glue remark',
  `add_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `xxl_job_registry` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `registry_group` varchar(50) NOT NULL,
  `registry_key` varchar(255) NOT NULL,
  `registry_value` varchar(255) NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `i_g_k_v` (`registry_group`,`registry_key`,`registry_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `xxl_job_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `app_name` varchar(64) NOT NULL COMMENT 'Executor app name',
  `title` varchar(12) NOT NULL COMMENT 'Executor title',
  `address_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT 'Address type',
  `address_list` text COMMENT 'Address list',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `xxl_job_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT 'Username',
  `password` varchar(50) NOT NULL COMMENT 'Password',
  `role` tinyint(4) NOT NULL COMMENT 'Role',
  `permission` varchar(255) DEFAULT NULL COMMENT 'Permission',
  PRIMARY KEY (`id`),
  UNIQUE KEY `i_username` (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `xxl_job_lock` (
  `lock_name` varchar(50) NOT NULL COMMENT 'Lock name',
  PRIMARY KEY (`lock_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO `xxl_job_group`(`id`, `app_name`, `title`, `address_type`, `address_list`, `update_time`)
VALUES (1, 'business-service', '业务服务', 0, NULL, NOW());

INSERT IGNORE INTO `xxl_job_info`(`id`, `job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`, `executor_block_strategy`, `executor_timeout`, `executor_fail_retry_count`, `glue_type`, `glue_source`, `glue_remark`, `glue_updatetime`, `child_jobid`)
VALUES (1, 1, '测试任务1', NOW(), NOW(), 'XXL', '', 'CRON', '0 0 0 * * ? *', 'DO_NOTHING', 'FIRST', 'demoJobHandler', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE代码初始化', NOW(), '');

INSERT IGNORE INTO `xxl_job_user`(`id`, `username`, `password`, `role`, `permission`)
VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', 1, NULL);

INSERT IGNORE INTO `xxl_job_lock` (`lock_name`) VALUES ('schedule_lock');
