# afeng-live-app

一个基于 Spring Cloud 微服务架构的直播平台后端项目。

## 项目简介

本项目实现了一个完整的直播平台后端服务，支持用户访问直播首页、浏览直播列表、进入直播间观看直播、发送弹幕聊天、赠送礼物、直播 PK、红包雨活动、购物车与下单等功能。

### 核心业务流程

1. 用户访问直播首页，拉取直播列表
2. 点击具体直播间，判断登录状态
   - **已登录**：进入直播间观看直播，可进行送礼和聊天互动
   - **未登录**：跳转登录页面

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 基础运行环境 |
| Spring Boot | 3.0.4 | 应用框架 |
| Spring Cloud Alibaba | 2022.0.0.0-RC1 | 微服务全家桶（Nacos 服务发现 & 配置中心） |
| Spring Cloud Gateway | 4.0.6 | API 网关 |
| Apache Dubbo | 3.2.0-beta.3 | RPC 远程调用框架 |
| Netty | - | IM 核心服务底层通信 |
| MySQL | 8.0.28 | 关系型数据库 |
| Redis | - | 缓存 |
| RocketMQ | - | 消息队列 |
| Docker | - | 容器化部署 |

## 模块架构

项目采用 Maven 多模块结构，每个业务域划分为 `interface`（接口定义）和 `provider`（服务实现）两个子模块，遵循面向接口编程的设计理念。

```
afeng-live-app
├── afeng-live-api                  # API 层 - Spring Boot Web 应用，提供 REST 接口
├── afeng-live-gateway              # 网关层 - Spring Cloud Gateway，统一入口路由
│
├── afeng-live-framework            # 基础框架层（父模块）
│   ├── afeng-live-framework-datasource-starter   # 数据源自动配置
│   ├── afeng-live-framework-redis-starter        # Redis 自动配置
│   ├── afeng-live-framework-web-starter          # Web 层通用组件（限流、统一异常处理等）
│   └── afeng-live-framework-mq-starter           # 消息队列自动配置
│
├── afeng-live-common-interface     # 公共接口 & VO 定义
│
├── afeng-live-user-interface       # 用户服务 - 接口定义
├── afeng-live-user-provider        # 用户服务 - Dubbo 提供者
│
├── afeng-live-account-interface    # 账户服务 - 接口定义
├── afeng-live-account-provider     # 账户服务 - Dubbo 提供者
│
├── afeng-live-living-interface     # 直播服务 - 接口定义
├── afeng-live-living-provider      # 直播服务 - Dubbo 提供者
│
├── afeng-live-gift-interface       # 礼物服务 - 接口定义
├── afeng-live-gift-provider        # 礼物服务 - Dubbo 提供者
│
├── afeng-live-bank-interface       # 支付服务 - 接口定义
├── afeng-live-bank-provider        # 支付服务 - Dubbo 提供者
├── afeng-live-bank-api             # 支付回调 API
│
├── afeng-live-im-interface         # IM 服务 - 接口定义
├── afeng-live-im-provider          # IM 服务 - Dubbo 提供者
├── afeng-live-im-consumer          # IM 消息消费者
├── afeng-live-im-core-server       # IM 核心服务（Netty）
├── afeng-live-im-core-server-interfaces  # IM 核心服务接口
├── afeng-live-im-router-interface   # IM 路由 - 接口定义
├── afeng-live-im-router-provider    # IM 路由 - Dubbo 提供者
│
├── afeng-live-msg-interface        # 消息服务 - 接口定义
├── afeng-live-msg-provider         # 消息服务 - Dubbo 提供者
│
├── afeng-live-sms-interface        # 短信服务 - 接口定义
├── afeng-live-sms-provider         # 短信服务 - Dubbo 提供者
│
├── afeng-live-id-generate-interface  # ID 生成服务 - 接口定义
├── afeng-live-id-generate-provider   # ID 生成服务 - Dubbo 提供者
│
└── sql/                            # 数据库初始化脚本
```

## 功能特性

### 直播功能
- 直播间列表分页查询
- 开播 / 关播
- 定时任务缓存直播间信息
- 主播配置管理

### IM 即时通讯
- 基于 **Netty** 的长连接通信
- WebSocket 连接管理
- 登录认证 & 心跳检测
- 聊天消息实时收发
- 消息 ACK 确认机制
- 用户在线状态判断
- IM 路由层实现消息转发

### 互动玩法
- 礼物赠送（基础礼物 & 特效礼物）
- 直播 PK 对战
- 红包雨活动（准备 → 开启 → 领取）
- 弹幕聊天

### 交易体系
- 用户充值流程
- 支付中台对接
- 购物车管理
- 商品下单

### 基础能力
- 分布式 ID 生成
- 接口限流（基于注解）
- 统一异常处理
- Nacos 服务注册 & 配置管理
- Docker 容器化部署

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0
- Redis
- RocketMQ
- Nacos Server

### 本地启动

1. **初始化数据库**

   执行 `sql/` 目录下的 SQL 脚本创建所需的数据库和表。

2. **启动 Nacos Server**

   确保 Nacos 服务运行在本地 `8848` 端口（或其他配置的地址）。

3. **编译项目**

   ```bash
   mvn clean compile -DskipTests
   ```

4. **按顺序启动服务**

   建议按以下顺序启动：
   - `afeng-live-gateway` — 网关
   - `afeng-live-user-provider` — 用户服务
   - `afeng-live-account-provider` — 账户服务
   - `afeng-live-living-provider` — 直播服务
   - `afeng-live-im-core-server` & `afeng-live-im-router-provider` — IM 服务
   - `afeng-live-gift-provider` — 礼物服务
   - `afeng-live-bank-provider` — 支付服务
   - `afeng-live-api` — API 层

### Docker 部署

项目配置了 Docker 镜像仓库地址，可通过 Maven 插件构建镜像并推送：

```bash
# 镜像仓库：registry.baidubce.com
# 命名空间：afeng-live-test
mvn clean package docker:build docker:push
```

## 项目地址

- GitHub: https://github.com/afeng-hash/live-streaming.git

## License

本项目仅用于学习交流目的。
