package com.example.demo.repository;

import com.example.demo.entity.TaskEntity;
import com.example.demo.entity.TaskStatus;
import jakarta.transaction.Transactional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class DynamicTaskTableDao {
    private final JdbcTemplate jdbc;
    private static final String BASE_DDL =
            """
            CREATE TABLE IF NOT EXISTS %s (
                        id            BIGINT PRIMARY KEY AUTO_INCREMENT,
                        task_class_name    VARCHAR(255)   NOT NULL,
                        params_json   TEXT           NOT NULL,
                        retry_params_json    TEXT,
                        retry_type ENUM('NONE','CONSTANT','EXPONENT') DEFAULT 'NONE',
                        scheduled_time  TIMESTAMP      NOT NULL,
                        attempt_count   INT            NOT NULL,
                        max_attempts  INT            NOT NULL,
                        status         VARCHAR(100)     NOT NULL
                    ) ENGINE=InnoDB
            """;

    public DynamicTaskTableDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private String table(String category) {
        return "task_category_" + category.toLowerCase(Locale.ROOT);
    }

    private void ensureTable(String category) {
        jdbc.execute(BASE_DDL.formatted(table(category)));
    }

    public long save(TaskEntity task) {

        ensureTable(task.getCategory());
        String sql = """
            INSERT INTO %s (task_class_name, params_json, retry_params_json, retry_type,
                            scheduled_time, attempt_count, max_attempts, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.formatted(table(task.getCategory()));

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, task.getTaskClassName());
            ps.setString(2, task.getParamsJSON());
            ps.setString(3, task.getRetryParamsJSON());
            ps.setString(4, task.getRetryType() != null ? task.getRetryType().name() : null);
            ps.setTimestamp(5, Timestamp.valueOf(task.getScheduledTime()));
            ps.setInt(6, 0);
            ps.setInt(7, task.getMaxAttempts());
            ps.setString(8, TaskStatus.CONSIDERED.name());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Transactional
    public Optional<TaskEntity> fetchReadyTask(String category) {
        ensureTable(category);
        String selectIdSql = """
        SELECT id FROM %s
        WHERE status = 'CONSIDERED' AND scheduled_time <= ?
        ORDER BY scheduled_time ASC
        LIMIT 1
        FOR UPDATE
    """.formatted(table(category));

        List<Long> ids = jdbc.queryForList(selectIdSql, Long.class, Timestamp.valueOf(LocalDateTime.now()));
        if (ids.isEmpty()) return Optional.empty();

        long id = ids.get(0);

        String updateSql = """
        UPDATE %s SET status = ? WHERE id = ?
    """.formatted(table(category));
        jdbc.update(updateSql, TaskStatus.RUNNING.name(), id);

        String fetchSql = """
        SELECT * FROM %s WHERE id = ?
    """.formatted(table(category));

        TaskEntity task = jdbc.queryForObject(fetchSql, (rs, n) -> TaskEntity.fromRs(rs), id);
        return Optional.of(task);
    }

    public TaskStatus getStatus(long id, String category) {
        ensureTable(category);
        return jdbc.queryForObject(
                "SELECT status FROM %s WHERE id=?".formatted(table(category)),
                (rs, n) -> TaskStatus.valueOf(rs.getString(1)),
                id);
    }

    public int updateStatus(long id, String category, TaskStatus expected, TaskStatus next) {
        ensureTable(category);
        String sql = """
            UPDATE %s SET status=? WHERE id=? AND status=?
            """.formatted(table(category));
        return jdbc.update(sql, next.name(), id, expected.name());
    }

    public void finalStatus(long id, String category, TaskStatus status) {
        var sql = """
                UPDATE %s SET status = ? WHERE id = ? 
                """.formatted(table(category));
        jdbc.update(sql, status.name(), id);
    }

    public void updateForRetry(TaskEntity task, String category) {
        ensureTable(category);
        var sql = """
            UPDATE %s
            SET scheduled_time = ?, attempt_count = ?, status = ?
            WHERE id = ?
            """.formatted(table(category));
        jdbc.update(sql,
                Timestamp.valueOf(task.getScheduledTime()),
                task.getAttemptCount(),
                task.getStatus().name(),
                task.getId());
    }

    public List<String> getAllCategories() {
        String sql = """
        SELECT table_name
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name LIKE 'task_category_%'
        """;

        List<String> tables = jdbc.queryForList(sql, String.class);

        return tables.stream()
                .map(name -> name.substring("task_category_".length()))
                .toList();
    }

    public TaskEntity getTaskById(long id, String category) {
        ensureTable(category);
        String sql = """
        SELECT * FROM %s WHERE id = ?
    """.formatted(table(category));
        List<TaskEntity> list = jdbc.query(sql, (rs, n) -> TaskEntity.fromRs(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<TaskEntity> fetchAllConsidered(String category) {
        ensureTable(category);
        String sql = """
        SELECT * FROM %s
        WHERE status = 'CONSIDERED'
    """.formatted(table(category));
        return jdbc.query(sql, (rs, n) -> TaskEntity.fromRs(rs));
    }

}
