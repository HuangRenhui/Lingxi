package com.lingxi.service.impl;

import com.lingxi.common.BusinessException;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.TaskCreateRequest;
import com.lingxi.dto.TaskUpdateRequest;
import com.lingxi.entity.AiTask;
import com.lingxi.mapper.AiTaskMapper;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.AgentTaskService;
import com.lingxi.vo.TaskVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 定时任务服务实现。
 *
 * <p>当前仅落地任务定义管理；调度触发、Agent 编排循环、Skills/MCP 授权执行
 * 属于 B7 后续里程碑（见 docs/架构设计.md）。</p>
 */
@Service
public class AgentTaskServiceImpl implements AgentTaskService {

    private static final Set<String> RUN_LOCATIONS = new HashSet<>(Arrays.asList("CLOUD", "LOCAL"));
    private static final Set<String> SCHEDULE_TYPES =
            new HashSet<>(Arrays.asList("INTERVAL", "HOURLY", "DAILY", "WEEKLY", "CRON"));

    private final AiTaskMapper aiTaskMapper;

    public AgentTaskServiceImpl(AiTaskMapper aiTaskMapper) {
        this.aiTaskMapper = aiTaskMapper;
    }

    @Override
    public List<TaskVO> listMine(int page, int size) {
        // page/size 由调用方传入：page 至少 1，size 默认 20 且最多 100，避免一次拉取全量任务。
        int current = Math.max(page, 1);
        int pageSize = size < 1 ? 20 : Math.min(size, 100);
        // 查询条件强制带上 userId，保证用户只能看到自己的任务；
        // 以创建时间倒序为主，id 作为创建时间相同时的稳定兜底排序。
        Long userId = SecurityUtils.getUserId();
        Page<AiTask> result = aiTaskMapper.selectPage(new Page<>(current, pageSize),
                new LambdaQueryWrapper<AiTask>()
                        .eq(AiTask::getUserId, userId)
                        .orderByDesc(AiTask::getCreatedAt)
                        .orderByDesc(AiTask::getId));
        // 响应结构保持为 List，只返回当前页记录。
        return result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskVO create(TaskCreateRequest request) {
        AiTask task = new AiTask();
        // 任务归属只取自登录态，避免前端伪造 userId 把任务挂到他人名下。
        task.setUserId(SecurityUtils.getUserId());
        task.setName(request.getName().trim());
        task.setPromptTemplate(request.getPromptTemplate());
        task.setModel(request.getModel());
        // 可选字段统一在这里补默认值，保证后续调度器读取任务时字段完整；
        // 补值后仍会经过 validate 校验，非法组合不会被保存。
        task.setRunLocation(defaultIfBlank(request.getRunLocation(), "CLOUD"));
        task.setTimezone(defaultIfBlank(request.getTimezone(), "Asia/Shanghai"));
        task.setScheduleType(defaultIfBlank(request.getScheduleType(), "DAILY"));
        task.setCronExpr(request.getCronExpr());
        task.setIntervalMinutes(request.getIntervalMinutes() == null ? 60 : request.getIntervalMinutes());
        task.setExecuteTime(defaultIfBlank(request.getExecuteTime(), "09:00"));
        task.setWeekDays(request.getWeekDays());
        task.setJitterSeconds(request.getJitterSeconds() == null ? 0 : request.getJitterSeconds());
        task.setMaxRounds(request.getMaxRounds() == null ? 50 : request.getMaxRounds());
        task.setStatus("ENABLED");
        task.setRunCount(0L);
        task.setDeleted(0);

        validate(task);
        aiTaskMapper.insert(task);
        return toVO(aiTaskMapper.selectById(task.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskVO update(Long id, TaskUpdateRequest request) {
        // 先做归属校验，防止用户通过猜测 id 修改他人任务。
        AiTask task = getOwnedTask(id);
        // 记录读取时的状态，作为条件更新的乐观校验条件，避免并发覆盖他人的状态切换。
        String expectedStatus = task.getStatus();
        // 只覆盖请求中非 null 的字段，null 表示“不修改”，
        // 这样前端可以只提交变更项，不必回传完整任务定义。
        if (request.getName() != null) {
            task.setName(request.getName().trim());
        }
        if (request.getPromptTemplate() != null) {
            task.setPromptTemplate(request.getPromptTemplate());
        }
        if (request.getModel() != null) {
            task.setModel(request.getModel());
        }
        if (request.getRunLocation() != null) {
            task.setRunLocation(request.getRunLocation());
        }
        if (request.getTimezone() != null) {
            task.setTimezone(request.getTimezone());
        }
        if (request.getScheduleType() != null) {
            task.setScheduleType(request.getScheduleType());
        }
        if (request.getCronExpr() != null) {
            task.setCronExpr(request.getCronExpr());
        }
        if (request.getIntervalMinutes() != null) {
            task.setIntervalMinutes(request.getIntervalMinutes());
        }
        if (request.getExecuteTime() != null) {
            task.setExecuteTime(request.getExecuteTime());
        }
        if (request.getWeekDays() != null) {
            task.setWeekDays(request.getWeekDays());
        }
        if (request.getJitterSeconds() != null) {
            task.setJitterSeconds(request.getJitterSeconds());
        }
        if (request.getMaxRounds() != null) {
            task.setMaxRounds(request.getMaxRounds());
        }

        validate(task);
        // 条件更新：仅在任务仍处于读取时的状态才允许写入，影响行数为 0 说明期间已被其他请求修改。
        task.setUpdatedAt(LocalDateTime.now());
        int affected = aiTaskMapper.update(task, new LambdaUpdateWrapper<AiTask>()
                .eq(AiTask::getId, id)
                .eq(AiTask::getUserId, SecurityUtils.getUserId())
                .eq(AiTask::getStatus, expectedStatus));
        if (affected == 0) {
            throw new BusinessException(ResultCode.TASK_CONCURRENT_MODIFIED);
        }
        return toVO(aiTaskMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 归属校验通过后才删除；实体使用 @TableLogic，deleteById 实际执行逻辑删除（UPDATE deleted=1）。
        getOwnedTask(id);
        aiTaskMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskVO updateStatus(Long id, String status) {
        // 状态只允许在启用/停用之间切换，避免出现调度器无法识别的中间状态。
        if (!"ENABLED".equals(status) && !"DISABLED".equals(status)) {
            throw new BusinessException(ResultCode.TASK_STATUS_INVALID);
        }
        AiTask task = getOwnedTask(id);
        String expectedStatus = task.getStatus();
        // 状态未变化时直接返回，避免“值相同导致影响行数为 0”被误判为并发冲突。
        if (status.equals(expectedStatus)) {
            return toVO(task);
        }
        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());
        int affected = aiTaskMapper.update(task, new LambdaUpdateWrapper<AiTask>()
                .eq(AiTask::getId, id)
                .eq(AiTask::getUserId, SecurityUtils.getUserId())
                .eq(AiTask::getStatus, expectedStatus));
        if (affected == 0) {
            throw new BusinessException(ResultCode.TASK_CONCURRENT_MODIFIED);
        }
        return toVO(aiTaskMapper.selectById(id));
    }

    private AiTask getOwnedTask(Long id) {
        AiTask task = aiTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        // 水平越权防护：任务存在但归属他人时同样拒绝操作。
        if (!SecurityUtils.getUserId().equals(task.getUserId())) {
            throw new BusinessException(ResultCode.TASK_NOT_OWNED);
        }
        return task;
    }

    private void validate(AiTask task) {
        if (!StringUtils.hasText(task.getName())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "任务名称不能为空");
        }
        if (!StringUtils.hasText(task.getPromptTemplate())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "提示词不能为空");
        }
        if (!RUN_LOCATIONS.contains(task.getRunLocation())) {
            throw new BusinessException(ResultCode.TASK_SCHEDULE_INVALID);
        }
        if (!SCHEDULE_TYPES.contains(task.getScheduleType())) {
            throw new BusinessException(ResultCode.TASK_SCHEDULE_INVALID);
        }
        // 时区必须是合法的 IANA 标识，否则调度器无法计算下次执行时间。
        if (!StringUtils.hasText(task.getTimezone())) {
            throw new BusinessException(ResultCode.TASK_SCHEDULE_INVALID, "时区不能为空");
        }
        try {
            ZoneId.of(task.getTimezone());
        } catch (DateTimeException ex) {
            throw new BusinessException(ResultCode.TASK_SCHEDULE_INVALID, "时区不合法: " + task.getTimezone());
        }
        // 不同调度类型依赖的字段不同，这里做类型相关的必填校验，避免保存后调度器无法计算下次执行时间。
        if ("INTERVAL".equals(task.getScheduleType())
                && (task.getIntervalMinutes() == null || task.getIntervalMinutes() < 1)) {
            throw new BusinessException(ResultCode.TASK_SCHEDULE_INVALID, "INTERVAL 模式需要设置间隔分钟数");
        }
        if ("CRON".equals(task.getScheduleType()) && !StringUtils.hasText(task.getCronExpr())) {
            throw new BusinessException(ResultCode.TASK_SCHEDULE_INVALID, "CRON 模式需要设置 cron 表达式");
        }
        if (StringUtils.hasText(task.getCronExpr())) {
            try {
                // 使用 Spring 自身的解析器校验，保证与后续调度执行时的语义完全一致（6 段，含秒）。
                CronExpression.parse(task.getCronExpr());
            } catch (IllegalArgumentException ex) {
                throw new BusinessException(ResultCode.TASK_SCHEDULE_INVALID, "cron 表达式不合法: " + task.getCronExpr());
            }
        }
        // WEEKLY 未指定星期时默认周一，保证任务有明确的执行日。
        if ("WEEKLY".equals(task.getScheduleType()) && !StringUtils.hasText(task.getWeekDays())) {
            task.setWeekDays("1");
        }
        // weekDays 沿用现有语义：逗号分隔的 1-7（1 表示周一）。
        if (StringUtils.hasText(task.getWeekDays())) {
            for (String segment : task.getWeekDays().split(",")) {
                try {
                    int day = Integer.parseInt(segment.trim());
                    if (day < 1 || day > 7) {
                        throw new NumberFormatException("out of range");
                    }
                } catch (NumberFormatException ex) {
                    throw new BusinessException(ResultCode.TASK_SCHEDULE_INVALID, "weekDays 仅支持 1-7，逗号分隔");
                }
            }
        }
        // maxRounds 限制编排循环上限，jitterSeconds 用于错开触发高峰，二者均限制在安全区间内。
        if (task.getMaxRounds() == null || task.getMaxRounds() < 1 || task.getMaxRounds() > 50) {
            throw new BusinessException(ResultCode.TASK_SCHEDULE_INVALID, "最大轮次需在 1-50 之间");
        }
        if (task.getJitterSeconds() == null || task.getJitterSeconds() < 0 || task.getJitterSeconds() > 60) {
            throw new BusinessException(ResultCode.TASK_SCHEDULE_INVALID, "抖动需在 0-60 秒之间");
        }
    }

    private TaskVO toVO(AiTask task) {
        TaskVO vo = new TaskVO();
        BeanUtils.copyProperties(task, vo);
        return vo;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
