package com.notecastai.common;

import java.util.List;

public interface EntityMapper<E, D> {

     D toDto(E entity);

     List<D> toDto(List<E> entityList);

}
