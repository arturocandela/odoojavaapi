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

        final String url = "http://nuestratienda.sge.com:8069",
                db = "Preba2",
                username = "frisix@gmail.com",
                password = "FalodrVqems";


        final XmlRpcClient client = new XmlRpcClient();

        final XmlRpcClientConfigImpl start_config = new XmlRpcClientConfigImpl();

        start_config.setBasicUserName(username);
        start_config.setBasicPassword(password);


        start_config.setServerURL(new URL("https://demo.odoo.com/start"));

        final Map<String, Object> info = (Map<String, Object>)client.execute( start_config, "start", emptyList());

/*
        final String url = info.get("host"),
                db = info.get("database"),
                username = info.get("user"),
                password = info.get("password");*/


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
                "res.partner", "check_access_rights",
                asList("read"),
                new HashMap() {{ put("raise_exception", false); }}
        ));

        System.out.println(asList((Object[])models.execute("execute_kw", asList(
                db, uid, password,
                "res.partner", "search",
                asList(asList(
                        asList("is_company", "=", true)))
        ))));


    }

}
