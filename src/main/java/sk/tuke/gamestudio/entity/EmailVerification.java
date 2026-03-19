package sk.tuke.gamestudio.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_emails")
public class EmailVerification {

    @Id
    @Column(name = "email_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "email")
    private String email;

    @Column(name = "email_code")
    private Integer code;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    protected EmailVerification() {}

    public EmailVerification(String email, Integer code) {
        this.email = email;
        this.code = code;
    }

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Integer getCode() {
        return code;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }
}
