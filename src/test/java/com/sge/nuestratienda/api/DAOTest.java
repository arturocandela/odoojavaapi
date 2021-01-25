package com.sge.nuestratienda.api;

import com.sge.nuestratienda.client.model.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DAOTest {

    @Test
    void instance() {

        assertNotNull(DAO.Instance());

    }

    @Test
    void getVersion() throws DAOOperationException {

        Object version = DAO.Instance().getVersion();
        assertNotNull(version);

        System.out.println(version);

    }

    @Test
    void getAllTasks() throws DAOOperationException {
        assertNotNull(DAO.Instance().getAllTasks());
    }

    @Test
    void getTaskForId() throws DAOOperationException {
        Task task = DAO.Instance().getTaskForId(20);
        assertNotNull(task);
    }

    @Test
    void addTask() throws  DAOOperationException {

        Task task = new Task();
        task.setName("Task Name");
        task.setActive(true);
        task.setIs_done(false);

        boolean result = DAO.Instance().addTask(task);
        assertTrue(result);
        assertNotEquals(0,task.getId());

    }

    @Test
    void updateTask() throws DAOOperationException {

        Task task = new Task("Hola que tal?");

        DAO.Instance().addTask(task);

        int id = task.getId();
        String nuevoTexto = task.getName() + " (Hola guapi)";
        task.setName(nuevoTexto);
        boolean newActive = !task.isActive();
        task.setActive(newActive);
        boolean newIsDone = !task.isIs_done();
        task.setIs_done(newIsDone);

        DAO.Instance().updateTask(task);

        //Task taskUpdated = DAO.Instance().getTaskForId(task.getId());
        //assertEquals(nuevoTexto,taskUpdated.getName());
        //assertEquals(newActive,taskUpdated.isActive());
        //assertEquals(newIsDone,taskUpdated.isIs_done());

        DAO.Instance().deleteTask(task);

    }

    void deleteTask() throws DAOOperationException {

    }

    void CRUD() throws DAOOperationException {

    }

}