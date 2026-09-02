CREATE TABLE todos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    detail VARCHAR(255) NULL,
    category VARCHAR(255) NOT NULL,
    priority INT NOT NULL DEFAULT 2,
    due_date DATE NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT chk_todos_category
        CHECK (category IN (
            'デザイン',
            'マーケティング',
            'プログラミング',
            '資格',
            '就職活動'
        )),
    CONSTRAINT chk_todos_priority
        CHECK (priority IN (1, 2, 3)),
    CONSTRAINT chk_todos_completed
        CHECK (completed IN (0, 1)),
    INDEX idx_todos_category (category),
    INDEX idx_todos_due_date (due_date)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

INSERT INTO todos (title, detail, category, priority, due_date, completed) VALUES
    ('todo-06', NULL, '繝・じ繧､繝ｳ', 2, NULL, FALSE),
    ('todo-07', NULL, '繝・じ繧､繝ｳ', 2, NULL, FALSE),
    ('todo-08', NULL, '繝・じ繧､繝ｳ', 2, NULL, FALSE),
    ('todo-09', NULL, '繝・じ繧､繝ｳ', 2, NULL, FALSE),
    ('todo-10', NULL, '繝・じ繧､繝ｳ', 2, NULL, FALSE),
    ('todo-11', NULL, '繝・じ繧､繝ｳ', 2, NULL, FALSE),
    ('todo-12', NULL, '繝・じ繧､繝ｳ', 2, NULL, FALSE),
    ('todo-13', NULL, '繝・じ繧､繝ｳ', 2, NULL, FALSE),
    ('todo-14', NULL, '繝・じ繧､繝ｳ', 2, NULL, FALSE),
    ('todo-15', NULL, '繝・じ繧､繝ｳ', 2, NULL, FALSE);
