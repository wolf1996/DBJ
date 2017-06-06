CREATE OR REPLACE FUNCTION on_insert_post_or_thread()
  RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO forum_users (user_id, forum_id) VALUES (NEW.user_id, NEW.forum_id);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER post_insert_trigger
AFTER INSERT ON posts
FOR EACH ROW EXECUTE PROCEDURE on_insert_post_or_thread();

CREATE TRIGGER thread_insert_trigger
AFTER INSERT ON threads
FOR EACH ROW EXECUTE PROCEDURE on_insert_post_or_thread();

--

CREATE OR REPLACE FUNCTION update_or_insert_votes(vote_user_id INTEGER, vote_thread_it INTEGER, vote_value INTEGER)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO votes (user_id, thread_id, voice) VALUES (vote_user_id, vote_thread_it, vote_value)
  ON CONFLICT (user_id, thread_id)
    DO UPDATE SET voice = vote_value;
  UPDATE threads
  SET votes = (SELECT SUM(voice)
               FROM votes
               WHERE thread_id = vote_thread_it)
  WHERE id = vote_thread_it;
END;
$$ LANGUAGE plpgsql;