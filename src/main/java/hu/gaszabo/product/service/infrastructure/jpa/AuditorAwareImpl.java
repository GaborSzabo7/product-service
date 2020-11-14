package hu.gaszabo.product.service.infrastructure.jpa;

import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.springframework.util.Assert.isTrue;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

@Component("auditorProvider")
public class AuditorAwareImpl implements AuditorAware<String> {

	private final String auditUser;

	@Autowired
	public AuditorAwareImpl(@Value("${audit.user}") final String auditUser) {
		isTrue(isNoneBlank(auditUser), "audit.user can't be blank");
		this.auditUser = auditUser;
	}

	@Override
	public Optional<String> getCurrentAuditor() {
		return Optional.of(auditUser);

	}
}
