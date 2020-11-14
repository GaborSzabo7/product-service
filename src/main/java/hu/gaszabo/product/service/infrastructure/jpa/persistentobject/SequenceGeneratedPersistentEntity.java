package hu.gaszabo.product.service.infrastructure.jpa.persistentobject;

import static javax.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PROTECTED;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = PROTECTED)
@MappedSuperclass
public class SequenceGeneratedPersistentEntity extends PersistentEntity {

	private static final long serialVersionUID = 4629290592133691984L;

	@Id
	@SequenceGenerator(name = "APP_SEQ", sequenceName = "APP_SEQ", allocationSize = 100)
	@GeneratedValue(strategy = SEQUENCE, generator = "APP_SEQ")
	private Long id;

}
