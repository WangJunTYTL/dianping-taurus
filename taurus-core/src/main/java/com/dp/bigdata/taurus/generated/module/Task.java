package com.dp.bigdata.taurus.generated.module;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable{
	private static final long serialVersionUID = -2140768961712627228L;

	/**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.taskID
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private String taskid;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.name
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private String name;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.creator
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private String creator;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.dependencyExpr
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private String dependencyexpr;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.addTime
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private Date addtime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.lastScheduleTime
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private Date lastscheduletime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.updateTime
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private Date updatetime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.crontab
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private String crontab;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.status
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private Integer status;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.isAutoKill
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private Boolean isautokill;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.proxyUser
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private String proxyuser;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.waitTimeout
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private Integer waittimeout;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.executionTimeout
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private Integer executiontimeout;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.isAutoRetry
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private Boolean isautoretry;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.retryTimes
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private Integer retrytimes;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.command
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private String command;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.hostName
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private String hostname;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.poolID
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private Integer poolid;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.type
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private String type;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.fileName
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private String filename;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.description
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private String description;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.hadoopName
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private String hadoopname;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TaurusTask.appName
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    private String appname;
    
    /**
     * 是否自动杀死拥塞调度的后续但已过期的调度，默认不杀死
     * This field corresponds to the database column TaurusTask.isKillCongExp
     */
    private Boolean iskillcongexp;

    private Boolean isnotconcurrency;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.taskID
     *
     * @return the value of TaurusTask.taskID
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public String getTaskid() {
        return taskid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.taskID
     *
     * @param taskid the value for TaurusTask.taskID
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.name
     *
     * @return the value of TaurusTask.name
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.name
     *
     * @param name the value for TaurusTask.name
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.creator
     *
     * @return the value of TaurusTask.creator
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public String getCreator() {
        return creator;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.creator
     *
     * @param creator the value for TaurusTask.creator
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.dependencyExpr
     *
     * @return the value of TaurusTask.dependencyExpr
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public String getDependencyexpr() {
        return dependencyexpr;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.dependencyExpr
     *
     * @param dependencyexpr the value for TaurusTask.dependencyExpr
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setDependencyexpr(String dependencyexpr) {
        this.dependencyexpr = dependencyexpr;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.addTime
     *
     * @return the value of TaurusTask.addTime
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public Date getAddtime() {
        return addtime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.addTime
     *
     * @param addtime the value for TaurusTask.addTime
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setAddtime(Date addtime) {
        this.addtime = addtime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.lastScheduleTime
     *
     * @return the value of TaurusTask.lastScheduleTime
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public Date getLastscheduletime() {
        return lastscheduletime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.lastScheduleTime
     *
     * @param lastscheduletime the value for TaurusTask.lastScheduleTime
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setLastscheduletime(Date lastscheduletime) {
        this.lastscheduletime = lastscheduletime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.updateTime
     *
     * @return the value of TaurusTask.updateTime
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public Date getUpdatetime() {
        return updatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.updateTime
     *
     * @param updatetime the value for TaurusTask.updateTime
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.crontab
     *
     * @return the value of TaurusTask.crontab
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public String getCrontab() {
        return crontab;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.crontab
     *
     * @param crontab the value for TaurusTask.crontab
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setCrontab(String crontab) {
        this.crontab = crontab;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.status
     *
     * @return the value of TaurusTask.status
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.status
     *
     * @param status the value for TaurusTask.status
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.isAutoKill
     *
     * @return the value of TaurusTask.isAutoKill
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public Boolean getIsautokill() {
        return isautokill;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.isAutoKill
     *
     * @param isautokill the value for TaurusTask.isAutoKill
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setIsautokill(Boolean isautokill) {
        this.isautokill = isautokill;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.proxyUser
     *
     * @return the value of TaurusTask.proxyUser
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public String getProxyuser() {
        return proxyuser;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.proxyUser
     *
     * @param proxyuser the value for TaurusTask.proxyUser
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setProxyuser(String proxyuser) {
        this.proxyuser = proxyuser;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.waitTimeout
     *
     * @return the value of TaurusTask.waitTimeout
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public Integer getWaittimeout() {
        return waittimeout;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.waitTimeout
     *
     * @param waittimeout the value for TaurusTask.waitTimeout
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setWaittimeout(Integer waittimeout) {
        this.waittimeout = waittimeout;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.executionTimeout
     *
     * @return the value of TaurusTask.executionTimeout
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public Integer getExecutiontimeout() {
        return executiontimeout;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.executionTimeout
     *
     * @param executiontimeout the value for TaurusTask.executionTimeout
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setExecutiontimeout(Integer executiontimeout) {
        this.executiontimeout = executiontimeout;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.isAutoRetry
     *
     * @return the value of TaurusTask.isAutoRetry
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public Boolean getIsautoretry() {
        return isautoretry;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.isAutoRetry
     *
     * @param isautoretry the value for TaurusTask.isAutoRetry
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setIsautoretry(Boolean isautoretry) {
        this.isautoretry = isautoretry;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.retryTimes
     *
     * @return the value of TaurusTask.retryTimes
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public Integer getRetrytimes() {
        return retrytimes;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.retryTimes
     *
     * @param retrytimes the value for TaurusTask.retryTimes
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setRetrytimes(Integer retrytimes) {
        this.retrytimes = retrytimes;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.command
     *
     * @return the value of TaurusTask.command
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public String getCommand() {
        return command;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.command
     *
     * @param command the value for TaurusTask.command
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.hostName
     *
     * @return the value of TaurusTask.hostName
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.hostName
     *
     * @param hostname the value for TaurusTask.hostName
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.poolID
     *
     * @return the value of TaurusTask.poolID
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public Integer getPoolid() {
        return poolid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.poolID
     *
     * @param poolid the value for TaurusTask.poolID
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setPoolid(Integer poolid) {
        this.poolid = poolid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.type
     *
     * @return the value of TaurusTask.type
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public String getType() {
        return type;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.type
     *
     * @param type the value for TaurusTask.type
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.fileName
     *
     * @return the value of TaurusTask.fileName
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public String getFilename() {
        return filename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.fileName
     *
     * @param filename the value for TaurusTask.fileName
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.description
     *
     * @return the value of TaurusTask.description
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public String getDescription() {
        return description;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.description
     *
     * @param description the value for TaurusTask.description
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.hadoopName
     *
     * @return the value of TaurusTask.hadoopName
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public String getHadoopname() {
        return hadoopname;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.hadoopName
     *
     * @param hadoopname the value for TaurusTask.hadoopName
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setHadoopname(String hadoopname) {
        this.hadoopname = hadoopname;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TaurusTask.appName
     *
     * @return the value of TaurusTask.appName
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public String getAppname() {
        return appname;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TaurusTask.appName
     *
     * @param appname the value for TaurusTask.appName
     *
     * @mbggenerated Thu May 15 16:53:11 HKT 2014
     */
    public void setAppname(String appname) {
        this.appname = appname;
    }

	public Boolean getIskillcongexp() {
		return iskillcongexp;
	}

	public void setIskillcongexp(Boolean iskillcongexp) {
		this.iskillcongexp = iskillcongexp;
	}

    public Boolean getIsnotconcurrency() {
		return isnotconcurrency;
	}

	public void setIsnotconcurrency(Boolean isnotconcurrency) {
		this.isnotconcurrency = isnotconcurrency;
	}


}