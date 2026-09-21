-- =============================================================================
--   可扩展字段/表，但不得删除或篡改官方字段名与语义
-- =============================================================================

CREATE DATABASE IF NOT EXISTS qx_repair
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE qx_repair;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS repair_ticket_log;
DROP TABLE IF EXISTS repair_ticket;
DROP TABLE IF EXISTS repair_category;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

SET FOREIGN_KEY_CHECKS = 1;

-- -----------------------------------------------------------------------------
-- 1. 用户表
-- -----------------------------------------------------------------------------
CREATE TABLE sys_user (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户主键ID',
  username        VARCHAR(32)  NOT NULL                COMMENT '登录用户名，唯一',
  password        VARCHAR(100) NOT NULL                COMMENT '加密登录密码',
  nickname        VARCHAR(32)  NOT NULL                COMMENT '用户昵称',
  phone           VARCHAR(20)  DEFAULT NULL            COMMENT '手机号，可空',
  status          TINYINT      NOT NULL DEFAULT 1      COMMENT '账号状态：1=启用，0=禁用',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_username (username),
  KEY idx_sys_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -----------------------------------------------------------------------------
-- 2. 角色表
-- -----------------------------------------------------------------------------
CREATE TABLE sys_role (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色主键ID',
  role_code       VARCHAR(32)  NOT NULL                COMMENT '角色编码，唯一，如 ADMIN/USER',
  role_name       VARCHAR(50)  NOT NULL                COMMENT '角色显示名称',
  description     VARCHAR(200) DEFAULT NULL            COMMENT '角色说明',
  status          TINYINT      NOT NULL DEFAULT 1      COMMENT '角色状态：1=启用，0=停用',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_code (role_code),
  KEY idx_sys_role_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- -----------------------------------------------------------------------------
-- 3. 权限表
-- -----------------------------------------------------------------------------
CREATE TABLE sys_permission (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '权限主键ID',
  perm_code       VARCHAR(64)  NOT NULL                COMMENT '权限编码，唯一，如 ticket:create',
  perm_name       VARCHAR(50)  NOT NULL                COMMENT '权限显示名称',
  description     VARCHAR(200) DEFAULT NULL            COMMENT '权限说明',
  status          TINYINT      NOT NULL DEFAULT 1      COMMENT '权限状态：1=启用，0=停用',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_permission_code (perm_code),
  KEY idx_sys_permission_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- -----------------------------------------------------------------------------
-- 4. 用户-角色关联表（多对多）
-- -----------------------------------------------------------------------------
CREATE TABLE sys_user_role (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '关联主键ID',
  user_id         BIGINT       NOT NULL                COMMENT '用户ID，对应 sys_user.id',
  role_id         BIGINT       NOT NULL                COMMENT '角色ID，对应 sys_role.id',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_role (user_id, role_id),
  KEY idx_user_role_role_id (role_id),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户与角色关联表';

-- -----------------------------------------------------------------------------
-- 5. 角色-权限关联表（多对多）
-- -----------------------------------------------------------------------------
CREATE TABLE sys_role_permission (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '关联主键ID',
  role_id         BIGINT       NOT NULL                COMMENT '角色ID，对应 sys_role.id',
  permission_id   BIGINT       NOT NULL                COMMENT '权限ID，对应 sys_permission.id',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_permission (role_id, permission_id),
  KEY idx_role_permission_perm_id (permission_id),
  CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role (id),
  CONSTRAINT fk_role_permission_perm FOREIGN KEY (permission_id) REFERENCES sys_permission (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色与权限关联表';

-- -----------------------------------------------------------------------------
-- 6. 报修分类表
-- -----------------------------------------------------------------------------
CREATE TABLE repair_category (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类主键ID',
  name            VARCHAR(50)  NOT NULL                COMMENT '分类名称，唯一',
  status          TINYINT      NOT NULL DEFAULT 1      COMMENT '分类状态：1=启用，0=停用；停用后不可新建工单',
  sort_no         INT          NOT NULL DEFAULT 0      COMMENT '排序号，数值越小越靠前',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_repair_category_name (name),
  KEY idx_repair_category_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报修分类表';

-- -----------------------------------------------------------------------------
-- 7. 报修工单表
-- -----------------------------------------------------------------------------
CREATE TABLE repair_ticket (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '工单主键ID',
  user_id         BIGINT       NOT NULL                COMMENT '报修人用户ID，对应 sys_user.id',
  category_id     BIGINT       NOT NULL                COMMENT '报修分类ID，对应 repair_category.id',
  category_name   VARCHAR(50)  NOT NULL                COMMENT '分类名称快照：创建工单时写入，之后不随分类表改名而变化',
  title           VARCHAR(100) NOT NULL                COMMENT '工单标题',
  description     TEXT         NOT NULL                COMMENT '问题详细描述',
  location        VARCHAR(100) NOT NULL                COMMENT '报修地点',
  contact_phone   VARCHAR(20)  DEFAULT NULL            COMMENT '联系电话，可空',
  image_url       VARCHAR(255) DEFAULT NULL            COMMENT '现场图片URL，可空',
  status          VARCHAR(16)  NOT NULL                COMMENT '工单状态：PENDING/ACCEPTED/PROCESSING/DONE/REJECTED/CANCELLED',
  reject_reason   VARCHAR(200) DEFAULT NULL            COMMENT '驳回原因，状态为 REJECTED 时必有',
  handle_result   VARCHAR(500) DEFAULT NULL            COMMENT '处理结果，状态为 DONE 时必有',
  handler_id      BIGINT       DEFAULT NULL            COMMENT '当前受理人用户ID，对应 sys_user.id',
  accept_time     DATETIME     DEFAULT NULL            COMMENT '受理时间',
  finish_time     DATETIME     DEFAULT NULL            COMMENT '完成时间',
  cancel_time     DATETIME     DEFAULT NULL            COMMENT '取消时间',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（提交报修时间）',
  update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (id),
  KEY idx_repair_ticket_user_id (user_id),
  KEY idx_repair_ticket_category_id (category_id),
  KEY idx_repair_ticket_status (status),
  KEY idx_repair_ticket_handler_id (handler_id),
  KEY idx_repair_ticket_user_status (user_id, status),
  CONSTRAINT fk_repair_ticket_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_repair_ticket_category FOREIGN KEY (category_id) REFERENCES repair_category (id),
  CONSTRAINT fk_repair_ticket_handler FOREIGN KEY (handler_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报修工单表';

-- -----------------------------------------------------------------------------
-- 8. 工单处理日志表（关键状态变化写入；无官方查询API，考核查库）
-- -----------------------------------------------------------------------------
CREATE TABLE repair_ticket_log (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志主键ID',
  ticket_id       BIGINT       NOT NULL                COMMENT '工单ID，对应 repair_ticket.id',
  operator_id     BIGINT       NOT NULL                COMMENT '操作人用户ID，对应 sys_user.id',
  from_status     VARCHAR(16)  DEFAULT NULL            COMMENT '变更前状态；用户首次创建可为 NULL',
  to_status       VARCHAR(16)  NOT NULL                COMMENT '变更后状态',
  action          VARCHAR(32)  NOT NULL                COMMENT '动作类型：CREATE/ACCEPT/REJECT/START/FINISH/CANCEL 等',
  content         VARCHAR(500) DEFAULT NULL            COMMENT '原因/备注/处理结果等说明，可空但驳回、完成等场景应有内容',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (id),
  KEY idx_repair_ticket_log_ticket_id (ticket_id),
  KEY idx_repair_ticket_log_operator_id (operator_id),
  KEY idx_repair_ticket_log_action (action),
  CONSTRAINT fk_repair_ticket_log_ticket FOREIGN KEY (ticket_id) REFERENCES repair_ticket (id),
  CONSTRAINT fk_repair_ticket_log_operator FOREIGN KEY (operator_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单处理日志表';


-- =============================================================================
-- 种子数据：角色 / 权限 / 用户 / 关联 / 分类 / 示例工单
-- 明文密码（仅考核环境）：
--   admin  / Admin@123
--   user01 / User@123
--   user02 / User@123
-- =============================================================================

INSERT INTO sys_role (id, role_code, role_name, description, status) VALUES
(1, 'ADMIN', '管理员', '可管理分类、处理全部工单、查看看板', 1),
(2, 'USER',  '普通用户', '可提交报修、查看与取消自己的待受理工单', 1);

INSERT INTO sys_permission (id, perm_code, perm_name, description, status) VALUES
(1,  'user:profile',        '查看与修改个人资料', '访问 /api/users/me', 1),
(2,  'category:view',       '查看启用分类',       '访问 /api/categories', 1),
(3,  'category:manage',     '管理报修分类',       '访问 /api/admin/categories**（含 DELETE）', 1),
(4,  'ticket:create',       '提交报修工单',       'POST /api/tickets', 1),
(5,  'ticket:view:own',     '查看自己的工单',     'GET /api/tickets/me 与本人详情', 1),
(6,  'ticket:cancel:own',   '取消自己的待受理工单', 'POST /api/tickets/{id}/cancel', 1),
(7,  'ticket:view:all',     '查看全部工单',       'GET /api/admin/tickets**', 1),
(8,  'ticket:accept',       '受理工单',           'POST /api/admin/tickets/{id}/accept', 1),
(9,  'ticket:reject',       '驳回工单',           'POST /api/admin/tickets/{id}/reject', 1),
(10, 'ticket:start',        '开始处理工单',       'POST /api/admin/tickets/{id}/start', 1),
(11, 'ticket:finish',       '完成工单',           'POST /api/admin/tickets/{id}/finish', 1),
(12, 'ticket:force-cancel', '强制取消工单',       'POST /api/admin/tickets/{id}/force-cancel', 1),
(13, 'dashboard:view',      '查看管理看板',       'GET /api/admin/dashboard', 1),
(14, 'ticket:delete',       '删除工单',           'DELETE /api/admin/tickets/{id}', 1);

-- USER 角色权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(2, 1), (2, 2), (2, 4), (2, 5), (2, 6);

-- ADMIN 角色权限（含普通用户能力 + 管理能力）
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6),
(1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14);

INSERT INTO sys_user (id, username, password, nickname, phone, status) VALUES
(1, 'admin',  '$2b$10$DSn6XPXVoeOVtlTsz7mjm.LTMyi42i9IP27g5YVLhwJI146ebOXIq', '系统管理员', '13800000000', 1),
(2, 'user01', '$2b$10$Xs/IMI/dfrSXScjZJ59Z/eHmCUZ4z7dGRZUsF72ulpZzDh6siQlAG', '同学甲', '13800000001', 1),
(3, 'user02', '$2b$10$Xs/IMI/dfrSXScjZJ59Z/eHmCUZ4z7dGRZUsF72ulpZzDh6siQlAG', '同学乙', '13900000002', 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 2);

INSERT INTO repair_category (id, name, status, sort_no) VALUES
(1, '水电', 1, 1),
(2, '门窗家具', 1, 2),
(3, '网络弱电', 1, 3),
(4, '已停用分类', 0, 99);

INSERT INTO repair_ticket (
  id, user_id, category_id, category_name, title, description, location, contact_phone, image_url,
  status, reject_reason, handle_result, handler_id, accept_time, finish_time, cancel_time
) VALUES
(1, 2, 1, '水电', '洗手台漏水', '洗手台一直滴水', '6号楼302', '13800000001', NULL,
 'PENDING', NULL, NULL, NULL, NULL, NULL, NULL),
(2, 2, 2, '门窗家具', '宿舍门锁松动', '锁芯不顺，开关费力', '6号楼302', '13800000001', NULL,
 'ACCEPTED', NULL, NULL, 1, '2026-09-10 10:00:00', NULL, NULL),
(3, 3, 3, '网络弱电', '走廊灯不亮', '整段走廊灯不亮', '教学楼A3层', '13900000002', NULL,
 'PROCESSING', NULL, NULL, 1, '2026-09-09 09:00:00', NULL, NULL),
(4, 3, 1, '水电', '已完成-更换阀芯', '历史完成单示例', '5号楼101', NULL, NULL,
 'DONE', NULL, '已更换阀芯', 1, '2026-08-01 10:00:00', '2026-08-01 16:00:00', NULL),
(5, 2, 2, '门窗家具', '已驳回示例', '不在报修范围内的示例', '食堂后门', NULL, NULL,
 'REJECTED', '非报修范围', NULL, 1, NULL, NULL, NULL);

INSERT INTO repair_ticket_log (id, ticket_id, operator_id, from_status, to_status, action, content, create_time) VALUES
(1, 4, 3, NULL,        'PENDING',    'CREATE', '用户提交报修', '2026-08-01 09:00:00'),
(2, 4, 1, 'PENDING',   'ACCEPTED',   'ACCEPT', '管理员受理', '2026-08-01 10:00:00'),
(3, 4, 1, 'ACCEPTED',  'PROCESSING', 'START',  '开始处理', '2026-08-01 11:00:00'),
(4, 4, 1, 'PROCESSING','DONE',       'FINISH', '已更换阀芯', '2026-08-01 16:00:00'),
(5, 2, 1, 'PENDING',   'ACCEPTED',   'ACCEPT', '管理员受理', '2026-09-10 10:00:00'),
(6, 3, 1, 'PENDING',   'ACCEPTED',   'ACCEPT', '管理员受理', '2026-09-09 09:00:00'),
(7, 3, 1, 'ACCEPTED',  'PROCESSING', 'START',  '开始处理', '2026-09-09 09:30:00'),
(8, 5, 1, 'PENDING',   'REJECTED',   'REJECT', '非报修范围', '2026-09-08 11:00:00');
