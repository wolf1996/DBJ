package DBPackage.views;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ksg on 10.03.17.
 */
public class VoteView {
    private String nickname;
    private Integer voice;

    @JsonCreator
    public VoteView(
            @JsonProperty("nickname") final String nickname,
            @JsonProperty("voice") final Integer voice
    ) {
        this.nickname = nickname;
        this.voice = voice;
    }

    public VoteView(final VoteView other) {
        this.nickname = other.nickname;
        this.voice = other.voice;
    }

    public final String getNickname() {
        return this.nickname;
    }

    public void setNickname(final String nickname) {
        this.nickname = nickname;
    }

    public final Integer getVoice() {
        return this.voice;
    }

    public void setVoice(final Integer voice) {
        this.voice = voice;
    }
}
