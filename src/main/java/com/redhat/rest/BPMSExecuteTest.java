package com.redhat.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.kie.api.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.consulting.User;

public class BPMSExecuteTest {

	private static Logger LOG = LoggerFactory.getLogger(BPMSExecuteTest.class);

	public static void main(String[] args) throws Exception {
		Response response = null;
		String processId = "com.redhat.consulting:Workflows:1.0";
		String workflowId = "Workflows.TaskAssignment";
		List<Command> cmds = new ArrayList<>();
		Scanner keyboard = null;
		long taskId = 0L;

		try {
			BpmsRestCommandHelper test = new BpmsRestCommandHelper();
			keyboard = new Scanner(System.in);

			// Create user parameter
			User u = new User();
			u.setUsername("mary");

			// Add User class to JAXB context for remote command client
			Set<Class<?>> extraJaxbClassList = new HashSet<>();
			extraJaxbClassList.add(u.getClass());

			// Add user parameter as process variable called "user"
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("user", u);

			// Create task instance
			cmds.add(test.createStartProcessCommand(workflowId, params));
			response = test.sendBpmsCommands(cmds, processId,
					extraJaxbClassList);
			System.out.println("Status: " + response.getStatus()+" class: "+response.getClass().getName());
			test.processJaxbCommandResponse(response);

			// Clear out command list
			cmds.clear();

			// Delegate task, which is initially assigned to user.username, to
			// user "john"
			System.out.println("Enter task id: ");
			taskId = keyboard.nextLong();
			cmds.add(test.createDelegateTaskCommand(taskId, "mary", "john"));
			response = test.sendBpmsCommands(cmds, processId,
					extraJaxbClassList);
			System.out.println("Status: " + response.getStatus());
			test.processJaxbCommandResponse(response);

		} finally {
			try {
				if (response != null)
					response.close();
			} catch (Throwable t) {
				// ignore
			}
			try {
				if(keyboard != null)
					keyboard.close();
			} catch(Throwable t) {
				// ignore
			}
		}

	}
}
