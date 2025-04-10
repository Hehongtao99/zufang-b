/*
 Navicat Premium Dump SQL

 Source Server         : 115.120.221.163
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : 115.120.221.163:3306
 Source Schema         : zufang_db

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 09/04/2025 13:59:53
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for appointment
-- ----------------------------
DROP TABLE IF EXISTS `appointment`;
CREATE TABLE `appointment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  `user_id` bigint NOT NULL COMMENT '用户ID（预约人）',
  `house_id` bigint NOT NULL COMMENT '房源ID',
  `landlord_id` bigint NOT NULL COMMENT '房东ID',
  `appointment_time` datetime NOT NULL COMMENT '预约看房时间',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系电话',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注信息',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '预约状态：PENDING-待处理，APPROVED-已同意，REJECTED-已拒绝，COMPLETED-已完成，CANCELED-已取消',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_house_id`(`house_id` ASC) USING BTREE,
  INDEX `idx_landlord_id`(`landlord_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约看房表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of appointment
-- ----------------------------
INSERT INTO `appointment` VALUES (1, 1, 20, 2, '2025-04-23 00:00:00', '13384403671', '13384403671 何鸿涛 重庆城市科技学院巴南校区', 'APPROVED', 1, '2025-04-05 15:51:10', '2025-04-05 15:51:27', 0);
INSERT INTO `appointment` VALUES (2, 1, 20, 2, '2025-04-15 00:00:00', '13384403671', '滚', 'REJECTED', 1, '2025-04-07 14:20:22', '2025-04-07 14:20:31', 0);
INSERT INTO `appointment` VALUES (3, 1, 20, 2, '2025-04-23 00:00:00', '13384403671', '13384403671 何鸿涛 重庆城市科技学院巴南校区', 'APPROVED', 1, '2025-04-07 14:20:53', '2025-04-07 14:21:02', 0);
INSERT INTO `appointment` VALUES (4, 4, 20, 2, '2025-04-08 00:00:00', '15674357823', '11', 'APPROVED', 1, '2025-04-07 15:09:48', '2025-04-07 15:09:59', 0);

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `house_id` bigint NOT NULL COMMENT '相关房源ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  `sender_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发送者头像',
  `sender_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发送者昵称',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sender_receiver`(`sender_id` ASC, `receiver_id` ASC) USING BTREE,
  INDEX `idx_house_id`(`house_id` ASC) USING BTREE,
  INDEX `idx_receiver_read`(`receiver_id` ASC, `is_read` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 50 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of chat_message
-- ----------------------------
INSERT INTO `chat_message` VALUES (1, 1, 2, 21, '你好', 1, '2025-04-07 16:46:07', '2025-04-09 01:48:13', 0, 'http://113.45.161.48:9000/zufang/avatar/34552beb4dc9424f8e6006f607871cac.png', NULL);
INSERT INTO `chat_message` VALUES (2, 1, 2, 21, '？', 1, '2025-04-07 16:46:16', '2025-04-07 09:27:15', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (3, 1, 2, 21, '1', 1, '2025-04-07 16:46:49', '2025-04-07 09:27:15', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (4, 1, 2, 21, '1', 1, '2025-04-07 16:49:24', '2025-04-07 09:27:15', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (5, 1, 2, 21, '1', 1, '2025-04-07 16:49:39', '2025-04-07 09:27:15', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (6, 1, 2, 21, '1', 1, '2025-04-07 16:50:43', '2025-04-07 09:27:15', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (7, 1, 2, 21, '2', 1, '2025-04-07 16:50:46', '2025-04-07 09:27:15', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (8, 1, 2, 21, '1', 1, '2025-04-07 16:53:15', '2025-04-07 09:27:15', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (9, 1, 2, 21, '你好', 1, '2025-04-07 16:56:39', '2025-04-07 09:27:15', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (10, 2, 1, 21, '你也好', 1, '2025-04-07 16:56:45', '2025-04-07 08:56:54', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (11, 1, 2, 21, '1', 1, '2025-04-07 17:27:15', '2025-04-07 09:27:15', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (12, 1, 2, 21, '123', 1, '2025-04-07 17:27:22', '2025-04-07 09:27:21', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (13, 2, 1, 21, '123', 1, '2025-04-07 17:28:26', '2025-04-07 09:28:25', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (14, 1, 2, 21, '你好', 1, '2025-04-08 20:43:58', '2025-04-08 12:44:31', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (15, 2, 1, 21, 'ni yehao', 1, '2025-04-08 20:44:04', '2025-04-08 12:44:16', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (16, 2, 1, 21, '1', 1, '2025-04-08 20:44:22', '2025-04-08 12:44:25', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (17, 1, 2, 21, '1', 1, '2025-04-08 20:44:32', '2025-04-08 12:44:31', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (18, 1, 2, 21, '1', 1, '2025-04-08 21:09:12', '2025-04-08 13:13:45', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (19, 2, 1, 21, '1', 1, '2025-04-08 21:09:29', '2025-04-08 13:10:14', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (20, 1, 2, 21, '1', 1, '2025-04-08 21:09:33', '2025-04-08 13:13:45', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (21, 1, 2, 21, '1', 1, '2025-04-08 21:10:24', '2025-04-08 13:13:45', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (22, 2, 1, 21, '1', 1, '2025-04-08 21:10:45', '2025-04-08 13:10:58', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (23, 1, 2, 21, '1', 1, '2025-04-08 21:10:47', '2025-04-08 13:13:45', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (24, 1, 2, 21, '1', 1, '2025-04-08 21:11:11', '2025-04-08 13:13:45', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (25, 2, 1, 21, '1', 1, '2025-04-08 21:13:31', '2025-04-08 13:13:41', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (26, 1, 2, 21, '1', 1, '2025-04-08 21:13:32', '2025-04-08 13:13:45', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (27, 1, 2, 21, '1', 1, '2025-04-08 21:13:46', '2025-04-08 13:13:45', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (28, 1, 2, 21, '1', 1, '2025-04-08 21:13:48', '2025-04-08 13:13:47', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (29, 2, 1, 21, '1', 1, '2025-04-08 21:13:51', '2025-04-08 13:13:50', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (30, 2, 1, 21, '1', 1, '2025-04-08 21:13:53', '2025-04-08 13:13:52', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (31, 1, 2, 21, '1', 1, '2025-04-08 21:13:55', '2025-04-08 13:13:54', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (32, 1, 2, 21, '1', 1, '2025-04-08 21:13:57', '2025-04-08 13:13:56', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (33, 2, 1, 21, '1', 1, '2025-04-09 08:58:59', '2025-04-09 00:59:03', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (34, 1, 2, 21, '11', 1, '2025-04-09 08:59:05', '2025-04-09 00:59:21', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (35, 2, 1, 21, '1', 1, '2025-04-09 08:59:12', '2025-04-09 00:59:18', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (36, 1, 2, 21, '1', 1, '2025-04-09 08:59:19', '2025-04-09 00:59:21', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (37, 2, 1, 21, '1', 1, '2025-04-09 08:59:23', '2025-04-09 00:59:24', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (38, 2, 1, 21, '1111', 1, '2025-04-09 08:59:28', '2025-04-09 01:02:53', 0, NULL, NULL);
INSERT INTO `chat_message` VALUES (39, 2, 1, 21, '1', 1, '2025-04-09 09:03:58', '2025-04-09 01:48:38', 0, 'http://113.45.161.48:9000/zufang/avatar/34552beb4dc9424f8e6006f607871cac.png', NULL);
INSERT INTO `chat_message` VALUES (40, 1, 2, 21, '1', 1, '2025-04-09 09:50:15', '2025-04-09 01:50:16', 0, 'http://113.45.161.48:9000/zufang/avatar/9ad2046a68da4519859dc319ad5fe892.jpg', '111的昵称');
INSERT INTO `chat_message` VALUES (41, 2, 1, 21, '1', 1, '2025-04-09 09:50:18', '2025-04-09 01:50:19', 0, 'http://113.45.161.48:9000/zufang/avatar/34552beb4dc9424f8e6006f607871cac.png', '1111');
INSERT INTO `chat_message` VALUES (42, 1, 2, 21, '1', 1, '2025-04-09 09:52:39', '2025-04-09 01:52:40', 0, 'http://113.45.161.48:9000/zufang/avatar/9ad2046a68da4519859dc319ad5fe892.jpg', '111的昵称');
INSERT INTO `chat_message` VALUES (43, 2, 1, 21, '1', 1, '2025-04-09 09:54:34', '2025-04-09 01:54:35', 0, 'http://113.45.161.48:9000/zufang/avatar/34552beb4dc9424f8e6006f607871cac.png', '1111');
INSERT INTO `chat_message` VALUES (44, 1, 2, 21, '1', 1, '2025-04-09 09:54:45', '2025-04-09 01:54:47', 0, 'http://113.45.161.48:9000/zufang/avatar/9ad2046a68da4519859dc319ad5fe892.jpg', '111的昵称');
INSERT INTO `chat_message` VALUES (45, 2, 1, 21, '1', 1, '2025-04-09 10:00:04', '2025-04-09 02:00:10', 0, 'http://113.45.161.48:9000/zufang/avatar/34552beb4dc9424f8e6006f607871cac.png', '1111');
INSERT INTO `chat_message` VALUES (46, 1, 2, 21, '你好', 1, '2025-04-09 10:00:14', '2025-04-09 02:00:15', 0, 'http://113.45.161.48:9000/zufang/avatar/9ad2046a68da4519859dc319ad5fe892.jpg', '111的昵称');
INSERT INTO `chat_message` VALUES (47, 2, 1, 21, '你也好', 1, '2025-04-09 10:00:29', '2025-04-09 02:04:54', 0, 'http://113.45.161.48:9000/zufang/avatar/34552beb4dc9424f8e6006f607871cac.png', '1111');
INSERT INTO `chat_message` VALUES (48, 2, 1, 21, '你好', 1, '2025-04-09 10:05:00', '2025-04-09 02:05:24', 0, 'http://113.45.161.48:9000/zufang/avatar/34552beb4dc9424f8e6006f607871cac.png', '1111');
INSERT INTO `chat_message` VALUES (49, 2, 1, 21, '12', 1, '2025-04-09 10:05:32', '2025-04-09 02:05:39', 0, 'http://113.45.161.48:9000/zufang/avatar/34552beb4dc9424f8e6006f607871cac.png', '1111');

-- ----------------------------
-- Table structure for contract
-- ----------------------------
DROP TABLE IF EXISTS `contract`;
CREATE TABLE `contract`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '合同ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `house_id` bigint NOT NULL COMMENT '房源ID',
  `user_id` bigint NOT NULL COMMENT '租客ID',
  `landlord_id` bigint NOT NULL COMMENT '房东ID',
  `contract_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '合同编号',
  `contract_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '合同文件URL',
  `start_date` date NOT NULL COMMENT '租期开始日期',
  `end_date` date NOT NULL COMMENT '租期结束日期',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态：PENDING-待签署，SIGNED-已签署，TERMINATED-已终止',
  `contract_template_id` bigint NOT NULL COMMENT '使用的合同模板ID',
  `filled_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '填充后的合同内容',
  `party_a_signature` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '甲方签名',
  `party_b_signature` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '乙方签名',
  `sign_date` datetime NULL DEFAULT NULL COMMENT '签署日期',
  `penalty_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '违约金',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_contract_no`(`contract_no` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_house_id`(`house_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_landlord_id`(`landlord_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '合同表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of contract
-- ----------------------------
INSERT INTO `contract` VALUES (11, 11, 20, 1, 2, 'CTR202504079870923E', NULL, '2025-04-07', '2025-07-06', 'SIGNED', 1, '<h1 style=\"text-align: center;\">房屋租赁合同</h1>\r\n<p style=\"text-align: right;\">合同编号：CTR202504079870923E</p>\r\n<p style=\"text-align: right;\">签订日期：2025-04-07</p>\r\n\r\n<p>出租方（以下简称甲方）：<strong>李白</strong></p>\r\n<p>身份证号码：500234200309282435</p>\r\n<p>联系电话：18723577492</p>\r\n\r\n<p>承租方（以下简称乙方）：<strong>何鸿涛</strong></p>\r\n<p>身份证号码：50023420030928243X</p>\r\n<p>联系电话：13384403671</p>\r\n\r\n<h3>第一条 租赁房屋基本情况</h3>\r\n<p>1.1 房屋坐落于中央大街（以下简称该房屋）。</p>\r\n<p>1.2 该房屋建筑面积为100平方米，房屋类型为，朝向为。</p>\r\n<p>1.3 该房屋装修情况为，该房屋附属设施、设备状况详见合同附件一《房屋设备设施清单》。</p>\r\n\r\n<h3>第二条 租赁期限</h3>\r\n<p>2.1 租赁期共个月，自2025-04-07起至2025-07-06止。</p>\r\n<p>2.2 租赁期满后，如乙方要求继续租赁，应提前30天向甲方提出，协商一致后重新签订租赁合同。</p>\r\n\r\n<h3>第三条 租金及押金</h3>\r\n<p>3.1 该房屋月租金为人民币1528.00元整。</p>\r\n<p>3.2 租金支付方式：月付/季付/半年付/年付。</p>\r\n<p>3.3 乙方应于每月/季/半年/年的第一天前支付租金。</p>\r\n<p>3.4 押金：人民币元整，合同终止时，如乙方无违约行为且按约定结清各项费用，甲方应全额退还押金。</p>\r\n\r\n<h3>第四条 房屋用途及要求</h3>\r\n<p>4.1 该房屋用途为居住，乙方不得擅自改变房屋用途。</p>\r\n<p>4.2 乙方保证遵守国家法律法规及当地政府的有关规定，不利用该房屋从事违法违规活动。</p>\r\n<p>4.3 乙方应爱护并合理使用房屋及其附属设施，如有损坏，应及时通知甲方并负责修复或经济赔偿。</p>\r\n\r\n<h3>第五条 相关费用的承担</h3>\r\n<p>5.1 在租赁期内，与该房屋有关的水费、电费、燃气费、网络费、物业管理费等费用由乙方承担。</p>\r\n<p>5.2 在租赁期内，该房屋及附属设施、设备保险费、房产税等由甲方承担。</p>\r\n\r\n<h3>第六条 合同的变更、解除与终止</h3>\r\n<p>6.1 经甲乙双方协商一致，可以变更或解除本合同。</p>\r\n<p>6.2 有下列情形之一的，甲方有权解除合同，收回房屋：</p>\r\n<p>&nbsp;&nbsp;a) 乙方擅自将房屋转租、分租或转让给第三方；</p>\r\n<p>&nbsp;&nbsp;b) 乙方利用该房屋从事违法经营活动；</p>\r\n<p>&nbsp;&nbsp;c) 乙方拖欠租金累计达30天以上；</p>\r\n<p>&nbsp;&nbsp;d) 乙方故意损坏房屋。</p>\r\n<p>6.3 在租赁期内，甲方如需提前收回房屋，应至少提前30天书面通知乙方，并与乙方协商补偿事宜。</p>\r\n<p>6.4 在租赁期内，乙方如需提前退租，应至少提前30天书面通知甲方，经甲方同意后办理退租手续。</p>\r\n\r\n<h3>第七条 违约责任</h3>\r\n<p>7.1 甲方违反本合同约定，未能及时交付该房屋或者交付的房屋不符合约定，乙方有权要求甲方按照月租金的30%支付违约金。</p>\r\n<p>7.2 乙方违反本合同约定，未能按时支付租金，除应补交租金外，还应按日支付应付而未付租金的0.5%作为违约金。</p>\r\n<p>7.3 乙方违反本合同约定，擅自将该房屋转租、分租或转让给第三方，甲方有权解除合同并要求乙方按照月租金的50%支付违约金。</p>\r\n<p>7.4 乙方在租赁期内提前退租的，应支付违约金0.00元。</p>\r\n\r\n<h3>第八条 争议解决方式</h3>\r\n<p>8.1 本合同履行中如发生争议，双方应协商解决；协商不成的，可向房屋所在地的人民法院提起诉讼。</p>\r\n\r\n<h3>第九条 其他约定事项</h3>\r\n<p>9.1 本合同未尽事宜，可由双方协商一致，签订补充协议。补充协议与本合同具有同等效力。</p>\r\n<p>9.2 本合同连同附件一经签字或盖章后生效。本合同一式两份，甲乙双方各执一份，具有同等法律效力。</p>\r\n\r\n<p>&nbsp;</p>\r\n<p>甲方（签字）：李白&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;乙方（签字）：何鸿涛</p>\r\n<p>&nbsp;</p>\r\n<p>签订日期：2025年4月7日&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;签订日期：2025年4月7日</p>', 'data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"50\"><text x=\"10\" y=\"30\" font-family=\"Arial\" font-size=\"20\" fill=\"blue\">李白（已预签）</text></svg>', 'data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"50\"><text x=\"10\" y=\"30\" font-family=\"Arial\" font-size=\"20\" fill=\"green\">租客已签名</text></svg>', '2025-04-07 14:19:58', 0.00, '2025-04-07 14:19:49', '2025-04-07 14:19:49', 0);
INSERT INTO `contract` VALUES (12, 12, 20, 1, 2, 'CTR20250407E4CB93BC', NULL, '2025-04-07', '2025-07-06', 'SIGNED', 1, '<h1 style=\"text-align: center;\">房屋租赁合同</h1>\r\n<p style=\"text-align: right;\">合同编号：CTR20250407E4CB93BC</p>\r\n<p style=\"text-align: right;\">签订日期：2025-04-07</p>\r\n\r\n<p>出租方（以下简称甲方）：<strong>李白</strong></p>\r\n<p>身份证号码：500234200309282435</p>\r\n<p>联系电话：18723577492</p>\r\n\r\n<p>承租方（以下简称乙方）：<strong>何鸿涛</strong></p>\r\n<p>身份证号码：50023420030928243X</p>\r\n<p>联系电话：13384403671</p>\r\n\r\n<h3>第一条 租赁房屋基本情况</h3>\r\n<p>1.1 房屋坐落于中央大街（以下简称该房屋）。</p>\r\n<p>1.2 该房屋建筑面积为100平方米，房屋类型为，朝向为。</p>\r\n<p>1.3 该房屋装修情况为，该房屋附属设施、设备状况详见合同附件一《房屋设备设施清单》。</p>\r\n\r\n<h3>第二条 租赁期限</h3>\r\n<p>2.1 租赁期共个月，自2025-04-07起至2025-07-06止。</p>\r\n<p>2.2 租赁期满后，如乙方要求继续租赁，应提前30天向甲方提出，协商一致后重新签订租赁合同。</p>\r\n\r\n<h3>第三条 租金及押金</h3>\r\n<p>3.1 该房屋月租金为人民币1528.00元整。</p>\r\n<p>3.2 租金支付方式：月付/季付/半年付/年付。</p>\r\n<p>3.3 乙方应于每月/季/半年/年的第一天前支付租金。</p>\r\n<p>3.4 押金：人民币元整，合同终止时，如乙方无违约行为且按约定结清各项费用，甲方应全额退还押金。</p>\r\n\r\n<h3>第四条 房屋用途及要求</h3>\r\n<p>4.1 该房屋用途为居住，乙方不得擅自改变房屋用途。</p>\r\n<p>4.2 乙方保证遵守国家法律法规及当地政府的有关规定，不利用该房屋从事违法违规活动。</p>\r\n<p>4.3 乙方应爱护并合理使用房屋及其附属设施，如有损坏，应及时通知甲方并负责修复或经济赔偿。</p>\r\n\r\n<h3>第五条 相关费用的承担</h3>\r\n<p>5.1 在租赁期内，与该房屋有关的水费、电费、燃气费、网络费、物业管理费等费用由乙方承担。</p>\r\n<p>5.2 在租赁期内，该房屋及附属设施、设备保险费、房产税等由甲方承担。</p>\r\n\r\n<h3>第六条 合同的变更、解除与终止</h3>\r\n<p>6.1 经甲乙双方协商一致，可以变更或解除本合同。</p>\r\n<p>6.2 有下列情形之一的，甲方有权解除合同，收回房屋：</p>\r\n<p>&nbsp;&nbsp;a) 乙方擅自将房屋转租、分租或转让给第三方；</p>\r\n<p>&nbsp;&nbsp;b) 乙方利用该房屋从事违法经营活动；</p>\r\n<p>&nbsp;&nbsp;c) 乙方拖欠租金累计达30天以上；</p>\r\n<p>&nbsp;&nbsp;d) 乙方故意损坏房屋。</p>\r\n<p>6.3 在租赁期内，甲方如需提前收回房屋，应至少提前30天书面通知乙方，并与乙方协商补偿事宜。</p>\r\n<p>6.4 在租赁期内，乙方如需提前退租，应至少提前30天书面通知甲方，经甲方同意后办理退租手续。</p>\r\n\r\n<h3>第七条 违约责任</h3>\r\n<p>7.1 甲方违反本合同约定，未能及时交付该房屋或者交付的房屋不符合约定，乙方有权要求甲方按照月租金的30%支付违约金。</p>\r\n<p>7.2 乙方违反本合同约定，未能按时支付租金，除应补交租金外，还应按日支付应付而未付租金的0.5%作为违约金。</p>\r\n<p>7.3 乙方违反本合同约定，擅自将该房屋转租、分租或转让给第三方，甲方有权解除合同并要求乙方按照月租金的50%支付违约金。</p>\r\n<p>7.4 乙方在租赁期内提前退租的，应支付违约金0.00元。</p>\r\n\r\n<h3>第八条 争议解决方式</h3>\r\n<p>8.1 本合同履行中如发生争议，双方应协商解决；协商不成的，可向房屋所在地的人民法院提起诉讼。</p>\r\n\r\n<h3>第九条 其他约定事项</h3>\r\n<p>9.1 本合同未尽事宜，可由双方协商一致，签订补充协议。补充协议与本合同具有同等效力。</p>\r\n<p>9.2 本合同连同附件一经签字或盖章后生效。本合同一式两份，甲乙双方各执一份，具有同等法律效力。</p>\r\n\r\n<p>&nbsp;</p>\r\n<p>甲方（签字）：李白&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;乙方（签字）：何鸿涛</p>\r\n<p>&nbsp;</p>\r\n<p>签订日期：2025年4月7日&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;签订日期：2025年4月7日</p>', 'data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"50\"><text x=\"10\" y=\"30\" font-family=\"Arial\" font-size=\"20\" fill=\"blue\">李白（已预签）</text></svg>', 'data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"50\"><text x=\"10\" y=\"30\" font-family=\"Arial\" font-size=\"20\" fill=\"green\">租客已签名</text></svg>', '2025-04-07 14:21:31', 0.00, '2025-04-07 14:21:27', '2025-04-07 14:21:27', 0);
INSERT INTO `contract` VALUES (13, 13, 20, 4, 2, 'CTR202504076117591F', NULL, '2025-04-07', '2025-07-06', 'PENDING', 1, '<h1 style=\"text-align: center;\">房屋租赁合同</h1>\r\n<p style=\"text-align: right;\">合同编号：CTR202504076117591F</p>\r\n<p style=\"text-align: right;\">签订日期：2025-04-07</p>\r\n\r\n<p>出租方（以下简称甲方）：<strong>李白</strong></p>\r\n<p>身份证号码：500234200309282435</p>\r\n<p>联系电话：18723577492</p>\r\n\r\n<p>承租方（以下简称乙方）：<strong>123456</strong></p>\r\n<p>身份证号码：</p>\r\n<p>联系电话：17623772904</p>\r\n\r\n<h3>第一条 租赁房屋基本情况</h3>\r\n<p>1.1 房屋坐落于中央大街（以下简称该房屋）。</p>\r\n<p>1.2 该房屋建筑面积为100平方米，房屋类型为，朝向为。</p>\r\n<p>1.3 该房屋装修情况为，该房屋附属设施、设备状况详见合同附件一《房屋设备设施清单》。</p>\r\n\r\n<h3>第二条 租赁期限</h3>\r\n<p>2.1 租赁期共个月，自2025-04-07起至2025-07-06止。</p>\r\n<p>2.2 租赁期满后，如乙方要求继续租赁，应提前30天向甲方提出，协商一致后重新签订租赁合同。</p>\r\n\r\n<h3>第三条 租金及押金</h3>\r\n<p>3.1 该房屋月租金为人民币1528.00元整。</p>\r\n<p>3.2 租金支付方式：月付/季付/半年付/年付。</p>\r\n<p>3.3 乙方应于每月/季/半年/年的第一天前支付租金。</p>\r\n<p>3.4 押金：人民币元整，合同终止时，如乙方无违约行为且按约定结清各项费用，甲方应全额退还押金。</p>\r\n\r\n<h3>第四条 房屋用途及要求</h3>\r\n<p>4.1 该房屋用途为居住，乙方不得擅自改变房屋用途。</p>\r\n<p>4.2 乙方保证遵守国家法律法规及当地政府的有关规定，不利用该房屋从事违法违规活动。</p>\r\n<p>4.3 乙方应爱护并合理使用房屋及其附属设施，如有损坏，应及时通知甲方并负责修复或经济赔偿。</p>\r\n\r\n<h3>第五条 相关费用的承担</h3>\r\n<p>5.1 在租赁期内，与该房屋有关的水费、电费、燃气费、网络费、物业管理费等费用由乙方承担。</p>\r\n<p>5.2 在租赁期内，该房屋及附属设施、设备保险费、房产税等由甲方承担。</p>\r\n\r\n<h3>第六条 合同的变更、解除与终止</h3>\r\n<p>6.1 经甲乙双方协商一致，可以变更或解除本合同。</p>\r\n<p>6.2 有下列情形之一的，甲方有权解除合同，收回房屋：</p>\r\n<p>&nbsp;&nbsp;a) 乙方擅自将房屋转租、分租或转让给第三方；</p>\r\n<p>&nbsp;&nbsp;b) 乙方利用该房屋从事违法经营活动；</p>\r\n<p>&nbsp;&nbsp;c) 乙方拖欠租金累计达30天以上；</p>\r\n<p>&nbsp;&nbsp;d) 乙方故意损坏房屋。</p>\r\n<p>6.3 在租赁期内，甲方如需提前收回房屋，应至少提前30天书面通知乙方，并与乙方协商补偿事宜。</p>\r\n<p>6.4 在租赁期内，乙方如需提前退租，应至少提前30天书面通知甲方，经甲方同意后办理退租手续。</p>\r\n\r\n<h3>第七条 违约责任</h3>\r\n<p>7.1 甲方违反本合同约定，未能及时交付该房屋或者交付的房屋不符合约定，乙方有权要求甲方按照月租金的30%支付违约金。</p>\r\n<p>7.2 乙方违反本合同约定，未能按时支付租金，除应补交租金外，还应按日支付应付而未付租金的0.5%作为违约金。</p>\r\n<p>7.3 乙方违反本合同约定，擅自将该房屋转租、分租或转让给第三方，甲方有权解除合同并要求乙方按照月租金的50%支付违约金。</p>\r\n<p>7.4 乙方在租赁期内提前退租的，应支付违约金0.00元。</p>\r\n\r\n<h3>第八条 争议解决方式</h3>\r\n<p>8.1 本合同履行中如发生争议，双方应协商解决；协商不成的，可向房屋所在地的人民法院提起诉讼。</p>\r\n\r\n<h3>第九条 其他约定事项</h3>\r\n<p>9.1 本合同未尽事宜，可由双方协商一致，签订补充协议。补充协议与本合同具有同等效力。</p>\r\n<p>9.2 本合同连同附件一经签字或盖章后生效。本合同一式两份，甲乙双方各执一份，具有同等法律效力。</p>\r\n\r\n<p>&nbsp;</p>\r\n<p>甲方（签字）：李白&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;乙方（签字）：123456</p>\r\n<p>&nbsp;</p>\r\n<p>签订日期：2025年4月7日&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;签订日期：2025年4月7日</p>', 'data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"50\"><text x=\"10\" y=\"30\" font-family=\"Arial\" font-size=\"20\" fill=\"blue\">李白（已预签）</text></svg>', NULL, NULL, 0.00, '2025-04-07 15:10:04', '2025-04-07 15:10:04', 0);
INSERT INTO `contract` VALUES (14, 14, 20, 4, 2, 'CTR2025040733A7F220', NULL, '2025-04-07', '2025-07-06', 'SIGNED', 1, '<h1 style=\"text-align: center;\">房屋租赁合同</h1>\r\n<p style=\"text-align: right;\">合同编号：CTR2025040733A7F220</p>\r\n<p style=\"text-align: right;\">签订日期：2025-04-07</p>\r\n\r\n<p>出租方（以下简称甲方）：<strong>李白</strong></p>\r\n<p>身份证号码：500234200309282435</p>\r\n<p>联系电话：18723577492</p>\r\n\r\n<p>承租方（以下简称乙方）：<strong>1232</strong></p>\r\n<p>身份证号码：500333203929102</p>\r\n<p>联系电话：17623772904</p>\r\n\r\n<h3>第一条 租赁房屋基本情况</h3>\r\n<p>1.1 房屋坐落于中央大街（以下简称该房屋）。</p>\r\n<p>1.2 该房屋建筑面积为100平方米，房屋类型为，朝向为。</p>\r\n<p>1.3 该房屋装修情况为，该房屋附属设施、设备状况详见合同附件一《房屋设备设施清单》。</p>\r\n\r\n<h3>第二条 租赁期限</h3>\r\n<p>2.1 租赁期共个月，自2025-04-07起至2025-07-06止。</p>\r\n<p>2.2 租赁期满后，如乙方要求继续租赁，应提前30天向甲方提出，协商一致后重新签订租赁合同。</p>\r\n\r\n<h3>第三条 租金及押金</h3>\r\n<p>3.1 该房屋月租金为人民币1528.00元整。</p>\r\n<p>3.2 租金支付方式：月付/季付/半年付/年付。</p>\r\n<p>3.3 乙方应于每月/季/半年/年的第一天前支付租金。</p>\r\n<p>3.4 押金：人民币元整，合同终止时，如乙方无违约行为且按约定结清各项费用，甲方应全额退还押金。</p>\r\n\r\n<h3>第四条 房屋用途及要求</h3>\r\n<p>4.1 该房屋用途为居住，乙方不得擅自改变房屋用途。</p>\r\n<p>4.2 乙方保证遵守国家法律法规及当地政府的有关规定，不利用该房屋从事违法违规活动。</p>\r\n<p>4.3 乙方应爱护并合理使用房屋及其附属设施，如有损坏，应及时通知甲方并负责修复或经济赔偿。</p>\r\n\r\n<h3>第五条 相关费用的承担</h3>\r\n<p>5.1 在租赁期内，与该房屋有关的水费、电费、燃气费、网络费、物业管理费等费用由乙方承担。</p>\r\n<p>5.2 在租赁期内，该房屋及附属设施、设备保险费、房产税等由甲方承担。</p>\r\n\r\n<h3>第六条 合同的变更、解除与终止</h3>\r\n<p>6.1 经甲乙双方协商一致，可以变更或解除本合同。</p>\r\n<p>6.2 有下列情形之一的，甲方有权解除合同，收回房屋：</p>\r\n<p>&nbsp;&nbsp;a) 乙方擅自将房屋转租、分租或转让给第三方；</p>\r\n<p>&nbsp;&nbsp;b) 乙方利用该房屋从事违法经营活动；</p>\r\n<p>&nbsp;&nbsp;c) 乙方拖欠租金累计达30天以上；</p>\r\n<p>&nbsp;&nbsp;d) 乙方故意损坏房屋。</p>\r\n<p>6.3 在租赁期内，甲方如需提前收回房屋，应至少提前30天书面通知乙方，并与乙方协商补偿事宜。</p>\r\n<p>6.4 在租赁期内，乙方如需提前退租，应至少提前30天书面通知甲方，经甲方同意后办理退租手续。</p>\r\n\r\n<h3>第七条 违约责任</h3>\r\n<p>7.1 甲方违反本合同约定，未能及时交付该房屋或者交付的房屋不符合约定，乙方有权要求甲方按照月租金的30%支付违约金。</p>\r\n<p>7.2 乙方违反本合同约定，未能按时支付租金，除应补交租金外，还应按日支付应付而未付租金的0.5%作为违约金。</p>\r\n<p>7.3 乙方违反本合同约定，擅自将该房屋转租、分租或转让给第三方，甲方有权解除合同并要求乙方按照月租金的50%支付违约金。</p>\r\n<p>7.4 乙方在租赁期内提前退租的，应支付违约金0.00元。</p>\r\n\r\n<h3>第八条 争议解决方式</h3>\r\n<p>8.1 本合同履行中如发生争议，双方应协商解决；协商不成的，可向房屋所在地的人民法院提起诉讼。</p>\r\n\r\n<h3>第九条 其他约定事项</h3>\r\n<p>9.1 本合同未尽事宜，可由双方协商一致，签订补充协议。补充协议与本合同具有同等效力。</p>\r\n<p>9.2 本合同连同附件一经签字或盖章后生效。本合同一式两份，甲乙双方各执一份，具有同等法律效力。</p>\r\n\r\n<p>&nbsp;</p>\r\n<p>甲方（签字）：李白&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;乙方（签字）：1232</p>\r\n<p>&nbsp;</p>\r\n<p>签订日期：2025年4月7日&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;签订日期：2025年4月7日</p>', 'data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"50\"><text x=\"10\" y=\"30\" font-family=\"Arial\" font-size=\"20\" fill=\"blue\">李白（已预签）</text></svg>', 'data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"50\"><text x=\"10\" y=\"30\" font-family=\"Arial\" font-size=\"20\" fill=\"green\">租客已签名</text></svg>', '2025-04-07 15:10:55', 0.00, '2025-04-07 15:10:49', '2025-04-07 15:10:49', 0);

-- ----------------------------
-- Table structure for contract_template
-- ----------------------------
DROP TABLE IF EXISTS `contract_template`;
CREATE TABLE `contract_template`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板名称',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '合同正文内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '合同模板表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of contract_template
-- ----------------------------
INSERT INTO `contract_template` VALUES (1, '标准住房租赁合同', '<h1 style=\"text-align: center;\">房屋租赁合同</h1>\r\n<p style=\"text-align: right;\">合同编号：${contractNo}</p>\r\n<p style=\"text-align: right;\">签订日期：${signDate}</p>\r\n\r\n<p>出租方（以下简称甲方）：<strong>${landlordName}</strong></p>\r\n<p>身份证号码：${landlordIdCard}</p>\r\n<p>联系电话：${landlordPhone}</p>\r\n\r\n<p>承租方（以下简称乙方）：<strong>${userName}</strong></p>\r\n<p>身份证号码：${userIdCard}</p>\r\n<p>联系电话：${userPhone}</p>\r\n\r\n<h3>第一条 租赁房屋基本情况</h3>\r\n<p>1.1 房屋坐落于${houseAddress}（以下简称该房屋）。</p>\r\n<p>1.2 该房屋建筑面积为${houseArea}平方米，房屋类型为${houseType}，朝向为${orientation}。</p>\r\n<p>1.3 该房屋装修情况为${decoration}，该房屋附属设施、设备状况详见合同附件一《房屋设备设施清单》。</p>\r\n\r\n<h3>第二条 租赁期限</h3>\r\n<p>2.1 租赁期共${leaseTerm}个月，自${startDate}起至${endDate}止。</p>\r\n<p>2.2 租赁期满后，如乙方要求继续租赁，应提前30天向甲方提出，协商一致后重新签订租赁合同。</p>\r\n\r\n<h3>第三条 租金及押金</h3>\r\n<p>3.1 该房屋月租金为人民币${monthlyRent}元整。</p>\r\n<p>3.2 租金支付方式：月付/季付/半年付/年付。</p>\r\n<p>3.3 乙方应于每月/季/半年/年的第一天前支付租金。</p>\r\n<p>3.4 押金：人民币${deposit}元整，合同终止时，如乙方无违约行为且按约定结清各项费用，甲方应全额退还押金。</p>\r\n\r\n<h3>第四条 房屋用途及要求</h3>\r\n<p>4.1 该房屋用途为居住，乙方不得擅自改变房屋用途。</p>\r\n<p>4.2 乙方保证遵守国家法律法规及当地政府的有关规定，不利用该房屋从事违法违规活动。</p>\r\n<p>4.3 乙方应爱护并合理使用房屋及其附属设施，如有损坏，应及时通知甲方并负责修复或经济赔偿。</p>\r\n\r\n<h3>第五条 相关费用的承担</h3>\r\n<p>5.1 在租赁期内，与该房屋有关的水费、电费、燃气费、网络费、物业管理费等费用由乙方承担。</p>\r\n<p>5.2 在租赁期内，该房屋及附属设施、设备保险费、房产税等由甲方承担。</p>\r\n\r\n<h3>第六条 合同的变更、解除与终止</h3>\r\n<p>6.1 经甲乙双方协商一致，可以变更或解除本合同。</p>\r\n<p>6.2 有下列情形之一的，甲方有权解除合同，收回房屋：</p>\r\n<p>&nbsp;&nbsp;a) 乙方擅自将房屋转租、分租或转让给第三方；</p>\r\n<p>&nbsp;&nbsp;b) 乙方利用该房屋从事违法经营活动；</p>\r\n<p>&nbsp;&nbsp;c) 乙方拖欠租金累计达30天以上；</p>\r\n<p>&nbsp;&nbsp;d) 乙方故意损坏房屋。</p>\r\n<p>6.3 在租赁期内，甲方如需提前收回房屋，应至少提前30天书面通知乙方，并与乙方协商补偿事宜。</p>\r\n<p>6.4 在租赁期内，乙方如需提前退租，应至少提前30天书面通知甲方，经甲方同意后办理退租手续。</p>\r\n\r\n<h3>第七条 违约责任</h3>\r\n<p>7.1 甲方违反本合同约定，未能及时交付该房屋或者交付的房屋不符合约定，乙方有权要求甲方按照月租金的30%支付违约金。</p>\r\n<p>7.2 乙方违反本合同约定，未能按时支付租金，除应补交租金外，还应按日支付应付而未付租金的0.5%作为违约金。</p>\r\n<p>7.3 乙方违反本合同约定，擅自将该房屋转租、分租或转让给第三方，甲方有权解除合同并要求乙方按照月租金的50%支付违约金。</p>\r\n<p>7.4 乙方在租赁期内提前退租的，应支付违约金${penaltyAmount}元。</p>\r\n\r\n<h3>第八条 争议解决方式</h3>\r\n<p>8.1 本合同履行中如发生争议，双方应协商解决；协商不成的，可向房屋所在地的人民法院提起诉讼。</p>\r\n\r\n<h3>第九条 其他约定事项</h3>\r\n<p>9.1 本合同未尽事宜，可由双方协商一致，签订补充协议。补充协议与本合同具有同等效力。</p>\r\n<p>9.2 本合同连同附件一经签字或盖章后生效。本合同一式两份，甲乙双方各执一份，具有同等法律效力。</p>\r\n\r\n<p>&nbsp;</p>\r\n<p>甲方（签字）：____________________&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;乙方（签字）：____________________</p>\r\n<p>&nbsp;</p>\r\n<p>签订日期：_______年_____月_____日&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;签订日期：_______年_____月_____日</p>', '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `contract_template` VALUES (2, '商铺租赁合同', '<h1 style=\"text-align: center;\">商铺租赁合同</h1>\r\n<p style=\"text-align: right;\">合同编号：${contractNo}</p>\r\n<p style=\"text-align: right;\">签订日期：${signDate}</p>\r\n\r\n<p>出租方（以下简称甲方）：<strong>${landlordName}</strong></p>\r\n<p>身份证号码：${landlordIdCard}</p>\r\n<p>联系电话：${landlordPhone}</p>\r\n\r\n<p>承租方（以下简称乙方）：<strong>${userName}</strong></p>\r\n<p>身份证号码：${userIdCard}</p>\r\n<p>联系电话：${userPhone}</p>\r\n\r\n<h3>第一条 租赁物业基本情况</h3>\r\n<p>1.1 商铺坐落于${houseAddress}（以下简称该商铺）。</p>\r\n<p>1.2 该商铺建筑面积为${houseArea}平方米。</p>\r\n<p>1.3 该商铺装修情况为${decoration}，该商铺附属设施、设备状况详见合同附件一《商铺设备设施清单》。</p>\r\n\r\n<h3>第二条 租赁期限</h3>\r\n<p>2.1 租赁期共${leaseTerm}个月，自${startDate}起至${endDate}止。</p>\r\n<p>2.2 租赁期满后，乙方享有同等条件下的优先承租权，如乙方要求继续租赁，应提前60天向甲方提出，协商一致后重新签订租赁合同。</p>\r\n\r\n<h3>第三条 租金及押金</h3>\r\n<p>3.1 该商铺月租金为人民币${monthlyRent}元整。</p>\r\n<p>3.2 租金支付方式：季付/半年付/年付。</p>\r\n<p>3.3 乙方应于每季/半年/年的第一天前支付租金。</p>\r\n<p>3.4 押金：人民币${deposit}元整，合同终止时，如乙方无违约行为且按约定结清各项费用，甲方应全额退还押金。</p>\r\n\r\n<h3>第四条 商铺用途及要求</h3>\r\n<p>4.1 该商铺用途为商业经营，乙方拟经营的业态为________________，不得擅自改变商铺约定用途。</p>\r\n<p>4.2 乙方保证遵守国家法律法规及当地政府的有关规定，具备从事相关经营活动的合法资质，并依法办理相关证照。</p>\r\n<p>4.3 乙方应爱护并合理使用商铺及其附属设施，如有损坏，应及时通知甲方并负责修复或经济赔偿。</p>\r\n\r\n<h3>第五条 相关费用的承担</h3>\r\n<p>5.1 在租赁期内，与该商铺有关的水费、电费、燃气费、网络费、物业管理费等费用由乙方承担。</p>\r\n<p>5.2 在租赁期内，该商铺及附属设施、设备保险费、房产税等由甲方承担。</p>\r\n<p>5.3 乙方应当自行办理经营所需的相关许可证照，相关费用由乙方承担。</p>\r\n\r\n<h3>第六条 装修与改造</h3>\r\n<p>6.1 乙方对商铺进行装修、改造，须事先征得甲方书面同意，并确保装修符合消防、环保等要求。</p>\r\n<p>6.2 租赁期满或合同解除时，除甲乙双方另有约定外，乙方应将商铺恢复原状后返还甲方。</p>\r\n\r\n<h3>第七条 合同的变更、解除与终止</h3>\r\n<p>7.1 经甲乙双方协商一致，可以变更或解除本合同。</p>\r\n<p>7.2 有下列情形之一的，甲方有权解除合同，收回商铺：</p>\r\n<p>&nbsp;&nbsp;a) 乙方擅自将商铺转租、分租或转让给第三方；</p>\r\n<p>&nbsp;&nbsp;b) 乙方利用该商铺从事违法经营活动；</p>\r\n<p>&nbsp;&nbsp;c) 乙方拖欠租金累计达30天以上；</p>\r\n<p>&nbsp;&nbsp;d) 乙方未经甲方同意对商铺进行结构性改造。</p>\r\n<p>7.3 在租赁期内，甲方如需提前收回商铺，应至少提前90天书面通知乙方，并向乙方支付相当于三个月租金的补偿金。</p>\r\n<p>7.4 在租赁期内，乙方如需提前退租，应至少提前60天书面通知甲方，经甲方同意后办理退租手续，并向甲方支付相当于两个月租金的违约金。</p>\r\n\r\n<h3>第八条 违约责任</h3>\r\n<p>8.1 甲方违反本合同约定，未能及时交付该商铺或者交付的商铺不符合约定，乙方有权要求甲方按照月租金的50%支付违约金。</p>\r\n<p>8.2 乙方违反本合同约定，未能按时支付租金，除应补交租金外，还应按日支付应付而未付租金的0.5%作为违约金。</p>\r\n<p>8.3 乙方违反本合同约定，擅自将该商铺转租、分租或转让给第三方，甲方有权解除合同并要求乙方按照月租金的三倍支付违约金。</p>\r\n\r\n<h3>第九条 不可抗力</h3>\r\n<p>9.1 因地震、台风、水灾、火灾等不可抗力原因导致无法履行合同的，彼此不承担违约责任，但应及时通知对方并提供相应证明。</p>\r\n<p>9.2 因不可抗力导致合同目的无法实现的，合同终止。</p>\r\n\r\n<h3>第十条 争议解决方式</h3>\r\n<p>10.1 本合同履行中如发生争议，双方应协商解决；协商不成的，可向商铺所在地的人民法院提起诉讼。</p>\r\n\r\n<h3>第十一条 其他约定事项</h3>\r\n<p>11.1 本合同未尽事宜，可由双方协商一致，签订补充协议。补充协议与本合同具有同等效力。</p>\r\n<p>11.2 本合同连同附件一经签字或盖章后生效。本合同一式两份，甲乙双方各执一份，具有同等法律效力。</p>\r\n\r\n<p>&nbsp;</p>\r\n<p>甲方（签字）：____________________&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;乙方（签字）：____________________</p>\r\n<p>&nbsp;</p>\r\n<p>签订日期：_______年_____月_____日&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;签订日期：_______年_____月_____日</p>', '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);

-- ----------------------------
-- Table structure for house
-- ----------------------------
DROP TABLE IF EXISTS `house`;
CREATE TABLE `house`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '房源ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `area` int NOT NULL COMMENT '面积（平方米）',
  `price` decimal(10, 2) NOT NULL COMMENT '月租价格',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '地址',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区域',
  `province_id` bigint NULL DEFAULT NULL COMMENT '省份ID',
  `city_id` bigint NULL DEFAULT NULL COMMENT '城市ID',
  `district_id` bigint NULL DEFAULT NULL COMMENT '区域ID',
  `bedroom_count` int NOT NULL COMMENT '卧室数量',
  `living_room_count` int NOT NULL COMMENT '客厅数量',
  `bathroom_count` int NOT NULL COMMENT '卫生间数量',
  `orientation` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '朝向',
  `floor` int NULL DEFAULT NULL COMMENT '楼层',
  `total_floor` int NULL DEFAULT NULL COMMENT '总楼层',
  `decoration` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '装修情况',
  `has_elevator` tinyint(1) NULL DEFAULT NULL COMMENT '是否有电梯',
  `has_parking` tinyint(1) NULL DEFAULT NULL COMMENT '是否有停车位',
  `house_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '房源类型：APARTMENT-公寓，HOUSE-别墅',
  `rent_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出租类型：WHOLE-整租，SHARED-合租',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态：PENDING-待审核，APPROVED-已上架，REJECTED-已拒绝，RENTED-已出租，OFFLINE-已下架',
  `reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片',
  `owner_id` bigint NOT NULL COMMENT '房东ID',
  `contract_template_id` bigint NULL DEFAULT NULL COMMENT '使用的合同模板ID',
  `min_lease_term` int NOT NULL DEFAULT 1 COMMENT '最短租期(月)',
  `deposit_months` int NOT NULL DEFAULT 1 COMMENT '押金月数',
  `penalty_rate` decimal(5, 2) NOT NULL DEFAULT 30.00 COMMENT '违约金比例（百分比）',
  `min_penalty` decimal(10, 2) NOT NULL DEFAULT 1000.00 COMMENT '最低违约金金额',
  `penalty_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '违约金',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_owner_id`(`owner_id` ASC) USING BTREE,
  INDEX `idx_city_district`(`city` ASC, `district` ASC) USING BTREE,
  INDEX `idx_province_city`(`province_id` ASC, `city_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '房源表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of house
-- ----------------------------
INSERT INTO `house` VALUES (20, '测试房源', '测试的房源', 100, 1528.00, '中央大街', '重庆市', '重庆市', '万州区', 7, 10, 29, 1, 1, 1, '东', 3, 5, '精装修', 1, 1, 'APARTMENT', 'WHOLE', 'RENTED', NULL, 'http://113.45.161.48:9000/zufang/house/fb1a55221dd1444e98506d74885a6726.jpg', 2, 1, 3, 3, 30.00, 1000.00, 0.00, '2025-04-05 15:49:06', '2025-04-07 15:11:05', 0);
INSERT INTO `house` VALUES (21, '短发书法大赛', '发大水放大放大', 1, 2.00, '放大放大放', '江苏省', '南京市', '鼓楼区', 4, 5, 13, 1, 1, 1, '东', 3, 4, '精装修', 0, 0, 'APARTMENT', 'WHOLE', 'APPROVED', NULL, 'http://113.45.161.48:9000/zufang/house/6a848281ef0b4f9c9239f2d6b5f788d0.png', 2, 1, 12, 1, 30.00, 1000.00, 0.00, '2025-04-05 16:13:51', '2025-04-07 14:27:33', 0);

-- ----------------------------
-- Table structure for house_comment
-- ----------------------------
DROP TABLE IF EXISTS `house_comment`;
CREATE TABLE `house_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `house_id` bigint NOT NULL COMMENT '房源ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父评论ID，如果是回复其他评论则填写',
  `root_id` bigint NULL DEFAULT NULL COMMENT '根评论ID，用于标识评论树',
  `reply_user_id` bigint NULL DEFAULT NULL COMMENT '被回复用户ID',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_house_id`(`house_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_root_id`(`root_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '房源评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of house_comment
-- ----------------------------
INSERT INTO `house_comment` VALUES (19, 20, 1, '这个房子真的可以', NULL, NULL, NULL, 0, '2025-04-05 07:50:23', '2025-04-05 07:50:23', 0);
INSERT INTO `house_comment` VALUES (20, 20, 4, 'sdafasdf ', NULL, NULL, NULL, 0, '2025-04-07 07:08:16', '2025-04-07 07:08:16', 0);
INSERT INTO `house_comment` VALUES (21, 21, 1, '111', NULL, NULL, NULL, 0, '2025-04-07 08:43:45', '2025-04-07 08:43:45', 0);

-- ----------------------------
-- Table structure for house_image
-- ----------------------------
DROP TABLE IF EXISTS `house_image`;
CREATE TABLE `house_image`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `house_id` bigint NOT NULL COMMENT '房源ID',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `is_cover` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否封面图片：0-否，1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_house_id`(`house_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '房源图片表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of house_image
-- ----------------------------
INSERT INTO `house_image` VALUES (20, 20, 'http://113.45.161.48:9000/zufang/house/effc0d7772ee4d839443567ff2116093.jpg', 0, 0, '2025-04-05 15:49:06', '2025-04-05 15:49:06', 0);
INSERT INTO `house_image` VALUES (21, 20, 'http://113.45.161.48:9000/zufang/house/e76219d49bc14f27997f2cc8959d4863.jpg', 1, 0, '2025-04-05 15:49:06', '2025-04-05 15:49:06', 0);
INSERT INTO `house_image` VALUES (22, 21, 'http://113.45.161.48:9000/zufang/house/7ce44066ea8d4274bbc42441bf2e623d.jpg', 0, 0, '2025-04-05 16:13:51', '2025-04-07 14:27:33', 0);
INSERT INTO `house_image` VALUES (23, 21, 'http://113.45.161.48:9000/zufang/house/6f8701f4a7f34fef8b7a0686387ce350.png', 1, 0, '2025-04-07 14:27:33', '2025-04-07 14:27:33', 0);
INSERT INTO `house_image` VALUES (24, 21, 'http://113.45.161.48:9000/zufang/house/6a848281ef0b4f9c9239f2d6b5f788d0.png', 0, 1, '2025-04-07 14:27:34', '2025-04-07 14:27:34', 0);

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `house_id` bigint NOT NULL COMMENT '房源ID',
  `user_id` bigint NOT NULL COMMENT '租客ID',
  `landlord_id` bigint NOT NULL COMMENT '房东ID',
  `start_date` date NOT NULL COMMENT '租期开始日期',
  `end_date` date NOT NULL COMMENT '租期结束日期',
  `monthly_rent` decimal(10, 2) NOT NULL COMMENT '月租金',
  `deposit` decimal(10, 2) NOT NULL COMMENT '押金',
  `service_fee` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '服务费',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '总金额',
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态：UNPAID-待支付，PAID-已支付，CANCELLED-已取消，PAYMENT_CANCELLED-取消支付，REFUNDING-退款中，REFUNDED-已退款，COMPLETED-已完成，TERMINATE_REQUESTED-申请退租，TERMINATED-已退租',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `pay_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付方式',
  `transaction_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '交易流水号',
  `cancel_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取消原因',
  `terminate_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '退租原因',
  `terminate_request_time` datetime NULL DEFAULT NULL COMMENT '退租申请时间',
  `expected_terminate_date` date NULL DEFAULT NULL COMMENT '期望退租日期',
  `actual_terminate_date` date NULL DEFAULT NULL COMMENT '实际退租日期',
  `terminate_reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '退租拒绝原因',
  `terminate_time` datetime NULL DEFAULT NULL COMMENT '退租时间',
  `penalty_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '违约金',
  `is_penalty_paid` tinyint(1) NULL DEFAULT 0 COMMENT '违约金是否已支付',
  `penalty_pay_time` datetime NULL DEFAULT NULL COMMENT '违约金支付时间',
  `penalty_pay_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '违约金支付方式',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_house_id`(`house_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_landlord_id`(`landlord_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order
-- ----------------------------
INSERT INTO `order` VALUES (11, 'ORD20250407F8A0568F', 20, 1, 2, '2025-04-07', '2025-07-06', 1528.00, 4584.00, 30.56, 9198.56, 'PAYMENT_CANCELLED', NULL, NULL, NULL, '用户取消支付', NULL, NULL, NULL, NULL, NULL, NULL, 0.00, 0, NULL, NULL, NULL, '2025-04-07 14:19:48', '2025-04-07 14:20:07', 0);
INSERT INTO `order` VALUES (12, 'ORD20250407D910C01E', 20, 1, 2, '2025-04-07', '2025-07-06', 1528.00, 4584.00, 30.56, 9198.56, 'TERMINATED', '2025-04-07 14:21:34', 'BANK', 'TRX202504071421347BCF4C71-013', NULL, '1', '2025-04-07 14:25:34', '2025-04-10', '2025-04-07', '不准', '2025-04-07 14:25:50', 1329.36, 1, '2025-04-07 14:26:07', 'ONLINE', '实际租期: 23/90 天, 剩余天数: 67 天', '2025-04-07 14:21:26', '2025-04-07 14:25:34', 0);
INSERT INTO `order` VALUES (13, 'ORD2025040794F8D6EA', 20, 4, 2, '2025-04-07', '2025-07-06', 1528.00, 4584.00, 30.56, 9198.56, 'UNPAID', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.00, 0, NULL, NULL, NULL, '2025-04-07 15:10:03', '2025-04-07 15:10:03', 0);
INSERT INTO `order` VALUES (14, 'ORD20250407AC6897FB', 20, 4, 2, '2025-04-07', '2025-07-06', 1528.00, 4584.00, 30.56, 9198.56, 'PAID', '2025-04-07 15:11:05', 'WECHAT', 'TRX2025040715110536FE87F2-83B', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0.00, 0, NULL, NULL, NULL, '2025-04-07 15:10:47', '2025-04-07 15:10:47', 0);

-- ----------------------------
-- Table structure for region_city
-- ----------------------------
DROP TABLE IF EXISTS `region_city`;
CREATE TABLE `region_city`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '城市ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '城市名称',
  `code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '城市代码',
  `province_id` bigint NOT NULL COMMENT '所属省份ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_code`(`code` ASC) USING BTREE,
  INDEX `idx_province_id`(`province_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '城市表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of region_city
-- ----------------------------
INSERT INTO `region_city` VALUES (1, '北京市', '110100', 1, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_city` VALUES (2, '上海市', '310100', 2, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_city` VALUES (3, '广州市', '440100', 3, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_city` VALUES (4, '深圳市', '440300', 3, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_city` VALUES (5, '南京市', '320100', 4, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_city` VALUES (6, '苏州市', '320500', 4, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_city` VALUES (7, '杭州市', '330100', 5, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_city` VALUES (8, '宁波市', '330200', 5, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_city` VALUES (9, '成都市', '510100', 6, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_city` VALUES (10, '重庆市', '500100', 7, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_city` VALUES (11, '嘉兴市', '330嘉兴6187', 5, '2025-04-05 15:44:46', '2025-04-05 07:44:55', 1);
INSERT INTO `region_city` VALUES (12, '烟台市', '山东省烟台1277', 10, '2025-04-07 14:29:01', '2025-04-07 14:29:01', 0);

-- ----------------------------
-- Table structure for region_district
-- ----------------------------
DROP TABLE IF EXISTS `region_district`;
CREATE TABLE `region_district`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '区域ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区域名称',
  `code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区域代码',
  `city_id` bigint NOT NULL COMMENT '所属城市ID',
  `province_id` bigint NOT NULL COMMENT '所属省份ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_code`(`code` ASC) USING BTREE,
  INDEX `idx_city_id`(`city_id` ASC) USING BTREE,
  INDEX `idx_province_id`(`province_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '区域表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of region_district
-- ----------------------------
INSERT INTO `region_district` VALUES (1, '朝阳区', '110105', 1, 1, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (2, '海淀区', '110108', 1, 1, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (3, '西城区', '110102', 1, 1, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (4, '东城区', '110101', 1, 1, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (5, '浦东新区', '310115', 2, 2, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (6, '徐汇区', '310104', 2, 2, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (7, '静安区', '310106', 2, 2, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (8, '天河区', '440106', 3, 3, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (9, '越秀区', '440104', 3, 3, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (10, '海珠区', '440105', 3, 3, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (11, '福田区', '440304', 4, 3, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (12, '南山区', '440305', 4, 3, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (13, '鼓楼区', '320106', 5, 4, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (14, '姑苏区', '320508', 6, 4, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (15, '西湖区', '330106', 7, 5, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (16, '江北区', '330205', 8, 5, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (17, '武侯区', '510107', 9, 6, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (18, '渝中区', '500103', 10, 7, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (19, '渝北区', '500112', 10, 7, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (20, '江北区', '500105', 10, 7, '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_district` VALUES (21, '怀柔区', '110怀柔377', 1, 1, '2025-04-05 15:08:40', '2025-04-05 15:08:40', 0);
INSERT INTO `region_district` VALUES (22, '密云区', '110密云8690', 1, 1, '2025-04-05 15:10:59', '2025-04-07 06:28:28', 1);
INSERT INTO `region_district` VALUES (23, '巴南区', '500巴南151', 10, 7, '2025-04-05 15:13:10', '2025-04-05 15:13:10', 0);
INSERT INTO `region_district` VALUES (24, '开州区', '500大渡1898', 10, 7, '2025-04-05 15:13:22', '2025-04-05 07:42:40', 1);
INSERT INTO `region_district` VALUES (25, '九龙坡区', '500九龙9944', 10, 7, '2025-04-05 15:16:10', '2025-04-05 07:37:35', 1);
INSERT INTO `region_district` VALUES (26, '开州区', '500开州8621', 10, 7, '2025-04-05 15:20:19', '2025-04-05 07:36:01', 1);
INSERT INTO `region_district` VALUES (27, '万州区', '500万州1421', 10, 7, '2025-04-05 15:22:41', '2025-04-05 07:34:23', 1);
INSERT INTO `region_district` VALUES (28, '开州区', '500开州7727', 10, 7, '2025-04-05 15:37:48', '2025-04-05 07:40:28', 1);
INSERT INTO `region_district` VALUES (29, '万州区', '500开州3513', 10, 7, '2025-04-05 15:42:54', '2025-04-05 15:47:34', 0);
INSERT INTO `region_district` VALUES (30, '万州区', '500万州1258', 10, 7, '2025-04-05 15:47:11', '2025-04-05 07:47:27', 1);
INSERT INTO `region_district` VALUES (31, 'xx区', '山东省xx4519', 12, 10, '2025-04-07 14:29:15', '2025-04-07 14:29:15', 0);

-- ----------------------------
-- Table structure for region_province
-- ----------------------------
DROP TABLE IF EXISTS `region_province`;
CREATE TABLE `region_province`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '省份ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省份名称',
  `code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省份代码',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '省份表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of region_province
-- ----------------------------
INSERT INTO `region_province` VALUES (1, '北京市', '110000', '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_province` VALUES (2, '上海市', '310000', '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_province` VALUES (3, '广东省', '440000', '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_province` VALUES (4, '江苏省', '320000', '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_province` VALUES (5, '浙江省', '330000', '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_province` VALUES (6, '四川省', '510000', '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_province` VALUES (7, '重庆市', '500000', '2025-04-05 10:00:00', '2025-04-05 10:00:00', 0);
INSERT INTO `region_province` VALUES (8, '怀柔区', '怀柔区7107', '2025-04-05 14:49:28', '2025-04-05 06:53:08', 1);
INSERT INTO `region_province` VALUES (9, '密云区', '密云区588', '2025-04-05 14:50:01', '2025-04-05 06:53:11', 1);
INSERT INTO `region_province` VALUES (10, '山东省', '山东省9853', '2025-04-07 14:28:40', '2025-04-07 14:28:40', 0);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色：ADMIN-管理员，LANDLORD-房东，USER-用户',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-正常，LOCKED-锁定，INACTIVE-未激活',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人简介',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '111', '111111', '111的昵称', 'http://113.45.161.48:9000/zufang/avatar/9ad2046a68da4519859dc319ad5fe892.jpg', '13384403671', '2237701658@qq.com', 'USER', '50023420030928243X', '何鸿涛', 'ACTIVE', '一般', '2025-04-03 14:22:11', '2025-04-08 20:40:51', 0);
INSERT INTO `user` VALUES (2, '1111', '111111', '1111', 'http://113.45.161.48:9000/zufang/avatar/34552beb4dc9424f8e6006f607871cac.png', '18723577492', '3401611059@qq.com', 'LANDLORD', '500234200309282435', '李白', 'ACTIVE', '我是房东', '2025-04-03 14:22:29', '2025-04-05 13:16:18', 0);
INSERT INTO `user` VALUES (3, 'admin', 'admin', NULL, NULL, NULL, NULL, 'ADMIN', NULL, NULL, 'ACTIVE', NULL, '2025-04-03 06:23:12', '2025-04-03 06:23:12', 0);
INSERT INTO `user` VALUES (4, '123456', '123456', '123456', NULL, '17623772904', '343502@qq.com', 'USER', '500333203929102', '1232', 'ACTIVE', 'ss', '2025-04-07 15:07:16', '2025-04-07 15:10:36', 0);

-- ----------------------------
-- Table structure for zf_feature_toggle
-- ----------------------------
DROP TABLE IF EXISTS `zf_feature_toggle`;
CREATE TABLE `zf_feature_toggle`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '功能开关ID',
  `feature_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '功能键名',
  `feature_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '功能名称',
  `enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '功能描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_feature_key`(`feature_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '功能开关表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of zf_feature_toggle
-- ----------------------------

-- ----------------------------
-- Table structure for zf_message
-- ----------------------------
DROP TABLE IF EXISTS `zf_message`;
CREATE TABLE `zf_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息类型（SYSTEM：系统消息；APPOINTMENT：预约消息；ORDER：订单消息）',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  `reference_id` bigint NULL DEFAULT NULL COMMENT '关联ID（比如订单ID，预约ID等）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_global` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否全局消息',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_reference_id`(`reference_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of zf_message
-- ----------------------------
INSERT INTO `zf_message` VALUES (13, 1, '测试', '测试', 'SYSTEM', 1, NULL, '2025-04-09 05:19:16', '2025-04-09 05:21:50', 0);
INSERT INTO `zf_message` VALUES (14, 2, '测试', '测试', 'SYSTEM', 0, NULL, '2025-04-09 05:19:16', '2025-04-09 05:19:16', 0);
INSERT INTO `zf_message` VALUES (15, 3, '测试', '测试', 'SYSTEM', 0, NULL, '2025-04-09 05:19:16', '2025-04-09 05:19:16', 0);
INSERT INTO `zf_message` VALUES (16, 4, '测试', '测试', 'SYSTEM', 0, NULL, '2025-04-09 05:19:16', '2025-04-09 05:19:16', 0);
INSERT INTO `zf_message` VALUES (17, 0, '阿斯蒂芬', '阿斯蒂芬阿斯蒂芬', 'SYSTEM', 0, NULL, '2025-04-09 13:29:07', '2025-04-09 13:29:07', 1);

-- ----------------------------
-- Table structure for zf_message_read_status
-- ----------------------------
DROP TABLE IF EXISTS `zf_message_read_status`;
CREATE TABLE `zf_message_read_status`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `message_id` bigint NOT NULL COMMENT '消息ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `is_read` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_user`(`message_id` ASC, `user_id` ASC) USING BTREE COMMENT '消息用户唯一索引',
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_message_id`(`message_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息阅读状态表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of zf_message_read_status
-- ----------------------------

-- ----------------------------
-- Table structure for zf_system_setting
-- ----------------------------
DROP TABLE IF EXISTS `zf_system_setting`;
CREATE TABLE `zf_system_setting`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设置ID',
  `system_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '系统名称',
  `system_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统描述',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系邮箱',
  `icp` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备案信息',
  `logo_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统Logo URL',
  `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统版本',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统设置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of zf_system_setting
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
