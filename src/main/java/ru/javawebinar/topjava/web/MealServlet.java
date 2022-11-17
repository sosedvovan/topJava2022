package ru.javawebinar.topjava.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.repository.InMemoryMealRepository;
import ru.javawebinar.topjava.repository.MealRepository;
import ru.javawebinar.topjava.util.MealsUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class MealServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(MealServlet.class);

    private MealRepository repository;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        repository = new InMemoryMealRepository();
    }

    //принимаем запрос из формы создания/обновления еды
    //создает/обновляет новый объект Meal, сохраняет с пом репозитория
    //и редирект на страницу со всем, уже обновленным, списком еды
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String id = request.getParameter("id");

        Meal meal = new Meal(id.isEmpty() ? null : Integer.valueOf(id),
                LocalDateTime.parse(request.getParameter("dateTime")),
                request.getParameter("description"),
                Integer.parseInt(request.getParameter("calories")));

        log.info(meal.isNew() ? "Create {}" : "Update {}", meal);
        repository.save(meal);
        response.sendRedirect("meals");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //В String action берем параметр action из  request-а (action будет null
        // или конкретный )
        String action = request.getParameter("action");

        switch (action == null ? "all" : action) {
            //если delete
            case "delete":
                //получаем id из request
                int id = getId(request);
                log.info("Delete {}", id);
                //удаляем по id
                repository.delete(id);
                response.sendRedirect("meals");
                break;
            // если   create или   update
            case "create":
            case "update":
                //если create
                final Meal meal = "create".equals(action) ?
                        //создаем новую еду (усекаем секунды)
                        new Meal(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES), "", 1000) :
                        //если update - получаем из репозитория уже имеющуюся еду
                        repository.get(getId(request));
                //созданную еду положим в атрибут для первоначального отображения в форме
                request.setAttribute("meal", meal);
                //форвардим на форму
                request.getRequestDispatcher("/mealForm.jsp").forward(request, response);
                break;
            //если all и если по default
            case "all":
            default:
                log.info("getAll");
                //в атрибут под ключем "meals" кладем коллекцию List<MealTo>
                //которую вернет метод getTos (объекты MealTo с правильным полем excess)
                request.setAttribute("meals",
                        MealsUtil.getTos(repository.getAll(), MealsUtil.DEFAULT_CALORIES_PER_DAY));
                //форвардим на список всей еды
                request.getRequestDispatcher("/meals.jsp").forward(request, response);
                break;
        }
    }

    //возвращает Integer id из параметров request-а
    private int getId(HttpServletRequest request) {
        String paramId = Objects.requireNonNull(request.getParameter("id"));
        return Integer.parseInt(paramId);
    }
}
