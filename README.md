# 环境监测数据采集与环保公众监督系统

> 面向**公众监督员 / 网格员 / 系统管理员**三端的环保监管系统。围绕「污染线索上报 → 后台指派 → 现场实测 → 处置办结 → 数据大屏」构建完整业务闭环。

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.6-brightgreen)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3-42b883)](https://vuejs.org/)
[![Tests](https://img.shields.io/badge/tests-30%20passed-success)](#测试)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

---

## 目录

- [项目背景](#项目背景)
- [功能概览](#功能概览)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [快速开始](#快速开始)
- [演示账号](#演示账号)
- [关键设计决策](#关键设计决策)
- [数据模型](#数据模型)
- [接口说明](#接口说明)
- [测试](#测试)
- [项目结构](#项目结构)
- [后续规划](#后续规划)

---

## 项目背景

公众发现空气污染问题后缺乏便捷的上报渠道；监管部门接到线索后，也缺少「谁去核查、核查结果如何、是否办结」的闭环管理手段。

本系统把这条链路数字化：

1. **公众监督员**发现污染问题，选择所属城市、填写位置与描述后提交反馈；
2. **系统管理员**查看反馈，按「属地优先、跨省兜底」规则指派给对应网格的**网格员**；
3. **网格员**到现场实测 SO₂ / CO / PM2.5 等污染物浓度并上报；
4. 服务端依据国标计算 **AQI 与空气质量等级**；
5. **管理员**填写处理回复并办结或驳回，公众监督员可查看处理结果；
6. 全程数据汇总为**可视化监管大屏**（污染物分布、AQI 分布、反馈趋势、区域排行）。

---

## 功能概览

### 公众监督员端

- 注册 / 登录（账号与手机号唯一，密码 BCrypt 加密存储）
- 提交污染反馈：城市、污染类型、地址、经纬度、问题描述
- 查看自己提交的反馈及**管理员处理回复**

### 系统管理员端

- 反馈列表：按城市、状态筛选 + 分页
- 反馈详情：基础信息、处置与检测信息、处理结果
- 指派网格员（本市指派 / 外省调派，含候选人与可用性提示）
- 处理反馈：填写回复并**办结**或**驳回**
- 可视化监管大屏

### 网格员端（移动端）

- 工号登录
- 待办任务列表 / 任务详情
- 上报实测数据（SO₂、CO、PM2.5，可选 PM10、NO₂、O₃）

---

## 技术栈

| 层次 | 技术选型 |
| --- | --- |
| 后端框架 | Java 17、Spring Boot 3.3.6、Spring Web、Spring Data JPA、Bean Validation |
| 安全 | JJWT 0.12.6（HS256 无状态令牌）、BCrypt（spring-security-crypto）、自定义拦截器鉴权 |
| 数据库 | H2（开发/演示，内存库）、MySQL（`db/schema.sql` 可直接建库）；Hibernate 自动建表 |
| 前端 | Vue 3、Vite、Vue Router、Pinia |
| UI | Element Plus（管理端 PC）、Vant（网格员/公众端移动端） |
| 可视化 | ECharts（含中国地图 `china.json`） |
| 构建/工具 | Maven 3.9、npm、Git |

---

## 系统架构

```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  管理端 (PC)     │  │ 网格员端 (移动)  │  │ 公众监督员端     │
│  Element Plus   │  │     Vant        │  │     Vant        │
│  反馈列表/大屏   │  │  待办/实测上报   │  │  注册/提交/我的  │
└────────┬────────┘  └────────┬────────┘  └────────┬────────┘
         │                    │                    │
         └────────────────────┼────────────────────┘
                              │  HTTP / JSON
                    ┌─────────▼──────────┐
                    │   AuthInterceptor  │  JWT 校验 · 角色授权 · 401/403
                    └─────────┬──────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
┌───────▼───────┐   ┌─────────▼─────────┐   ┌───────▼────────┐
│ 反馈与任务服务 │   │   字典与计算服务   │   │  统计服务       │
│ 上报/指派/办结 │   │ 字典缓存/AQI 计算  │   │ 大屏聚合查询    │
└───────┬───────┘   └─────────┬─────────┘   └───────┬────────┘
        │                     │                     │
        └─────────────────────┼─────────────────────┘
                    ┌─────────▼──────────┐
                    │  Spring Data JPA   │
                    └─────────┬──────────┘
                    ┌─────────▼──────────┐
                    │  H2 / MySQL（8 张表）│
                    └────────────────────┘
```

---

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 18+（含 npm）

### 1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

服务启动在 `http://localhost:8080`，H2 控制台 `http://localhost:8080/h2-console`。

> 开发环境使用 H2 **内存库**，每次重启自动重建表并写入种子数据（无需手动建库）。
> JDBC：`jdbc:h2:mem:env_supervision`，用户名 `sa`，密码为空。

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173`。前端默认请求 `http://localhost:8080/api`，后端已放行 `5173` 端口的跨域请求。

### 3. 切换 MySQL（可选）

```bash
mysql -u root -p < db/schema.sql
```

然后修改 `backend/src/main/resources/application.yml` 的 `spring.datasource` 配置，并把 `spring.jpa.hibernate.ddl-auto` 改为 `none`。

### 4. 配置 JWT 密钥（部署时必做）

仓库内的默认密钥仅供本地演示，**部署时请通过环境变量覆盖**：

```bash
# Linux / macOS
export SUPERVISION_JWT_SECRET=$(openssl rand -base64 48)

# Windows PowerShell
$env:SUPERVISION_JWT_SECRET = [Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Max 256 }))
```

---

## 演示账号

| 角色 | 登录入口 | 账号 | 密码 |
| --- | --- | --- | --- |
| 系统管理员 | `http://localhost:5173/login` | `admin` | `123` |
| 网格员 | `http://localhost:5173/grid/login` | `SY-001` | `123` |
| 公众监督员 | `http://localhost:5173/public/login` | `supervisor001` | `123456` |

> 网格员工号可选：`SY-001`、`SY-002`（沈阳）、`DL-001`（大连）、`BJ-001`（北京）、`TJ-001`（天津）；
> 公众监督员可选：`supervisor001` ~ `supervisor003`，密码均为 `123456`。
> 以上账号与数据均为演示用的虚构数据，不对应任何真实个人。

---

## 关键设计决策

以下 5 个问题是开发中真实遇到并解决的，也是本项目的核心工程价值所在。

### 1. AQI 判定收归服务端（原由前端计算）

**问题**：初版由前端按硬编码阈值判断等级，并把「轻度污染」这样的**字符串**提交给后端，后端照单落库。这意味着上报方可以任意伪造等级，而统计大屏的所有图表都建立在这份不可信数据上；同时 AQI 数值本身没有落库。

**方案**：

- 服务端只接收污染物**浓度值**，`GridMeasurementSubmitRequest` 中不再有等级字段；
- 新增 `AqiCalculator`：按各污染物浓度限值分段，**线性插值**求单项 IAQI，取三者最大值作为 AQI，再按 `aqi_level` 表的 `min_aqi` / `max_aqi` 区间定级；
- AQI 数值落库到 `aqi_value`；
- 补齐浓度合理性校验（超限拒绝）与超限封顶（IAQI 500）。

**两个容易踩的边界**：

- 级别区间**不等宽**：`aqi_level` 第五档（201–300）宽度是其余档的两倍，用等宽公式推算会把 IAQI 250 判错级别；
- 浓度**恰好等于限值**时，IAQI 取该段起点而非段终点（例如 PM2.5 = 250 → IAQI 250）。

配套 10 个单元测试覆盖分段边界、段内插值、封顶、级别映射与异常入参。

### 2. 重构业务流程，使状态机真正成立

**问题**：初版流程是「网格员提交实测 → 反馈直接置为已完成」。这导致管理员全程没有决策动作、`已驳回` 状态从未被使用——一个「监督系统」里管理员只能转发，流程在语义上不成立。

**方案**：把「实测」与「办结」拆成两项职责，状态机覆盖 4 个状态：

```
待处理(0) ──指派网格员──▶ 处理中(1) ──管理员办结──▶ 已完成(2)
                              └──────管理员驳回──▶ 已驳回(3)
```

- 网格员负责**录入实测数据**，提交后仍停留在「处理中」；
- 管理员填写回复后**办结或驳回**，进入终态；
- 终态不可逆，重复处理返回明确语义的错误（400）；
- 状态校验放在归属校验**之前**，否则对已完成任务重复提交会误报「任务未指派给该网格员」。

### 3. 身份冒用治理（无鉴权接口）

**问题**：初版没有任何鉴权层，且身份以**请求参数**传入（`gridMemberId`、`supervisorId`）。任何人都能查看全部反馈，或冒用他人身份提交实测数据/反馈。

**方案**：

- 引入 JWT（HS256，载荷含用户编号与角色）+ `AuthInterceptor` 统一鉴权；
- 按路径前缀做**角色授权**，未登记路径默认拒绝；401（未认证）与 403（越权）语义分离；
- **身份一律从令牌解析**，彻底移除请求参数形式的身份标识；
- 密码统一改为 **BCrypt 摘要**存储；
- 全局异常处理统一输出错误码与文案。

> 细节：共享字典接口（`/api/grid-cities`、`/api/aqi-levels`）三种角色都要用，但路径本身推断不出唯一角色——`/grid-cities` 会先命中 `/grid` 规则而被判定为网格员身份，导致公众监督员 401。因此共享字典接口改用「当前有效身份」取令牌，而非按路径绑定角色。

### 4. 派单规则落到服务端

**问题**：网格员按市域配置，部分城市没有在岗人员，单一指派规则无法覆盖；且规则若放在前端可被绕过。

**方案**：实现「属地优先 + 跨省兜底」判定并全部落在服务层：

- 本市存在在岗网格员时，**禁止**发起外省调派；
- 跨省调派必须选择**外省**在岗人员；
- 本地指派必须选择**同城**网格员；
- 字典缺失时**显式拒绝**——原实现以伪省份 ID `0` 兜底，会让跨省判定静默错判。

### 5. 消除硬编码字典与数据一致性处理

**问题**：城市名、省份映射、处理人姓名等原先以 `Map` 散落在业务类中，而数据库里已建的字典表完全没被使用；新增一个城市必须改 Java 代码。

**方案**：

- 落地 7 张业务表并实现 `DictionaryService`（惰性加载 + 内存缓存）替换全部硬编码映射；
- 实测数据**双写**：`public_feedback` 保留冗余字段便于详情直接展示，`air_quality_stat` 作为权威明细，支持同一反馈**多次复测**；
- 统计查询用 JPQL 连表一次取出城市、网格员与级别名称，避免逐条回查。

> 补充修掉的工程问题：Maven 未显式指定源码编码，导致 native SQL 中的中文字面量编译成乱码，已在 `pom.xml` 固定 `UTF-8`。

---

## 数据模型

共 8 张表，实际使用 7 张业务表：

| 表名 | 说明 |
| --- | --- |
| `public_supervisor` | 公众监督员账号 |
| `sys_admin` | 系统管理员账号（含 `last_login_time`、`role_name`） |
| `grid_member` | 网格员（所属省/市、责任网格、在岗状态） |
| `grid_province` / `grid_city` | 省 / 市字典（行政区划编码，市级关联省级） |
| `aqi_level` | AQI 六级标准（指数区间、健康影响、防护建议） |
| `public_feedback` | 反馈主表（状态机、指派信息、处理结果、冗余实测字段） |
| `air_quality_stat` | 实测数据明细（六项污染物、AQI、级别、上报人、监测时间） |

关系要点：

- 一条反馈可对应**多条**监测数据（复测）；
- 反馈状态：`0 待处理 / 1 处理中 / 2 已完成 / 3 已驳回`；
- 字典表数据由 `DataInitializer` 在启动时写入，`DictionaryService` 缓存并提供按 ID 反查。

---

## 接口说明

统一响应结构：

```json
{ "code": 200, "message": "success", "data": {} }
```

鉴权：除登录接口与公众端注册外，均需请求头 `Authorization: Bearer <token>`。

| 路径前缀 | 允许角色 |
| --- | --- |
| `/api/auth/**`、`POST /api/public-supervisors` | 公开 |
| `/api/feedback/**`、`/api/statistics/**` | 管理员 |
| `/api/grid/**` | 网格员 |
| `/api/public-feedback/**`、`/api/public-supervisors/me` | 公众监督员 |
| `/api/grid-cities`、`/api/aqi-levels`、`/api/air-quality-stats` | 三种角色均可 |

主要接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/admin/login` | 管理员登录 |
| POST | `/api/auth/grid-member/login` | 网格员登录 |
| POST | `/api/auth/public-supervisor/login` | 公众监督员登录 |
| POST | `/api/public-supervisors` | 公众监督员注册 |
| GET | `/api/feedback` | 反馈分页（支持 `cityId`、`status` 筛选） |
| GET | `/api/feedback/{afId}` | 反馈详情 |
| GET | `/api/feedback/{afId}/assignment-candidates/local` | 本地指派候选人 |
| GET | `/api/feedback/{afId}/assignment-candidates/cross-province` | 外省调派候选人 |
| PUT | `/api/feedback/{afId}/assign` | 指派网格员 |
| PUT | `/api/feedback/{afId}/handle` | 处理反馈（办结 / 驳回） |
| GET | `/api/grid/tasks` | 网格员待办任务 |
| POST | `/api/grid/tasks/{afId}/measurements` | 上报实测数据 |
| GET | `/api/public-feedback/mine` | 我的反馈与处理回复 |
| GET | `/api/air-quality-stats` | 实测数据统计（城市/网格员/时间范围） |
| GET | `/api/statistics/dashboard` | 大屏聚合数据 |

完整接口文档见 [`docs/接口设计.md`](docs/接口设计.md)。

---

## 测试

```bash
cd backend
mvn test
```

当前 **30 个单元 / 集成测试全部通过**：

| 测试类 | 用例数 | 覆盖内容 |
| --- | --- | --- |
| `AqiCalculatorTest` | 10 | 分段边界、段内插值、超限封顶、级别映射、异常入参 |
| `PublicFeedbackControllerTest` | 20 | 注册登录、鉴权与越权、状态机全部转移、实测上报、监测统计查询与筛选、字典接口 |

覆盖的关键场景：

- 匿名访问受保护接口返回 401，跨角色访问返回 403；
- 请求体伪造 `supervisorId`、`aqiLevel` 均无效；
- 已办结 / 已驳回的任务重复提交实测被拒；
- 非法状态转移与空回复被拒；
- 按城市、状态、时间范围筛选的正确性；
- 处理人姓名取自 `sys_admin` 表。

---

## 项目结构

```
.
├── backend/                              # Spring Boot 后端
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/neusoft/supervision/
│       │   ├── auth/                     # 登录、JWT、拦截器鉴权
│       │   │   ├── controller/           # 三角色登录接口
│       │   │   ├── service/              # 认证服务（BCrypt 校验）
│       │   │   ├── security/             # JwtTokenService、AuthInterceptor、AuthContext
│       │   │   ├── entity/ repository/   # sys_admin
│       │   │   └── dto/
│       │   ├── feedback/                 # 核心业务：反馈、指派、实测
│       │   │   ├── controller/
│       │   │   ├── service/              # PublicFeedbackService、DictionaryService
│       │   │   ├── entity/ repository/   # 7 张表实体与仓储
│       │   │   ├── dto/
│       │   │   └── support/              # AqiCalculator、状态常量、字典种子
│       │   ├── statistics/               # 大屏与实测统计
│       │   ├── config/                   # WebConfig、DataInitializer、密码编码器
│       │   └── common/                   # 统一响应、全局异常处理
│       ├── main/resources/application.yml
│       └── test/java/                    # 单元与集成测试
├── frontend/                             # Vue 3 前端（三端合一）
│   └── src/
│       ├── api/                          # 接口封装（含令牌注入与 401 处理）
│       ├── stores/                       # Pinia：三种身份登录态
│       ├── router/                       # 路由与角色守卫
│       ├── pages/                        # 登录、反馈列表/详情、任务、大屏
│       ├── components/                   # ECharts 图表组件
│       └── layouts/
├── db/
│   └── schema.sql                        # MySQL 建表脚本
└── docs/                                 # 设计文档
```

---

## 后续规划

- [ ] 反馈支持图片上传（`image_url` 字段与设计已预留，缺文件服务）
- [ ] 统计大屏数据源由 `public_feedback` 冗余字段切换为 `air_quality_stat`
- [ ] 管理员多账号与角色权限矩阵
- [ ] 网格员独立密码与修改密码
- [ ] 接入 MySQL 并补充 Flyway 数据库版本管理
- [ ] Docker Compose 一键部署

---

## 关于作者

- **陈盛铭** — 重庆邮电大学 软件工程（2023.09 - 2027.07）
- GitHub：[@Chen214-git214](https://github.com/Chen214-git214)
- 邮箱：csm005214@qq.com

## 许可证

本项目采用 [MIT](LICENSE) 许可协议。
