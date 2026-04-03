# YOLO Schedule 接口文档

## 基础信息
- **API Base URL**: `/api/schedule`
- **数据格式**: JSON
- **统一请求方式**: `POST`
- **统一响应结构** (基于项目中可能的通用 ResponseEntity 封装，示例结构如下):
  ```json
  {
    "code": 200,
    "message": "Success",
    "data": { ... }
  }
  ```

## 1. 日程活动类型 (Schedule Activity Type)

### 1.1 查询活动类型
根据类型名称模糊搜索，或查询所有启用的活动类型。

- **URL**: `/getActivityTypes`
- **Method**: `POST`
- **Query Params**:
  - `userId` (Long, 必填): 用户ID，用于数据隔离
  - `typeName` (String, 可选): 根据类型名称模糊搜索
- **Body**: 空
- **Response**: `List<ScheduleActivityType>`
  ```json
  {
    "code": 200,
    "message": "Success",
    "data": [
      {
        "id": 1,
        "typeCode": "WORK",
        "typeName": "工作",
        "color": "#FF5733",
        "sort": 1,
        "enableFlag": 1,
        "createTime": "2023-10-25T10:00:00",
        "updateTime": "2023-10-25T10:00:00"
      }
    ]
  }
  ```

### 1.2 创建活动类型
新增一种日程活动类型。

- **URL**: `/createActivityType`
- **Method**: `POST`
- **Query Params**:
  - `userId` (Long, 必填): 用户ID，用于数据隔离
- **Body**:
  ```json
  {
    "typeCode": "STUDY",
    "typeName": "学习",
    "color": "#33FF57",
    "sort": 2,
    "enableFlag": 1
  }
  ```
- **Response**: 返回创建成功的实体 (包含生成的 `id`)

### 1.3 更新活动类型
修改已存在的日程活动类型信息。

- **URL**: `/updateActivityType`
- **Method**: `POST`
- **Query Params**:
  - `userId` (Long, 必填): 用户ID，用于数据隔离
- **Body**:
  ```json
  {
    "id": 2, // 必须提供主键 ID
    "typeCode": "STUDY_UPDATE",
    "typeName": "深度学习",
    "color": "#33FF57",
    "sort": 2,
    "enableFlag": 1
  }
  ```
- **Response**: 返回更新后的实体

### 1.4 删除活动类型
根据 ID 删除指定的活动类型。

- **URL**: `/deleteActivityType`
- **Method**: `POST`
- **Query Params**:
  - `id` (Long, 必填): 活动类型ID
  - `userId` (Long, 必填): 用户ID，用于数据隔离
- **Response**: `Void`

---

## 2. 日程记录 (Schedule Record)

### 2.1 查询日程记录
获取指定日期或记录类型的日程排期数据。

- **URL**: `/getRecords`
- **Method**: `POST`
- **Query Params**:
  - `userId` (Long, 必填): 用户ID，用于数据隔离
  - `startDate` (String, 可选): 开始日期, 格式 `YYYY-MM-DD`
  - `endDate` (String, 可选): 结束日期, 格式 `YYYY-MM-DD`
  - `recordType` (String, 可选): 记录类型 (如 `plan` 计划, `actual` 实际)
- **Response**: `List<ScheduleRecord>`
  ```json
  {
    "code": 200,
    "message": "Success",
    "data": [
      {
        "id": 101,
        "bizDate": "2023-10-25",
        "startTime": "09:00",
        "endTime": "10:30",
        "recordType": "plan",
        "activityType": "WORK",
        "createTime": "2023-10-25T08:00:00",
        "updateTime": "2023-10-25T08:00:00"
      }
    ]
  }
  ```

### 2.2 批量保存日程记录
批量新增或更新日程记录。如果库中已存在相同 `bizDate`, `recordType`, `startTime`, `endTime` 的记录，则更新其 `activityType`；否则插入新记录。（支持 `recordType` 为空或不传，将默认处理为空字符串）

- **URL**: `/batchSaveRecords`
- **Method**: `POST`
- **Query Params**:
  - `userId` (Long, 必填): 用户ID，用于数据隔离
- **Body**: `List<ScheduleRecord>` 数组
  ```json
  [
    {
      "bizDate": "2023-10-25", // 必填
      "startTime": "09:00",    // 必填
      "endTime": "10:30",      // 必填
      "recordType": "plan",    // 可填为空
      "activityType": "WORK"
    },
    {
      "bizDate": "2023-10-25",
      "startTime": "10:30",
      "endTime": "11:00",
      "recordType": "plan",
      "activityType": "WORK"
    }
  ]
  ```
- **Response**: `Void`

### 2.3 删除日程记录
根据 ID 删除指定的日程记录。

- **URL**: `/deleteRecord`
- **Method**: `POST`
- **Query Params**:
  - `id` (Long, 必填): 日程记录ID
  - `userId` (Long, 必填): 用户ID，用于数据隔离
- **Response**: `Void`
