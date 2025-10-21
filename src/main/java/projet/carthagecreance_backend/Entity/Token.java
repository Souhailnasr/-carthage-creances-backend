package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import org.hibernate.annotations.CreationTimestamp;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Token implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final LocalDateTime LocalDateTime = null;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer TokenId;

	@Column(unique = true ,name = "token", length = 2048)
	public String token;

	@Enumerated(EnumType.STRING)
	public TokenType tokenType = TokenType.BEARER;

	public boolean revoked;

	public boolean expired;
	
	// Getters et setters pour compatibilité
	public String getToken() { return token; }
	public void setToken(String token) { this.token = token; }
	
	public boolean isExpired() { return expired; }
	public void setExpired(boolean expired) { this.expired = expired; }
	
	public boolean isRevoked() { return revoked; }
	public void setRevoked(boolean revoked) { this.revoked = revoked; }
	
	public LocalDateTime getExpireAt() { return expireAt; }
	public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
	
	@CreationTimestamp
    @Column(updatable = false)
    private Timestamp timeStamp;

    @Column(updatable = false)
    //@Basic(optional = false)
    private LocalDateTime expireAt;

	@JsonBackReference
	@ManyToOne
	@JoinColumn(name = "user_id")
	public Utilisateur user;
}
