package ru.javawebinar.topjava.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        request.getRequestDispatcher("/users.jsp").forward(request, response);
        response.sendRedirect("users.jsp");// "/" перед users.jsp не ставь чтобы урл был
        //от контекста (http://localhost:8080/topjava/users.jsp)
        //а если поставить "/users.jsp" тобудет от рута (http://localhost:8080/users.jsp)
        //и не найдет страницу тк у нас контекст /topjava задан в run-> Edit Configuration
    }
}
