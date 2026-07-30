CREATE DATABASE IF NOT EXISTS yolo_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE yolo_db;

-- 1. Life Profile Table
CREATE TABLE IF NOT EXISTS life_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key',
    user_id BIGINT NOT NULL DEFAULT 1 COMMENT 'Fixed User ID for MVP',
    birth_date DATE NOT NULL COMMENT 'Date of Birth',
    expected_life_years INT NOT NULL COMMENT 'Expected Life Span in Years',
    energy_life_years INT COMMENT 'Energy Life Span in Years',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation Time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Life Configuration';

-- 2. Goal Table
CREATE TABLE IF NOT EXISTS goal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key',
    user_id BIGINT NOT NULL DEFAULT 1 COMMENT 'Fixed User ID for MVP',
    title VARCHAR(100) NOT NULL COMMENT 'Goal Title',
    description TEXT COMMENT 'Goal Description',
    expected_total_hours INT COMMENT 'Expected Total Hours',
    north_star VARCHAR(255) COMMENT 'North Star Metric',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'Status: ACTIVE, COMPLETED, ARCHIVED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation Time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    UNIQUE KEY uk_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Goal Management';

-- 3. Focus Session Table
CREATE TABLE IF NOT EXISTS focus_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key',
    goal_id BIGINT NOT NULL COMMENT 'Associated Goal ID',
    start_time DATETIME NOT NULL COMMENT 'Start Time',
    end_time DATETIME DEFAULT NULL COMMENT 'End Time',
    status VARCHAR(20) NOT NULL COMMENT 'Status: RUNNING, COMPLETED',
    duration_minutes INT DEFAULT 0 COMMENT 'Duration in Minutes',
    memo TEXT COMMENT 'Session Memo',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation Time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    INDEX idx_goal_id (goal_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Focus Records';

-- 4. Milestone Table
CREATE TABLE IF NOT EXISTS milestone (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '自增ID',
    goal_id INT DEFAULT NULL COMMENT '目标ID',
    milestone_title VARCHAR(100) DEFAULT NULL COMMENT '里程碑标题',
    milestone_date DATE DEFAULT NULL COMMENT '里程碑日期',
    milestone_desc VARCHAR(255) DEFAULT NULL COMMENT '里程碑记录',
    own_feel VARCHAR(255) DEFAULT NULL COMMENT '心情记录',
    create_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_goal_id (goal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Goal Milestones';

-- 5. User Table
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(64) NOT NULL UNIQUE COMMENT 'WeChat OpenID',
    session_key VARCHAR(128) COMMENT 'WeChat Session Key',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation Time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    INDEX idx_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Information';

-- 6. Schedule Activity Type Table
CREATE TABLE `schedule_activity_type` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `type_code` VARCHAR(50) NOT NULL COMMENT '类型编码',
  `type_name` VARCHAR(50) NOT NULL COMMENT '类型名称',
  `color` VARCHAR(20) NOT NULL COMMENT '颜色',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `enable_flag` TINYINT DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_type_code` (`user_id`, `type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日程事项类型表';

-- 7. Schedule Record Table
CREATE TABLE `schedule_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `biz_date` date NOT NULL COMMENT '日期',
  `record_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'plan/actual',
  `start_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '开始时间',
  `end_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '结束时间',  
  `activity_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '活动类型',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_date_slot_type` (`user_id`,`biz_date`,`record_type`,`start_time`,`end_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=191 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='日程记录表';

-- 8. Todo Item Table
CREATE TABLE IF NOT EXISTS `todo_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `date_type` VARCHAR(20) NOT NULL COMMENT '日期类型: DAY, WEEK, MONTH, YEAR',
  `start_date` VARCHAR(20) NOT NULL COMMENT '开始日期',
  `end_date` VARCHAR(20) NOT NULL COMMENT '结束日期',
  `content` VARCHAR(500) NOT NULL COMMENT '待办事项',
  `priority` VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '优先级: HIGH, MEDIUM, LOW',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `is_completed` TINYINT DEFAULT 0 COMMENT '是否已完成: 0否, 1是',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user_date` (`user_id`, `date_type`, `start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='待办事项表';

-- 9. Do Not Do Item Table
CREATE TABLE IF NOT EXISTS `do_not_do_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `item_type` VARCHAR(20) NOT NULL COMMENT '类型: DO, NOT_DO',
  `content` VARCHAR(500) NOT NULL COMMENT '清单内容',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user_type` (`user_id`, `item_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='做/不做清单表';

-- 10. Goal and Activity Type Relation Table
CREATE TABLE IF NOT EXISTS `goal_activity_type_relation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `goal_id` BIGINT NOT NULL COMMENT '目标ID',
  `type_code` VARCHAR(50) NOT NULL COMMENT '活动类型Code',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_goal_type_code` (`user_id`, `goal_id`, `type_code`),
  INDEX `idx_goal_id` (`goal_id`),
  INDEX `idx_type_code` (`type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='目标与活动类型关联表';

-- 11. SOP Template Table
CREATE TABLE IF NOT EXISTS `sop_template` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(255) NOT NULL COMMENT '模版名称',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SOP模版表';

-- 12. SOP Category Table
CREATE TABLE IF NOT EXISTS `sop_category` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  `template_id` BIGINT NOT NULL COMMENT '关联模版ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(255) NOT NULL COMMENT '分类名称',
  `type` VARCHAR(50) NOT NULL COMMENT '类型: key-value, checked, step',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_template_id` (`template_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SOP模版分类表';

-- 13. SOP Item Table
CREATE TABLE IF NOT EXISTS `sop_item` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  `category_id` BIGINT NOT NULL COMMENT '关联分类ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `item_key` VARCHAR(255) COMMENT '键',
  `item_value` TEXT COMMENT '值',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_category_id` (`category_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SOP模版明细项表';

-- 14. Event Schedule Table
CREATE TABLE IF NOT EXISTS `event_schedule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `event_name` VARCHAR(100) NOT NULL COMMENT '事件名称',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `effective_start_date` DATE NOT NULL COMMENT '有效开始日期',
  `effective_end_date` DATE DEFAULT NULL COMMENT '有效结束日期，NULL表示当前生效',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0否，1是',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_active` (`user_id`, `is_deleted`, `effective_end_date`),
  INDEX `idx_effective_range` (`effective_start_date`, `effective_end_date`),
  UNIQUE KEY `uk_active_event` (`user_id`, `event_name`, `start_time`, `end_time`, `effective_end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时间待办事件配置表';

-- 15. Event Execution Table
CREATE TABLE IF NOT EXISTS `event_execution` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `schedule_id` BIGINT NOT NULL COMMENT '事件配置ID',
  `execute_date` DATE NOT NULL COMMENT '执行日期',
  `is_executed` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已执行：0否，1是',
  `executed_at` DATETIME DEFAULT NULL COMMENT '实际执行时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_schedule_date` (`schedule_id`, `execute_date`),
  INDEX `idx_execute_date` (`execute_date`),
  CONSTRAINT `fk_event_execution_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `event_schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时间待办执行记录表';
