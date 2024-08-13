package models;

public class UsuarioComentarios {
	private Usuario usuario;
	private Comentario comentario;
	
	public UsuarioComentarios(Usuario usuario, Comentario comentario) {
		this.usuario = usuario;
		this.comentario = comentario;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Comentario getComentario() {
		return comentario;
	}

	public void setComentario(Comentario comentario) {
		this.comentario = comentario;
	}

	@Override
	public String toString() {
		return "UsuarioComentarios [usuario=" + usuario + ", comentario=" + comentario + "]";
	}
	
	
	
}
