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
初始化或更新用户的出生日期、预期寿命和精力寿命。

- **URL**: `/life/configLife`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "birthDate": "1995-05-20", // 出生日期 (YYYY-MM-DD)
    "expectedLifeYears": 80,   // 预期寿命 (年)
    "energyLifeYears": 60      // 精力寿命 (年)
  }
  ```
- **Response**: `ApiResponse<Void>`

### 1.2 获取人生配置
获取用户配置的出生日期、预期寿命和精力寿命。

- **URL**: `/life/getLifeConfig`
- **Method**: `GET`
- **Response**: `ApiResponse<LifeConfigDto>`
  ```json
  {
    "code": 200,
    "message": "Success",
    "data": {
      "birthDate": "1995-05-20",
      "expectedLifeYears": 80,
      "energyLifeYears": 60
    }
  }
  ```

### 1.3 获取人生状态
获取当前已使用的时间比例、精力时间状态和“人生时钟”时间。

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
      "energyDays": 21900,     // 精力寿命天数
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
结束当前正在进行的专注会话，或根据 ID 更新/结束指定会话。支持手动指定时间或持续时长。

- **URL**: `/focus/end`
- **Method**: `POST`
- **Request Body**: `application/json` (可选)
  ```json
  {
    "id": 101, // 可选，若不传则查找当前 RUNNING 状态的会话
    "startTime": "2024-03-20T14:00:00", // 可选，修改开始时间
    "endTime": "2024-03-20T14:45:00", // 可选，指定结束时间
    "durationMinutes": 45, // 可选，指定持续时长（若存在，结束时间 = 开始时间 + 时长）
    "memo": "Completed the chapter on Spring Security" // 可选，备注
  }
  ```
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
      "memo": "Completed the chapter on Spring Security",
      ...
    }
  }
  ```

### 3.3 获取进行中的专注会话
获取指定目标（或任意目标）当前正在进行的专注会话。

- **URL**: `/focus/running`
- **Method**: `GET`
- **Query Params**:
  - `goalId` (可选): 筛选指定目标的进行中会话
- **Response**: `ApiResponse<FocusSession>`
  ```json
  {
    "code": 200,
    "message": "Success",
    "data": {
      "id": 102,
      "goalId": 1,
      "startTime": "2024-03-20T15:00:00",
      "status": "RUNNING",
      ...
    }
  }
  ```

### 3.4 获取专注列表
获取专注记录列表，支持按目标筛选。

- **URL**: `/focus`
- **Method**: `GET`
- **Query Params**:
  - `goalId` (可选): 筛选指定目标的专注记录
- **Response**: `ApiResponse<List<FocusSessionDto>>`
  ```json
  {
    "code": 200,
    "message": "Success",
    "data": [
      {
        "id": 101,
        "goalId": 1,
        "goalTitle": "学习 Java",
        "startTime": "2024-03-20T14:00:00",
        "endTime": "2024-03-20T14:45:00",
        "status": "COMPLETED",
        "durationMinutes": 45,
        "memo": "Completed the chapter on Spring Security",
        ...
      }
    ]
  }
  ```

### 3.5 获取单个专注记录
获取指定 ID 的专注记录详情。

- **URL**: `/focus/{id}`
- **Method**: `GET`
- **Response**: `ApiResponse<FocusSessionDto>`
  ```json
  {
    "code": 200,
    "message": "Success",
    "data": {
      "id": 101,
      "goalId": 1,
      "goalTitle": "学习 Java",
      "startTime": "2024-03-20T14:00:00",
      "endTime": "2024-03-20T14:45:00",
      "status": "COMPLETED",
      "durationMinutes": 45,
      "memo": "Completed the chapter on Spring Security",
      ...
    }
  }
  ```

### 3.6 获取专注统计
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
      "totalMinutes": 309,
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

## 4. 里程碑 (Milestone)

### 4.1 创建里程碑
- **URL**: `/milestones`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "goalId": 1,
    "milestoneTitle": "Phase 1 Completion",
    "milestoneDate": "2024-03-21",
    "milestoneDesc": "Completed Module 1",
    "ownFeel": "Feeling great!"
  }
  ```
- **Response**: `ApiResponse<Milestone>`

### 4.2 更新里程碑
- **URL**: `/milestones`
- **Method**: `PUT`
- **Body**:
  ```json
  {
    "id": 1,
    "goalId": 1,
    "milestoneTitle": "Phase 1 Completion (Revised)",
    "milestoneDate": "2024-03-21",
    "milestoneDesc": "Completed Module 1 with extra credits",
    "ownFeel": "Feeling awesome!"
  }
  ```
- **Response**: `ApiResponse<Milestone>`

### 4.3 删除里程碑
- **URL**: `/milestones/{id}`
- **Method**: `DELETE`
- **Response**: `ApiResponse<Void>`

### 4.4 获取里程碑列表
支持按目标ID筛选。
- **URL**: `/milestones`
- **Method**: `GET`
- **Query Params**:
  - `goalId` (可选): 筛选指定目标的里程碑
- **Response**: `ApiResponse<List<Milestone>>`
  ```json
  {
    "code": 200,
    "message": "Success",
    "data": [
      {
        "id": 1,
        "goalId": 1,
        "milestoneTitle": "Phase 1 Completion",
        "milestoneDate": "2024-03-21",
        "milestoneDesc": "Completed Module 1",
        "ownFeel": "Feeling great!",
        "createAt": "2024-03-21T10:00:00",
        "updateAt": "2024-03-21T10:00:00"
      }
    ]
  }
  ```

### 4.5 获取单个里程碑
- **URL**: `/milestones/{id}`
- **Method**: `GET`
- **Response**: `ApiResponse<Milestone>`
