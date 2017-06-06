package DBPackage.models.interfaces;

import DBPackage.views.*;

/**
 * Created by ksg on 20.05.17.
 */
public interface Thread {
    ThreadView insertThread(String author, String created, String forum, String message, String slug, String title);
    void updateThread(String message, String title, String slug_or_id);
    ThreadView getThread(String slug_or_id);
    ThreadView updateVotes(VoteView view, String slug_or_id);
    Integer countThreads();
    void clearThreadsTable();
}
