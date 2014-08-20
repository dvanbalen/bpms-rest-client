package com.redhat.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.drools.core.command.runtime.process.StartProcessCommand;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.jboss.resteasy.plugins.providers.jaxb.JaxbMap.Entry;
import org.jboss.resteasy.util.Base64;
import org.jbpm.services.task.commands.DelegateTaskCommand;
import org.kie.api.command.Command;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BpmsRestCommandHelper {

	private static Logger LOG = LoggerFactory
			.getLogger(BpmsRestCommandHelper.class);

	private Set<Class<?>> extraJaxbClassList = new HashSet<>();
	private List<Command> cmds = new ArrayList<>();

	private String runtime_uri = "http://localhost:8080/business-central/rest/runtime/";

	public void createStartProcessCommand(String workflowId,
			Map<String, Object> params) {

		// Get list of parameter classes to add to JAXB context
		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, Object> e : params.entrySet()) {
				extraJaxbClassList.add(e.getValue().getClass());
			}
		}

		StartProcessCommand cmd = new StartProcessCommand(workflowId, params);

		cmds.add(cmd);

	}

	public void createDelegateTaskCommand(long taskId, String userId,
			String targetUserId) {

		DelegateTaskCommand cmd = new DelegateTaskCommand(taskId, userId,
				targetUserId);

		cmds.add(cmd);
	}

	public void sendBpmsCommands(String processId) throws Exception {
		Response response = null;

		String uri = runtime_uri + processId + "/execute";
		String username = "mary";
		String password = "mary123!";
		String b64enc = Base64.encodeBytes((username + ":" + password)
				.getBytes());
		String auth = "Basic " + b64enc;
		String jaxbRequestString = null;

		try {
			System.out.println("Sending " + cmds.size() + " commands to BPMS.");
			System.out.println("Auth: " + auth);

			JaxbSerializationProvider jaxbProvider = new JaxbSerializationProvider(
					extraJaxbClassList);

			JaxbCommandsRequest req = new JaxbCommandsRequest(processId, cmds);

			jaxbRequestString = jaxbProvider.serialize(req);

			Client client = ClientBuilder.newClient();
			WebTarget target = client.target(uri);

			response = target
					.request()
					.header("Authorization", auth)
					.post(Entity.entity(jaxbRequestString,
							MediaType.APPLICATION_XML));

			this.processJaxbCommandResponse(response);
		} finally {
			if (cmds != null) {
				cmds.clear();
			}
			if(extraJaxbClassList != null) {
				extraJaxbClassList.clear();
			}
			try {
				if (response != null) {
					response.close();
				}
			} catch (Throwable t) {
				// ignore
			}
		}

	}

	public void processJaxbCommandResponse(Response response) throws Exception {
		JaxbCommandsResponse commandResponse = null;
		JaxbCommandResponse<?> responseObject = null;

		System.out.println("Status: " + response.getStatus() + " class: "
				+ response.getClass().getName());

		commandResponse = response.readEntity(JaxbCommandsResponse.class);
		if (commandResponse == null) {
			System.out.println("command response is null");
		}
		List<JaxbCommandResponse<?>> responses = commandResponse.getResponses();
		JaxbExceptionResponse exceptionResponse;
		if (responses.size() == 0) {
			System.out.println("Responses size is zero.");
			return;
		} else if (responses.size() == 1) {
			System.out.println("Responses size is one.");
			responseObject = responses.get(0);
			if (responseObject instanceof JaxbExceptionResponse) {
				System.out.println("Resepose is exception.");
				exceptionResponse = (JaxbExceptionResponse) responseObject;
				System.err.println("Exception response from command: "
						+ exceptionResponse.getCommandName() + " message: "
						+ exceptionResponse.getMessage() + " result: "
						+ exceptionResponse.getResult());
			} else {
				System.out.println("Response isn't exception.");
				System.out.println("Response Object: "
						+ responseObject.getResult().toString());
			}
		} else {
			throw new Exception(
					"Unexpected number of results from single command");
		}

	}

}
