# 东软环保公众监督系统后端

## 技术栈

- Java 17
- Spring Boot 3.3
- Spring Web
- Spring Data JPA
- H2 内存数据库
- JJWT（HS256 无状态令牌）

## 运行方式

```bash
mvn spring-boot:run
```

启动后访问：

- 接口地址：`http://localhost:8080/api/public-feedback`
- H2 控制台：`http://localhost:8080/h2-console`

H2 连接信息：

- JDBC URL：`jdbc:h2:mem:env_supervision`
- Username：`sa`
- Password：空

## 认证与鉴权

除登录接口和公众端接口外，其余接口都需要在请求头携带 JWT：

```text
Authorization: Bearer <token>
```

令牌通过登录接口获取：

| 角色 | 接口 | 默认账号 |
| --- | --- | --- |
| 管理员 | `POST /api/auth/admin/login` | `admin` / `123`（`sys_admin` 表中的种子账号） |
| 网格员 | `POST /api/auth/grid-member/login` | 工号（如 `SY-001`）/ `123` |
| 公众监督员 | `POST /api/auth/public-supervisor/login` | `supervisor001` / `123456` |

三类账号均已入库，密码一律以 BCrypt 摘要存储：管理员在 `sys_admin`、网格员在 `grid_member`（统一初始密码）、
公众监督员在 `public_supervisor`。管理员登录成功会更新 `last_login_time`，停用账号（`status = 0`）拒绝登录。

公众监督员也可先注册（`POST /api/public-supervisors`，账号需 4-50 位、密码 6-32 位、手机号 11 位且唯一），
注册成功即返回令牌；密码以 BCrypt 摘要存储，不存明文。

授权规则（由 `AuthInterceptor` 统一执行）：

| 路径前缀 | 允许角色 | 未携带或无效令牌 | 角色不匹配 |
| --- | --- | --- | --- |
| `/api/auth/**`、`POST /api/public-supervisors` | 公开 | — | — |
| `/api/feedback/**`、`/api/statistics/**` | 管理员 | 401 | 403 |
| `/api/grid/**` | 网格员 | 401 | 403 |
| `/api/public-feedback/**`、`/api/public-supervisors/me` | 公众监督员 | 401 | 403 |
| `/api/grid-cities` | 三种角色均可 | 401 | — |

## 字典数据

省、市、AQI 级别三张字典表在启动时由 `DataInitializer` 写入，运行时由 `DictionaryService` 缓存并提供按 ID 反查：

| 表 | 用途 |
| --- | --- |
| `grid_province` | 省级区域（辽宁省 / 北京市 / 天津市） |
| `grid_city` | 市级区域，含行政区划编码与所属省份，并提供 `GET /api/grid-cities` 接口 |
| `aqi_level` | AQI 六级（优…严重污染）及各自动区间、健康影响、防护建议 |
| `sys_admin` | 管理员账号（登录校验、处理反馈时记录并展示处理人真实姓名） |
| `public_supervisor` | 公众监督员账号（注册、登录、反馈提交人） |
| `air_quality_stat` | 实测数据（网格员每次上报一行，含六项浓度、AQI 与级别，支持复测） |

反馈列表中的监督员姓名/电话、处理人姓名、城市名、预计等级，以及统计大屏的 AQI 分布口径，
均来自上述账号表与字典表，不再有硬编码映射：新增城市、调整级别或新增管理员只需改数据，不用改代码。

## 反馈状态机与 AQI 计算

状态流转（`public_feedback.status`）：

```text
待处理(0) ──指派网格员──> 处理中(1) ──管理员办结──> 已完成(2)
                              └──────管理员驳回──> 已驳回(3)
```

- 实测数据提交后反馈**仍停在「处理中」**：网格员负责录入，办结权在管理员；
- 已完成/已驳回为终态，再次处理返回 400，避免重复处理与状态回退；
- 处理结果写入 `admin_id` / `reply_content` / `handle_time`。

AQI 由服务端计算（`AqiCalculator`）：

- 按 SO2 / CO / PM2.5 的浓度限值分段做线性插值得到单项 IAQI；
- `AQI = max(IAQI_SO2, IAQI_CO, IAQI_PM25)`，并落库 `aqi_value`；
- 级别按 `aqi_level` 表的 `min_aqi` / `max_aqi` 判定，前端提交的等级字段一律忽略。

单元测试见 `AqiCalculatorTest`（分段边界、插值、封顶、级别映射、异常入参）。

实测数据同时写入两张表：`public_feedback` 上保留一份冗余（便于反馈详情直接展示），
`air_quality_stat` 保存完整记录（六项浓度 + AQI + 上报人 + 监测时间），
`GET /api/air-quality-stats` 以及大数据屏的按月统计均以 `air_quality_stat` 为准，
因此同一条反馈可以保留多次复测结果。

要点：**身份一律由令牌决定，不再通过请求参数传入**。网格员提交实测数据、公众监督员提交反馈时，
请求体都不需要（也无法）携带 `gridMemberId` / `supervisorId`，从根上避免冒用他人身份。

令牌签名密钥由 `supervision.jwt.secret` 配置，生产环境应改用环境变量 `SUPERVISION_JWT_SECRET` 注入。

## 公众监督员数据提交示例

```bash
# 1) 登录取令牌
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/public-supervisor/login \
  -H "Content-Type: application/json" \
  -d '{"username":"supervisor001","password":"123456"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')

# 2) 携带令牌提交反馈（请求体无需 supervisorId）
curl -X POST http://localhost:8080/api/public-feedback \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"cityId\":1,\"levelId\":3,\"pollutionType\":\"扬尘\",\"address\":\"沈阳市和平区南京北街100号\",\"longitude\":123.414662,\"latitude\":41.799921,\"description\":\"施工现场未覆盖防尘网，扬尘明显。\",\"imageUrl\":\"https://example.com/images/feedback-1.jpg\"}"

# 3) 查看自己提交的反馈及处理进展
curl http://localhost:8080/api/public-feedback/mine -H "Authorization: Bearer $TOKEN"
```

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "feedbackId": 1,
    "supervisorId": 1,
    "cityId": 1,
    "pollutionType": "扬尘",
    "address": "沈阳市和平区南京北街100号",
    "status": 0,
    "submittedAt": "2026-06-05T09:30:00"
  }
}
```

## 数据库脚本

MySQL 建表脚本位于：

```text
03.项目源码/03.数据库/schema.sql
```

开发运行默认使用 H2 内存数据库，JPA 会自动创建 `public_feedback` 表。
