package com.springboot.webflux.app.models.dao;

import com.springboot.webflux.app.models.documents.Producto;

import reactor.core.publisher.Mono;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductoDao extends ReactiveMongoRepository<Producto,String>{

	public Mono<Producto> findByNombre(String nombre);
}
