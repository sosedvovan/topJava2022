package ru.javawebinar.topjava.repository.inmemory;

import org.springframework.stereotype.Repository;
import ru.javawebinar.topjava.model.AbstractBaseEntity;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class InMemoryBaseRepository<T extends AbstractBaseEntity> {

    private static AtomicInteger counter = new AtomicInteger(0);

    //мапа которая может хранить и юзеров и их еду
    private Map<Integer, T> map = new ConcurrentHashMap<>();

    public T save(T entry) {
        if (entry.isNew()) {//если новая(Id еще не назначен) тогда
            entry.setId(counter.incrementAndGet());//назначаем id
            map.put(entry.getId(), entry);//кладем в мапу-мемери-хранилище
            return entry;//возвращаем объект уже с id
        }
        //иначе(обновление старой - такой id уже есть в мапе) -
        //для уже имеющегося id вычисляем новое значение(его берем из аргументов метода)
        return map.computeIfPresent(entry.getId(), (id, oldT) -> {return entry;});
    }

    public boolean delete(int id) {
        return map.remove(id) != null;
    }

    public T get(int id) {
        return map.get(id);
    }

    Collection<T> getCollection() {
        return map.values();
    }
}