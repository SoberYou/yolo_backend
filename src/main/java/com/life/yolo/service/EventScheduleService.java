package com.life.yolo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.life.yolo.entity.EventExecution;
import com.life.yolo.entity.EventSchedule;
import com.life.yolo.mapper.EventExecutionMapper;
import com.life.yolo.mapper.EventScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventScheduleService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] WEEKDAY_LABELS = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    private final EventScheduleMapper eventScheduleMapper;
    private final EventExecutionMapper eventExecutionMapper;
    private final UserService userService;

    public List<EventSchedule> listActiveEvents(Long userId) {
        userService.getInternalUserId(userId);
        QueryWrapper<EventSchedule> query = new QueryWrapper<>();
        query.eq("user_id", userId)
                .eq("is_deleted", 0)
                .isNull("effective_end_date")
                .orderByAsc("start_time")
                .orderByAsc("event_name")
                .orderByAsc("effective_start_date");
        return eventScheduleMapper.selectList(query);
    }

    public List<EventSchedule> listArchivedEvents(Long userId) {
        userService.getInternalUserId(userId);
        QueryWrapper<EventSchedule> query = new QueryWrapper<>();
        query.eq("user_id", userId)
                .eq("is_deleted", 0)
                .isNotNull("effective_end_date")
                .orderByDesc("effective_end_date")
                .orderByAsc("start_time")
                .orderByAsc("event_name");
        return eventScheduleMapper.selectList(query);
    }

    @Transactional
    public EventSchedule createEvent(Long userId, EventSchedule payload) {
        userService.getInternalUserId(userId);
        validateEventPayload(payload, true);

        QueryWrapper<EventSchedule> duplicateQuery = new QueryWrapper<>();
        duplicateQuery.eq("user_id", userId)
                .eq("event_name", payload.getEventName())
                .eq("start_time", payload.getStartTime())
                .eq("end_time", payload.getEndTime())
                .eq("is_deleted", 0)
                .isNull("effective_end_date");
        if (eventScheduleMapper.selectCount(duplicateQuery) > 0) {
            throw new IllegalArgumentException("已存在相同的生效中事件配置");
        }

        QueryWrapper<EventSchedule> mergeQuery = new QueryWrapper<>();
        mergeQuery.eq("user_id", userId)
                .eq("event_name", payload.getEventName())
                .eq("start_time", payload.getStartTime())
                .eq("end_time", payload.getEndTime())
                .eq("is_deleted", 0)
                .eq("effective_end_date", payload.getEffectiveStartDate().minusDays(1))
                .orderByDesc("id")
                .last("limit 1");
        EventSchedule mergeCandidate = eventScheduleMapper.selectOne(mergeQuery);
        if (mergeCandidate != null) {
            mergeCandidate.setEffectiveEndDate(null);
            mergeCandidate.setUpdatedAt(LocalDateTime.now());
            eventScheduleMapper.updateById(mergeCandidate);
            return mergeCandidate;
        }

        payload.setUserId(userId);
        payload.setIsDeleted(0);
        payload.setCreatedAt(LocalDateTime.now());
        payload.setUpdatedAt(LocalDateTime.now());
        eventScheduleMapper.insert(payload);
        return payload;
    }

    @Transactional
    public EventSchedule renameEvent(Long userId, Long scheduleId, String eventName) {
        if (eventName == null || eventName.trim().isEmpty()) {
            throw new IllegalArgumentException("事件名称不能为空");
        }
        EventSchedule schedule = requireOwnedSchedule(userId, scheduleId);
        schedule.setEventName(eventName.trim());
        schedule.setUpdatedAt(LocalDateTime.now());
        eventScheduleMapper.updateById(schedule);
        return schedule;
    }

    @Transactional
    public EventSchedule archiveEvent(Long userId, Long scheduleId, LocalDate effectiveEndDate) {
        if (effectiveEndDate == null) {
            throw new IllegalArgumentException("有效结束日期不能为空");
        }
        EventSchedule schedule = requireOwnedSchedule(userId, scheduleId);
        if (schedule.getEffectiveEndDate() != null) {
            throw new IllegalArgumentException("该事件已归档");
        }
        if (effectiveEndDate.isBefore(schedule.getEffectiveStartDate())) {
            throw new IllegalArgumentException("有效结束日期不能早于有效开始日期");
        }
        schedule.setEffectiveEndDate(effectiveEndDate);
        schedule.setUpdatedAt(LocalDateTime.now());
        eventScheduleMapper.updateById(schedule);
        return schedule;
    }

    @Transactional
    public void deleteEvent(Long userId, Long scheduleId) {
        EventSchedule schedule = requireOwnedSchedule(userId, scheduleId);
        QueryWrapper<EventExecution> query = new QueryWrapper<>();
        query.eq("schedule_id", scheduleId);
        if (eventExecutionMapper.selectCount(query) > 0) {
            throw new IllegalArgumentException("该事件已有执行记录，不能删除");
        }
        schedule.setIsDeleted(1);
        schedule.setUpdatedAt(LocalDateTime.now());
        eventScheduleMapper.updateById(schedule);
    }

    public Map<String, Object> getScheduleView(Long userId, LocalDate startDate, LocalDate endDate) {
        userService.getInternalUserId(userId);
        LocalDate rangeStart = startDate == null ? LocalDate.now() : startDate;
        LocalDate rangeEnd = endDate == null ? rangeStart.plusDays(6) : endDate;
        if (rangeEnd.isBefore(rangeStart)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
        if (Duration.between(rangeStart.atStartOfDay(), rangeEnd.plusDays(1).atStartOfDay()).toDays() > 31) {
            throw new IllegalArgumentException("时间范围最多支持31天");
        }

        QueryWrapper<EventSchedule> scheduleQuery = new QueryWrapper<>();
        scheduleQuery.eq("user_id", userId)
                .eq("is_deleted", 0)
                .le("effective_start_date", rangeEnd)
                .and(wrapper -> wrapper.isNull("effective_end_date").or().ge("effective_end_date", rangeStart))
                .orderByAsc("start_time")
                .orderByAsc("event_name");
        List<EventSchedule> schedules = eventScheduleMapper.selectList(scheduleQuery);

        List<Long> scheduleIds = schedules.stream().map(EventSchedule::getId).collect(Collectors.toList());
        Map<String, EventExecution> executionMap = new HashMap<>();
        if (!scheduleIds.isEmpty()) {
            QueryWrapper<EventExecution> executionQuery = new QueryWrapper<>();
            executionQuery.in("schedule_id", scheduleIds)
                    .between("execute_date", rangeStart, rangeEnd);
            List<EventExecution> executions = eventExecutionMapper.selectList(executionQuery);
            for (EventExecution execution : executions) {
                executionMap.put(execution.getScheduleId() + "_" + execution.getExecuteDate(), execution);
            }
        }

        List<Map<String, String>> dateHeaders = new ArrayList<>();
        for (LocalDate date = rangeStart; !date.isAfter(rangeEnd); date = date.plusDays(1)) {
            Map<String, String> header = new HashMap<>();
            header.put("date", date.toString());
            header.put("weekLabel", WEEKDAY_LABELS[date.getDayOfWeek().getValue() - 1]);
            dateHeaders.add(header);
        }

        List<ScheduleOccurrence> occurrences = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (EventSchedule schedule : schedules) {
            LocalDate effectiveStart = schedule.getEffectiveStartDate().isAfter(rangeStart) ? schedule.getEffectiveStartDate() : rangeStart;
            LocalDate effectiveEnd = schedule.getEffectiveEndDate() == null || schedule.getEffectiveEndDate().isAfter(rangeEnd) ? rangeEnd : schedule.getEffectiveEndDate();
            LocalDate currentDate = effectiveStart;
            while (!currentDate.isAfter(effectiveEnd)) {
                EventExecution execution = executionMap.get(schedule.getId() + "_" + currentDate);
                Map<String, Object> item = new HashMap<>();
                item.put("scheduleId", schedule.getId());
                item.put("eventName", schedule.getEventName());
                item.put("startTime", formatTime(schedule.getStartTime()));
                item.put("endTime", formatTime(schedule.getEndTime()));
                item.put("isExecuted", execution != null && execution.getIsExecuted() != null && execution.getIsExecuted() == 1);
                item.put("executedAt", execution != null && execution.getExecutedAt() != null ? formatTime(execution.getExecutedAt().toLocalTime()) : null);
                item.put("statusView", buildStatusView(currentDate, schedule.getEndTime(), execution, now));
                occurrences.add(new ScheduleOccurrence(currentDate.toString(), schedule.getStartTime(), schedule.getEndTime(), item));
                currentDate = currentDate.plusDays(1);
            }
        }

        List<ScheduleOccurrence> sortedOccurrences = occurrences.stream()
                .sorted(Comparator.comparing(ScheduleOccurrence::getStartTime)
                        .thenComparing(ScheduleOccurrence::getEndTime)
                        .thenComparing(occurrence -> String.valueOf(occurrence.getItem().get("eventName"))))
                .collect(Collectors.toList());

        List<List<ScheduleOccurrence>> mergedRows = new ArrayList<>();
        for (ScheduleOccurrence occurrence : sortedOccurrences) {
            if (mergedRows.isEmpty()) {
                List<ScheduleOccurrence> row = new ArrayList<>();
                row.add(occurrence);
                mergedRows.add(row);
                continue;
            }
            List<ScheduleOccurrence> lastRow = mergedRows.get(mergedRows.size() - 1);
            LocalTime rowEndTime = getMaxEndTime(lastRow);
            if (!occurrence.getStartTime().isAfter(rowEndTime)) {
                lastRow.add(occurrence);
            } else {
                List<ScheduleOccurrence> row = new ArrayList<>();
                row.add(occurrence);
                mergedRows.add(row);
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (List<ScheduleOccurrence> mergedRow : mergedRows) {
            LocalTime rowStartTime = getMinStartTime(mergedRow);
            LocalTime rowEndTime = getMaxEndTime(mergedRow);
            Map<String, List<Map<String, Object>>> itemsByDate = new LinkedHashMap<>();
            for (ScheduleOccurrence occurrence : mergedRow) {
                itemsByDate.computeIfAbsent(occurrence.getExecuteDate(), key -> new ArrayList<>()).add(occurrence.getItem());
            }
            for (List<Map<String, Object>> items : itemsByDate.values()) {
                items.sort(Comparator.comparing((Map<String, Object> item) -> String.valueOf(item.get("startTime")))
                        .thenComparing(item -> String.valueOf(item.get("eventName"))));
            }
            Map<String, Object> row = new HashMap<>();
            row.put("startTime", formatTime(rowStartTime));
            row.put("endTime", formatTime(rowEndTime));
            row.put("timeRange", formatTime(rowStartTime) + " - " + formatTime(rowEndTime));
            row.put("itemsByDate", itemsByDate);
            rows.add(row);
        }

        List<Map<String, Object>> timeGroups = new ArrayList<>();
        if (!rows.isEmpty()) {
            Map<String, Object> groupItem = new HashMap<>();
            groupItem.put("rows", rows);
            timeGroups.add(groupItem);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dateHeaders", dateHeaders);
        result.put("timeGroups", timeGroups);
        result.put("currentSlotTime", now.withMinute(now.getMinute() < 30 ? 0 : 30).withSecond(0).withNano(0).toLocalTime().format(TIME_FORMATTER));
        result.put("rangeStart", rangeStart.toString());
        result.put("rangeEnd", rangeEnd.toString());
        return result;
    }

    @Transactional
    public Map<String, Object> toggleExecution(Long userId, Long scheduleId, LocalDate executeDate) {
        EventSchedule schedule = requireOwnedSchedule(userId, scheduleId);
        if (executeDate == null) {
            throw new IllegalArgumentException("执行日期不能为空");
        }
        QueryWrapper<EventExecution> query = new QueryWrapper<>();
        query.eq("schedule_id", scheduleId).eq("execute_date", executeDate).last("limit 1");
        EventExecution execution = eventExecutionMapper.selectOne(query);
        LocalDateTime now = LocalDateTime.now();
        if (execution != null && execution.getIsExecuted() != null && execution.getIsExecuted() == 1) {
            execution.setIsExecuted(0);
            execution.setUpdatedAt(now);
            eventExecutionMapper.updateById(execution);
        } else if (execution != null) {
            execution.setIsExecuted(1);
            if (execution.getExecutedAt() == null) {
                execution.setExecutedAt(now);
            }
            execution.setUpdatedAt(now);
            eventExecutionMapper.updateById(execution);
        } else {
            execution = new EventExecution();
            execution.setScheduleId(scheduleId);
            execution.setExecuteDate(executeDate);
            execution.setIsExecuted(1);
            execution.setExecutedAt(now);
            execution.setCreatedAt(now);
            execution.setUpdatedAt(now);
            eventExecutionMapper.insert(execution);
        }
        return buildExecutionResponse(schedule, execution, executeDate, now);
    }

    @Transactional
    public Map<String, Object> updateExecutionTime(Long userId, Long scheduleId, LocalDate executeDate, LocalTime executedAt) {
        EventSchedule schedule = requireOwnedSchedule(userId, scheduleId);
        if (executeDate == null) {
            throw new IllegalArgumentException("执行日期不能为空");
        }
        if (executedAt == null) {
            throw new IllegalArgumentException("执行时间不能为空");
        }
        QueryWrapper<EventExecution> query = new QueryWrapper<>();
        query.eq("schedule_id", scheduleId).eq("execute_date", executeDate).last("limit 1");
        EventExecution execution = eventExecutionMapper.selectOne(query);
        LocalDateTime now = LocalDateTime.now();
        if (execution == null) {
            execution = new EventExecution();
            execution.setScheduleId(scheduleId);
            execution.setExecuteDate(executeDate);
            execution.setCreatedAt(now);
        }
        execution.setIsExecuted(1);
        execution.setExecutedAt(LocalDateTime.of(executeDate, executedAt));
        execution.setUpdatedAt(now);
        if (execution.getId() == null) {
            eventExecutionMapper.insert(execution);
        } else {
            eventExecutionMapper.updateById(execution);
        }
        return buildExecutionResponse(schedule, execution, executeDate, now);
    }

    private EventSchedule requireOwnedSchedule(Long userId, Long scheduleId) {
        userService.getInternalUserId(userId);
        EventSchedule schedule = eventScheduleMapper.selectById(scheduleId);
        if (schedule == null || !userId.equals(schedule.getUserId()) || (schedule.getIsDeleted() != null && schedule.getIsDeleted() == 1)) {
            throw new IllegalArgumentException("未找到对应事件");
        }
        return schedule;
    }

    private void validateEventPayload(EventSchedule payload, boolean requireStartDate) {
        if (payload == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        if (payload.getEventName() == null || payload.getEventName().trim().isEmpty()) {
            throw new IllegalArgumentException("事件名称不能为空");
        }
        payload.setEventName(payload.getEventName().trim());
        if (payload.getStartTime() == null || payload.getEndTime() == null) {
            throw new IllegalArgumentException("开始时间和结束时间不能为空");
        }
        if (!(payload.getStartTime().getMinute() == 0 || payload.getStartTime().getMinute() == 30)
                || !(payload.getEndTime().getMinute() == 0 || payload.getEndTime().getMinute() == 30)
                || payload.getStartTime().getSecond() != 0
                || payload.getEndTime().getSecond() != 0) {
            throw new IllegalArgumentException("开始时间和结束时间必须使用整点或半点");
        }
        if (!payload.getStartTime().isBefore(payload.getEndTime())) {
            throw new IllegalArgumentException("开始时间必须早于结束时间");
        }
        if (requireStartDate && payload.getEffectiveStartDate() == null) {
            throw new IllegalArgumentException("有效开始日期不能为空");
        }
    }

    private String buildStatusView(LocalDate executeDate, LocalTime endTime, EventExecution execution, LocalDateTime now) {
        if (execution != null && execution.getIsExecuted() != null && execution.getIsExecuted() == 1) {
            return "done";
        }
        return now.isAfter(LocalDateTime.of(executeDate, endTime)) ? "expired" : "pending";
    }

    private LocalTime getMinStartTime(List<ScheduleOccurrence> occurrences) {
        return occurrences.stream()
                .map(ScheduleOccurrence::getStartTime)
                .min(LocalTime::compareTo)
                .orElse(LocalTime.MIN);
    }

    private LocalTime getMaxEndTime(List<ScheduleOccurrence> occurrences) {
        return occurrences.stream()
                .map(ScheduleOccurrence::getEndTime)
                .max(LocalTime::compareTo)
                .orElse(LocalTime.MIN);
    }

    private Map<String, Object> buildExecutionResponse(EventSchedule schedule, EventExecution execution, LocalDate executeDate, LocalDateTime now) {
        Map<String, Object> result = new HashMap<>();
        result.put("scheduleId", schedule.getId());
        result.put("executeDate", executeDate.toString());
        result.put("isExecuted", execution != null && execution.getIsExecuted() != null && execution.getIsExecuted() == 1);
        result.put("executedAt", execution != null && execution.getExecutedAt() != null ? formatTime(execution.getExecutedAt().toLocalTime()) : null);
        result.put("statusView", buildStatusView(executeDate, schedule.getEndTime(), execution, now));
        return result;
    }

    private String formatTime(LocalTime time) {
        return time.format(TIME_FORMATTER);
    }

    private static class ScheduleOccurrence {
        private final String executeDate;
        private final LocalTime startTime;
        private final LocalTime endTime;
        private final Map<String, Object> item;

        private ScheduleOccurrence(String executeDate, LocalTime startTime, LocalTime endTime, Map<String, Object> item) {
            this.executeDate = executeDate;
            this.startTime = startTime;
            this.endTime = endTime;
            this.item = item;
        }

        public String getExecuteDate() {
            return executeDate;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        public Map<String, Object> getItem() {
            return item;
        }
    }
}
