package domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by jasbuber on 2014-12-07.
 */
@Entity
@Table(name = "COMMENTS")
public class Comment {

    @Id
    @SequenceGenerator(name = "COMMENTS_SEQ_GEN", sequenceName = "COMMENTS_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COMMENTS_SEQ_GEN")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "AUTHOR_ID")
    @NotNull
    private User author;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATION_TIMESTAMP")
    @NotNull
    private Date creationTimestamp = new Date();

    @Column(name = "MESSAGE")
    private String message;

    @Column(name = "RELEVANT_OBJECT_ID")
    private long relevantObjectId;

    public Comment(){}

    public Comment(User author, String message, long relevantObjectId){
        this.author = author;
        this.message = message;
        this.relevantObjectId = relevantObjectId;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "author='" + author + '\'' +
                ", objectId='" + relevantObjectId + '\'' +
                ",time=" + creationTimestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return false;
    }

    @Override
    public int hashCode() {
        int result = message.hashCode();
        result = 31 * result + author.hashCode();
        return result;
    }

    public Long getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public String getMessage() {
        return message;
    }

    public long getRelevantObjectId() {
        return relevantObjectId;
    }
}
