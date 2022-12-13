package ru.javawebinar.topjava.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.repository.MealRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class JdbcMealRepository implements MealRepository {

    //в классе модели Meal должны быть геттеры и сеттеры и пустой конструктор(и в базовых)
    //тк классы, кот в полях, их используют

    //получаем RowMapper для авто маппинга РезультСета на нашу сущность Meal.class
    private static final RowMapper<Meal> ROW_MAPPER = BeanPropertyRowMapper.newInstance(Meal.class);

    //попробывал сделать свой роу маппер, но с ним много тестов не проходят
//    private static final RowMapper<Meal> ROW_MAPPER =
//            new RowMapper<Meal>() {
//                @Override
//                public Meal mapRow(ResultSet rs, int rowNum) throws SQLException {
//                    return new Meal(
//                            rs.getInt("id"),
//                            rs.getTimestamp("data_time").toLocalDateTime(),
//                            rs.getString("description"),
//                            rs.getInt("calories"));
//                }
//            };

    //JdbcTemplate для выполнения sql запросов, создан в spring-db.xml
    private final JdbcTemplate jdbcTemplate;

    //NamedParameterJdbcTemplate - при составлении sql запроса берет именованные данные из мапы, создан в spring-db.xml
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    //JdbcTemplate для выполнения sql запросов, создаем в конструкторе.
    //указывая .withTableName("meals") и .usingGeneratedKeyColumns("id")
    //sql запрос составляется сам-автоматом из мапы, возвращает назначенный в дб "id"
    private final SimpleJdbcInsert insertMeal;

    @Autowired
    public JdbcMealRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.insertMeal = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("meals")
                .usingGeneratedKeyColumns("id");

        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Meal save(Meal meal, int userId) {
        //создаем мапу для insertMeal
        MapSqlParameterSource map = new MapSqlParameterSource()
                .addValue("id", meal.getId())
                .addValue("description", meal.getDescription())
                .addValue("calories", meal.getCalories())
                .addValue("date_time", meal.getDateTime())
                .addValue("user_id", userId);

        //если в meal (пришедшей в параметрах) id == null значит еда новая и ее надо сохранить
        if (meal.isNew()) {
            //insertMeal сохранит новую еду, автоматом создаст sql запрос с пом map и вернет назначенный в дб id
            Number newId = insertMeal.executeAndReturnKey(map);
            //в meal, которая из параметров добавим id
            meal.setId(newId.intValue());

        //а если уже существующую еду надо только обновить:
        //так понимаю что параметры возьмет из мапы,
        // кот создали выше в методе(подаем 2-м параметром)
        //и при удачном выполнении запроса вернет 0,  а из метода тогда
            // return null (все хорошо - вернется null)
        //а при неудачном выполнении запроса блок else не сработает вообще и из метода тогда
        //вернется та еда, которая пришла для обновления
        } else {
            if (namedParameterJdbcTemplate.update("" +
                            "UPDATE meals " +
                            "   SET description=:description, calories=:calories, date_time=:date_time " +
                            " WHERE id=:id AND user_id=:user_id"
                    , map) == 0) {
                return null;
            }
        }
        return meal;
    }

    @Override
    public boolean delete(int id, int userId) {
        return jdbcTemplate.update("DELETE FROM meals WHERE id=? AND user_id=?", id, userId) != 0;
    }

    @Override
    public Meal get(int id, int userId) {
        List<Meal> meals = jdbcTemplate.query(
                "SELECT * FROM meals WHERE id = ? AND user_id = ?", ROW_MAPPER, id, userId);
        return DataAccessUtils.singleResult(meals);
    }

    @Override
    public List<Meal> getAll(int userId) {
        return jdbcTemplate.query(
                "SELECT * FROM meals WHERE user_id=? ORDER BY date_time DESC", ROW_MAPPER, userId);
    }

    //для фильтрации получаем 2-е даты и userId
    @Override
    public List<Meal> getBetween(LocalDateTime startDate, LocalDateTime endDate, int userId) {
        return jdbcTemplate.query(
                "SELECT * FROM meals WHERE user_id=?  AND date_time BETWEEN  ? AND ? ORDER BY date_time DESC",
                ROW_MAPPER, userId, startDate, endDate);
    }
}
