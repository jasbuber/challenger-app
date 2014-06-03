package domain;

import services.ChallengeEvent;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "NOTIFICATIONS")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "IS_READ")
    private Character isRead = 'N';

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    @NotNull
    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATION_TIMESTAMP")
    @NotNull
    private Date creationTimestamp = new Date();

    @Column(name = "MESSAGE")
    private String notificationMsg;

    protected Notification() {
    }

    public Notification(User user, String notificationMsg) {
        this.user = user;
        this.notificationMsg = notificationMsg;
    }

    public void read() {
        this.isRead = 'Y';
    }

    public boolean isRead() {
        return isRead == 'Y';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Notification that = (Notification) o;

        return id != null && id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public User getUser() {
        return user;
    }
}
