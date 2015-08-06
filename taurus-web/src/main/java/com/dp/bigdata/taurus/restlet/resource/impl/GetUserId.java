package com.dp.bigdata.taurus.restlet.resource.impl;

import com.dp.bigdata.taurus.generated.mapper.UserMapper;
import com.dp.bigdata.taurus.restlet.resource.IGetUserId;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by kirinli on 14/11/23.
 */
public class GetUserId  extends ServerResource implements IGetUserId {
    @Autowired
    private UserMapper userMapper;

    @Override
    public int retrieve() {
        String userName = (String) getRequestAttributes().get("userName");
        int userId = userMapper.getUserId(userName);
        return userId;
    }
}
