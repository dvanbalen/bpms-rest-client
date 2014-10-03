package com.redhat.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.consulting.User;

public class BPMSExecuteTest {

	private static Logger LOG = LoggerFactory.getLogger(BPMSExecuteTest.class);

	public static void main(String[] args) throws Exception {
		String processId = "com.redhat.consulting:Workflows:1.0";
		String workflowId = "Workflows.TestProcess";
		Scanner keyboard = null;
		long taskId = 0L;

		try {
			BpmsRestCommandHelper test = new BpmsRestCommandHelper();
			keyboard = new Scanner(System.in);

			// Create user parameter
			User u = new User();
			u.setUsername("mary");

			// Add user parameter as process variable called "user"
			Map<String, Object> params = new HashMap<String, Object>();
			List<String> myList = new ArrayList<>();
			myList.add("fred");
			myList.add("ted");
			params.put("user", "foo");
			params.put("email", "bar");
			params.put("myList", myList);

			// Create task instance
			test.createStartProcessCommand(workflowId, params);
			test.sendBpmsCommands(processId);

			// Delegate task, which is initially assigned to user.username, to
			// user "john"
			System.out.println("Enter task id: ");
			taskId = keyboard.nextLong();
			test.createDelegateTaskCommand(taskId, "mary", "david");
			test.sendBpmsCommands(processId);

		} finally {
			try {
				if (keyboard != null)
					keyboard.close();
			} catch (Throwable t) {
				// ignore
			}
		}

	}
}
