package ru.javawebinar.topjava.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.repository.MealRepository;
import ru.javawebinar.topjava.util.DateTimeUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static ru.javawebinar.topjava.util.ValidationUtil.checkNotFoundWithId;

@Service
public class MealService {


//    @Qualifier(value = "JdbcMealRepository")
    private final MealRepository repository;

//    @Autowired
    public MealService(MealRepository repository) {
        this.repository = repository;
    }

    public Meal get(int id, int userId) {
        return checkNotFoundWithId(repository.get(id, userId), id);
    }

    public void delete(int id, int userId) {
        checkNotFoundWithId(repository.delete(id, userId), id);
    }

    //сервис получает 2-е даты и userId (по этим данным надо отфильтровать) и отправляет их в репозиторий
    //тк даты возможно @Nullable то с пом метода createDateTime создадутся дефолтные даты
    // (когда фильтруем по дефолтным датам - фильтруем по всем возможным временам)
    public List<Meal> getBetweenDates(@Nullable LocalDate startDate, @Nullable LocalDate endDate, int userId) {
        return repository.getBetween(
                DateTimeUtil.createDateTime(startDate, LocalDate.MIN, LocalTime.MIN),
                DateTimeUtil.createDateTime(endDate, LocalDate.MAX, LocalTime.MAX),
                userId);
    }

    public List<Meal> getAll(int userId) {
        return repository.getAll(userId);
    }

    public void update(Meal meal, int userId) {
        checkNotFoundWithId(repository.save(meal, userId), meal.getId());
    }

    public Meal create(Meal meal, int userId) {
        return repository.save(meal, userId);
    }
}