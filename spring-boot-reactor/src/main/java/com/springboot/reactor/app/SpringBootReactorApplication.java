package com.springboot.reactor.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import models.Comentario;
import models.Usuario;
import models.UsuarioComentarios;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner{

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		//ejemploIterable();
		//ejemploFlatMap();
		//ejemploToString(); //transforma un Flux List a un Flux String
		//ejemploCollectList(); //Transforma un observable flux a mono
		//ejemploUsuarioComentariosFlatMap();
		//ejemploUsuarioComentariosZipWith();
		//ejemploUsuarioComentariosZipWith2();
		//ejemploZipWithRangos();
		//ejemploInterval();
		//ejemploDelayElements();
		//ejemploIntervalInfinito();
		//ejemploIntervalDesdeCreate();
		ejemploContraPresion();
	}
	
	public void ejemploContraPresion() {
		Flux.range(1, 10)
		.log()
		//.limitRate(2)
		.subscribe(new Subscriber<Integer>() {
			private Subscription s;
			private Integer limite = 2;
			private Integer consumido = 0;
			
			@Override
			public void onSubscribe(Subscription s) {
				this.s = s;
				s.request(limite);
			}

			@Override
			public void onNext(Integer t) {
				log.info(t.toString());
				consumido++;
				if(consumido == limite) {
					consumido = 0;
					s.request(limite);
				}
			}

			@Override
			public void onError(Throwable t) {
				
			}

			@Override
			public void onComplete() {
				
			}
		});
	}
	
	public void ejemploIntervalDesdeCreate(){
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				private Integer contador = 0;
				@Override
				public void run() {
					emitter.next(++contador);
					if(contador ==10) {
						timer.cancel();
						emitter.complete();
					}
				}
			}, 1000, 1000);
		})
		.doOnNext(next -> log.info(next.toString()))
		.doOnComplete(() -> log.info("Hemos terminado"))
		.subscribe();
	}
	
	public void ejemploIntervalInfinito() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		
		Flux.interval(Duration.ofSeconds(1))
		.doOnTerminate(() -> latch.countDown())
		.flatMap(i -> {
			if(i>=5) {
				return Flux.error(new InterruptedException("Solo hasta 5"));
			}
			return Flux.just(i);
		})
		.map(i -> "Hola "+i)
		.retry(2) //intenta dos veces mas si hay un error
		.subscribe(s -> log.info(s), e -> log.error(e.getMessage()));
		
		latch.await();
		
	}
	
	
	public void ejemploDelayElements() {
		Flux<Integer> rango = Flux.range(1, 12)
			.delayElements(Duration.ofSeconds(1))
			.doOnNext(i -> log.info(i.toString()));
		
		rango.blockLast(); //debe ir subscribe, es para el ejemplo el blockLast
		
	}
	
	public void ejemploInterval() {
		Flux<Integer> rango = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
		
		rango.zipWith(retraso, (ra, re) -> ra)
		.doOnNext(i -> log.info(i.toString()))
		.blockLast(); //debe ir subscribe, es para el ejemplo el blockLast
	}
	
	public void ejemploZipWithRangos() {
		Flux.just(1, 2, 3, 4)
		.map(i -> (i*2))
		.zipWith(Flux.range(0, 4), (uno, dos) -> String.format("Primer flux: %d , Segundo Flux: %d", uno, dos))
		.subscribe(texto -> log.info(texto));
		
	}
	
	public Usuario crearUsuario(String nombre, String apellido){
		return new Usuario(nombre, apellido);
	}
	
	public void ejemploUsuarioComentariosZipWith2() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> crearUsuario("John", "Doe"));
		
		Mono<Comentario> comentariosUsuarioMono = Mono.fromCallable(()->{ 
			Comentario comentario = new Comentario();
			comentario.addComentario("Hola, que tal!");
			comentario.addComentario("Yo muy bien");
			comentario.addComentario("Que haces hoy?");
			return comentario;
				});
		
		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono
				.zipWith(comentariosUsuarioMono)
				.map(tuple -> {
					Usuario u = tuple.getT1();
					Comentario c= tuple.getT2();
					return new UsuarioComentarios(u, c);
				});
				
		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}
	
	public void ejemploUsuarioComentariosZipWith() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> crearUsuario("John", "Doe"));
		
		Mono<Comentario> comentariosUsuarioMono = Mono.fromCallable(()->{ 
			Comentario comentario = new Comentario();
			comentario.addComentario("Hola, que tal!");
			comentario.addComentario("Yo muy bien");
			comentario.addComentario("Que haces hoy?");
			return comentario;
				});
		
		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono
				.zipWith(comentariosUsuarioMono, (usuario, comentariosUsuario) -> new UsuarioComentarios(usuario, comentariosUsuario));
				
		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}
	
	public void ejemploUsuarioComentariosFlatMap() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> crearUsuario("John", "Doe"));
		
		Mono<Comentario> comentariosUsuarioMono = Mono.fromCallable(()->{ 
			Comentario comentario = new Comentario();
			comentario.addComentario("Hola, que tal!");
			comentario.addComentario("Yo muy bien");
			comentario.addComentario("Que haces hoy?");
			return comentario;
				});
		
		usuarioMono.flatMap(u -> comentariosUsuarioMono.map(c -> new UsuarioComentarios(u, c)))
		.subscribe(uc -> log.info(uc.toString()));
	}
	
	public void ejemploCollectList() throws Exception {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Coleman"));
		usuariosList.add(new Usuario("Rocio", "Martinez"));
		usuariosList.add(new Usuario("Pedro", "Capo"));
		usuariosList.add(new Usuario("Diego", "Ramos"));
		usuariosList.add(new Usuario("Juan", "Acosta"));
		Flux.fromIterable(usuariosList)
			.collectList()
			.subscribe(lista -> {
				lista.forEach(item -> log.info(item.toString()));
			
			});
		
	}
	
	public void ejemploToString() throws Exception {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Coleman"));
		usuariosList.add(new Usuario("Rocio", "Martinez"));
		usuariosList.add(new Usuario("Pedro", "Capo"));
		usuariosList.add(new Usuario("Diego", "Ramos"));
		usuariosList.add(new Usuario("Juan", "Acosta"));
		Flux.fromIterable(usuariosList)
				.map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
				.flatMap(nombre -> {
					if(nombre.contains("bruce".toUpperCase())) {
						return Mono.just(nombre);
					} else {
						return Mono.empty();
					}
				})
				.map(nombre -> {
					return nombre.toLowerCase();
					}).subscribe(u -> log.info(u.toString()));
		
	}
	
	public void ejemploFlatMap() throws Exception {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Coleman");
		usuariosList.add("Rocio Martinez");
		usuariosList.add("Pedro Capo");
		usuariosList.add("Diego Ramos");
		usuariosList.add("Juan Acosta");
		Flux.fromIterable(usuariosList).map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(),nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> {
					if(usuario.getNombre().equalsIgnoreCase("bruce")) {
						return Mono.just(usuario);
					} else {
						return Mono.empty();
					}
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase().concat(" ").concat(usuario.getApellido().toLowerCase());
					usuario.setNombre(nombre);
					return usuario;
					}).subscribe(u -> log.info(u.getNombre()));
		
	}
	
	public void ejemploIterable() throws Exception {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Coleman");
		usuariosList.add("Rocio Martinez");
		usuariosList.add("Pedro Capo");
		usuariosList.add("Diego Ramos");
		usuariosList.add("Juan Acosta");
		Flux<String> nombres = Flux.fromIterable(usuariosList); /*Flux.just("Bruce Lee", "Bruce Coleman","Rocio Martinez","Pedro Capo", "Diego Ramos", "Juan Acosta");*/
		
				Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(),nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().equalsIgnoreCase("bruce"))
				.doOnNext(usuario -> {
					if(usuario.getNombre().isEmpty()) {
						throw new RuntimeException("Nombres no pueden ser vacíos");
					}
					else {
						System.out.println(usuario.getNombre().concat(" ".concat(usuario.getApellido())));
					}
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase().concat(" ").concat(usuario.getApellido().toLowerCase());
					usuario.setNombre(nombre);
					return usuario;
					});
				
		
		usuarios.subscribe(e -> log.info(e.getNombre()), error -> log.error(error.getMessage()),
				new Runnable() {
					@Override
					public void run() {
						log.info("Ha finalizado la ejecucón del observable con éxito!");
					}
				} );
		
	}

}
