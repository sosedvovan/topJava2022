package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.MealTo;

import java.time.LocalDate;
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

        List<MealTo> mealsTo1 = getFilteredWithExcess(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo1.forEach(System.out::println);

//        List<MealTo> mealsTo2 = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
//        mealsTo2.forEach(System.out::println);

//        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    /**
     * про методы Мапы merge, getOrDefault...
     https://javarush.ru/groups/posts/524-khvatit-pisatjh-ciklih-top-10-luchshikh-metodov-dlja-rabotih-s-kollekcijami-iz-java8
     */
    //решение чз цикл
    public static List<MealTo> getFilteredWithExcessByCycle(List<Meal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

        //подготавливаем мапу
        final Map<LocalDate, Integer> caloriesSumByDate = new HashMap<>();
        //в подготовленную мапу кладем в ключ(сортировка по дате - только уникальные даты будут) - meal.getDate()
        //в значение кладем сумму калорий в одной дате - meal.getCalories(), Integer::sum
        //для такой группировки используем метод мапы merge()
        meals.forEach(meal -> caloriesSumByDate.merge(meal.getDate(), meal.getCalories(), Integer::sum));

//        for(Meal meal:meals){
//            LocalDate mealDate = meal.getDateTime().toLocalDate();
//            caloriesSumByDate.put(mealDate,
//                    //getOrDefault() Возвращает значение, соответствующее ключу key (mealDate).
//                    //Если такой ключ не существует — возвращает значение по умолчанию - у нас это 0.
//                    //то для завтрака вернет 0 + meal.getCalories() завтрака
//                    //для обеда вернет то что уже для завтрака + meal.getCalories() - обеда и тд
//                    caloriesSumByDate.getOrDefault(mealDate, 0) + meal.getCalories());
//        }



        //подготавливаем лист
        final List<MealTo> mealsWithExcess = new ArrayList<>();
        //в подготовленный лист попадают только отфильтрованные элементы(фильтруем с пом метода isBetween())
        //и на лету преобразованные из Meal в MealTo (с пом приват метода createWithExcess()) элементы
        meals.forEach(meal -> {
            if (TimeUtil.isBetween(meal.getTime(), startTime, endTime)) {
                mealsWithExcess.add(createWithExcess(meal, caloriesSumByDate.get(meal.getDate()) > caloriesPerDay));
            }
        });
        return mealsWithExcess;
    }

    private static MealTo createWithExcess(Meal meal, boolean excess) {
        return new MealTo(meal.getDateTime(), meal.getDescription(), meal.getCalories(), excess);
    }

    //решение чз стримы
    public static List<MealTo> getFilteredWithExcess(List<Meal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        //1-й шаг:
        //делаем группировку листа с едой meals по дате
        //и получаем мапу: ключ-дата, в значении- сумма калорий в этой дате
        Map<LocalDate, Integer> caloriesSumByDate = meals.stream()
                .collect(
                        //в 1-м параметре указываем что группируем по дате (дата будет ключом)
                        //во втором параметре указываем что в значение надо положить сумму калорий в этой дате
                        Collectors.groupingBy(Meal::getDate, Collectors.summingInt(Meal::getCalories))
//                      Collectors.toMap(Meal::getDate, Meal::getCalories, Integer::sum)
                );

        //2-й шаг:
        return meals.stream()
                //фильтруем тот же лист с едой meals по указанному(в параметрах метода) промежутку времени
                .filter(meal -> TimeUtil.isBetween(meal.getTime(), startTime, endTime))
                //элементы прошедших фильтр преобразуем в объекты MealTo
                .map(meal ->
                        new MealTo(meal.getDateTime(), meal.getDescription(), meal.getCalories(),
                                //для boolean поля excess объекта MealTo вычисляем true или false
                                //те в мапе из первого шага: берем значение по ключу(дате) и сравниваем его
                                //с caloriesPerDay(из параметров метода)
                                caloriesSumByDate.get(meal.getDate()) > caloriesPerDay))
                .collect(Collectors.toList());
    }



//мой вариант решения
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
            if(TimeUtil.isBetween(LocalTime.of(resultsUserMealWithExcess.get(i).getDateTime().getHour(),
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
