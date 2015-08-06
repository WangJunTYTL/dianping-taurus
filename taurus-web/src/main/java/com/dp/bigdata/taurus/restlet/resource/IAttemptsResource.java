package com.dp.bigdata.taurus.restlet.resource;

import java.util.ArrayList;

import com.dp.bigdata.taurus.restlet.shared.AttemptDTO;
import org.restlet.resource.Get;

/**
 * 
 * IAttemptsResource
 * @author damon.zhu
 *
 */
public interface IAttemptsResource {

   @Get
   public ArrayList<AttemptDTO> retrieve();

}
