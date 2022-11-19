package ru.javawebinar.topjava.repository.inmemory;

import org.springframework.stereotype.Repository;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.repository.MealRepository;
import ru.javawebinar.topjava.util.MealsUtil;
import ru.javawebinar.topjava.util.Util;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ru.javawebinar.topjava.repository.inmemory.InMemoryUserRepository.ADMIN_ID;
import static ru.javawebinar.topjava.repository.inmemory.InMemoryUserRepository.USER_ID;

@Repository
public class InMemoryMealRepository implements MealRepository {

    // Map  userId -> mealRepository
    //то для каждого юзера или админа будет свой репозиторий для еды(объект InMemoryBaseRepository<Meal>
    // который может хранить <Meal> в своей внутренней мапе)
    private Map<Integer, InMemoryBaseRepository<Meal>> usersMealsMap = new ConcurrentHashMap<>();

    {
        //еда юзера
        MealsUtil.MEALS.forEach(meal -> save(meal, USER_ID));

        //еда админа
        save(new Meal(LocalDateTime.of(2015, Month.JUNE, 1, 14, 0), "Админ ланч", 510), ADMIN_ID);
        save(new Meal(LocalDateTime.of(2015, Month.JUNE, 1, 21, 0), "Админ ужин", 1500), ADMIN_ID);
    }


    //у нас захардкоденно 2-а юзера(админ и юзер)
    //из сервиса в параметры здесь получаем еду (без Id) и Id юзера, или админа
    @Override
    public Meal save(Meal meal, int userId) {
        //хитрым образом получаем объект MemoryBase<Meal> класса(способного хранить Meal)
        //из возврата метода computeIfAbsent
        //https://javarush.ru/groups/posts/524-khvatit-pisatjh-ciklih-top-10-luchshikh-metodov-dlja-rabotih-s-kollekcijami-iz-java8
        InMemoryBaseRepository<Meal> meals = usersMealsMap.computeIfAbsent(userId, uid -> new InMemoryBaseRepository<Meal>());
        //и на этом полученном объекте(параметризованного едой) вызываем метод объекта для сохр еды
        //которые вернет Meal уже с id
        return meals.save(meal);
    }

    @Override
    public boolean delete(int id, int userId) {
        InMemoryBaseRepository<Meal> meals = usersMealsMap.get(userId);
        return meals != null && meals.delete(id);
    }

    @Override
    public Meal get(int id, int userId) {
        //сначала получаем хранилище еды-meals юзера, кот пришел в параметры-userId
        InMemoryBaseRepository<Meal> meals = usersMealsMap.get(userId);
        //и из этого хранилища достаем еду по id еды
        return meals == null ? null : meals.get(id);
    }

    @Override
    public List<Meal> getAll(int userId) {
        return getAllFiltered(userId, meal -> true);
    }

    @Override
    public List<Meal> getBetween(LocalDateTime startDateTime, LocalDateTime endDateTime, int userId) {
        return getAllFiltered(userId, meal -> Util.isBetweenInclusive(meal.getDateTime(), startDateTime, endDateTime));
    }

    private List<Meal> getAllFiltered(int userId, Predicate<Meal> filter) {
        InMemoryBaseRepository<Meal> meals = usersMealsMap.get(userId);
        return meals == null ? Collections.emptyList() :
                meals.getCollection().stream()
                        .filter(filter)
                        .sorted(Comparator.comparing(Meal::getDateTime).reversed())
                        .collect(Collectors.toList());
    }
}