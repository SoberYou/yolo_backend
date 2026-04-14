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
    "userId": 12345, // 用户ID (Long)
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
- **Query Params**:
  - `userId`: 用户ID (Long)
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
- **Query Params**:
  - `userId`: 用户ID (Long)
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
    "userId": 12345, // 用户ID (Long)
    "title": "学习 Java",
    "description": "掌握 Spring Boot 开发",
    "expectedTotalHours": 100,
    "northStar": "成为高级 Java 工程师",
    "status": "ACTIVE" // 可选，状态: ACTIVE, COMPLETED, ARCHIVED
  }
  ```
- **Response**: `ApiResponse<Goal>`

### 2.2 获取目标列表
- **URL**: `/goals`
- **Method**: `GET`
- **Query Params**:
  - `userId`: 用户ID (Long)
  - `status` (可选): 筛选状态 (如 "ARCHIVED")。若不传，默认筛选非归档目标。
- **Response**: `ApiResponse<List<GoalWithStatsDto>>`

### 2.3 配置目标与活动类型关联 (批量覆盖)
为指定目标批量配置或覆盖活动类型关联。
- **URL**: `/goals/configureGoalActivityRelations`
- **Method**: `POST`
- **Headers**: 
  - `X-User-Id`: 用户ID (Long)
- **Body**:
  ```json
  {
    "goalId": 1,
    "typeCodes": ["coding", "reading", "meeting"]
  }
  ```
- **Response**: `ApiResponse<Void>`

### 2.4 获取目标绑定的活动类型列表
获取指定目标已绑定的所有活动类型 Code 列表。
- **URL**: `/goals/getGoalActivityRelations/{goalId}`
- **Method**: `GET`
- **Headers**: 
  - `X-User-Id`: 用户ID (Long)
- **Response**: `ApiResponse<List<String>>`
  ```json
  {
    "code": 200,
    "message": "Success",
    "data": ["coding", "reading"]
  }
  ```

### 2.5 绑定单个活动类型到目标
为指定目标添加单个活动类型的绑定关系。
- **URL**: `/goals/addGoalActivityRelation/{goalId}/{typeCode}`
- **Method**: `POST`
- **Headers**: 
  - `X-User-Id`: 用户ID (Long)
- **Response**: `ApiResponse<Void>`

### 2.6 解除目标与活动类型的绑定
移除指定目标与单个活动类型的绑定关系。
- **URL**: `/goals/deleteGoalActivityRelation/{goalId}/{typeCode}`
- **Method**: `DELETE`
- **Headers**: 
  - `X-User-Id`: 用户ID (Long)
- **Response**: `ApiResponse<Void>`
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

### 2.4 删除目标
- **URL**: `/goals/{goalId}`
- **Method**: `DELETE`
- **Query Params**:
  - `userId`: 用户唯一标识
- **Response**: `ApiResponse<Void>`

## 3. 专注 (Focus)

### 3.1 开始专注
同一时间只能有一个 RUNNING 状态的专注会话。

- **URL**: `/focus/start`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "userId": 12345, // 用户ID (Long)
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
    "userId": 12345, // 用户ID (Long)
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
  - `userId`: 用户ID (Long)
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
  - `userId`: 用户ID (Long)
  - `goalId` (可选): 筛选指定目标的专注记录
- **Response**: `ApiResponse<List<FocusSessionDto>>`

### 3.5 获取单个专注会话
- **URL**: `/focus/{id}`
- **Method**: `GET`
- **Query Params**:
  - `userId`: 用户ID (Long)
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
  - `userId`: 用户ID (Long) (必填)
  - `goalId`: 指定目标ID (必填)
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
    "userId": 12345, // 用户ID (Long)
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
    "userId": 12345, // 用户ID (Long)
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
- **Query Params**:
  - `userId`: 用户ID (Long)
- **Response**: `ApiResponse<Void>`

### 4.4 获取里程碑列表
支持按目标ID筛选。
- **URL**: `/milestones`
- **Method**: `GET`
- **Query Params**:
  - `userId`: 用户ID (Long) (必填)
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
- **Query Params**:
  - `userId`: 用户ID (Long)
- **Response**: `ApiResponse<Milestone>`

## 5. 认证 (Auth)

### 5.1 微信静默登录
使用微信小程序 wx.login 获取的 code 进行登录。

- **URL**: `/auth/login`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "code": "0a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p"
  }
  ```
- **Response**: `User`
  ```json
  {
    "id": 1,
    "openid": "wx_openid_sample",
    "sessionKey": "wx_session_key_sample",
    "createdAt": "2024-03-20T10:00:00",
    "updatedAt": "2024-03-20T10:00:00"
  }
  ```

## 6. 待办事项 (Todo)

### 6.1 获取待办事项列表
查询指定用户的待办事项，支持按日期类型、时间范围和完成状态过滤。

- **URL**: `/todo/getTodos`
- **Method**: `GET`
- **Query Params**:
  - `userId` (Long, 必填): 用户ID
  - `dateType` (String, 选填): 日期类型 (DAY, WEEK, MONTH, YEAR)
  - `startDate` (String, 选填): 开始日期 (如 "2023-10-25")
  - `endDate` (String, 选填): 结束日期 (如 "2023-10-25")
  - `isCompleted` (Integer, 选填): 是否完成 (0: 未完成, 1: 已完成)
