DROP TABLE IF EXISTS `upay_sequence_key`;
CREATE TABLE `upay_sequence_key` (
  `id` BIGINT NOT NULL,
  `key` VARCHAR(50) NOT NULL,
  `start_with` BIGINT DEFAULT '1',
  `inc_span` BIGINT DEFAULT '1',
  `scope` VARCHAR(50),
  `description` VARCHAR(128) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- 商户表
-- ----------------------------
DROP TABLE IF EXISTS `upay_merchant`;
CREATE TABLE `upay_merchant` (
  `id` BIGINT NOT NULL COMMENT '商户ID-具有生成规则',
  `code` VARCHAR(20) NOT NULL COMMENT '商户编码',
  `name` VARCHAR(50) NOT NULL COMMENT '商户名称',
  `account_id` BIGINT NOT NULL COMMENT '资金账号',
  `address` VARCHAR(128) COMMENT '商户地址',
  `contact` VARCHAR(50) COMMENT '联系人',
  `mobile` VARCHAR(20) COMMENT '手机号',
  `secret_key` VARCHAR(250) NOT NULL COMMENT '安全密钥-接口使用',
  `access_token` VARCHAR(40) COMMENT '授权Token',
  `status` TINYINT UNSIGNED NOT NULL COMMENT '商户状态-正常等',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- 资金账户表
-- ----------------------------
DROP TABLE IF EXISTS `upay_fund_account`;
CREATE TABLE `upay_fund_account` (
  `id` BIGINT NOT NULL COMMENT '账号ID-具有生成规则',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '账号类型-商户 个人',
  `code` VARCHAR(20) COMMENT '登录账号-如卡号',
  `name` VARCHAR(20) NOT NULL COMMENT '用户名',
  `gender` TINYINT UNSIGNED COMMENT '性别',
  `mobile` VARCHAR(20) NOT NULL COMMENT '手机号',
  `email` VARCHAR(40) COMMENT '邮箱地址',
  `id_code` VARCHAR(20) COMMENT '身份证号码',
  `address` VARCHAR(128) COMMENT '联系地址',
  `login_pwd` VARCHAR(50) NOT NULL COMMENT '登陆密码',
  `password` VARCHAR(50) NOT NULL COMMENT '交易密码',
  `pwd_change` TINYINT UNSIGNED COMMENT '修改登陆密码?',
  `login_time` DATETIME COMMENT '最近登陆时间',
  `secret_key` VARCHAR(80) NOT NULL COMMENT '安全密钥',
  `merchant_id` BIGINT NOT NULL COMMENT '商户ID',
  `status` TINYINT UNSIGNED NOT NULL COMMENT '账号状态-正常 锁定等',
  `lock_time` DATETIME COMMENT '锁定时间',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_fund_account_code` (`code`) USING BTREE,
  KEY `idx_fund_account_name` (`name`) USING BTREE,
  KEY `idx_fund_account_mobile` (`mobile`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- 账户资金表
-- ----------------------------
DROP TABLE IF EXISTS `upay_account_fund`;
CREATE TABLE `upay_account_fund` (
  `id` BIGINT NOT NULL COMMENT '账号ID',
  `balance` BIGINT NOT NULL COMMENT '账户余额-分',
  `frozen_amount` BIGINT NOT NULL COMMENT '冻结金额-分',
  `version` INTEGER UNSIGNED NOT NULL COMMENT '数据版本号',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- 账户资金流水表
-- 账户资金表的任何变化必须在此表有记录
-- ----------------------------
DROP TABLE IF EXISTS `upay_fund_statement`;
CREATE TABLE `upay_fund_statement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `transaction_id` BIGINT NOT NULL COMMENT '事务ID',
  `pipeline` TINYINT UNSIGNED NOT NULL COMMENT '渠道类型-现金 账户等',
  `action` TINYINT UNSIGNED NOT NULL COMMENT '动作-收入 支出',
  `balance` BIGINT NOT NULL COMMENT '(前)余额-分',
  `amount` BIGINT NOT NULL COMMENT '金额-分(正值 负值)',
  `description` VARCHAR(128) COMMENT '备注',
  `created_time` DATETIME COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_fund_stmt_accountId` (`account_id`) USING BTREE,
  KEY `idx_fund_stmt_transactionId` (`transaction_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- 资金事务表
-- 账户资金流水表的任何变化必须通过资金事务来完成
-- ----------------------------
DROP TABLE IF EXISTS `upay_fund_transaction`;
CREATE TABLE `upay_fund_transaction` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商户ID',
  `serial_no` VARCHAR(40) NOT NULL COMMENT '交易流水号',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '业务类型-充值 提现 交易 退款等',
  `from_id` BIGINT COMMENT '账号ID',
  `from_name` VARCHAR(20) COMMENT '用户名-冗余',
  `to_id` BIGINT NOT NULL COMMENT '账号ID',
  `to_name` VARCHAR(20) COMMENT '用户名-冗余',
  `pipeline` TINYINT UNSIGNED NOT NULL COMMENT '渠道类型-现金 账户余额 工商银行等',
  `amount` BIGINT NOT NULL COMMENT '金额-分(正值)',
  `status` TINYINT UNSIGNED NOT NULL COMMENT '状态-申请 完成 失败等',
  `description` VARCHAR(128) COMMENT '备注',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_fund_transaction_fromId` (`from_id`) USING BTREE,
  KEY `idx_fund_transaction_toId` (`to_id`) USING BTREE,
  UNIQUE KEY `idx_fund_transaction_serialNo` (`serial_no`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- 事务费用表
-- 资金事务的费用，比如提现手续费等，此表的数据将引起商户收益账户的资金变动
-- ----------------------------
DROP TABLE IF EXISTS `upay_transaction_fee`;
CREATE TABLE `upay_transaction_fee` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `transaction_id` BIGINT NOT NULL COMMENT '事务ID',
  `pipeline` TINYINT UNSIGNED NOT NULL COMMENT '渠道类型-现金 账户余额 工商银行等',
  `amount` BIGINT NOT NULL COMMENT '金额-分(正值)',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '费用类型-手续费等',
  `created_time` DATETIME COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_transaction_fee_transactionId` (`transaction_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- 资金冻结记录
-- ----------------------------
DROP TABLE IF EXISTS `upay_fund_frozen`;
CREATE TABLE `upay_fund_frozen` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `serial_no` VARCHAR(40) NOT NULL COMMENT '操作流水号',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `account_name` VARCHAR(20) COMMENT '用户名-冗余',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '冻结类型-系统冻结 交易冻结',
  `amount` BIGINT NOT NULL COMMENT '金额-分',
  `status` TINYINT UNSIGNED NOT NULL COMMENT '冻结状态-冻结 解冻',
  `frozen_time` DATETIME NOT NULL COMMENT '冻结时间',
  `unfrozen_time` DATETIME COMMENT '解冻时间',
  `merchant_id` BIGINT NOT NULL COMMENT '商户ID',
  `frozen_uid` BIGINT NULL COMMENT '冻结人',
  `frozen_uname` VARCHAR(20) NULL COMMENT '冻结人名称',
  `unfrozen_uid` BIGINT NULL COMMENT '解冻人',
  `unfrozen_uname` VARCHAR(20) NULL COMMENT '解冻人名称',
  `description` VARCHAR(128) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_fund_frozen_serialNo` (`serial_no`) USING BTREE,
  KEY `idx_fund_frozen_accountId` (`account_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- 分布式子事务表
-- 当与外部系统发生交易（分布式事务）时使用，如果只是发生系统内部账号之间的交易无需使用。
-- ----------------------------
DROP TABLE IF EXISTS `upay_xa_transaction`;
CREATE TABLE `upay_xa_transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `transaction_id` BIGINT NOT NULL COMMENT '事务ID',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `pipeline` TINYINT UNSIGNED NOT NULL COMMENT '渠道类型-现金 账户余额 工商银行等',
  `action` TINYINT UNSIGNED NOT NULL COMMENT '动作-收入 支出',
  `amount` BIGINT NOT NULL COMMENT '金额-分',
  `status` TINYINT UNSIGNED NOT NULL COMMENT '状态-申请 成功 失败等',
  `sequence` TINYINT UNSIGNED NOT NULL COMMENT '子事务顺序',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间'
  PRIMARY KEY (`id`),
  KEY `idx_xa_transaction_transactionId` (`transaction_id`) USING BTREE,
  KEY `idx_xa_transaction_accountId` (`account_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
