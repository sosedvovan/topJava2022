package ru.javawebinar.topjava.web;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.web.meal.MealRestController;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static ru.javawebinar.topjava.util.DateTimeUtil.parseLocalDate;
import static ru.javawebinar.topjava.util.DateTimeUtil.parseLocalTime;

public class MealServlet extends HttpServlet {

    private ConfigurableApplicationContext springContext;
    private MealRestController mealController;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        //эта сервлета должна знать о настройках спринга
        springContext = new ClassPathXmlApplicationContext("spring/spring-app.xml", "spring/spring-db.xml");
        //контроллер перенаправляет на сервис
        mealController = springContext.getBean(MealRestController.class);
    }

    @Override
    public void destroy() {
        //закроем контекст перед выходом из сервлеты
        springContext.close();
        super.destroy();
    }

    //сюда попадем из формы при создании новой еды или обновлении старой еды
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        //создаем еду из параметров запроса
        Meal meal = new Meal(
                LocalDateTime.parse(request.getParameter("dateTime")),
                request.getParameter("description"),
                Integer.parseInt(request.getParameter("calories")));

        //если в еде из формы нет id значит мы хотим сохранить новую еду
        if (StringUtils.isEmpty(request.getParameter("id"))) {
            mealController.create(meal);
        //иначе обновляем старую еду
        } else {
            mealController.update(meal, getId(request));
        }
        response.sendRedirect("meals");
    }

    //принимает гет запрос и определяет дальнейшее действие- или вызов формы или метода сервиса
    // напр: http://localhost:8080/topjava/meals?action=create
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        //если в гет запросе нет action=... тогда обрабатываем как "all"
        switch (action == null ? "all" : action) {
            //если в гет запросе action="delete"
            case "delete":
                int id = getId(request);
                mealController.delete(id);
                response.sendRedirect("meals");
                break;
            //если в гет запросе action="create" или "update"
            case "create":
            case "update":
                //если "create"
                final Meal meal = "create".equals(action) ?
                        //если "create" создаем новую еду
                        new Meal(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES), "", 1000) :
                        //если "update" берем старую еду из дб
                        mealController.get(getId(request));
                //кладем еду в атрибут
                request.setAttribute("meal", meal);
                //и вызываем форму (в форму пойдет и атрибут)
                request.getRequestDispatcher("/mealForm.jsp").forward(request, response);
                break;
            //если в гет запросе будет action="filter"
            //значит мы ввели в форме фильтра свои данные для фильтрации(две даты и 2-а времени)
            //http://localhost:8080/topjava/meals?action=filter&startDate=&endDate=&startTime=&endTime=
            case "filter":
                //из гет запроса из формы получаем параметры
                LocalDate startDate = parseLocalDate(request.getParameter("startDate"));
                LocalDate endDate = parseLocalDate(request.getParameter("endDate"));
                LocalTime startTime = parseLocalTime(request.getParameter("startTime"));
                LocalTime endTime = parseLocalTime(request.getParameter("endTime"));
                //в атрибут кладем лист: в контроллер(далее в сервис и репозиторий) отправляем две даты и 2-а времени
                request.setAttribute("meals", mealController.getBetween(startDate, startTime, endDate, endTime));
                request.getRequestDispatcher("/meals.jsp").forward(request, response);
                break;
            case "all":
            default:
                request.setAttribute("meals", mealController.getAll());
                request.getRequestDispatcher("/meals.jsp").forward(request, response);
                break;
        }
    }

    private int getId(HttpServletRequest request) {
        String paramId = Objects.requireNonNull(request.getParameter("id"));
        return Integer.parseInt(paramId);
    }
}
