# DB設計書

## 1. 文書の目的

この文書では、やること管理アプリが使うデータベース（データを保存する仕組み）の構造を定めます。

画面の配置や見た目は、この文書では定めません。アプリが使うテーブル（データを表形式で保存する場所）は、前提どおり `todos` の1つだけとします。

## 2. 設計方針

- データベースは MySQL を使用します。
- 1件のやることを、`todos` テーブルの1行で表します。
- 利用者は1人で、ログイン情報や利用者IDは保存しません。
- `id` は主キー（1行を一意に見分ける番号）とします。
- ジャンルと優先度は、決められた値だけを保存できるようにします。
- 登録日時と最終更新日時は、MySQLが自動で設定します。
- 削除したやることを元に戻すためのデータは保存しません。

### 2.1 テーブル構成

| テーブル名 | 用途 | 1行が表すもの |
|---|---|---|
| `todos` | やることの保存 | 1件のやること |

このアプリではテーブル同士の関係（リレーション）はありません。`todos` だけを使用するためです。

## 3. `todos` テーブル定義

### 3.1 カラム一覧

カラム（表の縦方向の項目）ごとの定義は次のとおりです。

| カラム名 | データ型 | NULL | 初期値 | 制約・内容 | 対応する要件 |
|---|---|---|---|---|---|
| `id` | `BIGINT` | 不可 | 自動採番 | 主キー。保存時に1ずつ増える番号 | 仕様書「保存するもの」、画面URLの `{id}`、FR-03、FR-04 |
| `title` | `VARCHAR(255)` | 不可 | なし | やることの名前。255文字以内。空白だけは不可 | 要件 4.1、FR-01〜FR-05、要件 6.1〜6.2 |
| `detail` | `VARCHAR(255)` | 可 | `NULL` | メモ。255文字以内。省略可能 | 要件 4.1、FR-02、FR-03、要件 6.2 |
| `category` | `VARCHAR(255)` | 不可 | なし | ジャンル。5種類のいずれか | 要件 4.1〜4.2、FR-01〜FR-05、要件 6.1〜6.2 |
| `priority` | `INT` | 不可 | `2` | 優先度。`1`=高、`2`=中、`3`=低 | 要件 4.1、4.3、FR-01〜FR-03、要件 6.1〜6.2 |
| `due_date` | `DATE` | 可 | `NULL` | 期限。省略可能。日付だけを保存 | 要件 4.1、FR-01、FR-02、FR-03、FR-04、FR-06 |
| `completed` | `BOOLEAN` | 不可 | `FALSE` | 完了状態。`FALSE`=未完了、`TRUE`=完了 | 要件 4.1、4.3、FR-01、FR-03 |
| `created_at` | `DATETIME` | 不可 | 現在日時 | 登録日時。MySQLが自動設定 | 要件 4.1、操作記録に関する対象外事項 |
| `updated_at` | `DATETIME` | 不可 | 現在日時 | 最終更新日時。登録時と更新時にMySQLが自動設定 | 要件 4.1、FR-03 |

### 3.2 保存する値の詳細

#### `category`（ジャンル）

`category` には自由入力を許可せず、次の5つのどれかだけを保存します。

| 保存値 |
|---|
| `デザイン` |
| `マーケティング` |
| `プログラミング` |
| `資格` |
| `就職活動` |

#### `priority`（優先度）

画面上の表示名と、データベースに保存する数値を対応させます。

| 保存値 | 画面表示 |
|---:|---|
| `1` | 高 |
| `2` | 中 |
| `3` | 低 |

画面では優先度を必ず選択します。DBの初期値 2（中）は、画面以外から値が指定されなかった場合の予備です。

#### `completed`（完了状態）

| 保存値 | 画面表示 |
|---|---|
| `0`（`FALSE`） | 未完了 |
| `1`（`TRUE`） | 完了 |

`BOOLEAN` は、はい・いいえのような2択を保存するデータ型（データの種類）です。MySQLでは内部的に `0` または `1` として扱われます。

## 4. 制約と入力チェック

制約（保存してよい値を制限する決まり）は、データベース側とアプリ側で分担します。

### 4.1 データベース側で守ること

- `id` は主キーとし、同じ番号を重複させません。
- `title`、`category`、`priority`、`completed`、`created_at`、`updated_at` は `NULL`（値がない状態）を許可しません。
- `detail` と `due_date` は省略できるため、値がない場合は `NULL` を保存します。
- `priority` は `1`、`2`、`3` だけを許可します。
- `category` は指定された5種類だけを許可します。
- `completed` は `0` または `1` だけを許可します。
- `title` と `detail` は `VARCHAR(255)` とし、255文字を超える値を保存しません。

### 4.2 アプリ側で行うこと

入力チェック（保存前に入力内容を確認する処理）は、画面から保存する前にアプリ側で行います。

- `title` が未入力、または半角・全角スペースだけの場合は保存しません。
- `title` が255文字を超える場合は保存しません。
- `detail` が255文字を超える場合は保存しません。
- `category` が未選択の場合は保存しません。
- `priority` が未選択の場合は保存しません。
- `due_date` には、仕様書で指定された入力チェックを設けません。

入力エラーの文言は、要件定義書の「6.2 入力チェック」に従います。データベースは画面に表示するエラー文を管理しません。

