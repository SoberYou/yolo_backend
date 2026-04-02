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
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `biz_date` DATE NOT NULL COMMENT '日期',
  `time_slot` INT NOT NULL COMMENT '时间槽0-47',
  `record_type` VARCHAR(10) NOT NULL COMMENT 'plan/actual',
  `activity_type` VARCHAR(50) COMMENT '活动类型',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date_slot_type`
  (`user_id`,`biz_date`,`time_slot`,`record_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日程记录表';

