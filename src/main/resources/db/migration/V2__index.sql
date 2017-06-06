DROP INDEX IF EXISTS user_id_forums_idx;

CREATE INDEX IF NOT EXISTS user_id_forums_idx
  ON forums (user_id);

--

DROP INDEX IF EXISTS user_id_threads_idx;
DROP INDEX IF EXISTS forum_id_threads_idx;

CREATE INDEX IF NOT EXISTS user_id_threads_idx
  ON threads (user_id);
CREATE INDEX IF NOT EXISTS forum_id_threads_idx
  ON threads (forum_id);

--

DROP INDEX IF EXISTS user_id_posts_idx;
DROP INDEX IF EXISTS forum_id_posts_idx;
DROP INDEX IF EXISTS flat_sort_posts_idx;
DROP INDEX IF EXISTS tree_sort_posts_idx;
DROP INDEX IF EXISTS parent_tree_sort_posts_idx;
DROP INDEX IF EXISTS parent_tree_sort_posts_sub_idx;

CREATE INDEX IF NOT EXISTS user_id_posts_idx
  ON posts (user_id);
CREATE INDEX IF NOT EXISTS forum_id_posts_idx
  ON posts (forum_id);
CREATE INDEX IF NOT EXISTS flat_sort_posts_idx
  ON posts (thread_id, created, id);
CREATE INDEX IF NOT EXISTS tree_sort_posts_idx
  ON posts (thread_id, path);
CREATE INDEX IF NOT EXISTS parent_tree_sort_posts_idx
  ON posts (root_id, path);
CREATE INDEX IF NOT EXISTS parent_tree_sort_posts_sub_idx
  ON posts (thread_id, parent, id);

--

DROP INDEX IF EXISTS user_id_forum_users_idx;
DROP INDEX IF EXISTS forum_id_forum_users_idx;

CREATE INDEX IF NOT EXISTS user_id_forum_users_idx
  ON forum_users (user_id);
CREATE INDEX IF NOT EXISTS forum_id_forum_users_idx
  ON forum_users (forum_id);