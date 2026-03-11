-- 初始化 HR 系统种子数据

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 清空数据
TRUNCATE TABLE `hr_employee`;
TRUNCATE TABLE `hr_position`;
TRUNCATE TABLE `hr_position_level`;
TRUNCATE TABLE `hr_position_category`;
TRUNCATE TABLE `hr_organization`;

-- ----------------------------
-- 插入组织架构 (1集团 2公司 3事业部 4中心 5部门 6团队)
-- ----------------------------
INSERT INTO `hr_organization` VALUES (100, 0, '0', '星辰科技集团', 'XCKJ', '1', 1, 1, '400-888-9999', '星辰大厦', '2010-01-01', 1, '0', 100, 'admin', sysdate(), '', null);
INSERT INTO `hr_organization` VALUES (200, 100, '0,100', '北京星辰科技有限公司', 'BJXC', '2', 2, 2, '010-12345678', '北京朝阳区星辰大厦', '2010-01-01', 1, '0', 101, 'admin', sysdate(), '', null);
INSERT INTO `hr_organization` VALUES (201, 100, '0,100', '上海星辰科技有限公司', 'SHXC', '2', 2, 3, '021-12345678', '上海浦东新区星辰大厦', '2015-01-01', 2, '0', 102, 'admin', sysdate(), '', null);

-- 部门 - 研发中心
INSERT INTO `hr_organization` VALUES (300, 200, '0,100,200', '研发中心', 'RD', '4', 3, 4, '010-12345678', '北京朝阳区星辰大厦研发部', '2010-01-01', 1, '0', 103, 'admin', sysdate(), '', null);
-- 部门 - 产品中心
INSERT INTO `hr_organization` VALUES (301, 200, '0,100,200', '产品中心', 'PD', '4', 3, 5, '010-12345678', '北京朝阳区星辰大厦产品部', '2010-01-01', 2, '0', 104, 'admin', sysdate(), '', null);
-- 部门 - 人力资源部
INSERT INTO `hr_organization` VALUES (302, 200, '0,100,200', '人力资源部', 'HR', '5', 3, 6, '010-12345678', '北京朝阳区星辰大厦HR部', '2010-01-01', 3, '0', 105, 'admin', sysdate(), '', null);

-- 团队
INSERT INTO `hr_organization` VALUES (400, 300, '0,100,200,300', '前端开发一组', 'F1', '6', 4, 7, '010-12345678', '研发区域', '2010-01-01', 1, '0', 106, 'admin', sysdate(), '', null);
INSERT INTO `hr_organization` VALUES (401, 300, '0,100,200,300', '后端开发一组', 'B1', '6', 4, 8, '010-12345678', '研发区域', '2010-01-01', 2, '0', 107, 'admin', sysdate(), '', null);

-- ----------------------------
-- 插入职族
-- ----------------------------
INSERT INTO `hr_position_category` VALUES (1, '技术族', 'T', '从事技术研发、测试、运维等相关工作', '0', 'admin', sysdate(), '', null);
INSERT INTO `hr_position_category` VALUES (2, '管理族', 'M', '从事团队管理、项目管理等工作', '0', 'admin', sysdate(), '', null);
INSERT INTO `hr_position_category` VALUES (3, '职能族', 'A', '从事人力资源、财务、行政等工作', '0', 'admin', sysdate(), '', null);
INSERT INTO `hr_position_category` VALUES (4, '产品族', 'P', '从事产品设计、运营等工作', '0', 'admin', sysdate(), '', null);

-- ----------------------------
-- 插入职级
-- ----------------------------
INSERT INTO `hr_position_level` VALUES (1, 1, '初级工程师 (T1)', 'T1', 1, 8000.00, 15000.00, '应届生或入门级技术人员', '0', 'admin', sysdate(), '', null);
INSERT INTO `hr_position_level` VALUES (2, 1, '中级工程师 (T2)', 'T2', 2, 15000.00, 25000.00, '核心业务开发', '0', 'admin', sysdate(), '', null);
INSERT INTO `hr_position_level` VALUES (3, 1, '高级工程师 (T3)', 'T3', 3, 25000.00, 40000.00, '系统架构设计及核心研发', '0', 'admin', sysdate(), '', null);
INSERT INTO `hr_position_level` VALUES (4, 2, '主管 (M1)', 'M1', 4, 20000.00, 35000.00, '基层管理', '0', 'admin', sysdate(), '', null);
INSERT INTO `hr_position_level` VALUES (5, 2, '经理 (M2)', 'M2', 5, 30000.00, 50000.00, '中层管理', '0', 'admin', sysdate(), '', null);
INSERT INTO `hr_position_level` VALUES (6, 3, '专员 (A1)', 'A1', 1, 6000.00, 12000.00, '基础职能工作', '0', 'admin', sysdate(), '', null);
INSERT INTO `hr_position_level` VALUES (7, 4, '产品经理 (P2)', 'P2', 2, 15000.00, 30000.00, '负责独立产品线', '0', 'admin', sysdate(), '', null);

