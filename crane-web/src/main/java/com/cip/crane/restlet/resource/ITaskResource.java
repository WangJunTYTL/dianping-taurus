package com.cip.crane.restlet.resource;

import com.cip.crane.restlet.shared.TaskDTO;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * 
 * ITaskResource
 * @author damon.zhu
 *
 */
public interface ITaskResource {
   
   @Get
   public TaskDTO retrieve();
   
   @Post
   public void update(Representation re);

   @Delete
   public void remove();

}
