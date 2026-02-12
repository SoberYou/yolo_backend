# YOLO Life Timer 接口文档

## 基础信息
- **API Base URL**: `/api`
- **数据格式**: JSON
- **统一响应结构**:
  ```json
  {
    "code": 200,
    "message": "Success",
    "data": { ... }
  }
  ```

## 1. 人生时间 (Life)

### 1.1 配置人生参数
初始化或更新用户的出生日期和预期寿命。

- **URL**: `/life/configLife`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "birthDate": "1995-05-20", // 出生日期 (YYYY-MM-DD)
    "expectedLifeYears": 80    // 预期寿命 (年)
  }
  ```
- **Response**: `ApiResponse<Void>`

### 1.2 获取人生配置
获取用户配置的出生日期和预期寿命。

- **URL**: `/life/getLifeConfig`
- **Method**: `GET`
- **Response**: `ApiResponse<LifeConfigDto>`
  ```json
  {
    "code": 200,
    "message": "Success",
    "data": {
      "birthDate": "1995-05-20",
      "expectedLifeYears": 80
    }
  }
  ```

### 1.3 获取人生状态
获取当前已使用的时间比例和“人生时钟”时间。

- **URL**: `/life/getLifeStatus`
- **Method**: `GET`
- **Response**: `ApiResponse<LifeStatusDto>`
  ```json
  {
    "code": 200,
    "message": "Success",
    "data": {
      "totalDays": 29200,      // 总寿命天数
      "usedDays": 10500,       // 已使用天数
      "usedRatio": "35.96%",   // 已使用比例
      "lifeClock": "08:37"     // 映射到24小时制的时间点
    }
  }
  ```

## 2. 目标管理 (Goal)

### 2.1 创建或更新目标 (Save Goal)
- **URL**: `/goals`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "id": 1, // 可选，若存在则更新，若为空则创建
    "title": "学习 Java",
    "description": "掌握 Spring Boot 开发",
    "expectedTotalHours": 100,
    "northStar": "成为高级 Java 工程师"
  }
  ```
- **Response**: `ApiResponse<Goal>`

### 2.2 获取目标列表
- **URL**: `/goals`
- **Method**: `GET`
- **Response**: `ApiResponse<List<GoalWithStatsDto>>`
  ```json
  {
    "code": 200,
    "message": "Success",
    "data": [
      {
        "id": 1,
        "title": "学习 Java",
        "description": "掌握 Spring Boot 开发",
        "expectedTotalHours": 100,
        "northStar": "成为高级 Java 工程师",
        "status": "ACTIVE",
        "createdAt": "2024-03-20T10:00:00",
        "updatedAt": "2024-03-20T10:00:00",
        "last7DaysMinutes": 120
      }
    ]
  }
  ```

### 2.3 获取单个目标详情
- **URL**: `/goals/{goalId}`
- **Method**: `GET`
- **Response**: `ApiResponse<Goal>`

## 3. 专注 (Focus)

### 3.1 开始专注
同一时间只能有一个 RUNNING 状态的专注会话。

- **URL**: `/focus/start`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "goalId": 1
  }
  ```
- **Response**: `ApiResponse<FocusSession>`

### 3.2 结束专注
结束当前正在进行的专注会话，并计算时长。

- **URL**: `/focus/end`
- **Method**: `POST`
- **Response**: `ApiResponse<FocusSession>`
  ```json
  {
    "code": 200,
    "message": "Success",
    "data": {
      "id": 101,
      "goalId": 1,
      "startTime": "2024-03-20T14:00:00",
      "endTime": "2024-03-20T14:45:00",
      "status": "COMPLETED",
      "durationMinutes": 45,
      ...
    }
  }
  ```

### 3.3 获取专注统计
按目标 ID 统计专注时长详情。

- **URL**: `/focus/statistics`
- **Method**: `GET`
- **Query Params**:
  - `goalId` (必填): 指定目标ID
- **Response**: `ApiResponse<FocusStatsDto>`
  ```json
  {
    "code": 200,
    "message": "Success",
    "data": {
      "goalId": 1,
      "goalTitle": "学习 Java",
      "last7DaysMinutes": 120,
      "last30DaysMinutes": 500,
      "dailyRecords": [
        {
          "date": "2024-03-20",
          "minutes": 45
        }
      ]
    }
  }
  ```