-- ----------------------------
-- 插入岗位
-- ----------------------------
INSERT INTO `hr_position` VALUES (1, '前端开发工程师', 'FED', 1, 300, 10, 5, '负责Web前端及小程序开发', '熟练掌握Vue, React等主流框架', '0', 4, 'admin', sysdate(), '', null);
INSERT INTO `hr_position` VALUES (2, 'Java后端开发工程师', 'BED', 1, 300, 15, 8, '负责核心业务后端开发', '扎实的Java基础，熟悉Spring Boot/Cloud生态', '0', 4, 'admin', sysdate(), '', null);
INSERT INTO `hr_position` VALUES (3, '研发总监', 'RD_D', 2, 300, 1, 1, '负责研发团队整体规划与管理', '10年以上研发及管理经验', '0', 2, 'admin', sysdate(), '', null);
INSERT INTO `hr_position` VALUES (4, '产品经理', 'PROD_M', 4, 301, 5, 3, '负责产品生命周期管理', '敏锐的市场洞察力及出色的逻辑思维', '0', 4, 'admin', sysdate(), '', null);
INSERT INTO `hr_position` VALUES (5, 'HR专员', 'HR_S', 3, 302, 3, 2, '负责员工入转调离等日常人事业务', '良好的沟通能力和亲和力', '0', 4, 'admin', sysdate(), '', null);

-- ----------------------------
-- 插入员工数据
-- ----------------------------
INSERT INTO `hr_employee` VALUES (1, '90001', 1, '若依管理员', '0', '110105199001011234', '1990-01-01', '15888888888', 'ry@qq.com', NULL, 100, 3, 5, NULL, '1', '2', '2015-01-01', '2015-04-01', '2015-04-01', NULL, NULL, '北京', '汉族', '北京', '0', '中共党员', '本科', 'admin', sysdate(), '', null);
INSERT INTO `hr_employee` VALUES (2, '90002', 2, '张三', '0', '110105199201011234', '1992-01-01', '13800138000', 'zhangsan@xckj.com', NULL, 200, 3, 4, 1, '1', '2', '2016-05-01', '2016-08-01', '2016-08-01', NULL, NULL, '北京', '汉族', '河北', '1', '群众', '本科', 'admin', sysdate(), '', null);
INSERT INTO `hr_employee` VALUES (3, '90003', NULL, '李四', '1', '110105199501011234', '1995-01-01', '13800138001', 'lisi@xckj.com', NULL, 302, 5, 6, 2, '1', '2', '2018-07-01', '2018-10-01', '2018-10-01', NULL, NULL, '北京', '回族', '宁夏', '0', '群众', '本科', 'admin', sysdate(), '', null);
INSERT INTO `hr_employee` VALUES (4, '90004', NULL, '王五', '0', '110105199011011234', '1990-11-01', '13800138002', 'wangwu@xckj.com', NULL, 300, 3, 5, 2, '1', '2', '2017-03-01', '2017-06-01', '2017-06-01', NULL, NULL, '北京', '汉族', '山东', '1', '群众', '硕士', 'admin', sysdate(), '', null);
INSERT INTO `hr_employee` VALUES (5, '90005', NULL, '赵六', '0', '110105199601011234', '1996-01-01', '13800138003', 'zhaoliu@xckj.com', NULL, 400, 1, 2, 4, '1', '2', '2020-07-01', '2020-10-01', '2020-10-01', NULL, NULL, '北京', '汉族', '河南', '0', '中共党员', '本科', 'admin', sysdate(), '', null);
INSERT INTO `hr_employee` VALUES (6, '90006', NULL, '钱七', '1', '110105199801011234', '1998-01-01', '13800138004', 'qianqi@xckj.com', NULL, 401, 2, 1, 4, '2', '1', '2024-03-01', '2024-06-01', NULL, NULL, NULL, '北京', '满族', '辽宁', '0', '共青团员', '本科', 'admin', sysdate(), '', null);
INSERT INTO `hr_employee` VALUES (7, '90007', NULL, '孙八', '0', '110105199401011234', '1994-01-01', '13800138005', 'sunba@xckj.com', NULL, 301, 4, 7, 2, '1', '2', '2019-05-01', '2019-08-01', '2019-08-01', NULL, NULL, '北京', '汉族', '江苏', '1', '群众', '硕士', 'admin', sysdate(), '', null);

SET FOREIGN_KEY_CHECKS = 1;

