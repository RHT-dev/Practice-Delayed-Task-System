package com.example.demo.JDBC_TEMPLATE;

import com.example.demo.entityDB.TaskEntity;
import com.example.demo.entityDB.TaskStatus;
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

@Service
// @RequiredArgsConstructor
public class DynamicTaskDao {
    private final JdbcTemplate jdbc;
    private static final String BASE_DDL =
            """
            CREATE TABLE IF NOT EXISTS %s (
                        id            BIGINT PRIMARY KEY AUTO_INCREMENT,
                        task_class_name    VARCHAR(255)   NOT NULL,
                        params_json   TEXT           NOT NULL,
                        retry_params_json    TEXT,
                        retry_type    ENUM('CONSTANT','EXPONENT') NOT NULL,
                        scheduled_time  TIMESTAMP      NOT NULL,
                        attempt_count   INT            NOT NULL,
                        max_attempts  INT            NOT NULL,
                        status        ENUM('CONSIDERED','PROCESSING','SUCCESS','FAILED','CANCELED') NOT NULL,
                        version       BIGINT         NOT NULL
                    ) ENGINE=InnoDB
            """;

    private String table(String category) {
        return "task_category_" + category.toLowerCase(Locale.ROOT);
    }

    private void ensureTable(String category) {
        jdbc.execute(BASE_DDL.formatted(table(category)));
    }


    public long save(TaskEntity task) {
        ensureTable(task.getCategory());
        var sql = """
                INSERT INTO %s (task_class_name, params_json, retry_params_json, retry_type,
                scheduled_time, attempt_count, max_attempts, status, version)
                
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.formatted(table(task.getCategory()));

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, task.getTaskClassName());
            ps.setString(2, task.getParamsJSON());
            ps.setString(3, task.getRetryParamsJSON());
            ps.setString(4, task.getRetryType().name());
            ps.setTimestamp(5, Timestamp.valueOf(task.getScheduledTime()));
            ps.setInt(6, 0);
            ps.setInt(7, task.getMaxAttempts());
            ps.setString(8, TaskStatus.CONSIDERED.name());
            ps.setLong(9, 0);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public List<TaskEntity> findReadyTasks(String category) {
        ensureTable(category);

        var sql = """
                SELECT * from %s
                WHERE status = 'CONSIDERED' AND scheduled_time <= ?
                """.formatted(table(category));

        return jdbc.query(sql,
                (rs, n) -> TaskEntity.fromRs(rs),
                Timestamp.valueOf(LocalDateTime.now()));
    }

    public int updateStatus(long id, String category, TaskStatus expectedStatus, TaskStatus newStatus) {
        ensureTable(category);

        var sql = """
                UPDATE %s SET status = ? WHERE id = ? AND status = ?
                """.formatted(table(category));

        return jdbc.update(sql, newStatus.name(), expectedStatus.name());
    }

    public void finalStatus(long id, String category, TaskStatus status) {
        var sql = """
                UPDATE %s SET status = ? WHERE id = ? 
                """.formatted(table(category));
        jdbc.update(sql, status.name(), id);
    }
}
