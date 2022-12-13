package ru.javawebinar.topjava.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import ru.javawebinar.topjava.UserTestData;
import ru.javawebinar.topjava.model.Role;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.util.exception.NotFoundException;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static ru.javawebinar.topjava.UserTestData.*;

//настройки для спринг-тест
@ContextConfiguration({
        "classpath:spring/spring-app.xml",
        "classpath:spring/spring-db.xml"
})
@RunWith(SpringRunner.class)//SpringRunner это альяс к SpringJUnit4ClassRunner
//тесты выполняются в произв порядке, след тесты не должны зависить друг от друга
//@Sql - перед каждым тестом будет восстанавливать дб
@Sql(scripts = "classpath:db/populateDB.sql", config = @SqlConfig(encoding = "UTF-8"))
public class UserServiceTest {

    @Autowired
    private UserService service;

    //чтобы сравнивать ожидаемого юзера с полученным мы в AbstractBaseEntity добавили
    //эквалс и хешкод на основе Id (на основе даты и сета ролей нельзя вообще)
    //на первой яве сравнивали по туСтринг

    //но мы здесь будем сравнивать по всем полям, исключая 2-а поля: "registered", "roles"
    //с пом isEqualToIgnoringGivenFields из org.assertj (см UserTestData)

    @Test
    public void create() throws Exception {
        //создали нового юзера
        User newUser = new User(null, "New", "new@gmail.com", "newPass", 1555, false, new Date(), Collections.singleton(Role.ROLE_USER));
        //нового юзера сохранили в дб и получили сохраненного юзера с заданным Id
        User created = service.create(newUser);
        //созданному юзеру задали Id от сохраненного
        newUser.setId(created.getId());
        //проверили: getAll() вернет юзеров: ADMIN, newUser, USER
        assertMatch(service.getAll(), ADMIN, newUser, USER);
    }

    //метод должен выбросить ошибку тк на сохранение в дб отправили юзера с дублирующимся emal-ом
    @Test(expected = DataAccessException.class)
    public void duplicateMailCreate() throws Exception {
        service.create(new User(null, "Duplicate", "user@yandex.ru", "newPass", Role.ROLE_USER));
    }

    @Test
    public void delete() throws Exception {
        //удаляем юзера
        service.delete(USER_ID);
        //проверили: getAll() вернет юзеров: ADMIN
        assertMatch(service.getAll(), ADMIN);
    }

    //пытаемся удалить с несуществующим id, должны получить ексепшен
    @Test(expected = NotFoundException.class)
    public void deletedNotFound() throws Exception {
        service.delete(1);
    }

    @Test
    public void get() throws Exception {
        //получим юзера по USER_ID
        User user = service.get(UserTestData.USER_ID);
        //сравним полученного юзера с имеющимся USER
        assertMatch(user, UserTestData.USER);
    }

    //пытаемся get с несуществующим id, должны получить ексепшен
    @Test(expected = NotFoundException.class)
    public void getNotFound() throws Exception {
        service.get(1);
    }

    @Test
    public void getByEmail() throws Exception {
        //получим юзера по Email
        User user = service.getByEmail("user@yandex.ru");
        //сравним полученного юзера с имеющимся USER
        assertMatch(user, USER);
    }

    @Test
    public void update() throws Exception {
        //получим юзера из имеющегося юзера с пом конструктора копирования
        User updated = new User(USER);
        //у полученного юзера изменим имя
        updated.setName("UpdatedName");
        //и изменим калории
        updated.setCaloriesPerDay(330);
        //сохраним изменения
        service.update(updated);
        //сравним обновление в дб с User updated
        assertMatch(service.get(USER_ID), updated);
    }

    @Test
    public void getAll() throws Exception {
        //получим всех юзеров
        List<User> all = service.getAll();
        //лист сравним с реально имеющимися в дб юзерами
        assertMatch(all, ADMIN, USER);
    }
}