- **Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "userId": 12345,
      "dateType": "DAY",
      "startDate": "2023-10-25",
      "endDate": "2023-10-25",
      "content": "买牛奶",
      "priority": "HIGH",
      "sortOrder": 1,
      "isCompleted": 0,
      "createTime": "2023-10-25T10:00:00",
      "updateTime": "2023-10-25T10:00:00"
    }
  ]
}
```

### 6.2 创建待办事项
新增一条待办事项。

- **URL**: `/todo/createTodo`
- **Method**: `POST`
- **Query Params**:
  - `userId` (Long, 必填): 用户ID
- **Body**:
```json
{
  "dateType": "DAY",
  "startDate": "2023-10-25",
  "endDate": "2023-10-25",
  "content": "买牛奶",
  "priority": "HIGH",
  "sortOrder": 1
}
```
- **Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "userId": 12345,
    "dateType": "DAY",
    "startDate": "2023-10-25",
    "endDate": "2023-10-25",
    "content": "买牛奶",
    "priority": "HIGH",
    "sortOrder": 1,
    "isCompleted": 0,
    "createTime": "2023-10-25T10:00:00",
    "updateTime": "2023-10-25T10:00:00"
  }
}
```

### 6.3 更新待办事项
更新已有待办事项信息。

- **URL**: `/todo/updateTodo/{id}`
- **Method**: `PUT`
- **Path Variables**:
  - `id` (Long, 必填): 待办事项ID
- **Query Params**:
  - `userId` (Long, 必填): 用户ID
- **Body**:
```json
{
  "isCompleted": 1,
  "priority": "LOW",
  "content": "买牛奶和面包"
}
```
- **Response**: 返回更新后的对象数据。

### 6.4 删除待办事项
删除指定ID的待办事项。

- **URL**: `/todo/deleteTodo/{id}`
- **Method**: `DELETE`
- **Path Variables**:
  - `id` (Long, 必填): 待办事项ID
- **Query Params**:
  - `userId` (Long, 必填): 用户ID
- **Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": null
}
```

### 5.4 Update Activity Type
* **URL:** `/api/schedule/updateActivityType`
* **Method:** `POST`
* **Query Parameters:**
  * `userId` (Long, required): User ID
* **Request Body:** `ScheduleActivityType` (JSON)
* **Response:** `ApiResponse<ScheduleActivityType>`

### 5.5 Batch Update Activity Type Sort
* **URL:** `/api/schedule/batchUpdateActivityTypeSort`
* **Method:** `POST`
* **Query Parameters:**
  * `userId` (Long, required): User ID
* **Request Body:** Array of `ActivityTypeSortUpdateDto` (JSON)
  * `id` (Long): Activity type ID
  * `sortOrder` (Integer): New sort order
* **Response:** `ApiResponse<Void>`

### 6.5 批量更新排序
批量更新待办事项的排序顺序。

- **URL**: `/todo/batchUpdateSort`
- **Method**: `PUT`
- **Query Params**:
  - `userId` (Long, 必填): 用户ID
- **Body**:
```json
[
  {
    "id": 1,
    "sortOrder": 1
  },
  {
    "id": 2,
    "sortOrder": 2
  }
]
```
- **Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": null
}
```

## 7. 做/不做清单 (Do Not Do)

### 7.1 获取做/不做清单
查询指定用户的做/不做清单列表，支持按类型过滤。

- **URL**: `/donotdo/getItems`
- **Method**: `GET`
- **Query Params**:
  - `userId` (Long, 必填): 用户ID
  - `itemType` (String, 选填): 类型 (DO, NOT_DO)
- **Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "userId": 12345,
      "itemType": "NOT_DO",
      "content": "熬夜",
      "sortOrder": 1,
      "createTime": "2023-10-25T10:00:00",
      "updateTime": "2023-10-25T10:00:00"
    }
  ]
}
```

### 7.2 创建清单项
新增一条做/不做清单记录。

- **URL**: `/donotdo/createItem`
- **Method**: `POST`
- **Query Params**:
  - `userId` (Long, 必填): 用户ID
- **Body**:
```json
{
  "itemType": "NOT_DO",
  "content": "熬夜",
  "sortOrder": 1
}
```
- **Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "userId": 12345,
    "itemType": "NOT_DO",
    "content": "熬夜",
    "sortOrder": 1,
    "createTime": "2023-10-25T10:00:00",
    "updateTime": "2023-10-25T10:00:00"
  }
}
```

### 7.3 更新清单项
更新指定的清单记录信息。

- **URL**: `/donotdo/updateItem/{id}`
- **Method**: `PUT`
- **Path Params**:
  - `id` (Long, 必填): 记录ID
- **Query Params**:
  - `userId` (Long, 必填): 用户ID
- **Body**:
```json
{
  "content": "不要熬夜",
  "sortOrder": 2
}
```
- **Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "userId": 12345,
    "itemType": "NOT_DO",
    "content": "不要熬夜",
    "sortOrder": 2,
    "createTime": "2023-10-25T10:00:00",
    "updateTime": "2023-10-26T10:00:00"
  }
}
```

### 7.4 删除清单项
删除指定的清单记录。

- **URL**: `/donotdo/deleteItem/{id}`
- **Method**: `DELETE`
- **Path Params**:
  - `id` (Long, 必填): 记录ID
- **Query Params**:
  - `userId` (Long, 必填): 用户ID
- **Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": null
}
```

### 7.5 批量更新排序
批量更新清单项的排序顺序。

- **URL**: `/donotdo/batchUpdateSort`
- **Method**: `PUT`
- **Query Params**:
  - `userId` (Long, 必填): 用户ID
- **Body**:
```json
[
  {
    "id": 1,
    "sortOrder": 1
  },
  {
    "id": 2,
    "sortOrder": 2
  }
]
```
- **Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": null
}
```

