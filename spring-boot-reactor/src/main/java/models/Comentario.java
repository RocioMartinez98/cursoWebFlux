package models;

import java.util.ArrayList;
import java.util.List;

public class Comentario {

	private List<String> comentarios;

	public Comentario() {
		this.comentarios = new ArrayList<>();
	}

	public void addComentario(String comentario) {
		comentarios.add(comentario);
	}

	@Override
	public String toString() {
		return "comentarios= "+comentarios;
	}
	
	
	
}
