package com.cip.crane.restlet.resource.impl;

import com.cip.crane.generated.mapper.TaskAttemptMapper;
import com.cip.crane.restlet.resource.IGroupTasks;
import com.cip.crane.generated.module.GroupTaskExample;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

/**
 * Created by kirinli on 14-10-11.
 */
public class GroupTasks extends ServerResource implements IGroupTasks {
    @Autowired
    private TaskAttemptMapper taskAttemptMapper;

    @Override
    public String retrieve() {
        JSONArray jsonData = new JSONArray();
        try {
            String user = (String) getRequestAttributes().get("username");
            String start = (String) getRequestAttributes().get("starttime");
            String end = (String) getRequestAttributes().get("endtime");
            int[] successStatus = {7};
            int[] failedStatus = {8};
            int[] killStatus = {10};
            int[] timeoutStatus = {9};
            int[] congestStatus = {2};

            ArrayList<GroupTaskExample> successTasks = taskAttemptMapper.getGroupTasks(user, start, end,successStatus);
            ArrayList<GroupTaskExample> failedTasks = taskAttemptMapper.getGroupTasks(user,start, end,failedStatus);
            ArrayList<GroupTaskExample> killTasks = taskAttemptMapper.getGroupTasks(user,start, end,killStatus);
            ArrayList<GroupTaskExample> timeoutTasks = taskAttemptMapper.getGroupTasks(user,start, end,timeoutStatus);
            ArrayList<GroupTaskExample> congestTasks = taskAttemptMapper.getGroupTasks(user,start, end,congestStatus);

            for (GroupTaskExample task : successTasks) {

                JSONObject json = new JSONObject();
                json.put("taskName", task.getName());
                json.put("taskId", task.getTaskID());
                json.put("nums", task.getNum());
                json.put("status", "success");
                json.put("creator", task.getCreator());
                jsonData.put(json);


            }
            for (GroupTaskExample task : failedTasks) {
                if (task.getNum() == null){
                    break;
                }

                JSONObject json = new JSONObject();
                json.put("taskName", task.getName());
                json.put("taskId", task.getTaskID());
                json.put("nums", task.getNum());
                json.put("status", "failed");
                json.put("creator", task.getCreator());
                jsonData.put(json);


            }

            for (GroupTaskExample task : killTasks) {
                if (task.getNum() == null){
                    break;
                }

                JSONObject json = new JSONObject();
                json.put("taskName", task.getName());
                json.put("taskId", task.getTaskID());
                json.put("nums", task.getNum());
                json.put("status", "killed");
                json.put("creator", task.getCreator());
                jsonData.put(json);


            }

            for (GroupTaskExample task : timeoutTasks) {
                if (task.getNum() == null){
                    break;
                }

                JSONObject json = new JSONObject();
                json.put("taskName", task.getName());
                json.put("taskId", task.getTaskID());
                json.put("nums", task.getNum());
                json.put("status", "timeout");
                json.put("creator", task.getCreator());
                jsonData.put(json);


            }

            for (GroupTaskExample task : congestTasks) {
                if (task.getNum() == null){
                    break;
                }

                JSONObject json = new JSONObject();
                json.put("taskName", task.getName());
                json.put("taskId", task.getTaskID());
                json.put("nums", task.getNum());
                json.put("status", "congest");
                json.put("creator", task.getCreator());
                jsonData.put(json);


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonData.toString();
    }
}
