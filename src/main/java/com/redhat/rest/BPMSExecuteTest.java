package com.redhat.rest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.drools.core.command.runtime.process.StartProcessCommand;
import org.jboss.resteasy.util.Base64;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;

import com.redhat.consulting.User;

public class BPMSExecuteTest {

	public static void main(String[] args) throws Exception {
		Response response = null;

		try {
			String jaxbRequestString = null;
			
			String username = "david";
			String password = "david123!";
			String b64enc = Base64.encodeBytes((username+":"+password).getBytes());
			String auth = "Basic "+b64enc;
			
			System.out.println("Auth: "+auth);

			User u = new User();
			u.setUsername(username);

			Set<Class<?>> extraJaxbClassList = new HashSet<>();
			extraJaxbClassList.add(u.getClass());

			JaxbSerializationProvider jaxbProvider = new JaxbSerializationProvider(
					extraJaxbClassList);

			Map<String, Object> vars = new HashMap<String, Object>();
			vars.put("appraiserFirst", "David");
			vars.put("appraiserLast", "vB");
			vars.put("user", u);
			StartProcessCommand cmd = new StartProcessCommand(
					"Workflows.TaskAssignment", vars);
			JaxbCommandsRequest req = new JaxbCommandsRequest(
					"com.redhat.consulting:Workflows:1.0", cmd);

			Client client = ClientBuilder.newClient();
			WebTarget target = client
					.target("http://localhost:8080/business-central/rest/runtime/com.redhat.consulting:Workflows:1.0/execute");

			jaxbRequestString = jaxbProvider.serialize(req);

			response = target.request().header("Authorization", auth)
					.post(Entity.entity(jaxbRequestString,
							MediaType.APPLICATION_XML));
			System.out.println("Status: " + response.getStatus());

		} finally {
			response.close();
		}

	}

}
