package controllers;

import play.data.validation.Constraints;

/**
 * Created by jasbuber on 2014-12-07.
 */
public class CreateCommentForm {

    @Constraints.Required(message = "If you want to post a comment, write it first -_-")
    @Constraints.MaxLength(value = 250, message = "Only 250 characters allowed. It's just a comment, not a poem ;]")
    private String message;

    private long relevantObjectId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getRelevantObjectId() {
        return relevantObjectId;
    }

    public void setRelevantObjectId(long relevantObjectId) {
        this.relevantObjectId = relevantObjectId;
    }
}
