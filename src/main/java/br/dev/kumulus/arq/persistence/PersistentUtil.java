package br.dev.kumulus.arq.persistence;

public class PersistentUtil {
	
	private PersistentUtil() {
		// constructor not implement
	}

	public static boolean hasSameId(Persistent p1, Persistent p2) {
		boolean result = false;

		if (p1 == null || p2 == null)
			throw new IllegalArgumentException("Os objetos a serem comparados não podem estar nulos");

		if (p1.getId() != null && p2.getId() != null && p1.getId().equals(p2.getId())) {
			result = true;
		}

		return result;
	}

}
