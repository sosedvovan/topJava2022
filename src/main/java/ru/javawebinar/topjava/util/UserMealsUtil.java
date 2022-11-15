package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.MealTo;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<Meal> meals = Arrays.asList(
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 510),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
//                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new Meal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        List<MealTo> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);

//        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<MealTo> filteredByCycles(List<Meal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        // TODO return filtered list with excess. Implement by cycles

        //сгруппировали UserMeal по дате и получили мапу
        Map<Integer, List<Meal>> collect = meals.stream()
                .collect(Collectors.groupingBy(m -> m.getDateTime().getDayOfMonth()));
        System.out.println("сгруппированная по дате мапа объектов UserMeal");
        System.out.println(collect);
        //на этой мапе получаем итератор
        Iterator<Map.Entry<Integer, List<Meal>>> iterator = collect.entrySet().iterator();
        //подготовим лист для всех обектов UserMealWithExcess с правильным полем excess
        List<MealTo> resultsUserMealWithExcess = new ArrayList<>();
        //итерируемся по каждой ентри и если ее ключ <= или >caloriesPerDay
        //берем значение ентри(List<UserMeal>) делаем UserMealWithExcess из элементов в List<UserMeal>
        //налету задавая значение поля excess(false или true) и КЛАДЕМ в resultsUserMealWithExcess
        while (iterator.hasNext()){
            //получаем ентри пару
            Map.Entry<Integer, List<Meal>> next = iterator.next();
            //из ентри пары берем значение - это лист с UserMeal из одной даты
            //и надо в этой дате подчитать калории
            List<Meal> value = next.getValue();
            //заведем счетчик калорий за день sumPerDay
            int sumPerDay = 0;
            //идем пол листу одного дня и считаем дневные калории
            for (int i = 0; i < value.size(); i++){
                sumPerDay += value.get(i).getCalories();
            }
            //сравниваем подчитанные калории с заданными, если меньше - тогда переедание = false
            if(sumPerDay <= caloriesPerDay){

                for(int i = 0; i < value.size(); i++){
                    resultsUserMealWithExcess.add(new MealTo(value.get(i).getDateTime(),
                            value.get(i).getDescription(), value.get(i).getCalories(), false));
                }
            }else {
                for(int i = 0; i < value.size(); i++){
                    resultsUserMealWithExcess.add(new MealTo(value.get(i).getDateTime(),
                            value.get(i).getDescription(), value.get(i).getCalories(), true));
                }
            }
            sumPerDay = 0;
        }

        //получили лист resultsUserMealWithExcess со всеми имеющимися UserMealWithExcess с
        // правильным полем excess. Теперь надо отфильтровать по заданному промежутку времени
        System.out.println("все имеющиеся UserMealWithExcess с правильным полем excess");
        System.out.println(resultsUserMealWithExcess);

        //подготовим лист для отфильтрованных по заданному промежутку UserMealWithExcess
        List<MealTo> resultsUserMealWithExcessFiltered = new ArrayList<>();

        //с помощью утильного метода фильтруем элементы листа resultsUserMealWithExcess
        for(int i = 0; i < resultsUserMealWithExcess.size(); i++){
            if(TimeUtil.isBetweenInclusive(LocalTime.of(resultsUserMealWithExcess.get(i).getDateTime().getHour(),
                    resultsUserMealWithExcess.get(i).getDateTime().getMinute()), startTime, endTime)) {

                resultsUserMealWithExcessFiltered.add(resultsUserMealWithExcess.get(i));
            }
        }

        //возвращаем лист с отфильтрованными результатами
        return resultsUserMealWithExcessFiltered;
    }






    public static List<MealTo> filteredByStreams(List<Meal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        // TODO Implement by streams
        return null;
    }
}
