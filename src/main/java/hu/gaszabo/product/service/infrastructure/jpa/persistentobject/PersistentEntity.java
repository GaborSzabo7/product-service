package hu.gaszabo.product.service.infrastructure.jpa.persistentobject;

import static lombok.AccessLevel.PROTECTED;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.InstantConverter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = PROTECTED)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class PersistentEntity implements Serializable {

	private static final long serialVersionUID = -1474282195013092487L;

	@Column(name = "CREATION_DATE", nullable = false)
	@Convert(converter = InstantConverter.class)
	@CreatedDate
	private Instant creationDate;

	@Column(name = "LAST_UPDATE_DATE", nullable = false)
	@Convert(converter = InstantConverter.class)
	@LastModifiedDate
	private Instant lastUpdateDate;

	@Column(name = "VERSION", nullable = false)
	@Version
	private Long version;

}
