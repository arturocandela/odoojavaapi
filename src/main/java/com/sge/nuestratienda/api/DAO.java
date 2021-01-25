package com.sge.nuestratienda.api;

import com.sge.nuestratienda.client.config.PropertyValues;
import com.sge.nuestratienda.client.model.Task;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

/**
 * Singleton pattern
 */
public class DAO {

    private static final Object instanceLock = new Object();
    private static DAO Instance = null;

    private String url = null;
    private String db = null;
    private String username = null;
    private String password = null;

    private Integer uid = null;

    XmlRpcClient client;
    XmlRpcClient models;
    XmlRpcClientConfigImpl common_config;

    private final String MODEL_ODOO_TODO_TASK = "todo.task";

    /**
     * Stream Por si se quieren recuperar los errores de la clase
     */
    PrintStream errorStream;

    /**
     *
     * @return The Default class instance
     */
    public static DAO Instance() {

        synchronized (instanceLock) {
            if (Instance == null){
                Instance = new DAO();
            }
        }
        return Instance;
    }

    private DAO () {

        url = PropertyValues.Instance().getOdooUrl();
        db = PropertyValues.Instance().getDBName();
        username = PropertyValues.Instance().getDBUser();
        password = PropertyValues.Instance().getDBPassword();

        setUp();

    }

    private void setUp() {

        client = new XmlRpcClient();
        common_config = new XmlRpcClientConfigImpl();

        try {
            common_config.setServerURL(
                    new URL(String.format("%s/xmlrpc/2/common", url)));
        } catch (MalformedURLException e){
            e.printStackTrace(System.err);
        }

        try {
            // In one line... (the same of the previous block)
            models = new XmlRpcClient() {{
                setConfig(new XmlRpcClientConfigImpl() {{
                    setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
                }});
            }};
        } catch (MalformedURLException e){
            e.printStackTrace(System.err);
        }

    }

    private void authenticate() throws XmlRpcException,DAOOperationException {

        Object obj_response = null;

        obj_response = client.execute(common_config, "authenticate", asList(
                            db, username, password, emptyMap()));

        try {
            uid = (Integer) obj_response;
        } catch (Exception e){
            if (obj_response instanceof Boolean){
                uid = null;
                throw new DAOOperationException("There was a problem during the auth (maybe user or password are wrong)");
            } else {
                uid = null;
                throw new DAOOperationException("There was an unknown error During the process");
            }
        }

    }

    public Map<String,?> getVersion() throws DAOOperationException {

        Object obj_response = null;
        Map<String,Object> response = null;

        try {
            obj_response = client.execute(common_config, "version", emptyList());
        } catch (XmlRpcException e){
            throw new DAOOperationException(e.getLocalizedMessage());
        }

        try {
            response = (Map<String, Object>) obj_response;
        } catch (Exception e){
            throw new DAOOperationException("Error casting response to map");
        }

        return response;

    }

    private Task[] getTasksByQuery(List<?> query) throws DAOOperationException {

        List ids;
        Object obj_response = null;

        //Check IF we are authenticated
        if ( null == uid ) {

            try {
                authenticate();
            } catch (XmlRpcException e) {
                throw new DAOOperationException(e.getLocalizedMessage());
            }

        }

        //Get the IDS
        try {
            ids = asList((Object[])models.execute("execute_kw", asList(
                    db, uid, password,
                    MODEL_ODOO_TODO_TASK, "search",
                    asList(asList(
                            query))
            )));
        } catch (XmlRpcException e) {
            throw new DAOOperationException(e.getLocalizedMessage());
        }

        if (ids.size() >= 1){

            try {
                obj_response = asList((Object[])models.execute("execute_kw", asList(
                        db, uid, password,
                        MODEL_ODOO_TODO_TASK, "read",
                        asList(ids),
                        new HashMap() {{
                            put("fields", asList("name", "is_done", "active"));
                        }}
                )));
            } catch (XmlRpcException e) {
                throw new DAOOperationException(e.getLocalizedMessage());
            }

        }

        Task[] tasks = null;

        if (obj_response != null) {

            List<HashMap<String,Object>> mapaTareas = (List<HashMap<String, Object>>) obj_response;
            tasks = new Task[mapaTareas.size()];

            for (int i = 0; i < mapaTareas.size(); i++){

                HashMap<String,?> mapaTareaActual = mapaTareas.get(i);

                tasks[i] = new Task();
                tasks[i].setId((Integer)mapaTareaActual.get("id"));
                tasks[i].setName((String)mapaTareaActual.get("name"));
                tasks[i].setActive((Boolean)mapaTareaActual.get("active"));
                tasks[i].setIs_done((Boolean)mapaTareaActual.get("is_done"));

            }
        }

        return tasks;
    }

    public Task[] getAllTasks() throws DAOOperationException{
        return getTasksByQuery(asList("id",">",0));
    }

    public Task getTaskForId(int id) throws DAOOperationException{
        Task[] array = getTasksByQuery(asList("id","=",id));
        if (array != null && array.length == 1) {
            return array[0];
        } else {
            return null;
        }
    }


    /**
     * Creates a new Task, the idd will be added to the task passed as reference
     *
     * @param task Task to create
     * @return true if the task was successfully created
     */
    public boolean addTask(Task task) throws DAOOperationException{

        if ( null == uid ) {

            try {
                authenticate();
            } catch (XmlRpcException e) {
                throw new DAOOperationException(e.getLocalizedMessage());
            }

        }

        Integer id = null;

        try {
            id = (Integer)models.execute("execute_kw", asList(
                    db, uid, password,
                    MODEL_ODOO_TODO_TASK, "create",
                    asList(new HashMap() {{ put("name", task.getName());
                                            put("active",task.isActive());
                                            put("is_done",task.isIs_done());
                    }})
            ));
        } catch (XmlRpcException e) {
            throw new DAOOperationException(e.getLocalizedMessage());
        }

        if ( id != null ){
            task.setId((int)id);
            return true;
        }

        return false;
    }

    /**
     * Updates a Task by the id
     *
     * @param task Task to update
     * @return true if the task was completed successfully
     * @throws DAOOperationException If something goes wrong
     */
    public boolean updateTask(Task task) throws DAOOperationException {

        Object res_object = null;

        if ( null == uid ) {

            try {
                authenticate();
            } catch (XmlRpcException e) {
                throw new DAOOperationException(e.getLocalizedMessage());
            }

        }

        try {

            models.execute("execute_kw", asList(
                    db, uid, password,
                    MODEL_ODOO_TODO_TASK, "write",
                    asList(
                            asList(task.getId()),
                            new HashMap() {{ put("name", task.getName());
                                            put("active",task.isActive());
                                            put("is_done",task.isIs_done());
                            }}
                    )
            ));

        } catch (XmlRpcException e) {
            throw new DAOOperationException(e.getLocalizedMessage());
        }

        return true;
    }

    public boolean deleteTask(Task task) throws DAOOperationException {

        Object res_object = null;

        if ( null == uid ) {

            try {
                authenticate();
            } catch (XmlRpcException e) {
                throw new DAOOperationException(e.getLocalizedMessage());
            }

        }

        try {
            res_object = models.execute("execute_kw", asList(
                    db, uid, password,
                    MODEL_ODOO_TODO_TASK, "unlink",
                    asList(asList(task.getId()))));

        } catch (XmlRpcException e) {

            throw new DAOOperationException(e.getLocalizedMessage());

        }

        return true;

    }

}
