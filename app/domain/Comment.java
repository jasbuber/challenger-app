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
    @GeneratedValue(strategy = GenerationType.AUTO)
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
    private String relevantObjectId;

    public Comment(){}

    public Comment(User author, String message, String relevantObjectId){
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

    public String getRelevantObjectId() {
        return relevantObjectId;
    }
}