## 5. DDL（テーブル作成SQL）

DDL（Data Definition Language、テーブルなどの構造を作るSQL）は次のとおりです。MySQL 8.0.16以降で使用する想定です。

```sql
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
```

### 5.1 DDLの補足

- `AUTO_INCREMENT` は、番号を自動で1ずつ増やす仕組みです。
- `NOT NULL` は、値を必須にする指定です。
- `DEFAULT` は、値が指定されなかった場合に使う初期値です。
- `ON UPDATE CURRENT_TIMESTAMP` により、行を更新したときに `updated_at` が自動更新されます。
- `utf8mb4` は、日本語を含む文字を保存するための文字コード（文字の保存方法）です。
- `InnoDB` は、MySQLで一般的に使われる保存方式です。
- `idx_todos_category` と `idx_todos_due_date` はインデックス（検索や並び替えを速くするための目次）です。
- `title` の一部を含む検索は、要件どおり `LIKE '%検索語%'` で行います。この検索は通常のインデックスが効きにくいため、まずは追加の全文検索機能を設けません。

## 6. 要件とカラムの対応

### 6.1 機能要件との対応

| 要件ID | 要件の概要 | 使用するカラム | DBでの扱い |
|---|---|---|---|
| FR-01 | 一覧表示 | `id`, `title`, `category`, `priority`, `due_date`, `completed` | `todos` から取得して表示します |
| FR-02 | 新規登録 | `title`, `detail`, `category`, `priority`, `due_date`, `completed` | 1行を追加します。`completed` は未完了で登録します |
| FR-03 | 編集 | `id`, `title`, `detail`, `category`, `priority`, `due_date`, `completed`, `updated_at` | `id` で対象行を特定して更新します |
| FR-04 | 削除 | `id` | `id` で対象行を特定して削除します |
| FR-05 | 名前・ジャンルで絞り込み | `title`, `category` | `title` は一部一致、`category` は完全一致で検索します |
| FR-06 | 期限で並び替え | `due_date` | 期限の近い順または遠い順で並べます |

### 6.2 その他の要件との対応

| 要件 | 対応する設計 |
|---|---|
| 必須項目 | `title`, `category`, `priority` を `NOT NULL` にし、保存前の空白チェックはアプリ側で行います |
| メモと期限は省略可能 | `detail`, `due_date` を `NULL` 可にします |
| 優先度の初期値は「中」 | `priority DEFAULT 2` |
| 完了状態の初期値は「未完了」 | `completed DEFAULT FALSE` |
| 登録日時を記録 | `created_at DEFAULT CURRENT_TIMESTAMP` |
| 最終更新日時を記録 | `updated_at ... ON UPDATE CURRENT_TIMESTAMP` |
| 存在しないIDへの対応 | 対象行が取得できない場合は、アプリ側で一覧へ戻し「見つかりませんでした」と表示します |
| ページ分け、復元、完了日時、ログイン、共有、添付は対象外 | これらのためのカラムやテーブルは作りません |

## 7. 検索・並び替えでの利用方法

### 7.1 名前とジャンルで絞り込む

`title` は入力された文字を一部に含むかで検索し、`category` は値が完全に一致するかで検索します。両方が指定された場合は、両方に一致する行だけを取得します。

```sql
SELECT id, title, category, priority, due_date, completed
FROM todos
WHERE title LIKE CONCAT('%', :keyword, '%')
  AND category = :category;
```

`:keyword` と `:category` は、アプリから渡す検索条件です。条件が指定されていない場合は、その条件を `WHERE`（検索条件）から外します。

### 7.2 期限で並び替える

初期表示は期限の近い順とします。期限がない行は、期限がある行の後ろに表示する想定です。

```sql
-- 期限が近い順
SELECT id, title, category, priority, due_date, completed
FROM todos
ORDER BY due_date IS NULL, due_date ASC, id ASC;

-- 期限が遠い順
SELECT id, title, category, priority, due_date, completed
FROM todos
ORDER BY due_date IS NULL, due_date DESC, id DESC;
```

`id` も並びに加えているのは、同じ期限の行の順番を安定させるためです。

## 8. 操作ログの扱い

要件定義書では、登録・編集・削除が成功したときに、操作の種類と対象IDを1行のログ（操作の記録）として残すことが求められています。一方、このアプリで使うテーブルは `todos` の1つだけという前提があります。

そのため、操作ログ用のテーブルや `todos` のログ用カラムは追加しません。登録・編集・削除の成功時に、アプリケーションログ（アプリが別途出力する操作記録）へ次の2項目だけを書き出します。

| ログ項目 | 内容 |
|---|---|
| 操作種別 | 登録・編集・削除のいずれか |
| 対象ID | 操作対象だった `todos.id` |

`title` と `detail` の内容はログへ出力しません。ログの保存先や出力形式は、アプリケーションの実装設計で定めます。

## 9. 見出し一覧

1. 文書の目的
2. 設計方針
3. `todos` テーブル定義
4. 制約と入力チェック
5. DDL（テーブル作成SQL）
6. 要件とカラムの対応
7. 検索・並び替えでの利用方法
8. 操作ログの扱い
9. 見出し一覧
