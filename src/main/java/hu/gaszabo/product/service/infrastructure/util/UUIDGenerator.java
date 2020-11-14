package hu.gaszabo.product.service.infrastructure.util;

import static java.lang.Character.MAX_RADIX;

import java.time.Instant;
import java.util.UUID;

public final class UUIDGenerator {

	private UUIDGenerator() {
		throw new UnsupportedOperationException("Class used to generate UUID");
	}

	public static String generate() {
		final UUID uuid = UUID.randomUUID();

		return Long.toString(Instant.now().toEpochMilli(), MAX_RADIX) //
				+ Long.toString(uuid.getMostSignificantBits(), MAX_RADIX) //
				+ Long.toString(uuid.getLeastSignificantBits(), MAX_RADIX);
	}

}
