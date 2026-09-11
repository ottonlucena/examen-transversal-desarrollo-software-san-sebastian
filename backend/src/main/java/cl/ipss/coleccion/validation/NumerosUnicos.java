package cl.ipss.coleccion.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Valida que una lista de laminas no repita el mismo numero. Se usa en la carga en lote:
 * dos laminas con el mismo numero en una misma peticion son un error del cliente (HTTP 400).
 */
@Documented
@Constraint(validatedBy = NumerosUnicosValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
public @interface NumerosUnicos {

	String message() default "El lote contiene números de lámina repetidos";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
