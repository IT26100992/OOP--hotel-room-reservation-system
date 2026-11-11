
package com.hotel.common.repository;

import com.hotel.common.JsonFileStorage;
import com.hotel.common.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class JsonCrudRepository<T>
        implements CrudRepository<T> {

    private final JsonFileStorage<List<T>> storage;
    private final Function<T, String> idGetter;

    protected JsonCrudRepository(
            JsonFileStorage<List<T>> storage,
            Function<T, String> idGetter) {
        this.storage = storage;
        this.idGetter = idGetter;
    }

    // Each model supplies its own basic validation rules.
    protected abstract void validate(T entity);

    private List<T> readAll() {
        List<T> records = storage.read();
        return records == null
                ? new ArrayList<>()
                : new ArrayList<>(records);
    }

    @Override
    public synchronized T create(T entity) {
        validate(entity);

        String id = idGetter.apply(entity);

        if (id == null || id.isBlank()) {
            throw new ValidationException(
                    "Record ID cannot be empty."
            );
        }

        List<T> records = readAll();

        boolean duplicate = records.stream()
                .anyMatch(record -> id.equals(idGetter.apply(record)));

        if (duplicate) {
            throw new ValidationException(
                    "A record with ID " + id + " already exists."
            );
        }

        records.add(entity);
        storage.write(records);

        return entity;
    }

    @Override
    public synchronized Optional<T> findById(String id) {
        if (id == null || id.isBlank()) {
            throw new ValidationException(
                    "Record ID cannot be empty."
            );
        }

        return readAll().stream()
                .filter(record -> id.equals(idGetter.apply(record)))
                .findFirst();
    }

    @Override
    public synchronized List<T> findAll() {
        return readAll();
    }

    @Override
    public synchronized T update(String id, T updatedEntity) {
        if (id == null || id.isBlank()) {
            throw new ValidationException(
                    "Record ID cannot be empty."
            );
        }

        validate(updatedEntity);

        if (!id.equals(idGetter.apply(updatedEntity))) {
            throw new ValidationException(
                    "The URL ID must match the updated record ID."
            );
        }

        List<T> records = readAll();

        for (int i = 0; i < records.size(); i++) {
            if (id.equals(idGetter.apply(records.get(i)))) {
                records.set(i, updatedEntity);
                storage.write(records);
                return updatedEntity;
            }
        }

        throw new ValidationException(
                "Cannot update: record " + id + " does not exist."
        );
    }

    @Override
    public synchronized boolean deleteById(String id) {
        if (id == null || id.isBlank()) {
            throw new ValidationException(
                    "Record ID cannot be empty."
            );
        }

        List<T> records = readAll();

        boolean removed = records.removeIf(
                record -> id.equals(idGetter.apply(record))
        );

        if (removed) {
            storage.write(records);
        }

        return removed;
    }
}