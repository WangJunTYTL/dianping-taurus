package com.cip.crane.springmvc.utils;

import com.cip.crane.zookeeper.common.elect.LeaderElector;
import com.cip.crane.common.Scheduler;
import com.cip.crane.common.structure.BooleanConverter;
import com.cip.crane.common.structure.Converter;
import com.cip.crane.common.lion.AbstractLionPropertyInitializer;
import com.cip.crane.zookeeper.common.infochannel.guice.LeaderElectorChanelModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Author   mingdongli
 * 16/5/10  下午6:33.
 */
public abstract class AbstractAttemptTask extends AbstractLionPropertyInitializer<Boolean> implements InitializingBean {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected LeaderElector leaderElector;

    @Autowired
    protected Scheduler scheduler;

    @Override
    public void afterPropertiesSet() throws Exception {

        super.afterPropertiesSet();
        Injector injector = Guice.createInjector(new LeaderElectorChanelModule());
        leaderElector = injector.getInstance(LeaderElector.class);
    }

    @Override
    protected Boolean getDefaultValue() {
        return false;
    }

    @Override
    protected Converter<Boolean> getConvert() {
        return new BooleanConverter();
    }
}
