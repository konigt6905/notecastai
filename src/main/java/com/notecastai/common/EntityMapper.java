package com.notecastai.common;

import java.util.List;

public interface EntityMapper<E, D> {

     D toDto(E entity);

     List<D> toDto(List<E> entityList);

     E toEntity(D dto);

     List<E> toEntity(List<D> dtoList);

}
