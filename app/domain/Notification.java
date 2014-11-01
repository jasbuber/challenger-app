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
    @NotNull
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

    @Column(name = "SHORT_MESSAGE")
    private String shortNotificationMsg;

    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    @NotNull
    private NotificationType notificationType;

    @Column(name = "RELEVANT_OBJECT_ID")
    private String relevantObjectId;

    public enum NotificationType {
        new_participant, participant_left, new_response, response_accepted, response_refused, challenge_completed, challenge_invitation
    }

    protected Notification() {
    }

    public Notification(User user, String notificationMsg) {
        this.user = user;
        this.notificationMsg = notificationMsg;
    }

    public Notification(User user, NotificationType type, String notificationMsg, String shortNotificationMsg) {
        this.user = user;
        this.notificationMsg = notificationMsg;
        this.shortNotificationMsg = shortNotificationMsg;
        this.notificationType = type;
    }

    public Notification(User user, NotificationType type, String notificationMsg, String shortNotificationMsg, String relevantObjectId) {
        this.user = user;
        this.notificationMsg = notificationMsg;
        this.shortNotificationMsg = shortNotificationMsg;
        this.notificationType = type;
        this.relevantObjectId = relevantObjectId;
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

    public String getNotificationMsg() {
        return notificationMsg;
    }

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public Character getIsRead() {
        return isRead;
    }

    public Long getId() {
        return id;
    }

    public String getShortNotificationMsg() {
        return shortNotificationMsg;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public String getRelevantObjectId() {
        return relevantObjectId;
    }
}
