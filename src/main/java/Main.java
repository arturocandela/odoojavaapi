import com.sge.nuestratienda.client.config.PropertyValues;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Arrays.asList;

public class Main {

    public static void main(String[] args) throws Exception{

        final String url = PropertyValues.Instance().getOdooUrl(),
                db = PropertyValues.Instance().getDBName(),
                username = PropertyValues.Instance().getDBUser(),
                password = PropertyValues.Instance().getDBPassword();

        final XmlRpcClient client = new XmlRpcClient();

        final XmlRpcClientConfigImpl common_config = new XmlRpcClientConfigImpl();
        common_config.setServerURL(
                new URL(String.format("%s/xmlrpc/2/common", url)));

        Map<String,String> response = (Map<String,String>)client.execute(common_config, "version", emptyList());

        MapUtils.debugPrint(System.out,"Response",response);

        int uid = (int)client.execute(
                common_config, "authenticate", asList(
                        db, username, password, emptyMap()));

        System.out.println(uid);

        final XmlRpcClient models = new XmlRpcClient() {{
            setConfig(new XmlRpcClientConfigImpl() {{
                setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
            }});
        }};

        boolean hasAccessToModules = (boolean)models.execute("execute_kw", asList(
                db, uid, password,
                "todo.task", "check_access_rights",
                asList("read"),
                new HashMap() {{ put("raise_exception", false); }}
        ));

        System.out.println(asList((Object[])models.execute("execute_kw", asList(
                db, uid, password,
                "todo.task", "search",
                asList(asList(
                        asList("active", "=", true)))
        ))));

        Map<String, Map<String, Object>> tareas = (Map<String, Map<String, Object>>)models.execute("execute_kw", asList(
                db, uid, password,
                "todo.task", "fields_get",
                emptyList(),
                new HashMap() {{
                    put("attributes", asList("string", "help", "type"));
                }}
        ));

        MapUtils.debugPrint(System.out,"Tareas",tareas);



    }

}
