package ru.javawebinar.topjava.util;

import org.springframework.lang.Nullable;

//подавать методу в его параметры будем LocalDateTime(value - дата создания проверяемой еды,
// start-end заданный промежуток для фильтрации )
public class Util {
    public static <T extends Comparable<? super T>> boolean isBetweenInclusive(T value, @Nullable T start, @Nullable T end) {
        return (start == null || value.compareTo(start) >= 0) && (end == null || value.compareTo(end) <= 0);
    }
}


/**
 * Рассмотрим выражение: <T extends Comparable<? super T>>
 *
 * //<T extends Comparable> - означает ограничение по верхней границе НЕвключительно -
 * можно подать только детёныша Comparable,
 * //но сам Comparable подать нельзя. (ДОСЛОВНО- Можно подать тип T который
 * имлементит(или ЭКСТЕНДИТ) Comparable)
 *
 * //(здесь Comparable только для примера. на месте Comparable может быть любой интерфейс или класс,
 * от которого екстендятся).
 *
 * //<? super T>  - означает ограничение по нижней границе включительно- можно подать класс T или любой
 * из родителей T.
 * (ДОСЛОВНО- можно подать тип T или тип ЛЮБОГО РОДИТЕЛЯ T)
 *
 * Общее выражение: <T extends Comparable<? super T>> перед методом означает- метод может заменить T на тип,
 * класс которого
 * имплементит(екстендит) Comparable. А Comparable в свою очередь должен быть параметризован или всё тем же
 * типом T или одним из родителей T.
 *
 * Слова "extends" и "super" могут быть скомбинированны в любых вариациях со словом "?"
 * (где "?"  ДОСЛОВНО-"ЛЮБЫХ").
 * Соответственно в таких логически-дословных вариациях и следует читать подобные выражения.
 */