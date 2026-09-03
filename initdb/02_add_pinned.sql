SET @pinned_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'todos' AND column_name = 'pinned');
SET @add_pinned_sql = IF(@pinned_exists = 0, 'ALTER TABLE todos ADD COLUMN pinned BOOLEAN NOT NULL DEFAULT FALSE AFTER due_date', 'SELECT 1');
PREPARE add_pinned_stmt FROM @add_pinned_sql;
EXECUTE add_pinned_stmt;
DEALLOCATE PREPARE add_pinned_stmt;
