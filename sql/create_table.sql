# 数据库初始化（基础表结构）
# @author super.winner
# 注意：此文件只包含基础表结构，其他字段由增量 SQL 文件添加

-- 设置字符集（解决中文乱码问题）
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建库
create database if not exists ai_article_creator CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 切换库
use ai_article_creator;

-- 用户表（基础字段，quota 和 vipTime 由增量脚本添加）
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    quota        int default 5 not null comment '剩余配额' ,
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    vipTime      DATETIME NULL COMMENT '成为会员时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 初始化数据
INSERT INTO user (id, userAccount, userPassword, userName, userAvatar, userProfile, userRole) VALUES
(1, 'admin', '10670d38ec32fa8102be6a37f8cb52bf', '管理员', '', '系统管理员', 'admin'),
(2, 'user', '10670d38ec32fa8102be6a37f8cb52bf', '普通用户', '', '普通用户', 'user');

-- 文章表（基础字段，style/phase/titleOptions/userDescription/enabledImageMethods 由增量脚本添加）
create table if not exists article
(
    id              bigint auto_increment comment 'id' primary key,
    taskId          varchar(64)                        not null comment '任务ID（UUID）',
    userId          bigint                             not null comment '用户ID',
    topic           varchar(500)                       not null comment '选题',
    style           VARCHAR(20)                        null COMMENT '文章风格',
    mainTitle       varchar(200)                       null comment '主标题',
    subTitle        varchar(300)                       null comment '副标题',
    outline         json                               null comment '大纲（JSON格式）',
    content         text                               null comment '正文（Markdown格式）',
    fullContent     text                               null comment '完整图文（Markdown格式，含配图）',
    coverImage      varchar(512)                       null comment '封面图 URL',
    images          json                               null comment '配图列表（JSON数组，包含封面图 position=1）',
    status          varchar(20) default 'PENDING'      not null comment '状态：PENDING/PROCESSING/COMPLETED/FAILED',
    phase           VARCHAR(50) DEFAULT 'PENDING' COMMENT '当前阶段：PENDING/TITLE_GENERATING/TITLE_SELECTING/OUTLINE_GENERATING/OUTLINE_EDITING/CONTENT_GENERATING' ,
    titleOptions    JSON NULL COMMENT '标题方案列表（3-5个方案）'  ,
    userDescription TEXT NULL COMMENT '用户补充描述'  ,
    enabledImageMethods JSON NULL COMMENT '允许的配图方式列表'  ;
    errorMessage    text                               null comment '错误信息',
    createTime      datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    completedTime   datetime                           null comment '完成时间',
    updateTime      datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint     default 0              not null comment '是否删除',
    UNIQUE KEY uk_taskId (taskId),
    INDEX idx_userId (userId),
    INDEX idx_status (status),
    INDEX idx_createTime (createTime),
    INDEX idx_userId_status (userId, status)
) comment '文章表' collate = utf8mb4_unicode_ci;

-- 智能体执行日志表
create table if not exists agent_log
(
    id              bigint auto_increment comment 'id' primary key,
    taskId          varchar(64)                        not null comment '任务ID',
    agentName       varchar(50)                        not null comment '智能体名称',
    startTime       datetime                           not null comment '开始时间',
    endTime         datetime                           null comment '结束时间',
    durationMs      int                                null comment '耗时（毫秒）',
    status          varchar(20)                        not null comment '状态：SUCCESS/FAILED',
    errorMessage    text                               null comment '错误信息',
    prompt          text                               null comment '使用的Prompt',
    inputData       json                               null comment '输入数据（JSON格式）',
    outputData      json                               null comment '输出数据（JSON格式）',
    createTime      datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint     default 0              not null comment '是否删除',
    INDEX idx_taskId (taskId),
    INDEX idx_agentName (agentName),
    INDEX idx_status (status),
    INDEX idx_createTime (createTime)
) comment '智能体执行日志表' collate = utf8mb4_unicode_ci;

-- 2. 创建支付记录表
CREATE TABLE IF NOT EXISTS payment_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  userId BIGINT NOT NULL COMMENT '用户ID',
  stripeSessionId VARCHAR(128) COMMENT 'Stripe Checkout Session ID',
    stripePaymentIntentId VARCHAR(128) COMMENT 'Stripe 支付意向ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额（美元）',
    currency VARCHAR(8) DEFAULT 'usd' COMMENT '货币',
    status VARCHAR(32) NOT NULL COMMENT '状态：PENDING/SUCCEEDED/FAILED/REFUNDED',
    productType VARCHAR(32) NOT NULL COMMENT '产品类型：VIP_PERMANENT',
    description VARCHAR(256) COMMENT '描述',
    refundTime DATETIME NULL COMMENT '退款时间',
    refundReason VARCHAR(512) NULL COMMENT '退款原因',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_userId (userId),
    INDEX idx_stripeSessionId (stripeSessionId),
    INDEX idx_status (status),
    INDEX idx_createTime (createTime)
    ) COMMENT '支付记录表' COLLATE = utf8mb4_unicode_ci;

-- 热点模块表
-- 抓取源配置表
CREATE TABLE IF NOT EXISTS crawl_source (
                                            id              BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
                                            name            VARCHAR(100)  NOT NULL COMMENT '源名称（如 Hacker News）',
    url             VARCHAR(1024) NOT NULL COMMENT '抓取目标 URL',
    itemSelector    VARCHAR(500)  NOT NULL COMMENT '条目 CSS 选择器',
    titleSelector   VARCHAR(500)  NOT NULL COMMENT '标题 CSS 选择器（相对于 item）',
    linkSelector    VARCHAR(500)  NOT NULL COMMENT '链接 CSS 选择器（相对于 item）',
    descSelector    VARCHAR(500)  NULL     COMMENT '摘要 CSS 选择器（可选）',
    cronExpression  VARCHAR(50)   DEFAULT '0 */30 * * * ?' COMMENT 'cron 表达式',
    enabled         TINYINT       DEFAULT 1 COMMENT '是否启用',
    createTime      DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime      DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete        TINYINT       DEFAULT 0 NOT NULL COMMENT '是否删除',
    INDEX idx_enabled (enabled)
    ) COMMENT '抓取源配置' COLLATE = utf8mb4_unicode_ci;

-- AI 热榜条目表
CREATE TABLE IF NOT EXISTS hot_article (
                                           id              BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
                                           sourceId        BIGINT        NOT NULL COMMENT '来源 ID',
                                           originalTitle   VARCHAR(500)  NOT NULL COMMENT '原始标题',
    originalUrl     VARCHAR(1024) NOT NULL COMMENT '原始链接',
    originalDesc    TEXT          NULL     COMMENT '原始摘要',
    aiTitle         VARCHAR(500)  NULL     COMMENT 'AI 改写的中文标题',
    aiSummary       VARCHAR(1000) NULL     COMMENT 'AI 一句话点评',
    aiScore         INT           NULL     COMMENT 'AI 推荐分 1-10',
    crawlTime       DATETIME      NOT NULL COMMENT '抓取时间',
    createTime      DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime      DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete        TINYINT       DEFAULT 0 NOT NULL COMMENT '是否删除',
    UNIQUE KEY uk_url (originalUrl(255)),
    INDEX idx_sourceId (sourceId),
    INDEX idx_aiScore (aiScore),
    INDEX idx_crawlTime (crawlTime)
    ) COMMENT 'AI 热榜条目' COLLATE = utf8mb4_unicode_ci;
