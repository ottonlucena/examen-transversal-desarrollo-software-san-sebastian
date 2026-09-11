package cl.ipss.coleccion.audit;

import org.hibernate.envers.RevisionListener;

import cl.ipss.coleccion.entity.Revision;

/**
 * Completa el usuario de cada nueva revision de Envers con el usuario de la peticion actual.
 */
public class RevisionUsuarioListener implements RevisionListener {

	@Override
	public void newRevision(Object revisionEntity) {
		((Revision) revisionEntity).setUsuario(UsuarioActual.obtener());
	}
}
