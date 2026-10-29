
package com.hotel.common.repository;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T> {

    T create(T entity);

    Optional<T> findById(String id);

    List<T> findAll();

    T update(String id, T updatedEntity);

    boolean deleteById(String id);
}
