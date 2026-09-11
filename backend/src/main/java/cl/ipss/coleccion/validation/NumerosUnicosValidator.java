package cl.ipss.coleccion.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import cl.ipss.coleccion.dto.LaminaRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementacion de {@link NumerosUnicos}. El mensaje de error indica que numeros se repiten.
 */
public class NumerosUnicosValidator implements ConstraintValidator<NumerosUnicos, List<LaminaRequest>> {

	@Override
	public boolean isValid(List<LaminaRequest> laminas, ConstraintValidatorContext contexto) {
		if (laminas == null) {
			return true; // la obligatoriedad la valida @NotEmpty
		}
		Set<Integer> vistos = new HashSet<>();
		Set<Integer> repetidos = new TreeSet<>();
		for (LaminaRequest lamina : laminas) {
			if (lamina != null && lamina.numero() != null && !vistos.add(lamina.numero())) {
				repetidos.add(lamina.numero());
			}
		}
		if (repetidos.isEmpty()) {
			return true;
		}
		String lista = repetidos.stream().map(String::valueOf).collect(Collectors.joining(", "));
		contexto.disableDefaultConstraintViolation();
		contexto.buildConstraintViolationWithTemplate("El lote contiene números repetidos: " + lista)
				.addConstraintViolation();
		return false;
	}
}
