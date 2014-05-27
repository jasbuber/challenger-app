package domain;

import services.ChallengeEvent;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

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

    public Notification() {
    }

    public Notification(User user) {
        this.user = user;
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
}
